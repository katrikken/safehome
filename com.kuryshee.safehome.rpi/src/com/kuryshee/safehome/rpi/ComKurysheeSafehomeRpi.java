package com.kuryshee.safehome.rpi;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ComKurysheeSafehomeRpi {
    public static Logger log = Logger.getLogger("RPi");
    
    //These data are read from configuration file in the main() method.
    public static String id;
    public static String serverAddress;
    public static String photoDir;
    public static String localServerAddress;

    public static String formatOfImage = ".jpg";

    //Standard charset for communication with server.
    public static final String STD_CHARSET = "UTF-8";
    
    //The following set of key words serves for templated query building purposes.
    //This part of key words are server commands.
    public static final String COMMAND_GETSTATE = "/getstate";
    public static final String COMMAND_SWITCHOFF = "/switchoff";
    public static final String COMMAND_SWITCHON = "/switchon";
    public static final String COMMAND_TAKEPHOTO = "/takephoto";
    
    //This part of key words are local server commands.
    public static final String COMMAND_READCARD = "/read";
    public static final String COMMAND_SAVEUSER = "/saveuser";
    
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
    private static final String KEYWORD_LOCSERVER ="locserver";
    private static final String KEYWORD_DELIM = "=";
    
    //These queues contain tasks for threads.
    public static ConcurrentLinkedQueue<String> forServer = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<String> insideTasks = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<String> forRFID = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<String> forLocalServer = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<String> photoPaths = new ConcurrentLinkedQueue<>();
    
    //The map of RFID tags and usernames assosiated with them.
    public static ConcurrentHashMap<String, String> rfidKeys = new ConcurrentHashMap<>(); 
    
    public static MotionController motionController;
    
    /**
     * This method encodes given attributes to the query string.
     * @param command is the directory under the main server address.
     * @param attributes is an array of parameter names and parameter values.
     * @param charset is the charset for the encoding.
     */
    public static void addQuery(String command, String[] attributes, String charset){
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
            log.log(Level.SEVERE, "--Main thread -- could not form the query string");          
        }
    }

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
                        serverAddress = strb.toString().split(KEYWORD_DELIM)[1];
                        
                        log.log(Level.CONFIG, "--Config: server address == {0}", serverAddress);
                    }
                    else if(strb.toString().startsWith(KEYWORD_MYID)){
                        id = strb.toString().split(KEYWORD_DELIM)[1];
                        
                        log.log(Level.CONFIG, "--Config: id == {0}", id);
                    }
                    else if(strb.toString().startsWith(KEYWORD_PHOTO)){
                        photoDir = strb.toString().split(KEYWORD_DELIM)[1];
                        
                        log.log(Level.CONFIG, "--Config: photo path == {0}", photoDir);
                    }
                    else if(strb.toString().startsWith(KEYWORD_LOCSERVER)){
                        localServerAddress = strb.toString().split(KEYWORD_DELIM)[1];
                        
                        log.log(Level.CONFIG, "--Config: local server == {0}", localServerAddress);
                    }
                    strb = new StringBuilder();
                }
            }
            
            //Creating a configuration file for RFID keys during the first launch.
            File keysConfig = new File(CONFIG_KEYSFILE);
            if(!keysConfig.exists() ||  keysConfig.isDirectory()){
                keysConfig.createNewFile();
            }
            
            //Debugging 
            //rfidKeys.put("36D9C53B11", "chip");
            //rfidKeys.put("C4703ED55F", "card");
            
            //Starting threads.
            RFIDController rfidThread = new RFIDController(CONFIG_KEYSFILE);  
            motionController= new MotionController();
            ServerChecker requestThread = new ServerChecker();
            LocalServerChecker localChecker = new LocalServerChecker();

            log.log(Level.INFO, "--Starting thread for server");
            requestThread.start();
            
            log.log(Level.INFO, "--Starting thread for rfid");
            rfidThread.start();
            
            log.log(Level.INFO, "--Starting thread for local server");
            localChecker.start();
        }
        catch(IOException e){
            log.log(Level.SEVERE, "--Could not find configuration file. Programm stopped");
        }
    }
    
    
}