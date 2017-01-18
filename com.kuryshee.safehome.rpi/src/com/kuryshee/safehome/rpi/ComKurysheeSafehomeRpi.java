package com.kuryshee.safehome.rpi;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.io.File;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ComKurysheeSafehomeRpi {
    
    //These data are read from configuration file in the main() method.
    private static String id;
    private static String serverAddress;
    private static String photoDir;

    public static String formatOfImage = ".jpg";

    //Standard charset for communication with server.
    public static final String STD_CHARSET = "UTF-8";
    
    //The following set of key words serves for templated query building purposes.
    //This part of key words are server commands.
    public static final String COMMAND_GETSTATE = "/getstate";
    public static final String COMMAND_SWITCHOFF = "/switchoff";
    public static final String COMMAND_SWITCHON = "/switchon";
    public static final String COMMAND_TAKEPHOTO = "/takephoto";
    
    //This part of key words are application requests.
    public static final String REQ_RFIDSWITCH = "/rfid";  
    public static final String REQ_MOTIONDETECTED = "/motiondetected";
    public static final String REQ_PHOTOTAKEN = "/photo";  
    
    public static final String UPLOAD_PHOTO = "/upload";  
    public static final String ATT_RPI = "rpi";
    public static final String NO_ANSWER = "no answer";
    public static final String OK_ANSWER = "ok";
    public static final String ERROR_ANSWER = "error";
     
    /* Configuration file for RFID keys is created in the main method. 
    * This is the explicit path for the file. */
    private static final String CONFIG_KEYSFILE = "keys.txt";
    
    //The path to the configuration file.
    private static final String CONFIG_MAINFILE = "config.txt";
    
    //Theese key words are for reading configuration files.
    private static final String KEYWORD_SERVER = "server";
    private static final String KEYWORD_PHOTO = "photo";
    private static final String KEYWORD_MYID = "myID";   
    
    //These queues contain tasks for threads.
    public static ConcurrentLinkedQueue<String> forServer = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<String> insideTasks = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<String> photoPaths = new ConcurrentLinkedQueue<>();
    
    //The map of RFID tags and usernames assosiated with them.
    private static HashMap<String, String> rfidKeys = new HashMap<>(); 

    public static void main(String[] args) {
        try{
            
            /* Firstly programm reads configuration file containing server address, 
             * ID of this particular instance,
             * path for saving photos */
            
            BufferedReader bfr = new BufferedReader(new FileReader(CONFIG_MAINFILE));
            int c;
            StringBuilder strb = new StringBuilder();
            while((c = bfr.read()) != -1){
                if ((char) c != '\n' && (char) c != '\r' && c != -1){
                    strb.append((char) c);
                }
                else{
                    if (strb.toString().startsWith(KEYWORD_SERVER)){        
                        serverAddress = strb.toString().split("=")[1];
                        
                        System.out.println("--Config: server address == " + serverAddress);
                    }
                    if(strb.toString().startsWith(KEYWORD_MYID)){
                        id = strb.toString().split("=")[1];
                        
                        System.out.println("--Config: id == " + id);
                    }
                    if(strb.toString().startsWith(KEYWORD_PHOTO)){
                        photoDir = strb.toString().split("=")[1];
                        
                        System.out.println("--Config: photo path == " + photoDir);
                    }
                    strb = new StringBuilder();
                }
            }
            
            //Creating a configuration file for RFID keys for the during the first launch.
            File keysConfig = new File(CONFIG_KEYSFILE);
            if(!keysConfig.exists() ||  keysConfig.isDirectory()){
                keysConfig.createNewFile();
            }
            
             //Debugging 
            rfidKeys.put("36D9C53B11", "chip");
            rfidKeys.put("C4703ED55F", "card");
            
            //Starting threads.
            RFIDController rfidThread = new RFIDController(CONFIG_KEYSFILE);  
            MotionController insideThread = new MotionController(id, photoDir);
            ServerChecker requestThread = new ServerChecker(id, serverAddress, photoDir);
             
            
            System.out.println("--Starting thread for inside");
            insideThread.start();

            System.out.println("--Starting thread for server");
            //requestThread.setPriority(Thread.MAX_PRIORITY);
            requestThread.start();
            
            System.out.println("--Starting thread for rfid");
            rfidThread.start();
        }
        catch(IOException e){
            System.out.println("--Could not find configuration file. Programm stopped");
        }
    }
    
    
}