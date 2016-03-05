/*
 * AppDAO represents An ArrayList of App Objects
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

public class AppDAO {
    
    /**
     * Retrieve the name of an app providing the appid value
     *
     * @param appid the appid of another app Object
     * @param conn the connection Object passed in for sql statement
     * @return the name of an app Object which has a specific appid
     */
    public static String retrieveName(int appid, Connection conn){
        String name = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try{
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("select appname from applookup where appid=?;");
            stmt.setInt(1, appid);
            rs = stmt.executeQuery();
            while(rs.next()){
                name = rs.getString("appname");
            }
            conn.commit();
            stmt.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        return name;
    }
    
    /**
     * Return an String ArrayList of all categories in AppDAO
     *
     * @param conn connection Object passed in for sql statement
     * @return an ArrayList of String representing all categories in AppDAO
     */
    public static ArrayList<String> retrieveAllAppCategory(Connection conn){
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<String> catList = new ArrayList<>();
        try{
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("select distinct(appcategory) from applookup;");
            rs = stmt.executeQuery();
            while (rs.next()) {
                String category = rs.getString(1);
                catList.add(category);
            }
            conn.commit();
            stmt.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        return catList;
    }
    
    /**
     * Return a HashSet of all id values in AppDAO
     *
     * @param conn the connection Object passed in for sql statement
     * @return a HashSet of integers representing all id values in AppDAO
     */
    
    public static HashSet retrieveAllID(Connection conn) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        HashSet idSet = new HashSet();
        try {
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("select appid from applookup;");
            rs = stmt.executeQuery();

            while (rs.next()) {
                int appid = rs.getInt(1);
                idSet.add(appid);
            }
            conn.commit();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("bug occurs when read app from app-lookup.csv");
            System.out.println(e.toString());
        }
        return idSet;
    }
    
    /**
     * Return an ArrayList of integers representing the id of the app which has a certain category
     *
     * @param category a String value represents the category of app
     * @param conn the Connection Object passed in for sql statement
     * @return an ArrayList of integers representing the id of the app which has a certain category
     */
    public static ArrayList<Integer> retrieveID(String category, Connection conn){
        PreparedStatement stmt = null;
        ResultSet rs = null;
        category = category.toLowerCase();
        ArrayList<Integer> idList = new ArrayList<>();
        try{
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("select appid from applookup where appcategory=?;");
            stmt.setString(1, category);
            rs = stmt.executeQuery();
            while (rs.next()) {
                int appid = rs.getInt(1);
                idList.add(appid);
            }
            conn.commit();
            stmt.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        return idList;
    }

    /**
     * bootstrap all data from applookup.csv and load all correct data into sql tables
     *
     * @param apps an ArrayList of App Object
     * @param conn the connection Object passed in for sql statement
     */
    public static void bootstrap(ArrayList<App> apps, Connection conn) {
        PreparedStatement stmt = null;
        //int testIndex = 0;
        try {
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS applookup(appid int(11) NOT NULL,"
                    + "appname varchar(60) CHARACTER SET utf8 COLLATE utf8_general_mysql500_ci DEFAULT NULL,"
                    + "appcategory varchar(13) CHARACTER SET latin1 DEFAULT NULL,"
                    + "PRIMARY KEY (appid));");
            stmt.execute();
            stmt = conn.prepareStatement("truncate table applookup;");//clear the previous data from last bootstrap
            stmt.executeUpdate();
            stmt = conn.prepareStatement("insert into applookup values(?,?,?);");
            
            int count = 0;
            for (App a : apps) {
                int id = a.getId();
                //System.out.println(id);
                String name = a.getName();
                String category = a.getCategory();
                //testIndex++;
                stmt.setInt(1, id);
                stmt.setString(2, name);
                stmt.setString(3, category);
                stmt.addBatch();
                count++;
                stmt.clearParameters();
                if(count % 1000 == 0 || count == apps.size()){
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
