/*
 * Validation for location-lookup.csv which checks whether the location-id is a valid positive
 * non-zero integer value, whether the semantic place is in the format of "SMUSISL<level number>
 * <specific location>" or "SMUSISB<level number><specific location>"
 */
package com.app.model;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author G4T6
 */
public class LocationValidation {

    private ArrayList<ErrorMessage> errorMsg;
    private ArrayList<Location> locations;
    private List<String[]> csvFile;
    private Connection conn;
    
    /**
     *Gets the number of Locations
     * @return the number of location records
     */
    public int getSize(){
        return locations.size();
    }
    
    /**
     *Construct a LocationValidation Object with the following parameters
     * @param csvFile a List of String array
     * @param conn a Connection
     */
    public LocationValidation(List<String[]> csvFile, Connection conn) {
        errorMsg = new ArrayList<>();
        locations = new ArrayList<>();
        this.csvFile = csvFile;
        this.conn = conn;
    }

    //these methods check the data, delete the invalid data from the csvFile list
    //add the error message into the the errorMsg
    /**
     *
     */
    public void check() {
        int count = 2;
        String[] header = csvFile.get(0);
        int iID = -1;
        int iPlace = -1;
        for(int i = 0; i < header.length; i++){
            String h = header[i];
            if(h.toLowerCase().trim().equals("location-id")){
                iID = i;
            }else{
                iPlace = i;
            }
        }
        for (int i = 1; i < csvFile.size(); i++) {
            String id = csvFile.get(i)[iID].trim();
            String place = csvFile.get(i)[iPlace].trim().toUpperCase();
            String msg = "";
            boolean isValid = true;

            if (id.length() == 0 || place.length() == 0) {
                isValid = false;
                if (id.length() == 0) {
                    msg += "blank location id,";
                }
                if (place.length() == 0) {
                    msg += "blank semantic place,";
                }
            } else {
                //check app id
                try {
                    int idNum = Integer.parseInt(id);
                    if (idNum <= 0) {
                        isValid = false;
                        msg += "invalid location id,";
                    }
                } catch (NumberFormatException e) {
                    isValid = false;
                    msg += "invalid location id,";
                }

                //check the semantic place
                //the name of the semantic place in SIS-building. 
                //Should be of format "SMUSISL<level number><specific location>" or "SMUSISB<level number><specific location>"
                String sch = place.substring(0, 7);
                if (!sch.equals("SMUSISL") && !sch.equals("SMUSISB")) {//check the position of school
                    isValid = false;
                    msg += "invalid semantic place,";
                    //System.out.println("SMUSIS");
                } else {
                    String levelNum = "" + place.charAt(7);
                    int level;
                    try {
                        level = Integer.parseInt(levelNum);//check the level number
                        if (level <= 0 || level > 5) {
                            isValid = false;
                            msg += "invalid semantic place,";
                            //System.out.println("level");
                        }
                    } catch (NumberFormatException e) {
                        isValid = false;
                        msg += "invalid semantic place,";
                        //System.out.println("format");
                    }
                }
            }

            //delete invalid data
            if (!isValid) {
                errorMsg.add(new ErrorMessage("location-lookup.csv", count, msg.split(",")));
            } else {
                locations.add(new Location(Integer.parseInt(id), place));
                //System.out.println(id);
            }
            count++;
        }
    }

    //after check all the component in the csv file, return the error message to DataProcess.java
    /**
     *bootstrap data into database
     * @return a ArrayList of ErrorMessage
     */
    public ArrayList<ErrorMessage> bootstrap() {
        check();
        LocationDAO.bootstrap(locations, conn);
        return errorMsg;
    }
}
