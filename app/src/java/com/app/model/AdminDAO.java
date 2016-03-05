/*
 * AdminDAO represents an ArrayList of Admin values
 */
package com.app.model;

import java.util.ArrayList;

/**
 *
 * @author G4T6
 */
public class AdminDAO {

    private static ArrayList<Admin> adminList = new ArrayList<Admin>();
    
    /**
     * Adding a new Admin Object into the ArrayList: username = "admin"; password = "password".
     */

    static {
        adminList.add(new Admin("admin", "password"));
    }

    /**
     * Retrieve a specific Admin Object given a String value of username
     *
     * @param username another username String
     * @return an Admin Object if its username is same as another username, null if there is no corresponding admin Object
     */
    public static Admin retrieveAdmin(String username) {
        for (Admin admin : adminList) {
            if (admin.getUsername().equals(username)) {
                return admin;
            }
        }

        return null;
    }
}
