package cz.kuryshee.safehome.safehomeclient;

import android.app.Application;
import android.util.LruCache;

/**
 * Class extends {@link Application} in order to add properties for sharing across the application.
 *
 * @author Ekaterina Kurysheva
 */
public class GlobalVariables extends Application {

    private String serverPath = null;

    private boolean isDebugMode;

    private LruCache<String, PhotoItem> pictureMemoryCache = null;

    private LruCache<String, RpiEventItem> eventsMemoryCache = null;

    private LruCache<String, String> stringMemoryCache = null;

    private String token;

    /**
     * Getter fot the identity token of the user, logged in the app.
     * @return identity token.
     */
    public String getToken() {
        return token;
    }

    /**
     * Setter fot the identity token of the user, logged in the app.
     * @param token identity token.
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Getter for the property containing address of the server.
     * @return server address.
     */
    public String getServerPath() {
        return serverPath;
    }

    /**
     * Setter for the property containing address of the server.
     * @param serverPath is a full server address.
     */
    public void setServerPath(String serverPath) {
        this.serverPath = serverPath;
    }

    /**
     * Getter for the boolean property containing info about the app mode.
     * @return true, if app is running in debug mode.
     */
    public boolean isDebugMode() {
        return isDebugMode;
    }

    /**
     * Setter for the boolean property containing info about the app mode.
     * @param debugMode boolean.
     */
    public void setDebugMode(boolean debugMode) {
        isDebugMode = debugMode;
    }

    /**
     * Getter for the property containing cached photos.
     * @return {@link LruCache}.
     */
    public LruCache<String, PhotoItem> getPictureMemoryCache() {
        return pictureMemoryCache;
    }

    /**
     * Setter for the property containing cached photos.
     * @param pictureMemoryCache {@link LruCache}.
     */
    public void setPictureMemoryCache(LruCache<String, PhotoItem> pictureMemoryCache) {
        this.pictureMemoryCache = pictureMemoryCache;
    }

    /**
     * Getter for the property containing cached events.
     * @return {@link LruCache}.
     */
    public LruCache<String, RpiEventItem> getEventsMemoryCache() {
        return eventsMemoryCache;
    }

    /**
     * Setter for the property containing cached events.
     * @param eventsMemoryCache {@link LruCache}.
     */
    public void setEventsMemoryCache(LruCache<String, RpiEventItem> eventsMemoryCache) {
        this.eventsMemoryCache = eventsMemoryCache;
    }

    /**
     * Getter for the property containing cached strings for the application run.
     * @return {@link LruCache}.
     */
    public LruCache<String, String> getStringMemoryCache() {
        return stringMemoryCache;
    }

    /**
     * Setter for the property containing cached strings for the application run.
     * @param stringMemoryCache {@link LruCache}.
     */
    public void setStringMemoryCache(LruCache<String, String> stringMemoryCache) {
        this.stringMemoryCache = stringMemoryCache;
    }

}
