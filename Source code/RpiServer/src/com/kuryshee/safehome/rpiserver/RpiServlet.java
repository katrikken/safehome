package com.kuryshee.safehome.rpiserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.kuryshee.safehome.httprequestsender.AnswerConstants;
import com.kuryshee.safehome.requestdataretriever.PostDataRetriever;
import com.kuryshee.safehome.rpicommunicationconsts.RpiCommunicationConsts;


/**
 * Servlet implementation class RpiServlet.
 * This servlet is placed locally on the Raspberry Pi and is accessed from the local network.
 * 
 * @author Ekaterina Kurysheva
 */
@WebServlet(loadOnStartup = 1, urlPatterns = {"/RpiServlet/*"})
@MultipartConfig
public class RpiServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * The path to the shared configuration file.
	 */
    private static final String CONFIG_R = "/WEB-INF/config/config.json";
    
    /**
     * The real path to the configuration file
     */
    public static String CONFIG = null;
    
    public void init(ServletConfig servletConfig) throws ServletException{
        super.init(servletConfig);
        CONFIG = servletConfig.getServletContext().getRealPath(CONFIG_R);
    }
	
	/**
	 * The queue for the tasks to the Raspberry Pi logic part application.
	 * The queue is being polled when {@link RpiCommunicationConsts#GET_TASK} arrives.
	 */
	public static ConcurrentLinkedQueue<String> tasks = new ConcurrentLinkedQueue<>();
	
	private static Logger log = Logger.getLogger(RpiServlet.class.getName());   

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String query = request.getQueryString();
		log.log(Level.INFO, "-- Got request {0}", query);
		
		RpiLocalRequestProcessor processor = new RpiLocalRequestProcessor();
		processor.process(response.getOutputStream(), query);
	}

	/**
	 * This method processes POST request and sends relevant data to the Servlet context.
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {	
		PostDataRetriever db = new PostDataRetriever();
		String action = db.getTextPart(request, RpiCommunicationConsts.ACTION);
		
		if(action.equals(RpiCommunicationConsts.COMMAND_READTOKEN)){ //The servlet gets POST request with the token data.
			try{		
				String token = db.getTextPart(request, RpiCommunicationConsts.CARD_PARAM); //Fetch token data from the request.
				
				if(token!= null && !token.isEmpty()){
					ServletContext sc = getServletContext();
					sc.setAttribute(RpiCommunicationConsts.CARD_PARAM, token.trim());
					
					log.log(Level.INFO, "-- Token is passed to the servlet context");
					
					response.getWriter().println(AnswerConstants.OK_ANSWER);
				}
				else{
					log.log(Level.INFO, "-- Token is not in the request");
					response.getWriter().println(AnswerConstants.ERROR_ANSWER);
				}
			}
			catch(Exception e){
				response.getWriter().println(AnswerConstants.ERROR_ANSWER);
				log.log(Level.WARNING, "--Failed to fetch token data with an exception", e);
			}
		}
		else{
			log.log(Level.WARNING, "--Invalid POST request");
		}		 
	}
	
	/**
	 * Reads configuration file {@link RpiServlet#CONFIG} and saves its values.
	 * @return directory, where shared user configuration is.
	 */
	public static String readConfig(){	
		if(CONFIG != null) {
		
			try(InputStream is = new FileInputStream(new File(CONFIG)); 
					JsonReader reader = Json.createReader(is)){
				JsonObject conf = reader.readObject();
				String path = conf.getString("keys");	
				return path;
			
			} catch (IOException e) {
				log.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		log.log(Level.WARNING, "Configuration file not found");
		return "";
	}
}
