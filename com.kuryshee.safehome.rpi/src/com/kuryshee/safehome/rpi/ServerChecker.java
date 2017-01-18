/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kuryshee.safehome.rpi;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * This class interacts with the main server.
 */
public class ServerChecker extends Thread{ //ComKurysheeSafehomeRpi.
    private final String id;
    private final String serverAddress;
    private final String photoDir;
    
    private final String REQ_CHECKTASK = "/checktask";  
    private long millis = 100;
    
    /**
     * This constructor creates a thread.
     * @param id
     * @param serverAddress
     * @param photoDir 
     */
    public ServerChecker(String id, String serverAddress, String photoDir){
       this.id = id;
       this.serverAddress = serverAddress;
       this.photoDir = photoDir;
    }
    
    /**
    * This method uses utility class GetRequestSender to send GET 
    * @param request to the server.
    */
    private void sendGETRequest(String request){      
        GetRequestSender sender = null;
        try{
            sender = new GetRequestSender(serverAddress + request, ComKurysheeSafehomeRpi.STD_CHARSET);
            String answer = sender.connect();
            if(!answer.equals(ComKurysheeSafehomeRpi.NO_ANSWER) && !answer.equals(ComKurysheeSafehomeRpi.ERROR_ANSWER)){
                ComKurysheeSafehomeRpi.insideTasks.add(answer);
                        
                System.out.println("--Server thread -- got inside task: " + answer);  
            }
        }
        catch(IOException e){ 
            System.out.println("--Server thread -- sending GET request failed"); 
        }
        finally{
            if (sender != null){
                sender.finish();
            }
        }
    }
    
    /**
     * This method uses utility class FileUploader to send POST request to the server.
     */
    private void sendPOSTRequest(){
        FileUploader uploader;
        try{
            uploader = new FileUploader(serverAddress + ComKurysheeSafehomeRpi.UPLOAD_PHOTO, ComKurysheeSafehomeRpi.STD_CHARSET);
            uploader.addFormField("time", 
                    ComKurysheeSafehomeRpi.photoPaths.peek().substring(
                            photoDir.length(), ComKurysheeSafehomeRpi.photoPaths.peek().length() - ComKurysheeSafehomeRpi.formatOfImage.length())
            );
            uploader.addFormField("rpi", id);
            
            uploader.addFilePart("photo", new File(ComKurysheeSafehomeRpi.photoPaths.peek()));
            String response = uploader.finish();
            if(response.equals(ComKurysheeSafehomeRpi.OK_ANSWER)){
                ComKurysheeSafehomeRpi.photoPaths.poll();
            }         
        }
        catch(IOException e){
            System.out.println("--Server thread -- sending POST request failed"); 
        }           
    }
    
    /**
     * This method repeatedly checks for the requests to send to the main server, 
     * sends GET and POST HTTP requests and processes the responses.
     */
    @Override
    public void run(){
        while(true){ 
            System.out.println("--Server thread -- check for tasks");
            if(ComKurysheeSafehomeRpi.forServer.isEmpty()){  
                try{
                    String query = String.format("%s?%s=%s", 
                        REQ_CHECKTASK, 
                        ComKurysheeSafehomeRpi.ATT_RPI,
                        URLEncoder.encode(id, ComKurysheeSafehomeRpi.STD_CHARSET));
                        
                    sendGETRequest(query);
                }
                catch(UnsupportedEncodingException e){ 
                    System.out.println("--Server thread -- sending checktask failed ");
                }   
            }
            else{
                System.out.println("--Server thread -- got task from inside ");
                    
                //Parsing query for server to distinguish between GET and POST requests.
                int index = ComKurysheeSafehomeRpi.forServer.peek().indexOf("?");
                String command = ComKurysheeSafehomeRpi.forServer.peek().substring(0, index);
                
                //Answers for server tasks
                switch (command) {
                    case ComKurysheeSafehomeRpi.COMMAND_GETSTATE:
                    case ComKurysheeSafehomeRpi.COMMAND_SWITCHOFF:
                    case ComKurysheeSafehomeRpi.COMMAND_SWITCHON:
                    case ComKurysheeSafehomeRpi.COMMAND_TAKEPHOTO:
                        sendGETRequest(ComKurysheeSafehomeRpi.forServer.poll());
                        break;
                    case ComKurysheeSafehomeRpi.REQ_RFIDSWITCH:
                    case ComKurysheeSafehomeRpi.REQ_MOTIONDETECTED:
                    case ComKurysheeSafehomeRpi.REQ_PHOTOTAKEN:
                        sendGETRequest(ComKurysheeSafehomeRpi.forServer.poll());
                        break;
                    case ComKurysheeSafehomeRpi.UPLOAD_PHOTO:
                        //Try to send all photoes which are made from the last synchronization.
                        for(int i = 0; i< ComKurysheeSafehomeRpi.photoPaths.size(); i++ ){
                            sendPOSTRequest();
                        }   
                        break;
                    default:
                        break;
                }
            }
            try{ Thread.sleep(millis); }
            catch(InterruptedException e){ System.out.println("--Server thread -- Interrupted"); }
        }
    }
}
