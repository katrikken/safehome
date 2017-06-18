package com.kuryshee.safehome.safehomeclient;

import android.os.AsyncTask;
import android.content.Context;
import com.kuryshee.safehome.httprequestsender.AnswerConstants;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;

public class DownloadData extends AsyncTask<String, Integer, String> {
    private Context context;
    private static String serverAddress = "http://10.0.2.2:8080/SafeHome/SafeHomeServer/app";

    public static final String DEFAULT_ENCODING = "UTF-8";

    private static final Logger LOGGER = Logger.getLogger("Download Data");


    public DownloadData(Context context) {
        this.context = context;
    }

    /**
     * This method downloads file from the server.
     * @param urls consists of two fields: file url and destination path.
     * @return {@link AnswerConstants#OK_ANSWER} if the file was successfully downloaded,
     * {@link AnswerConstants#ERROR_ANSWER} otherwise.
     */
    @Override
    protected String doInBackground(String... urls) {
        if(urls.length == 2) {
            HttpURLConnection connection = null;
            BufferedInputStream bis = null;
            FileOutputStream fis = null;
            try {
                File file = new File(context.getFilesDir() + urls[1]);
                LOGGER.log(Level.INFO, "Writing file to: " + file.getPath());
                URL url = new URL(serverAddress + urls[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    LOGGER.log(Level.WARNING, "HTTP response code: " + connection.getResponseCode());
                    return AnswerConstants.ERROR_ANSWER;
                }

                bis = new BufferedInputStream(connection.getInputStream());
                fis = new FileOutputStream(file);

                byte[] buffer = new byte[1024];
                int count=0;
                while((count = bis.read(buffer,0,1024)) != -1)
                {
                    fis.write(buffer, 0, count);
                }

                return AnswerConstants.OK_ANSWER;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            } finally {
                if (connection != null)
                    connection.disconnect();
                if(bis != null){
                    try {
                        bis.close();
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
                if(fis != null){
                    try {
                        fis.close();
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
            }
        }
        return AnswerConstants.ERROR_ANSWER;
    }
}
