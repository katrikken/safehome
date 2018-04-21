package com.kuryshee.safehome.server;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletOutputStream;
import javax.sql.DataSource;

import com.kuryshee.safehome.appcommunicationconsts.AppCommunicationConsts;
import com.kuryshee.safehome.database.DatabaseAccessInterface;
import com.kuryshee.safehome.httprequestsender.AnswerConstants;

/**
 * Class implements handling predefined POST requests from Android App.
 * @author Ekaterina Kurysheva.
 *
 */
public class AppPostRequestProcessor{
	
	private DatabaseAccessInterface database;
	private String user;
	
	/**
	 * Public constructor.
	 * @param context is the environment context.
	 */
	public AppPostRequestProcessor(InitialContext context){
		try {
			Context envContext  = (Context) context.lookup("java:/comp/env");
			database = new DatabaseAccessImpl((DataSource) envContext.lookup("jdbc/xe"));
		} catch (Exception ex) {
			Logger.getLogger(AppPostRequestProcessor.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
		}
	}
	
	/**
	 * Gets the user from the database.
	 * @param token
	 * @return true, if the user is found, false otherwise.
	 * @throws SQLException 
	 */
	private boolean getUserByToken(String token) throws SQLException {
		user = database.getUserByToken(token);
		
		if(user.length() > 0)
			return true;
		
		return false;
	}
	
	/**
	 * Simple token creation.
	 * @param login
	 * @param password
	 * @return token for user authorization.
	 */
	private String createToken(String login, String password) {
		
		return login + System.currentTimeMillis();
	}
	
	/**
	 * Closes the connection to the database.
	 */
	public void closeConnection() {
		try {
			database.closeConnection();
		}
		catch(SQLException ex) {
			Logger.getLogger(AppPostRequestProcessor.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
		}
	}

	/**
	 * Creates a token for user authorization upon logging in.
	 * Writes the token to the output stream.
	 * @param output is the HTTP response output stream.
	 * @param login
	 * @param password
	 */
	public void getToken(ServletOutputStream output, String login, String password) {	
		try {
		
			if(database.validateUserCredentials(login, password)) {
				String token = createToken(login, password);
				database.addUserToken(login, token);
				output.println(token);
			}
			else {
				output.println(AppCommunicationConsts.INVALID_USER_ERROR);
			}
		}
		catch(Exception e) {
			Logger.getLogger(AppPostRequestProcessor.class.getName()).log(Level.SEVERE, e.getMessage(), e);
			try {
				output.println(AppCommunicationConsts.REQUEST_PROCESS_ERROR);
			}
			catch(IOException ex) {
				Logger.getLogger(AppPostRequestProcessor.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
			}
		}
	}
	
	/**
	 * Validates that the token is valid. 
	 * Writes {@link AppCommunicationConsts#TRUE} or {@link AppCommunicationConsts#FALSE} in the output.
	 * @param output is the HTTP response output stream
	 * @param token
	 */
	public void validateToken(ServletOutputStream output, String token) {
		try {
			if (getUserByToken(token)) output.println(AppCommunicationConsts.TRUE); 
			else output.println(AppCommunicationConsts.FALSE);
		}
		catch(Exception e) {
			Logger.getLogger(AppPostRequestProcessor.class.getName()).log(Level.SEVERE, e.getMessage(), e);
			try {
				output.println(AppCommunicationConsts.REQUEST_PROCESS_ERROR);
			}
			catch(IOException ex) {
				Logger.getLogger(AppPostRequestProcessor.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
			}
		}
	}
	
	/**
	 * Deletes the photo from the database
	 * @param output
	 * @param token
	 * @param photoId
	 */
	public void deletePhoto(ServletOutputStream output, String token, String photoId) {
		try {
			if (getUserByToken(token)) { //user is identified
				String rpiId = database.getRpiByUser(user);
				if(rpiId != null && rpiId.length() > 0) { //user is registered for a certain Raspberry Pi
					database.deleteRpiPhoto(rpiId, photoId);
					output.println(AnswerConstants.OK_ANSWER);
				}
				else {
					output.println(AppCommunicationConsts.INVALID_USER_ERROR);
				}
			}
			else {
				output.println(AppCommunicationConsts.INVALID_USER_ERROR);
			}
		}
		catch(Exception e) {
			Logger.getLogger(AppGetRequestProcessor.class.getName()).log(Level.SEVERE, e.getMessage(), e);
			
			try {
				output.println(AppCommunicationConsts.REQUEST_PROCESS_ERROR);
			}
			catch(IOException ex) {
				Logger.getLogger(AppGetRequestProcessor.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
			}
		}
	}
	
	/**
	 * Not supported in this version.
	 * Communicates with Raspberry Pi to change state.
	 * @param output
	 * @param token
	 * @param state
	 */
	public void changeState(ServletOutputStream output, String token, String state) {
		try {
			output.println(AppCommunicationConsts.REQUEST_PROCESS_ERROR);
		}
		catch(IOException ex) {
			Logger.getLogger(AppGetRequestProcessor.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
		}
	}
	
	/**
	 * Not supported in this version.
	 * Communicate to Raspberry Pi to take picture.
	 * @param output
	 * @param token
	 */
	public void takePicture(ServletOutputStream output, String token) {
		try {
			output.println(AppCommunicationConsts.REQUEST_PROCESS_ERROR);
		}
		catch(IOException ex) {
			Logger.getLogger(AppGetRequestProcessor.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
		}
	}
}
