package com.kuryshee.safehome.rpi;

import com.kuryshee.safehome.httprequestsender.AnswerConstants;
import com.kuryshee.safehome.rpicommunicationconsts.RpiCommunicationConsts;
import com.liangyuen.util.Convert;
import com.liangyuen.util.RaspRC522;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class interacts with the RFID reader in a thread. 
 * It creates an instance of the GPIO controller.
 * 
 * @author Ekaterina Kurysheva
 */
public class RFIDController extends Thread{
    
    /**
     * The constant for {@link InsideTasksManager} to send answer with RFID tag to local server.
     */
    public static final String REQ_RFIDSWITCH = "/rfid";
    
    /**
     * The map where key is a RFID tag, value is a name of the user associated with the tag.
     */
    public static Map<String, String> rfidKeys = new HashMap<>(); 
    
    /**
     * The constant which defines that no tag has been read by the reader.
     */
    private final String ZEROTAG = "0000000000";
    
    private UserManager manager;
    
    /**
     * RFID RC522 controller.
     */
    private RaspRC522 rc522;
    
    private final long TEN_SEC = 10000;
    private final long TWO_SEC = 2000;
    private final long ONE_MINUTE = 60000;
    private final Logger LOGGER = Logger.getLogger(RFIDController.class.getName());
    
    /**
     * Constructor initializes a new instance of RFID controller.
     */
    public RFIDController(){
        manager = new UserManager(new File(Main.CONFIG_KEYSFILE));
        rfidKeys = manager.getTagNameMap();
        rc522 = new RaspRC522();
    }
    
    /**
     * Sends a single command to the RFID to read card tag.
     * @return String of ten hexadecimal numbers. Contains {@link #ZEROTAG} in case no tag has been read.
     */
    private String readCard(){
        byte[] tagid = new byte[5]; //Card ID has 5 bytes.

        rc522.Select_MirareOne(tagid);
        String strUID = Convert.bytesToHex(tagid);
            
        LOGGER.log(Level.INFO, "--RFID thread read a tag: {0}", strUID);
            
        return strUID;
    }    
    
    /**
     * Waits for the new token to be read for one minute.
     * If the token has not been read or is equal to some of already registered tokens, the method reports error to the local server.
     * Blinks LED once when some token has been read.
     */
    private void waitForNewToken(){
        String token = ZEROTAG;
        
        long t = System.currentTimeMillis();
        long end = t + ONE_MINUTE;
        
        while(System.currentTimeMillis() < end && token.equals(ZEROTAG)) {
            token = readCard();
        }
        
        if(!token.equals(ZEROTAG)){
            if(rfidKeys.containsKey(token)){
                Main.forLocalServer.add(RpiCommunicationConsts.COMMAND_READTOKEN + AnswerConstants.ERROR_ANSWER);
            }
            else{
                Main.forLocalServer.add(RpiCommunicationConsts.COMMAND_READTOKEN + token);
                Main.insideTasks.add(InsideTasksManager.BLINK);
            }
        }
        else{
            Main.forLocalServer.add(RpiCommunicationConsts.COMMAND_READTOKEN + AnswerConstants.ERROR_ANSWER);
        }
    }
    
    /**
     * Processes tasks coming to the {@link Main#forRFID} from other parts of the application.
     */
    private void processTask(String task){     
        if(task.equals(RpiCommunicationConsts.COMMAND_READTOKEN)){
            waitForNewToken();
        }
        else if(task.equals(RpiCommunicationConsts.COMMAND_UPDATEUSERS)){
            rfidKeys = manager.updateUsers();
        }
    }
    

    /**
     * Reads tags from RFID in a continuous loop.
     */
    @Override
    public void run(){
        while (true){
            if(Main.forRFID.isEmpty()){
                String tag;
                if (!(tag = readCard()).equals(ZEROTAG) && rfidKeys.containsKey(tag)){      
                    Main.insideTasks.add(REQ_RFIDSWITCH + rfidKeys.get(tag));
                    
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
