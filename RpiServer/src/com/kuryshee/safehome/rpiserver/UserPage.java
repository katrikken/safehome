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
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;

/**
 * This class implements managed bean for the page with user management logic.
 * @author Ekaterina Kurysheva
 */
@ManagedBean(name="userPage")
@RequestScoped
public class UserPage implements Serializable {
	
	/**
	 * The property is bounded to the index page and contains user name entered during logging in.
	 */
	@ManagedProperty("#{indexPage.userName}")
	private String userName;
	
	private List<UserBean> userBeans = new ArrayList<>();
	
	/**
	 * Getter for the property {@link #userName}
	 * @return user name provided during logging in.
	 */
	public String getUserName() {
		return userName;
	}
	
	/**
	 * Setter for the property {@link #userName}
	 * @param userName is provided during logging in.
	 */
	public void setUserName(String userName) {	
		this.userName = userName;
	}

	/**
	 * Getter for the property userBeans.
	 * Updates the content of a property within a request scope.
	 * @return list of instances of {@link UserBean} class.
	 */
	public List<UserBean> getUserBeans(){
		setUserBeans();
		return userBeans;
	}
	
	/**
	 * Setter for the property userBeans.
	 * The method reads configuration file and creates instances of a {@link UserBean} class.
	 */
	private void setUserBeans(){
		//List of users should be updated upon new page request.
		userBeans = new ArrayList<>();
		
		try(BufferedReader br = new BufferedReader(new FileReader(RpiServlet.USERCONFIG))){
			String conf;
			
			while( (conf = br.readLine()) != null){
				if(!conf.trim().isEmpty()){
					String params[] = conf.substring(RpiServlet.KEY.length()).split("-");
					
					UserBean bean = new UserBean();
					bean.setToken(params[0]);
					bean.setName(params[1]);
					userBeans.add(bean);
				}
			}		
		
		} catch (Exception e) {
			Logger.getLogger("userPage").log(Level.SEVERE, "Config file exception", e);
		}
	}
	
	/**
	 * This method rewrites user configuration file according to the form GET request.
	 * @param user is a selected instance of a {@link UserBean} class.
	 * @return updated userpage. 
	 */
	public String deleteUser(UserBean user){
		
		Logger.getLogger("userPage").log(Level.INFO, "Delete user command on user " + user.getName());
		
		File users = new File(RpiServlet.USERCONFIG);
		try(FileOutputStream fstream = new FileOutputStream(users, false)){
			userBeans.remove(user);
			
			Logger.getLogger("userPage").log(Level.INFO, "User count: " + userBeans.size());
			
			fstream.write('\n');
			
			for(UserBean bean : userBeans){
				String line = RpiServlet.KEY + bean.getToken() + "-" + bean.getName() + '\n';
				byte[] bytes = line.getBytes();
				fstream.write(bytes);
			}
			
		} catch (IOException e) {
			Logger.getLogger("userPage").log(Level.SEVERE, e.getMessage());
		} 	

		return "userpage";
	}
	
	/**
	 * This method redirects to a page, where new user can be registered.
	 * @return newuser page
	 */
	public String createUser(){
		return "newuser";
	}
}
