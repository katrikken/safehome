package com.kuryshee.safehome.safehomeclient;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HistoryActivity extends AppCompatActivity {

    TextView text;
    Button back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        text = (TextView )findViewById(R.id.his_text);
        setHisText();

        back = (Button) findViewById(R.id.his_back);
        back.setOnClickListener((v) ->{
            Intent i = new Intent(getApplicationContext(), MenuActivity.class);
            startActivity(i);
        });
    }

    private void setHisText(){
        BufferedReader br = null;
        try{
            br = new BufferedReader(new FileReader(
                    this.getApplicationContext().getFilesDir() + MenuActivity.HISTORY_FILE));
            String line;
            while ((line=br.readLine()) != null){
                text.append(line);
                text.append("\n");
            }
        }
        catch(Exception e){
            Logger.getLogger("Writing history").log(Level.SEVERE, e.getMessage(), e);
        }
        finally {
            if(br != null){
                try {
                    br.close();
                }
                catch(IOException e){
                    Logger.getLogger("Writing history").log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
    }
}
