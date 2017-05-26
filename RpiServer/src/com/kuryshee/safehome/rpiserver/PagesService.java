package com.kuryshee.safehome.rpiserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

@ManagedBean(name="pagesService")
@RequestScoped
public class PagesService {

    private String CONFIG = "/WEB-INF/config.txt";
	
	private String login;
	
	private String password;
	
	private String userName;
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}	
	
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
	 * This method compares configuration file data with user sign in data and decides if they are valid.
	 * @return true if data are valid.
	 */
	private boolean checkUserRequest(){	
		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
		try(BufferedReader br = new BufferedReader(new InputStreamReader(ec.getResourceAsStream(CONFIG)))){
			String conf = br.readLine();
			String params[] = conf.split(" ");		
			if (params[0].equals(login) && params[1].equals(password)){
				Logger.getLogger("Page Service").log(Level.INFO, "Login and password are correct");
				return true;			
			}
		
		} catch (IOException e) {
			Logger.getLogger("Page Service").log(Level.SEVERE, e.getMessage());
		}
		Logger.getLogger("Page Service").log(Level.INFO, "Login and password are incorrect");
		return false;
	}
}
