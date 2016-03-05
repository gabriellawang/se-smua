/*
 * AppUsageDAO represents an ArrayList of AppUsage
 */
package com.app.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author G4T6
 */
public class AppUsageDAO {

    /**
     * Retrieve an ArrayList of AppUsage by given specific criteria of year,
     * school, gender and date
     *
     * @param year a String value represents year
     * @param school a String value represents school
     * @param gender a String value represents gender
     * @param date a String value represents date and time
     * @param conn the Connection Object passed in for sql statement
     * @return An ArrayList of AppUsage Object representing all AppUsage Objects
     * sort by year, school, gender and date
     */
    public static ArrayList<AppUsage> retrieveAppUsageGivenCriteria(String year, String school,
            String gender, String date, Connection conn) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        year = "%" + year + "%";
        school = "%" + school + "%";
        gender = "%" + gender + "%";
        date += "%";
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ArrayList<AppUsage> usageList = new ArrayList<>();
        
        try {
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("select timestamp, macaddress, appid from app AS a, demographics AS d where a.macaddress=d.mac_address "
                    + " and d.email like ? and d.email like ? and d.gender like ? and timestamp like ? order by timestamp asc;");
            stmt.setString(1, year);
            stmt.setString(2, school);
            stmt.setString(3, gender);
            stmt.setString(4, date);
            rs = stmt.executeQuery();

            while (rs.next()) {
                String timestamp = rs.getString(1);
                String macAddress = rs.getString(2);
                int appid = rs.getInt(3);
                usageList.add(new AppUsage(timestamp,macAddress,appid));
            }
            conn.commit();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return usageList;
    }

    /**
     * Retrieve an ArrayList of Date Object given a String value of MAC address
     *
     * @param macAddress a String value represents MAC address value
     * @param conn the Connection Object passed in for sql statement
     * @return an ArrayList of Date Object given a String value of MAC address
     */
    public static ArrayList<Date> retrieveDate(String macAddress, Connection conn) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ArrayList<Date> timeList = new ArrayList<>();
        try {
            conn.setAutoCommit(false);
            conn = ConnectionManager.getConnection();
            stmt = conn.prepareStatement("select timestamp from app where macaddress =?;");
            stmt.setString(1, macAddress);
            rs = stmt.executeQuery();

            while (rs.next()) {
                String timestamp = rs.getString(1);
                Date appTimestamp = fmt.parse(timestamp);
                timeList.add(appTimestamp);
            }
            conn.commit();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return timeList;
    }

