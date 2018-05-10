package com.kuryshee.safehome.rpi;

import com.kuryshee.safehome.httprequestsender.AnswerConstants;
import com.kuryshee.safehome.httprequestsender.GetRequestSender;
import com.kuryshee.safehome.httprequestsender.FormUploader;
import com.kuryshee.safehome.requestdataretriever.GetDataRetriever;
import com.kuryshee.safehome.rpicommunicationconsts.RpiCommunicationConsts;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class implements the custom thread.
 * It communicates with the main server and passes the answers to the other parts of the application.
 * 
 * @author Ekaterina Kurysheva
 */
public class ServerChecker extends Thread{ 
    
    private long THREE_SEC = 3000;
    
    private static final Logger LOGGER = Logger.getLogger(ServerChecker.class.getName());
    
    private static GetRequestSender sender = new GetRequestSender();
    

    /**
     * Uploads photos to the server in a POST request.
     * Deletes photo if successfully sent.
     * @return true if the photo was successfully sent.
     */
    private Boolean uploadPhoto(String photopath){
        File photo = new File(photopath);
            
        if(photo.exists() && photo.length() > 0){ //photo might have not been created yet
                
            FormUploader uploader;
            try{
                uploader = new FormUploader(Main.serverAddress, Main.DEFAULT_ENCODING);
                uploader.addHeader(RpiCommunicationConsts.RPI_ID, Main.id);
                uploader.connect();

                String timePart = photo.getName()
                                  .substring(0, photo.getName().length() - MotionController.formatOfImage.length()); 

                uploader.addFormField(RpiCommunicationConsts.ACTION, RpiCommunicationConsts.SAVE_PHOTO);
                uploader.addFormField(RpiCommunicationConsts.TIME, timePart);
                uploader.addFormField(RpiCommunicationConsts.PHOTO_NAME, photo.getName());

                    
                uploader.addFilePart(RpiCommunicationConsts.PHOTO, photo);

                String response = uploader.finish();
                if(response.equals(AnswerConstants.OK_ANSWER)){
                    LOGGER.log(Level.INFO, "Photo {0} was successfully sent.", Main.photoPaths.peek());

                    photo.delete();    
                    return true;
                }       
                else{
                    LOGGER.log(Level.INFO, "Photo has not been sent. Server response: {0}", response);
                }
            }
            catch(Exception e){
                LOGGER.log(Level.SEVERE, e.getMessage(), e); 
            }              
        }
        else{
            LOGGER.log(Level.INFO, "Photo {0} does not exist", photopath);
        }
        return false;
    }
    
    /**
     * Uploads information about events on Raspberry Pi.
     * @param atts contains data about the events.
     * @return true if the request was successfully sent.
     */
    private Boolean sendReport(Map<String, String> atts){
        FormUploader uploader;
            try{
                uploader = new FormUploader(Main.serverAddress, Main.DEFAULT_ENCODING);
                uploader.addHeader(RpiCommunicationConsts.RPI_ID, Main.id);
                uploader.connect();
                
                for(String key: atts.keySet()){
                    uploader.addFormField(key, atts.get(key));
                }

                String response = uploader.finish();
                if(response.equals(AnswerConstants.OK_ANSWER)){
                    LOGGER.log(Level.INFO, "Report was successfully sent.");
    
                    return true;
                }       
                else{
                    LOGGER.log(Level.INFO, "Report has not been sent. Server response: {0}", response);
                }
            }
            catch(Exception e){
                LOGGER.log(Level.SEVERE, e.getMessage(), e); 
            }        
        return false;
    }
    
    /**
     * Sends the GET request to a {@link #REQ_CHECKTASK} address on the server. 
     */
    private void sendCheckTask(){
        String query;
        try {
            query = String.format("?%s=%s&%s=%s", 
                    RpiCommunicationConsts.ACTION,
                    RpiCommunicationConsts.GET_TASK,
                    RpiCommunicationConsts.RPI_ID,
                    URLEncoder.encode(Main.id, Main.DEFAULT_ENCODING));
            
            sendGETRequest(query);
            
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }                      
    }
    
    /**
    * Sends GET request to the server.
    * @param request is a string to add to the server address.
    * The method stores the answer in the queue for inside tasks in {@link Main#insideTasks}.
    * @return true if the request was successfully sent.
    */
    public static Boolean sendGETRequest(String request){      
        try{
            LOGGER.log(Level.INFO, "-- Send request " + request + " to " + Main.serverAddress ); 

            String query = Main.serverAddress + request;
            sender.setRpiId(Main.id);
            byte[] input = sender.connect(query, Main.DEFAULT_ENCODING);
            String answer = new GetDataRetriever().getStringData(input);
            
            if(!answer.equals(AnswerConstants.NO_ANSWER) && !answer.equals(AnswerConstants.ERROR_ANSWER) && answer.length() > 0){
                Main.insideTasks.add(answer);
                        
                LOGGER.log(Level.INFO, "-- Got inside task: {0}", answer);  
            }
            return true;
        }
        catch(Exception e){ 
            LOGGER.log(Level.SEVERE, "-- Sending GET request to {0} failed", Main.serverAddress); 
        }
        return false;
    }
    
    /**
     * Checks for the queue of requests to send in a continuous loop. 
     */
    @Override
    public void run(){
        while(true){ 
            
            if(Reporter.forServer.isEmpty()){  
                if(!Main.photoPaths.isEmpty()){
                    if(uploadPhoto(Main.photoPaths.peek())){
                        Main.photoPaths.poll();
                    }
                }
                //sendCheckTask();
            }
            else{
                LOGGER.log(Level.INFO, "--Got a task");

                Boolean ok = this.sendReport(Reporter.forServer.peek());
                if(ok){
                    Reporter.forServer.poll();
                }
            }
            try{ Thread.sleep(THREE_SEC); }
            catch(InterruptedException e){ System.out.println("--Server thread -- Interrupted"); }
        }
    }
}
