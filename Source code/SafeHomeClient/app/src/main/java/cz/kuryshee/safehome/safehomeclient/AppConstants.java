package cz.kuryshee.safehome.safehomeclient;

/**
 * Class containing constants, which are used exclusively in the application.
 *
 * @author Ekaterina Kurysheva
 */
public class AppConstants {
    /**
     * The name of configuration file in application assets.
     */
    public static final String CONFIG = "config.json";

    /**
     * The string identifying application mode in {@link #CONFIG}.
     */
    public static final String DEBUG_MODE = "debugMode";

    /**
     * The string identifying server address in {@link #CONFIG}.
     */
    public static final String SERVER_ADDRESS = "serverPath";

    /**
     * The string identifying true for the debug mode in {@link #DEBUG_MODE}.
     */
    public static final String YES = "yes";

    /**
     * The string for storing identity token in cache.
     */
    public static final String TOKEN = "token";

    /**
     * The string for storing user login in cache.
     */
    public static final String LOGIN = "login";

    /**
     * The string for storing user password in cache.
     */
    public static final String PASSWORD = "password";

    /**
     * Date format for presenting to the user.
     */
    public static final String DATE_USER_FORMAT = "HH:mm dd-MM-yyyy";

    /**
     * The string for getting time parameter in JSON object from the server.
     */
    public static final String JSON_TIME_PARAM = "t";

    /**
     * The string for getting photo name parameter in JSON object from the server.
     */
    public static final String JSON_NAME_PARAM = "n";

    /**
     * The string for getting event level parameter in JSON object from the server.
     */
    public static final String JSON_LEVEL_PARAM = "l";

    /**
     * The string for getting event description parameter in JSON object from the server.
     */
    public static final String JSON_ACTIOM_PARAM = "a";

    /**
     * The constant for interacting about application permission to write to external storage with the environment.
     */
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXT_STORAGE = 1905;

    /**
     * The constant for the application's notification channel.
     */
    public static final String CHANNEL = "1905";

    /**
     * The constant for intrusion notifications.
     */
    public static final int NOTIFY_ID = 1995;

    /**
     * The constant for log in notifications.
     */
    public static final int LOGIN_NOTIF_ID = 9505;

    /**
     * Name of the file to store cached data.
     */
    public static final String APP_CACHE = "temp";

    /**
     * Constant of the server path on the domain.
     */
    public static final String SERVER_SUFFIX = "/SafeHome/SafeHomeServer/app";
}
