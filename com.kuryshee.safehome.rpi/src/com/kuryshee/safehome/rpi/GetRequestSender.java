/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kuryshee.safehome.rpi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GetRequestSender {

    private HttpURLConnection connection;
    private String charset;
    private OutputStream outputStream;
    private PrintWriter writer;
    
    public GetRequestSender(String query, String charset) throws IOException{
        this.charset = charset;
        URL url = new URL(query);
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.setRequestProperty("Accept-Charset", charset);                                      
    }
    
    public String connect() throws IOException{
        connection.connect();
        try(BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), charset));){     
            for (int i = 0; i < 10; i++){  
                if(reader != null){
                    String answer = reader.readLine().trim();
                    return answer;
                }
                else{            
                    try{ Thread.sleep(100); }
                    catch(InterruptedException e){ //log 
                        return ComKurysheeSafehomeRpi.ERROR_ANSWER;
                    }               
                }
            }
            return ComKurysheeSafehomeRpi.NO_ANSWER;
                       
        }
        catch(IOException e){
            return ComKurysheeSafehomeRpi.ERROR_ANSWER;
        }
    }   
    
    public void finish(){
        if (connection != null){
            connection.disconnect();
        }
    }
}
