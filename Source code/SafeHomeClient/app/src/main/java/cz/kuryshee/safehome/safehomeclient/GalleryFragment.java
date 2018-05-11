package cz.kuryshee.safehome.safehomeclient;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kuryshee.safehome.appcommunicationconsts.AppCommunicationConsts;
import com.kuryshee.safehome.httprequestsender.AnswerConstants;
import com.kuryshee.safehome.httprequestsender.GetRequestSender;
import com.kuryshee.safehome.requestdataretriever.GetDataRetriever;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.JsonArray;
import javax.json.JsonObject;

/**
 * Class implements Fragment with photos from Raspberry Pi.
 *
 * @author Ekaterina Kurysheva.
 */
public class GalleryFragment extends Fragment {

    private Application application;
    private PhotoItemsAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private String userToken;

    /**
     * Sets the view and listeners.
     * @see Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        this.application = this.getActivity().getApplication();
        this.userToken = ((GlobalVariables) application).getToken();

        if (userToken == null || userToken.length() == 0) {
            redirectToAuthorizationActivity();
        }
        else {
            adapter = new PhotoItemsAdapter(application);

            recyclerView = (RecyclerView) view.findViewById(R.id.gallery_recycler_view);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getActivity().getApplicationContext());
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(adapter);

            adapter.notifyDataSetChanged();

            swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.gallery_fragment);
            final SwipeRefreshLayout.OnRefreshListener listener = new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    checkExtStorageReady();
                    swipeRefreshLayout.setRefreshing(true);
                    new GetPhotos().execute(userToken);
                }
            };

            swipeRefreshLayout.setOnRefreshListener(listener);

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    if (!recyclerView.canScrollVertically(-1)) { //If you are at the bottom of the list
                        new GetPhotos().execute(userToken, adapter.getOldestTimestamp());
                    }
                }
            });

            swipeRefreshLayout.post(new Runnable() {
                @Override public void run() {
                    listener.onRefresh();
                }
            });
        }

        return view;
    }

    /**
     * Checks if external storage is available for read and write
     * @return true if the storage is available.
     * */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Logger.getLogger(GalleryFragment.class.getName()).log(Level.INFO, "External storage is available");
            return true;
        }
        return false;
    }

    /**
     * Checks if external storage is available to at least read.
     * @return true if the storage is available.
     * */
    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Gets the directory to write the file into.
     * @param albumName
     * @return {@link File}
     */
    private File getPublicAlbumStorageDir(String albumName) {
        File file = null;
        if(checkExtStorageReady()) {
            // Get the directory for the user's public pictures directory.
             file = new File(Environment.getExternalStorageDirectory(), albumName);
            if (!file.exists()) {
                if(!file.mkdirs()) {
                    Logger.getLogger(GalleryFragment.class.getName()).log(Level.WARNING, "Directory not created.");
                }
                else{
                    Logger.getLogger(GalleryFragment.class.getName()).log(Level.WARNING, "Directory was created: " + file.getAbsolutePath());
                }
            }
            else{
                Logger.getLogger(GalleryFragment.class.getName()).log(Level.WARNING, "Directory was created: " + file.getAbsolutePath());
            }
        }
        return file;
    }

    /**
     * Checks whether the application has permission to write to external storage.
     * @return true if it has the permission.
     */
    private boolean isPermissionGranted(){
        if (ContextCompat.checkSelfPermission(this.getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return false;

        } else {
            // Permission has already been granted
            return true;
        }
    }

    /**
     * Asks for permission to write to external storage.
     */
    private void requestPermission(){
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                AppConstants.MY_PERMISSIONS_REQUEST_WRITE_EXT_STORAGE);
    }

    /**
     * Tries to save cached files to the storage, if permission was granted.
     *
     * @see Fragment#onRequestPermissionsResult(int, String[], int[])
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case AppConstants.MY_PERMISSIONS_REQUEST_WRITE_EXT_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Logger.getLogger(GalleryFragment.class.getName()).log(Level.INFO, "Permission granted");
                    saveDownloadedFiles();
                } else {
                    Logger.getLogger(GalleryFragment.class.getName()).log(Level.INFO, "Permission denied");
                }
            }
        }
    }

    /**
     * Tries to save cached images to external storage.
     */
    private void saveDownloadedFiles(){
        List<PhotoItem> list = adapter.getPhotoItems();
        for( PhotoItem item: list){
            saveToStorage(item.getImageBitmap(), item.getName());
        }
    }

    /**
     * Manages the permission to write to external storage.
     * @return true, if permission is granted.
     */
    private boolean checkExtStorageReady(){
        if(isPermissionGranted()){
            return true;
        }
        else{
            Logger.getLogger(GalleryFragment.class.getName()).log(Level.INFO, "Doesn't have permission");
            requestPermission();
        }

        return false;
    }

    /**
     * Saves the image to external storage.
     * @param bitmapImage image
     * @param name of the image.
     */
    private void saveToStorage(Bitmap bitmapImage, String name){
        if(checkExtStorageReady() && isExternalStorageWritable() && bitmapImage != null){
            File directory = getPublicAlbumStorageDir(getResources().getString(R.string.image_dir));
            if(directory != null) {
                // Create imageDir
                File mypath = new File(directory, name);
                if(!mypath.exists()) {
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(mypath);
                        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        fos.flush();
                        Logger.getLogger(GalleryFragment.class.getName()).log(Level.INFO, "File was successfully written to: " + mypath.getAbsolutePath());

                        MediaStore.Images.Media.insertImage(this.getActivity().getContentResolver(), mypath.getAbsolutePath(), mypath.getName(), mypath.getName());

                    } catch (Exception e) {
                        Logger.getLogger(GalleryFragment.class.getName()).log(Level.SEVERE, e.getMessage(), e);
                    } finally {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            Logger.getLogger(GalleryFragment.class.getName()).log(Level.SEVERE, e.getMessage(), e);
                        }
                    }
                }
                else{
                    Logger.getLogger(GalleryFragment.class.getName()).log(Level.INFO, "File already exists");
                }
            }
            else{
                Logger.getLogger(GalleryFragment.class.getName()).log(Level.INFO, "Could get directory to write file");
            }
        }
        else{
            Logger.getLogger(GalleryFragment.class.getName()).log(Level.INFO, "Could not save file");
        }
    }

    /**
     * Checks whether the given image exists in storage.
     * @param directory to image
     * @param item to fill the bitmap to.
     */
    private void loadImageFromStorage(File directory, PhotoItem item) {
        if(isPermissionGranted() && isExternalStorageReadable() && directory != null) {
            try {
                File f = new File(directory, item.getName());
                if (f.exists() && f.canRead()) {
                    Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
                    if(b != null && b.getByteCount() > 0) {
                        item.setImageBitmap(b);
                    }
                }
            } catch (FileNotFoundException e) {
                Logger.getLogger(PhotoItemsAdapter.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }
        }
        else{
            Logger.getLogger(GalleryFragment.class.getName()).log(Level.INFO, "Could not check storage yet.");
        }
    }

    /**
     * Starts the {@link AuthorizationActivity}.
     */
    private void redirectToAuthorizationActivity(){
        Intent intent = new Intent(application, AuthorizationActivity.class);
        startActivity(intent);
    }

    /**
     * Loads recent photo updates from server.
     */
    private class GetPhotos extends AsyncTask<String, Void, Integer> {

        String token = null;

        /**
         * Parses photo identifiers from the server answer.
         * @param array {@link JsonArray}
         * @return list of {@link PhotoItem} without bitmaps.
         * @throws ParseException
         */
        private List<PhotoItem> parsePhotoIds(JsonArray array) throws ParseException{
            List<PhotoItem> list = new ArrayList<>();
            for( int i = 0; i < array.size(); i++){
                PhotoItem item = new PhotoItem();
                JsonObject object = array.getJsonObject(i);

                item.setDateFromDateFormatString(object.getString(AppConstants.JSON_TIME_PARAM));
                item.setName(object.getString(AppConstants.JSON_NAME_PARAM));

                list.add(item);
            }

            return list;
        }

        /**
         * Gets the list of photo identifiers.
         * @param time is the latest time of taken photo known to the application.
         * @return list of {@link PhotoItem} without bitmaps.
         * @throws ParseException
         */
        private List<PhotoItem> getListOfTimestamps(String time) throws ParseException{
            GetRequestSender sender = new GetRequestSender();
            StringBuilder query = new StringBuilder(((GlobalVariables) application).getServerPath());
            query.append('?')
                    .append(AppCommunicationConsts.ACTION).append('=').append(AppCommunicationConsts.GET_PHOTO_IDS).append("&")
                    .append(AppCommunicationConsts.TIME).append("=").append(time);
            sender.setToken(token);
            byte[] input = sender.connect( query.toString(), null);

            JsonArray array = new GetDataRetriever().getJsonArray(input);
            return adapter.filterToDownload(parsePhotoIds(array)); //get list of photos which are neither in IS nor cache
        }

        /**
         * Loads bitmaps of the photos from server.
         * @param toLoad is a list of photo identifiers to load.
         * @return number of loaded images.
         */
        private int loadPhotos(List<PhotoItem> toLoad){
            Logger.getLogger(GetPhotos.class.getName() + ".loadPhotos").log(Level.INFO, "Number of photos to load: " + toLoad.size());
            int countAdded = 0;
            for(PhotoItem item: toLoad){ //fetch that photos

                loadImageFromStorage( //try to find it in storage
                        getPublicAlbumStorageDir(getResources().getString(R.string.image_dir)),
                        item);
                if(item.getImageBitmap() != null && item.getImageBitmap().getByteCount() > 0){
                    Logger.getLogger(GetPhotos.class.getName() + ".loadPhotos").log(Level.INFO, "Found data in storage");
                    adapter.addPhoto(item);
                    countAdded++;
                }
                else{
                    GetRequestSender sender = new GetRequestSender();
                    StringBuilder query = new StringBuilder(((GlobalVariables) application).getServerPath());
                    query.append('?')
                            .append(AppCommunicationConsts.ACTION).append('=').append( AppCommunicationConsts.GET_PHOTO).append("&")
                            .append(AppCommunicationConsts.TIME).append("=").append(item.getStringUnixEpochDate());
                    sender.setToken(token);
                    byte[] input = sender.connect( query.toString(), null);
                    if(input != null && input.length > 0) {
                        item.setImageBitmap(input);
                        adapter.addPhoto(item);
                        saveToStorage(item.getImageBitmap(), item.getName());
                        countAdded++;
                    }
                    else{
                        Logger.getLogger(GetPhotos.class.getName() + ".loadPhotos").log(Level.WARNING, "Could not load data: " + item.getStringUnixEpochDate());
                    }
                }
            }

            return countAdded;
        }

        /**
         * Gets the date of the latest photo registered on Raspberry Pi.
         * @return answer from server.
         */
        private String getLatestDate(){
            GetRequestSender sender = new GetRequestSender();
            StringBuilder query = new StringBuilder(((GlobalVariables) application).getServerPath());
            query.append('?').append(AppCommunicationConsts.ACTION).append('=').append( AppCommunicationConsts.GET_LATEST_PHOTO_TIME);
            sender.setToken(token);
            byte[] input = sender.connect( query.toString(), null);
            GetDataRetriever retriever = new GetDataRetriever();
            return retriever.getStringData(input);
        }

        /**
         * Loads the latest dates of photos made on Raspberry Pi.
         * @param params expects the identity token on 0 position, timestamp of the latest known photo on 1 position (optional).
         * @return number of loaded photos.
         */
        @Override
        protected Integer doInBackground(String... params){
            int countAdded = 0;

            if(params.length > 0){
                try {
                    this.token = params[0];

                    String strdate;

                    if(params.length == 2){
                        strdate = params[1];
                    }
                    else{
                        //Get latest registered date of photo
                        strdate = getLatestDate();
                        if(strdate.length() == 0 || strdate == null ||
                                strdate.equals(AppCommunicationConsts.ERROR) ||
                                strdate.equals(AppCommunicationConsts.INVALID_USER_ERROR) ||
                                strdate.equals(AppCommunicationConsts.REQUEST_FORMAT_ERROR) ||
                                strdate.equals(AppCommunicationConsts.REQUEST_PROCESS_ERROR) ||
                                strdate.equals(AnswerConstants.NO_ANSWER)){

                            Logger.getLogger(GetPhotos.class.getName() + ".doInBackground").log(Level.WARNING, "Could not load data: " + strdate);
                            return countAdded;
                        }

                        //Check if the photo is cached
                        LruCache<String, PhotoItem> cache = ((GlobalVariables) application).getPictureMemoryCache();
                        if(cache.get(strdate) != null){ //if the latest photos are present, don't load anything.
                            Logger.getLogger(GetPhotos.class.getName() + ".doInBackground").log(Level.INFO, "Photo is cached: " + strdate);
                            return countAdded;
                        }
                    }

                    Logger.getLogger(GetPhotos.class.getName() + ".doInBackground").log(Level.INFO, "Latest photo to load: " + strdate);

                    //get list of photos which are neither in IS nor cache
                    List<PhotoItem> toLoad = getListOfTimestamps(strdate);
                    //load those photos
                    countAdded += loadPhotos(toLoad);
                }
                catch (Exception e){
                    Logger.getLogger(GetPhotos.class.getName() + ".doInBackground").log(Level.SEVERE, e.getMessage());
                }
            }

            return countAdded;
        }

        /**
         * Passes the result to the view adapter.
         * @param result
         */
        @Override
        protected void onPostExecute(Integer result){
            if (result == 0){
                Logger.getLogger(GetPhotos.class.getName() + ".onPostExecute").log(Level.INFO, "No photos were loaded");
            }
            else{
                Logger.getLogger(GetPhotos.class.getName() + ".onPostExecute").log(Level.INFO, "Loaded photos: " + result);
            }
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
