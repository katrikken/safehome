package com.kuryshee.safehome.server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * This class handles HTTP requests from RPi.
 * @implements RequestProcessor
 */
public class RpiRequestProcessor implements RequestProcessor{
	
	private static final String REQ_RFIDSWITCH = "/rfid"; 
	private static final String REQ_MOTIONDETECTED = "/motiondetected";
	private static final String REQ_PHOTOTAKEN = "/photo"; 

	/**
	 * This method is a bridge between App requests and RPi answers.
	 * @param command is a type of request from the app.
	 * @param query contains the answer.
	 * @return value is the answer to the RPi request, which provided answer to the App.
	 */
	private String sendAnswerFromRPItoApp(String command, String query){
		log.info("--sendAnswerFromRPI: " + command + "&" + query);

		try {
			String[] params = parseQuery(query);
			String rpi = params[0];
			String answer = params[1];
				
			SafeHomeServer.forApp.put(rpi + command, answer);
	
		} catch (Exception e) {
			log.severe(e.getMessage());				
		}
		
		return SafeHomeServer.NO_ANSWER;
	}
	
	/**
	 * This method records changes in RPi invoked by using valid cards.
	 * @param query contains information about who used the card.
	 * @return value is the answer to the RPi request, which provided data.
	 */
	private String RFIDswitch(String query){
		String[] params = parseQuery(query);
		String rpiId = params[0];
		String user = params[1];
		
		//database?
		try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(rpiId, true)))){
			    out.println("User key from " + user + " was detected.");
		} catch (IOException e) {
			log.severe(e.getMessage());	
		}
		
		return SafeHomeServer.OK_ANSWER;
	}
	
	/**
	 * This method records changes in RPi invoked by motion detection
	 * @param query 
	 * @return value is the answer to the RPi request, which provided data.
	 */
	private String RFIDevent(String command, String query){
		String[] params = parseQuery(query);
		String rpiId = params[0];
		
		//database?
		try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(rpiId, true)))){
			if(command.equals(REQ_MOTIONDETECTED)){
			    out.println("Motion was detected.");
			}
			else{
				out.println("Photo was made.");
			}
		} catch (IOException e) {
			log.severe(e.getMessage());	
		}
		
		return SafeHomeServer.OK_ANSWER;
	}
	
	/**
	 * This method checks if the app has requests for the RPi.
	 * @param query contains RPi id data.
	 * @return value is a query if the task exists or no answer value.
	 */
	private String checktaskRPI(String query){
		try {
			String rpi = parseQuery(query)[0];

			if (SafeHomeServer.forRpi.containsKey(rpi)){	
				log.info("--checktaskRPI has task for rpi");
				
				return SafeHomeServer.forRpi.remove(rpi);
			}		
		} 
		catch (Exception e) {
			log.severe(e.getMessage());
		}
		return SafeHomeServer.NO_ANSWER;
	}
	
	@Override
	public String process(String command, String query) {
		if(command.equals(SafeHomeServer.COMMAND_GETSTATE) | command.equals(SafeHomeServer.COMMAND_SWITCHOFF)
				| command.equals(SafeHomeServer.COMMAND_SWITCHON)){
			return sendAnswerFromRPItoApp(command, query);
		}
		else if (command.equals(SafeHomeServer.REQ_CHECKTASK)){
			return checktaskRPI(query);
		}
		else if (command.equals(REQ_MOTIONDETECTED) || command.equals(REQ_PHOTOTAKEN)){
			return RFIDevent(command, query);
		}
		else if (command.equals(REQ_RFIDSWITCH)){
			return RFIDswitch(query);
		}
		else{
			return SafeHomeServer.NO_ANSWER;
		}
	}
}
