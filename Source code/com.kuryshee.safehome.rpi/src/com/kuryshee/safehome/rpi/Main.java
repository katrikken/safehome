package com.kuryshee.safehome.rpi;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

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
     * The default encoding for the communication over HTTP.
     */
    public static final String DEFAULT_ENCODING = "UTF-8";
     
    /**
     * The path to the configuration file with information about registered users.
     */
    public static String CONFIG_KEYSFILE;
    
    /**
     * The path to the configuration file with definitions of variables 
     * {@link #id}, {@link #localServerAddress}, {@link #photoDir}, {@link #serverAddress}.
     */
    private static String CONFIG_MAINFILE;
    
    //Theese are key words for reading configuration files.
    private static final String KEYWORD_SERVER = "server";
    private static final String KEYWORD_PHOTO = "photo";
    private static final String KEYWORD_MYID = "myID";   
    private static final String KEYWORD_LOCSERVER ="locserver";
    
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
    
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    
    /**
     * Reads the program configuration file and sets variables {@link #serverAddress},  
     * {@link #localServerAddress}, {@link #id}, {@link #photoDir}.
     */
    private static void readConfigurations(){
        try(InputStream is = new FileInputStream(new File(CONFIG_MAINFILE)); 
                JsonReader reader = Json.createReader(is)){
            
            JsonObject config = reader.readObject();
            serverAddress = config.getString(KEYWORD_SERVER);
            LOGGER.log(Level.CONFIG, "--Config: server address == {0}", serverAddress);
            
            localServerAddress =  config.getString(KEYWORD_LOCSERVER);
            LOGGER.log(Level.CONFIG, "--Config: local server == {0}", localServerAddress);
            
            id = config.getString(KEYWORD_MYID);
            LOGGER.log(Level.CONFIG, "--Config: id == {0}", id);
            
            photoDir =  config.getString(KEYWORD_PHOTO);
            File photoDirectory = new File(photoDir);
            photoDirectory.mkdirs();
            
            LOGGER.log(Level.CONFIG, "--Config: photo path == {0}", photoDir);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    /**
     * Ensures that the configuration file for tokens exists.
     */
    private static void createTokenConfig(){
        try {
            File keysConfig = new File(CONFIG_KEYSFILE);

            keysConfig.createNewFile(); 
        } 
        catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * The entry point of the program.
     * Initializes and starts working threads.
     * @param args command line arguments. Expects paths to the configuration file and user managing file.
     */
    public static void main(String[] args) {     
        if(args.length == 2){
            CONFIG_MAINFILE = args[0];
            CONFIG_KEYSFILE = args[1];
        
            readConfigurations();
            createTokenConfig();

            RFIDController rfidThread = new RFIDController();  
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
            
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() { 
                    LOGGER.log(Level.INFO, "SHUTTING DOWN");
                    rfidThread.interrupt();
                    manager.interrupt();
                    requestThread.interrupt();
                    localChecker.interrupt();
                }
            });
        }
        else{
            LOGGER.log(Level.INFO, "Missing arguments for \"configuration\" and \"keys\"");
        }
    }    
}