package com.kuryshee.safehome.appcommunicationconsts;

/**
 * Class contains constants for communication with client application on Android.
 * 
 * @author Ekaterina Kurysheva
 *
 */
public class AppCommunicationConsts {
	
	/**
	 * Constant for passing identity token.
	 */
	public static final String TOKEN = "token";
	
	/**
	 * Constant for communicating requested action.
	 */
	public static final String ACTION = "action";
	
	/**
	 * Constant for checking server activity.
	 */
	public static final String PING = "ping";
	
	/**
	 * Constant for answering to server checking.
	 */
	public static final String PONG = "pong";
	
	/**
	 * Constant for providing expected number of items.
	 */
	public static final String COUNT = "count";
	
	/**
	 * Constant for passing time parameter.
	 */
	public static final String TIME = "time";
	
	/**
	 * Constant for passing login.
	 */
	public static final String LOGIN = "login";
	
	/**
	 * Constant for passing password.
	 */
	public static final String PASSWORD = "password";
	
	/**
	 * Constant for passing state information.
	 */
	public static final String STATE = "state";
	
	/**
	 * Constant for communicating "turned on" state.
	 */
	public static final String ON = "on";
	
	/**
	 * Constant for communicating "turned off" state.
	 */
	public static final String OFF = "off";
	
	/**
	 * Constant for communicating level of event, when "dangerous".
	 */
	public static final String DANGEROUS = "D";
	
	/**
	 * Constant for communicating level of event, when "normal".
	 */
	public static final String NORMAL = "N";
	
	/**
	 * Constant for message, when motion is detected on Raspberry Pi.
	 */
	public static final String MOTION = "Motion detected!";
	
	/**
	 * Constant for message, when photo was taken on Raspberry Pi.
	 */
	public static final String PHOTO = "Photo was taken.";
	
	/**
	 * Constant for message, when Raspberry Pi was turned off with RFID token.
	 */
	public static final String RFIDSWITCHOFF = " has turned the alarm off.";
	
	/**
	 * Constant for message, when Raspberry Pi was turned on with RFID token.
	 */
	public static final String RFIDSWITCHON = " has turned the alarm on";
	
	/**
	 * Constant for request to get identity token.
	 */
	public static final String GET_TOKEN = "gettoken";
	
	/**
	 * Constant for request to validate identity token.
	 */
	public static final String VALIDATE = "validatetoken";
	
	/**
	 * Constant for request to get latest events on Raspberry Pi.
	 */
	public static final String GET_ACTIONS = "getactions";
	
	/**
	 * Constant for request to get latest date of events registered on Raspberry Pi.
	 */
	public static final String GET_LATEST_ACTION_TIME = "getlatestactiontime";
	
	/**
	 * Constant for request to get latest date on registered photo on Raspberry Pi.
	 */
	public static final String GET_LATEST_PHOTO_TIME = "getlatestphototime";
	
	/**
	 * Constant for request to get photo identifiers for downloading.
	 */
	public static final String GET_PHOTO_IDS = "getphotoids";
	
	/**
	 * Constant for request to download photo registered on Raspberry Pi.
	 */
	public static final String GET_PHOTO = "getphoto";
	
	/**
	 * Constant for request to get Raspberry Pi state.
	 */
	public static final String GET_RPI_STATE = "getstate";
	
	/**
	 * Constant for request to delete photo from the database.
	 */
	public static final String DELETE_PHOTO = "deletephoto";
	
	/**
	 * Constant for request to change state of Raspberry Pi.
	 */
	public static final String CHANGE_STATE = "changestate";
	
	/**
	 * Constant for request to take photo on Raspberry Pi.
	 */
	public static final String TAKE_PICTURE = "takepicture";
	
	/**
	 * Constant for answer "true".
	 */
	public static final String TRUE = "true";
	
	/**
	 * Constant for answer "false".
	 */
	public static final String FALSE = "false";
	
	/**
	 * Constant for database timestamp formatting.
	 */
	public static final String DATE_FORMAT_DB = "DD-MM-YYYY HH24:MI:SS.FF";
	
	/**
	 * Constant for application date formatting.
	 */
	public static final String DATE_FORMAT_APP = "dd-MM-yyyy HH:mm:ss.SSS";
	
	/**
	 * Constant for UTF-8 charset.
	 */
	public static final String UTF_CHARSET = "UTF-8";
	
	/**
	 * The answer to request with unknown or invalid parameters.
	 */
	public static final String REQUEST_FORMAT_ERROR = "Invalid request";
	
	/**
     * The definition of the answer when error occurred during request processing.
     */
    public static final String REQUEST_PROCESS_ERROR = "Process error";
    
    /**
     * The definition of the answer when user credentials are invalid.
     */
    public static final String INVALID_USER_ERROR = "Invalid user data";
    
    /**
     * The definition of the answer, when not specified error occurred.
     */
    public static final String ERROR = "error";
    
    /**
     * Pattern for user login validation
     */
    public static final String LOGIN_PATTERN = "[A-Za-z]+[A-Za-z0-9]*";
    
    /**
     * Pattern for user password validation
     */
    public static final String PASSWORD_PATTERN = "[A-Za-z0-9\\.,?!@#$%&\\*\\(\\)\\-=]+";
}
