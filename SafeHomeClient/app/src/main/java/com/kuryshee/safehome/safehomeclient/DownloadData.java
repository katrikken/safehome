package com.kuryshee.safehome.safehomeclient;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.content.Context;
import android.os.PowerManager;

import com.kuryshee.safehome.httprequestsender.AnswerConstants;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import android.app.ProgressDialog;
import android.widget.Toast;

public class DownloadData extends AsyncTask<String, Integer, String> {
    private Context context;
    private PowerManager.WakeLock mWakeLock;
    private static String serverAddress = "http://10.0.2.2:8080/SafeHome/SafeHomeServer/app";

    public static final String DEFAULT_ENCODING = "UTF-8";

    private static final Logger LOGGER = Logger.getLogger("SendGetRequest");

    ProgressDialog mProgressDialog;

    public DownloadData(Context context) {
        this.context = context;
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Loading data...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
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
            try {
                URL url = new URL(serverAddress + urls[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                int fileLength = connection.getContentLength();

                BufferedReader reader = null;
                PrintWriter out = null;
                try {
                    reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), DEFAULT_ENCODING));
                    out = new PrintWriter(new BufferedWriter(new FileWriter(context.getFilesDir() + urls[1], false))); //rewrites file

                    char data[] = new char[4096];
                    long total = 0;
                    int count;
                    while ((count = reader.read()) != -1) {
                        if (isCancelled()) {
                            return AnswerConstants.ERROR_ANSWER;
                        }

                        total += count;

                        if (fileLength > 0)
                            publishProgress((int) (total * 100 / fileLength));
                        out.write(data);
                    }
                    return AnswerConstants.OK_ANSWER;
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            } finally {
                if (connection != null)
                    connection.disconnect();
            }
        }
        return AnswerConstants.ERROR_ANSWER;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // take CPU lock to prevent CPU from going off if the user
        // presses the power button during download
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        mWakeLock.acquire();
        mProgressDialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        mWakeLock.release();
        mProgressDialog.dismiss();
        if (result != null)
            Toast.makeText(context,"Download error: " + result, Toast.LENGTH_LONG).show();
        else
            Toast.makeText(context,"File downloaded", Toast.LENGTH_SHORT).show();
    }
}
