/*
 * This the utility for SocialActivenessReport 
 */
package com.app.controller;

import com.app.model.Activity;
import com.app.model.AppUsage;
import com.app.model.LocationDAO;
import com.app.model.LocationRecord;
import com.app.model.Pair;
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

/**
 *
 * @author G4-T6
 */
public class SocialActivenessUtility {

    /**
     * Calculate the application usage time of every application within a given
     * day, return a map with the application id as key and the application
     * usage time as value
     *
     * @param usages a list of AppUsage Objects
     * @param date a specific Date Object representing one day
     * @param socialId
     * @return a map with the application id as key and the application usage
     * time as value
     * @throws ParseException
     */
    public static HashMap<Integer, Long> getAppDuration(ArrayList<AppUsage> usages, String date, ArrayList<Integer> socialId) throws ParseException {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        TreeMap<Long, Integer> timeIdMap = new TreeMap<>();//date-appid
        long duration = 0;
        for (AppUsage u : usages) {
            String ts = u.getTimestamp();
            int id = u.getId();
            Date d = fmt.parse(ts);
            timeIdMap.put(d.getTime(), id);
        }
        HashMap<Integer, Long> appDuMap = new HashMap<>();
        //<appid, duration>
        Set<Long> timeSet = timeIdMap.keySet();
        Iterator timeIte = timeSet.iterator();
        long time1 = (long) timeIte.next();
        long time2;
        int id1 = timeIdMap.get(time1);
        int id2;
        if (!timeIte.hasNext()) {
            Date end = fmt.parse(date + " 23:59:59");
            long lastDif = (end.getTime() - time1) / 1000; //milisecond --> second
            if (lastDif > 120) {
                lastDif = 10;
            }
            duration += lastDif;
            if (socialId.contains(id1)) {
                if (appDuMap.get(id1) == null) {
                    appDuMap.put(id1, duration);
                } else {
                    long d = appDuMap.get(id1) + duration;
                    appDuMap.put(id1, d);
                }

            }
        } else {
            while (timeIte.hasNext()) {//go through the treemap<date, appid> of one mac address
                time2 = (long) timeIte.next();
                id2 = timeIdMap.get(time2);
                long dif = (time2 - time1) / 1000; // seconds
                if (dif > 120) {
                    dif = 10;
                }
                duration += dif;
                if (id1 != id2) {
                    if (socialId.contains(id1)) {
                        if (appDuMap.get(id1) == null) {
                            appDuMap.put(id1, duration);
                        } else {
                            long d = appDuMap.get(id1) + duration;
                            appDuMap.put(id1, d);
                        }
                        duration = 0;
                    }
                }

                if (!timeIte.hasNext()) {//the last record
                    Date end = fmt.parse(date + " 23:59:59");
                    long lastDif = (end.getTime() - time2) / 1000;
                    if (lastDif > 120) {
                        lastDif = 10;
                    }
                    duration += lastDif;
                    if (socialId.contains(id2)) {
                        if (appDuMap.get(id2) == null) {
                            appDuMap.put(id2, duration);
                        } else {
                            long d = appDuMap.get(id2) + duration;
                            appDuMap.put(id2, d);
                        }
                    }

                }
                id1 = id2;
                time1 = time2;
            }
        }
        return appDuMap;
    }

    /**
     * Given the usage time of every application, return the total usage time of
     * all applications
     *
     * @param appDuMap a map using application id as key and the usage time of
     * every application as value
     * @return total usage time of all applications
     */
    public static long getDuration(HashMap<Integer, Long> appDuMap) {
        Collection<Long> duCollect = appDuMap.values();
        Iterator duIte = duCollect.iterator();
        long totalDu = 0;
        while (duIte.hasNext()) {
            long dur = (long) duIte.next();
            totalDu += dur;
        }
        return totalDu;
    }

    /**
     * Given the list of LocationRecord Objects of all users, sort the list by
     * user's MAC address, return a map using MAC address as key
     *
     * @param records a list of LocationRecord Object
     * @return a map using MAC address as key and list of LocationRecord Object
     * as value
     */
    public static HashMap<String, ArrayList<LocationRecord>> sortRecordByUser(ArrayList<LocationRecord> records) {
        HashMap<String, ArrayList<LocationRecord>> userRecordMap = new HashMap<>();
        System.out.println("the size of records: " + records.size());
        for (LocationRecord r : records) {
            String macaddress = r.getMacAddress();
            if (userRecordMap.get(macaddress) == null) {
                ArrayList<LocationRecord> rList = new ArrayList<>();
                rList.add(r);
                userRecordMap.put(macaddress, rList);
            } else {
                ArrayList<LocationRecord> rList = userRecordMap.get(macaddress);
                rList.add(r);
                userRecordMap.put(macaddress, rList);
            }
        }
        return userRecordMap;
    }

