/*
 * This servlet retrieves individual user that is accessing to any particular apps by match the user's and app's macaddress. 
 * It then processes the data given a start date and end date (both inclusive). It accumlates the durations in seconds 
 * and determines with the if the seconds is more than 120 or less. If durations is less than 120 secs, it adds up that amount.
 * If the seconds is above 120 secs, it add up as 10 seconds.
 */
package com.app.controller;

import com.app.model.AppUsage;
import com.app.model.User;
import com.app.model.UserDAO;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author G4-T6
 */
public class BreakdownTimeDemoUtility {

    /**
     * Convert a HashMap contains a user Object and the corresponding app usage duration
     * into a HashMap contains a user Object and the user type 
     *
     * @param macDuMap A HashMap contains a user Object and the corresponding app usage duration
     * @param conn the Connection Object passed in for sql statement
     * @return a HashMap contains a user Object and the user type
     */
    public static HashMap<User, String> convert(HashMap<String, Float> macDuMap, Connection conn) {
        HashMap<User, String> userCateMap = new HashMap<>();
        Set<String> macSet = macDuMap.keySet();
        Iterator ite = macSet.iterator();
        while (ite.hasNext()) {
            String mac = (String) ite.next();
            float aveDuration = macDuMap.get(mac);
            User u = UserDAO.retrieveUserByMac(mac, conn);
            if (aveDuration >= 5 * 3600) {
                userCateMap.put(u, "Intense");
            } else if (aveDuration >= 1 * 3600) {
                userCateMap.put(u, "Normal");
            } else {
                userCateMap.put(u, "Mild");
            }
        }
        return userCateMap;
    }

    /**
     * Convert a HashMap contains a user Object and the user type into a HashMap of different user 
     * type and number of those types
     *
     * @param values A HashMap of key is User Object and value is user type
     * @return a HashMap of different user type and number of those types
     */
    public static HashMap<String, Integer> convertToCount(HashMap<User, String> values) {
        HashMap<String, Integer> counts = new HashMap<>();
        counts.put("Intense", 0);
        counts.put("Normal", 0);
        counts.put("Mild", 0);
        Set<User> set = values.keySet();
        Iterator ite = set.iterator();
        while(ite.hasNext()){
            User user = (User)ite.next();
            String cate = values.get(user);
            int count = counts.get(cate) + 1;
            counts.put(cate, count);
        }
        return counts;
    }

    /**
     * Get individual app usage duration given an ArrayList of AppUsage Object, start date and end date
     *
     * @param usages an ArrayList of AppUsage Object
     * @param startDate A String representing the start date and time individual app usage duration
     * @param endDate A String representing the end date and time
     * @return A HashMap of String and Float number representing 
     * @throws ParseException
     */
    public static HashMap<String, Float> getIndividualDuration(ArrayList<AppUsage> usages, String startDate, String endDate) throws ParseException {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        HashMap<String, TreeMap<Long, Integer>> usageMap = new HashMap<>();
        //HashMap<macaddress, TreeMap<date, appid>>
        for (AppUsage usage : usages) {
            String mac = usage.getMacAddress();
            String timestamp = usage.getTimestamp();
            Date date = fmt.parse(timestamp);
            int appid = usage.getId();
            if (usageMap.get(mac) == null) {//this mac address hasn't been put into the hashmap
                TreeMap<Long, Integer> tm = new TreeMap<>();
                tm.put(date.getTime(), appid);
                usageMap.put(mac, tm);
            } else {//this mac address already exists in the hashmap
                TreeMap<Long, Integer> tm = usageMap.get(mac);
                tm.put(date.getTime(), appid);
                usageMap.put(mac, tm);
            }
        }

        HashMap<String, Float> macDuMap = new HashMap<>();
        //<MacAddress, Duration>
        Set<String> macSet = usageMap.keySet();
        Iterator ite = macSet.iterator();

        while (ite.hasNext()) {//for a mac address:
            HashMap<Integer, Long> appDuMap = new HashMap<>();
            //<appid, duration>
            String mac = (String) ite.next();
            TreeMap<Long, Integer> tm = usageMap.get(mac);
            //<date, appid>
            Set<Long> timeSet = tm.keySet();
            Iterator timeIte = timeSet.iterator();
            long time1 = (long) timeIte.next();
            long time2;
            int id1 = tm.get(time1);
            int id2;
            long duration = 0;
            if (!timeIte.hasNext()) {
                Date end = fmt.parse(endDate + " 23:59:59");
                long lastDif = (end.getTime() - time1) / 1000;
                if (lastDif > 120) {
                    lastDif = 10;
                }
                duration += lastDif;
                if (appDuMap.get(id1) == null) {
                    appDuMap.put(id1, duration);
                } else {
                    long d = appDuMap.get(id1) + duration;
                    appDuMap.put(id1, d);
                }
            } else {
                while (timeIte.hasNext()) {//go through the treemap<date, appid> of one mac address
                    time2 = (long) timeIte.next();
                    id2 = tm.get(time2);
                    long dif = (time2 - time1) / 1000; // seconds
                    if (dif > 120) {
                        dif = 10;
                    }
                    duration += dif;
                    if (id1 != id2) {
                        if (appDuMap.get(id1) == null) {
                            appDuMap.put(id1, duration);
                        } else {
                            long d = appDuMap.get(id1) + duration;
                            appDuMap.put(id1, d);
                        }
                        duration = 0;
                    }

                    if (!timeIte.hasNext()) {//the last record
                        Date end = fmt.parse(endDate + " 23:59:59");
                        long lastDif = (end.getTime() - time2) / 1000;
                        if (lastDif > 120) {
                            lastDif = 10;
                        }
                        duration += lastDif;
                        if (appDuMap.get(id2) == null) {
                            appDuMap.put(id2, duration);
                        } else {
                            long d = appDuMap.get(id2) + duration;
                            appDuMap.put(id2, d);
                        }
                    }
                    id1 = id2;
                    time1 = time2;
                }
            }

            //System.out.println(appDuMap.size());
            Collection<Long> duCollect = appDuMap.values();
            Iterator duIte = duCollect.iterator();
            long totalDu = 0;
            while (duIte.hasNext()) {
                long dur = (long) duIte.next();
                totalDu += dur;
            }
            SimpleDateFormat fmt2 = new SimpleDateFormat("yyyy-MM-dd");
            Date d1 = fmt2.parse(startDate);
            Date d2 = fmt2.parse(endDate);

            long days = TimeUnit.MILLISECONDS.toDays(d2.getTime() - d1.getTime()) + 1;
            float averageDuration = (float) totalDu / days;
            macDuMap.put(mac, averageDuration);
        }
        return macDuMap;
    }
}
