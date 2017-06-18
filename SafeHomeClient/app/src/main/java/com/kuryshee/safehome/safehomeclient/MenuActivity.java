package com.kuryshee.safehome.safehomeclient;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.kuryshee.safehome.httprequestsender.AnswerConstants;
import com.kuryshee.safehome.httprequestsender.GetRequestSender;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MenuActivity extends AppCompatActivity {
    /**
     * The constant for the POST request parameter of this Raspberry Pi ID.
     */
    public static final String RPI_PARAM = "rpi";

    /**
     * The constant for the server to ask for the program state.
     */
    public static final String COMMAND_GETSTATE = "/getstate";

    /**
     * The constant for commanding and reporting switching the program state to off.
     */
    public static final String COMMAND_SWITCHOFF = "/switchoff";

    /**
     * The constant for commanding and reporting switching the program state to on.
     */
    public static final String COMMAND_SWITCHON = "/switchon";

    /**
     * The constant for the server to command Raspberry Pi taking a photo.
     */
    public static final String COMMAND_TAKEPHOTO = "/takephoto";

    /**
     * The constant for requesting data about Raspberry Pi usage history.
     */
    public static final String REQ_HISTORY = "/history";

    /**
     * The constant for requesting data about available photos.
     */
    public static final String REQ_LIST= "/list";

    private static final String UNKNOWN = "UNKNOWN";
    private static final String STATE_TEXT = "The state is: ";
    private static final String ON = "ON";
    private static final String OFF = "OFF";
    private static final String PICTURE_BTN_TEXT = "Take a picture";
    private static final String PICTURE_BTN_TEXT_PROGRESS = "Sending the request...";

    public static final String HISTORY_FILE = "history.txt";
    public static final String PHOTOLIST_FILE = "list.txt";

    private static final Logger LOGGER = Logger.getLogger("MenuActivity");

    ExtendedApplication app;

    Button picture;
    Button history;
    Button download;
    TextView state;
    Switch switchbtn;

    DownloadData downloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        app = ((ExtendedApplication) this.getApplication());
        sendGetState();

        state = (TextView )findViewById(R.id.state);
        setStateText();

        picture = (Button)findViewById(R.id.picture);
        picture.setOnClickListener((v) ->{
            picture.setText(PICTURE_BTN_TEXT_PROGRESS);
            if(sendTakePhoto()){
                Toast.makeText(getApplicationContext(), "The picture was taken!", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(getApplicationContext(), "Could not take the picture", Toast.LENGTH_SHORT).show();
            }
            picture.setText(PICTURE_BTN_TEXT);
        });

        history = (Button)findViewById(R.id.history);
        history.setOnClickListener((v) ->{
            if(fetchHistory()){
                Intent i = new Intent(getApplicationContext(), HistoryActivity.class);
                startActivity(i);
            }
            else{
                Toast.makeText(getApplicationContext(), "Could not fetch data", Toast.LENGTH_SHORT).show();
            }
        });

        download = (Button)findViewById(R.id.download);
        download.setOnClickListener((v) ->{
            if(fetchPhotoData()){
                Intent i = new Intent(getApplicationContext(), DownloadPhotoesActivity.class);
                startActivity(i);
            }
            else{
                Toast.makeText(getApplicationContext(), "Could not fetch data", Toast.LENGTH_SHORT).show();
            }
        });

        switchbtn = (Switch)findViewById(R.id.switchbtn);
        switchbtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(sendSwitchCommand(COMMAND_SWITCHON)){
                        app.setState(ON);
                        setStateText();
                    }
                    else{
                        switchbtn.setChecked(false);
                    }
                }
                else{
                    if(sendSwitchCommand(COMMAND_SWITCHOFF)){
                        app.setState(OFF);
                        setStateText();
                    }
                    else {
                        switchbtn.setChecked(true);
                    }
                }
            }
        });
    }

    private Boolean fetchPhotoData(){
        HashMap<String, String> atts = new HashMap<>();
        atts.put(RPI_PARAM, app.getRaspberryID());
        String url = GetRequestSender.formatQuery(REQ_LIST, atts, SendGetRequest.DEFAULT_ENCODING);
        try {
            String answer = new DownloadData(this).execute(url, PHOTOLIST_FILE).get();
            if(answer.equals(AnswerConstants.OK_ANSWER)){
                return true;
            }
        }
        catch(Exception e){
            LOGGER.log(Level.SEVERE, "AsyncTask execution failed", e);
        }
        return false;
    }

    private Boolean fetchHistory(){
        HashMap<String, String> atts = new HashMap<>();
        atts.put(RPI_PARAM, app.getRaspberryID());
        String url = GetRequestSender.formatQuery(REQ_HISTORY, atts, SendGetRequest.DEFAULT_ENCODING);
        try {
            String answer = new DownloadData(this).execute(url, HISTORY_FILE).get();
            if(answer.equals(AnswerConstants.OK_ANSWER)){
                return true;
            }
        }
        catch(Exception e){
            LOGGER.log(Level.SEVERE, "AsyncTask execution failed", e);
        }

        return false;
    }

    private void setStateText(){
        state.setText(STATE_TEXT + app.getState());
    }

    private Boolean sendSwitchCommand(String command){
        HashMap<String, String> atts = new HashMap<>();
        atts.put(RPI_PARAM, app.getRaspberryID());
        String url = GetRequestSender.formatQuery(command , atts, SendGetRequest.DEFAULT_ENCODING);
        try {
            String[] answers = new SendGetRequest().execute(url).get();
            if(answers.length == 1){
                if(answers[0].startsWith(command) &&
                        answers[0].substring(command.length() + 1).equals(AnswerConstants.OK_ANSWER)){
                    return true;
                }
            }
        }
        catch(Exception e){
            LOGGER.log(Level.SEVERE, "AsyncTask execution failed", e);
        }
        return false;
    }

    private void sendGetState(){
        HashMap<String, String> atts = new HashMap<>();
        atts.put(RPI_PARAM, app.getRaspberryID());
        String url = GetRequestSender.formatQuery(COMMAND_GETSTATE , atts, SendGetRequest.DEFAULT_ENCODING);
        try {
            String[] answers = new SendGetRequest().execute(url).get();
            if(answers.length == 1){
                if(!answers[0].equals(AnswerConstants.ERROR_ANSWER) && answers[0].startsWith(COMMAND_GETSTATE)){
                    //set RPi state
                    app.setState(answers[0].substring(COMMAND_GETSTATE.length() + 1).toUpperCase());
                }
                else{
                    app.setState(UNKNOWN);
                }
            }
        }
        catch(Exception e){
            LOGGER.log(Level.SEVERE, "AsyncTask execution failed", e);
        }
    }

    private Boolean sendTakePhoto(){
        HashMap<String, String> atts = new HashMap<>();
        atts.put(RPI_PARAM, app.getRaspberryID());
        String url = GetRequestSender.formatQuery(COMMAND_TAKEPHOTO , atts, SendGetRequest.DEFAULT_ENCODING);
        try {
            String[] answers = new SendGetRequest().execute(url).get();
            if(answers.length == 1){
                if(answers[0].equals(AnswerConstants.OK_ANSWER)){
                    return true;
                }
            }
        }
        catch(Exception e){
            LOGGER.log(Level.SEVERE, "AsyncTask execution failed", e);
        }
        return false;
    }
}
