/*
 * Validation for app-lookup.csv which checks whether the app-id is a positive non-zero integer value
 * and whether the app category is valid
 */
package com.app.model;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author G4T6
 */
public class AppValidation {

    private ArrayList<ErrorMessage> errorMsg;
    private ArrayList<App> apps;
    private List<String[]> csvFile;
    private Connection conn;
    
    /**
     * Get the total lines of app-lookup.csv
     *
     * @return an int value represents the total lines of app-lookup.csv
     */
    public int getSize(){
        return apps.size();
    }

    /**
     * Construct a new AppValidation Object which consists of the following parameters
     *
     * @param csvFile an ArrayList of String arrays represents all lines in app-lookup.csv
     * @param conn the Connection Object passed in for sql statement
     */
    public AppValidation(List<String[]> csvFile, Connection conn) {
        errorMsg = new ArrayList<>();
        apps = new ArrayList<>();
        this.csvFile = csvFile;
        this.conn = conn;
    }

    /**
     * These methods check the data, delete the invalid data from the csvFile
     * list
     */
    public void check() {
        int count = 2;
        String[] header = csvFile.get(0);
        int iID = -1;
        int iName = -1;
        int iCate = -1;
        for(int i = 0; i < header.length; i++){
            String h = header[i];
            if(h.toLowerCase().trim().equals("app-id")){
                iID = i;
            }else if(h.toLowerCase().trim().equals("app-name")){
                iName = i;
            }else{
                iCate = i;
            }
        }
        for (int i = 1; i < csvFile.size(); i++) {
            String id = csvFile.get(i)[iID].trim();
            String name = csvFile.get(i)[iName].trim();
            String category = csvFile.get(i)[iCate].trim().toLowerCase();
            String msg = "";
            boolean isValid = true;
            //check app id
            if (id.length() == 0 || name.length() == 0 || category.length() == 0) {
                isValid = false;
                if (name.length() == 0) {
                    msg += "blank app-name,";
                }
                if (id.length() == 0) {
                    msg += "blank app-id,";
                }
                if (category.length() == 0) {
                    msg += "blank app category,";
                }
            } else {
                try {
                    int idNum = Integer.parseInt(id);
                    if (idNum <= 0) {
                        isValid = false;
                        msg += "invalid app id,";
                    }
                } catch (NumberFormatException e) {
                    isValid = false;
                    msg += "invalid app id,";
                }

                //check category
                // “Books”, “Social” , “Education”, “Entertainment”, “Information”, “Library”, “Local”, “Tools”, “Fitness”, “Games”, “Others”
                if (!(category.equals("books") || category.equals("social") || category.equals("education")
                        || category.equals("entertainment") || category.equals("information")
                        || category.equals("library") || category.equals("local") || category.equals("tools")
                        || category.equals("fitness") || category.equals("games") || category.equals("others"))) {
                    isValid = false;
                    msg += "invalid app category,";
                }
            }

            //delete invalid data
            if (!isValid) {
                errorMsg.add(new ErrorMessage("app-lookup.csv", count, msg.split(",")));
            } else {
                apps.add(new App(Integer.parseInt(id), name, category));
            }
            count++;
        }
    }

    //after check all the component in the csv file, return the error message to DataProcess.java
    /**
     *
     * @return an ArrayList of ErrorMessage Objects which represents all error messages from bootstrap
     */
    public ArrayList<ErrorMessage> bootstrap() {//throws SQLException {
        check();
        AppDAO.bootstrap(apps, conn);
        return errorMsg;
    }
}
