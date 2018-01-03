package com.kuryshee.safehome.httprequestsender;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements the set of functions for sending GET requests.
 * @author Ekaterina Kurysheva
 */
public class GetRequestSender {

    private HttpURLConnection connection;
    private final int TEN_SEC = 10000;
    
    /**
     * This method connects to the server and tries to get the answer.
     * @param query is a full URL address to set the connection.
     * @param charset defines the encoding for the request.
     * @return string with answer. 
     * In case no answer arrived, returns {@link AnswerConstants#NO_ANSWER}.
     * In case error occurred, returns {@link AnswerConstants#ERROR_ANSWER}.
     */
    public String connect(String query, String charset){
        try{
            URL url = new URL(query);
        
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.setRequestProperty("Accept-Charset", charset);                
            connection.setConnectTimeout(TEN_SEC); 
            connection.connect();
            try(BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), charset))){     
                for (int i = 0; i < 10; i++){  
                    if(reader != null){
                        String answer = reader.readLine();
                        if(answer != null)
                            return answer;
                    }
                    else{            
                        try{ Thread.sleep(100); }
                        catch(InterruptedException e){ //log 
                            return AnswerConstants.ERROR_ANSWER;
                        }               
                    }
                }

                return AnswerConstants.NO_ANSWER;                      
            }
            catch(IOException e){
                Logger.getLogger("Sending GET request").log(Level.SEVERE, e.getMessage());    
                return AnswerConstants.ERROR_ANSWER;
            }
            finally{
                if (connection != null){
                    connection.disconnect();
                }
            }
        }
        catch(IOException e){
            Logger.getLogger("Sending GET request").log(Level.SEVERE, e.getMessage());    
            return AnswerConstants.ERROR_ANSWER;
        }
    }   
   
    
    /**
     * This method encodes the query to the URL format.
     * @param command is a start of the query address
     * @param attributes is a map of parameter names and values of the query
     * @param charset is an encoding setting for the query
     * @return string with encoded query.
     */
    public static String formatQuery(String command, Map<String, String> attributes, String charset){
        try{
            StringBuilder query = new StringBuilder(command + '?');
            
            int size = attributes.size();
            
            for(String key : attributes.keySet()){
                query.append(key);
                query.append('=');
                query.append(URLEncoder.encode(attributes.get(key), charset));
                
                size--;
                if(size > 0){
                    query.append('&');
                }
            }
               
            return query.toString();
        }
        catch(UnsupportedEncodingException e){
            Logger.getLogger("Query encoding").log(Level.SEVERE, "--Main thread -- could not form the query string");          
        }
        return "";
    }
}
