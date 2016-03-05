/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.app.model;

/**
 *
 * @author G4T6
 */
public class User {

    private String macAddress;
    private String name;
    private String password;
    private String email;
    private char gender;
    private String cca;

    /**
     *Construct a User Object with the following parameters
     * @param macAddress a String
     * @param name a String
     * @param password a String
     * @param email a String
     * @param gender a char
     * @param cca a String
     */
    public User(String macAddress, String name, String password, String email, char gender, String cca) {
        this.macAddress = macAddress;
        this.name = name;
        this.password = password;
        this.email = email;
        this.gender = gender;
        this.cca = cca;
    }

    /**
     *Gets the macAddress of user
     * @return the macAddress of user
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     *Gets the name of user
     * @return the name of user
     */
    public String getName() {
        return name;
    }

    /**
     *Gets the password of user
     * @return the password of user
     */
    public String getPassword() {
        return password;
    }

    /**
     *Gets the email of user
     * @return the email of user
     */
    public String getEmail() {
        return email;
    }

    /**
     *Gets the gender of user
     * @return the gender of user
     */
    public char getGender() {
        return gender;
    }
    
    /**
     *Gets the cca of user
     * @return the cca of user
     */
    public String getCCA(){
        return cca;
    }

    /**
     *Gets the email id of user
     * @return the email id of user
     */
    public String getEmailID() {
        int index = email.indexOf("@");
        String id = email.substring(0, index);
        return id;
    }

    /**
     *Gets the year of user
     * @return the year of user
     */
    public int getYear() {
        int index2 = email.indexOf("@");
        int index1 = index2 - 4;
        String y = email.substring(index1, index2);
        int year;
        try {
            year = Integer.parseInt(y);
            return year;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     *Gets the school of user
     * @return the school of user
     */
    public String getSchool() {
        int index1 = email.indexOf("@");
        int index2 = email.indexOf(".", index1);
        String school = email.substring(index1 + 1, index2);
        return school;
    }
}
