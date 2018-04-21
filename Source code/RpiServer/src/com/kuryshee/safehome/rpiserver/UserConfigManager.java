package com.kuryshee.safehome.rpiserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;

import com.kuryshee.safehome.rpicommunicationconsts.RpiCommunicationConsts;

/**
 * Reader and writer for the user configuration file in JSON format.
 * 
 * @author Ekaterina Kurysheva
 */
public class UserConfigManager {
	
	private File config;
	
	/**
	 * Constructor
	 * @param config is {@link File} to get configured data from.
	 */
	public UserConfigManager(File config) {
		this.config = config;
	}
	
	/**
	 * Reads users from file with JSON array.
	 * @return list of {@link UserBean}.
	 */
	public List<UserBean> readUsersToUserBeans() {
		List<UserBean> list = new ArrayList<>();
		
        try (InputStream is = new FileInputStream(config);
        		JsonReader reader = Json.createReader(is)){
            
            JsonArray array = reader.readArray();
            
            // read string data
            for( int i = 0; i < array.size(); i++){
	            JsonObject object = array.getJsonObject(i);
	            UserBean bean = new UserBean();
	            bean.setName(object.getString(RpiCommunicationConsts.NAME));
	            bean.setToken(object.getString(RpiCommunicationConsts.TOKEN));
	            bean.setPassword(object.getString(RpiCommunicationConsts.PASSWORD));
	            
	            list.add(bean);
	        }
             
        } catch (FileNotFoundException e) {
        	Logger.getLogger(UserConfigManager.class.getName()).log(Level.SEVERE, e.getMessage(), e);
		} catch (IOException e) {
			Logger.getLogger(UserConfigManager.class.getName()).log(Level.SEVERE, e.getMessage(), e);
		} 

		return list;
	}
	
	/**
	 * Writes list of {@link UserBean} to configuration file.
	 * @param list of users to write.
	 */
	public void writeBeansToJson(List<UserBean> list) {
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		
		for(UserBean bean: list) {
			arrayBuilder.add(Json.createObjectBuilder()
					.add(RpiCommunicationConsts.NAME, bean.getName())
					.add(RpiCommunicationConsts.TOKEN, bean.getToken())
					.add(RpiCommunicationConsts.PASSWORD, bean.getPassword()));
		}
		
		JsonArray array = arrayBuilder.build();

		try(OutputStream os = new FileOutputStream(config, false); //overwrite the file
				JsonWriter writer = Json.createWriter(os)){
			
			writer.writeArray(array);
			
		} catch (FileNotFoundException e) {
			Logger.getLogger(UserConfigManager.class.getName()).log(Level.SEVERE, e.getMessage(), e);
		} catch (IOException e) {
			Logger.getLogger(UserConfigManager.class.getName()).log(Level.SEVERE, e.getMessage(), e);
		}
	}
}
