package com.kuryshee.safehome.rpiserver;

/**
 * This class implements user bean for users with registered chip tokens.
 * 
 * @author Ekaterina Kurysheva
 */
public class UserBean {
	
	private String name;
	
	private String token;
	
	private String password;
	
	/**
	 * Getter for the property containing user password.
	 * @return user password.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Setter for the property containing user password.
	 * @param password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Getter for the property name of a user with registered token.
	 * @return name of a user.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Setter for the property name.
	 * @param name of a user with registered token.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Getter for the property token.
	 * @return token code.
	 */
	public String getToken() {
		return token;
	}
	
	/**
	 * Setter for the property token.
	 * @param token code.
	 */
	public void setToken(String token) {
		this.token = token;
	}			
}
