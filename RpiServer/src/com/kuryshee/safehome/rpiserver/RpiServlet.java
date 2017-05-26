package com.kuryshee.safehome.rpiserver;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Servlet implementation class RpiServlet
 */
@WebServlet(loadOnStartup = 1, urlPatterns = {"/RpiServlet/*"})
public class RpiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String servletPath = "/RpiServer/RpiServlet";
	
	public static final String REQ_CHECKTASK = "/checktask";
	public static final String REQ_READCARD = "/read";
    public static final String COMMAND_SAVEUSER = "/saveuser";
	public static final String NO_ANSWER = "no answer";
	public static final String OK_ANSWER = "ok";
	public static final String ERROR_ANSWER = "error";
	
	public static ConcurrentLinkedQueue<String> requests = new ConcurrentLinkedQueue<>();
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
		
		String command = path.substring(servletPath.length());
		RpiLocalRequestProcessor processor = new RpiLocalRequestProcessor();
		
		String answer = processor.process(command, query);
		
		response.getWriter().println(answer);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {	
		String path = request.getRequestURI();
		if(path.startsWith(servletPath + REQ_READCARD, 0)){
			try{
				String rpiId = request.getParameter("rpi"); 
				log.log(Level.INFO, "-- Got key from RPi " + rpiId);
				String cardID = request.getParameter("card"); 
		   
				ServletContext sc = getServletContext();
				sc.setAttribute("card", cardID);
				log.log(Level.INFO, "-- Key is passed to the servlet context");
			}
			catch(Exception e){
				response.getWriter().println(ERROR_ANSWER);
				log.log(Level.WARNING, "--Failed to fetch card data with an exception", e);
			}
		}
		else{
			log.warning("--Invalid POST request " + path);
		}		 
	}
}
