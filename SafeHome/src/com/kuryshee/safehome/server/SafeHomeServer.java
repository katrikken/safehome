package com.kuryshee.safehome.server;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.kuryshee.safehome.requestprocessorinterface.RequestProcessor;

/**
 * This class extends {@link HttpServlet} class and overrides its doGet and doPost methods.
 * @author Ekaterina Kurysheva
 */
@WebServlet(loadOnStartup = 1, urlPatterns = {"/SafeHomeServer", "/SafeHomeServer/rpi/*", "/SafeHomeServer/app/*"})
@MultipartConfig
public class SafeHomeServer extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * URL address on which this servlet accepts requests form Raspberry Pi.
	 */
	private static final String servletPathRPi = "/SafeHome/SafeHomeServer/rpi";
	
	/**
	 * URL address on which this servlet accepts requests form the client application.
	 */
	private static final String servletPathApp = "/SafeHome/SafeHomeServer/app";
	
	/**
     * The constant for GET request to check up with the server.
     */
    public static final String REQ_CHECKTASK = "/checktask";  
    
    /**
     * The constant for commanding and reporting switching the Raspberry Pi state to off.
     */
    public static final String COMMAND_SWITCHOFF = "/switchoff";
    
    /**
     * The constant for commanding and reporting switching the Raspberry Pi state to on.
     */
    public static final String COMMAND_SWITCHON = "/switchon";
    
    /**
     * The constant for the server to ask for the Raspberry Pi state.
     */
    public static final String COMMAND_GETSTATE = "/getstate";
    
    /**
     * The constant for the server to command Raspberry Pi taking a photo.
     */
    public static final String COMMAND_TAKEPHOTO = "/takephoto";
    
    /**
     * The constant for the POST request containing a photo.
     */
    public static final String UPLOAD_PHOTO = "/upload"; 
    
    /**
     * The constant which is added to the address of a server when reporting motion detection.  
     */
    public static final String REQ_MOTIONDETECTED = "/motiondetected";
    
    /**
     * The constant which is added to the address of a server when reporting the taking of a new photo.  
     */
    public static final String REQ_PHOTOTAKEN = "/photo";  

    /**
     * The constant specifies the part to add to the server path to report switching the state of a program by a token.
     */
    public static final String REQ_RFIDSWITCH = "/rfid";
    
    /**
     * The constant which is added to the address of a server when the client application verifies user credentials.
     */
    public static final String REQ_LOGIN = "/login";
    
    /**
     * The constant for requesting data about Raspberry Pi usage history.
     */
    public static final String REQ_HISTORY = "/history";
    
    /**
     * The definition of the answer when no data are available.
     */
    public static final String NO_ANSWER = "no answer";
    
    /**
     * The definition of the answer when everything went as expected.
     */
    public static final String OK_ANSWER = "ok";
    
    /**
     * The definition of the answer when error occurred.
     */
    public static final String ERROR_ANSWER = "error";
    
    /**
     * The constant for the POST request parameter of time.
     */
    public static final String TIME_PARAM = "time";
    
    /**
     * The constant for the POST request parameter of this Raspberry Pi ID.
     */
    public static final String RPI_PARAM = "rpi";
    
    /**
     * The constant for the POST request parameter of photo.
     */
    public static final String PHOTO_PARAM = "photo";
    
    /**
     * The constant for the POST request parameter containing the name associated with used RFID token.
     */
    public static final String RFID_PARAM = "rfid";
    
    /**
     * The constant for the GET request parameter containing short answer.
     */
    public static final String ANSWR_PARAM = "answer";

    /**
     * The constant for key of a property for insertion into database.
     */
    public static final String COMMAND_PARAM = "command";
    
    /**
     * The path to store incoming pictures in.
     */
    public static final String PHOTO_PATH = "Pictures\\";
	
    /**
     * The map of tasks for Raspberry Pi to send upon {@link SafeHomeServer#REQ_CHECKTASK}.
     */
	public static ConcurrentMap<String, ConcurrentLinkedQueue<String>> forRpi = new ConcurrentHashMap<>();
	
	/**
	 * The map of tasks for the client application to send upon {@link SafeHomeServer#REQ_CHECKTASK}.
	 */
	public static ConcurrentMap<String, ConcurrentLinkedQueue<String>> forApp = new ConcurrentHashMap<>();

	private static Logger log = Logger.getLogger("My Servlet");
	
	/**
	 * The method processes GET requests depending on the URL-pattern, which triggered the servlet. 
	 * @throws ServletException, IOException.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RequestProcessor processor;
		
		String path = request.getRequestURI();
		String query = request.getQueryString();
		
		if(path.startsWith(servletPathApp, 0)){
			
			log.log(Level.INFO, "--Got app request {0}", path + " " + query);
			
			String command = path.substring(servletPathApp.length());
			processor = new AppRequestProcessor();
			response.getWriter().println(processor.process(command, query));	
		}
		else if(path.startsWith(servletPathRPi, 0)){
			
			log.log(Level.INFO, "--Got rpi request {0}", path + " " + query);
			
			String command = path.substring(servletPathRPi.length());
			processor = new RpiRequestProcessor();
			response.getWriter().println(processor.process(command, query));
		}
		else{
			log.log(Level.WARNING, "--Invalid GET request {0}", path + " " + query);
		}
	}
	
	/**
	 * The method processes POST requests from RPi. 
	 * @throws ServletException, IOException.
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = request.getRequestURI();
		
		if(path.startsWith(servletPathRPi + UPLOAD_PHOTO, 0)){
			log.info("-- Got new photo");
			RpiRequestProcessor processor = new RpiRequestProcessor();
			String answer = processor.uploadPhoto(request);
			response.getWriter().println(answer);		
		}
		else if(path.startsWith(servletPathRPi + REQ_RFIDSWITCH, 0)){
			log.info("-- RFID switch");
			RpiRequestProcessor processor = new RpiRequestProcessor();
			String answer = processor.RFIDswitch(request);
			response.getWriter().println(answer);		
		}
		else{
			log.warning("--Invalid POST request " + path);
		}
	}
		
}
