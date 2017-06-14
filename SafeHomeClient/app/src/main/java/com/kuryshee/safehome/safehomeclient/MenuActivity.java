package com.kuryshee.safehome.safehomeclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.kuryshee.safehome.httprequestsender.AnswerConstants;
import com.kuryshee.safehome.httprequestsender.GetRequestSender;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MenuActivity extends AppCompatActivity {

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

    private static final String UNKNOWN = "UNKNOWN";
    private static final String STATE_TEXT = "The state is: ";
    private static final String ON = "ON";
    private static final String OFF = "OFF";
    private static final Logger LOGGER = Logger.getLogger("MenuActivity");

    ExtendedApplication app;

    Button picture;
    Button history;
    Button download;
    EditText state;
    Switch switchbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        app = ((ExtendedApplication) this.getApplication());
        sendGetState();

        state = (EditText)findViewById(R.id.state);
        setStateText();

        picture = (Button)findViewById(R.id.picture);
        history = (Button)findViewById(R.id.history);
        download = (Button)findViewById(R.id.download);

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
                if(!answers[0].equals(AnswerConstants.ERROR_ANSWER)){
                    //set RPi state
                    app.setState(answers[0].toUpperCase());
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
}
