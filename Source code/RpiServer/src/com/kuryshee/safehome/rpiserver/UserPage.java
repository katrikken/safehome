package com.kuryshee.safehome.rpiserver;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import com.kuryshee.safehome.httprequestsender.AnswerConstants;
import com.kuryshee.safehome.rpicommunicationconsts.RpiCommunicationConsts;

/**
 * This class implements managed bean for the page with user management logic.
 * 
 * @author Ekaterina Kurysheva
 */
@ManagedBean(name="userPage")
@SessionScoped
public class UserPage implements Serializable {
	
	private UserConfigManager reader = null;
	
	/**
	 * The property is bounded to the index page and contains user name entered during logging in.
	 */
	@ManagedProperty("#{indexPage.userName}")
	private String userName;
	
	private UserBean changePswdBean = null;
	
	/**
	 * Getter for the property, where a {@link UserBean} of user, whose password is to be changed, stored.
	 * @return {@link UserBean} of the user.
	 */
	public UserBean getChangePswdBean() {
		return changePswdBean;
	}

	/**
	 * Setter for the property, where a {@link UserBean} of user, whose password is to be changed, stored.
	 * @param changePswdBean
	 */
	public void setChangePswdBean(UserBean changePswdBean) {
		this.changePswdBean = changePswdBean;
	}

	private List<UserBean> userBeans;
	
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
		try {
			if(reader == null) {
				reader = new UserConfigManager(new File(RpiServlet.readConfig()));
			}
			userBeans.addAll(reader.readUsersToUserBeans());
		} catch (Exception e) {
			Logger.getLogger(UserPage.class.getName()).log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	/**
	 * This method rewrites user configuration file according to the form GET request.
	 * @param user is a selected instance of a {@link UserBean} class.
	 * @return updated userpage. 
	 */
	public String deleteUser(UserBean user){
		
		Logger.getLogger(UserPage.class.getName()).log(Level.INFO, "Delete user command on user " + user.getName());
		
		try{
			userBeans.remove(user);
			
			if(reader == null) {
				reader = new UserConfigManager(new File(RpiServlet.readConfig()));
			}		
			reader.writeBeansToJson(userBeans);
			if(!RpiServlet.tasks.contains(RpiCommunicationConsts.COMMAND_UPDATEUSERS)){
				RpiServlet.tasks.add(RpiCommunicationConsts.COMMAND_UPDATEUSERS);
			}
			
		} catch (Exception e) {
			Logger.getLogger(UserPage.class.getName()).log(Level.SEVERE, e.getMessage(), e);
		} 	

		return PageNames.USERPAGE;
	}
	
	/**
	 * Redirects to the page where user can change password.
	 * @param user
	 * @return page name
	 */
	public String changePassword(UserBean user) {
		changePswdBean = user;
		
		return PageNames.CHANGEPSWD;
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
			
			String key = servletContext.getAttribute(RpiCommunicationConsts.CARD_PARAM).toString();

			//If the tag is not present
			if(!key.equals(AnswerConstants.ERROR_ANSWER)){
				return PageNames.NEWUSER;			
			}	
		}
		catch(Exception ex){
			Logger.getLogger(UserPage.class.getName()).log(Level.SEVERE, ex.getMessage());
		}		
		
		//Show the message
		FacesContext.getCurrentInstance().addMessage(
				errorMsgComponent.getClientId(), 
				new FacesMessage("Put the new token to the reader and press the button again!"));
		//Check whether the task has been set
		if(!RpiServlet.tasks.contains(RpiCommunicationConsts.COMMAND_READTOKEN)){
			RpiServlet.tasks.add(RpiCommunicationConsts.COMMAND_READTOKEN);
		}
		
		return null;
	}
}
