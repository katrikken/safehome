package com.kuryshee.safehome.rpiserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

@ManagedBean(name="pagesService")
@RequestScoped
public class PagesService {

    private String CONFIG = ".config";
    private String USERCONFIG = "/home/pi/NetBeansProjects/com.kuryshee.safehome.rpi/keys.txt";
    private String KEY = "key ";
	
	private String login;
	
	private String password;
	
	private List<UserBean> userBeans = new ArrayList<>();
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	/**
	 * This method validates user sign in data.
	 * @return user page if sign in was successful. 
	 */
	public String checkUser(){
		if(checkUserRequest()){
			return "userpage";
		}
		return "index";
	}
	
	/**
	 * This method checks configuration file if user sign in data are valid.
	 * @return true if data are valid.
	 */
	private boolean checkUserRequest(){	
		try(BufferedReader br = new BufferedReader(new FileReader(CONFIG))){
			String conf = br.readLine();
			String params[] = conf.split(" ");
			if (params[0] == login && params[1] == password){
				return true;
			}
		
		} catch (IOException e) {
			Logger.getLogger("Page Service").log(Level.SEVERE, e.getMessage());
		}
		return false;
	}
	
	public List<UserBean> getUsers(){
		setUserBeans();
		return userBeans;
	}
	
	private void setUserBeans(){
		try(BufferedReader br = new BufferedReader(new FileReader(USERCONFIG))){
			String conf;
			
			while( (conf = br.readLine()) != null){
				String params[] = conf.substring(0, KEY.length()).split("-");
				UserBean bean = new UserBean();
				bean.setKey(params[0]);
				bean.setName(params[1]);
				userBeans.add(bean);
			}		
		
		} catch (IOException e) {
			Logger.getLogger("Page Service").log(Level.SEVERE, e.getMessage());
		}
	}
	
	public String deleteUser(UserBean user){
		File users = new File(USERCONFIG);
		try(FileOutputStream fstream = new FileOutputStream(users, false)){
			userBeans.remove(user);
			for(UserBean bean : userBeans){
				String line = KEY + bean.getKey() + "-" + bean.getName() + '\n';
				byte[] bytes = line.getBytes();
				fstream.write(bytes);
			}
			
		} catch (IOException e) {
			Logger.getLogger("Page Service").log(Level.SEVERE, e.getMessage());
		} 	
		
		return "userpage";
	}
	
	public String createUser(){
		return "newuser";
	}
	
	public String createUserBean(){
		//save bean to the file
		//if card tag is avialable then userpage
		//else newuser
		return "userpage";
	}
}
