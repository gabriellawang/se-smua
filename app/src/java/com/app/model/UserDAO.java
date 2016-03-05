/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.app.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author G4T6
 */
public class UserDAO {
    
    /**
     *Returns all CCA record
     * @param conn a Connection
     * @return the String array of cca records
     */
    public static String[] retrieveCCA(Connection conn){
        PreparedStatement stmt = null;
        ResultSet rs  = null;
        String cca = "";
        String[] ccas = null;
        try{
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("select distinct cca from demographics;");
            rs = stmt.executeQuery();
            ArrayList<String> ccaList = new ArrayList();
            while(rs.next()){
                ccaList.add(rs.getString("cca"));
            }
            ccas = new String[ccaList.size()];
            ccas = ccaList.toArray(ccas);
            conn.commit();
            stmt.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        return ccas;
    }

    /**
     *Gets the school of user with the given macaddress
     * @param macaddress a String
     * @param conn a Connection
     * @return the school 
     */
    public static String retrieveSchool(String macaddress, Connection conn) {
        String school = "";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("select * from demographics where mac_address=?;");
            stmt.setString(1, macaddress);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String adrs = rs.getString(1);
                String name = rs.getString(2);
                String password = rs.getString(3);
                String email = rs.getString(4);
                char gender = rs.getString(5).charAt(0);
                String cca = rs.getString(6);
                User u = new User(adrs, name, password, email, gender, cca);
                school = u.getSchool();
            }
            conn.commit();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return school;
    }

    /**
     *Gets the user by macAddress
     * @param macAddress a String
     * @param conn a Connection
     * @return the User
     */
    public static User retrieveUserByMac(String macAddress, Connection conn) {
        User user = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("select * from demographics where mac_address=?;");
            stmt.setString(1, macAddress);
            rs = stmt.executeQuery();
            ArrayList<User> uList = new ArrayList<>();

            while (rs.next()) {
                String adrs = rs.getString(1);
                String name = rs.getString(2);
                String password = rs.getString(3);
                String email = rs.getString(4);
                char gender = rs.getString(5).charAt(0);
                String cca = rs.getString(6);
                uList.add(new User(adrs, name, password, email, gender, cca));
            }
            if(uList.size() == 0){
                user = null;
            }else{
                user = uList.get(0);
            }
            
            conn.commit();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    /**
     *Gets all the User 
     * @param conn a Connection
     * @return a ArrayList of User
     */
    public static ArrayList<User> retrieveAll(Connection conn) {
        User user = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<User> uList = new ArrayList<>();
        try {
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("select * from demographics");
            rs = stmt.executeQuery();
            while (rs.next()) {
                String adrs = rs.getString(1);
                String name = rs.getString(2);
                String password = rs.getString(3);
                String email = rs.getString(4);
                char gender = rs.getString(5).charAt(0);
                String cca = rs.getString(6);
                uList.add(new User(adrs, name, password, email, gender, cca));
            }
            conn.commit();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return uList;
    }

    /**
     *Gets the name of the user given a macaddress
     * @param macaddress a String
     * @param conn a Connection
     * @return the name of user with macaddress 
     */
    public static String retrieveName(String macaddress, Connection conn) {
        String name = "";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("select name from demographics where mac_address=?;");
            stmt.setString(1, macaddress);
            rs = stmt.executeQuery();
            while (rs.next()) {
                name = rs.getString("name");
            }
            conn.commit();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return name;
    }

    /**
     *Gets the user given a email id
     * @param emailID a String
     * @param conn a Connection
     * @return the User
     */
    public static User retrieveUser(String emailID, Connection conn) {
        User user = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        emailID = emailID + "@%";
        try {
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("select * from demographics where email like?;");
            stmt.setString(1, emailID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String adrs = rs.getString(1);
                String name = rs.getString(2);
                String password = rs.getString(3);
                String email = rs.getString(4);
                char gender = rs.getString(5).charAt(0);
                String cca = rs.getString(6);
                user = new User(adrs, name, password, email, gender, cca);
            }
            conn.commit();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    /**
     *Gets the user given the macAddress
     * @param macAddress a String
     * @param conn a Connection
     * @return the User
     */
    public static User retrieveUserByMacAddress(String macAddress, Connection conn) {
        User user = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("select * from demographics where mac_address=?;");
            stmt.setString(1, macAddress);
            rs = stmt.executeQuery();
            ArrayList<User> uList = new ArrayList<>();
            while (rs.next()) {
                String adrs = rs.getString(1);
                String name = rs.getString(2);
                String password = rs.getString(3);
                String email = rs.getString(4);
                char gender = rs.getString(5).charAt(0);
                String cca = rs.getString(6);
                uList.add(new User(adrs, name, password, email, gender, cca));
            }
            user = uList.get(0);
            conn.commit();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    /**
     *Gets the HashSet of all macaddress
     * @param conn a Connection
     * @return the HashSet of all the macAddress
     */
    public static HashSet retrieveAllMacAddress(Connection conn) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        HashSet macSet = new HashSet();
        try {
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("select mac_address from demographics;");
            rs = stmt.executeQuery();
            while (rs.next()) {
                String adrs = rs.getString(1);
                macSet.add(adrs);
            }
            conn.commit();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return macSet;
    }

    /**
     *Gets the list of Macaddress given a school
     * @param school a String
     * @param conn a Connection
     * @return a ArrayList of macAddress with specified school 
     */
    public static ArrayList<String> retrieveMacAddress(String school, Connection conn) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<String> mList = new ArrayList<>();
        school = "%" + school + "%";
        try {
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("select mac_address from demographics where email like ?;");
            stmt.setString(1, school);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String adrs = rs.getString(1);
                mList.add(adrs);
            }
            conn.commit();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mList;
    }

    /**
     *Bootstrap data given a list of users
     * @param users a ArrayList of User
     * @param clearTable a boolean
     * @param conn a Connection
     */
    public static void bootstrap(ArrayList<User> users, boolean clearTable, Connection conn) {//throws SQLException {
        PreparedStatement stmt = null;
        try {
            conn.setAutoCommit(false);
            if (clearTable) {
                stmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS demographics(mac_address char(40),"
                        + "name varchar(30),password varchar(20),email varchar(50),gender char(1),cca char(63),"
                        + "constraint tmpk PRIMARY KEY (mac_address, email));");
                stmt.execute();
                stmt = conn.prepareStatement("truncate table demographics;");
                stmt.executeUpdate();
            }
            stmt = conn.prepareStatement("insert into demographics values(?,?,?,?,?,?);");
            int count = 0;
            for (User u : users) {
                String adrs = u.getMacAddress();
                String name = u.getName();
                String psw = u.getPassword();
                String email = u.getEmail();
                char gender = u.getGender();
                String cca = u.getCCA();
                stmt.setString(1, adrs);
                stmt.setString(2, name);
                stmt.setString(3, psw);
                stmt.setString(4, email);
                stmt.setString(5, "" + gender);
                stmt.setString(6, cca);
                stmt.addBatch();
                count++;
                stmt.clearParameters();
                if (count % 1000 == 0 || count == users.size()) {
                    stmt.executeBatch();
                    stmt.clearBatch();
                }
            }
            conn.commit();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
