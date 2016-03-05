/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.app.model;

import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author G4-T6
 */
public class LocationRecordValidation {

    private ArrayList<ErrorMessage> errorMsg;
    private ArrayList<LocationRecord> locationRecords;
    private List<String[]> csvFile;
    private TreeMap<Integer, LocationRecord> additionalData;
    private Connection conn;
    private int additionalSize;

    /**
     *Construct a LocationRecordValidation Object with the following parameters
     * @param csvFile a List of String array
     * @param conn a Connection
     */
    public LocationRecordValidation(List<String[]> csvFile, Connection conn) {
        errorMsg = new ArrayList<>();
        locationRecords = new ArrayList<>();
        this.csvFile = csvFile;
        additionalData = new TreeMap<>();
        this.conn = conn;
        additionalSize = 0;
    }
    
    /**
     *Get the size of Location Records
     * @return the number of LocationRecord
     */
    public int getSize(){
        return locationRecords.size();
    }

    /**
     *Checks if LocationRecordValidation is additional
     * @param isAdditional a boolean
     */
    public void check(boolean isAdditional) {
        int count = 2;
        String [] header = csvFile.get(0);
        TreeMap<Integer, LocationRecord> recordTemp = new TreeMap<>();
        int iTime = -1;
        int iMac = -1;
        int iID = -1;
        for(int i = 0; i < header.length; i++){
            String h = header[i];
            if(h.toLowerCase().trim().equals("timestamp")){
                iTime = i;
            }else if(h.toLowerCase().trim().equals("mac-address")){
                iMac = i;
            }else{
                iID = i;
            }
        }
        HashSet idSet = LocationDAO.retrieveAllID(conn);
        HashMap<String, Integer> data = new HashMap<>();//use to save row number when checking deplicate rows
        for (int i = 1; i < csvFile.size(); i++) {
            String timestamp = csvFile.get(i)[iTime].trim();
            String adrs = csvFile.get(i)[iMac].trim().toLowerCase();
            String id = csvFile.get(i)[iID].trim();
            String msg = "";
            boolean isValid = true;

            if (id.length() == 0 || timestamp.length() == 0 || adrs.length() == 0) {
                isValid = false;
                if (timestamp.length() == 0) {
                    msg += "blank timestamp,";
                }
                if (adrs.length() == 0) {
                    msg += "blank mac address,";
                }
                if (id.length() == 0) {
                    msg += "blank location id,";
                }
            } else {
                int locationid;
                try {
                    locationid = Integer.parseInt(id);
                    if (!idSet.contains(locationid)) {
                        isValid = false;
                        msg += "invalid location,";
                    }
                } catch (NumberFormatException e) {
                    isValid = false;
                    msg += "invalid location,";
                }
                if (!adrs.matches("[a-fA-F0-9]{40}")) { //if (!adrs.matches("[\\dA-Fa-f]+"))
                    isValid = false;
                    msg += "invalid mac address,";
                }
                //check timestamp format: YYYY-MM-DD HH:MM:SS [invalid timestamp]
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    sdf.setLenient(false);
                    sdf.parse(timestamp);
                } catch (ParseException e) {
                    isValid = false;
                    msg += "invalid timestamp";
                }
            }

            if (isValid) {
                String key = timestamp + adrs;
                if (data.get(key) != null) {
                    int rowNum = data.get(key);
                    String[] dupErr = {"duplicate row"};
                    errorMsg.add(new ErrorMessage("location.csv", rowNum, dupErr));
                    if (!isAdditional) {
                        recordTemp.remove(rowNum);
                    } else {
                        additionalData.remove(rowNum);
                    }

                }
                data.put(key, count);

                LocationRecord lr = new LocationRecord(timestamp, adrs, Integer.parseInt(id));
                if (!isAdditional) {
                    recordTemp.put(count, lr);
                } else {
                    additionalData.put(count, lr);
                }

            } else {
                errorMsg.add(new ErrorMessage("location.csv", count, msg.split(",")));
            }
            count++;
        }
        locationRecords = new ArrayList<LocationRecord>(recordTemp.values());
    }

    /**
     *Bootstrap the data
     * @return a ArrayList of ErrorMessage
     */
    public ArrayList<ErrorMessage> bootstrap() {//throws SQLException {
        check(false);
        LocationRecordDAO.bootstrap(locationRecords, conn);
        return errorMsg;
    }

    /**
     *Adds additional data to database
     * @return a ArrayList of ErrorMessage
     */
    public ArrayList<ErrorMessage> addAdditionalData() {
        check(true);
        ArrayList<ErrorMessage> errors = LocationRecordDAO.update(additionalData, conn);
        additionalSize = additionalData.size() - errors.size();
        errorMsg.addAll(errors);
        return errorMsg;
    }
    
    /**
     *Gets the additional size 
     * @return the number of additional data loaded
     */
    public int getAdditionalSize(){
        return additionalSize;
    }

    /**
     *Deletes the location data
     * @return the HashMap of deleted location data
     */
    public HashMap<Boolean, Integer> deleteLocationData() {
        HashMap<Boolean, Integer> result  = new HashMap<>();
        HashSet idSet = LocationDAO.retrieveAllID(conn);
        for (int i = 1; i < csvFile.size(); i++) {
            String timestamp = csvFile.get(i)[0].trim();
            String adrs = csvFile.get(i)[1].trim().toLowerCase();
            String id = csvFile.get(i)[2].trim();
            boolean isValid = true;

            if (timestamp.length() == 0 || adrs.length() == 0) {
                isValid = false;
            } else {
                int locationid;
                if (!id.isEmpty()) {
                    try {
                        locationid = Integer.parseInt(id);
                        if (!idSet.contains(locationid)) {
                            isValid = false;
                        }
                    } catch (NumberFormatException e) {
                        isValid = false;
                    }
                }
                if (!adrs.matches("[a-fA-F0-9]{40}")) { //if (!adrs.matches("[\\dA-Fa-f]+"))
                    isValid = false;
                }
                //check timestamp format: YYYY-MM-DD HH:MM:SS [invalid timestamp]
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    sdf.setLenient(false);
                    sdf.parse(timestamp);
                } catch (ParseException e) {
                    isValid = false;
                }
            }
            if (isValid) {
                int locationId;
                if(id.isEmpty()){
                    locationId = -1;
                }else{
                    locationId = Integer.parseInt(id);
                }
                LocationRecord lr = new LocationRecord(timestamp, adrs, locationId);
                locationRecords.add(lr);
            }
        }
        result = LocationRecordDAO.delete(locationRecords, conn);
        return result;
    }
}

