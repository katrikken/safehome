package com.kuryshee.safehome.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet(loadOnStartup = 1, urlPatterns = {"/SafeHomeServer", "/SafeHomeServer/rpi/*", "/SafeHomeServer/app/*"})

//klient posila login, heslo -> 
//server vraci id zarizeni a id klienta -> klient (vybira zarizeni) dalsi requesty posila s parametry user a rpi

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
	
	private static final String UPLOAD_PHOTO = "/upload"; 
	
	public static final String NO_ANSWER = "no answer";
	
	public static final String OK_ANSWER = "ok";
	private static final String ERROR_ANSWER = "error";
	
	public static ConcurrentMap<String, String> forRpi = new ConcurrentHashMap<>();
	public static ConcurrentMap<String, String> forApp = new ConcurrentHashMap<>();
	
	RpiRequestProcessor rpiProcessor = new RpiRequestProcessor();
	AppRequestProcessor appProcessor = new AppRequestProcessor();
	private static Logger log = Logger.getLogger("My Servlet");
	
	/**
	 * The method processes GET requests depending on the url-pattern, which caused the reaction of the servlet. 
	 * @throws ServletException, IOException.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String path = request.getRequestURI();
		String query = request.getQueryString();
		
		if(path.startsWith(servletPathApp, 0)){
			
			log.info("--Got app request ");
			
			String command = path.substring(servletPathApp.length());
			response.getWriter().println(appProcessor.process(command, query));	
		}
		else if(path.startsWith(servletPathRPi, 0)){
			
			log.info("--Got rpi request");
			
			String command = path.substring(servletPathRPi.length());
			response.getWriter().println(rpiProcessor.process(command, query));
		}
		else{
			log.warning("--Invalid GET request " + path + query);
		}
	}
	
	/**
	 * The method processes POST requests from RPi. 
	 * @throws ServletException, IOException.
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = request.getRequestURI();
		if(path.startsWith(servletPathRPi + UPLOAD_PHOTO, 0)){
		    String rpiId = request.getParameter("rpi"); 
		    Part filePart = request.getPart("photo"); 
		    String fileName = rpiId + "_" + request.getParameter("time"); 
		    InputStream fileContent = filePart.getInputStream();
		    
		    File photo = new File(fileName + ".jpg");
		    byte[] buffer = new byte[fileContent.available()];
		    fileContent.read(buffer);
		    OutputStream outStream = new FileOutputStream(photo);
		    outStream.write(buffer);
		}
		else{
			log.warning("--Invalid POST request " + path);
		}
	}
		
}
