package cz.kuryshee.safehome.safehomeclient;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.LruCache;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.kuryshee.safehome.appcommunicationconsts.AppCommunicationConsts;
import com.kuryshee.safehome.httprequestsender.AnswerConstants;
import com.kuryshee.safehome.httprequestsender.FormUploader;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class implements Activity where user authorizes via main server.
 *
 * @author Ekaterina Kurysheva
 */
public class AuthorizationActivity extends AppCompatActivity {

    private EditText loginInput;
    private EditText passwordInput;
    private Button loginButton;
    private ProgressBar progressBar;

    private Pattern loginPattern = Pattern.compile("[A-Za-z]+[A-Za-z0-9]*");
    private Pattern passwordPattern = Pattern.compile("[A-Za-z0-9\\.,?!@#$%&\\*\\(\\)\\-=]+");

    private Application application;
    private Context context = this;
    private LruCache<String, String> stringCache;


    /**
     * Sets the view elements.
     *
     * @see AppCompatActivity#onCreate(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);

        application = this.getApplication();
        stringCache = ((GlobalVariables) application).getStringMemoryCache();

        loginInput = (EditText) findViewById(R.id.loginInput);
        passwordInput = (EditText) findViewById(R.id.passwordInput);

        progressBar = (ProgressBar) findViewById(R.id.authActivityProgressBar);
        loginButton = (Button) findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Validates input and starts the asynchronous task of authorization.
             *
             * @see View.OnClickListener#onClick(View).
             */
            @Override
            public void onClick(View view) {
                cleanCache();

                loginInput.setText(loginInput.getText().toString().trim());
                passwordInput.setText(passwordInput.getText().toString().trim());

                if (validateInput(loginInput.getText().toString(), passwordInput.getText().toString())){
                    loginButton.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    new GetToken().execute(loginInput.getText().toString(), passwordInput.getText().toString());
                }
                else{
                    showToast(getResources().getString(R.string.invalid_user_input));
                }
            }
        });

        setCredentialsFromCache();
    }

    /**
     * Shows the toast message.
     * @param errorString is the message to show.
     */
    private void showToast(String errorString){
        Toast toast = Toast.makeText(context, errorString, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /**
     * Fills the fields with cached data and tries to authorize with them.
     */
    private void setCredentialsFromCache(){
        if(stringCache.get(AppConstants.LOGIN) != null){
            loginInput.setText(stringCache.get(AppConstants.LOGIN));
            if(stringCache.get(AppConstants.PASSWORD) != null){
                passwordInput.setText(stringCache.get(AppConstants.PASSWORD));

                loginButton.callOnClick();
            }
        }
    }

    /**
     * Cleans credentials from cache.
     *
     * @see AppCompatActivity#onStop()
     */
    @Override
    protected void onStop(){
        super.onStop();

        cleanCache();
    }

    /**
     * Validates the input login and password against simple patterns.
     * @param login is allowed to be alphanumerical, first char must be a letter.
     * @param password is allowed to contain alphanumerical symbols and .,?!@#$%&*()-= characters.
     * @return true if credentials are in allowed format.
     */
    private boolean validateInput(String login, String password){

        Matcher loginMatcher = loginPattern.matcher(login);
        Matcher passwordMatcher = passwordPattern.matcher(password);

        return loginMatcher.matches() && passwordMatcher.matches();
    }

    /**
     * Navigates to the {@link MainActivity}.
     */
    private void redirectToMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * Removes the login and password from cache.
     */
    private void cleanCache(){
        if(stringCache.get(AppConstants.LOGIN) != null){
            stringCache.remove(AppConstants.LOGIN);
        }
        if(stringCache.get(AppConstants.PASSWORD) != null){
            stringCache.remove(AppConstants.PASSWORD);
        }
    }

    /**
     * Class implements asynchronous authorization task.
     */
    private class GetToken extends AsyncTask<String, Void, String> {

        /**
         * Sends the authorization request to the server.
         * @param params expects login and password on 0 and 1 position respectively.
         * @return answer from the server.
         */
        @Override
        protected String doInBackground(String... params){
            if(params.length == 2){
                try {
                    FormUploader formUploader = new FormUploader(((GlobalVariables) application).getServerPath(), null);
                    formUploader.connect();

                    formUploader.addFormField(AppCommunicationConsts.ACTION, AppCommunicationConsts.GET_TOKEN);
                    formUploader.addFormField(AppCommunicationConsts.LOGIN, params[0]);
                    formUploader.addFormField(AppCommunicationConsts.PASSWORD, params[1]);

                    return formUploader.finish();
                }
                catch (IOException e){
                    Logger.getLogger(AuthorizationActivity.class.getName()).log(Level.SEVERE, e.getMessage());
                }
            }
            return null;
        }

        /**
         * Interprets the server answer.
         * @param result is the server answer.
         */
        @Override
        protected void onPostExecute(String result){
            Logger.getLogger(AuthorizationActivity.class.getName()).log(Level.INFO, result);

            loginButton.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);

            if(result == null){
                showToast(getResources().getString(R.string.error));
                redirectToMainActivity();
            }
            else if (result.equals(AnswerConstants.NO_ANSWER) || result.equals(AnswerConstants.ERROR_ANSWER)){
                showToast(getResources().getString(R.string.server_not_resp));

                ((GlobalVariables) application).getStringMemoryCache().put(AppConstants.LOGIN, loginInput.getText().toString());
                ((GlobalVariables) application).getStringMemoryCache().put(AppConstants.PASSWORD, passwordInput.getText().toString());
            }
            else if (result.equals(AppCommunicationConsts.INVALID_USER_ERROR) || result.equals(AppCommunicationConsts.REQUEST_PROCESS_ERROR)){
                showToast(getResources().getString(R.string.auth_failed));

                ((GlobalVariables) application).getStringMemoryCache().put(AppConstants.LOGIN, loginInput.getText().toString());
            }
            else{
                ((GlobalVariables) application).getStringMemoryCache().put(AppConstants.TOKEN, result);
                redirectToMainActivity(); //MainActivity performs the token validation.
            }
        }
    }
}