    /**
     * Convert the LocationRecord Objects of every single user into Activity
     * Objects
     *
     * @param userRecordMap a map using user's MAC address as key and list of
     * LocationRecord Objects as value
     * @param date a specific Date representing a certain day chosen by the
     * logged-in user
     * @param conn a specific Connection Object
     * @return a map using MAC address as key and list of Activity Objects as
     * value
     * @throws ParseException
     */
    public static HashMap<String, ArrayList<Activity>> convert(HashMap<String, ArrayList<LocationRecord>> userRecordMap,
            String date, Connection conn) throws ParseException {
        HashMap<String, ArrayList<Activity>> userActivityMap = new HashMap<>();
        Set<String> macSet = userRecordMap.keySet();
        Iterator ite = macSet.iterator();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        HashMap<Integer, String> places = LocationDAO.retrieveSemanticPlace(conn);//<location id, semantice place>
        while (ite.hasNext()) {
            String mac = (String) ite.next();
            ArrayList<LocationRecord> records = userRecordMap.get(mac);
            ArrayList<Activity> activities = new ArrayList<>();
            LocationRecord r1 = records.get(0);
            LocationRecord r2;
            if (records.size() == 1) {//when this user only has one record
                String ts = r1.getTimestamp();
                int id = r1.getLocationId();
                Date start = fmt.parse(ts);
                Date end = fmt.parse(date + " 23:59:59");
                long s = start.getTime();
                long e = end.getTime();
                long dif = e - s;//miliseconds
                if (dif > 300000) {//if duration larger than 5mins
                    dif = 300000;
                    end = new Date(s + dif);
                }
                activities.add(new Activity(start, end, places.get(id), null));//when user considered leaving SIS building, semantice place just put NULL
            } else {//user has more than one record
                for (int i = 1; i < records.size(); i++) {
                    //inside this loop, no need to check user move or not. just convert every record into activity
                    r2 = records.get(i);
                    String ts1 = r1.getTimestamp();
                    String ts2 = r2.getTimestamp();
                    Date d1 = fmt.parse(ts1);
                    Date d2 = fmt.parse(ts2);
                    int id1 = r1.getLocationId();
                    int id2 = r2.getLocationId();
                    long s = d1.getTime();
                    long e = d2.getTime();
                    long dif = e - s;//milisecond
                    if (dif > 300000) {//larger than 5min -->leaving SIS
                        dif = 300000;
                        d2 = new Date(s + dif);
                        activities.add(new Activity(d1, d2, places.get(id1), null));
                    } else {
                        activities.add(new Activity(d1, d2, places.get(id1), places.get(id2)));
                    }

                    if (i == records.size() - 1) {//the last record
                        Date end = fmt.parse(date + " 23:59:59");
                        s = d2.getTime();
                        e = end.getTime();
                        dif = e - s;//miliseconds
                        if (dif > 300000) {
                            dif = 300000;
                            end = new Date(s + dif);
                        }
                        activities.add(new Activity(d2, end, places.get(id2), null));
                    }
                    r1 = r2;
                }
                int i = 0;
                while (i < activities.size()) {//merge the activities if user doesn' move
                    Activity a1 = activities.get(i);
                    if (a1.getCurrentLocation().equals(a1.getNextLocation())) {
                        //current location = next location --> user didn't move
                        Activity a2 = activities.get(i + 1);
                        a1.setNextLocation(a2.getNextLocation());
                        a1.setEndTime(a2.getEndTime());
                        activities.remove(i);
                        activities.add(i, a1);
                        activities.remove(i + 1);
                    } else {
                        i++;
                    }
                }
            }
            userActivityMap.put(mac, activities);
        }
        return userActivityMap;
    }

    /**
     * Give a certain date and the start time and end time of all grouping
     * activities, return the total time user spent in groups
     *
     * @param date a specific Date Object
     * @param groupLog a list of the start time and end time of all grouping
     * activities
     * @return the total time user spent in groups
     * @throws ParseException
     */
    public static long getGroupDuration(String date, ArrayList<Pair<Date, Date>> groupLog) throws ParseException {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date start = fmt.parse(date + " 00:00:00");
        Date end = fmt.parse(date + " 23:59:59");
        long t1 = start.getTime();
        long t2 = end.getTime();
        ArrayList<Long> timeline = new ArrayList<>();
        while (t1 <= t2) {
            timeline.add(t1);
            t1 += 1000;
        }
        for (int i = 0; i < groupLog.size(); i++) {
            Pair<Date, Date> p = groupLog.get(i);
            long time1 = p.getFirst().getTime();
            long time2 = p.getSecond().getTime();
            while (time1 <= time2) {
                timeline.remove(time1);
                time1 += 1000;
            }
        }
        int sec = timeline.size();//time during the whole day which is not with a group
        return (end.getTime() - start.getTime()) / 1000 - sec;
    }

    /**
     *
     * @param activities a ArrayList of Activity
     * @return the total duration of activities
     */
    public static long getTotalDuration(ArrayList<Activity> activities) {
        return (activities.get(activities.size() - 1).getEndTime().getTime() - activities.get(0).getStartTime().getTime()) / 1000 + 1;
    }
}
