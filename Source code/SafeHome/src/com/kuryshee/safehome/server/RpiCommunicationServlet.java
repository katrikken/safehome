package com.kuryshee.safehome.server;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
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
 * Servlet implementation class.
 * @author Ekaterina Kurysheva.
 */
@WebServlet(loadOnStartup = 1, urlPatterns = {"/SafeHomeServer/rpi/*"})
@MultipartConfig
public class RpiCommunicationServlet extends HttpServlet{
	private static final long serialVersionUID = 1L;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RpiCommunicationServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Logger.getLogger(RpiCommunicationServlet.class.getName()).log(Level.INFO, "--Rpi GET request registered");
		
		String rpiId = request.getHeader(RpiCommunicationConsts.RPI_ID);
		String query = request.getQueryString();
		
		RpiHttpRequestProcessor processor;
		try {
			processor = new RpiHttpRequestProcessor(new InitialContext());
			processor.process(response.getOutputStream(), rpiId, query);
		} catch (NamingException e) {
			response.getWriter().println(AnswerConstants.ERROR_ANSWER);
			Logger.getLogger(RpiCommunicationServlet.class.getName()).log(Level.SEVERE, e.getMessage(), e);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String rpiId = request.getHeader(RpiCommunicationConsts.RPI_ID);
		
		PostDataRetriever db = new PostDataRetriever();
		String action = db.getTextPart(request, RpiCommunicationConsts.ACTION);
		
		Logger.getLogger(RpiCommunicationServlet.class.getName()).log(Level.INFO, "--Rpi: " + rpiId + ", action: " + action);
		
		RpiHttpRequestProcessor processor = null;
		try {
			processor = new RpiHttpRequestProcessor(new InitialContext());
			
			switch (action) {
				case RpiCommunicationConsts.REGISTER_USER: processor.registerUser(response.getOutputStream(), rpiId, 
						db.getTextPart(request, RpiCommunicationConsts.USER_LOGIN), 
						db.getTextPart(request, RpiCommunicationConsts.USER_PASSWORD));
					break;
				case RpiCommunicationConsts.DELETE_USER: processor.deleteUser(response.getOutputStream(), rpiId, 
						db.getTextPart(request, RpiCommunicationConsts.USER_LOGIN));
					break;
				case RpiCommunicationConsts.REGISTER_ACTION: processor.registerAction(response.getOutputStream(), rpiId, 
						db.getTextPart(request, RpiCommunicationConsts.RPI_ACTION_INFO), 
						db.getTextPart(request, RpiCommunicationConsts.TIME),
						db.getTextPart(request, RpiCommunicationConsts.LEVEL));
					break;
				case RpiCommunicationConsts.SAVE_PHOTO: processor.savePhoto(response.getOutputStream(), rpiId, 
						db.getTextPart(request, RpiCommunicationConsts.TIME), 
						db.getTextPart(request, RpiCommunicationConsts.PHOTO_NAME),
						db.getFilePart(request, RpiCommunicationConsts.PHOTO));
					break;
				case RpiCommunicationConsts.POST_STATE: //Not supported in this version.
					response.getWriter().print(AnswerConstants.OK_ANSWER);
					break;
				default: response.getWriter().println(AnswerConstants.ERROR_ANSWER);
					break;
			}
		} catch (NamingException e) {
			response.getWriter().println(AnswerConstants.ERROR_ANSWER);
			Logger.getLogger(RpiCommunicationServlet.class.getName()).log(Level.SEVERE, e.getMessage(), e);
		}
		finally {
			processor.closeConnection();
		}
	}
}
