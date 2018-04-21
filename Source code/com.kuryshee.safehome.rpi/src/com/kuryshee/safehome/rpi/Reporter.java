package com.kuryshee.safehome.rpi;

import com.kuryshee.safehome.appcommunicationconsts.AppCommunicationConsts;
import com.kuryshee.safehome.rpicommunicationconsts.RpiCommunicationConsts;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Class implements manager of report messages for the server.
 * 
 * @author Ekaterina Kurysheva
 */
public class Reporter {
    
     /**
     * Queue of strings with tasks which should be processed by the {@link ServerChecker}.
     */
    public static ConcurrentLinkedQueue< Map< String, String>> forServer = new ConcurrentLinkedQueue<>();
    
    /**
     * Forms a report for registration of changes in user list.
     * @param user 
     */
    public static void reportUserChange(UserBean user){
        Map<String, String> atts = new HashMap<>();
        atts.put(RpiCommunicationConsts.ACTION, RpiCommunicationConsts.REGISTER_USER);
        atts.put(RpiCommunicationConsts.USER_LOGIN, user.getName());
        atts.put(RpiCommunicationConsts.USER_PASSWORD, user.getPassword());
        
        forServer.add(atts);
    }
    
    /**
     * Forms a report for deleting of the user.
     * @param user to report
     */
    public static void reportDeleteUser(UserBean user){
        Map<String, String> atts = new HashMap<>();
        atts.put(RpiCommunicationConsts.ACTION, RpiCommunicationConsts.DELETE_USER);
        atts.put(RpiCommunicationConsts.USER_LOGIN, user.getName());
        
        forServer.add(atts);
    }
    
    /**
     * Forms a report for changed state of Raspberry Pi.
     * @param state to report
     */
    public static void reportState(String state){
        Map<String, String> atts = new HashMap<>();
        atts.put(RpiCommunicationConsts.ACTION, RpiCommunicationConsts.POST_STATE);
        atts.put(RpiCommunicationConsts.STATE, state);

        forServer.add(atts);  
    }
    
    /**
     * Forms a report for switching of the state by the user.
     * @param date of event
     * @param reportMessage information about the event
     */
    private static void reportSwitch(String date, String reportMessage){
        Map<String, String> atts = new HashMap<>();
        atts.put(RpiCommunicationConsts.ACTION, RpiCommunicationConsts.REGISTER_ACTION);
        atts.put(RpiCommunicationConsts.LEVEL, AppCommunicationConsts.NORMAL);
        atts.put(RpiCommunicationConsts.TIME, date);
        atts.put(RpiCommunicationConsts.RPI_ACTION_INFO, reportMessage);
        
        forServer.add(atts);
    }
    
    /**
     * Forms a report of turning the application on by a user.
     * @param date of the event
     * @param name of user
     */
    public static void reportSwitchOn(String date, String name){
        reportSwitch(date, name + AppCommunicationConsts.RFIDSWITCHON);
    }
    
    /**
     * Forms a report of turning the application off by a user.
     * @param date of the event
     * @param name of user
     */
    public static void reportSwitchOff(String date, String name){
        reportSwitch(date, name + AppCommunicationConsts.RFIDSWITCHOFF);
    }
    
    /**
     * Forms a report of motion detection.
     * @param date of the event
     */
    public static void reportMotion(String date){
        Map<String, String> atts = new HashMap<>();
        atts.put(RpiCommunicationConsts.ACTION, RpiCommunicationConsts.REGISTER_ACTION);
        atts.put(RpiCommunicationConsts.TIME, date);
        atts.put(RpiCommunicationConsts.LEVEL, AppCommunicationConsts.DANGEROUS);
        atts.put(RpiCommunicationConsts.RPI_ACTION_INFO, AppCommunicationConsts.MOTION);

        forServer.add(atts);  
    }
    
    /**
     * Forms a report of taking picture.
     * @param date of the event
     */
    public static void reportPhoto(String date){
        Map<String, String> atts = new HashMap<>();
        atts.put(RpiCommunicationConsts.ACTION, RpiCommunicationConsts.REGISTER_ACTION);
        atts.put(RpiCommunicationConsts.TIME, date);
        atts.put(RpiCommunicationConsts.LEVEL, AppCommunicationConsts.NORMAL);
        atts.put(RpiCommunicationConsts.RPI_ACTION_INFO, AppCommunicationConsts.PHOTO);
        
        forServer.add(atts);  
    }
}
