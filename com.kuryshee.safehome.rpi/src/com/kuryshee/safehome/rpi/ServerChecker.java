package com.kuryshee.safehome.rpi;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements the custom thread.
 * It communicates with the main server and passes the answers to the other parts of the application.
 * @author Ekaterina Kurysheva
 */
public class ServerChecker extends Thread{ 
    
    /**
     * The constant for GET request to check up with the main server.
     */
    public final String REQ_CHECKTASK = "/checktask";  
    
    /**
     * The constant for commanding and reporting switching the program state to off.
     */
    public static final String COMMAND_SWITCHOFF = "/switchoff";
    
    /**
     * The constant for commanding and reporting switching the program state to on.
     */
    public static final String COMMAND_SWITCHON = "/switchon";
    
    /**
     * The constant for the server to ask for the program state.
     */
    public static final String COMMAND_GETSTATE = "/getstate";
    
    /**
     * The constant for the server to command taking a photo.
     */
    public static final String COMMAND_TAKEPHOTO = "/takephoto";
    
    /**
     * The constant for the POST request to the server when sending a photo.
     */
    public static final String UPLOAD_PHOTO = "/upload"; 
    
    /**
     * The constant for the POST request parameter of time.
     */
    public static final String TIME_PARAM = "time";
    
    /**
     * The constant for the POST request parameter of this Raspberry Pi ID.
     */
    public static final String ID_PARAM = "rpi";
    
    /**
     * The constant for the POST request parameter of photo.
     */
    public static final String PHOTO_PARAM = "photo";
    
    /**
     * The constant for the POST request parameter containing the name associated with used RFID token.
     */
    public static final String RFID_PARAM = "rfid";
    
    /**
     * The constant for the GET request parameter containing short answer.
     */
    public static final String ATT_ANSWER = "answer";
    
    private long ONE_SEC = 1000;
    
    private static final Logger LOGGER = Logger.getLogger("Server Check");
    

    /**
     * This method uploads photos to the server.
     * @return true if the photo was successfully sent.
     */
    private Boolean uploadPhoto(){
        for(int i = 0; i < Main.photoPaths.size(); i++){
            FormUploader uploader;
            try{
                uploader = new FormUploader(Main.serverAddress + UPLOAD_PHOTO, Main.DEFAULT_ENCODING);
                uploader.addFormField(TIME_PARAM, 
                        Main.photoPaths.peek().substring(
                                Main.photoDir.length(), Main.photoPaths.peek().length() - Main.formatOfImage.length())
                );
                uploader.addFormField(ID_PARAM, Main.id);

                uploader.addFilePart(PHOTO_PARAM, new File(Main.photoPaths.peek()));
                String response = uploader.finish();
                if(response.equals(Main.OK_ANSWER)){
                    LOGGER.log(Level.INFO, "Photo {0} was successfully sent.", Main.photoPaths.peek());
                    
                    Main.photoPaths.poll();      
                    return true;
                }       
                else{
                    LOGGER.log(Level.INFO, "Photo has not been sent. Server response: {0}", response);
                }
            }
            catch(Exception e){
                LOGGER.log(Level.SEVERE, "--Server thread -- sending POST request failed", e); 
            }        
        }
        return false;
    }
    
    /**
     * This method uploads information about switching the state of a program by a token.
     * @param info contains data about the registered token.
     * @return true if the request was successfully sent.
     */
    private Boolean sendSwitchInfo(String info){
        FormUploader uploader;
        try{
            uploader = new FormUploader(Main.serverAddress + RFIDController.REQ_RFIDSWITCH,
                    Main.DEFAULT_ENCODING);

            uploader.addFormField(ID_PARAM, Main.id);
            uploader.addFormField(RFID_PARAM, info);
            uploader.addFormField(TIME_PARAM, MotionController.dateFormat.format(new Date()));
                
            String response = uploader.finish();
            if(response.equals(Main.OK_ANSWER)){
                LOGGER.log(Level.INFO, "Switching by the token was logged on the server");      
                return true;
            }       
            else{
                LOGGER.log(Level.INFO, "Switching by the token was not logged on the server");
            }
        }
        catch(Exception e){
            LOGGER.log(Level.SEVERE, "--Server thread -- sending POST request failed", e); 
        }     
        return false;
    }
    
    /**
     * This method sends the GET request to a {@link #REQ_CHECKTASK} address on the server. 
     */
    private void sendCheckTask(){
        String query;
        try {
            query = String.format("%s?%s=%s", 
                    REQ_CHECKTASK,
                    ID_PARAM,
                    URLEncoder.encode(Main.id, Main.DEFAULT_ENCODING));
            
            sendGETRequest(query);
            
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.SEVERE, "--Sending checktask request failed", ex);
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
            sender = new GetRequestSender(Main.serverAddress  + request, Main.DEFAULT_ENCODING);
            LOGGER.log(Level.INFO, "-- Send request " + request + " to " + Main.serverAddress ); 
            
            String answer = sender.connect();

            LOGGER.log(Level.INFO, "-- Answer: ", answer); 
            if(!answer.equals(Main.NO_ANSWER) && !answer.equals(Main.ERROR_ANSWER)){
                Main.insideTasks.add(answer);
                        
                LOGGER.log(Level.INFO, "-- Got inside task: {0}", answer);  
            }
            return true;
        }
        catch(Exception e){ 
            LOGGER.log(Level.SEVERE, "-- Sending GET request to {0} failed", Main.serverAddress); 
        }
        finally{
            if (sender != null){
                sender.finish();
            }
        }
        return false;
    }
    
    /**
     * This method repeatedly checks for the requests to send to the main server. 
     */
    @Override
    public void run(){
        while(true){ 
            if(Main.forServer.isEmpty()){  
                sendCheckTask();
            }
            else{
                LOGGER.log(Level.INFO, "--Got a task");
                    
                //Parsing query for server to distinguish between GET and POST requests.
                int index = Main.forServer.peek().indexOf("?");
                String command;
                if(index != -1 ){
                    command = Main.forServer.peek().substring(0, index);
                }
                else{
                    command = Main.forServer.peek();
                }

                Boolean ok = false;
                
                switch (command) {
                    case COMMAND_GETSTATE:
                    case COMMAND_SWITCHOFF:
                    case COMMAND_SWITCHON:
                    case COMMAND_TAKEPHOTO:
                    case MotionController.REQ_MOTIONDETECTED:
                        ok = sendGETRequest(Main.forServer.peek());
                        if(ok){
                            Main.forServer.poll();
                        }
                        break;
                    case MotionController.REQ_PHOTOTAKEN:
                        ok = uploadPhoto();
                        if(ok){
                            Main.forServer.poll();
                        }
                        break;
                    default:
                        if(command.startsWith(RFIDController.REQ_RFIDSWITCH)){
                            ok = sendSwitchInfo(command.substring(RFIDController.REQ_RFIDSWITCH.length()));
                            if(ok){
                                Main.forServer.poll();
                            }            
                        }
                        ok = false;
                        break;
                }
            }
            try{ Thread.sleep(ONE_SEC); }
            catch(InterruptedException e){ System.out.println("--Server thread -- Interrupted"); }
        }
    }
}
