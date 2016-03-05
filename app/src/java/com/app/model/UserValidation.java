/*
 * Validation for demographics.csv which checks if the value is a SHA-1 hash value, if the length
 * of the password is less than 8 characters or it include whitespace, if the email is not of the
 * proper format XXX.<year>@<school>.smu.edu.sg, if the gender is not either "M" or "F".
 */
package com.app.model;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author G4T6
 */
public class UserValidation {

    private ArrayList<ErrorMessage> errorMsg;
    private ArrayList<User> users;
    private List<String[]> csvFile;
    private Connection conn;
    
    /**
     *Gets the number of users
     * @return the number of users
     */
    public int getSize(){
        return users.size();
    }
    /**
     *Construct a UserValidation Object with the following parameters
     * @param csvFile a List of String array
     * @param conn a Connection
     */
    public UserValidation(List<String[]> csvFile, Connection conn) {
        errorMsg = new ArrayList<>();
        users = new ArrayList<>();
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
        int iAdrs = -1;
        int iName = -1;
        int iPsw = -1;
        int iEmail = -1;
        int iGen = -1;
        int iCCA = -1;
        for (int i = 0; i < header.length; i++) {
            String h = header[i];
            System.out.println(h);
            if (h.toLowerCase().trim().equals("mac-address")) {
                iAdrs = i;
            } else if (h.toLowerCase().trim().equals("name")) {
                iName = i;
            } else if (h.toLowerCase().trim().equals("password")) {
                iPsw = i;
            } else if (h.toLowerCase().trim().equals("email")) {
                iEmail = i;
            } else if (h.toLowerCase().trim().equals("gender")) {
                iGen = i;
            } else {
                iCCA = i;
            }
        }
        for (int i = 1; i < csvFile.size(); i++) {
            String adrs = csvFile.get(i)[iAdrs].trim();
            String name = csvFile.get(i)[iName].trim();
            String psw = csvFile.get(i)[iPsw].trim();
            String email = csvFile.get(i)[iEmail].trim().toLowerCase();
            String gender = csvFile.get(i)[iGen].trim().toUpperCase();
            String cca = csvFile.get(i)[iCCA].trim();
            String msg = "";
            boolean isValid = true;
            if (adrs.length() == 0 || name.length() == 0 || psw.length() == 0 || email.length() == 0 || gender.length() == 0) {
                isValid = false;
                if (adrs.length() == 0) {
                    msg += "blank mac-address,";
                }
                if (name.length() == 0) {
                    msg += "blank name,";
                }
                if (psw.length() == 0) {
                    msg += "blank password,";
                }
                if (email.length() == 0) {
                    msg += "blank email,";
                }
                if (gender.length() == 0) {
                    msg += "blank gender,";
                }
                if (cca.length() == 0) {
                    msg += "blank cca record,";
                }
            } else {
                //check mac address
                if (!adrs.matches("[a-fA-F0-9]{40}")) { //if (!adrs.matches("[\\dA-Fa-f]+"))
                    isValid = false;
                    msg += "invalid mac address,";
                }

                //check cca record
                if (cca.length() > 63) {
                    isValid = false;
                    msg += "cca record too long,";
                }

                //check password
                if (psw.length() < 8 || psw.contains(" ")) {
                    isValid = false;
                    msg += "invalid password,";
                }

                //check email 
                int index = email.indexOf("@");
                String id = email.substring(0, index);
                boolean validEmail = true;
                //check emailID (check first)
                if (!id.matches("^[a-zA-Z0-9.]*$")) {
                    validEmail = false;
                } else {
                    //check school
                    if (!email.contains("@")) {
                        validEmail = false;
                    } else {
                        int index1 = email.indexOf("@");
                        int index2 = email.indexOf(".", index1);
                        String s = email.substring(index1 + 1, index2);
                        if (!(s.equals("business") || s.equals("accountancy") || s.equals("sis")
                                || s.equals("economics") || s.equals("law") || s.equals("socsc"))) {//arraylist
                            validEmail = false;
                        } else {
                            //check .smu.edu.sg
                            String str = email.substring(index2);
                            if (!str.equals(".smu.edu.sg")) {
                                validEmail = false;
                            } else {
                                //check year
                                index2 = email.indexOf("@");
                                index1 = index2 - 4;
                                String y = email.substring(index1, index2);
                                int year;
                                try {
                                    year = Integer.parseInt(y);
                                    if (year < 2011 || year > 2015) {
                                        validEmail = false;
                                    }
                                } catch (NumberFormatException e) {
                                    validEmail = false;
                                }
                            }
                        }
                    }

                }
                if (!validEmail) {
                    isValid = false;
                    msg += "invalid email,";
                }
                //check gender
                if (!(gender.equals("M") || gender.equals("F"))) {
                    isValid = false;
                    msg += "invalid gender,";
                }
            }

            //delete invalid data
            if (!isValid) {
                errorMsg.add(new ErrorMessage("demographics.csv", count, msg.split(",")));
            } else {
                users.add(new User(adrs, name, psw, email, gender.charAt(0), cca));
            }
            count++;
        }
    }

    //after check all the component in the csv file, return the error message to DataProcess.java
    /**
     *Bootstrap data into database
     * @return a ArrayList of ErrorMessage
     */
    public ArrayList<ErrorMessage> bootstrap() {//throws SQLException {
        check();
        System.out.println(users.size());
        UserDAO.bootstrap(users, true, conn);
        return errorMsg;
    }

    /**
     *Add additionalData to the database
     * @return a ArrayList of ErrorMessage
     */
    public ArrayList<ErrorMessage> addAdditionalData() {
        check();
        UserDAO.bootstrap(users, false, conn);
        return errorMsg;
    }
}
