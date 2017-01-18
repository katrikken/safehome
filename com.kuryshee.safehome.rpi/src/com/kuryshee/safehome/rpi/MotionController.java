/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kuryshee.safehome.rpi;

import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.GpioInterrupt;
import com.pi4j.wiringpi.GpioInterruptEvent;
import com.pi4j.wiringpi.GpioUtil;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class interacts with the web camera and motion sensor.
 * sends 
 * REQ_RFIDSWITCH (/rfid?rpi=id&key=user)
 * REQ_MOTIONDETECTED (/motiondetected?rpi=id)
 * REQ_PHOTOTAKEN (/photo?rpi=id)
 * UPLOAD_PHOTO (/upload)
 * reacts to
 * COMMAND_GETSTATE (/getstate?rpi=id&answer=state) 
 * COMMAND_SWITCHON (/switchon?rpi=id&answer=ok)
 * COMMAND_SWITCHOFF (/switchoff?rpi=id&answer=ok)
 * COMMAND_TAKEPHOTO (/photo?rpi=id)
 */
public class MotionController extends Thread{
    private boolean currentState = false;
    private final String id;
    private final String photoDir;
    private final int PIR_IN = 11; //GPIO 11 pin number for motion sensor output
    private DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");    
    
    private long millis = 1000;
    
    private final String REQ_MOTIONDETECTED = "/motiondetected";
    private final String ATT_ANSWER = "answer";
    private final String ATT_KEY = "key";
    
    /**
     * This constructor creates a thread and sets the motion sensor output pin.
     * @param id is the ID of this instance of the program, known to the main server.
     * @param photoDir is the directory for photo storage.
     */
    public MotionController(String id, String photoDir){
        this.id = id;
        this.photoDir = photoDir;
        setPin();
    }
    
    /**
     * This method sets listener to the Motion Sensor output pin without creation of new GPIO Controller instance. 
     * The listener reacts to the event in case the state of the MotionController instance is set to "ON".
     */
    private void setPin(){
        GpioInterrupt.addListener((GpioInterruptEvent event) -> {
            if(currentState && event.getState()){
                System.out.println(" --> GPIO trigger callback received");
      
                //Sending query string for server indicating that motion has been detected.
                String[] atts = {ComKurysheeSafehomeRpi.ATT_RPI, id};
                addQuery(REQ_MOTIONDETECTED, atts, ComKurysheeSafehomeRpi.STD_CHARSET);  
                
                String path = takePhoto();
                if(path.length() != 0){
                    ComKurysheeSafehomeRpi.photoPaths.add(path);
                    
                    String[] attsp = {ComKurysheeSafehomeRpi.ATT_RPI, id};
                    addQuery(ComKurysheeSafehomeRpi.REQ_PHOTOTAKEN, attsp, ComKurysheeSafehomeRpi.STD_CHARSET); 
                }
            }
        });

        GpioUtil.export(PIR_IN, GpioUtil.DIRECTION_IN);
        GpioUtil.setEdgeDetection(PIR_IN, GpioUtil.EDGE_BOTH);      
        Gpio.pinMode(PIR_IN, Gpio.INPUT);
        Gpio.pullUpDnControl(PIR_IN, Gpio.PUD_DOWN);
        GpioInterrupt.enablePinStateChangeCallback(PIR_IN);       
            
        System.out.println("--Inside thread -- PIN is set");
    }
    
    /**
     * This method takes a photo and saves it into the directory set in the configuration file.
     * @return String photo directory.
     */
    private String takePhoto(){
        try{
            String command = "fswebcam ";

            String date = dateFormat.format(new Date());
            String path = photoDir + date + ComKurysheeSafehomeRpi.formatOfImage;
            Process p = Runtime.getRuntime().exec(command + path);
                
            System.out.println("--Inside thread - took photo at: " + path);
            return path;
        }
        catch(IOException e){
            System.out.println("--Inside thread - could not take a photo");
            return "";
        }
    }
        
    /**
     * This method encodes given attributes to the query string.
     * @param command is the directory under the main server address.
     * @param attributes is an array of parameter names and parameter values.
     * @param charset is the charset for the encoding.
     */
    private void addQuery(String command, String[] attributes, String charset){
        try{
            StringBuilder query = new StringBuilder(command + '?');
            for (int i = 0; i < attributes.length / 2; i++){
                query.append(attributes[i*2]);
                query.append('=');
                query.append(URLEncoder.encode(attributes[i*2 + 1], charset));
                if (i != attributes.length / 2 - 1){
                    query.append('&');
                }
            }
               
            ComKurysheeSafehomeRpi.forServer.add(query.toString());
        }
        catch(UnsupportedEncodingException e){
            System.out.println("--Inside thread -- could not form the query string");          
        }
    }
    
