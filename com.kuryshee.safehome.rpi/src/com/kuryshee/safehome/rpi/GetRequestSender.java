
package com.kuryshee.safehome.rpi;

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

public class GetRequestSender {

    private HttpURLConnection connection;
    private String charset;
    
    /**
     * This constructor creates a new instance of HttpURLConnection to form a GET request.
     * @param query full URL address to set the connection.
     * @param charset
     * @throws IOException
     */
    public GetRequestSender(String query, String charset) throws IOException{
        this.charset = charset;
        URL url = new URL(query);
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.setRequestProperty("Accept-Charset", charset);                                      
    }
    
    public String connect() throws IOException{
        connection.connect();
        try(BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), charset))){     
            for (int i = 0; i < 10; i++){  
                if(reader != null){
                    String answer = reader.readLine().trim();
                    return answer;
                }
                else{            
                    try{ Thread.sleep(100); }
                    catch(InterruptedException e){ //log 
                        return Main.ERROR_ANSWER;
                    }               
                }
            }
            return Main.NO_ANSWER;
                       
        }
        catch(IOException e){
            return Main.ERROR_ANSWER;
        }
    }   
    
    public void finish(){
        if (connection != null){
            connection.disconnect();
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
