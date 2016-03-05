/*
 * This the utility for SmartphoneOveruseReport 
 */
package com.app.controller;

import com.app.model.AppUsage;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author G4-T6
 */
public class SmartphoneOveruseUtility {

    /**
     *
     * @param usages ArrayList of AppUsage
     * @param endDate String user input endDate
     * @param days long days required for frequency count
     * @return the frequency of AppUsage
     * @throws ParseException Signals that an error has been reached
     * unexpectedly while parsing.
     */
    public static float getFrequency(ArrayList<AppUsage> usages, String endDate, long days) throws ParseException {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        TreeSet<Long> timeSet = new TreeSet<>();
        if (usages.size() == 0) {
            return 0;
        }
        for (AppUsage usage : usages) {
            String timestamp = usage.getTimestamp();
            Date date = fmt.parse(timestamp);
            timeSet.add(date.getTime());
        }

        Iterator timeIte = timeSet.iterator();
        long time1 = (long) timeIte.next();
        long time2;
        Date d1 = new Date(time1);
        int count = 0;//count the number of sessions
        long duration = 0;//used to count the duration, check whether <= 120
        Date end = fmt.parse(endDate + " 23:59:59");
        if (!timeIte.hasNext()) { // only has 1 app usage
            count += 1;
        } else {
            while (timeIte.hasNext()) {
                time2 = (long) timeIte.next();
                Date d2 = new Date(time2);
                if (d1.getHours() != d2.getHours()) { // check if it's within that same hour 
                    Date temp = d1;
                    temp.setMinutes(59);
                    temp.setSeconds(59);
                    long diff = (temp.getTime() - time1) / 1000;
                    if (diff > 120) {
                        count ++;
                    }
                } else { //within the same hour
                    long dif = (time2 - time1) / 1000; // seconds
                    if (dif > 120) {
                        count++;
                    }

                    if (!timeIte.hasNext()) {//the last record
                        Date temp = d2;
                        temp.setMinutes(59);
                        temp.setSeconds(59);
                        long diff = (temp.getTime() - time2) / 1000;
                        if (diff > 120) {
                            count++;
                        }
                    }
                }
                d1 = d2;
                time1 = time2;
            }
        }
        float f = (float) count / (days * 24);
        return f;
    }

    /**
     *
     * @param usages ArrayList of AppUsage
     * @param endDate String user input endDate
     * @return the duration of AppUsage
     * @throws ParseException Signals that an error has been reached
     * unexpectedly while parsing.
     */
    public static long getDuration(ArrayList<AppUsage> usages, String endDate) throws ParseException {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        TreeMap<Long, Integer> usageMap = new TreeMap<>();
        if (usages.size() == 0) {
            return 0;
        }
        for (AppUsage usage : usages) {
            String timestamp = usage.getTimestamp();
            Date date = fmt.parse(timestamp);
            int appid = usage.getId();
            usageMap.put(date.getTime(), appid);
        }
        System.out.println("usageMap size" + usageMap.size());

        HashMap<Integer, Long> appDuMap = new HashMap<>();
        Set<Long> timeSet = usageMap.keySet();
        Iterator timeIte = timeSet.iterator();
        long time1 = (long) timeIte.next();
        long time2;
        int id1 = usageMap.get(time1);
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
                id2 = usageMap.get(time2);
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

        System.out.println("appDuMap size" + appDuMap.size());
        Collection<Long> duCollect = appDuMap.values();
        Iterator duIte = duCollect.iterator();
        long totalDu = 0;
        while (duIte.hasNext()) {
            long dur = (long) duIte.next();
            totalDu += dur;
        }
        System.out.println(totalDu);
        return totalDu;
    }

    /**
     *
     * @param start String input start date
     * @param end String input end date
     * @return the number of days
     * @throws ParseException Signals that an error has been reached
     * unexpectedly while parsing.
     */
    public static long getDays(String start, String end) throws ParseException {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        Date d1 = fmt.parse(start);
        Date d2 = fmt.parse(end);

        long days = TimeUnit.MILLISECONDS.toDays(d2.getTime() - d1.getTime()) + 1;
        return days;
    }
}
