/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kuryshee.safehome.rpi;

import static com.kuryshee.safehome.rpi.ComKurysheeSafehomeRpi.log;
import java.util.logging.Level;

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
 * COMMAND_READCARD (/read?card=id)
 * @author Katrikken
 */
public class InsideTasksManager extends Thread{
    private long millis = 1000;
    private final String ATT_ANSWER = "answer";
    private final String ATT_KEY = "key";
    
    private void processTask(String task){
        switch (task){
            case ComKurysheeSafehomeRpi.COMMAND_GETSTATE: getstateTask();
                break;         
            case ComKurysheeSafehomeRpi.COMMAND_SWITCHON: switchOn();
                break;
            case ComKurysheeSafehomeRpi.COMMAND_SWITCHOFF: switchOff();                
                break;
            case ComKurysheeSafehomeRpi.COMMAND_TAKEPHOTO: takePhoto();
                break;
            case ComKurysheeSafehomeRpi.COMMAND_READCARD: readNewCard();
                break;
            case ComKurysheeSafehomeRpi.COMMAND_SAVEUSER: rereadUserConfiguration();
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
     * This method creates program status update request for the server.
     */
    private void getstateTask(){
        String state = ComKurysheeSafehomeRpi.motionController.getState();    
        String[] atts = {ComKurysheeSafehomeRpi.ATT_RPI, ComKurysheeSafehomeRpi.id, ATT_ANSWER, state};
        ComKurysheeSafehomeRpi.addQuery(ComKurysheeSafehomeRpi.COMMAND_GETSTATE, atts, ComKurysheeSafehomeRpi.STD_CHARSET);           
    }
    
    /**
     * This method sets the current program mode to "ON".
     */
    private void switchOn() {
        //for the thread debugging purposes
        if(!ComKurysheeSafehomeRpi.motionController.isON()){       
            log.log(Level.INFO, "--Inside thread -- switched on");
        }
        else{               
            log.log(Level.INFO, "--Inside thread -- already switched on" ); 
        }
        
        ComKurysheeSafehomeRpi.motionController.setON(true);

        //Sending query string for server that switching on succeded.
        String[] atts = {ComKurysheeSafehomeRpi.ATT_RPI, ComKurysheeSafehomeRpi.id, ATT_ANSWER, ComKurysheeSafehomeRpi.OK_ANSWER};
        ComKurysheeSafehomeRpi.addQuery(ComKurysheeSafehomeRpi.COMMAND_SWITCHON, atts, ComKurysheeSafehomeRpi.STD_CHARSET);
    }
        
    /**
     * This method sets the current program mode to "OFF".
     */
    private void switchOff(){
        //for the thread debugging purposes
        if(ComKurysheeSafehomeRpi.motionController.isON()){        
            log.log(Level.INFO, "--Inside thread -- returned query ");          
        }
        else{
            log.log(Level.INFO, "--Inside thread -- already switched off");
        }
        
        ComKurysheeSafehomeRpi.motionController.setON(false);
                
        //Sending query string indicating that switching off succeded.
        String[] atts = {ComKurysheeSafehomeRpi.ATT_RPI, ComKurysheeSafehomeRpi.id, ATT_ANSWER, ComKurysheeSafehomeRpi.OK_ANSWER};
        ComKurysheeSafehomeRpi.addQuery(ComKurysheeSafehomeRpi.COMMAND_SWITCHOFF, atts, ComKurysheeSafehomeRpi.STD_CHARSET);
    }
    
    /**
     * This method invokes function takePhoto() and prepares response to the server.
     */
    private void takePhoto(){
        String path = ComKurysheeSafehomeRpi.motionController.takePhoto();
            if(path.length() != 0){
                ComKurysheeSafehomeRpi.photoPaths.add(path);
                    
                String[] attsp = {ComKurysheeSafehomeRpi.ATT_RPI, ComKurysheeSafehomeRpi.id};
                ComKurysheeSafehomeRpi.addQuery(ComKurysheeSafehomeRpi.REQ_PHOTOTAKEN, attsp, ComKurysheeSafehomeRpi.STD_CHARSET); 
            }
        ComKurysheeSafehomeRpi.forServer.add(ComKurysheeSafehomeRpi.UPLOAD_PHOTO);
    }
    
    private void readNewCard(){
        switchOff();
        ComKurysheeSafehomeRpi.forRFID.add(ComKurysheeSafehomeRpi.COMMAND_READCARD);
    }
    
    private void rereadUserConfiguration(){
        ComKurysheeSafehomeRpi.forRFID.add(ComKurysheeSafehomeRpi.COMMAND_SAVEUSER);
    }
    
    /**
     * This method switches the program mode upon the reading of a known RFID tag and sends to the server the updated state.
     * @param tag 
     */
    private void rfidSwitch(String tag){
        if (ComKurysheeSafehomeRpi.motionController.isON()){
            switchOff();
        }
        else{
            switchOn();
        }
           
        String[] attributes = {ComKurysheeSafehomeRpi.ATT_RPI, ComKurysheeSafehomeRpi.id, ATT_KEY, tag};
        ComKurysheeSafehomeRpi.addQuery(ComKurysheeSafehomeRpi.REQ_RFIDSWITCH, attributes, ComKurysheeSafehomeRpi.STD_CHARSET);
        
        getState();
        log.log(Level.INFO, "--Inside thread -- rfid switch command done.");
    }
    
    /**
     * The thread repeatedly checks if any task has occurred.
     */
    @Override
    public void run(){
        while(true){
            if(!ComKurysheeSafehomeRpi.insideTasks.isEmpty()){
                log.log(Level.INFO, "--Inside thread -- Got task");
                processTask(ComKurysheeSafehomeRpi.insideTasks.poll());
            }

            try { Thread.sleep(millis); }
            catch(InterruptedException e){
                log.log(Level.SEVERE, "--Inside thread -- Interrupted");
            }
        }
    }

}
