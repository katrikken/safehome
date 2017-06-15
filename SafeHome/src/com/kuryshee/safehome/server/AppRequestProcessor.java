package com.kuryshee.safehome.server;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.kuryshee.safehome.requestprocessorinterface.RequestProcessor;

public class AppRequestProcessor implements RequestProcessor{
	private int ONE_SEC = 1000;

	/*private static final String NO_ANSWER_RPI = "Device is unreachable";
	
	private String doCommandAPP(String command, String query){
		
		log.info("--doCommandAPP: " + command + "&" + query);
			
		long millis = 100;
		int tries = 20;
			
		try{
			String[] params = parseQuery(query);
			String user = params[0];
			String rpi = params[1];
				
			SafeHomeServer.forRpi.put(rpi, command);
				
			for(int i = 0; i < tries; i++){
				if(!SafeHomeServer.forApp.containsKey(rpi + command)){
					Thread.sleep(millis);
				}
				else{
					break;
				}
			}
			if (SafeHomeServer.forApp.containsKey(rpi + command)){
				log.info("--doCommandAPP got answer from rpi");
					
				return SafeHomeServer.forApp.remove(rpi + command);
			}
			else{
				log.info("--doCommandAPP did not get answer from rpi");
					
				return NO_ANSWER_RPI;
			}
		}
		catch (Exception e) {
			log.severe(e.getMessage());	
		}
		
		return SafeHomeServer.NO_ANSWER;
	}
	
	private String checktaskAPP(String query){

		try {
			String user = parseQuery(query)[0];

			if (SafeHomeServer.forApp.containsKey(user)){		
				log.info("--checktaskAPP has task for app");
				
				return SafeHomeServer.forApp.remove(user);
			}
			else{
				return SafeHomeServer.NO_ANSWER;
			}
		} 
		catch (Exception e) {
			log.severe(e.getMessage());
				
			return SafeHomeServer.NO_ANSWER;
		}
	}
	*/
	
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
