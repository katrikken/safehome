package cz.kuryshee.safehome.safehomeclient;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.LruCache;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.kuryshee.safehome.appcommunicationconsts.AppCommunicationConsts;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 * Class implements Service, which performs synchronizations with the server, when the application is closed.
 *
 * @see GcmTaskService
 * @author Ekaterina Kurysheva
 */
public class SynchronizeTaskService extends GcmTaskService {

    private Application application;
    private String userToken;
    private int taskResult = GcmNetworkManager.RESULT_FAILURE;

    /**
     * Tries to initialize synchronization with the server.
     *
     * @see GcmTaskService#onRunTask(TaskParams)
     */
    @Override
    public int onRunTask(TaskParams taskParams) {
        Logger.getLogger(SynchronizeTaskService.class.getName()).log(Level.INFO, "Starting Sync task");
        application = getApplication();

        readCache();

        userToken = ((GlobalVariables) application).getToken();

        if (userToken != null && userToken.length() != 0) {
            Logger.getLogger(SynchronizeTaskService.class.getName()).log(Level.INFO, "Trying to load");
            try {
                synchronizeEvents(userToken);
            }
            catch (Exception e){
                Logger.getLogger(SynchronizeTaskService.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }
        }

        Logger.getLogger(SynchronizeTaskService.class.getName()).log(Level.INFO, "Sync task finished");

        return taskResult;
    }

    /**
     * Reads configurations from cache, if they exist.
     */
    private void readCache(){
        File file = new File(getApplicationContext().getCacheDir(), AppConstants.APP_CACHE);
        try {
            if(file.exists()){
                InputStream fs = null;
                JsonReader reader = null;
                try {
                    fs = new FileInputStream(file);
                    reader = Json.createReaderFactory(null).createReader(fs,  StandardCharsets.UTF_8);
                    JsonObject object = reader.readObject();

                    Set<String> keySet = object.keySet();
                    if(keySet.contains(AppConstants.SERVER_ADDRESS)){
                        ((GlobalVariables) application).setServerPath(object.getString(AppConstants.SERVER_ADDRESS));
                    }
                    if(keySet.contains(AppConstants.TOKEN)){
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
     * Creates notification for the user to log in.
     */
    private void createLoginNotification(){

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            NotificationChannel channel = manager.getNotificationChannel(AppConstants.CHANNEL);

            if (channel == null) {
                channel = new NotificationChannel(AppConstants.CHANNEL, name, NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription(description);
                manager.createNotificationChannel(channel);
            }
        }

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), AppConstants.CHANNEL)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(getResources().getString(R.string.notif_login_title))
                .setContentText(getResources().getString(R.string.notif_login))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        Notification notification = builder.build();

        manager.notify(AppConstants.LOGIN_NOTIF_ID, notification);
    }

    /**
     * Loads the latest events registered on Raspberry Pi.
     * Notifies the user about intrusion, if present in the events.
     *
     * @param params expects the identity token on 0 position.
     * @return list of {@link RpiEventItem}.
     */
    protected void synchronizeEvents(String... params){
        String token;

        if(params.length > 0){
            try {
                token = params[0];

                //Get latest registered date of event
                String strdate = EventsFragment.loadDateOfLatestEvent(((GlobalVariables) application).getServerPath(), token);
                Logger.getLogger(SynchronizeTaskService.class.getName()).log(Level.INFO, "Latest event on server: " + strdate);

                //Check if the action is cached
                LruCache<String, RpiEventItem> cache = ((GlobalVariables) application).getEventsMemoryCache();
                if(cache.get(strdate) == null){ //If not in the cache, download it
                    Logger.getLogger(SynchronizeTaskService.class.getName()).log(Level.INFO, "Loading events after: " + strdate);
                    List<RpiEventItem> result = EventsFragment.loadEventsFromServer(((GlobalVariables) application).getServerPath(), strdate, token);

                    Logger.getLogger(SynchronizeTaskService.class.getName()).log(Level.INFO, "Events: " + result.size());
                    //Check latest events
                    Collections.sort(result, new RpiEventItemComparator());
                    if(result.size() > 0){
                        //if last known action (first in the sorted list) is not switching the device off
                        if(!result.get(0).getEventInfo().contains(AppCommunicationConsts.RFIDSWITCHOFF)){
                            //then watch for motion detection
                            for(int i = 0; i < result.size(); i++){
                                //if we did not hit the initial switch on message yet
                                if(!result.get(i).getEventInfo().contains(AppCommunicationConsts.RFIDSWITCHON)){
                                    if(result.get(i).getEventLevel().equals(AppCommunicationConsts.DANGEROUS)) {
                                        createNotification(result.get(i).getEventInfo());
                                    }
                                }
                                else{
                                    break;
                                }
                            }
                        }
                    }

                    taskResult = GcmNetworkManager.RESULT_SUCCESS;
                }
            }
            catch (Exception e){
                Logger.getLogger(SynchronizeTaskService.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    /**
     * Creates notification of the intrusion.
     * @param text is the event text.
     */
    private void createNotification(String text){

        Logger.getLogger(SynchronizeTaskService.class.getName()).log(Level.INFO, "Creating notification");

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            NotificationChannel channel = manager.getNotificationChannel(AppConstants.CHANNEL);

            if (channel == null) {
                channel = new NotificationChannel(AppConstants.CHANNEL, name, NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription(description);
                manager.createNotificationChannel(channel);
            }
        }

        Intent intent = new Intent(getApplicationContext(), RpiControlActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), AppConstants.CHANNEL)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(getResources().getString(R.string.notif_title))
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        Notification notification = builder.build();
        manager.notify(AppConstants.NOTIFY_ID, notification);
    }
}
