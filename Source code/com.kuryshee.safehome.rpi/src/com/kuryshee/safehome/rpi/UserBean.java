package com.kuryshee.safehome.rpi;

/**
 * Class implements bean containing user information.
 * 
 * @author Ekaterina Kurysheva
 */
public class UserBean {
    private String tag;
    private String name;
    private String password;

    /**
     * Getter for the property containing RFID tag associated with the user.
     * @return tag as a string.
     */
    public String getTag() {
        return tag;
    }

    /**
     * Setter for the property containing RFID tag associated with the user.
     * @param tag as a string.
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * Getter for the property containing name of the user.
     * @return name of the user.
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for the property containing name of the user.
     * @param name of the user.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for the property containing password for the user.
     * @return password for the user.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Setter for the property containing password for the user.
     * @param password for the user.
     */
    public void setPassword(String password) {
        this.password = password;
    } 
}
