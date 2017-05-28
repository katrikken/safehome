package com.kuryshee.safehome.rpiserver;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

@ManagedBean(name="newUserPage")
@RequestScoped
public class NewUserPage implements Serializable{
	
	//private String USERCONFIG = "/home/pi/NetBeansProjects/com.kuryshee.safehome.rpi/keys.txt";
	private String USERCONFIG = "keys.txt";
	private String KEY = "key ";
	
	private String userName;
	
	private String cardCode;
	
	public NewUserPage(){
		RpiServlet.tasks.add(RpiServlet.REQ_READCARD);
	}

	private String getCardCode() {
		if(cardCode == null){
			return "";
		}
		return cardCode;
	}

	private void setCardCode(String cardCode) {
		this.cardCode = cardCode;
	}
	
	public String getUserName() {
		if(userName == null){
			return "";
		}
		return userName;
	}
	
	
	public void setUserName(String userName) {
		
		this.userName = Sanitizer.sanitize(userName);
	}
	
	public String createUserBean(){
		try{
			ServletContext servletContext = 
				(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
			String key = servletContext.getAttribute("card").toString();
			setCardCode(key);
		}
		catch(Exception ex){
			Logger.getLogger("NewUserPage").log(Level.SEVERE, ex.getMessage());
		}
		
		if(!getCardCode().isEmpty() && !getUserName().isEmpty()){
			Logger.getLogger("NewUserPage").log(Level.INFO, "User can be stored.");
			
			try(BufferedWriter bw = new BufferedWriter(new FileWriter(USERCONFIG))){
				bw.write(KEY + getCardCode() + "-" + getUserName() + '\n');
				RpiServlet.tasks.add(RpiServlet.COMMAND_SAVEUSER);
				return "userpage";
			} catch (IOException e) {
				Logger.getLogger("NewUserPage").log(Level.SEVERE, e.getMessage());
			} 	
		}
		else{
			FacesContext context = FacesContext.getCurrentInstance();
			context.addMessage(null, new FacesMessage("You can't save the new user!\n"
					+ "Put your card to the reader and provide valid name."));
		}
		
		return "newuser";
	}
	
}
