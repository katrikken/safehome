package com.kuryshee.safehome.requestdataretriever;

import java.io.ByteArrayInputStream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;


/**
 * Implements extraction of data from HTTP GET response input stream.
 * 
 * @author Ekaterina Kurysheva
 *
 */
public class GetDataRetriever {
	
	private String charset = "UTF-8";
	
	/**
	 * Extracts short data in string format.
	 * @param input is an array of bytes.
	 * @return string is converted input.
	 */
	public String getStringData(byte[] input) {
		return new String(input).trim();
	}
	
	/**
	 * Extracts JSON array from input stream.
	 * @param input as byte array.
	 * @return {@link JsonArray}
	 */
	public JsonArray getJsonArray(byte[] input) {
		JsonReader jsonReader = Json.createReader(new ByteArrayInputStream(input));
		JsonArray array = jsonReader.readArray();
		jsonReader.close();
		
		return array;
	}
	
}
