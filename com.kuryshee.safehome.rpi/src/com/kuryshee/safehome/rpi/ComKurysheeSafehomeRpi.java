package com.kuryshee.safehome.rpi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.LinkedList;
import java.util.Queue;
import java.net.URLEncoder;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.HashMap;
import com.liangyuen.util.RaspRC522;
import com.liangyuen.util.Convert;
import java.io.File;
import java.io.UnsupportedEncodingException;
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.GpioInterrupt;
import com.pi4j.wiringpi.GpioInterruptEvent;
import com.pi4j.wiringpi.GpioInterruptListener;
import com.pi4j.wiringpi.GpioUtil;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ComKurysheeSafehomeRpi {
    
    //These data are read from configuration file in the main() method.
    private static String id;
    private static String serverAddress;
    private static String photoPath;

    //Standard charset for communication with server.
    private static final String STD_CHARSET = "UTF-8";
    
    //The following set of key words serves for templated query building purposes.
    private static final String COMMAND_GETSTATE = "/getstate";
    private static final String COMMAND_CHECKTASK = "/checktask";
    private static final String COMMAND_SWITCHOFF = "/switchoff";
    private static final String COMMAND_SWITCHON = "/switchon";
    private static final String COMMAND_RFIDSWITCH = "/rfid";
    private static final String COMMAND_TAKEPHOTO = "/takephoto";
    private static final String COMMAND_MOTIONDETECTED = "/motiondetected";
    
    private static final String ATT_RPI = "rpi";
    private static final String ATT_ANSWER = "answer";
    private static final String ATT_KEY = "key";
    
    private static final String NO_ANSWER = "no answer";
    private static final String OK_ANSWER = "ok";
    private static final String ERROR_ANSWER = "error";
     
    /* Configuration file for RFID keys is created in the main method. 
    * This is the explicit path for the file. */
    private static final String CONFIG_KEYSFILE = "keys.txt";
    
    //The path to the configuration file.
    private static final String CONFIG_MAINFILE = "config.txt";
    
    //Theese key words are for reading configuration files.
    private static final String KEYWORD_SERVER = "server";
    private static final String KEYWORD_PHOTO = "photo";
    private static final String KEYWORD_MYID = "myID";   
    
    //These queues contain tasks for threads.
    private static ConcurrentLinkedQueue<String> forServer = new ConcurrentLinkedQueue<>();
    private static ConcurrentLinkedQueue<String> insideTasks = new ConcurrentLinkedQueue<>();
    
    //The map of RFID tags and usernames assosiated with them.
    private static HashMap<String, String> rfidKeys = new HashMap<>(); 

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
                        serverAddress = strb.toString().split("=")[1];
                        
                        System.out.println("--Config: server address == " + serverAddress);
                    }
                    if(strb.toString().startsWith(KEYWORD_MYID)){
                        id = strb.toString().split("=")[1];
                        
                        System.out.println("--Config: id == " + id);
                    }
                    if(strb.toString().startsWith(KEYWORD_PHOTO)){
                        photoPath = strb.toString().split("=")[1];
                        
                        System.out.println("--Config: photo path == " + photoPath);
                    }
                    strb = new StringBuilder();
                }
            }
            
            //Creating a configuration file for RFID keys for the during the first launch.
            File keysConfig = new File(CONFIG_KEYSFILE);
            if(!keysConfig.exists() ||  keysConfig.isDirectory()){
                keysConfig.createNewFile();
            }
            
             //Debugging 
            rfidKeys.put("36D9C53B11", "chip");
            rfidKeys.put("C4703ED55F", "card");
            
            //Starting threads.
            InsideThread insideThread = new InsideThread();
            RequestThread requestThread = new RequestThread();
            RFIDThread rfidThread = new RFIDThread();   
            
            System.out.println("--Starting thread for inside");
            insideThread.start();

            System.out.println("--Starting thread for server");
            requestThread.setPriority(Thread.MAX_PRIORITY);
            requestThread.start();
            
            System.out.println("--Starting thread for rfid");
            rfidThread.start();
        }
        catch(IOException e){
            System.out.println("--Could not find configuration file. Programm stopped");
        }
    }
    
    //This thread takes care of communication with RFID MFRC522 card reader.
    private static class RFIDThread extends Thread{     
        private static final String KEYWORD_KEY = "key";
        //This is a constant for the value returned by RFID when no card tag is read.
        private static final String ZEROTAG = "0000000000";
        private RaspRC522 rc522 = new RaspRC522();
        private long millis = 1000;
        
        /* RC522 controller is initialized in the main thread method . 
         * Card keys from configuration file are read into heap.
         * Card reading loop is started. */
        @Override
        public void run(){

            readKnownTags();
            
            while (true){
                String tag = readCard();
                if (!tag.equals(ZEROTAG) && rfidKeys.containsKey(tag)){
                    //This String for InsideTasks has attribute for further processing within InsideThread.
                    insideTasks.add(COMMAND_RFIDSWITCH + '=' + rfidKeys.get(tag));
                    
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
        
        /* This method reads saved tags from configuration file and addes them to HashMap rfidKeys.
         * Configuration file has format as:
         * KEYWORD_KEY "string of 8 hexadcimal numbers" "username" */
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
        
        /* This method communicates with RFID. It returns card tag if the card was used and returns zeroes if not. */
        private String readCard(){
            byte[] tagid = new byte[5]; //Card ID has 5 bytes.

            rc522.Select_MirareOne(tagid);
            String strUID = Convert.bytesToHex(tagid);
            
            System.out.println("--RFID thread read a tag: " + strUID);
            
            return strUID;
        }
    }
    
    //This thread handles signals from motion sensor, camera usage and holds status information about program state.
    private static class InsideThread extends Thread{
        private static final int PIROUT = 11; //GPIO 11 pin number for motion sensor output
        
        //Constants for the functions within the thread.
        private static final String STATE_OFF = "off";
        private static final String STATE_ON = "on";
        
        //This parameter represent program state. Value of true means watching mode on, value of false means watching mode off.
        boolean currentState = false;
        
        long millis = 300;
        
        //These parameters are used for taking photos.
        String formatOfImage = ".jpg";
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

        /* The main method of the thread checks for new tasks in the queue and passes them for further processing. */
        @Override
        public void run(){
            
            //The setting of pins should be done only once during the programm.
            setPIROutputPin();
            
            while(true){
                System.out.println("--Inside thread running. State: " + currentState);
                if(!insideTasks.isEmpty()){
                    System.out.println("--Inside thread -- Got task");
                    processTask(insideTasks.poll());
                }
                
                try { Thread.sleep(millis); }
                catch(InterruptedException e){
                    System.out.println("--Inside thread -- Interrupted");
                }
            }
        }
        
        //This method is a gateway for incoming tasks.
        private void processTask(String task){
            switch (task){
                case COMMAND_GETSTATE: getstateTask();
                    break;
                case COMMAND_SWITCHOFF: switchOff();                
                    break;
                case COMMAND_SWITCHON: switchOn();
                    break;
                default: 
                    if (task.startsWith(COMMAND_RFIDSWITCH)){
                        //The RFID switch command has attribute user preceeded with '=' char.
                        rfidSwitch(task.substring(COMMAND_RFIDSWITCH.length() + 1));
                    }
                    break;
            }
        }
        /* This method sets the output GPIO pin for the Motion Sensor so the program can react on changes in the pin state. */
        private void setPIROutputPin(){
            GpioInterrupt.addListener(new GpioInterruptListener() {
                @Override
                public void pinStateChange(GpioInterruptEvent event) {
                    //If the program mode is "on" and the listener got high state of pin, the motion counts.
                    if(currentState && event.getState()){
                        System.out.println(" --> GPIO trigger callback received");
                    
                        takePhoto();
                            
                        //Sending query string for server indicating that motion has been detected.
                        String[] atts = {ATT_RPI, id};
                        addQuery(COMMAND_MOTIONDETECTED, atts, STD_CHARSET);
                            
                        //TODO: add post() query
                    }
                }
            });

            GpioUtil.export(PIROUT, GpioUtil.DIRECTION_IN);
            GpioUtil.setEdgeDetection(PIROUT, GpioUtil.EDGE_BOTH);      
            Gpio.pinMode(PIROUT, Gpio.INPUT);
            Gpio.pullUpDnControl(PIROUT, Gpio.PUD_DOWN);
            GpioInterrupt.enablePinStateChangeCallback(PIROUT);       
            
            System.out.println("--Inside thread -- PIN is set");
        }
        
        /* This method builds query string according to the notation and sends it to the queue processed in the RequestThread. */
        private void addQuery(String command, String[] attributes, String charset){
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
               
                forServer.add(query.toString());
            }
            catch(UnsupportedEncodingException e){
                System.out.println("--Inside thread -- could not form the query string");          
            }
        }
        
        /* This method takes a photo and saves it into directory configured in the configuration file. */
        private void takePhoto(){
            try{
                String command = "fswebcam ";

                String date = dateFormat.format(new Date());
                Process p = Runtime.getRuntime().exec(command + photoPath + date + formatOfImage);
                
                System.out.println("--Inside thread - took photo at: " + photoPath + date + formatOfImage);
            }
            catch(IOException e){
                System.out.println("--Inside thread - could not take a photo");
            }
        }
        
        //This method handles query starting with COMMAND_GETSTATE. It sends information about current status of program.
        private void getstateTask(){
            String state;
            if (currentState){
                state = STATE_ON;
            }
            else{
                state = STATE_OFF;
            }
               
            String[] atts = {ATT_RPI, id, ATT_ANSWER, state};
            addQuery(COMMAND_GETSTATE, atts, STD_CHARSET);
            
            System.out.println("--Inside thread -- returned query");             
        }
        
        //This method handles request of switching on the watching mode of program.
        private void switchOn() {
            
            if(!currentState){

                currentState = true;

                //Sending query string for server that switching on succeded.
                String[] atts = {ATT_RPI, id, ATT_ANSWER, OK_ANSWER};
                addQuery(COMMAND_SWITCHON, atts, STD_CHARSET);
                   
                System.out.println("--Inside thread -- returned query ");
            }
            else{               
                String[] atts = {ATT_RPI, id, ATT_ANSWER, OK_ANSWER};
                addQuery(COMMAND_SWITCHON, atts, STD_CHARSET);
                   
                System.out.println("--Inside thread -- already switched on --returned query " ); 
            }
        }
        
        /* This method changes state of program to the switched off mode. */
        private void switchOff(){
            if(currentState){
                currentState = false;
                
                //Sending query string indicating that switching off succeded.
                String[] atts = {ATT_RPI, id, ATT_ANSWER, OK_ANSWER};
                addQuery(COMMAND_SWITCHOFF, atts, STD_CHARSET);
                   
                System.out.println("--Inside thread -- returned query ");          
            }
            else{
                System.out.println("--Inside thread -- already switched off");
                
                String[] atts = {ATT_RPI, id, ATT_ANSWER, OK_ANSWER};
                addQuery(COMMAND_SWITCHOFF, atts, STD_CHARSET);
            }
        }
    
        /* This method switches state of program by the command from RFID thread and sends query indicating that to the server. */
        private void rfidSwitch(String user){
            if (currentState){
                switchOff();
            }
            else{
                switchOn();
            }
           
            String[] attributes = {ATT_RPI, id, ATT_KEY, user};
            addQuery(COMMAND_RFIDSWITCH, attributes, STD_CHARSET);
            
            System.out.println("--Inside thread -- rfid switch command done.");
        }
    }
    
    //This thread handles communication with server.
    private static class RequestThread extends Thread{
        private static final String METHOD_GET = "GET";
        private static final String METHOD_POST = "POST";
        private static final String PROPERTY_CHARSET = "Accept-Charset";
        private long millis = 100;
        
        /* The main method of the thread check the queue for incoming tasks from other threads
         * and passes them for further processing.*/
        @Override
        public void run(){
            while(true){ 
                System.out.println("--Server thread -- check for tasks");
                if(forServer.isEmpty()){  
                    try{
                        String query = String.format("%s?%s=%s", 
                                COMMAND_CHECKTASK, 
                                ATT_RPI,
                                URLEncoder.encode(id, STD_CHARSET));
                        
                        sendGETRequest(query);
                    }
                    catch(UnsupportedEncodingException e){ 
                        System.out.println("--Server thread -- sending checktask failed ");
                        e.printStackTrace();
                    }   
                }
                else{
                    System.out.println("--Server thread -- got task from inside " + forServer.peek());
                    
                    //Parsing query for server to distinguish between GET and POST requests.
                    int index = forServer.peek().indexOf("?");
                    String command = forServer.peek().substring(0, index);
                    if (command.equals(COMMAND_GETSTATE) | command.equals(COMMAND_SWITCHOFF)
                        | command.equals(COMMAND_SWITCHON) | command.equals(COMMAND_RFIDSWITCH)){
                        sendGETRequest(forServer.poll());                       
                    }
                    //TODO: sending POST requests with the photo
                }
                try{ Thread.sleep(millis); }
                catch(InterruptedException e){ System.out.println("--Server thread -- Interrupted"); }
            }
        }
        
        //This method sends GET requests to the server.
        private void sendGETRequest(String query){
            HttpURLConnection connection = null;
            try{
                connection = setConnection(METHOD_GET, true, false, query);      
                connection.connect();
                
                try{ Thread.sleep(millis / 5); } //Waiting for the response should not take too long. 
                catch(InterruptedException e){ System.out.println("--Server thread -- Interrupted");}
                
                readResponse(connection);
            }
            catch(IOException e){ System.out.println("--Server thread -- sending GET request failed"); }
            finally{
                connection.disconnect();
            }
        }
        
        /* This method sets connection properties such as Time Out parameter, server address and others 
         * and returns set HttpURLConnection instance. */
        private HttpURLConnection setConnection(String method, boolean doInput, boolean doOutput, String query){
            int millis = 20000;
            HttpURLConnection connection = null;
            try{
                URL url = new URL(serverAddress + query);
                connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(millis);
                connection.setConnectTimeout(millis);
                connection.setRequestMethod(method);
                connection.setDoOutput(doOutput);
                connection.setDoInput(doInput);
                if (doInput){
                    connection.setRequestProperty(PROPERTY_CHARSET, STD_CHARSET);
                }                    
            }
            catch(IOException e){
                System.out.println("--Server thread -- connection settings failed");                
            }
            
            return connection;  
        }
        
        /* This method reads the response from server and decides whether any command came from server with it. */
        private void readResponse(HttpURLConnection connection){           
            try(BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), STD_CHARSET));){     
                
                if(reader != null){
                    String answer = reader.readLine().trim();
                    if(!answer.equals(NO_ANSWER)){
                        insideTasks.add(answer);
                        
                        System.out.println("--Server thread -- got inside task: " + answer);  
                    }
                    //If there is no task from server, the program will wait longer to ask for incoming tasks again.
                    else{
                        try{ Thread.sleep(millis); }
                        catch(InterruptedException e){ System.out.println("--Server thread -- Interrupted");}
                    }                    
                }
            }
            catch(IOException e){
                System.out.println("--Server thread -- reading response failed");
            }
        }   
    }
}