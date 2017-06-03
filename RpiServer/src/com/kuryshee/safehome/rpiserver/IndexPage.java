package com.kuryshee.safehome.rpiserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

/**
 * This class implements a managed bean for the index page of the application.
 * @author Ekaterina Kurysheva
 */
@ManagedBean(name="indexPage")
@SessionScoped
public class IndexPage implements Serializable{

	/**
	 * The attribute contains the path to the file with predefined log in parameters for the administrator user.
	 */
    private final String CONFIG = "/WEB-INF/config.txt";
	
    /**
     * Constant attribute for the HTTP session map. 
     */
    public static final String AUTH_KEY = "user.name";
	
	private String password;
	
	private String userName;
	
	/**
	 * Getter for the property userName bounded to the user input.
	 * @return user name from the input.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Setter for the property userName bounded to the user input.
	 * @param userName is provided by user via HTML form.
	 */
	public void setUserName(String userName) {
		this.userName = Sanitizer.sanitize(userName);
	}	
	
	/**
	 * Getter for the property password bounded to the user input.
	 * @return password entered by user.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Setter for the property password bounded to the user input.
	 * @param password is provided by user via HTML form.
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * The method checks whether user is identified.
	 * @return true if HTTP session has defined {@link #AUTH_KEY}.
	 */
	public boolean isLoggedIn() {
	    return FacesContext.getCurrentInstance().getExternalContext()
	        .getSessionMap().get(AUTH_KEY) != null;
	}

	/**
	 * This method performs user logging in and page redirecting.
	 * @return page name to which the user should be navigated. In case logging failed, the user is redirected to the same page.
	 */
	public String login() {
		if(checkUserRequest()){
			FacesContext.getCurrentInstance().getExternalContext()
				.getSessionMap().put(AUTH_KEY, userName);
			
			return "restricted/userpage";
		}
		else{
			FacesContext.getCurrentInstance().addMessage("myForm:userpswd", 
					new FacesMessage("Incorrect password!", "Incorrect password!"));
			return "index";
		}	    
	}
	
	/**
	 * This method compares configuration file data with user sign in data and decides if they are valid.
	 * @return true if data are valid and user can be logged in.
	 */
	private boolean checkUserRequest(){	
		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
		
		try(BufferedReader br = new BufferedReader(new InputStreamReader(ec.getResourceAsStream(CONFIG)))){
			String conf = br.readLine();
			String params[] = conf.split(" ");		
			if (params[0].equals(userName) && params[1].equals(password)){
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
