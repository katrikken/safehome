package com.kuryshee.safehome.rpiserver;

import java.net.URLDecoder;
import java.util.logging.Logger;

public class RpiLocalRequestProcessor{

	private static Logger log = Logger.getLogger("Processor");
	private static final String STD_CHARSET = "charset=UTF-8";
	
	private String key;
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String process(String command, String query) {
		if (command.equals(RpiServlet.REQ_CHECKTASK)){
			return checktask();
		}
		else if (command.equals(RpiServlet.REQ_KEYDETECTED)){
			return processKey(query);
		}	
		else{
			return RpiServlet.NO_ANSWER;
		}	
	}
	
	/**
	 * This method indicates to the servlet if an awaited card key was detected.
	 * @return true if card key is stored.
	 */
	public boolean cardIsRead(){
		return !getKey().isEmpty();
	}
	
	/**
	 * This method checks queue of tasks if there is any for Raspberry Pi.
	 * @return task
	 */
	private String checktask(){
		try {

			if (!RpiServlet.tasks.isEmpty()){	
				log.info("--Local server has task for rpi");
				
				return RpiServlet.tasks.poll();
			}		
		} 
		catch (Exception e) {
			log.severe(e.getMessage());
		}
		return RpiServlet.NO_ANSWER;
	}
	
	/**
	 * This method parses the card key and stores it.
	 * @param query
	 * @return ok if the key is present. 
	 */
	private String processKey(String query){
		try {
			String[] params = parseQuery(query);
			setKey(params[0]);
			
			return RpiServlet.OK_ANSWER;
		} 
		catch (Exception e) {
			log.severe(e.getMessage());
		}
		return RpiServlet.NO_ANSWER;
	}
	
	/**
	 * @param query
	 * @return array of parameter values from GET request query part.
	 */
	public String[] parseQuery(String query){
		String[] params;
		if (query.indexOf("&") != -1){
			params = query.split("&");
		}
		else{
			params = new String[1];
			params[0] = query;
		}
		String[] answer = new String[params.length];
		try{
			for(int i = 0; i < params.length; i++){
				answer[i] = URLDecoder.decode(params[i].split("=")[1], STD_CHARSET);
			}
		}
		catch(Exception e){
			log.severe("Could not parse query" + query);
		}
		
		return answer;
	}

}
