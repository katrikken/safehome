package com.kuryshee.safehome.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;

import com.kuryshee.safehome.appcommunicationconsts.AppCommunicationConsts;
import com.kuryshee.safehome.database.DatabaseAccessInterface;


/**
 * This class implements the mock database access.
 * It writes information to the files instead of writing to the actual database.
 * @author Ekaterina Kurysheva
 */
public class DatabaseAccessImpl implements DatabaseAccessInterface{
	
	DataSource ds;
	Connection conn;
	
	public DatabaseAccessImpl(DataSource ds) throws SQLException {

		this.ds = ds;

		conn = ds.getConnection();
		conn.setAutoCommit(false);
	}
	
	/**
	 * @see DatabaseAccessInterface#addUserCredentials(String login, String password)
	 */
	@Override
	public void addUserCredentials(String login, String password) throws SQLException {
		CallableStatement callStmt = null;
		try {
			callStmt = conn.prepareCall("{call ADD_USER_CREDENTIALS(?, ?)}");
	        callStmt.setString(1, login);
	        callStmt.setString(2, password);
	        callStmt.execute();
	        conn.commit();
        } 
        finally {
        	try {
	        	callStmt.close();
        	}
        	catch(Exception e) {
        		Logger.getLogger(DatabaseAccessImpl.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        	}
        }
	}

	/**
	 * @see DatabaseAccessInterface#addUserToken(String login, String token)
	 */
	@Override
	public void addUserToken(String login, String token) throws SQLException {
		CallableStatement callStmt = null;
		try {
			callStmt = conn.prepareCall("{call ADD_USER_TOKEN(?, ?)}");
	        callStmt.setString(1, login);
	        callStmt.setString(2, token);
	        callStmt.execute();
	        conn.commit();
        } 
        finally {
        	try {
	        	if(callStmt != null ) callStmt.close();
        	}
        	catch(Exception e) {
        		Logger.getLogger(DatabaseAccessImpl.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        	}
        }
	}

	/**
	 * @see DatabaseAccessInterface#deleteUserCredentials(String login)
	 */
	@Override
	public void deleteUserCredentials(String login) throws SQLException {
		CallableStatement callStmt = null;
		try {
			callStmt = conn.prepareCall("{call DELETE_USER_CREDENTIALS(?)}");
	        callStmt.setString(1, login);
	        callStmt.execute();
	        conn.commit();
        } 
        finally {
        	try {
	        	callStmt.close();
        	}
        	catch(Exception e) {
        		Logger.getLogger(DatabaseAccessImpl.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        	}
        }
	}
	
	/**
	 * @see DatabaseAccessInterface#getUserByToken(String token)
	 */
	@Override
	public String getUserByToken(String token) throws SQLException {
		CallableStatement callStmt = null;
		String user;
		try {
			callStmt = conn.prepareCall("{? = call GET_USER_BY_TOKEN(?)}");
			callStmt.registerOutParameter(1, java.sql.Types.VARCHAR);
	        callStmt.setString(2, token);
	        callStmt.execute();
	        
	        user = callStmt.getString(1);
        } 
        finally {
        	try {
	        	callStmt.close();
        	}
        	catch(Exception e) {
        		Logger.getLogger(DatabaseAccessImpl.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        	}
        }

		if(user != null && user.length() > 0) {
			return user;
		}
		
		return "";
	}

	/**
	 * @see DatabaseAccessInterface#validateUserCredentials(String login, String password)
	 */
	@Override
	public boolean validateUserCredentials(String login, String password) throws SQLException {
		
		CallableStatement callStmt = null;
		int user = -1;
		try {
			callStmt = conn.prepareCall("{? = call VALIDATE_USER_CREDENTIALS(?, ?)}");
			callStmt.registerOutParameter(1, java.sql.Types.INTEGER);
	        callStmt.setString(2, login);
	        callStmt.setString(3, password);
	        callStmt.execute();
	        
	        user = callStmt.getInt(1);
        } 
        finally {
        	try {
	        	callStmt.close();
        	}
        	catch(Exception e) {
        		Logger.getLogger(DatabaseAccessImpl.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        	}
        }
		
		if (user == 1) return true;
		
		else return false;
	}

	/**
	 * @see DatabaseAccessInterface#validateUserToken(String login, String token)
	 */
	@Override
	public boolean validateUserToken(String login, String token) throws SQLException {
		CallableStatement callStmt = null;
		int user = -1;
		try {
			callStmt = conn.prepareCall("{? = call VALIDATE_USER_TOKEN(?,?)}");
			callStmt.registerOutParameter(1, java.sql.Types.INTEGER);
	        callStmt.setString(2, login);
	        callStmt.setString(3, token);
	        callStmt.execute();
	        
	        user = callStmt.getInt(1);
        } 
        finally {
        	try {
	        	callStmt.close();
        	}
        	catch(Exception e) {
        		Logger.getLogger(DatabaseAccessImpl.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        	}
        }
		
		if(user != -1) {
			if (user == 1) return true;
		}
		
		return false;
	}
	
	/**
	 * @see DatabaseAccessInterface#getRpiByUser(String user)
	 */
	@Override
	public String getRpiByUser(String user) throws SQLException{
		CallableStatement callStmt = null;
		String rpiId = null;
		try {
			callStmt = conn.prepareCall("{? = call GET_RPI_BY_USER(?)}");
			callStmt.registerOutParameter(1, java.sql.Types.VARCHAR);
	        callStmt.setString(2, user);
	        callStmt.execute();
	        
	        rpiId = callStmt.getString(1);
        } 
        finally {
        	try {
	        	callStmt.close();
        	}
        	catch(Exception e) {
        		Logger.getLogger(DatabaseAccessImpl.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        	}
        }
		
		return rpiId;
	}
	
	/**
	 * @see DatabaseAccessInterface#getLatestDateOnActions(String rpiId)
	 */
	@Override
	public String getLatestDateOnActions(String rpiId) throws SQLException, IOException {
		CallableStatement callStmt = null;
		Timestamp time = null;
		try {
			callStmt = conn.prepareCall("{? = call GET_LATEST_DATE_ON_ACTIONS(?)}");
			callStmt.registerOutParameter(1, java.sql.Types.TIMESTAMP);
	        callStmt.setString(2, rpiId);
	        callStmt.execute();
	        
	        time = callStmt.getTimestamp(1);
        } 
        finally {
        	try {
	        	callStmt.close();
        	}
        	catch(Exception e) {
        		Logger.getLogger(DatabaseAccessImpl.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        	}
        }
		
		if (time != null) {
			return time.getTime() + "";
		}
		
		else return "";
	}

	/**
	 * @see DatabaseAccessInterface#addRpiAction(String rpiId, String time, String action, String level)
	 */
	@Override
	public void addRpiAction(String rpiId, String time, String action, String level) throws SQLException {
		CallableStatement callStmt = null;
		try {
			Timestamp timestamp = new Timestamp(Long.parseLong(time));
			callStmt = conn.prepareCall("{call ADD_RPI_ACTION(?, ?, ?, ?)}");
	        callStmt.setString(1, rpiId);
	        callStmt.setTimestamp(2, timestamp);
	        callStmt.setString(3, action);
	        callStmt.setString(4, level);
	        callStmt.execute();
	        conn.commit();
        } 
        finally {
        	try {
	        	callStmt.close();
        	}
        	catch(Exception e) {
        		Logger.getLogger(DatabaseAccessImpl.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        	}
        }
	}

	/**
	 * @see DatabaseAccessInterface#addRpiPhoto(String rpiId, String time, String name, byte[] photo)
	 */
	@Override
	public void addRpiPhoto(String rpiId, String time, String name, byte[] photo) throws SQLException, IOException {
		CallableStatement callStmt = null;
		try(ByteArrayInputStream is = new ByteArrayInputStream(photo)) {
			Timestamp timestamp = new Timestamp(Long.parseLong(time));
			callStmt = conn.prepareCall("{call ADD_RPI_PHOTO(?, ?, ?, ?)}");
			callStmt.setString(1, rpiId); 
			callStmt.setTimestamp(2, timestamp);
			callStmt.setString(3, name);
			callStmt.setBinaryStream(4, is, is.available());  
			callStmt.execute();
	        conn.commit();  
        } 
        finally {
        	try {
        		callStmt.close();
        	}
        	catch(Exception e) {
        		Logger.getLogger(DatabaseAccessImpl.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        	}
        }
		
	}

	/**
	 * @see DatabaseAccessInterface#addRpiUserRelation(String rpiId, String user)
	 */
	@Override
	public void addRpiUserRelation(String rpiId, String user) throws SQLException {
		CallableStatement callStmt = null;
		try {
			callStmt = conn.prepareCall("{call ADD_RPI_USER_RELATION(?, ?)}");
	        callStmt.setString(1, user);
	        callStmt.setString(2, rpiId);
	        
	        callStmt.execute();
	        conn.commit();
        } 
        finally {
        	try {
	        	callStmt.close();
        	}
        	catch(Exception e) {
        		Logger.getLogger(DatabaseAccessImpl.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        	}
        }
	}

	/**
	 * @see DatabaseAccessInterface#deleteRpiPhoto(String rpiId, String time)
	 */
	@Override
	public void deleteRpiPhoto(String rpiId, String time) throws SQLException {
		CallableStatement callStmt = null;
		try {
			Timestamp timestamp = new Timestamp(Long.parseLong(time));
			callStmt = conn.prepareCall("{call DELETE_RPI_PHOTO(?, ?)}");
	        callStmt.setString(1, rpiId);
	        callStmt.setTimestamp(2, timestamp);
	        
	        callStmt.execute();
	        conn.commit();
        } 
        finally {
        	try {
	        	callStmt.close();
        	}
        	catch(Exception e) {
        		Logger.getLogger(DatabaseAccessImpl.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        	}
        }
	}

	/**
	 * @see DatabaseAccessInterface#getLatestDateOnPhotos(String rpiId)
	 */
	@Override
	public String getLatestDateOnPhotos(String rpiId) throws SQLException {
		CallableStatement callStmt = null;
		Timestamp time = null;
		try {
			callStmt = conn.prepareCall("{? = call GET_LATEST_DATE_ON_PHOTOS(?)}");
			callStmt.registerOutParameter(1, java.sql.Types.TIMESTAMP);
	        callStmt.setString(2, rpiId);
	        callStmt.execute();
	        
	        time = callStmt.getTimestamp(1);
        } 
        finally {
        	try {
	        	callStmt.close();
        	}
        	catch(Exception e) {
        		Logger.getLogger(DatabaseAccessImpl.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        	}
        }
		
		return time.getTime() + "";
	}

	/**
	 * @see DatabaseAccessInterface#getPhoto(String rpiId, String time)
	 */
	@Override
	public byte[] getPhoto(String rpiId, String time) throws SQLException, IOException {
		CallableStatement callStmt = null;
		byte[] result = null;
		try {
			Timestamp timestamp = new Timestamp(Long.parseLong(time));
			callStmt = conn.prepareCall("{? = call GET_PHOTO(?, ?)}");
			callStmt.registerOutParameter(1, java.sql.Types.BLOB);
			callStmt.setString(2, rpiId); 
			callStmt.setTimestamp(3, timestamp);
			callStmt.execute();
			
			result = IOUtils.toByteArray(callStmt.getBlob(1).getBinaryStream());
        } 
        finally {
        	try {
        		callStmt.close();
        	}
        	catch(Exception e) {
        		Logger.getLogger(DatabaseAccessImpl.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        	}
        }
		
		return result;
	}

	/**
	 * @see DatabaseAccessInterface#getPhotoTimesBefore(String rpiId, String time, int numberOfDates)
	 */
	@Override
	public byte[] getPhotoTimesBefore(String rpiId, String time, int numberOfDates) throws SQLException, IOException {
		CallableStatement callStmt = null;
		byte[] result = null;
		try {
			Timestamp timestamp = new Timestamp(Long.parseLong(time));
			callStmt = conn.prepareCall("{? = call GET_PHOTO_TIMES_BEFORE(?, ?, ?)}");
			callStmt.registerOutParameter(1, java.sql.Types.CLOB);
			callStmt.setString(2, rpiId); 
			callStmt.setTimestamp(3, timestamp);
			callStmt.setInt(4, numberOfDates); 
			callStmt.execute();
			
			result = IOUtils.toByteArray(callStmt.getClob(1).getCharacterStream(), AppCommunicationConsts.UTF_CHARSET);
        } 
        finally {
        	try {
        		callStmt.close();
        	}
        	catch(Exception e) {
        		Logger.getLogger(DatabaseAccessImpl.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        	}
        }
		
		return result;
	}

	/**
	 * @see DatabaseAccessInterface#getRpiActionsBefore(String rpiId, String time, int numberOfActions)
	 */
	@Override
	public byte[] getRpiActionsBefore(String rpiId, String time, int numberOfActions) throws SQLException, IOException {
		CallableStatement callStmt = null;
		byte[] result = null;
		try {
			Timestamp timestamp = new Timestamp(Long.parseLong(time));
			callStmt = conn.prepareCall("{? = call GET_RPI_ACTIONS_BEFORE(?, ?, ?)}");
			callStmt.registerOutParameter(1, java.sql.Types.CLOB);
			callStmt.setString(2, rpiId); 
			callStmt.setTimestamp(3, timestamp);
			callStmt.setInt(4, numberOfActions); 
			callStmt.execute();
			
			result = IOUtils.toByteArray(callStmt.getClob(1).getCharacterStream(), AppCommunicationConsts.UTF_CHARSET);
        } 
        finally {
        	try {
        		callStmt.close();
        	}
        	catch(Exception e) {
        		Logger.getLogger(DatabaseAccessImpl.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        	}
        }
		
		return result;
	}

	/**
	 * @see DatabaseAccessInterface#closeConnection()
	 */
	@Override
	public void closeConnection() throws SQLException {
		conn.close();
	}

	/**
	 * @see DatabaseAccessInterface#verifyRpiRegistration(String rpiId)
	 */
	@Override
	public boolean verifyRpiRegistration(String rpiId) throws SQLException {
		CallableStatement callStmt = null;
		int rpi = -1;
		try {
			callStmt = conn.prepareCall("{? = call VERIFY_RPI_REGISTERED(?)}");
			callStmt.registerOutParameter(1, java.sql.Types.INTEGER);
	        callStmt.setString(2, rpiId);
	        callStmt.execute();
	        
	        rpi = callStmt.getInt(1);
        } 
        finally {
        	try {
	        	callStmt.close();
        	}
        	catch(Exception e) {
        		Logger.getLogger(DatabaseAccessImpl.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        	}
        }
		
		if (rpi == 1) return true;
		
		else return false;
	}
}
