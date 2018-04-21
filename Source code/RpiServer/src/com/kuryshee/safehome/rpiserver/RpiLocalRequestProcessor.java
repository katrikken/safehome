package com.kuryshee.safehome.rpiserver;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletOutputStream;

import com.kuryshee.safehome.httprequestsender.AnswerConstants;
import com.kuryshee.safehome.requestprocessorinterface.RequestProcessor;
import com.kuryshee.safehome.rpicommunicationconsts.RpiCommunicationConsts;

/**
 * This class implements {@link RequestProcessor} interface according to this servlet's REST API.
 * 
 * @author Ekaterina Kurysheva
 */
public class RpiLocalRequestProcessor implements RequestProcessor{

	private static Logger log = Logger.getLogger(RpiLocalRequestProcessor.class.getName());

	/**
	 * Method is a part of the interface implementation.
	 * It chooses actions for incoming requests.
	 */
	@Override
	public void process(ServletOutputStream output, String... parameters) {
		try {
			if(parameters != null && parameters.length  > 0) {
				Map<String, String> params = parseQuery(parameters[0]);
				String action = params.get(RpiCommunicationConsts.ACTION);
				switch(action) {
					case RpiCommunicationConsts.GET_TASK: 
						output.println(checktask());
						break;
					default: output.println(AnswerConstants.ERROR_ANSWER);
						break;
				}
			}	
		}
		catch(Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	/**
	 * Method checks queue of {@link RpiServlet#tasks} if there is any for Raspberry Pi.
	 * @return task string
	 */
	private String checktask(){
		if (!RpiServlet.tasks.isEmpty()){	
			log.info("-- Local server has task for rpi " + RpiServlet.tasks.peek());
				
			return RpiServlet.tasks.poll();
		}		
		
		return AnswerConstants.NO_ANSWER;
	}
}
