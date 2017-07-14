package com.kuryshee.safehome.rpi;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class initializes program threads and sets the configuration of the application.
 * @author Ekaterina Kurysheva
 */
public class Main {
    /**
     * This Raspberry Pi ID string.
     */
    public static String id;
    
    /**
     * The URL address of the main server.
     */
    public static String serverAddress;
    
    /**
     * The directory on the Raspberry Pi where the photos are saved to.
     */
    public static String photoDir;
    
    /**
     * The URL address of the local server on this Raspberry Pi.
     */
    public static String localServerAddress;

    /**
     * The format of the saved photos.
     */
    public static String formatOfImage = ".jpg";

    /**
     * The default encoding for the communication over HTTP.
     */
    public static final String DEFAULT_ENCODING = "UTF-8";
     
    /**
     * The path to the configuration file with information about registered users.
     */
    public static final String CONFIG_KEYSFILE = "/home/pi/NetBeansProjects/com.kuryshee.safehome.rpi/dist/keys.txt";
    
    /**
     * The path to the configuration file with definitions of variables 
     * {@link #id}, {@link #localServerAddress}, {@link #photoDir}, {@link #serverAddress}.
     */
    private static final String CONFIG_MAINFILE = "/home/pi/NetBeansProjects/com.kuryshee.safehome.rpi/dist/config.txt";;
    
    //Theese are key words for reading configuration files.
    private static final String KEYWORD_SERVER = "server";
    private static final String KEYWORD_PHOTO = "photo";
    private static final String KEYWORD_MYID = "myID";   
    private static final String KEYWORD_LOCSERVER ="locserver";
    private static final String KEYWORD_DELIM = "=";
    
    /**
     * Queue of strings with tasks which should be processed by the {@link ServerChecker}.
     */
    public static ConcurrentLinkedQueue<String> forServer = new ConcurrentLinkedQueue<>();
    
    /**
     * Queue of strings with tasks which should be processed by the {@link InsideTasksManager}.
     */
    public static ConcurrentLinkedQueue<String> insideTasks = new ConcurrentLinkedQueue<>();
    
    /**
     * Queue of strings with tasks which should be processed by the {@link RFIDController}.
     */
    public static ConcurrentLinkedQueue<String> forRFID = new ConcurrentLinkedQueue<>();
    
    /**
     * Queue of strings with tasks which should be processed by the {@link LocalServerChecker}.
     */
    public static ConcurrentLinkedQueue<String> forLocalServer = new ConcurrentLinkedQueue<>();
    
    /**
     * Queue of strings with photo paths which should be processed by the {@link ServerChecker}.
     */
    public static ConcurrentLinkedQueue<String> photoPaths = new ConcurrentLinkedQueue<>();
    
    /**
     * The instance of the {@link MotionController} class to control this Raspberry Pi GPIO pins.
     */
    public static MotionController motionController;
    
    private static final Logger LOGGER = Logger.getLogger("RPi");
    
    /**
     * Reads the program configuration file and sets variables {@link #serverAddress},  
     * {@link #localServerAddress}, {@link #id}, {@link #photoDir}.
     */
    private static void readConfigurations(){
        try(BufferedReader br = new BufferedReader(new FileReader(CONFIG_MAINFILE))){
            String conf;
            while((conf = br.readLine()) != null){
		if(!conf.trim().isEmpty()){
                    if (conf.startsWith(KEYWORD_SERVER)){        
                        serverAddress = conf.split(KEYWORD_DELIM)[1];
                        
                        LOGGER.log(Level.CONFIG, "--Config: server address == {0}", serverAddress);
                    }
                    else if(conf.startsWith(KEYWORD_MYID)){
                        id = conf.split(KEYWORD_DELIM)[1];
                        
                        LOGGER.log(Level.CONFIG, "--Config: id == {0}", id);
                    }
                    else if(conf.startsWith(KEYWORD_PHOTO)){
                        photoDir = conf.split(KEYWORD_DELIM)[1];
                        
                        LOGGER.log(Level.CONFIG, "--Config: photo path == {0}", photoDir);
                    }
                    else if(conf.startsWith(KEYWORD_LOCSERVER)){
                        localServerAddress = conf.split(KEYWORD_DELIM)[1];
                        
                        LOGGER.log(Level.CONFIG, "--Config: local server == {0}", localServerAddress);
                    }
		}
            }	
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Ensures that the configuration file for tokens exists.
     */
    private static void createTokenConfig(){
        try {
            File keysConfig = new File(CONFIG_KEYSFILE);
            if(!keysConfig.exists() ||  keysConfig.isDirectory()){
                keysConfig.createNewFile();  
            }
        } 
        catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * The entry point of the program.
     * Initializes and starts working threads.
     * @param args command line arguments
     */
    public static void main(String[] args) {     
        readConfigurations();
        createTokenConfig();

        RFIDController rfidThread = new RFIDController();  
        motionController= new MotionController();
        InsideTasksManager manager = new InsideTasksManager();
        ServerChecker requestThread = new ServerChecker();
        LocalServerChecker localChecker = new LocalServerChecker();
       
        LOGGER.log(Level.INFO, "--Starting thread for rfid");
        rfidThread.start();
        
        LOGGER.log(Level.INFO, "--Starting thread for inside tasks manager");
        manager.start();
            
        LOGGER.log(Level.INFO, "--Starting thread for server");
        requestThread.start();
       
        LOGGER.log(Level.INFO, "--Starting thread for local server");
        localChecker.start();     
    }    
}