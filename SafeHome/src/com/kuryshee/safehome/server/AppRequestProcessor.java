package com.kuryshee.safehome.server;

import com.kuryshee.safehome.requestprocessorinterface.RequestProcessor;

public class AppRequestProcessor implements RequestProcessor{

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
	@Override
	public String process(String command, String query) {
		log.info("--processAppRequest got command: " + command);
		if(command.equals(SafeHomeServer.COMMAND_GETSTATE) | command.equals(SafeHomeServer.COMMAND_SWITCHOFF)
					| command.equals(SafeHomeServer.COMMAND_SWITCHON)){
			//return doCommandAPP(command, query);
		}
		else if(command.equals(SafeHomeServer.REQ_CHECKTASK)){
			//return checktaskAPP(query);
		}
		else{
			return SafeHomeServer.NO_ANSWER;
		}	
		return SafeHomeServer.NO_ANSWER;
	}

}
