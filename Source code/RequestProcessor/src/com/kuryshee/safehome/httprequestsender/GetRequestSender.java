package com.kuryshee.safehome.httprequestsender;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

import com.kuryshee.safehome.appcommunicationconsts.AppCommunicationConsts;
import com.kuryshee.safehome.requestdataretriever.GetDataRetriever;
import com.kuryshee.safehome.rpicommunicationconsts.RpiCommunicationConsts;

/**
 * This class implements the set of functions for sending GET requests.
 * @author Ekaterina Kurysheva
 */
public class GetRequestSender {

    private HttpURLConnection connection;
    private final int TEN_SEC = 10000;
    private String token = null;
    private String rpiId = null;
    
    /**
     * Sets identity token to request headers.
     * @param token
     */
    public void setToken(String token) {
    	this.token = token;
    }
    
    /**
     * Sets the Raspbery Pi id to the headers.
     * @param id
     */
    public void setRpiId(String id) {
    	this.rpiId = id;
    }
    
    /**
     * Connects to the server and tries to get the answer.
     * @param query is a full URL address to set the connection.
     * @param charset defines the encoding for the request, default is UTF-8
     * @return byte array with answer.
     * In case error occurred, returns {@link AnswerConstants#ERROR_ANSWER}.
     * In case no answer arrived, returns null.
     */
    public byte[] connect(String query, String charset){
        if(charset == null) charset = "UTF-8";
        
        try{
            URL url = new URL(query);
        
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.setRequestProperty("Accept-Charset", charset);   
			if(this.token != null) {
				connection.setRequestProperty(AppCommunicationConsts.TOKEN, token);
			}
			if(this.rpiId != null) {
				connection.setRequestProperty(RpiCommunicationConsts.RPI_ID, rpiId);
			}
            connection.setConnectTimeout(TEN_SEC); 
            connection.connect();
            
            byte[] bytes = null;
            
            try(InputStream in = new BufferedInputStream(connection.getInputStream())){
            	bytes = IOUtils.toByteArray(in); 
            }
            catch (IOException e) {
    			Logger.getLogger(GetRequestSender.class.getName()).log(Level.SEVERE, e.getMessage(), e);
    		}
            return bytes;
        }
        catch(IOException e){
            Logger.getLogger(GetRequestSender.class.getName()).log(Level.SEVERE, e.getMessage(), e); 
            try {
    			return AnswerConstants.ERROR_ANSWER.getBytes(charset);
    		} catch (UnsupportedEncodingException ex) {
    			Logger.getLogger(GetRequestSender.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);    
    		}
        }
        finally{
            if (connection != null){
                connection.disconnect();
            }
            
        }
        
        return null;
    }   
   
    
    /**
     * Encodes the query to the URL format.
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
            Logger.getLogger(GetRequestSender.class.getName()).log(Level.SEVERE, e.getMessage(), e);          
        }
        return "";
    }
}
