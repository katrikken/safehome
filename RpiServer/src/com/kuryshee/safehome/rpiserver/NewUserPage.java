package com.kuryshee.safehome.rpiserver;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

/**
 * This class implements managed bean for the "newuser" page.
 * @author Ekaterina Kurysheva
 */
@ManagedBean(name="newUserPage")
@RequestScoped
public class NewUserPage implements Serializable{
	
	/**
	 * The constant contains path to the file with registered tokens for a card reader.
	 */
	//private String USERCONFIG = "/home/pi/NetBeansProjects/com.kuryshee.safehome.rpi/keys.txt";
	private final String USERCONFIG = "keys.txt";
	
	/**
	 * The constant contains key word for the configuration file {@link #USERCONFIG}.
	 */
	private final String KEY = "key ";
	
	private String name;

	private String token;
	
	/**
	 * The property is bounded to the index page and contains user name entered during logging in.
	 */
	@ManagedProperty("#{indexPage.userName}")
	private String userName;
	
	/**
	 * This constructor fires a communication chain between this application and logic application on the Raspberry Pi.
	 * It instructs the logic part to read the token from a card reader.
	 */
	public NewUserPage(){
		RpiServlet.tasks.add(RpiServlet.REQ_READCARD);
	}

	/**
	 * Getter for the property token, which is passed to the bean through the Servlet Context.
	 * @return string with a token code. In case it has not been set, returns empty string.
	 */
	private String getToken() {
		if(token == null){
			return "";
		}
		return token;
	}

	/**
	 * Setter for the property token.
	 * @param token is provided through the Servlet Context.
	 */
	private void setToken(String token) {
		this.token = token;
	}
	
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
	 * Getter for the property name.
	 * @return name for the new user defined in the HTML input. In case the name is not set, returns empty string. 
	 */
	public String getName() {
		if(name == null){
			return "";
		}
		return name;
	}

	/**
	 * Setter for the property name.
	 * @param name is provided via HTML input.
	 */
	public void setName(String name) {
		this.name = Sanitizer.sanitize(name);	
	}
	
	/**
	 * This method checks whether the Servlet got the token information from a logic part.
	 * In case the token was read and the new user name was provided, it writes information about new user to the configuration file.
	 * @return userpage in case of successful creation of a new user. Otherwise redirects to the current page.
	 */
	public String createNewUser(){
		try{
			ServletContext servletContext = 
				(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
			
			String key = servletContext.getAttribute("card").toString();
			if(!key.equals(RpiServlet.ERROR_ANSWER)){
				setToken(key);
			}
		}
		catch(Exception ex){
			Logger.getLogger("NewUserPage").log(Level.SEVERE, ex.getMessage());
		}
		
		if(!getToken().isEmpty() && !getName().isEmpty()){
			Logger.getLogger("NewUserPage").log(Level.INFO, "User can be stored.");
			
			try(BufferedWriter bw = new BufferedWriter(new FileWriter(USERCONFIG))){
				bw.write(KEY + getToken() + "-" + getName() + '\n');
				RpiServlet.tasks.add(RpiServlet.COMMAND_SAVEUSER);
				
				return "userpage";
				
			} catch (IOException e) {
				Logger.getLogger("NewUserPage").log(Level.SEVERE, e.getMessage());
			} 	
		}
		else{
			FacesContext context = FacesContext.getCurrentInstance();
			context.addMessage("You can't save the new user!", new FacesMessage("You can't save the new user!\n"
					+ "Put your card to the reader and provide a valid name."
					+ "Check whether the card has not been already registered with another name."));
		}
		
		return "newuser";
	}
}
