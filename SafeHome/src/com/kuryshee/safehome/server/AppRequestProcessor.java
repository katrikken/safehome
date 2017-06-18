package com.kuryshee.safehome.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import com.kuryshee.safehome.requestprocessorinterface.RequestProcessor;

public class AppRequestProcessor implements RequestProcessor{
	private int ONE_SEC = 1000;

	public void sendPhoto(HttpServletResponse response, String query, ServletContext ctx){
		Map<String, String> params = parseQuery(query);
		String rpi = params.get(SafeHomeServer.RPI_PARAM);	
		String fileName = params.get(SafeHomeServer.PHOTO_PARAM);
		if(rpi != null && fileName != null){
			File file = new File(fileName);
			
			String mimeType = ctx.getMimeType(file.getAbsolutePath());
			response.setContentType(mimeType != null? mimeType:"application/octet-stream");
			response.setContentLength((int) file.length());
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");			
			
			try(ServletOutputStream os = response.getOutputStream();
					InputStream fis = new FileInputStream(file)){
				byte[] bufferData = new byte[1024];
				int read=0;
				while((read = fis.read(bufferData))!= -1){
					os.write(bufferData, 0, read);
				}
				
				log.info("File sent");	
			} catch (IOException e) {
				log.severe(e.getMessage());	
			}
		}
		else{
			log.warning("Could not find history file for null rpi ID");	
		}
	}
	
	public void sendText(HttpServletResponse response, String command, String query, ServletContext ctx){
		Map<String, String> params = parseQuery(query);
		String rpi = params.get(SafeHomeServer.RPI_PARAM);	
		if(rpi != null){
			String fileName;
			if(command.equals(SafeHomeServer.REQ_HISTORY)){
				fileName = MockDatabaseAccess.DB_PATH + rpi;
			}
			else{
				fileName = MockDatabaseAccess.DB_PATH + rpi + MockDatabaseAccess.LIST;
			}
			
			File file = new File(fileName);
		
			String mimeType = ctx.getMimeType(file.getAbsolutePath());
			response.setContentType(mimeType != null? mimeType:"application/octet-stream");
			response.setContentLength((int) file.length());
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");			
			
			try(ServletOutputStream os = response.getOutputStream();
					InputStream fis = new FileInputStream(file)){
				byte[] bufferData = new byte[1024];
				int read=0;
				while((read = fis.read(bufferData))!= -1){
					os.write(bufferData, 0, read);
				}
			} catch (IOException e) {
				log.severe(e.getMessage());	
			}
		}
		else{
			log.warning("Could not find history file for null rpi ID");	
		}

	}
	
	/**
	 * This method adds the task to the queue for Raspberry Pi and delivers answer.
	 * @param command specifies the task.
	 * @param query contains identification data.
	 * @return answer from the Raspberry Pi or {@link SafeHomeServer#NO_ANSWER} if no answer was provided.
	 */
	private String redirectTask(String command, String query){
		Map<String, String> params = parseQuery(query);
		String rpi = params.get(SafeHomeServer.RPI_PARAM);
		
		if(rpi != null){
			//Redirect the task
			SafeHomeServer.forRpi.putIfAbsent(rpi, new ConcurrentLinkedQueue<String>());
			SafeHomeServer.forRpi.get(rpi).add(command); 
			
			//Wait for the answer
			int tries = 10;
			for(int i = 0; i < tries; i++){
				if(SafeHomeServer.forApp.containsKey(rpi) && !SafeHomeServer.forApp.get(rpi).isEmpty()){
					if(SafeHomeServer.forApp.get(rpi).peek().startsWith(command)){
						return SafeHomeServer.forApp.get(rpi).poll();
					}
				}
				try {
					Thread.sleep(ONE_SEC);
				} catch (InterruptedException e) {
					log.severe(e.getMessage());	
				}
			}
		}
		SafeHomeServer.forRpi.get(rpi).remove(command);
		return SafeHomeServer.NO_ANSWER;
	}
	
	/**
	 * This method is a mock for verifying user credentials.
	 * @param query contains the credentials.
	 * @return associated Raspberry Pi ID in case the data are valid.
	 */
	private String verifyUser(String query){
		return "rpi";
	}
	
	/**
	 * This method manages redirecting information about taking pictures on the Raspberry Pi, 
	 * which follows slightly different logic than {@link #redirectTask(String, String)} method.
	 * @param query contains Raspberry Pi ID.
	 * @return {@link SafeHomeServer#OK_ANSWER} if the photo was taken and {@link SafeHomeServer#NO_ANSWER} otherwise.
	 */
	private String waitForAPhoto(String query){
		Map<String, String> params = parseQuery(query);
		String rpi = params.get(SafeHomeServer.RPI_PARAM);
		
		if(rpi != null){
			//Redirect the task
			SafeHomeServer.forRpi.putIfAbsent(rpi, new ConcurrentLinkedQueue<String>());
			SafeHomeServer.forRpi.get(rpi).add(SafeHomeServer.COMMAND_TAKEPHOTO); 
			
			//Wait for the answer
			int tries = 10;
			for(int i = 0; i < tries; i++){
				if(SafeHomeServer.forApp.containsKey(rpi) && !SafeHomeServer.forApp.get(rpi).isEmpty()){
					if(SafeHomeServer.forApp.get(rpi).peek().equals(SafeHomeServer.REQ_PHOTOTAKEN)){
						SafeHomeServer.forApp.get(rpi).poll();
						return SafeHomeServer.OK_ANSWER;
					}
				}
				try {
					Thread.sleep(ONE_SEC);
				} catch (InterruptedException e) {
					log.severe(e.getMessage());	
				}
			}
		}
		SafeHomeServer.forRpi.get(rpi).remove(SafeHomeServer.COMMAND_TAKEPHOTO);
		return SafeHomeServer.NO_ANSWER;
	}
	
	@Override
	public String process(String command, String query) {
		if(command.equals(SafeHomeServer.COMMAND_GETSTATE) | command.equals(SafeHomeServer.COMMAND_SWITCHOFF)
					| command.equals(SafeHomeServer.COMMAND_SWITCHON)){
			return redirectTask(command, query);
		}
		else if(command.equals(SafeHomeServer.REQ_CHECKTASK)){
			//return checktaskAPP(query);
		}
		else if(command.equals(SafeHomeServer.REQ_LOGIN)){
			return verifyUser(query);
		}
		else if(command.equals(SafeHomeServer.COMMAND_TAKEPHOTO)){
			return waitForAPhoto(query);
		}
		else{
			return SafeHomeServer.NO_ANSWER;
		}	
		return SafeHomeServer.NO_ANSWER;
	}
}
