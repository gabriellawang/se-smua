/*
 * An App Object represents an app.
 */
package com.app.model;

/**
 *
 * @author G4T6
 */
public class App {

    private int id;
    private String name;
    private String category;

    /**
     * Construct an App Object with the following parameters
     *
     * @param id the id of app
     * @param name the name of app
     * @param category the category of the app belongs to
     */
    public App(int id, String name, String category) {
        this.id = id;
        this.name = name;
        this.category = category;
    }

    /**
     * get Id of the App
     *
     * @return int value representing id of the app
     */
    public int getId() {
        return id;
    }

    /*
     * get the name of the App
     *
     * @return a String value representing the name of the app
     */

    /**
     *
     * @return the name of the user
     */
    
    public String getName() {
        return name;
    }

    /**
     * get the category of the App
     *
     * @return a String value representing the category of the app belongs to
     */
    public String getCategory() {
        return category;
    }

}
