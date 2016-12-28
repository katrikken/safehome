package com.kuryshee.safehome.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(loadOnStartup = 1, urlPatterns = {"/SafeHomeServer", "/SafeHomeServer/rpi/*", "/SafeHomeServer/app/*"})

//klient posila login, heslo -> server vraci id zarizeni a id klienta -> klient (vybira zarizeni) dalsi requesty posila s parametry user a rpi

public class SafeHomeServer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String servletPathRPi = "/SafeHome/SafeHomeServer/rpi";
	private static final String servletPathApp = "/SafeHome/SafeHomeServer/app";
	
	private static final String STD_CHARSET = "charset=UTF-8";
	
	private static final String COMMAND_CHECKTASK = "/checktask";
	private static final String COMMAND_GETSTATE = "/getstate";
	private static final String COMMAND_SWITCHOFF = "/switchoff";
	private static final String COMMAND_SWITCHON = "/switchon";
	
	
	private static final String NO_ANSWER = "no answer";
	private static final String NO_ANSWER_RPI = "Device is unreachable";
	private static final String OK_ANSWER = "ok";
	private static final String ERROR_ANSWER = "error";
	
	private HashMap<String, String> forRpi = new HashMap<>();
	private HashMap<String, String> forApp = new HashMap<>();
	
	
	private static Logger log = Logger.getLogger("My Servlet");
	
	public SafeHomeServer(){

	}
	
	/*
	 * The method reacts on GET requests to the server depending on the url-pattern which caused the reaction of servlet.
	 * @see ...   
	 * */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String path = request.getRequestURI();
		String query = request.getQueryString();
		
		if(path.startsWith(servletPathApp, 0)){
			
			log.info("--Got app request ");
			
			String command = path.substring(servletPathApp.length());
			response.getWriter().println(processAppRequest(command, query));	
		}
		else if(path.startsWith(servletPathRPi, 0)){
			
			log.info("--Got rpi request");
			
			String command = path.substring(servletPathRPi.length());
			response.getWriter().println(processRpiRequest(command, query));
		}
	}
	
	
	/*
	 * The method decodes query parameters' values from the pattern "param1=value1&param2=value2&param3=value3..." or "param=value"
	 * into array of values.
	 * */
	private String[] parseQuery(String query){
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
			log.severe(e.getMessage());
			log.info("could not parse query");
		}
		
		return answer;
	}
	
		
	private String processRpiRequest(String command, String query){
			
		log.info("--processRpiRequest got request: " + command + "?" + query);

		if(command.equals(COMMAND_GETSTATE) | command.equals(COMMAND_SWITCHOFF)
					| command.equals(COMMAND_SWITCHON)){
			return sendAnswerFromRPI(command, query);
		}
		else if (command.equals(COMMAND_CHECKTASK)){
				return checktaskRPI(query);
		}
		else{
			return NO_ANSWER;
		}
	}
		
	private String checktaskRPI(String query){
		//muze byt chyba v parse query
		try {
			String rpi = parseQuery(query)[0];

			if (forRpi.containsKey(rpi)){	
				log.info("--checktaskRPI has for rpi: " + forRpi.get(rpi));
				
				return forRpi.remove(rpi);
			}
			else{
				return NO_ANSWER;
			}
		} 
		catch (Exception e) {
			log.severe(e.getMessage());
				
			return NO_ANSWER;
		}
	}
	
	private String sendAnswerFromRPI(String command, String query){
		log.info("--sendAnswerFromRPI: " + command + "&" + query);

		try {
			String[] params = parseQuery(query);
			String rpi = params[0];
			String answer = params[1];
				
			forApp.put(rpi + command, answer);
	
		} catch (Exception e) {
			log.severe(e.getMessage());				
		}
		
		return NO_ANSWER;
	}
	
		
	private String processAppRequest(String command, String query){
		log.info("--processAppRequest got command: " + command);
		if(command.equals(COMMAND_GETSTATE) | command.equals(COMMAND_SWITCHOFF)
					| command.equals(COMMAND_SWITCHON)){
			return doCommandAPP(command, query);
		}
		else if(command.equals(COMMAND_CHECKTASK)){
			return checktaskAPP(query);
		}
		else{
			return NO_ANSWER;
		}		
	}
		
	private String checktaskAPP(String query){

		try {
			String user = parseQuery(query)[0];

			if (forApp.containsKey(user)){		
				log.info("--checktaskAPP has for app " + forApp.get(user));
				
				return forApp.remove(user);
			}
			else{
				return NO_ANSWER;
			}
		} 
		catch (Exception e) {
			log.severe(e.getMessage());
				
			return NO_ANSWER;
		}
	}
		
	private String doCommandAPP(String command, String query){
			
		log.info("--doCommandAPP: " + command + "&" + query);
			
		long millis = 100;
		int tries = 20;
			
		try{
			String[] params = parseQuery(query);
			String user = params[0];
			String rpi = params[1];
				
			forRpi.put(rpi, command);
				
			for(int i = 0; i < tries; i++){
				if(!forApp.containsKey(rpi + command)){
					Thread.sleep(millis);
				}
				else{
					break;
				}
			}
			if (forApp.containsKey(rpi + command)){
				log.info("--doCommandAPP got answer from rpi");
					
				return forApp.remove(rpi + command);
			}
			else{
				log.info("--doCommandAPP did not get answer from rpi");
					
				return NO_ANSWER_RPI;
			}
		}
		catch (Exception e) {
			log.severe(e.getMessage());
			
			return NO_ANSWER;
		}
	}
}
