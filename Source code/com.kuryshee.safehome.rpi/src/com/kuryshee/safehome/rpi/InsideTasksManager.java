package com.kuryshee.safehome.rpi;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.kuryshee.safehome.rpicommunicationconsts.RpiCommunicationConsts;
import java.util.Date;

/**
 * Class implements managing the inside logic of the application.
 * 
 * @author Ekaterina Kurysheva
 */
public class InsideTasksManager extends Thread{
    
    /**
     * The instance of the {@link MotionController} class to control this Raspberry Pi GPIO pins.
     */
    private MotionController motionController = new MotionController();
    
    /**
     * Constant for internal command to blink LED.
     */
    public static final String BLINK = "blink";
    /**
     * The constant of one second in milliseconds.
     */
    private long ONE_SEC = 1000;
    
    private static final Logger LOGGER = Logger.getLogger(InsideTasksManager.class.getName());
    
    /**
     * Processes the tasks arriving from other logical parts of the application.
     * @param task specifies the task.
     */
    private void processTask(String task){
        switch (task){
            case BLINK: motionController.blink();
                break;
            case RpiCommunicationConsts.POST_STATE: getstateTask();
                break;         
            case RpiCommunicationConsts.TURN_ON: switchOn();
                break;
            case RpiCommunicationConsts.TURN_OFF: switchOff();                
                break;
            case RpiCommunicationConsts.TAKE_PICTURE: takePhoto();
                break;
            case RpiCommunicationConsts.COMMAND_READTOKEN: readNewCard(task);
                break;
            case RpiCommunicationConsts.COMMAND_UPDATEUSERS: rereadUserConfiguration(task);
                break;
            default: if(task.startsWith(RFIDController.REQ_RFIDSWITCH)){ rfidSwitch(task.substring(RFIDController.REQ_RFIDSWITCH.length()));}
                break;
        }
    }
    
    /**
     * Creates program status update request for the server.
     */
    private void getstateTask(){
        Reporter.reportState(motionController.getStateString());    
    }
    
    /**
     * Sets the current program mode to "ON".
     */
    private void switchOn() {
        motionController.switchOn();
    }
        
    /**
     * Sets the current program mode to "OFF".
     */
    private void switchOff(){
        motionController.switchOff();
    }
    
    /**
     * Invokes a function on {@link MotionController} instance to take a new photo.
     */
    private void takePhoto(){
        motionController.takePhoto();
    }
    
    /**
     * Passes the tasks to the {@link MotionController} and {@link RFIDController} to start reading new token.
     * @param command is a task to pass to the {@link RFIDController}.
     */
    private void readNewCard(String command){
        motionController.switchOff();
        Main.forRFID.add(command);
    }
    
    /**
     * Invokes a function on {@link RFIDController} instance to update the information about registered tokens.
     * @param command is a task to pass to the {@link RFIDController}.
     */
    private void rereadUserConfiguration(String command){
        Main.forRFID.add(command);
    }
    
    /**
     * Method switches the program mode upon the reading of a known RFID token.
     */
    private void rfidSwitch(String name){
        LOGGER.log(Level.INFO, "-- Switching the state from: " + motionController.isON());
        
        String date = new Date().getTime() + "";
        
        if (motionController.isON()){
            switchOff();
            Reporter.reportSwitchOff(date, name);
        }
        else{
            switchOn();
            Reporter.reportSwitchOn(date, name);
        }
    }
    
    /**
     * Checks if any task has arrived in a continuous loop.
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
