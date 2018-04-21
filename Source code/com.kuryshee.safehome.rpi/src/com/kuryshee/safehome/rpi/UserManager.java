package com.kuryshee.safehome.rpi;

import com.kuryshee.safehome.rpicommunicationconsts.RpiCommunicationConsts;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 * Class implements manager of shared user configuration file.
 * 
 * @author Ekaterina Kurysheva
 */
public class UserManager {
    
    private File config;
    private List<UserBean> users;
    private final Logger LOGGER = Logger.getLogger(UserManager.class.getName());
    
    /**
     * Constructor.
     * @param config is a shared configuration file.
     */
    public UserManager(File config){
        this.config = config;
        users = readUsers();
    }
    
    /**
     * Reads users to the list of {@link UserBean} instances.
     * @return list of {@link UserBean} instances.
     */
    private List<UserBean> readUsers(){
        List<UserBean> list = new ArrayList<>();
        
        try (InputStream is = new FileInputStream(config);
        	JsonReader reader = Json.createReader(is)){
            
            JsonArray array = reader.readArray();
            
            // read string data
            for( int i = 0; i < array.size(); i++){
	        JsonObject object = array.getJsonObject(i);
                UserBean user = new UserBean();
                user.setName(object.getString(RpiCommunicationConsts.NAME));
                user.setTag(object.getString(RpiCommunicationConsts.TOKEN));
                user.setPassword(object.getString(RpiCommunicationConsts.PASSWORD));
                list.add(user);
	    }
             
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
	} catch (IOException e) {
		LOGGER.log(Level.SEVERE, e.getMessage(), e);
	} 
        finally{
            LOGGER.log(Level.INFO, "Read users: " + list.size());
        }
        
        return list;
    }
    
    /**
     * Retrieves user tags and names from the list of known users.
     * 
     * @return map of users where key is RFID tag and value is the name of user associated with it.
     */
    public Map<String, String> getTagNameMap(){
        Map<String, String> map = new HashMap<>();
        
        for(UserBean user: users){
            map.put(user.getTag(), user.getName());
        }
        
        return map;
    }
    
    /**
     * Updates list of known users.
     * @return updated map of users where key is RFID tag and value is the name of user associated with it.
     */
    public Map<String, String> updateUsers(){
        List<UserBean> newlist = readUsers();
        
        Collections.sort(users, new UserComparator());
	Collections.sort(newlist, new UserComparator());
        users = mergeLists(users, newlist);

        return getTagNameMap();
    }
    
    /**
     * Merges two sorted lists of users and reports changes via {@link Reporter}.
     * @param oldlist
     * @param newlist
     * @return merged list.
     */
    private List<UserBean> mergeLists(List<UserBean> oldlist, List<UserBean> newlist){
        List<UserBean> list = new ArrayList<>();
        
        if(newlist.isEmpty()) { //all users are deleted
            for(int i = 0; i < oldlist.size(); i++){
                Reporter.reportDeleteUser(oldlist.get(i));
            }
        	 
            return list;
        }
        
        int index = 0;
        
        for(int i = 0; i < oldlist.size(); i++){
            UserBean user = oldlist.get(i);
            boolean found = false;
            
            for(int j = index; j < newlist.size(); j++){
                UserBean newuser = newlist.get(j);
                if(newuser.getTag().compareTo(user.getTag()) < 0){
                    list.add(newuser);
                    Reporter.reportUserChange(newuser);
                }
                else if(newuser.getTag().compareTo(user.getTag()) == 0){ 
                	found = true;
                	
                    list.add(newuser);
                    if(user.getName().equals(newuser.getName())){ //check change of password
                        if(!user.getPassword().equals(newuser.getPassword())){
                            Reporter.reportUserChange(newuser);
                        }
                    }
                    else{ //tag has been registered to different user.
                        Reporter.reportDeleteUser(user);
                        Reporter.reportUserChange(newuser);
                    }
                    
                    index = j + 1; //start next iteration from next position in newlist.
                    break; //pick next old item.
                }
                else if(newuser.getTag().compareTo(user.getTag()) > 0){ //old user tag was deleted
                	found = true;
                    Reporter.reportDeleteUser(user);
                    index = j; //start next iteration from this point
                    break;
                }
            }
            
            if(!found) {
            	 Reporter.reportDeleteUser(user);
            }
        }
        
        if(index < newlist.size()){
            for(int j = index; j < newlist.size(); j++){
                list.add(newlist.get(j));
                Reporter.reportUserChange(newlist.get(j));
            }
        }
            
        return list;
    }
}
