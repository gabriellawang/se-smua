/*
 * An AppUsage Object represents the usage record of an app
 */
package com.app.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author G4T6
 */
public class AppUsage implements Comparable<AppUsage> {

    private String timestamp;
    private String macAddress;
    private int id;

    /**
     * Construct an AppUsage Object with the following parameters
     *
     * @param timestamp the time of that a user accessed the app
     * @param macAddress the hashed MAC address indicating the unique id of the traced device
     * @param id the unique integer identifier of the app
     */
    public AppUsage(String timestamp, String macAddress, int id) {
        this.timestamp = timestamp;
        this.macAddress = macAddress;
        this.id = id;
    }

    /**
     * Get the value of the timestamp value
     *
     * @return a String value representing the time of that a user accessed the app
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Get the value of MAC address
     *
     * @return a String value representing the hashed MAC address indicating the unique id of the traced device
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     * Get the value of id
     *
     * @return the unique integer identifier of the app
     */
    public int getId() {
        return id;
    }

    /**
     * Get the specific date and time of the app being accessed
     *
     * @return a Date object representing the date and time of the app being accessed
     */
    public Date getDate() {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = fmt.parse(timestamp);
        } catch (ParseException ex) {
            Logger.getLogger(AppUsage.class.getName()).log(Level.SEVERE, null, ex);
        }
        return date;
       
    }
    @Override
    public int compareTo(AppUsage other){
        AppUsage otherAppUsage  = (AppUsage)other;
        return getDate().compareTo(otherAppUsage.getDate());
    }

}
