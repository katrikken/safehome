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
 * Class interacts with the web camera, motion sensor and LED.
 * @author Ekaterina Kurysheva
 */
public final class MotionController{
    
    /**
     * GPIO pin number for motion sensor (PIR) output in wiringPi numbering.
     */
    private final int PIR_IN = 11;
    
    /**
     * GPIO pin number for LED output in wiringPi numbering.
     */
    private final int LED_OUT = 1;
    
    /**
     * 500 milliseconds.
     */
    private final long HALF_SEC = 500;
    
    /**
     * Date format which is used to capture the time when the photo was taken.
     */
    public static DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");   
    
    /**
     * The constant which is added to the address of a server when reporting motion detection.  
     */
    public static final String REQ_MOTIONDETECTED = "/motiondetected";
    
    /**
     * The constant which is added to the address of a server when reporting taking of a new photo.  
     */
    public static final String REQ_PHOTOTAKEN = "/photo";  
    
    /**
     * Attribute indicating the state of the application.
     */
    private boolean ON = false;
    
    private static final Logger LOGGER = Logger.getLogger("Motion Controller");

    /**
     * Getter for the property {@link #ON).
     * @return true if the state of the program is "ON".
     */
    public boolean isON() {
        return ON;
    }

    /**
     * Setter for the Boolean property {@link #ON). 
     * @param ON is true in case the state should be "ON".
     */
    private void setON(boolean ON) {
        this.ON = ON;
    }
     
    /**
     * Constructor sets the motion sensor and LED pin.
     * Blinks three times to indicate that the program is ready for use.
     */
    public MotionController(){
        setON(false);
        setPin();
        setLED();
        
        blink();
        blink();
        blink();
    }
    
    /**
     * Returns the string specifying the state of the program.
     * @return "on" or "off" accordingly.
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
     * Sets listener to the Motion Sensor input pin. 
     * The listener reacts to the event in case the state of the MotionController instance is set to "ON".
     * The listener reports to the server about actions.
     */
    private void setPin(){
        GpioInterrupt.addListener((GpioInterruptEvent event) -> {
            if(event.getPin() == PIR_IN && isON() && event.getState()){
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
     * Sets the output pin for LED.
     */
    private void setLED(){
        GpioUtil.export(LED_OUT, GpioUtil.DIRECTION_OUT);
        Gpio.pinMode(LED_OUT, Gpio.OUTPUT);  
    }
    
    
    /**
     * Sends the command to take a photo and save it into the directory set in the configuration file.
     * Reports the path to the photo to the {@link Main#photoPaths}.
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
     * Sets the current program mode to "OFF" and reports this to the server.
     * Blinks LED once to indicate switching OFF.
     */
    public void switchOff(){       
        setON(false);
        Map<String, String> atts = new HashMap<>();
        atts.put(ServerChecker.ATT_ANSWER, AnswerConstants.OK_ANSWER);
        report(ServerChecker.COMMAND_SWITCHOFF, atts);
        blink();
    }
    
    /**
     * Sets the current program mode to "ON" and reports this to the server.
     * LED blinks two times to indicate setting ON.
     */
    public void switchOn(){
        setON(true);
        Map<String, String> atts = new HashMap<>();
        atts.put(ServerChecker.ATT_ANSWER, AnswerConstants.OK_ANSWER);
        report(ServerChecker.COMMAND_SWITCHON, atts);
        
        blink();
        blink();
    }
    
    /**
     * Creates a query string to specify the state of the application and passes it to the {@link Main#forServer}.
     * Adds this Raspberry Pi id to every query.
     * @param command specifies the command upon which the state has been changed.
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
    
    /**
     * Blinks led once with {@link #HALF_SEC} interval.
     */
    public void blink(){
 
        long startTime = System.currentTimeMillis();
        
        Gpio.digitalWrite(LED_OUT, Gpio.HIGH);
        
        while((System.currentTimeMillis() - startTime) < HALF_SEC)
        {
        //wait
        }
             
        Gpio.digitalWrite(LED_OUT, Gpio.LOW);    
        
        while((System.currentTimeMillis() - startTime) < HALF_SEC * 2)
        {
        //wait before the next blink
        }
    }
}
