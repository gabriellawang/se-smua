/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.app.model;

/**
 *
 * @author G4-T6
 */
public class LocationRecord {

    private String timestamp;
    private String macAddress;
    private int locationId;

    /**
     *Construct a LocationRecord Object with the following parameters
     * @param timestamp a String 
     * @param macAddress a String
     * @param locationId a Integer
     */
    public LocationRecord(String timestamp, String macAddress, int locationId) {
        this.timestamp = timestamp;
        this.macAddress = macAddress;
        this.locationId = locationId;
    }

    /**
     *Gets the timestamp
     * @return a String timestap of the LocationRecord
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     *Gets the MacAddress
     * @return a String macAddress of the LocationRecord
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     *Gets the location Id
     * @return a integer location id of the LocationRecord
     */
    public int getLocationId() {
        return locationId;
    }

}
