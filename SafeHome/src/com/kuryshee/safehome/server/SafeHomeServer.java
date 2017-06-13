package com.kuryshee.safehome.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import javax.servlet.http.Part;

import com.kuryshee.safehome.requestprocessorinterface.RequestProcessor;

@WebServlet(loadOnStartup = 1, urlPatterns = {"/SafeHomeServer", "/SafeHomeServer/rpi/*", "/SafeHomeServer/app/*"})
@MultipartConfig
/**
 * This class extends HttpServlet and overrides doGet and doPost methods.
 */
public class SafeHomeServer extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	private static final String servletPathRPi = "/SafeHome/SafeHomeServer/rpi";
	private static final String servletPathApp = "/SafeHome/SafeHomeServer/app";
	
	private static final String STD_CHARSET = "charset=UTF-8";
	
	public static final String REQ_CHECKTASK = "/checktask";
	public static final String REQ_PHOTOTAKEN = "/photo";  
	public static final String REQ_RFIDSWITCH = "/rfid";  
    public static final String REQ_MOTIONDETECTED = "/motiondetected";
	
	public static final String COMMAND_GETSTATE = "/getstate";
	public static final String COMMAND_SWITCHOFF = "/switchoff";
	public static final String COMMAND_SWITCHON = "/switchon";
	public static final String COMMAND_TAKEPHOTO = "/takephoto";
	
	public static final String UPLOAD_PHOTO = "/upload"; 
	
	public static final String NO_ANSWER = "no answer";
	
	public static final String OK_ANSWER = "ok";
	public static final String ERROR_ANSWER = "error";
	
	public static final String RPI_PARAM = "rpi";
	public static final String ANSWR_PARAM = "answer";
	/**
     * The constant for the POST request parameter containing the name associated with used RFID token.
     */
    public static final String RFID_PARAM = "rfid";
    
    /**
     * The constant for the POST request parameter of time.
     */
    public static final String TIME_PARAM = "time";
    
    /**
     * The constant for the POST request parameter of photo.
     */
    public static final String PHOTO_PARAM = "photo";
    
    /**
     * The constant for key of a property for insertion into database.
     */
    public static final String COMMAND_PARAM = "command";
    
    public static final String PHOTO_PATH = "Pictures\\";
	
	public static ConcurrentMap<String, ConcurrentLinkedQueue<String>> forRpi = new ConcurrentHashMap<>();
	public static ConcurrentMap<String, ConcurrentLinkedQueue<String>> forApp = new ConcurrentHashMap<>();

	private static Logger log = Logger.getLogger("My Servlet");
	
	/**
	 * The method processes GET requests depending on the url-pattern, which caused the reaction of the servlet. 
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
