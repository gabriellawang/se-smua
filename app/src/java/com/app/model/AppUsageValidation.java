/*
 * Validation for app.csv which checks whether the app-id is one of the app-id in app-lookup.csv, whether the value
 * is a SHA-1 hash value, whether the value is corresponding MAC address in demographics, whether the date and time
 * is not of the correct format, whether there is already an existing record with the same values
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
 * @author G4T6
 */
public class AppUsageValidation {

    private ArrayList<ErrorMessage> errorMsg;
    private ArrayList<AppUsage> appUsages;
    private List<String[]> csvFile;
    private TreeMap<Integer, AppUsage> additionalData;
    private Connection conn;
    private int additionalSize;

    /**
     * Get the total lines of app.csv
     *
     * @return an int value represents the total lines of app.csv
     */
    public int getSize(){
        return appUsages.size();
    }
    
    /**
     * Construct a new AppUsageValidation Object which consists of the following parameters
     *
     * @param csvFile an ArrayList of String arrays represents all lines in app.csv
     * @param conn the Connection Object passed in for sql statement
     */
    public AppUsageValidation(List<String[]> csvFile, Connection conn) {
        errorMsg = new ArrayList<>();
        appUsages = new ArrayList<>();
        this.csvFile = csvFile;
        additionalData = new TreeMap<>();
        this.conn = conn;
        additionalSize = 0;
    }

    //these methods check the data, delete the invalid data from the csvFile list
    //add the error message into the the errorMsg
    /**
     *
     * @param isAdditional a boolean
     */
    public void check(boolean isAdditional) {
        int count = 2;//row number of the csv file
        HashSet idSet = AppDAO.retrieveAllID(conn);
        HashSet macSet = UserDAO.retrieveAllMacAddress(conn);
        HashMap<String, Integer> data = new HashMap<>();
        TreeMap<Integer, AppUsage> usageTemp = new TreeMap<>();
        String [] header = csvFile.get(0);
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
                    msg += "blank mac-address,";
                }
                if (id.length() == 0) {
                    msg += "blank app id,";
                }
            } else {
                //check whether app-id is in app-lookup.csv [invalid app]
                int appid;
                try {
                    appid = Integer.parseInt(id);
                    //System.out.println(appid);
                    if (!idSet.contains(appid)) {
                        isValid = false;
                        msg += "invalid app,";
                    }
                } catch (NumberFormatException e) {
                    isValid = false;
                    msg += "invalid app,";
                }
                //check mac-address format [invalid mac-address] --> 
                //check whether it's in demographics.csv [no matching mac-address]
                if (!adrs.matches("[a-fA-F0-9]{40}")) { //if (!adrs.matches("[\\dA-Fa-f]+"))
                    isValid = false;
                    msg += "invalid mac address,";
                } else {
                    if (!macSet.contains(adrs)) {
                        isValid = false;
                        msg += "no matching mac address,";
                    }
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
                    //find duplicate
                    int rowNum = data.get(key);//row number of duplicate row
                    String[] dupErr = {"duplicate row"};
                    errorMsg.add(new ErrorMessage("app.csv", rowNum, dupErr));
                    if (!isAdditional) {
                        usageTemp.remove(rowNum);
                    } else {
                        additionalData.remove(rowNum);
                    }

                }
                data.put(key, count);
                AppUsage u = new AppUsage(timestamp, adrs, Integer.parseInt(id));
                if (!isAdditional) {
                    usageTemp.put(count, u);
                } else {
                    additionalData.put(count, u);
                }

            } else {
                errorMsg.add(new ErrorMessage("app.csv", count, msg.split(",")));
            }
            count++;
        }
        
        appUsages = new ArrayList<AppUsage>(usageTemp.values());
    }
    
    /**
     *
     * @return a ArrayList of ErrorMessage
     */
    public ArrayList<ErrorMessage> bootstrap() {//throws SQLException {
        check(false);
        AppUsageDAO.bootstrap(appUsages, conn);
        return errorMsg;
    }

    /**
     * Add additional data and update the results after uploading new files
     *
     * @return an ArrayList of ErrorMessage Objects
     */
    public ArrayList<ErrorMessage> addAdditionalData() {
        check(true);
        ArrayList<ErrorMessage> errors = AppUsageDAO.update(additionalData, conn);
        additionalSize = additionalData.size() - errors.size();
        errorMsg.addAll(errors);
        return errorMsg;
    }
    
    /**
     * Get the total lines of additional app.csv files 
     *
     * @return an int value represents the total lines of additional app.csv files 
     */
    public int getAdditionalSize(){
        return additionalSize;
    }
}
