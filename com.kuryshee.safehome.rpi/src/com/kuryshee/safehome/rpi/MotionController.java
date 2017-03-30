/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kuryshee.safehome.rpi;

import static com.kuryshee.safehome.rpi.ComKurysheeSafehomeRpi.log;
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.GpioInterrupt;
import com.pi4j.wiringpi.GpioInterruptEvent;
import com.pi4j.wiringpi.GpioUtil;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

/**
 * This class interacts with the web camera and motion sensor.
 */
public class MotionController{
    private final int PIR_IN = 11; //GPIO 11 pin number for motion sensor output
    private DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");     
    private final String REQ_MOTIONDETECTED = "/motiondetected";
    private boolean ON = false;

    public boolean isON() {
        return ON;
    }

    public void setON(boolean ON) {
        this.ON = ON;
    }
     
    /**
     * This constructor creates a thread and sets the motion sensor output pin.
     */
    public MotionController(){
        setON(false);
        setPin();
    }
    
    public String getState(){
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
     */
    private void setPin(){
        GpioInterrupt.addListener((GpioInterruptEvent event) -> {
            if(isON() && event.getState()){
                log.log(Level.INFO, " --> GPIO trigger callback received");
      
                //Sending query string for server indicating that motion has been detected.
                String[] atts = {ComKurysheeSafehomeRpi.ATT_RPI, ComKurysheeSafehomeRpi.id};
                ComKurysheeSafehomeRpi.addQuery(REQ_MOTIONDETECTED, atts, ComKurysheeSafehomeRpi.STD_CHARSET);  
                
                String path = takePhoto();
                if(path.length() != 0){
                    ComKurysheeSafehomeRpi.photoPaths.add(path);
                    
                    String[] attsp = {ComKurysheeSafehomeRpi.ATT_RPI, ComKurysheeSafehomeRpi.id};
                    ComKurysheeSafehomeRpi.addQuery(ComKurysheeSafehomeRpi.REQ_PHOTOTAKEN, attsp, ComKurysheeSafehomeRpi.STD_CHARSET); 
                }
            }
        });

        GpioUtil.export(PIR_IN, GpioUtil.DIRECTION_IN);
        GpioUtil.setEdgeDetection(PIR_IN, GpioUtil.EDGE_BOTH);      
        Gpio.pinMode(PIR_IN, Gpio.INPUT);
        Gpio.pullUpDnControl(PIR_IN, Gpio.PUD_DOWN);
        GpioInterrupt.enablePinStateChangeCallback(PIR_IN);       
            
        log.log(Level.INFO, "--Inside thread -- PIN is set");
    }
    
    
    /**
     * This method takes a photo and saves it into the directory set in the configuration file.
     * @return String photo directory.
     */
    public String takePhoto(){
        try{
            String command = "fswebcam ";

            String date = dateFormat.format(new Date());
            String path = ComKurysheeSafehomeRpi.photoDir + date + ComKurysheeSafehomeRpi.formatOfImage;
            Process p = Runtime.getRuntime().exec(command + path);
                
            log.log(Level.INFO, "--Inside thread - took photo at: {0}", path);
            return path;
        }
        catch(IOException e){
            log.log(Level.WARNING, "--Inside thread - could not take a photo");
            return "";
        }
    }
}
