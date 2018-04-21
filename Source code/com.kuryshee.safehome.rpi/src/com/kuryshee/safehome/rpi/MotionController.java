package com.kuryshee.safehome.rpi;

import com.kuryshee.safehome.appcommunicationconsts.AppCommunicationConsts;
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.GpioInterrupt;
import com.pi4j.wiringpi.GpioInterruptEvent;
import com.pi4j.wiringpi.GpioInterruptListener;
import com.pi4j.wiringpi.GpioUtil;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class interacts with the web camera, motion sensor and LED.
 * 
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
    
    private final long TWO_SEC = 2000;
    
    /**
     * The format of the saved photos.
     */
    public static String formatOfImage = ".jpg";
    
    /**
     * Attribute indicating the state of the application.
     */
    private boolean ON = false;
    
    private static final Logger LOGGER = Logger.getLogger(MotionController.class.getName());

    private GpioInterruptListener listener;
    
    /**
     * Getter for the property {@link #ON).
     * @return true if the state of the program is "ON".
     */
    public boolean isON() {
        return ON;
    }
     
    /**
     * Constructor sets the motion sensor and LED pin.
     * Blinks three times to indicate that the program is ready for use.
     */
    public MotionController(){
        this.ON = false;
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
            return AppCommunicationConsts.ON;
        }
        else{
            return AppCommunicationConsts.OFF;
        }
    }
    
    /**
     * Sets listener to the Motion Sensor input pin. 
     * The listener reacts to the event in case the state of the MotionController instance is set to "ON".
     * The listener reports to the server about actions.
     */
    private void setPin(){
        listener = (GpioInterruptEvent event) -> {
            if(ON && event.getPin() == PIR_IN && event.getState()){
                LOGGER.log(Level.INFO, " --> GPIO triggered");

                Reporter.reportMotion(new Date().getTime() + "");
                takePhoto();
            }
        };
        
        //GpioInterrupt.addListener(listener);

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

            Date date = new Date();
            String name = date.getTime() + "";
            String path = Main.photoDir + name + formatOfImage;
            Process p = Runtime.getRuntime().exec(command + path);
            
            LOGGER.log(Level.INFO, "--Inside thread - taking photo at: {0}", path);
            
            p.waitFor();
            File file = new File(path);
            if(file.exists()){
                if(file.length() > 0){
                    
                    LOGGER.log(Level.INFO, "--Inside thread - PHOTO IS READY at: {0}", path +":"+ file.length());
                    Main.photoPaths.add(path);
                    Reporter.reportPhoto(date.getTime() + "");
                }
                else{
                    //Sometimes the photo is still not written to the file by this moment
                    long startTime = System.currentTimeMillis();
                    while((System.currentTimeMillis() - startTime) < TWO_SEC)
                    {
                    //wait
                    }
                    if(file.length() > 0){
                        LOGGER.log(Level.INFO, "--Inside thread - PHOTO IS READY at: {0}", path +":"+ file.length());
                        Main.photoPaths.add(path);
                        Reporter.reportPhoto(date.getTime() + "");
                    }
                    else{
                        LOGGER.log(Level.INFO, "--Inside thread - photo {0} was not written to file!", path);
                    }
                }
            }
            else{
                LOGGER.log(Level.INFO, "--Inside thread - photo {0} was not taken!", path);
            }
        }
        catch(IOException | InterruptedException e){
            Logger.getLogger(MotionController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }
    }
    
    /**
     * Sets the current program mode to "OFF"..
     * Blinks LED once to indicate switching OFF.
     */
    public void switchOff(){       
        this.ON = false;
    
        GpioInterrupt.removeListener(listener);
        longBlink();
        
        //Reporter.reportState(AppCommunicationConsts.OFF);
    }
    
    /**
     * Sets the current program mode to "ON".
     * LED blinks two times to indicate setting ON.
     */
    public void switchOn(){
        this.ON = true;
 
        GpioInterrupt.addListener(listener);
        
        blink();
        blink();
        
        //Reporter.reportState(AppCommunicationConsts.ON);
    }
    
    /**
     * Blinks led with {@link #TWO_SEC} interval.
     */
    private void longBlink(){
        long startTime = System.currentTimeMillis();
        
        Gpio.digitalWrite(LED_OUT, Gpio.HIGH);
        
        while((System.currentTimeMillis() - startTime) < TWO_SEC)
        {
        //wait
        }
             
        Gpio.digitalWrite(LED_OUT, Gpio.LOW);    
        
        while((System.currentTimeMillis() - startTime) < HALF_SEC * 2)
        {
        //wait before the next blink
        }
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
