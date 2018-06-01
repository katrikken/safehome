package cz.kuryshee.safehome.safehomeclient;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.LruCache;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.kuryshee.safehome.appcommunicationconsts.AppCommunicationConsts;
import com.kuryshee.safehome.httprequestsender.FormUploader;
import com.kuryshee.safehome.httprequestsender.GetRequestSender;
import com.kuryshee.safehome.requestdataretriever.GetDataRetriever;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;

/**
 * Activity prepares the application for usage.
 * It pings the server and reads the configuration data.
 *
 * @author Ekaterina Kurysheva
 */
public class MainActivity extends AppCompatActivity {

    private Application application;
    private static final String TAG = "update_events";

    ImageView imageView;
    ProgressBar progressBar;
    TextView textView;
    Button button;

    /**
     * Prepares view and application environment.
     *
     * @see AppCompatActivity#onCreate(Bundle, PersistableBundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        application = this.getApplication();

        imageView = (ImageView) findViewById(R.id.main_error);
        progressBar = (ProgressBar) findViewById(R.id.main_progress);
        textView = (TextView) findViewById(R.id.main_error_text);
        button = (Button) findViewById(R.id.main_button_try);

        button.setOnClickListener(new View.OnClickListener() {
            /**
             * Validates input and starts the asynchronous task of authorization.
             *
             * @see View.OnClickListener#onClick(View).
             */
            @Override
            public void onClick(View view) {
                setNormalState();
                checkConnectionToServer();
            }
        });

        setNormalState();

        setStringCache();

        readCache();

        if(((GlobalVariables) application).getServerPath() == null){
            readConfigurations();
        }

        checkConnectionToServer();

        setSynchronizationWithServer();
    }

    /**
     * Schedules synchronization with server task.
     */
    private void setSynchronizationWithServer(){
        GcmNetworkManager gcmNetworkManager = GcmNetworkManager.getInstance(this);
        PeriodicTask task = new PeriodicTask.Builder()
                .setService(SynchronizeTaskService.class)
                .setTag(TAG)
                .setPeriod(30)
                .setFlex(10)
                .setPersisted(true)
                .setUpdateCurrent(true)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .setRequiresCharging(false)
                .build();

        gcmNetworkManager.schedule(task);
    }

    /**
     * Sets the view to show an error.
     */
    private void setErrorState(){
        Logger.getLogger(MainActivity.class.getName()).log(Level.INFO, "Setting error layout");

        progressBar.setVisibility(View.INVISIBLE);

        textView.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.VISIBLE);
        button.setVisibility(View.VISIBLE);

    }

    /**
     * Sets the view for the normal state layout.
     */
    private void setNormalState(){
        Logger.getLogger(MainActivity.class.getName()).log(Level.INFO, "Setting normal layout");
        textView.setVisibility(View.INVISIBLE);
        imageView.setVisibility(View.INVISIBLE);
        button.setVisibility(View.INVISIBLE);

        progressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Writes empty JSON object to cache file.
     */
    private void writeEmptyCache(File file){
        String braces = "{}";
        FileOutputStream fs = null;
        try {
            fs = new FileOutputStream(file, false);
            fs.write(braces.getBytes());
        } catch(Exception e){
            Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }
        finally {
            try {
                fs.close();
            }
            catch(Exception e) {
                Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    /**
     * Reads configurations from cache, if they exist.
     */
    private void readCache(){
        File file = new File(getApplicationContext().getCacheDir(), AppConstants.APP_CACHE);
        try {
            if(!file.exists()){
                file.createNewFile();
                writeEmptyCache(file);
            }
            else {
                InputStream fs = null;
                JsonReader reader = null;
                try {
                    fs = new FileInputStream(file);
                    reader = Json.createReaderFactory(null).createReader(fs,  StandardCharsets.UTF_8);
                    JsonObject object = reader.readObject();

                    Set<String> keySet = object.keySet();
                    if(keySet.contains(AppConstants.SERVER_ADDRESS) && ((GlobalVariables) application).getServerPath() == null){
                        ((GlobalVariables) application).setServerPath(object.getString(AppConstants.SERVER_ADDRESS));
                    }
                    if(keySet.contains(AppConstants.TOKEN) && ((GlobalVariables) application).getToken() == null){
                        ((GlobalVariables) application).setToken(object.getString(AppConstants.TOKEN));
                    }
                }
                catch(Exception e) {
                    Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, e.getMessage(), e);
                }
                finally {
                    try {
                        reader.close();
                        fs.close();
                    }
                    catch(Exception e) {
                        Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, e.getMessage(), e);
                    }
                }
            }

        } catch (Exception e) {
            Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * Loads server address and application mode from assets.
     */
    private void readConfigurations(){
        AssetManager assetManager = this.getAssets();
        InputStream is = null;

        try{
            is = assetManager.open(AppConstants.CONFIG);
            JsonReader jr = Json.createReader(is);

            JsonObject json = jr.readObject();

            String debugMode = json.getString(AppConstants.DEBUG_MODE);

            if(debugMode.equals(AppConstants.YES)){
                ((GlobalVariables) application).setDebugMode(true);
            }
            else {
                String serverPath = json.getString(AppConstants.SERVER_ADDRESS);

                ((GlobalVariables) application).setServerPath(serverPath);
                ((GlobalVariables) application).setDebugMode(false);
            }
        }
        catch (Exception e){
            Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, e.getMessage(), e);

            setErrorState();
        }
        finally {
            if(is != null){
                try{
                    is.close();
                }
                catch (IOException e){
                    Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Starts the {@link SetServerActivity}.
     */
    private void redirectToSetServerActivity(){
        Intent intent = new Intent(this, SetServerActivity.class);
        startActivity(intent);
    }

    /**
     * Starts the {@link AuthorizationActivity}.
     */
    private void redirectToAuthorizationActivity(){
        Intent intent = new Intent(this, AuthorizationActivity.class);
        startActivity(intent);
    }

    /**
     * Starts the {@link RpiControlActivity}.
     */
    private void redirectToRpiControlActivity(){
        Intent intent = new Intent(this, RpiControlActivity.class);
        startActivity(intent);
    }

    /**
     * Executes the asynchronous task {@link InitConversation}.
     */
    private void checkConnectionToServer(){
        new InitConversation().execute();
    }

    /**
     * Tries to get the authorization token from cache memory and if not succeeded, navigates to the {@link AuthorizationActivity}.
     * Validates the token via server, if present.
     */
    private void checkAuthorization(){
        String userToken = null;
        if(((GlobalVariables) application).getToken() != null){
            userToken = ((GlobalVariables) application).getToken();
        }
        else{
            userToken = ((GlobalVariables) application).getStringMemoryCache().get(AppCommunicationConsts.TOKEN);
        }

        if (userToken != null && userToken.length() != 0){
            new CheckToken().execute(userToken);
        }
        else{
            redirectToAuthorizationActivity();
        }
    }

    /**
     * Sets the application string cache.
     */
    private void setStringCache(){
        if(((GlobalVariables) application).getStringMemoryCache() == null){
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            final int cacheSize = maxMemory / 16;

            ((GlobalVariables) application).setStringMemoryCache(new LruCache<String, String>(cacheSize));
        }
    }

    /**
     * Implements the asynchronous call to the server in order to check its availability.
     */
    private class InitConversation extends AsyncTask<Void, Void, String> {

        /**
         * Sends ping to server.
         * @param params
         * @return server answer.
         */
        @Override
        protected String doInBackground(Void... params){

            GetRequestSender sender = new GetRequestSender();

            StringBuilder query = new StringBuilder();
            query.append(((GlobalVariables) application).getServerPath()).append('?').append(AppCommunicationConsts.ACTION).append('=').append(AppCommunicationConsts.PING); //?action=ping
            byte[] input = sender.connect(query.toString(), null);

            return new GetDataRetriever().getStringData(input);
        }

        /**
         * Reacts to the server answer.
         * Redirects to {@link SetServerActivity} in case of debug mode.
         * @param result
         */
        @Override
        protected void onPostExecute(String result){
            Logger.getLogger(InitConversation.class.getName()).log(Level.INFO, "Got result: " + result);

            if (result != null && result.equals(AppCommunicationConsts.PONG)){
                checkAuthorization();
            }
            else{
                if(((GlobalVariables) application).isDebugMode()){
                    Toast toast = Toast.makeText(application, getResources().getString(R.string.invalid_input), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();

                    redirectToSetServerActivity();
                }
                else{
                    setErrorState();
                }
            }
        }
    }

    /**
     * Sets the token to the application variable.
     * @param token
     */
    private void setValidToken(String token){
        ((GlobalVariables) application).setToken(token);

        JsonObject object  = Json.createObjectBuilder()
                .add(AppConstants.SERVER_ADDRESS, ((GlobalVariables) application).getServerPath())
                .add(AppConstants.TOKEN, token)
                .build();

        writeToCache(getApplicationContext(), AppConstants.APP_CACHE, object);
    }

    /**
     * Writes application info to cache.
     * @param context
     * @param filename
     */
    private void writeToCache(Context context, String filename, JsonObject object) {
        File file = new File(context.getCacheDir(), filename);

        OutputStream output = null;
        JsonWriter jw = null;
        try {
            output = new FileOutputStream(file, false);
            jw = Json.createWriter(output);

            jw.writeObject(object);
        }
        catch(Exception e) {
            Logger.getLogger("test").log(Level.SEVERE, e.getMessage(), e);
        }
        finally {
            try {
                jw.close();
                output.close();
            }
            catch(Exception e) {
                Logger.getLogger("test").log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    /**
     * Implements the asynchronous call to the server in order to validate the authorization token.
     */
    private class CheckToken extends AsyncTask<String, Void, String> {

        String token = null;

        /**
         * Sends request for token validation.
         * @param params
         * @return server answer.
         */
        @Override
        protected String doInBackground(String... params){

            if(params.length > 0){
                try {
                    this.token = params[0];

                    FormUploader formUploader = new FormUploader(((GlobalVariables) application).getServerPath(), null);
                    formUploader.addHeader(AppCommunicationConsts.TOKEN, params[0]);
                    formUploader.connect();

                    formUploader.addFormField(AppCommunicationConsts.ACTION, AppCommunicationConsts.VALIDATE);

                    return formUploader.finish();
                }
                catch (IOException e){
                    Logger.getLogger(CheckToken.class.getName()).log(Level.SEVERE, e.getMessage(), e);
                }
            }

            return null;
        }

        /**
         * Interprets server answer.
         * @param result server answer.
         */
        @Override
        protected void onPostExecute(String result){
            if(result == null){
                redirectToAuthorizationActivity();
            }
            else{
                if(result.equals(AppCommunicationConsts.TRUE)){
                    setValidToken(token);
                    redirectToRpiControlActivity();
                }
                else if(result.equals(AppCommunicationConsts.FALSE)){
                    redirectToAuthorizationActivity();
                }
                else{
                    setErrorState();
                }
            }
        }
    }
}
