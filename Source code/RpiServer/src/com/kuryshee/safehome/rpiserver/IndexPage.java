package com.kuryshee.safehome.rpiserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.kuryshee.safehome.rpicommunicationconsts.RpiCommunicationConsts;
import com.kuryshee.safehome.sanitizer.Sanitizer;

/**
 * This class implements a managed bean for the index page of the application.
 * 
 * @author Ekaterina Kurysheva
 */
@ManagedBean(name="indexPage")
@SessionScoped
public class IndexPage implements Serializable{
	
	/**
	 * The attribute contains the path to the file with predefined log in parameters for the administrator user.
	 */
    private final String CONFIG = "/WEB-INF/config/user.json";
	
    /**
     * Constant attribute for the HTTP session map. 
     */
    public static final String AUTH_KEY = "user.name";
	
	private String password;
	
	private String userName;
	
	private UIComponent errorMsgComponent;

	/**
	 * Getter for the property errorMsgComponent where {@link FacesMessage} is displayed.
	 * @return the component for error messages.
	 */
    public UIComponent getErrorMsgComponent() {
        return errorMsgComponent;
    }

    /**
     * Setter for the property errorMsgComponent.
     * @param errorMsgComponent is the component to display messages in.
     */
    public void setErrorMsgComponent(UIComponent errorMsgComponent) {
        this.errorMsgComponent = errorMsgComponent;
    }
	
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
			
			return "restricted/userpage?faces-redirect=true";
		}
		else{
			FacesContext.getCurrentInstance().addMessage(
					errorMsgComponent.getClientId(), 
					new FacesMessage("Incorrect login and password!"));
			return PageNames.INDEX;
		}	    
	}
	
	/**
	 * This method compares configuration file data with user sign in data and decides if they are valid.
	 * @return true if data are valid and user can be logged in.
	 */
	private boolean checkUserRequest(){	
		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
		
		try(InputStream is = ec.getResourceAsStream(CONFIG); JsonReader reader = Json.createReader(is)){
			JsonObject conf = reader.readObject();
			String login = conf.getString(RpiCommunicationConsts.USER_LOGIN);	
			String pswd = conf.getString(RpiCommunicationConsts.USER_PASSWORD);
			if (login.equals(userName) && pswd.equals(password)){
				Logger.getLogger(IndexPage.class.getName()).log(Level.INFO, "Login and password are correct");
				return true;			
			}
		
		} catch (IOException e) {
			Logger.getLogger(IndexPage.class.getName()).log(Level.SEVERE, e.getMessage());
		}
		return false;
	}
}
