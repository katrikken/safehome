package com.kuryshee.safehome.rpicommunicationconsts;

/**
 * Class contains constants for communicating with the application on Raspberry Pi.
 * 
 * @author Ekaterina Kurysheva.
 *
 */
public class RpiCommunicationConsts {
	
	/**
	 * Constant for communicating request to register user from Raspberry Pi application.
	 */
	public static final String REGISTER_USER = "registeruser";
	
	/**
	 * Constant for communicating request to delete user from Raspberry Pi application.
	 */
	public static final String DELETE_USER = "deleteuser";
	
	/**
	 * Constant for communicating request to register event on Raspberry Pi.
	 */
	public static final String REGISTER_ACTION = "registeraction";
	
	/**
     * Constant for the POST request to the server when sending a photo.
     */
	public static final String SAVE_PHOTO = "savephoto";
	
	/**
     * Constant for the server to ask for the program state.
     */
	public static final String POST_STATE = "poststate";
	
	/**
     * Constant for the Raspberry Pi to ask for new tasks from server.
     */
	public static final String GET_TASK = "gettask";
	
	/**
     * Constant for commanding switching the program state to on.
     */
	public static final String TURN_ON = "turnon";
	
	/**
     * Constant for commanding switching the program state to off.
     */
	public static final String TURN_OFF = "turnoff";
	
	/**
     * Constant for the server to command taking a photo.
     */
	public static final String TAKE_PICTURE = "takepicture";
	
	/**
	 * Constant for user login parameter in requests.
	 */
	public static final String USER_LOGIN = "login";
	
	/**
	 * Constant for user password parameter in requests.
	 */
	public static final String USER_PASSWORD = "password";
	
	/**
	 * Constant for request definition parameter.
	 */
	public static final String ACTION = "action";
	
	/**
	 * Constant for POST request parameter for description of events.
	 */
	public static final String RPI_ACTION_INFO = "info";
	
	/**
     * Constant for the POST request parameter of time.
     */
	public static final String TIME = "time";
	
	/**
	 * Constant for the POST request parameter of level of event.
	 */
	public static final String LEVEL = "level";
	
	/**
	 * Constant for the POST request parameter of the photo name.
	 */
	public static final String PHOTO_NAME = "name";
	
	/**
     * Constant for the POST request parameter of photo file.
     */
	public static final String PHOTO = "photo";
	
	/**
	 * Constant for the request parameter for passing Raspberry Pi state.
	 */
	public static final String STATE = "state";
	
	/**
     * The POST request parameter for passing the id data.
     */
	public static final String RPI_ID = "id";
	
	/**
	 * The POST request parameter for passing the RFID token data.
	 */
	public static final String CARD_PARAM = "card";
	
	/**
	 * Constant for the server to tell the Raspberry Pi to read a RFID token.
	 */
	public static final String COMMAND_READTOKEN = "read";
	
	/**
	 * Constant for the server to tell the Raspberry Pi to update user list.
	 */
    public static final String COMMAND_UPDATEUSERS = "saveuser";
    
	/**
	 * Constant contains key word "key" for the configuration file.
	 */
	public static final String KEY = "key";
	
	/**
	 * Constant contains key word "name" for the configuration file.
	 */
	public static final String NAME = "name";
	
	/**
	 * Constant contains key word "token" for the configuration file.
	 */
	public static final String TOKEN = "token";
	
	/**
	 * Constant contains key word "password" for the configuration file.
	 */
	public static final String PASSWORD = "password";
}

