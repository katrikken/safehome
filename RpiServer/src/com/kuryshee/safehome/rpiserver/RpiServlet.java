package com.kuryshee.safehome.rpiserver;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.kuryshee.safehome.postrequestretriever.DataRetriever;


/**
 * Servlet implementation class RpiServlet.
 * This servlet is placed locally on the Raspberry Pi and is accessed from the local network.
 * @author Ekaterina Kurysheva
 */
@WebServlet(loadOnStartup = 1, urlPatterns = {"/RpiServlet/*"})
@MultipartConfig
public class RpiServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * The constant path of this servlet.
	 */
	private static final String SERVLET_PATH = "/RpiServer/RpiServlet";
	
	/**
	 * The POST request parameter for passing the token data.
	 */
	public static final String CARD_PARAM = "card";
	
	/**
	 * The constant for the Raspberry Pi to ask for new tasks from server.
	 */
	public static final String REQ_CHECKTASK = "/checktask";
	
	/**
	 * The constant for the server to tell the Raspberry Pi to read a token.
	 */
	public static final String COMMAND_READTOKEN = "/read";
	
	/**
	 * The constant for the server to tell the Raspberry Pi to update user list.
	 */
    public static final String COMMAND_UPDATEUSERS = "/saveuser";
    
    /**
     * The constant for request responses with no data. 
     */
	public static final String NO_ANSWER = "no answer";
	
	/**
	 * The constant for request responses which were successfully completed.
	 */
	public static final String OK_ANSWER = "ok";
	
	/**
	 * The constant for request responses which encountered an error.
	 */
	public static final String ERROR_ANSWER = "error";

	
	/**
	 * The constant contains path to the file with registered tokens for a card reader.
	 */
	public static final String USERCONFIG = "/home/pi/NetBeansProjects/com.kuryshee.safehome.rpi/dist/keys.txt";
	//public static final String USERCONFIG = "keys.txt";
	
	/**
	 * The constant contains key word for the configuration file {@link #USERCONFIG}.
	 */
	public static final String KEY = "key ";
	
	/**
	 * The queue for the tasks to the Raspberry Pi logic part application.
	 * The queue is being polled when {@link #REQ_CHECKTASK} arrives.
	 */
	public static ConcurrentLinkedQueue<String> tasks = new ConcurrentLinkedQueue<>();
	
	private static Logger log = Logger.getLogger("RPi Servlet");
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RpiServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String path = request.getRequestURI();
		String query = request.getQueryString();
		log.log(Level.INFO, "-- Got request {0}", path + " " + query);
		
		String command = path.substring(SERVLET_PATH.length());
		RpiLocalRequestProcessor processor = new RpiLocalRequestProcessor();
		
		String answer = processor.process(command, query);
		
		response.getWriter().println(answer);
	}

	/**
	 * This method processes POST request and sends relevant data to the Servlet context.
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {	
		String path = request.getRequestURI();
		
		if(path.startsWith(SERVLET_PATH + COMMAND_READTOKEN, 0)){ //The servlet gets POST request with the token data.
			try{
				DataRetriever dr = new DataRetriever();			
				String token = dr.getTextPart(request, CARD_PARAM); //Fetch token data from the request.
				
				if(!token.isEmpty()){
					ServletContext sc = getServletContext();
					sc.setAttribute(CARD_PARAM, token.trim());
					
					log.log(Level.INFO, "-- Token is passed to the servlet context");
					
					response.getWriter().println(OK_ANSWER);
				}
				else{
					log.log(Level.INFO, "-- Token is not in the request");
					response.getWriter().println(ERROR_ANSWER);
				}
			}
			catch(Exception e){
				response.getWriter().println(ERROR_ANSWER);
				log.log(Level.WARNING, "--Failed to fetch token data with an exception", e);
			}
		}
		else{
			log.warning("--Invalid POST request " + path);
		}		 
	}
}
