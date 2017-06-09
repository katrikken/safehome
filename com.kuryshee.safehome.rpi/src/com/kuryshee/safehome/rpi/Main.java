package com.kuryshee.safehome.rpi;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Ekaterina Kurysheva
 */
public class Main {
    
    public static String id;
    public static String serverAddress;
    public static String photoDir;
    public static String localServerAddress;

    public static String formatOfImage = ".jpg";

    //Standard charset for communication with server.
    public static final String DEFAULT_ENCODING = "UTF-8";
    
    public static final String NO_ANSWER = "no answer";
    public static final String OK_ANSWER = "ok";
    public static final String ERROR_ANSWER = "error";
     
    /* Configuration file for RFID keys is created in the main method. 
    * This is the explicit path for the file. */
    public static final String CONFIG_KEYSFILE = "/home/pi/NetBeansProjects/com.kuryshee.safehome.rpi/dist/keys.txt";
    
    //The path to the configuration file.
    private static final String CONFIG_MAINFILE = "/home/pi/NetBeansProjects/com.kuryshee.safehome.rpi/dist/config.txt";;
    
    //Theese key words are for reading configuration files.
    private static final String KEYWORD_SERVER = "server";
    private static final String KEYWORD_PHOTO = "photo";
    private static final String KEYWORD_MYID = "myID";   
    private static final String KEYWORD_LOCSERVER ="locserver";
    private static final String KEYWORD_DELIM = "=";
    
    //These queues contain tasks for threads.
    public static ConcurrentLinkedQueue<String> forServer = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<String> insideTasks = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<String> forRFID = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<String> forLocalServer = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<String> photoPaths = new ConcurrentLinkedQueue<>();
    
    public static MotionController motionController;
    
    private static final Logger LOGGER = Logger.getLogger("RPi");
    
    /**
     * This method reads the program configuration file and sets variables {@link #serverAddress},  
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
     * This method ensures that the configuration file for tokens exists.
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
     * This method is an entry point of a program.
     * It initializes and starts working threads.
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