package com.kuryshee.safehome.rpi;

import com.kuryshee.safehome.httprequestsender.AnswerConstants;
import com.kuryshee.safehome.httprequestsender.GetRequestSender;
import com.kuryshee.safehome.httprequestsender.FormUploader;
import com.kuryshee.safehome.requestdataretriever.GetDataRetriever;
import com.kuryshee.safehome.rpicommunicationconsts.RpiCommunicationConsts;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class implements the custom thread.
 * It communicates with the local server and passes the answers to the other parts of the application.
 * 
 * @author Ekaterina Kurysheva
 */
public class LocalServerChecker extends Thread{ 
    
    /**
     * The constant defining how long the thread should sleep in between actions in milliseconds.
     */
    private long FIVE_SEC = 5000;
    
    private static final Logger LOGGER = Logger.getLogger(LocalServerChecker.class.getName());

    private static GetRequestSender sender = new GetRequestSender();
    
    /**
     * Sends the GET request to a {@link #REQ_CHECKTASK} address on the server. 
     */
    private void sendCheckTask(){
        try{
            Map<String, String> atts = new HashMap<>();
            atts.put(RpiCommunicationConsts.ACTION, RpiCommunicationConsts.GET_TASK);
            String query = GetRequestSender.formatQuery("", atts, Main.DEFAULT_ENCODING);
            
            sendGETRequest(query);
            
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }                      
    }
    
    /**
     * Sends card tag read by RFID thread to local server as a POST request.
     */
    private void sendCardID(){
        FormUploader uploader;
        try{
            
            uploader = new FormUploader(Main.localServerAddress, 
                    Main.DEFAULT_ENCODING);
            uploader.connect();
            
            uploader.addFormField(RpiCommunicationConsts.ACTION, RpiCommunicationConsts.COMMAND_READTOKEN);      
            uploader.addFormField(RpiCommunicationConsts.RPI_ID, Main.id);            
            uploader.addFormField(RpiCommunicationConsts.CARD_PARAM, Main.forLocalServer.peek().substring(RpiCommunicationConsts.COMMAND_READTOKEN.length()));
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
    * Sends GET request to the local server.
    * @param request is a string to add to the server address.
    * Stores the answer in the queue for inside tasks in {@link #insideTasks}.
    * @return true if the request was successfully sent.
    */
    private static Boolean sendGETRequest(String request){      
        try{
            LOGGER.log(Level.INFO, "-- Send request " + request + " to " + Main.localServerAddress); 
            
            sender.setRpiId(Main.id);
            byte[] result = sender.connect(Main.localServerAddress + request, Main.DEFAULT_ENCODING);
            String answer = new GetDataRetriever().getStringData(result);
            
            if(!answer.equals(AnswerConstants.NO_ANSWER) && !answer.equals(AnswerConstants.ERROR_ANSWER) && answer.length() > 0){
                Main.insideTasks.add(answer);
                        
                LOGGER.log(Level.INFO, "-- Got inside task: {0}", answer);  
            }
            return true;
        }
        catch(Exception e){ 
            LOGGER.log(Level.SEVERE, "-- Sending GET request to {0} failed", Main.localServerAddress); 
        }
        return false;
    }
    
    /**
     * Asks the server for tasks in a continuous loop.
     */
    @Override
    public void run(){
        while(true){ 
            LOGGER.log(Level.INFO, "--Local server thread -- check for tasks");
            if(Main.forLocalServer.isEmpty()){  
                sendCheckTask();            
            }
            else{        
                if (Main.forLocalServer.peek().startsWith(RpiCommunicationConsts.COMMAND_READTOKEN)){
                    LOGGER.log(Level.INFO, "--Local server thread -- got card ID to send ");
                    sendCardID();
                }
            }
            try{ Thread.sleep(FIVE_SEC); }
            catch(InterruptedException e){ LOGGER.log(Level.SEVERE, null, e); }
        }
    }
}
