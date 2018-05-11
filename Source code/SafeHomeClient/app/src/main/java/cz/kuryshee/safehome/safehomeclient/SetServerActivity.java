package cz.kuryshee.safehome.safehomeclient;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class implements Activity for setting the main server in Debug mode.
 *
 * @author Ekaterina Kurysheva.
 */
public class SetServerActivity extends AppCompatActivity {

    private Button connectButton;
    private EditText serverAddressInput;

    private Application application;
    private Context context;

    private Pattern pattern = Pattern.compile("http://[A-Za-z0-9:?&=\\./]+");

    /**
     * Sets the view and listeners.
     *
     * @see AppCompatActivity#onCreate(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_server);

        context = getApplicationContext();
        application = getApplication();

        connectButton = (Button) findViewById(R.id.connectButton);
        serverAddressInput = (EditText) findViewById(R.id.serverAddressInput);

        connectButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Validates the server address, sets it to the {@link GlobalVariables} and redirects to the {@link MainActivity}.
             *
             * @see View.OnClickListener#onClick(View)
             */
            @Override
            public void onClick(View view) {
                if(validateInput(serverAddressInput.getText().toString())){
                    String path = serverAddressInput.getText().toString();
                    if(!path.contains(AppConstants.SERVER_SUFFIX)){
                        path += AppConstants.SERVER_SUFFIX;
                    }
                    ((GlobalVariables) application).setServerPath(path);

                    Intent intent = new Intent(context , MainActivity.class);
                    startActivity(intent);
                }
                else{
                    serverAddressInput.setText("");

                    Logger.getLogger(SetServerActivity.class.getName()).log(Level.WARNING, "Validation against the pattern failed.");

                    Toast toast = Toast.makeText(context, getResources().getString(R.string.invalid_input), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });
    }

    /**
     * Validates the user input against a simple URL pattern.
     * @param link
     * @return true if matches the pattern.
     */
    private boolean validateInput(String link){
        Matcher matcher = pattern.matcher(link);
        return matcher.matches();
    }
}
