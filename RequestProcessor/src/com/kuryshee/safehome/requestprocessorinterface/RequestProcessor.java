package com.kuryshee.safehome.requestprocessorinterface;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This interface provides abstraction over Safe Home application GET request processing.
 * @author Ekaterina Kurysheva
 */
public interface RequestProcessor {
	
	static Logger log = Logger.getLogger("Request Processor");
	
	static final String DEFAULT_ENCODING = "UTF-8";
	
	/**
	 * This method parsers a standard GET request query.
	 * @param query to parse
	 * @return a map where keys are parameters' names and values are parameters' values.
	 */
	public default Map<String, String> parseQuery(String query){
		Map<String, String> map = new HashMap<>();
		
		//Split the query to the parameters by ampersand.
		String[] params;
		if (query.indexOf("&") != -1){
			params = query.split("&");
		}
		else{
			params = new String[1];
			params[0] = query;
		}
		
		//Parse parameters to the map.
		try{
			for(int i = 0; i < params.length; i++){
				String[] data = params[i].split("=");
				
				if(data.length == 2){//Parameter name and value are present.			
					map.put(URLDecoder.decode(data[0], DEFAULT_ENCODING), 
							URLDecoder.decode(data[1], DEFAULT_ENCODING));
				}
				else if(data.length == 1){//Parameter value is empty.
					map.put(URLDecoder.decode(data[0], DEFAULT_ENCODING), "");
				}
				else{
					log.warning("Invalid query parameters");
				}
			}
		}
		catch(Exception e){
			log.severe("Could not parse query" + query);
		}
		
		return map;
	}
	
	/**
	 * This method processes the request according to the command and provided data in the query.
	 * @param command to navigate process
	 * @param query with parameters
	 * @return response string.
	 */
	public String process(String command, String query);
}
