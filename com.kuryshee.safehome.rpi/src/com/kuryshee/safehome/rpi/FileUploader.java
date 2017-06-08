package com.kuryshee.safehome.rpi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides methods for sending multipart HTTP POST requests to a web server according to RFC1341 standard.
 */
public class FileUploader {
    private final String boundary;
    private static final String LINE_END = "\r\n";
    private HttpURLConnection connection;
    private String charset;
    private OutputStream outputStream;
    private PrintWriter writer;
 
    /**
     * This constructor initializes a new multipart HTTP POST request.
     * @param request
     * @param charset
     * @throws IOException
     */
    public FileUploader(String request, String charset) throws IOException {
        this.charset = charset;      
        boundary = System.currentTimeMillis() + "";
         
        URL url = new URL(request);
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true); // indicates POST method
        connection.setDoInput(true);
        connection.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + boundary);
        connection.setRequestProperty("Accept-Charset", charset);
        outputStream = connection.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
    }
 
    /**
     * Adds an upload file to the request
     * @param inputName name attribute in HTML form input
     * @param uploadFile a File to be uploaded
     * @throws IOException
     */
    public void addFilePart(String inputName, File uploadFile)
            throws IOException {
        String fileName = uploadFile.getName();
        
        //boundary encapsulation according to RFC1341 here and further in code.
        writer.append("--" + boundary).append(LINE_END);
        
        writer.append("Content-Disposition: form-data; name=\"" + inputName
                        + "\"; filename=\"" + fileName + "\"")
                .append(LINE_END);
        writer.append("Content-Type: "+ URLConnection.guessContentTypeFromName(fileName))
                .append(LINE_END);
        writer.append("Content-Transfer-Encoding: binary").append(LINE_END);
        writer.append(LINE_END);
        writer.flush();
 
        try (FileInputStream inputStream = new FileInputStream(uploadFile)) {
            byte[] buffer = new byte[4096];
            int bytes;
            while ((bytes = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytes);
            }
            outputStream.flush();
        }
         
        writer.append(LINE_END);
        writer.flush();    
    }
    
     /**
     * Adds a form input field to the request
     * @param name field name
     * @param value field value
     */
    public void addFormField(String name, String value) {
        writer.append("--" + boundary).append(LINE_END);
        writer.append("Content-Disposition: form-data; name=\"" + name + "\"")
                .append(LINE_END);
        writer.append("Content-Type: text/plain; charset=" + charset).append(
                LINE_END);
        writer.append(LINE_END);
        writer.append(value).append(LINE_END);
        writer.flush();
    }
     
    /**
     * Completes the request and reads response from the server.
     * @return server response as a String if status OK, otherwise an exception is thrown.
     * @throws IOException
     */
    public String finish() throws IOException {
        writer.append(LINE_END).flush();
        writer.append("--" + boundary + "--").append(LINE_END);
        writer.close();
        
        try(BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), charset));){     
            for (int i = 0; i < 10; i++){  
                if(reader != null){
                    String answer = reader.readLine().trim();
                    return answer;
                }
                else{            
                    try{ Thread.sleep(200); }
                    catch(InterruptedException e){ 
                        Logger.getLogger("File Uploader").log(Level.SEVERE, "Failed to get response to the POST request", e);
                        return Main.ERROR_ANSWER;
                    }               
                }
            }
            return Main.NO_ANSWER;
                       
        }
        catch(Exception e){
            Logger.getLogger("File Uploader").log(Level.SEVERE, "Failed to send POST request", e);
            return Main.ERROR_ANSWER;
        }
    }
}
