package com.kuryshee.safehome.rpiserver;

public final class Sanitizer {
	static String sanitize(String input){
		input.replaceAll("&", "&amp;");
		input.replaceAll("<", "&lt;");
		input.replaceAll(">", "&gt;");
		return input;
	}
}