    private void processTask(String task){
        switch (task){
            case ComKurysheeSafehomeRpi.COMMAND_GETSTATE: getstateTask();
                break;         
            case ComKurysheeSafehomeRpi.COMMAND_SWITCHON: switchOn();
                break;
            case ComKurysheeSafehomeRpi.COMMAND_SWITCHOFF: switchOff();                
                break;
            case ComKurysheeSafehomeRpi.COMMAND_TAKEPHOTO: takePhotoCommand();
                break;
            default: 
                if (task.startsWith(ComKurysheeSafehomeRpi.REQ_RFIDSWITCH)){
                        //The RFID switch command has attribute user preceeded with '=' char.
                    rfidSwitch(task.substring(ComKurysheeSafehomeRpi.REQ_RFIDSWITCH.length() + 1));
                }
                break;
        }
    }
    
    /**
     * This method invokes function takePhoto() and prepares response to the server.
     */
    private void takePhotoCommand(){
        String path = takePhoto();
            if(path.length() != 0){
                ComKurysheeSafehomeRpi.photoPaths.add(path);
                    
                String[] attsp = {ComKurysheeSafehomeRpi.ATT_RPI, id};
                addQuery(ComKurysheeSafehomeRpi.REQ_PHOTOTAKEN, attsp, ComKurysheeSafehomeRpi.STD_CHARSET); 
            }
        ComKurysheeSafehomeRpi.forServer.add(ComKurysheeSafehomeRpi.UPLOAD_PHOTO);
    }
    
    /**
     * This method creates program status update request for the server.
     */
    private void getstateTask(){
        String state;
        if (currentState){
            state = "on";
        }
        else{
            state = "off";
        }
               
        String[] atts = {ComKurysheeSafehomeRpi.ATT_RPI, id, ATT_ANSWER, state};
        addQuery(ComKurysheeSafehomeRpi.COMMAND_GETSTATE, atts, ComKurysheeSafehomeRpi.STD_CHARSET);           
    }
    
    /**
     * This method sets the current program mode to "ON".
     */
    private void switchOn() {
        //for the thread debugging purposes
        if(!currentState){       
            System.out.println("--Inside thread -- returned query ");
        }
        else{               
            System.out.println("--Inside thread -- already switched on --returned query " ); 
        }
        
        currentState = true;

        //Sending query string for server that switching on succeded.
        String[] atts = {ComKurysheeSafehomeRpi.ATT_RPI, id, ATT_ANSWER, ComKurysheeSafehomeRpi.OK_ANSWER};
        addQuery(ComKurysheeSafehomeRpi.COMMAND_SWITCHON, atts, ComKurysheeSafehomeRpi.STD_CHARSET);
    }
        
    /**
     * This method sets the current program mode to "OFF".
     */
    private void switchOff(){
        //for the thread debugging purposes
        if(currentState){        
            System.out.println("--Inside thread -- returned query ");          
        }
        else{
            System.out.println("--Inside thread -- already switched off");
        }
        
        currentState = false;
                
        //Sending query string indicating that switching off succeded.
        String[] atts = {ComKurysheeSafehomeRpi.ATT_RPI, id, ATT_ANSWER, ComKurysheeSafehomeRpi.OK_ANSWER};
        addQuery(ComKurysheeSafehomeRpi.COMMAND_SWITCHOFF, atts, ComKurysheeSafehomeRpi.STD_CHARSET);
    }
    
    /**
     * This method switches the program mode upon the reading of a known RFID tag and sends to the server the updated state.
     * @param tag 
     */
    private void rfidSwitch(String tag){
        if (currentState){
            switchOff();
        }
        else{
            switchOn();
        }
           
        String[] attributes = {ComKurysheeSafehomeRpi.ATT_RPI, id, ATT_KEY, tag};
        addQuery(ComKurysheeSafehomeRpi.REQ_RFIDSWITCH, attributes, ComKurysheeSafehomeRpi.STD_CHARSET);
        
        getState();
        System.out.println("--Inside thread -- rfid switch command done.");
    }
    
    /**
     * The thread repeatedly checks if any task has occurred.
     */
    @Override
    public void run(){
        while(true){
            if(!ComKurysheeSafehomeRpi.insideTasks.isEmpty()){
                System.out.println("--Inside thread -- Got task");
                processTask(ComKurysheeSafehomeRpi.insideTasks.poll());
            }

            try { Thread.sleep(millis); }
            catch(InterruptedException e){
                System.out.println("--Inside thread -- Interrupted");
            }
        }
    }
}
