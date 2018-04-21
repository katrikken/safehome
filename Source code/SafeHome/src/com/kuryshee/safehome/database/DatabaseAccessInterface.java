package com.kuryshee.safehome.database;

import java.io.IOException;
import java.sql.SQLException;

/**
 * The abstraction of interactions with the database.
 * @author Ekaterina Kurysheva.
 */
public interface DatabaseAccessInterface {
	
	/**
	 * Closes the database connection.
	 * @throws SQLException
	 */
	public void closeConnection() throws SQLException;
	
	/**
	 * Registers the action on Raspberry Pi into database.
	 * @param rpiId
	 * @param time
	 * @param action
	 * @param level
	 * @throws SQLException
	 */
	public void addRpiAction(String rpiId, String time, String action, String level) throws SQLException;
	
	/**
	 * Adds photo to the database.
	 * @param rpiId
	 * @param time
	 * @param name
	 * @param photo
	 * @throws SQLException
	 */
	public void addRpiPhoto(String rpiId, String time, String name, byte[] photo) throws SQLException, IOException;
	
	/**
	 * Registers the user for the given Raspberry Pi.
	 * @param rpiId
	 * @param user
	 * @throws SQLException
	 */
	public void addRpiUserRelation(String rpiId, String user) throws SQLException;
	
	
	/**
	 * Adds user credentials to the database.
	 * @param login
	 * @param password
	 */
	public void addUserCredentials(String login, String password) throws SQLException;
	
	/**
	 * Adds user token for communication authorization with the token.
	 * @param login
	 * @param token
	 */
	public void addUserToken(String login, String token) throws SQLException;
	
	/**
	 * Deletes photo from the database.
	 * @param rpiId
	 * @param time
	 * @throws SQLException
	 */
	public void deleteRpiPhoto(String rpiId, String time) throws SQLException;
	
	/**
	 * Deletes user credentials and all data connected to the user from the database.
	 * @param login
	 */
	public void deleteUserCredentials(String login) throws SQLException;
	
	/**
	 * Returns the time of most recent action on Raspberry Pi registered in the database.
	 * @param rpiId
	 * @return formatted date string
	 * @throws SQLException
	 * @throws IOException when converting the database output.
	 */
	public String getLatestDateOnActions(String rpiId) throws SQLException, IOException;
	
	/**
	 * Returns the time of most recent photo on Raspberry Pi registered in the database.
	 * @param rpiId
	 * @return formatted date String
	 * @throws SQLException
	 */
	public String getLatestDateOnPhotos(String rpiId) throws SQLException;
	
	/**
	 * Gets the photo from the database.
	 * @param rpiId
	 * @param time
	 * @return byte array
	 * @throws SQLException
	 * @throws IOException when converting the database output.
	 */
	public byte[] getPhoto(String rpiId, String time) throws SQLException, IOException;
	
	/**
	 * Gets the list of time stamps of most recent photos before the given date.
	 * @param rpiId
	 * @param time
	 * @param numberOfDates
	 * @return byte array of JSON string
	 * @throws SQLException
	 * @throws IOException when converting the database output.
	 */
	public byte[] getPhotoTimesBefore(String rpiId, String time, int numberOfDates) throws SQLException, IOException;
	
	/**
	 * Gets the list of time stamps of most recent actions before the given date.
	 * @param rpiId
	 * @param time
	 * @param numberOfActions
	 * @return byte array of JSON string
	 * @throws SQLException
	 * @throws IOException when converting the database output.
	 */
	public byte[] getRpiActionsBefore(String rpiId, String time, int numberOfActions) throws SQLException, IOException;
	
	/**
	 * Retrieves information about user and Rpi relation.
	 * @param user
	 * @return Rpi ID for the given user
	 * @throws SQLException
	 */
	public String getRpiByUser(String user) throws SQLException;
	
	/**
	 * Gets the user, which is identified be the token.
	 * @param token
	 * @return user name or empty string, if the user does not exist.
	 */
	public String getUserByToken(String token) throws SQLException;
	
	
	/**
	 * Validates user's login and password.
	 * @param login
	 * @param password
	 * @return true, if data are valid, false otherwise.
	 */
	public boolean validateUserCredentials(String login, String password) throws SQLException;
	
	/**
	 * Validates user's token for communication.
	 * @param login
	 * @param token
	 * @return true, if token is valid, false otherwise.
	 */
	public boolean validateUserToken(String login, String token) throws SQLException;
	
	/**
	 * Validates that such Raspberry Pi is registered.
	 * @param rpiId
	 * @return true, if such id exists in the database and device is active.
	 */
	public boolean verifyRpiRegistration(String rpiId) throws SQLException;
	
}
