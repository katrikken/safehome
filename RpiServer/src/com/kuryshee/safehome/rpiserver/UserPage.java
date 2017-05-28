package com.kuryshee.safehome.rpiserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

@ManagedBean(name="userPage")
@RequestScoped
public class UserPage implements Serializable {
	//private String USERCONFIG = "/home/pi/NetBeansProjects/com.kuryshee.safehome.rpi/keys.txt";
	private String USERCONFIG = "keys.txt";
	private String KEY = "key ";
	
	private List<UserBean> userBeans = new ArrayList<>();

	public List<UserBean> getUsers(){
		setUserBeans();
		return userBeans;
	}
	
	/**
	 * This method reads configuration file and creates User Beans, which are later available to the user page.
	 */
	private void setUserBeans(){
		userBeans = new ArrayList<>();
		try(BufferedReader br = new BufferedReader(new FileReader(USERCONFIG))){
			String conf;
			
			while( (conf = br.readLine()) != null){
				String params[] = conf.substring(KEY.length()).split("-");
				UserBean bean = new UserBean();
				bean.setKey(params[0]);
				bean.setName(params[1]);
				userBeans.add(bean);
			}		
		
		} catch (IOException e) {
			Logger.getLogger("userPage").log(Level.SEVERE, e.getMessage());
		}
	}
	
	/**
	 * This method rewrites user configuration file according to the form GET request.
	 * @param user
	 * @return userpage
	 */
	public String deleteUser(UserBean user){
		Logger.getLogger("userPage").log(Level.INFO, "Delete user command on user " + user.getName());
		
		File users = new File(USERCONFIG);
		try(FileOutputStream fstream = new FileOutputStream(users, false)){
			userBeans.remove(user);
			
			//delete later
			Logger.getLogger("userPage").log(Level.INFO, "Number of users: " + userBeans.size());
			
			for(UserBean bean : userBeans){
				String line = KEY + bean.getKey() + "-" + bean.getName() + '\n';
				byte[] bytes = line.getBytes();
				fstream.write(bytes);
			}
			
		} catch (IOException e) {
			Logger.getLogger("userPage").log(Level.SEVERE, e.getMessage());
		} 	

		return "userpage";
	}
	
	/**
	 * This method returns a page, where user can add new user information.
	 * @return newuser page
	 */
	public String createUser(){
		return "newuser";
	}
}
