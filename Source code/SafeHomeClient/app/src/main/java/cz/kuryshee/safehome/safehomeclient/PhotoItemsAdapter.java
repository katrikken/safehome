package cz.kuryshee.safehome.safehomeclient;

import android.app.Application;
import android.support.v7.widget.RecyclerView;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class implements custom adapter for a {@link RecyclerView} to display photos from Raspberry Pi.
 *
 * @see RecyclerView.Adapter
 * @author Ekaterina Kurysheva
 */
public class PhotoItemsAdapter extends RecyclerView.Adapter<PhotoItemsAdapter.InfoViewHolder> {

    private List<PhotoItem> photoList;
    private Application application;

    /**
     * Constructor. Initializes reading known data from cache.
     * @param application context.
     */
    public PhotoItemsAdapter(Application application){
        photoList = new ArrayList<>();
        this.application = application;

        getPhotosFromCache();
    }

    /**
     * Method extracts photo items from cache.
     */
    private void getPhotosFromCache(){
        LruCache<String, PhotoItem> cache = ((GlobalVariables) application).getPictureMemoryCache();

        if(cache == null){
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            final int cacheSize = maxMemory / 2;

            ((GlobalVariables) application).setPictureMemoryCache(new LruCache<String, PhotoItem>(cacheSize));
        }
        else{
            Set<String> keySet = cache.snapshot().keySet();

            for(String key : keySet){
                photoList.add(cache.get(key));
            }

            Collections.sort(photoList, new PhotoItemComparator());
        }

        this.notifyDataSetChanged();
    }

    /**
     * @see RecyclerView.Adapter#onCreateViewHolder(ViewGroup, int)
     */
    @Override
    public PhotoItemsAdapter.InfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gallery_item_row, parent, false);

        return new PhotoItemsAdapter.InfoViewHolder(itemView);
    }



    /**
     * Checks whether {@link PhotoItem} in list are present in cache or storage.
     * @param items to check
     * @return list of images which are not present.
     */
    public List<PhotoItem> filterToDownload(List<PhotoItem> items){
        List<PhotoItem> toLoad = new ArrayList<>();

        for(PhotoItem item: items){
            boolean exists = false;
            for(PhotoItem existing: photoList){
                if(existing.getDate().equals(item.getDate())){
                    exists = true;
                    break;
                }
            }
            if(!exists){
                toLoad.add(item);
            }
        }

        return toLoad;
    }

    /**
     * Adds photo to {@link GlobalVariables#pictureMemoryCache} and to data set.
     * Saves it to external storage.
     * @param photo {@link PhotoItem} to add.
     */
    public void addPhoto(PhotoItem photo){
        photoList.add(photo);
        Collections.sort(photoList, new PhotoItemComparator());
        ((GlobalVariables) application).getPictureMemoryCache().put(photo.getStringUnixEpochDate(), photo);
        this.notifyDataSetChanged();
    }

    /**
     * @return availabale {@link PhotoItem} in a list.
     */
    public List<PhotoItem> getPhotoItems(){
        return photoList;
    }

    /**
     * @return oldest known photo in data set.
     */
    public String getOldestTimestamp(){
        String timestamp = photoList.get(photoList.size() - 1).getStringUnixEpochDate();
        Logger.getLogger(PhotoItemsAdapter.class.getName()).log(Level.INFO, "Oldest timestamp: " + timestamp);
        return timestamp;
    }

    /**
     * @see RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int)
     */
    @Override
    public void onBindViewHolder(PhotoItemsAdapter.InfoViewHolder holder, int position) {
        PhotoItem item = photoList.get(position);
        holder.date.setText(item.getDateStringGUI());
        holder.photo.setImageBitmap(item.getImageBitmap());
    }

    /**
     * @see RecyclerView.Adapter#getItemCount()
     */
    @Override
    public int getItemCount() {
        return photoList.size();
    }

    /**
     * Inner class implements custom {@link RecyclerView.ViewHolder} for displaying photos.
     */
    public class InfoViewHolder extends RecyclerView.ViewHolder {
        /**
         * Property holding {@link ImageView} for the photo.
         */
        public ImageView photo;
        /**
         * Property holding {@link TextView} for the photo description.
         */
        public TextView date;

        /**
         * Constructor.
         * @param view is the container.
         */
        public InfoViewHolder(View view) {
            super(view);
            date = (TextView) view.findViewById(R.id.photo_date);
            photo = (ImageView) view.findViewById(R.id.photo);
        }
    }
}
