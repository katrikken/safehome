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

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

/**
 * This class implements managed bean for the page with user management logic.
 * @author Ekaterina Kurysheva
 */
@ManagedBean(name="userPage")
@ViewScoped
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
			
			fstream.write('\n');
			
			for(UserBean bean : userBeans){
				String line = RpiServlet.KEY + bean.getToken() + "-" + bean.getName() + '\n';
				byte[] bytes = line.getBytes();
				fstream.write(bytes);
			}
			RpiServlet.tasks.add(RpiServlet.COMMAND_UPDATEUSERS);
			
		} catch (IOException e) {
			Logger.getLogger("userPage").log(Level.SEVERE, e.getMessage());
		} 	

		return "userpage";
	}
	
	/**
	 * This method redirects to a page, where new user can be registered.
	 * It signalizes to the servlet about upcoming task of fetching data from the logic part.
	 * @return newuser page or null in case new user yet cannot be registered.
	 */
	public String createUser(){		
		try{
			//Check if the tag is known
			ServletContext servletContext = 
				(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
			
			String key = servletContext.getAttribute(RpiServlet.CARD_PARAM).toString();

			//If the tag is not present
			if(!key.equals(RpiServlet.ERROR_ANSWER)){
				return "newuser";			
			}	
		}
		catch(Exception ex){
			Logger.getLogger("UserPage").log(Level.SEVERE, ex.getMessage());
		}		
		
		//Show the message
		FacesContext.getCurrentInstance().addMessage(
				errorMsgComponent.getClientId(), 
				new FacesMessage("Put the new token to the reader and press the button again!"));
		//Check whether the task has been set
		if(!RpiServlet.tasks.contains(RpiServlet.COMMAND_READTOKEN)){
			RpiServlet.tasks.add(RpiServlet.COMMAND_READTOKEN);
		}
		
		return null;
	}
}
