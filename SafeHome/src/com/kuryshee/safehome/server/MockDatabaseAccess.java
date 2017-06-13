package com.kuryshee.safehome.server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.Map;
import java.util.logging.Logger;

import com.kuryshee.safehome.database.DatabaseAccessInterface;

/**
 * This class implements the mock database access.
 * It writes information to the files instead of writing to the actual database.
 * @author Ekaterina Kurysheva
 */
public class MockDatabaseAccess implements DatabaseAccessInterface{
	
	private static String DB_PATH = "Database\\";

	@Override
	public Boolean connect(String url, Map<String, String> properties) {
		return true;
	}

	@Override
	public Boolean insert(String table, Map<String, String> values) {
		String id = values.get(SafeHomeServer.RPI_PARAM);
		String command = values.get(SafeHomeServer.COMMAND_PARAM);
		String time = values.get(SafeHomeServer.TIME_PARAM);
		if (id != null){
			try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(DB_PATH + id, true)))){
				if(command != null){
					if(time != null){
						out.print(time.trim() + " ");
					}
					
					if(command.equals(SafeHomeServer.REQ_MOTIONDETECTED)){
					    out.println("Motion was detected.");
					    return true;
					}
					else if (command.equals(SafeHomeServer.REQ_PHOTOTAKEN)){
						out.println("Photo was made.");
						return true;
					}
					else if (command.equals(SafeHomeServer.REQ_RFIDSWITCH)){
						String user = values.get(SafeHomeServer.RFID_PARAM);
						if(user != null){
							out.print(user + " used the token. ");
						}
						out.println("State was switched.");
						return true;
					}
					else if (command.equals(SafeHomeServer.UPLOAD_PHOTO)){
						String path = values.get(SafeHomeServer.PHOTO_PARAM);
						if(path != null){
							out.print(path + " ");
						}
						out.println("Photo was saved.");
						return true;
					}
				}
			} catch (IOException e) {
				Logger.getLogger("Mock Database").severe(e.getMessage());	
			}
		}
		return false;
	}
	
	@Override
	public ResultSet select(String query) {
		return null;
	}

	@Override
	public Boolean close() {
		return true;
	}
}
