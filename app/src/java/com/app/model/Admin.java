/*
 * Admin Objects represents Admin staff who are able to changes to the database of the app
 */
package com.app.model;

/**
 *
 * @author G4T6
 */
public class Admin {

    private String username;
    private String password;

    /**
     * Construct an Admin Object with the following parameters
     *
     * @param username the log-in username for admin staff
     * @param password the log-in password for admin staff
     */
    public Admin(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Return the username of this admin
     *
     * @return a String value representing the username of this admin
     */
    public String getUsername() {
        return username;
    }

    /**
     * Return the password of this admin
     *
     * @return a String value representing the password of this admin
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the username of this admin Object
     *
     * @param username a String
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Set the password of this admin Object
     *
     * @param password a String
     */
    public void setPassword(String password) {
        this.password = password;
    }

}
