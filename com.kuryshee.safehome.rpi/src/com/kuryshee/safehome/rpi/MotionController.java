package com.kuryshee.safehome.rpi;

import com.kuryshee.safehome.httprequestsender.AnswerConstants;
import com.kuryshee.safehome.httprequestsender.GetRequestSender;
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.GpioInterrupt;
import com.pi4j.wiringpi.GpioInterruptEvent;
import com.pi4j.wiringpi.GpioUtil;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class interacts with the web camera and motion sensor.
 * @author Ekaterina Kurysheva
 */
public class MotionController{
    
    /**
     * GPIO pin number for motion sensor output.
     */
    private final int PIR_IN = 11;
    
    /**
     * The date format which is used to capture the time when the photo is taken.
     */
    public static DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");   
    
    /**
     * The constant which is added to the address of a server when reporting motion detection.  
     */
    public static final String REQ_MOTIONDETECTED = "/motiondetected";
    
    /**
     * The constant which is added to the address of a server when reporting the taking of a new photo.  
     */
    public static final String REQ_PHOTOTAKEN = "/photo";  
    
    private boolean ON = false;
    
    private static final Logger LOGGER = Logger.getLogger("Motion Controller");

    /**
     * Getter for the property ON.
     * @return true if the state of the program is "ON".
     */
    public boolean isON() {
        return ON;
    }

    /**
     * Setter for the Boolean property ON, which indicates the state of the program. 
     * @param ON is true in case the state should be "ON".
     */
    private void setON(boolean ON) {
        this.ON = ON;
    }
     
    /**
     * This constructor creates a thread and sets the motion sensor output pin.
     */
    public MotionController(){
        setON(false);
        setPin();
    }
    
    /**
     * This method returns the string specifying the state of the program.
     * @return "on" or "off".
     */
    public String getStateString(){
        if (isON()){
            return "on";
        }
        else{
            return "off";
        }
    }
    
    /**
     * This method sets listener to the Motion Sensor output pin without creation of new GPIO Controller instance. 
     * The listener reacts to the event in case the state of the MotionController instance is set to "ON".
     * The listener reports to the server about actions.
     */
    private void setPin(){
        GpioInterrupt.addListener((GpioInterruptEvent event) -> {
            if(isON() && event.getState()){
                LOGGER.log(Level.INFO, " --> GPIO triggered");
                
                String date = dateFormat.format(new Date());
                Map<String, String> atts = new HashMap<>();
                atts.put(ServerChecker.TIME_PARAM, date);
                report(REQ_MOTIONDETECTED, atts);

                takePhoto();         
            }
        });

        GpioUtil.export(PIR_IN, GpioUtil.DIRECTION_IN);
        GpioUtil.setEdgeDetection(PIR_IN, GpioUtil.EDGE_BOTH);      
        Gpio.pinMode(PIR_IN, Gpio.INPUT);
        Gpio.pullUpDnControl(PIR_IN, Gpio.PUD_DOWN);
        GpioInterrupt.enablePinStateChangeCallback(PIR_IN);       
            
        LOGGER.log(Level.INFO, "--Inside thread -- PIN is set");
    }
    
    
    /**
     * This method takes a photo and saves it into the directory set in the configuration file.
     * It reports the path to the photo to the {@link Main#photoPaths}.
     */
    public void takePhoto(){
        try{
            String command = "fswebcam ";

            String date = dateFormat.format(new Date());
            String path = Main.photoDir + date + Main.formatOfImage;
            Process p = Runtime.getRuntime().exec(command + path);
                
            LOGGER.log(Level.INFO, "--Inside thread - took photo at: {0}", path);

            Main.photoPaths.add(path);
            
            report(REQ_PHOTOTAKEN, null);
        }
        catch(IOException e){
            LOGGER.log(Level.WARNING, "--Inside thread - could not take a photo");
        }
    }
    
    /**
     * This method sets the current program mode to "OFF" and reports it to the server.
     */
    public void switchOff(){       
        setON(false);
        Map<String, String> atts = new HashMap<>();
        atts.put(ServerChecker.ATT_ANSWER, AnswerConstants.OK_ANSWER);
        report(ServerChecker.COMMAND_SWITCHOFF, atts);
    }
    
    /**
     * This method sets the current program mode to "ON" and reports it to the server.
     */
    public void switchOn(){
        setON(true);
        Map<String, String> atts = new HashMap<>();
        atts.put(ServerChecker.ATT_ANSWER, AnswerConstants.OK_ANSWER);
        report(ServerChecker.COMMAND_SWITCHON, atts);
    }
    
    /**
     * This method creates a query string to specify the state of the application and passes it to the {@link Main#forServer}.
     * It adds this Raspberry Pi id to every query.
     * @param command specifies the command upon which the state has been changed.
     * @param atts is a map of attributes and their values.
     * @param atts is a map of attributes for the query. In case it is null, the default attribute with this Raspberry Pi id is added.
     */
    private void report(String command, Map<String, String> atts){
        if(atts == null){
            atts = new HashMap<>();          
        }         
           
        atts.put(ServerChecker.ID_PARAM, Main.id);
        
        String query = GetRequestSender.formatQuery(command, atts, Main.DEFAULT_ENCODING);
        Main.forServer.add(query);
    }
}
