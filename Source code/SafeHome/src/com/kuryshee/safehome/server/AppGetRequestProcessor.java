package com.kuryshee.safehome.server;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletOutputStream;

import com.kuryshee.safehome.appcommunicationconsts.AppCommunicationConsts;
import com.kuryshee.safehome.database.DatabaseAccessInterface;
import com.kuryshee.safehome.requestprocessorinterface.RequestProcessor;

import javax.sql.DataSource;

import java.util.logging.Logger;

/**
 * Class implements processing of predefined GET requests from Android application.
 * @author Ekaterina Kurysheva
 *
 */
public class AppGetRequestProcessor implements RequestProcessor{
	
	private DatabaseAccessInterface database;
	
	private String user;
	private String rpiId;
	
	/**
	 * Public constructor.
	 * @param context of the server environment.
	 */
	public AppGetRequestProcessor(InitialContext context) {
		try {
			Context envContext  = (Context) context.lookup("java:/comp/env");
			database = new DatabaseAccessImpl((DataSource)  envContext.lookup("jdbc/xe"));
		} catch (Exception ex) {
			Logger.getLogger(AppGetRequestProcessor.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
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
	 * Finds Raspberry Pi associated with user and gets data from database.
	 * @param output
	 * @param time of events before which the data are requested with {@link DatabaseAccessImpl#getRpiActionsBefore}.
	 */
	private void getRpiActions(ServletOutputStream output, String time) {
		try {
			rpiId = database.getRpiByUser(user);
			if(rpiId != null && rpiId.length() > 0) {
				output.write(database.getRpiActionsBefore(rpiId, time, 10));
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
	 * Finds Raspberry Pi associated with user and gets data from database with {@link DatabaseAccessImpl#getLatestDateOnActions}.
	 * @param output
	 */
	private void getLatestActionTime(ServletOutputStream output) {
		try {
			rpiId = database.getRpiByUser(user);
			if(rpiId != null && rpiId.length() > 0) {
				output.println(database.getLatestDateOnActions(rpiId));
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
	 * Finds Raspberry Pi associated with user and gets data from database with {@link DatabaseAccessImpl#getLatestDateOnPhotos}.
	 * @param output
	 */
	private void getLatestPhotoTime(ServletOutputStream output) {
		try {
			rpiId = database.getRpiByUser(user);
			if(rpiId != null && rpiId.length() > 0) {
				output.println(database.getLatestDateOnPhotos(rpiId));
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
	 * Finds Raspberry Pi associated with user and gets data from database.
	 * @param output
	 * @param time of events before which the data are requested with {@link DatabaseAccessImpl#getPhotoTimesBefore}.
	 */
	private void getPhotoIdentifiers(ServletOutputStream output, String time) {
		try {
			rpiId = database.getRpiByUser(user);
			if(rpiId != null && rpiId.length() > 0) {
				output.write(database.getPhotoTimesBefore(rpiId, time, 10));
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
	 * Finds Raspberry Pi associated with user and gets data from database  with {@link DatabaseAccessImpl#getPhoto}.
	 * @param output
	 * @param time as identifier of the photo.
	 */
	private void getPhoto(ServletOutputStream output, String time) {
		try {
			rpiId = database.getRpiByUser(user);
			if(rpiId != null && rpiId.length() > 0) {
				output.write(database.getPhoto(rpiId, time));
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
	 * @param output
	 */
	private void getRpiState(ServletOutputStream output) {
		try {
			output.println(AppCommunicationConsts.REQUEST_PROCESS_ERROR);
		}
		catch(IOException ex) {
			Logger.getLogger(AppGetRequestProcessor.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
		}
	}
	
	/**
	 * Closes the database connection.
	 */
	private void closeConnection() {
		try {
			database.closeConnection();
		}
		catch(SQLException ex) {
			Logger.getLogger(AppGetRequestProcessor.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
		}
	}
	
	/**
	 * Processes incoming requests.
	 */
	@Override
	public void process(ServletOutputStream output, String... parameters) {
		try {
			if(parameters[1] != null) { //expecting query here
				Map<String, String> params = parseQuery(parameters[1]); //getting query parameters
				
				String action = params.get(AppCommunicationConsts.ACTION);
				String token = parameters[0];
				
				if(action != null && action.equals(AppCommunicationConsts.PING)) {
					output.println(AppCommunicationConsts.PONG);
				}
				else if (action != null){
					if(token != null && getUserByToken(token)) { //if token is passed and user is identified
						switch(action) {
							case AppCommunicationConsts.GET_LATEST_ACTION_TIME: getLatestActionTime(output);
								break; 
							case AppCommunicationConsts.GET_ACTIONS: getRpiActions(output, params.get(AppCommunicationConsts.TIME));
								break;
							case AppCommunicationConsts.GET_LATEST_PHOTO_TIME: getLatestPhotoTime(output);
								break;
							case AppCommunicationConsts.GET_PHOTO_IDS: getPhotoIdentifiers(output, params.get(AppCommunicationConsts.TIME));
								break;
							case AppCommunicationConsts.GET_PHOTO: getPhoto(output, params.get(AppCommunicationConsts.TIME));
								break;
							case AppCommunicationConsts.GET_RPI_STATE: getRpiState(output);
								break;
							default: output.println(AppCommunicationConsts.REQUEST_FORMAT_ERROR);
								break;
						}
					}
					else {
						output.println(AppCommunicationConsts.INVALID_USER_ERROR);
					}
				}
				else {
					output.println(AppCommunicationConsts.REQUEST_FORMAT_ERROR);
				}
			}
			else {
				output.println(AppCommunicationConsts.REQUEST_FORMAT_ERROR);
			}
		}
		catch(Exception e) {
			Logger.getLogger(AppGetRequestProcessor.class.getName()).log(Level.SEVERE, e.getMessage(), e);
			try {
				output.println(AppCommunicationConsts.ERROR);
			}
			catch(IOException ex) {
				Logger.getLogger(AppPostRequestProcessor.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
			}
		}
		finally {
			closeConnection();
		}
	}
}
