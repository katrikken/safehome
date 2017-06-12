package com.kuryshee.safehome.rpi;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements managing the inside logic of the application.
 * @author Ekaterina Kurysheva
 */
public class InsideTasksManager extends Thread{
    
    /**
     * The constant of one second (in milliseconds).
     */
    private long ONE_SEC = 1000;
    
    private static final Logger LOGGER = Logger.getLogger("RPi");
    
    /**
     * This method processes the tasks arriving from other logical parts of the application.
     * @param task specifies the task.
     */
    private void processTask(String task){
        switch (task){
            case ServerChecker.COMMAND_GETSTATE: getstateTask();
                break;         
            case ServerChecker.COMMAND_SWITCHON: switchOn();
                break;
            case ServerChecker.COMMAND_SWITCHOFF: switchOff();                
                break;
            case ServerChecker.COMMAND_TAKEPHOTO: takePhoto();
                break;
            case LocalServerChecker.COMMAND_READTOKEN: readNewCard(task);
                break;
            case LocalServerChecker.COMMAND_UPDATEUSERS: rereadUserConfiguration(task);
                break;
            case RFIDController.REQ_RFIDSWITCH : rfidSwitch();
                break;
        }
    }
    
    /**
     * This method creates program status update request for the server.
     */
    private void getstateTask(){
        Map<String, String> atts = new HashMap<>();
        atts.put(ServerChecker.ID_PARAM, Main.id);
        atts.put(ServerChecker.ATT_ANSWER, 
                Main.motionController.getStateString());
           
        String query = GetRequestSender.formatQuery(ServerChecker.COMMAND_GETSTATE, atts, Main.DEFAULT_ENCODING);
        Main.forServer.add(query);        
    }
    
    /**
     * This method sets the current program mode to "ON".
     */
    private void switchOn() {
        Main.motionController.switchOn();
    }
        
    /**
     * This method sets the current program mode to "OFF".
     */
    private void switchOff(){
        Main.motionController.switchOff();
    }
    
    /**
     * This method invokes a function on {@link MotionController} instance to take a new photo.
     */
    private void takePhoto(){
        Main.motionController.takePhoto();
    }
    
    /**
     * This method passes the tasks to the {@link MotionController} and {@link RFIDController} to start reading new token.
     * @param command is a task to pass to the {@link RFIDController}.
     */
    private void readNewCard(String command){
        Main.motionController.switchOff();
        Main.forRFID.add(command);
    }
    
    /**
     * This method invokes a function on {@link RFIDController} instance to update the information about registered tokens.
     * @param command is a task to pass to the {@link RFIDController}.
     */
    private void rereadUserConfiguration(String command){
        Main.forRFID.add(command);
    }
    
    /**
     * This method switches the program mode upon the reading of a known RFID token.
     */
    private void rfidSwitch(){
        if (Main.motionController.isON()){
            Main.motionController.switchOff();
        }
        else{
            Main.motionController.switchOn();
        }
    }
    
    /**
     * The thread repeatedly checks if any task has arrived.
     */
    @Override
    public void run(){
        while(true){
            if(!Main.insideTasks.isEmpty()){
                LOGGER.log(Level.INFO, "--Inside thread -- Got task");
                processTask(Main.insideTasks.poll());
            }

            try { Thread.sleep(ONE_SEC); }
            catch(InterruptedException e){
                LOGGER.log(Level.SEVERE, "--Inside thread -- Interrupted");
            }
        }
    }
}