    /**
     * Retrieve an ArrayList of AppUsage Objects between given start date and
     * end date
     *
     * @param startDate a String value represents the start date
     * @param endDate a String value represents the end date
     * @param conn the Connection Object passed in for sql statement
     * @return an ArrayList of AppUsage Objects between given start date and end
     * date
     */
    public static ArrayList<AppUsage> retrieve(String startDate, String endDate, Connection conn) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<AppUsage> uList = new ArrayList<>();
        startDate += " 00:00:00";
        endDate += " 23:59:59";
        try {
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("select * from app where timestamp>=? and timestamp<=?;");
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String ts = rs.getString(1);
                String mac = rs.getString(2);
                int appid = rs.getInt(3);
                uList.add(new AppUsage(ts, mac, appid));
            }
            conn.commit();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return uList;
    }

    /**
     * Retrieve an ArrayList of AppUsage Objects on a specific date and has a
     * certain MAC address value
     *
     * @param date a String represents a certain date
     * @param macaddress a String represents all the MAC address value
     * @param conn the Connection Object passed in for sql statement
     * @return an ArrayList of AppUsage Objects on a specific date and has a
     * certain MAC address value
     */
    public static ArrayList<AppUsage> retrieve(String date, Connection conn, String macaddress) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<AppUsage> uList = new ArrayList<>();
        date = date + "%";
        try {
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("select * from app where timestamp like ? and macaddress=?;");
            stmt.setString(1, date);
            stmt.setString(2, macaddress);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String ts = rs.getString(1);
                String mac = rs.getString(2);
                int appid = rs.getInt(3);
                uList.add(new AppUsage(ts, mac, appid));
            }

            conn.commit();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return uList;
    }

    /**
     * Retrieve an ArrayList of AppUsage Object given a specific MAC address
     * value, start date, end date and an ArrayList of app ids
     *
     * @param macaddress a String represents all the MAC address value
     * @param startDate a String value represents the start date
     * @param endDate a String value represents the end date
     * @param appId an ArryList of Integer which represents a multiple app ids
     * @param conn the Connection Object passed in for sql statement
     * @return an ArrayList of AppUsage Object given a specific MAC address
     * value, start date, end date and an ArrayList of app ids
     */
    public static ArrayList<AppUsage> retrieve(String macaddress, String startDate, String endDate,
            ArrayList<Integer> appId, Connection conn) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        startDate += " 00:00:00";
        endDate += " 23:59:59";
        ArrayList<AppUsage> uList = new ArrayList<>();
        System.out.println("size of app id: " + appId.size());
        try {
            conn.setAutoCommit(false);
            for (int id : appId) {
                stmt = conn.prepareStatement("select * from app where macaddress=? and appid=? and timestamp>=? and timestamp<=?;");
                stmt.setString(1, macaddress);
                stmt.setInt(2, id);
                stmt.setString(3, startDate);
                stmt.setString(4, endDate);
                rs = stmt.executeQuery();
                while (rs.next()) {
                    String ts = rs.getString(1);
                    String mac = rs.getString(2);
                    int appid = rs.getInt(3);
                    uList.add(new AppUsage(ts, mac, appid));
                }
            }
            conn.commit();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return uList;
    }

    /**
     * Retrieve an ArrayList of AppUsage Object given start date, end date and
     * an ArrayList of app ids
     *
     * @param startDate a String value represents the start date
     * @param endDate a String value represents the end date
     * @param appId an ArryList of Integer which represents a multiple app ids
     * @param conn the Connection Object passed in for sql statement
     * @return an ArrayList of AppUsage Object given start date, end date and an
     * ArrayList of app ids
     */
    public static ArrayList<AppUsage> retrieve(String startDate, String endDate, ArrayList<Integer> appId,
            Connection conn) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        startDate += " 00:00:00";
        endDate += " 23:59:59";
        ArrayList<AppUsage> uList = new ArrayList<>();
        try {
            conn.setAutoCommit(false);
            for (int id : appId) {
                stmt = conn.prepareStatement("select * from app where appid=? and timestamp>=? and timestamp<=?;");
                stmt.setInt(1, id);
                stmt.setString(2, startDate);
                stmt.setString(3, endDate);
                rs = stmt.executeQuery();
                while (rs.next()) {
                    String ts = rs.getString(1);
                    String mac = rs.getString(2);
                    int appid = rs.getInt(3);
                    uList.add(new AppUsage(ts, mac, appid));
                }
            }
            conn.commit();
            if(stmt!=null){
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return uList;
    }

    /**
     * Retrieve an ArrayList of AppUsage Object given start date, end date
     *
     * @param startDate A Date Object represents start date
     * @param endDate A DAte Object represents end date
     * @param conn the Connection Object passed in for sql statement
     * @return an ArrayList of AppUsage Object given start date, end date
     */
    public static ArrayList<AppUsage> retrieveAllAppUsageOnDates(Date startDate, Date endDate, Connection conn) {
        ArrayList<AppUsage> appUsageList = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Timestamp start = new Timestamp(startDate.getTime());
        Timestamp end = new Timestamp(endDate.getTime());
        try {
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("SELECT * FROM app WHERE timestamp BETWEEN ? AND ?;");
            stmt.setTimestamp(1, start);
            stmt.setTimestamp(2, end);
            rs = stmt.executeQuery();

            while (rs.next()) {
                String timestamp = rs.getString(1);
                String macAddr = rs.getString(2);
                int appid = rs.getInt(3);
                appUsageList.add(new AppUsage(timestamp, macAddr, appid));
            }
            conn.commit();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appUsageList;
    }

    /**
     * Retrieve an ArrayLIst of all AppUsage Objects in AppUsageDAO
     *
     * @param conn the Connection Object passed in for sql statement
     * @return an ArrayLIst of all AppUsage Objects in AppUsageDAO
     */
    public static ArrayList<AppUsage> retrieveAllAppUsage(Connection conn) {
        ArrayList<AppUsage> appUsageList = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("SELECT * FROM app;");
            rs = stmt.executeQuery();
            while (rs.next()) {
                String timestamp = rs.getString(1);
                String macAddr = rs.getString(2);
                int appid = rs.getInt(3);
                appUsageList.add(new AppUsage(timestamp, macAddr, appid));
            }
            conn.commit();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appUsageList;
    }
    
    /**
     * Retrieve an ArrayList of AppUsage Objects where the MAC address is
     * included in an ArrayList of String, given start date and end date
     *
     * @param macAddresses an ArrayList of MAC address values
     * @param startDate a String value represents the start date
     * @param endDate a String value represents the end date
     * @param conn the Connection Object passed in for sql statement
     * @return an ArrayList of AppUsage Objects where the MAC address is
     * included in an ArrayList of String, given start date and end date
     */
    public static ArrayList<AppUsage> retrieve(ArrayList<String> macAddresses,
            String startDate, String endDate, Connection conn) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        startDate += " 00:00:00";
        endDate += " 23:59:59";
        ArrayList<AppUsage> uList = new ArrayList<>();
        try {
            conn.setAutoCommit(false);
            for (String adr : macAddresses) {
                stmt = conn.prepareStatement("select * from app where macaddress=? and timestamp>=? and timestamp<=?;");
                stmt.setString(1, adr);
                stmt.setString(2, startDate);
                stmt.setString(3, endDate);
                System.out.println(stmt.toString());
                rs = stmt.executeQuery();
                while (rs.next()) {
                    String ts = rs.getString(1);
                    String mac = rs.getString(2);
                    int id = rs.getInt(3);
                    uList.add(new AppUsage(ts, mac, id));
                }
            }
            conn.commit();
            if(stmt!=null){
                stmt.close();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return uList;
    }

    /**
     * Bootstrap the data in app.csv and store the correct data into sql tables
     *
     * @param appUsages an ArrsyList of AppUsage Objects
     * @param conn the Connection Object passed in for sql statement
     */
    public static void bootstrap(ArrayList<AppUsage> appUsages, Connection conn) {
        PreparedStatement stmt = null;
        try {
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS app(timestamp timestamp,"
                    + "macaddress varchar(40), appid int(11),"
                    + "constraint tmpk PRIMARY KEY (timestamp,macaddress));");
            stmt.execute();
            stmt = conn.prepareStatement("truncate table app;");//clear the previous data from last bootstrap
            stmt.executeUpdate();
            stmt = conn.prepareStatement("insert into app values(?,?,?);");
            int count = 0;
            for (AppUsage au : appUsages) {
                String timestamp = au.getTimestamp();
                String macAddress = au.getMacAddress();
                int id = au.getId();
                stmt.setString(1, timestamp);
                stmt.setString(2, macAddress);
                stmt.setInt(3, id);
                stmt.addBatch();
                count++;
                stmt.clearParameters();
                if (count % 1000 == 0 || count == appUsages.size()) {
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
     * Add additional data from additional csv uploaded
     *
     * @param additionalData new uploaded csv files contain more AppUsage data
     * @param conn the Connection Object passed in for sql statement
     * @return an ArrayList of error messages appear during loading additional
     * data
     */
    public static ArrayList<ErrorMessage> update(TreeMap<Integer, AppUsage> additionalData, Connection conn) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<ErrorMessage> errors = new ArrayList<>();
        ArrayList<AppUsage> appUsages = new ArrayList<>();
        try {
            conn.setAutoCommit(false);
            Set<Integer> rowSet = additionalData.keySet();
            Iterator ite = rowSet.iterator();
            while (ite.hasNext()) {
                int rowNum = (Integer) ite.next();
                AppUsage appusage = additionalData.get(rowNum);
                String timestamp = appusage.getTimestamp();
                String macAddress = appusage.getMacAddress();
                stmt = conn.prepareStatement("select * from app where timestamp=? and macaddress=?;");
                stmt.setString(1, timestamp);
                stmt.setString(2, macAddress);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    String[] dupErr = {"duplicate row"};
                    errors.add(new ErrorMessage("app.csv", rowNum, dupErr));
                } else {
                    appUsages.add(appusage);
                }
            }
            stmt = conn.prepareStatement("insert into app values(?,?,?);");
            int count = 0;
            for (AppUsage au : appUsages) {
                String timestamp = au.getTimestamp();
                String macAddress = au.getMacAddress();
                int id = au.getId();
                stmt.setString(1, timestamp);
                stmt.setString(2, macAddress);
                stmt.setInt(3, id);
                stmt.addBatch();
                count++;
                stmt.clearParameters();
                if (count % 1000 == 0 || count == appUsages.size()) {
                    stmt.executeBatch();
                    stmt.clearBatch();
                }
            }
            conn.commit();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return errors;
    }

    /**
     * Retrieve an ArrayList of Date Objects given a specific value of MAC
     * address, start date and end date
     *
     * @param macAddress a String represents the MAC address value
     * @param startDate a String value represents the start date
     * @param endDate a String value represents the end date
     * @param conn the Connection Object passed in for sql statement
     * @return an ArrayList of Date Objects given a specific value of MAC
     * address, start date and end date
     */
    public static ArrayList<Date> retrieveTimeList(String macAddress, Date startDate, Date endDate, Connection conn) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Timestamp start = new Timestamp(startDate.getTime());
        Timestamp end = new Timestamp(endDate.getTime());
        ArrayList<Date> timeList = new ArrayList<>();
        try {
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("select timestamp from app where macaddress =? and timestamp BETWEEN ? AND ?;");
            stmt.setString(1, macAddress);
            stmt.setTimestamp(2, start);
            stmt.setTimestamp(3, end);
            rs = stmt.executeQuery();

            while (rs.next()) {
                String timestamp = rs.getString(1);
                Date appTimestamp = fmt.parse(timestamp);
                timeList.add(appTimestamp);
            }

            conn.commit();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return timeList;
    }

    /**
     *  Returns an ArrayList of String concatenated with timestamp and category
     * 
     * @param startDate a String value represents the start date
     * @param endDate a String value represents the end date
     * @param userMacAddr a String represents user's MAC address value
     * @param appMacAddr a String represents MAC address value of one application usage record
     * @param conn the Connection Object passed in for sql statement
     * @return an ArrayList of String concatenated with timestamp and category
     */
    public static ArrayList<String> retrieveTimestampAndCategory(Date startDate, Date endDate, String userMacAddr, String appMacAddr, Connection conn) {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        Timestamp start = new Timestamp(startDate.getTime());
        Timestamp end = new Timestamp(endDate.getTime());
        ArrayList<String> resultList = new ArrayList<>();

        try {
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("SELECT a.timestamp, b.appcategory FROM app AS a, applookup AS b"
                    + " WHERE b.appid = a.appid AND a.macaddress = ? AND a.macaddress = ?"
                    + " AND a.timestamp BETWEEN ? AND ?"
                    + " ORDER BY b.appcategory ASC, a.timestamp ASC;");
            stmt.setString(1, userMacAddr);
            stmt.setString(2, appMacAddr);
            stmt.setTimestamp(3, start);
            stmt.setTimestamp(4, end);
            rs = stmt.executeQuery();

            while (rs.next()) {
                String timestamp = rs.getString(1);
                String category = rs.getString(2);
                String result = timestamp + "," + category;
                resultList.add(result);
            }           
            conn.commit();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultList;
    }
}
