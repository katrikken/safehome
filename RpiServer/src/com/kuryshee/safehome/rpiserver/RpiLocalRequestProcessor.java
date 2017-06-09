package com.kuryshee.safehome.rpiserver;

import java.util.logging.Logger;
import com.kuryshee.safehome.requestprocessorinterface.RequestProcessor;

/**
 * This class implements {@link RequestProcessor} interface according to this servlet REST API.
 * @author Ekaterina Kurysheva
 */
public class RpiLocalRequestProcessor implements RequestProcessor{

	private static Logger log = Logger.getLogger("Local Request Processor");

	/**
	 * This method is a part of the interface implementation.
	 * It chooses actions for incoming requests.
	 */
	public String process(String command, String query) {
		if (command.equals(RpiServlet.REQ_CHECKTASK)){
			return checktask();
		}	
		else{
			return RpiServlet.NO_ANSWER;
		}	
	}
	
	/**
	 * This method checks queue of {@link RpiServlet#tasks} if there is any for Raspberry Pi.
	 * @return task string
	 */
	private String checktask(){
		if (!RpiServlet.tasks.isEmpty()){	
			log.info("-- Local server has task for rpi " + RpiServlet.tasks.peek());
				
			return RpiServlet.tasks.poll();
		}		
		
		return RpiServlet.NO_ANSWER;
	}
}
