package com.kuryshee.safehome.server;

import java.net.URLDecoder;
import java.util.logging.Logger;

public interface RequestProcessor {
	
	static Logger log = Logger.getLogger("Processor");
	static final String STD_CHARSET = "charset=UTF-8";
	
	/**
	 * @param query
	 * @return array of parameter values from GET request query part.
	 */
	public default String[] parseQuery(String query){
		String[] params;
		if (query.indexOf("&") != -1){
			params = query.split("&");
		}
		else{
			params = new String[1];
		}
		String[] answer = new String[params.length];
		try{
			for(int i = 0; i < params.length; i++){
				answer[i] = URLDecoder.decode(params[i].split("=")[1], STD_CHARSET);
			}
		}
		catch(Exception e){
			log.severe("Could not parse query" + e.getMessage());
		}
		
		return answer;
	}
	
	/**
	 * This method processes the request according to the command and provided data in the query.
	 * @param command,
	 * @param query,
	 * @return response.
	 */
	public String process(String command, String query);
}
