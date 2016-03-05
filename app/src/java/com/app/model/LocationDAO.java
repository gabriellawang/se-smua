
package com.app.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author G4-T6
 */
public class LocationDAO {
    
    /**
     *Check if semantic place exist
     * @param semanticPlace a String of the semantic place
     * @param conn a Connection
     * @return true if semantic place semantic place exist
     */
    public static boolean hasSemanticPlace(String semanticPlace, Connection conn){
        boolean hasPlace = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try{
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("select * from locationlookup where semanticplace=?");
            stmt.setString(1, semanticPlace);
            rs = stmt.executeQuery();
            while(rs.next()){
                hasPlace = true;
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return hasPlace;
    }
    
    /**
     *Get Semantic place given a location id
     * @param locationID A integer of location id
     * @param conn a Connection
     * @return the semantic place
     */
    public static String retrieveSemanticPlace(int locationID, Connection conn){
        String semanticPlace = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try{
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("select semanticeplace from locationlookup where locationid=?;");
            stmt.setInt(1, locationID);
            rs = stmt.executeQuery();
            while(rs.next()){
                semanticPlace = rs.getString(1);
            }
            conn.commit();
            stmt.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        return semanticPlace;
    }

    /**
     *Get HashMap of Semantic places with location id
     * @param conn a Connection
     * @return a HashMap containing the location Id and semantic place
     */
    public static HashMap<Integer, String> retrieveSemanticPlace(Connection conn){
        HashMap<Integer, String> semanticPlaces = new HashMap<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try{
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("select * from locationlookup;");
            rs = stmt.executeQuery();
            while(rs.next()){
                int id = rs.getInt(1);
                String place = rs.getString(2);
                semanticPlaces.put(id, place);
            }
            conn.commit();
            stmt.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        return semanticPlaces;
    }
    
    /**
     *Get a HashSet of location Id
     * @param conn a Connection
     * @return a HashSet of location Id
     */
    public static HashSet retrieveAllID(Connection conn) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        HashSet idSet = new HashSet();
        try {
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("select locationid from locationlookup;");
            rs = stmt.executeQuery();
            while (rs.next()) {
                int appid = rs.getInt(1);
                idSet.add(appid);
            }
            conn.commit();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return idSet;
    }

    /**
     *Bootstrap in locations
     * @param locations a ArrayList of of Location object
     * @param conn a Connection
     */
    public static void bootstrap(ArrayList<Location> locations, Connection conn) {
        PreparedStatement stmt = null;
        //int testIndex = 0;
        try {
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS locationlookup (locationid int(11) NOT NULL,"
                    + "semanticplace varchar(25) DEFAULT NULL,"
                    + "PRIMARY KEY (locationid));");
            stmt.execute();
            stmt = conn.prepareStatement("truncate table locationlookup;");//clear the previous data from last bootstrap
            stmt.executeUpdate();
            stmt = conn.prepareStatement("insert into locationlookup values(?,?);");
            int count = 0;
            for (Location l : locations) {
                int id = l.getLocationId();
                String place = l.getSemanticPlace();
                stmt.setInt(1, id);
                stmt.setString(2, place);
                stmt.addBatch();
                stmt.clearParameters();
                count++;
                if (count % 1000 == 0 || count == locations.size()) {
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

    /**
     *Gets Semantic places given a specific level
     * @param level a String of selected level
     * @param conn a Connection
     * @return a HashMap with key semantic place and ArrayList of location id
     */
    public static HashMap<String, ArrayList<Integer>> retrieveSemanticplaces(String level, Connection conn) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        HashMap<String, ArrayList<Integer>> semanticMap = new HashMap<String, ArrayList<Integer>>();
        try {
            conn.setAutoCommit(false);
            level="%"+level+"%"; 
            stmt = conn.prepareStatement("SELECT * FROM locationlookup where semanticplace like? ORDER BY locationlookup.semanticplace ASC;");
            stmt.setString(1, level);
            rs = stmt.executeQuery();
            while (rs.next()) {
                int locationId = rs.getInt(1);
                String semanticplace = rs.getString(2);
                if (semanticMap.get(semanticplace) == null) { // new entry
                    ArrayList<Integer> locationIdList = new ArrayList<Integer>();
                    locationIdList.add(locationId);
                    semanticMap.put(semanticplace, locationIdList);
                } else { //
                    ArrayList<Integer> locationIdList = semanticMap.get(semanticplace);
                    locationIdList.add(locationId);
                    semanticMap.put(semanticplace, locationIdList);
                }
            }
            conn.commit();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return semanticMap;
    }
}
