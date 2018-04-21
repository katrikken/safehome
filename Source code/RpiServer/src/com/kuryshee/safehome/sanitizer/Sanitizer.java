package com.kuryshee.safehome.sanitizer;

/**
 * This class performs user HTML input sanitizing.
 * 
 * @author Ekaterina Kurysheva
 */
public final class Sanitizer {
	
	/**
	 * This method replaces crucial HTML special characters with their encoded form.
	 * @param input is a user HTML input.
	 * @return sanitized string.
	 */
	public static String sanitize(String input){
		input.replaceAll("&", "&amp;");
		input.replaceAll("<", "&lt;");
		input.replaceAll(">", "&gt;");
		input.replaceAll("/", "&frasl;");
		return input;
	}
}
