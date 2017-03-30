/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kuryshee.safehome.rpi;

import static com.kuryshee.safehome.rpi.ComKurysheeSafehomeRpi.log;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Katrikken
 */
public class LocalServerChecker extends Thread{ 
    private final String REQ_CHECKTASK = "/checktask";  
    private long millis = 1000;
    
    /**
    * This method uses utility class GetRequestSender to send GET 
    * @param request to the server.
    * It stores the answer in the queue for inside tasks.
    */
    private void sendGETRequest(String request){      
        GetRequestSender sender = null;
        try{
            sender = new GetRequestSender(ComKurysheeSafehomeRpi.localServerAddress  + request, 
                    ComKurysheeSafehomeRpi.STD_CHARSET);
            log.log(Level.INFO, "--Local server thread -- send request {0}", ComKurysheeSafehomeRpi.localServerAddress + request); 
            
            String answer = sender.connect();
            if(!answer.equals(ComKurysheeSafehomeRpi.NO_ANSWER) && !answer.equals(ComKurysheeSafehomeRpi.ERROR_ANSWER)){
                ComKurysheeSafehomeRpi.insideTasks.add(answer);
                        
                log.log(Level.INFO, "--Local server thread -- got inside task: {0}", answer);  
            }
        }
        catch(IOException e){ 
            log.log(Level.SEVERE, "--Local server thread-- sending GET request failed"); 
        }
        finally{
            if (sender != null){
                sender.finish();
            }
        }
    }
    
    private void sendCheckTask(){
        String query;
        try {
            query = String.format("%s?%s=%s", 
                    REQ_CHECKTASK,
                    ComKurysheeSafehomeRpi.ATT_RPI,
                    URLEncoder.encode(ComKurysheeSafehomeRpi.id, ComKurysheeSafehomeRpi.STD_CHARSET));
            
            sendGETRequest(query);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(LocalServerChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
                             
    }
    
    /**
     * This method sends card ID read by RFID thread to local server as a POST request.
     */
    private void sendCardID(){
        FileUploader uploader;
        try{
            uploader = new FileUploader(ComKurysheeSafehomeRpi.localServerAddress + ComKurysheeSafehomeRpi.COMMAND_READCARD, 
                    ComKurysheeSafehomeRpi.STD_CHARSET);
            
            uploader.addFormField("rpi", ComKurysheeSafehomeRpi.id);            
            uploader.addFormField("card", ComKurysheeSafehomeRpi.forLocalServer.peek());
            String response = uploader.finish();
            if(response.equals(ComKurysheeSafehomeRpi.OK_ANSWER)){
                ComKurysheeSafehomeRpi.forLocalServer.poll();
            }         
        }
        catch(IOException e){
            log.log(Level.INFO, "-- Local server thread -- sending POST request failed"); 
        }           
    }
    
    @Override
    public void run(){
        while(true){ 
            Logger.getLogger(LocalServerChecker.class.getName()).log(Level.INFO, "--Local server thread -- check for tasks");
            if(ComKurysheeSafehomeRpi.forLocalServer.isEmpty()){  
                sendCheckTask();            
            }
            else{
                Logger.getLogger(LocalServerChecker.class.getName()).log(Level.INFO, "--Local server thread -- got card ID to send ");
                if (ComKurysheeSafehomeRpi.forLocalServer.peek().equals(ComKurysheeSafehomeRpi.COMMAND_READCARD)){
                    sendCardID();
                }
            }
            try{ Thread.sleep(millis); }
            catch(InterruptedException e){ Logger.getLogger(LocalServerChecker.class.getName()).log(Level.SEVERE, null, e); }
        }
    }
}
