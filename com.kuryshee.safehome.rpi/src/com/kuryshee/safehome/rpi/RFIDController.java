package com.kuryshee.safehome.rpi;

import com.liangyuen.util.Convert;
import com.liangyuen.util.RaspRC522;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class interacts with the RFID reader in a thread. 
 * It creates an instance of the GPIO controller.
 * @author Ekaterina Kurysheva
 */
public class RFIDController extends Thread{
    
    /**
     * The constant specifies the part to add to the server path to report switching the state of a program by a token.
     */
    public static final String REQ_RFIDSWITCH = "/rfid";
    
    /**
     * The map key is a RFID tag, the value is a name of the user associated with the tag.
     */
    public static Map<String, String> rfidKeys = new HashMap<>(); 
    
    /**
     * The constant which defines that no tag has been read by the reader.
     */
    private final String ZEROTAG = "0000000000";
    
    /**
     * The constant contains key word for the configuration file {@link Main#CONFIG_KEYSFILE}.
     */
    private final String KEYWORD_KEY = "key ";
    
    /**
     * RFID RC522 controller.
     */
    private RaspRC522 rc522;
    
    private long TEN_SEC = 10000;
    private long TWO_SEC = 2000;
    private static final Logger LOGGER = Logger.getLogger("RFID Controller");
    
    /**
     * This constructor initializes a new instance of RFID controller, which runs in a separate thread.
     */
    public RFIDController(){
        readKnownTags();
        rc522 = new RaspRC522();
    }

    /**
     * This method reads the known card tags from configuration file to the data structure {@link #rfidKeys}. 
     */
    private void readKnownTags(){
        try(BufferedReader br = new BufferedReader(new FileReader(Main.CONFIG_KEYSFILE))){
            String conf;
            while( (conf = br.readLine()) != null){
		if(!conf.trim().isEmpty()){
                    String params[] = conf.substring(KEYWORD_KEY.length()).split("-");
                    if (params.length == 2 && !rfidKeys.containsKey(params[0])){   
                            rfidKeys.put(params[0], params[1]);
                    }
		}
            }			
	} catch (Exception e) {
            LOGGER.log(Level.SEVERE, "--Could not find key configuration file");
	}
    }
    
    /**
     * This method sends the command to the RFID to read card tag.
     * @return String of ten hexadecimal numbers. Contains ten zeros in case no tag has been read.
     */
    private String readCard(){
        byte[] tagid = new byte[5]; //Card ID has 5 bytes.

        rc522.Select_MirareOne(tagid);
        String strUID = Convert.bytesToHex(tagid);
            
        LOGGER.log(Level.INFO, "--RFID thread read a tag: {0}", strUID);
            
        return strUID;
    }
    
    /**
     * This method reports the switch of states of a program to the server.
     * @param token is a tag which fired the switching.
     */
    private void reportSwitchByToken(String token){
        Main.forServer.add(REQ_RFIDSWITCH + rfidKeys.get(token));
    }
    
    /**
     * This method waits for the new token to be read during one minute.
     * If the token has not been read or is equal to some of already registered tokens, the method reports error to the local server.
     */
    private void waitForNewToken(){
        String token = ZEROTAG;
        
        long t = System.currentTimeMillis();
        long end = t + 60000;
        while(System.currentTimeMillis() < end && token.equals(ZEROTAG)) {
            token = readCard();
        }
        
        if(!token.equals(ZEROTAG)){
            if(rfidKeys.containsKey(token)){
                Main.forLocalServer.add(LocalServerChecker.COMMAND_READTOKEN + Main.ERROR_ANSWER);
            }
            else{
                Main.forLocalServer.add(LocalServerChecker.COMMAND_READTOKEN + token);
            }
        }
        else{
            Main.forLocalServer.add(LocalServerChecker.COMMAND_READTOKEN + Main.ERROR_ANSWER);
        }
    }
    
    /**
     * This method processes tasks coming to the {@link Main#forRFID} from other parts of the application.
     */
    private void processTask(String task){     
        if(task.equals(LocalServerChecker.COMMAND_READTOKEN)){
            waitForNewToken();
        }
        else if(task.equals(LocalServerChecker.COMMAND_UPDATEUSERS)){
            rfidKeys.clear();
            readKnownTags();
        }
    }
    

    /**
     * This method repeatedly reads tags from RFID.
     */
    @Override
    public void run(){
        while (true){
            if(Main.forRFID.isEmpty()){
                String tag;
                if (!(tag = readCard()).equals(ZEROTAG) && rfidKeys.containsKey(tag)){      
                    Main.insideTasks.add(REQ_RFIDSWITCH);
                    reportSwitchByToken(tag);
                    
                    LOGGER.log(Level.INFO, "--RFID sends request to switch");
                    // Sleep for 10 seconds after reading the known key in order not to react to the right key two times at one usage. 
                    try { Thread.sleep(TEN_SEC); } 
                    catch(InterruptedException e){
                        LOGGER.log(Level.SEVERE, "--RFID thread -- Interrupted");
                    }
                }
            }
            else{
                processTask(Main.forRFID.poll());
            }           
            
            try { Thread.sleep(TWO_SEC); } 
            catch(InterruptedException e){
                LOGGER.log(Level.SEVERE, "--RFID thread -- Interrupted");
            }
        }
    }
}
