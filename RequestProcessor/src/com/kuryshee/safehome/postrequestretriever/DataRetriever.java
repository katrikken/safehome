package com.kuryshee.safehome.postrequestretriever;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

/**
 * This class implements retrieval of data from POST multi-part requests. 
 * @author Ekaterina Kurysheva
 */
public class DataRetriever {
	
	private static final String DEFAULT_ENCODING = "UTF-8";
	
	/**
	 * This method retrieves data of a media file form the POST multi-part request.
	 * @param request is a POST request.
	 * @param name is a name of the file part.
	 * @param path is a directory where the file should be stored in.
	 * @return true if writing data file to the path was successfully finished.
	 */
	public Boolean saveFilePart(HttpServletRequest request, String name, String path){
		try{
			Part filePart = request.getPart(name); 
			InputStream fileContent = filePart.getInputStream();
			File file = new File(path);
			byte[] buffer = new byte[fileContent.available()];	
		    fileContent.read(buffer);
		    try(OutputStream outStream = new FileOutputStream(file)){
		    	outStream.write(buffer);
		    }
		    return true;
		}
		catch(IOException | ServletException e){
			Logger.getLogger("File retrieving").log(Level.SEVERE, "Exception while retrieving data", e);
		} 		
		return false;
	}
	
	/**
	 * This method retrieves text data from the multi-part POST request.
	 * @param request is a multi-part POST request.
	 * @param name is the name of the data value.
	 * @return data as a string. In case no data were found returns empty string.
	 */
	public String getTextPart(HttpServletRequest request, String name){
		try{
			Part textPart = request.getPart(name);
			
			if(textPart != null){
				String encoding = request.getCharacterEncoding();
				if(encoding == null){
					encoding = DEFAULT_ENCODING;
				}
				
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(textPart.getInputStream(), encoding));
				StringBuilder value = new StringBuilder();
				char[] buffer = new char[1024];
			    for (int length = 0; (length = reader.read(buffer)) > 0;) {
			    	value.append(buffer, 0, length);
			    }
			    
			    return value.toString();
			}
		}
		catch(IOException | ServletException e){
			Logger.getLogger("Text retrieving").log(Level.SEVERE, "Exception while retrieving data", e);
		}
		return "";
	}
}
