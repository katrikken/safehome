package com.kuryshee.safehome.httprequestsender;

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
 * This class provides methods for sending multi-part HTTP POST requests to a web server according to RFC1341 standard.
 * @author Ekaterina Kurysheva
 */
public class FormUploader {
    /**
     * The constant for the boundary in the POST request.
     */
    private final String boundary;
    
    private static final String LINE_END = "\r\n";
    private HttpURLConnection connection = null;
    private String charset;
    private OutputStream outputStream;
    private PrintWriter writer = null;
    private int TWENTY_SEC = 20000;
 
    /**
     * This constructor initializes a new multi-part HTTP POST request.
     * @param request is a full URL link.
     * @param charset is a definition of encoding for this request.
     * @throws IOException
     */
    public FormUploader(String request, String charset) throws IOException {
    	if(charset == null) {
    		charset = "UTF-8";
    	}
    	
    	this.charset = "UTF-8";
             
        boundary = System.currentTimeMillis() + "";
         
        URL url = new URL(request);
        connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(TWENTY_SEC); 
        connection.setDoOutput(true); // indicates POST method
        connection.setDoInput(true);
        connection.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + boundary);
        connection.setRequestProperty("Accept-Charset", charset);  
    }
 
    /**
     * Adds header to the request.
     * The method must be called before {@link FormUploader#connect()}.
     * @param key of the header
     * @param value for the key.
     */
    public void addHeader(String key, String value) {
    	connection.setRequestProperty(key, value);
    }
    
    /**
     * Finishes settings of the connection and gets output stream for writing data.
     * The method must be called before {@link FormUploader#addFormField(String, String)} or {@link FormUploader#addFilePart(String, File)} methods.
     * @throws IOException 
     */
    public void connect() throws IOException {
    	outputStream = connection.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
    }
    
    /**
     * Adds a file to the request
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
     * Completes the request and reads the response from the server.
     * @return server response as a String if the status is OK, otherwise an exception is thrown.
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
                        return AnswerConstants.ERROR_ANSWER;
                    }               
                }
            }
            return AnswerConstants.NO_ANSWER;
                       
        }
        catch(Exception e){
            Logger.getLogger("File Uploader").log(Level.SEVERE, "Failed to send POST request", e);
            return AnswerConstants.ERROR_ANSWER;
        }
    }
}
