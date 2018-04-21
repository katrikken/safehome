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

import com.kuryshee.safehome.appcommunicationconsts.AppCommunicationConsts;
import com.kuryshee.safehome.requestdataretriever.PostDataRetriever;

/**
 * Servlet implementation class AndroidAppServlet
 * @author Ekaterina Kurysheva
 */
@WebServlet(loadOnStartup = 1, urlPatterns = {"/SafeHomeServer/app/*"})
@MultipartConfig
public class AndroidAppServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AndroidAppServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String token = request.getHeader(AppCommunicationConsts.TOKEN);
		String query = request.getQueryString();
		
		Logger.getLogger(AndroidAppServlet.class.getName()).log(Level.INFO, "--Android GET request registered: " + query);
		
		AppGetRequestProcessor processor;
		try {
			processor = new AppGetRequestProcessor(new InitialContext());
			processor.process(response.getOutputStream(), token, query);
		} catch (Exception e) {
			response.getWriter().println(AppCommunicationConsts.REQUEST_PROCESS_ERROR);
			Logger.getLogger(AndroidAppServlet.class.getName()).log(Level.SEVERE, e.getMessage(), e);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Logger.getLogger(AndroidAppServlet.class.getName()).log(Level.INFO, "--Android POST request registered");
		
		String token = request.getHeader(AppCommunicationConsts.TOKEN);
		
		PostDataRetriever db = new PostDataRetriever();
		String action = db.getTextPart(request, AppCommunicationConsts.ACTION);
		
		AppPostRequestProcessor processor = null;
		try {
			processor = new AppPostRequestProcessor(new InitialContext());
			
			switch (action) {
				case AppCommunicationConsts.GET_TOKEN: processor.getToken(response.getOutputStream(),
						db.getTextPart(request, AppCommunicationConsts.LOGIN),
						db.getTextPart(request, AppCommunicationConsts.PASSWORD));
					break;
				case AppCommunicationConsts.VALIDATE: processor.validateToken(response.getOutputStream(), token);
					break;
				case AppCommunicationConsts.DELETE_PHOTO: processor.deletePhoto(response.getOutputStream(), token, 
						db.getTextPart(request, AppCommunicationConsts.TIME));
					break;
				case AppCommunicationConsts.CHANGE_STATE: processor.changeState(response.getOutputStream(), token,
						db.getTextPart(request, AppCommunicationConsts.STATE));
					break;
				case AppCommunicationConsts.TAKE_PICTURE: processor.takePicture(response.getOutputStream(), token);
					break;
				default: response.getWriter().println(AppCommunicationConsts.REQUEST_FORMAT_ERROR);
					break;
			}
		} catch (NamingException e) {
			response.getWriter().println(AppCommunicationConsts.REQUEST_PROCESS_ERROR);
		}
		finally {
			processor.closeConnection();
		}
	}
}
