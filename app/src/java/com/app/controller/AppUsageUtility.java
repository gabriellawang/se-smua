/*
 * This is AppUsageUtility.java which helps controller to process logic
 */
package com.app.controller;

import com.app.model.*;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author G4T6
 */
public class AppUsageUtility {

    /**
     *
     */
    public final String[] schools = {"business", "accountancy", "sis", "economics", "law", "socsc"};

    /**
     *
     */
    public final char[] gender = {'M', 'F'};

    /**
     * Retrieve a HashMap represents a specific user and his/her total app usage duration 
     * given the start date and end date
     *
     * @param startDate start date to retrieve data
     * @param endDate end date to retrieve data
     * @param conn the Connection Object passed in for sql statement
     * @return a HashMap of all the users with their app usage times given the time period
     */
    public static HashMap<User, Integer> getIndividualUsage(String startDate, String endDate, Connection conn) {

        HashMap<User, Integer> results = new HashMap<User, Integer>();

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startDateFmt = null;
        Date endDateFmt = null;
        try {
            startDateFmt = fmt.parse(startDate);
            endDateFmt = fmt.parse(endDate);
        } catch (ParseException ex) {
            Logger.getLogger(AppUsageUtility.class.getName()).log(Level.SEVERE, null, ex);
        }

        ArrayList<User> userList = UserDAO.retrieveAll(conn);
        ArrayList<AppUsage> appusageList = AppUsageDAO.retrieveAllAppUsageOnDates(startDateFmt, endDateFmt, conn);

        if (appusageList.size() == 0) {
            return null;
        }

        int intenseUser = 0;
        int normalUser = 0;
        int mildUser = 0;

        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userMacAddr = user.getMacAddress();

            for (int x = 0; x < appusageList.size(); x++) {
                AppUsage appusage = appusageList.get(x);
                String appMacAddr = appusage.getMacAddress();
                if (userMacAddr.equals(appMacAddr)) {
                    User uniqueUser = UserDAO.retrieveUserByMacAddress(userMacAddr, conn);
                    int countSecs = 10; // Assume user always ends an app
                    ArrayList<Date> timeList = AppUsageDAO.retrieveDate(appMacAddr, conn);
                    Collections.sort(timeList);
                    Date timeBeforeUsed = timeList.get(0);
                    long daysTp = timeList.get(timeList.size() - 1).getTime() - timeList.get(0).getTime();
                    long days = TimeUnit.MILLISECONDS.toDays(daysTp);
                    if (days < 1) {
                        days = 1;
                    }

                    for (int y = 1; y < timeList.size(); y++) {
                        Date timeAfterUsed = timeList.get(y);

                        try {
                            long diff = timeAfterUsed.getTime() - timeBeforeUsed.getTime();
                            //in seconds
                            long diffSeconds = diff / (1000);

                            if (diffSeconds < 121) {
                                countSecs += diffSeconds;
                            } else if (diffSeconds > 120) {
                                countSecs += 10;
                            }

                            timeBeforeUsed = timeAfterUsed;

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    countSecs /= days;
                    if (countSecs < 3600) {
                        mildUser++;
                    } else if (countSecs > 18000) {
                        intenseUser++;
                    } else {
                        normalUser++;
                    }
                    results.put(uniqueUser, countSecs);
                    break;
                }
            }
        }
        return results;
    }

    /**
     * Retrieve a HashMap represents the user type and number of these type of users
     *
     * @param startDate start date to retrieve data
     * @param endDate end date to retrieve data
     * @param conn the Connection Object passed in for sql statement
     * @return a HashMap of all the different categories of users with the
     * number of users given the time period
     */
        public static HashMap<String, Integer> breakdownByUsageTimeCategory(String startDate, String endDate, Connection conn) {

        HashMap<String, Integer> results = new HashMap<String, Integer>();

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        startDate += " 00:00:00";
        endDate += " 23:59:59";
        Date startDateFmt = null;
        Date endDateFmt = null;
        try {
            startDateFmt = fmt.parse(startDate);
            endDateFmt = fmt.parse(endDate);
        } catch (ParseException ex) {
            Logger.getLogger(AppUsageUtility.class.getName()).log(Level.SEVERE, null, ex);
        }

        ArrayList<User> userList = UserDAO.retrieveAll(conn);
        ArrayList<AppUsage> appusageList = AppUsageDAO.retrieveAllAppUsageOnDates(startDateFmt, endDateFmt, conn);
        
        int intenseUser = 0;
        int normalUser = 0;
        int mildUser = 0;
        
        if (appusageList.size() == 0) {
            results.put("intenseUser", intenseUser);
            results.put("normalUser", normalUser);
            results.put("mildUser", mildUser);
            return results;
        }
  
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userMacAddr = user.getMacAddress();

            for (int x = 0; x < appusageList.size(); x++) {
                AppUsage appusage = appusageList.get(x);
                String appMacAddr = appusage.getMacAddress();
                if (userMacAddr.equals(appMacAddr)) {

                    int countSecs = 10; // Assume user always ends an app
                    ArrayList<Date> timeList = AppUsageDAO.retrieveTimeList(appMacAddr, startDateFmt, endDateFmt, conn);
                    Collections.sort(timeList);
                    Date timeBeforeUsed = timeList.get(0);
                    long daysTp = timeList.get(timeList.size() - 1).getTime() - timeList.get(0).getTime();
                    long days = TimeUnit.MILLISECONDS.toDays(daysTp);
                    if (days < 1) {
                        days = 1;
                    }
                    for (int y = 1; y < timeList.size(); y++) {
                        Date timeAfterUsed = timeList.get(y);
                        try {
                            long diff = timeAfterUsed.getTime() - timeBeforeUsed.getTime();
                            //in seconds
                            long diffSeconds = diff / (1000);
                            //long diffSeconds = TimeUnit.MILLISECONDS.toSeconds(diff);
                            if (diffSeconds < 121) {
                                countSecs += diffSeconds;
                            } else if (diffSeconds > 120) {
                                countSecs += 10;
                            }
                            timeBeforeUsed = timeAfterUsed;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    
                    countSecs /= days;
                    if (countSecs <= 3600) {
                        mildUser++;
                    } else if (countSecs >= 18000) {
                        intenseUser++;
                    } else {
                        normalUser++;
                    }
                    break;
                }
            }
        }
        results.put("intenseUser", intenseUser);
        results.put("normalUser", normalUser);
        results.put("mildUser", mildUser);
        
        return results;
    }

}
