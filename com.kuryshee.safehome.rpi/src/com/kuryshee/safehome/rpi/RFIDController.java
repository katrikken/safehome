/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kuryshee.safehome.rpi;

import static com.kuryshee.safehome.rpi.ComKurysheeSafehomeRpi.log;
import static com.kuryshee.safehome.rpi.ComKurysheeSafehomeRpi.rfidKeys;
import com.liangyuen.util.Convert;
import com.liangyuen.util.RaspRC522;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;

/**
 * This class interacts with the RFID reader in a thread. 
 * It creates an instance of the GPIO controller.
 * Sends the 
 * REQ_RFIDSWITCH as (/rfid=user) string for the MotionController thread.
 * Reacts to
 * COMMAND_READCARD from the MotionController thread as (cadID) string for the local server.
 */
public class RFIDController extends Thread{
    
    private final String CONFIG_KEYSFILE;
    private final String ZEROTAG = "0000000000";
    private final String KEYWORD_KEY = "key";
    
    private RaspRC522 rc522;
    
    private long millis = 1000;
    
    /**
     * This constructor initializes a new instance of RFID controller, which runs in a separate thread.
     * @param configFilePath is the path to configuration file where all known RFID keys are stored.
     */
    public RFIDController(String configFilePath){
        CONFIG_KEYSFILE = configFilePath;
        readKnownTags();
        rc522 = new RaspRC522();
    }

    /**
     * This method reads the known card tags from configuration file to the data structure. 
     */
    private void readKnownTags(){
        try{
            BufferedReader bfr = new BufferedReader(new FileReader(CONFIG_KEYSFILE)); 
            int c;
            StringBuilder strb = new StringBuilder();
            while((c = bfr.read()) != -1){
                if ((char) c != '\r' && (char) c != '\n'){
                    strb.append((char) c);
                }
                else{
                    if (strb.toString().startsWith(KEYWORD_KEY)){
                        String[] keys = strb.toString().substring(KEYWORD_KEY.length() + 1).split("-");
                        if (keys.length == 2 && !rfidKeys.containsKey(keys[0])){   
                            rfidKeys.put(keys[0], keys[1]);
                        }
                    }                       
                    strb = new StringBuilder();
                }
            }
        }
        catch(IOException e){
            log.log(Level.SEVERE, "--Could not find key configuration file");
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
            
        log.log(Level.INFO, "--RFID thread read a tag: {0}", strUID);
            
        return strUID;
    }
    /**
     * This method repeatedly reads tags from RFID.
     */
    @Override
    public void run(){
        readKnownTags();
        while (true){
            String tag;
            if (!(tag = readCard()).equals(ZEROTAG)){
                if (ComKurysheeSafehomeRpi.forRFID.isEmpty()){
                    if(rfidKeys.containsKey(tag)){
                        log.log(Level.INFO, "--RFID thread sends tag to the inside thread");
                        ComKurysheeSafehomeRpi.insideTasks.add(ComKurysheeSafehomeRpi.REQ_RFIDSWITCH + '=' + rfidKeys.get(tag));
                    
                        // Sleep for 10 seconds after reading the known key in order not to react to the right key two times at one usage. 
                        try { Thread.sleep(millis * 10); } 
                        catch(InterruptedException e){
                            log.log(Level.SEVERE, "--RFID thread -- Interrupted");
                        }
                    }
                    else{
                        log.log(Level.INFO, "--RFID read unknown tag");
                    }
                }
                else
                {
                    if(ComKurysheeSafehomeRpi.forRFID.peek().equals(ComKurysheeSafehomeRpi.COMMAND_READCARD)){
                        ComKurysheeSafehomeRpi.forRFID.poll();
                        ComKurysheeSafehomeRpi.forLocalServer.add(tag);
                        log.log(Level.INFO, "--RFID sends tag to the local server");

                        // Sleep for 10 seconds after sending the key in order not to send multiple requests on the same issue.
                        try { Thread.sleep(millis * 10); } 
                        catch(InterruptedException e){
                            log.log(Level.SEVERE, "--RFID thread -- Interrupted");
                        }
                    }
                    else if(ComKurysheeSafehomeRpi.forRFID.peek().equals(ComKurysheeSafehomeRpi.COMMAND_SAVEUSER)){
                        ComKurysheeSafehomeRpi.rfidKeys.clear();
                        readKnownTags();
                    }
                }
            }         
        }
    }
}
