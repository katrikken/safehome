package com.kuryshee.safehome.safehomeclient;

import android.os.AsyncTask;
import com.kuryshee.safehome.httprequestsender.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;
import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 *
 */
public class SendGetRequest extends AsyncTask<String, Void, String[]>  {
    private static String serverAddress = "http://10.0.2.2:8080/SafeHome/SafeHomeServer/app";
    private static final Logger LOGGER = Logger.getLogger("SendGetRequest");

    /**
     * The default encoding for the communication over HTTP.
     */
    public static final String DEFAULT_ENCODING = "UTF-8";

    @Override
    protected String[] doInBackground(String... requests) {
        int count = requests.length;
        String[] answers = new String[count];
        for (int i = 0; i < count; i++) {
            GetRequestSender sender = null;
            try{
                sender = new GetRequestSender(serverAddress  + requests[i], DEFAULT_ENCODING);

                String answer = sender.connect();
                LOGGER.log(Level.INFO, "-- Server answer: " + answer);
                answers[i] = answer;
            }
            catch(Exception e){
                LOGGER.log(Level.SEVERE, "-- Sending GET request failed", e);
            }
            finally{
                if (sender != null){
                    sender.finish();
                }
            }

        }
        return answers;
    }
}
