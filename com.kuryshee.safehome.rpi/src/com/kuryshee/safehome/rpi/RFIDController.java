/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kuryshee.safehome.rpi;

import com.liangyuen.util.Convert;
import com.liangyuen.util.RaspRC522;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * This class interacts with the RFID reader in a thread. 
 * It creates an instance of the GPIO controller.
 * Sends the 
 * REQ_RFIDSWITCH (/rfid=user) 
 * String for the MotionController thread.
 */
public class RFIDController extends Thread{
    
    private final String CONFIG_KEYSFILE;
    private final String ZEROTAG = "0000000000";
    private final String KEYWORD_KEY = "key";
    
    private static HashMap<String, String> rfidKeys = new HashMap<>(); 
    
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
                if ((char) c != '\n' || (char) c != '\r'){
                    strb.append((char) c);
                }
                else{
                    if (strb.toString().startsWith(KEYWORD_KEY)){
                        String[] keys = strb.toString().substring(KEYWORD_KEY.length() + 1).split(" ");
                        if (keys.length == 2 && !rfidKeys.containsKey(keys[0])){   
                            rfidKeys.put(keys[0], keys[1]);
                        }
                    }                       
                    strb = new StringBuilder();
                }
            }
        }
        catch(IOException e){
            System.out.println("--Could not find configuration file");
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
            
        System.out.println("--RFID thread read a tag: " + strUID);
            
        return strUID;
    }
    /**
     * This method repeatedly sends the RFID reader request to read the card tag and passes the read value to the main thread.
     */
    @Override
    public void run(){
        readKnownTags();
            
        while (true){
            String tag = readCard();
            if (!tag.equals(ZEROTAG) && rfidKeys.containsKey(tag)){

                ComKurysheeSafehomeRpi.insideTasks.add(ComKurysheeSafehomeRpi.REQ_RFIDSWITCH + '=' + rfidKeys.get(tag));
                    
                System.out.println("--RFID thread sent tag to the inside thread");
                    
                /* Sleep for 10 seconds after reading the known key in order not to react to the right key two times at one usage. */
                try { Thread.sleep(millis * 10); } 
                catch(InterruptedException e){
                    System.out.println("--RFID thread -- Interrupted");
                }
            }
            else{
                try { Thread.sleep(millis); }
                catch(InterruptedException e){
                    System.out.println("--RFID thread -- Interrupted");
                }
            }
        }
    }
}
