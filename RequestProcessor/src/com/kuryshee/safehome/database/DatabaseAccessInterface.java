package com.kuryshee.safehome.database;

import java.sql.ResultSet;
import java.util.Map;

/**
 * This interface is an abstraction of interactions with the database.
 * @author Ekaterina Kurysheva.
 *
 */
public interface DatabaseAccessInterface {
	
	/**
	 * This method performs connection to the database.
	 * @param url is the link to the database.
	 * @param properties is a map where keys and values correspond to tag/value pairs for the connection.
	 * @return true if connection succeeded.
	 */
	public Boolean connect(String url, Map<String,String> properties);
	
	/**
	 * This method performs SQL INSERT command.
	 * @param table is a name of the table.
	 * @param values is a map, where keys are names of table rows and values are values to be inserted.
	 * @return true if insertion was successful.
	 */
	public Boolean insert(String table, Map<String,String> values);
	
	/**
	 * This method performs SQL SELECT command.
	 * @param query is a full command.
	 * @return {@link ResultSet}.
	 */
	public ResultSet select(String query);
	
	/**
	 * This method closes the connection to the database.
	 * @return true if the connection was successfully closed.
	 */
	public Boolean close();
}
