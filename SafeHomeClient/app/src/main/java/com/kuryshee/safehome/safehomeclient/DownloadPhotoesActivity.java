package com.kuryshee.safehome.safehomeclient;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.kuryshee.safehome.httprequestsender.AnswerConstants;
import com.kuryshee.safehome.httprequestsender.GetRequestSender;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DownloadPhotoesActivity extends AppCompatActivity {

    /**
     * The constant for requesting the file.
     */
    public static final String REQ_DOWNLOAD = "/download";

    /**
     * The constant for the POST request parameter of photo.
     */
    public static final String PHOTO_PARAM = "photo";
    ExtendedApplication app;
    Button back;
    LinearLayout list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_photoes);

        app = ((ExtendedApplication) this.getApplication());

        back = (Button) findViewById(R.id.dwn_back);
        back.setOnClickListener((v) ->{
            Intent i = new Intent(getApplicationContext(), MenuActivity.class);
            startActivity(i);
        });

        list = (LinearLayout) findViewById(R.id.list);

        setButtons();
    }

    private void setButtons(){

        BufferedReader br = null;
        try{
            br = new BufferedReader(new FileReader(
                    this.getApplicationContext().getFilesDir() +  MenuActivity.PHOTOLIST_FILE));
            String line;
            while ((line=br.readLine()) != null){
                Button b = new Button(this);
                b.setText(line);
                b.setOnClickListener((v)->{
                    String fileName = ((Button) v).getText().toString();
                    if(download(fileName)){
                        Toast.makeText(getApplicationContext(), "The photo was saved", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(
                                "file:/" + this.getApplicationContext().getFilesDir() + fileName.substring(fileName.indexOf('\\') + 1)+".jpg"),
                                "image/*");
                        startActivity(intent);
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Could not fetch data", Toast.LENGTH_SHORT).show();
                    }
                });

                list.addView(b);

                Logger.getLogger("Setting buttons").log(Level.INFO, line);
            }
        }
        catch(Exception e){
            Logger.getLogger("Photo download").log(Level.SEVERE, e.getMessage(), e);
        }
        finally {
            if(br != null){
                try {
                    br.close();
                }
                catch(IOException e){
                    Logger.getLogger("Photo download").log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
    }

    private Boolean download(String fileName){
        HashMap<String, String> atts = new HashMap<>();
        atts.put(MenuActivity.RPI_PARAM, app.getRaspberryID());
        atts.put(PHOTO_PARAM, fileName);
        String url = GetRequestSender.formatQuery(REQ_DOWNLOAD, atts, SendGetRequest.DEFAULT_ENCODING);
        try {
            String answer = new DownloadData(this).execute(url, fileName.substring(fileName.indexOf('\\') + 1)+".jpg").get();
            if(answer.equals(AnswerConstants.OK_ANSWER)){
                return true;
            }
        }
        catch(Exception e){
            Logger.getLogger("Download photo").log(Level.SEVERE, "AsyncTask execution failed", e);
        }

        return false;
    }
}
