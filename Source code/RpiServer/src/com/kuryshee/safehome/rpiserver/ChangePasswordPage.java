package com.kuryshee.safehome.rpiserver;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import com.kuryshee.safehome.appcommunicationconsts.AppCommunicationConsts;
import com.kuryshee.safehome.rpicommunicationconsts.RpiCommunicationConsts;

/**
 * Managed bean for change password page.
 * 
 * @author Ekaterina Kurysheva
 */
@ManagedBean(name="changePasswordPage")
@RequestScoped
public class ChangePasswordPage implements Serializable{
	
	private Pattern passwordPattern = Pattern.compile(AppCommunicationConsts.PASSWORD_PATTERN);
	
	@ManagedProperty("#{userPage.changePswdBean}")
	private UserBean changePswdBean;
	
	@ManagedProperty("#{indexPage.userName}")
	private String userName;
	
	private String changePswdName;
	
	private UIComponent errorMsgComponent;
	
	/**
	 * Getter for the property containing name of user, whose password is being changed.
	 * @return name of the user.
	 */
	public String getChangePswdName() {
		return changePswdName;
	}

	/**
	 * Setter for the property containing name of user, whose password is being changed.
	 * @param changePswdName name of the user.
	 */
	public void setChangePswdName(String changePswdName) {
		this.changePswdName = changePswdName;
	}

	/**
	 * Setter for the property containing {@link UserBean} of user, whose password is being changed.
	 * @param changePswdBean
	 */
	public void setChangePswdBean(UserBean changePswdBean) {
		this.changePswdBean = changePswdBean;
		this.changePswdName = this.changePswdBean.getName();
	}

	/**
	 * Setter for the property containing name of user, authorized in this session.
	 * @param userName
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Getter for the property containing {@link UserBean} of user, whose password is being changed.
	 * @return
	 */
	public UserBean getChangePswdBean() {
		return changePswdBean;
	}

	/**
	 * Getter for the property containing name of user, authorized in this session. 
	 * @return
	 */
	public String getUserName() {
		return userName;
	}

	private String password;

	/**
	 * Getter for the changed user password.
	 * @return new user password.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Setter for the changed user password.
	 * @param password
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
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
	 * Saves new password to configuration file.
	 * @return redirect to the user page in case of success.
	 */
	public String saveNewPassword() {
		Matcher passwordMatcher = passwordPattern.matcher(password);
		
		if(!passwordMatcher.matches()) {
			FacesContext.getCurrentInstance().addMessage(
					errorMsgComponent.getClientId(), 
					new FacesMessage("Password can only contain English letters, numbers and punctuation marks!"));
		}
		else {
			try{
				UserConfigManager reader = new UserConfigManager(new File(RpiServlet.readConfig()));
				List<UserBean> beans = reader.readUsersToUserBeans();
				
				for(UserBean bean: beans) {
					if(bean.getToken().equals(changePswdBean.getToken())) {
						bean.setPassword(password);
						
						reader.writeBeansToJson(beans);
						if(!RpiServlet.tasks.contains(RpiCommunicationConsts.COMMAND_UPDATEUSERS)){
							RpiServlet.tasks.add(RpiCommunicationConsts.COMMAND_UPDATEUSERS);
						}
						return PageNames.USERPAGE;
					}
				}
			
			} catch (Exception e) {
				Logger.getLogger(NewUserPage.class.getName()).log(Level.SEVERE, e.getMessage());
			} 	
		}
		return PageNames.CHANGEPSWD;
	}
}
