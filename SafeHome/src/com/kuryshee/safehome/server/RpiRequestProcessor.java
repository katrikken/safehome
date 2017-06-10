package com.kuryshee.safehome.server;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.http.HttpServletRequest;

import com.kuryshee.safehome.postrequestretriever.DataRetriever;
import com.kuryshee.safehome.requestprocessorinterface.RequestProcessor;

/**
 * This class handles HTTP requests from RPi.
 * @implements {@link RequestProcessor}
 * @author Ekaterina Kurysheva
 */
public class RpiRequestProcessor implements RequestProcessor{

	/**
	 * This method is a bridge between App requests and RPi answers.
	 * @param command is a type of request from the app.
	 * @param query contains the answer from the RPi.
	 * @return value is the answer to the RPi request. 
	 * In case RPi answer was successfully forwarded, returns {@link SafeHomeServer#OK_ANSWER}, otherwise {@link SafeHomeServer#ERROR_ANSWER}.
	 */
	private String sendAnswerFromRPItoApp(String command, String query){
		log.info("--sendAnswerFromRPI: " + command + "&" + query);

		try {
			Map<String, String> params = parseQuery(query);
			String rpi = params.get(SafeHomeServer.RPI_PARAM);
			String answer = params.get(SafeHomeServer.ANSWR_PARAM);
			
			if(rpi != null && answer != null){
				SafeHomeServer.forApp.putIfAbsent(rpi, new ConcurrentLinkedQueue<String>());
				SafeHomeServer.forApp.get(rpi).add(answer);
				
				return SafeHomeServer.OK_ANSWER;
			}
			else{
				return SafeHomeServer.ERROR_ANSWER;
			}	
	
		} catch (Exception e) {
			log.severe(e.getMessage());	
			return SafeHomeServer.ERROR_ANSWER;
		}
	}
	
	/**
	 * This method records switching of the RPi state invoked by valid tokens.
	 * @param request is a POST request with data.
	 * @return value {@link SafeHomeServer#OK_ANSWER} if data were successfully recorded, otherwise {@link SafeHomeServer#ERROR_ANSWER}. 
	 */
	public String RFIDswitch(HttpServletRequest request){
		DataRetriever dr = new DataRetriever();		
		String rpi = dr.getTextPart(request, SafeHomeServer.RPI_PARAM);
		String rfid = dr.getTextPart(request, SafeHomeServer.RFID_PARAM);
		
		if(!rpi.isEmpty() && !rfid.isEmpty()){
			//database?
			/*try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(rpiId, true)))){
				    out.println("User key from " + user + " was detected.");
			} catch (IOException e) {
				log.severe(e.getMessage());	
			}*/
			return SafeHomeServer.OK_ANSWER;
		}
		else
			return SafeHomeServer.ERROR_ANSWER;
	}
	
	/**
	 * This method uploads photo from the RPi.
	 * @param request is a POST request with data.
	 * @return value {@link SafeHomeServer#OK_ANSWER} if data were successfully uploaded, otherwise {@link SafeHomeServer#ERROR_ANSWER}. 
	 */
	public String uploadPhoto(HttpServletRequest request){
		DataRetriever dr = new DataRetriever();		
		
		String rpi = dr.getTextPart(request, SafeHomeServer.RPI_PARAM);
		String time = dr.getTextPart(request, SafeHomeServer.TIME_PARAM);
		
		if(!rpi.isEmpty() && !time.isEmpty()){
			String fileName = rpi + "_" + time; 
		    
			Boolean ok = dr.saveFilePart(request, SafeHomeServer.PHOTO_PARAM, SafeHomeServer.PHOTO_PATH + fileName);
			if(ok){
			//database?
				/*try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(rpiId, true)))){
					    
				} catch (IOException e) {
					log.severe(e.getMessage());	
				}*/
				return SafeHomeServer.OK_ANSWER;
			}
		}

		return SafeHomeServer.ERROR_ANSWER;
	}
	
	/**
	 * This method records changes in RPi invoked by motion detection
	 * @param query contains RPi id
	 * @return value is {@link SafeHomeServer#OK_ANSWER} if data were successfully recorded, otherwise {@link SafeHomeServer#ERROR_ANSWER}.
	 */
	private String RFIDevent(String command, String query){
		Map<String, String> params = parseQuery(query);
		String rpiId = params.get(SafeHomeServer.RPI_PARAM);
		
		if(rpiId != null){
			//database?
			/*try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(rpiId, true)))){
				if(command.equals(REQ_MOTIONDETECTED)){
				    out.println("Motion was detected.");
				}
				else{
					out.println("Photo was made.");
				}
			} catch (IOException e) {
				log.severe(e.getMessage());	
			}*/
			
			return SafeHomeServer.OK_ANSWER;
		}
	
		return SafeHomeServer.ERROR_ANSWER;
	}
	
	/**
	 * This method checks if the app has requests for the RPi.
	 * @param query contains RPi id data.
	 * @return value is a query if the task exists, 
	 * {@link SafeHomeServer#NO_ANSWER} if no task is present, 
	 * {@link SafeHomeServer#ERROR_ANSWER} if error occurred.
	 */
	private String checkTaskForRPi(String query){
		try {
			Map<String, String> params = parseQuery(query);
			String rpi = params.get(SafeHomeServer.RPI_PARAM);
			
			if(rpi != null){
				if (SafeHomeServer.forRpi.containsKey(rpi) && !SafeHomeServer.forRpi.get(rpi).isEmpty()){	
					log.info("--checktaskRPI has task for rpi");
					
					return SafeHomeServer.forRpi.get(rpi).poll();
				}	
				else{
					return SafeHomeServer.NO_ANSWER;
				}
			}
			else{
				return SafeHomeServer.ERROR_ANSWER;
			}		
		} 
		catch (Exception e) {
			log.severe(e.getMessage());
			return SafeHomeServer.ERROR_ANSWER;
		}
	}
	
	@Override
	public String process(String command, String query) {
		if(command.equals(SafeHomeServer.COMMAND_GETSTATE) | command.equals(SafeHomeServer.COMMAND_SWITCHOFF)
				| command.equals(SafeHomeServer.COMMAND_SWITCHON) | command.equals(SafeHomeServer.REQ_PHOTOTAKEN)){
			return sendAnswerFromRPItoApp(command, query);
		}
		else if (command.equals(SafeHomeServer.REQ_CHECKTASK)){
			return checkTaskForRPi(query);
		}
		else if (command.equals(SafeHomeServer.REQ_MOTIONDETECTED) || command.equals(SafeHomeServer.REQ_PHOTOTAKEN)){
			return RFIDevent(command, query);
		}
		else{
			return SafeHomeServer.NO_ANSWER;
		}
	}
}
