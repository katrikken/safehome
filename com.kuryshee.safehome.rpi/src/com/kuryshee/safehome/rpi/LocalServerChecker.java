package com.kuryshee.safehome.rpi;

import com.kuryshee.safehome.httprequestsender.AnswerConstants;
import com.kuryshee.safehome.httprequestsender.GetRequestSender;
import com.kuryshee.safehome.httprequestsender.FormUploader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements the custom thread.
 * It communicates with the local server and passes the answers to the other parts of the application.
 * @author Ekaterina Kurysheva
 */
public class LocalServerChecker extends Thread{ 
    
    /**
     * The constant for the Raspberry Pi to ask for new tasks from server.
     */
    public static final String REQ_CHECKTASK = "/checktask";
	
    /**
     * The constant for the server to tell the Raspberry Pi to read a token.
     */
    public static final String COMMAND_READTOKEN = "/read";
    
    /**
     * The constant for the server to tell the Raspberry Pi to update user list.
     */
    public static final String COMMAND_UPDATEUSERS = "/saveuser";

    /**
     * The POST request parameter for passing the token data.
     */
    public static final String CARD_PARAM = "card";
    
    /**
     * The POST request parameter for passing the id data.
     */
    public static final String ID_PARAM = "rpi";
    
    /**
     * The constant for the GET request parameter of this Raspberry Pi ID.
     */
    public static final String ATT_RPI = "rpi";
    
    /**
     * The constant defining how long the thread should sleep in between actions in milliseconds.
     */
    private long FIVE_SEC = 5000;
    
    private static final Logger LOGGER = Logger.getLogger("Local server checker");
    
    /**
     * This method sends the GET request to a {@link #REQ_CHECKTASK} address on the server. 
     */
    private void sendCheckTask(){
        String query;
        try {
            query = String.format("%s?%s=%s", 
                    REQ_CHECKTASK,
                    ATT_RPI,
                    URLEncoder.encode(Main.id, Main.DEFAULT_ENCODING));
            
            sendGETRequest(query);
            
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }                      
    }
    
    /**
     * This method sends card ID read by RFID thread to local server as a POST request.
     */
    private void sendCardID(){
        FormUploader uploader;
        try{
            uploader = new FormUploader(Main.localServerAddress + COMMAND_READTOKEN, 
                    Main.DEFAULT_ENCODING);
            
            uploader.addFormField(ID_PARAM, Main.id);            
            uploader.addFormField(CARD_PARAM, Main.forLocalServer.peek().substring(COMMAND_READTOKEN.length()));
            String response = uploader.finish();
            
            if(response.equals(AnswerConstants.OK_ANSWER)){
                Main.forLocalServer.poll();
            }         
        }
        catch(IOException e){
            LOGGER.log(Level.INFO, "-- Local server thread -- sending POST request failed"); 
        }           
    }
    
    /**
    * This method uses utility class {@link GetRequestSender} to send GET request
    * @param request is a string to add to the server address.
    * The method stores the answer in the queue for inside tasks in {@link #insideTasks}.
    * @return true if the request was successfully sent.
    */
    public static Boolean sendGETRequest(String request){      
        GetRequestSender sender = null;
        try{
            sender = new GetRequestSender(Main.localServerAddress + request, Main.DEFAULT_ENCODING);
            LOGGER.log(Level.INFO, "-- Send request " + request + " to " + Main.localServerAddress); 
            
            String answer = sender.connect();

            LOGGER.log(Level.INFO, "-- Answer: ", answer); 
            if(!answer.equals(AnswerConstants.NO_ANSWER) && !answer.equals(AnswerConstants.ERROR_ANSWER)){
                Main.insideTasks.add(answer);
                        
                LOGGER.log(Level.INFO, "-- Got inside task: {0}", answer);  
            }
            return true;
        }
        catch(Exception e){ 
            LOGGER.log(Level.SEVERE, "-- Sending GET request to {0} failed", Main.localServerAddress); 
        }
        finally{
            if (sender != null){
                sender.finish();
            }
        }
        return false;
    }
    
    /**
     * This method contains the infinite loop in which it tries to get the task from the server.
     */
    @Override
    public void run(){
        while(true){ 
            LOGGER.log(Level.INFO, "--Local server thread -- check for tasks");
            if(Main.forLocalServer.isEmpty()){  
                sendCheckTask();            
            }
            else{        
                if (Main.forLocalServer.peek().startsWith(COMMAND_READTOKEN)){
                    LOGGER.log(Level.INFO, "--Local server thread -- got card ID to send ");
                    sendCardID();
                }
            }
            try{ Thread.sleep(FIVE_SEC); }
            catch(InterruptedException e){ LOGGER.log(Level.SEVERE, null, e); }
        }
    }
}
