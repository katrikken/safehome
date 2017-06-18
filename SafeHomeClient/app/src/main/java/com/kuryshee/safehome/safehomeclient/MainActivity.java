package com.kuryshee.safehome.safehomeclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.widget.Toast;
import android.content.Intent;

import com.kuryshee.safehome.httprequestsender.GetRequestSender;
import com.kuryshee.safehome.httprequestsender.AnswerConstants;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    public static final String REQ_LOGIN = "/login";
    public static final String USR_PARAM = "user";
    public static final String PSWD_PARAM = "pswd";
    Button login;
    EditText username;
    EditText password;

    private static final Logger LOGGER = Logger.getLogger("MainActivity");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login = (Button)findViewById(R.id.login);
        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);



        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Loading...",Toast.LENGTH_SHORT).show();
                if(checkLogin(username.getText().toString(), password.getText().toString())) {
                    Intent i = new Intent(getApplicationContext(), MenuActivity.class);
                    startActivity(i);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Cannot log in", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private Boolean checkLogin(String username, String password){
        HashMap<String, String> atts = new HashMap<>();
        atts.put(USR_PARAM, username);
        atts.put(PSWD_PARAM, password);
        String url = GetRequestSender.formatQuery(REQ_LOGIN, atts, SendGetRequest.DEFAULT_ENCODING);
        try {
            String[] answers = new SendGetRequest().execute(url).get();
            if(answers.length == 1){
                if(!answers[0].equals(AnswerConstants.ERROR_ANSWER)){
                    //set RPi id
                    ((ExtendedApplication) this.getApplication()).setRaspberryID(answers[0]);

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
