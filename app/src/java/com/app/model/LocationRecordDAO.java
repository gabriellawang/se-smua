package com.app.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author G4-T6
 */
public class LocationRecordDAO {

    /**
     *Gets all the LocationRecord given the following parameters
     * @param startDate
     * @param endDate
     * @param conn
     * @return
     */
    public static ArrayList<LocationRecord> retrieveAllLocationsOnDates(Date startDate, Date endDate, Connection conn) {
        ArrayList<LocationRecord> locationRecordList = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Timestamp start = new Timestamp(startDate.getTime());
        Timestamp end = new Timestamp(endDate.getTime());
        try {
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("SELECT * FROM location WHERE timestamp BETWEEN ? AND ?;");
            stmt.setTimestamp(1, start);
            stmt.setTimestamp(2, end);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String timestamp = rs.getString(1);
                String macAddr = rs.getString(2);
                int location_id = rs.getInt(3);
                locationRecordList.add(new LocationRecord(timestamp, macAddr, location_id));
            }
            conn.commit();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return locationRecordList;
    }

    /**
     *Gets all the LocationRecord given a specific date
     * @param date
     * @param conn
     * @return
     */
    public static ArrayList<LocationRecord> retrieveByDate(String date, Connection conn) {
        ArrayList<LocationRecord> records = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        date = date + "%";
        try {
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("select * from location where timestamp like ? order by timestamp asc;");
            //the record will be stored in list by ascending order of timestamp;
            stmt.setString(1, date);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String ts = rs.getString(1);
                String mac = rs.getString(2);
                int id = rs.getInt(3);
                records.add(new LocationRecord(ts, mac, id));
            }
            conn.commit();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    /**
     *Bootstrap in ArrayList of LocationRecord
     * @param locationRecords
     * @param conn
     */
    public static void bootstrap(ArrayList<LocationRecord> locationRecords, Connection conn) {
        PreparedStatement stmt = null;
        try {
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("create table if not exists location(timestamp timestamp not null,macaddress varchar(40) "
                    + "not null,locationid int,constraint timemac_pk primary key(timestamp,macaddress));");
            stmt.execute();
            stmt = conn.prepareStatement("truncate table location;");//clear the previous data from last bootstrap
            stmt.executeUpdate();
            stmt = conn.prepareStatement("insert into location values(?,?,?);");
            int count = 0;
            for (LocationRecord lr : locationRecords) {
                String timestamp = lr.getTimestamp();
                String macAddress = lr.getMacAddress();
                int id = lr.getLocationId();
                stmt.setString(1, timestamp);
                stmt.setString(2, macAddress);
                stmt.setInt(3, id);
                stmt.addBatch();
                count++;
                stmt.clearParameters();
                if (count % 1000 == 0 || count == locationRecords.size()) {
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
     *update the database given additionalData 
     * @param additionalData
     * @param conn
     * @return
     */
    public static ArrayList<ErrorMessage> update(TreeMap<Integer, LocationRecord> additionalData, Connection conn) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<ErrorMessage> errors = new ArrayList<>();
        ArrayList<LocationRecord> locationRecords = new ArrayList<>();
        try {
            conn.setAutoCommit(false);
            Set<Integer> rowSet = additionalData.keySet();
            Iterator ite = rowSet.iterator();
            while (ite.hasNext()) {
                int rowNum = (Integer) ite.next();
                LocationRecord locationRecord = additionalData.get(rowNum);
                String timestamp = locationRecord.getTimestamp();
                String macAddress = locationRecord.getMacAddress();
                stmt = conn.prepareStatement("select * from location where timestamp=? and macaddress=?;");
                stmt.setString(1, timestamp);
                stmt.setString(2, macAddress);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    String[] dupErr = {"duplicate row"};
                    errors.add(new ErrorMessage("location.csv", rowNum, dupErr));
                } else {
                    locationRecords.add(locationRecord);
                }
            }
            stmt = conn.prepareStatement("insert into location values(?,?,?);");
            int count = 0;
            for (LocationRecord lr : locationRecords) {
                String timestamp = lr.getTimestamp();
                String macAddress = lr.getMacAddress();
                int id = lr.getLocationId();
                stmt.setString(1, timestamp);
                stmt.setString(2, macAddress);
                stmt.setInt(3, id);
                stmt.addBatch();
                count++;
                stmt.clearParameters();
                if (count % 1000 == 0 || count == locationRecords.size()) {
                    stmt.executeBatch();
                    stmt.clearBatch();
                }
            }
            conn.commit();
            stmt.close();
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return errors;
    }

    /**
     *Deletes the ArrayList of LocationRecord
     * @param locationRecords
     * @param conn
     * @return
     */
    public static HashMap<Boolean, Integer> delete(ArrayList<LocationRecord> locationRecords, Connection conn) {
        HashMap<Boolean, Integer> result = new HashMap<>();
        result.put(true, 0);
        result.put(false, 0);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn.setAutoCommit(false);
            for (LocationRecord record : locationRecords) {
                String timestamp = record.getTimestamp();
                String macadrs = record.getMacAddress();

                stmt = conn.prepareStatement("select * from location where timestamp=? and macaddress=?;");
                stmt.setString(1, timestamp);
                stmt.setString(2, macadrs);

                rs = stmt.executeQuery();
                if (rs.next()) {
                    rs.beforeFirst();
                    stmt = conn.prepareStatement("delete from location where timestamp=? and macaddress=?;");
                    stmt.setString(1, timestamp);
                    stmt.setString(2, macadrs);
                    stmt.execute();
                    int count = result.get(true);
                    count++;
                    result.put(true, count);

                } else {
                    int count = result.get(false);
                    count++;
                    result.put(false, count);
                }
            }
            conn.commit();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     *Delete the data input
     * @param input
     * @param conn
     * @return
     */
    public static ArrayList<LocationRecord> delete(HashMap<String, String> input, Connection conn){
        ArrayList<LocationRecord> deleteRecord = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = "select l.locationid,macaddress,timestamp from location l,locationlookup lk where l.locationid=lk.locationid and timestamp>=? ";
        String start = input.get("start");
        String end = input.get("end");
        if(end != null){
            sql += "and l.timestamp<? ";
        }
        String mac = input.get("mac");
        if(mac != null){
            sql += "and l.macaddress=? ";
        }
        String id = input.get("id");
        if(id != null){
            sql += "and l.locationid=? ";
        }
        String semantic = input.get("semantic");
        if(semantic != null){
            sql += "and lk.semanticplace=? ";
        }
        sql += "order by l.locationid,macaddress,timestamp asc;";
        try{
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, start);
            int count = 2;
            if(end != null){
                stmt.setString(count, end);
                count++;
            }
            if(mac != null){
                stmt.setString(count, mac);
                count++;
            }
            if(id != null){
                stmt.setString(count, id);
                count++;
            }
            if(semantic != null){
                stmt.setString(count, semantic);
            }
            rs = stmt.executeQuery();
            while(rs.next()){
                int locationID = rs.getInt(1);
                String macaddress = rs.getString(2);
                String ts = rs.getString(3);
                ts = ts.substring(0, ts.length() - 2);
                deleteRecord.add(new LocationRecord(ts, macaddress, locationID));
            }
            stmt = conn.prepareStatement("delete from location where timestamp=? and macaddress=? and locationid=?");
            count = 0;
            for(LocationRecord lr : deleteRecord){
                String t = lr.getTimestamp();
                String m = lr.getMacAddress();
                int i = lr.getLocationId();
                stmt.setString(1, t);
                stmt.setString(2, m);
                stmt.setInt(3, i);
                stmt.addBatch();
                count++;
                
                stmt.clearParameters();
                if (count % 1000 == 0 || count == deleteRecord.size()) {
                    stmt.executeBatch();
                    stmt.clearBatch();
                }
            }
            conn.commit();
            stmt.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        return deleteRecord;
    }
}
