/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.app.json;

import com.app.model.AppDAO;
import com.app.model.AppUsage;
import com.app.model.AppUsageDAO;
import com.app.model.ConnectionManager;
import com.app.model.UserDAO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import is203.JWTException;
import is203.JWTUtility;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author G4-T6
 */
public class top_k_most_used_schools extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws java.sql.SQLException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        response.setContentType("application/JSON");
        Connection conn = null;
        try (PrintWriter out = response.getWriter()) {
            conn = ConnectionManager.getConnection();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject jsonOutput = new JsonObject();
            JsonArray errorJsonList = new JsonArray();
            ArrayList<String> categories = AppDAO.retrieveAllAppCategory(conn);

            String startdate = request.getParameter("startdate");
            String enddate = request.getParameter("enddate");
            String token = request.getParameter("token");
            String k = request.getParameter("k");
            String appcategory = request.getParameter("appcategory");

            Date startDateFmt = null;
            Date endDateFmt = null;

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            df.setLenient(false);
            if (startdate == null || startdate.equals("")) {         
                errorJsonList.add(new JsonPrimitive("invalid startdate"));
            } else {
                try {
                    startDateFmt = df.parse(startdate);
                } catch (ParseException ex) {
                    errorJsonList.add(new JsonPrimitive("invalid startdate"));
                }
            }
            if (enddate == null || enddate.equals("")) {
                errorJsonList.add(new JsonPrimitive("invalid enddate"));
            } else {
                try {
                    endDateFmt = df.parse(enddate);
                } catch (ParseException ex) {
                    errorJsonList.add(new JsonPrimitive("invalid enddate"));
                }
            }
            if (startDateFmt != null && endDateFmt != null && startDateFmt.after(endDateFmt)) {
                errorJsonList.add(new JsonPrimitive("invalid startdate"));
            }
            int kValue = 0;
            if (k == null || k.equals("")) {
                kValue = 3;
            } else {
                try {
                    kValue = Integer.parseInt(k);
                    if (kValue < 1 || kValue > 10) {
                        errorJsonList.add(new JsonPrimitive("invalid k"));
                    }
                } catch (NumberFormatException e) {
                    errorJsonList.add(new JsonPrimitive("invalid k"));
                }
            }
            if (!categories.contains(appcategory)) {
                errorJsonList.add(new JsonPrimitive("invalid app category"));
            }
            String sharedSecret = "is203g4t6luvjava";
            String username = "";
            try {
                username = JWTUtility.verify(token, sharedSecret);
            } catch (JWTException ex) {
                errorJsonList.add(new JsonPrimitive("invalid token"));
            }
            if (errorJsonList.size() > 0) {
                jsonOutput.addProperty("status", "error");
                jsonOutput.add("messages", errorJsonList);
            } else {
                JsonArray results = new JsonArray();

                if (!k.equals("")) {
                    ArrayList<Integer> idList = AppDAO.retrieveID(appcategory, conn);
                    ArrayList<AppUsage> usages = AppUsageDAO.retrieve(startdate, enddate, conn);
                    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    HashMap<String, TreeMap<Long, Integer>> usageMap = new HashMap<>();
                    //HashMap<macaddress, TreeMap<date, appid>>
                    for (AppUsage usage : usages) {
                        String mac = usage.getMacAddress();
                        String timestamp = usage.getTimestamp();
                        Date date = new Date();
                        try {
                            date = fmt.parse(timestamp);
                        } catch (ParseException ex) {
                            Logger.getLogger(top_k_most_used_schools.class.getName()).log(Level.SEVERE, null, ex);
                        }
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
                    HashMap<String, Long> macDuMap = new HashMap<>();
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
                            Date end = new Date();
                            try {
                                end = fmt.parse(enddate + " 23:59:59");
                            } catch (ParseException ex) {
                                Logger.getLogger(top_k_most_used_schools.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            long lastDif = (end.getTime() - time1) / 1000;
                            if (lastDif > 120) {
                                lastDif = 10;
                            }
                            duration += lastDif;
                            if (idList.contains(id1)) {
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
                                id2 = tm.get(time2);
                                long dif = (time2 - time1) / 1000; // seconds
                                if (dif > 120) {
                                    dif = 10;
                                }
                                duration += dif;
                                if (id1 != id2) {
                                    if (idList.contains(id1)) {
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
                                    Date end = new Date();
                                    try {
                                        end = fmt.parse(enddate + " 23:59:59");
                                    } catch (ParseException ex) {
                                        Logger.getLogger(top_k_most_used_schools.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    long lastDif = (end.getTime() - time2) / 1000;
                                    if (lastDif > 120) {
                                        lastDif = 10;
                                    }
                                    duration += lastDif;
                                    if (idList.contains(id2)) {
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
                        Collection<Long> duCollect = appDuMap.values();
                        Iterator duIte = duCollect.iterator();
                        long totalDu = 0;
                        while (duIte.hasNext()) {
                            long dur = (long) duIte.next();
                            totalDu += dur;
                        }
                        macDuMap.put(mac, totalDu);

                    }
                    //convert the mac-duration map into school-duration hashmap
                    Set<String> mSet = macDuMap.keySet();
                    Iterator iter = mSet.iterator();
                    HashMap<String, Long> schoolDuMap = new HashMap<>();//<school, duration>
                    while (iter.hasNext()) {
                        String macAdr = (String) iter.next();
                        long dur = macDuMap.get(macAdr);
                        String school = UserDAO.retrieveSchool(macAdr, conn);
                        if (schoolDuMap.get(school) == null) {
                            schoolDuMap.put(school, dur);
                        } else {
                            long d = schoolDuMap.get(school);
                            schoolDuMap.put(school, dur + d);
                        }
                    }
                    //convert school-duration map into duration-school treemap
                    TreeMap<Long, ArrayList<String>> duSchoolMap = new TreeMap<>();
                    Set<String> schSet = schoolDuMap.keySet();
                    Iterator schIte = schSet.iterator();
                    while (schIte.hasNext()) {
                        String school = (String) schIte.next();
                        long dur = schoolDuMap.get(school);
                        ArrayList<String> schools = new ArrayList<>();
                        if (duSchoolMap.get(dur) == null) {
                            schools.add(school);
                            duSchoolMap.put(dur, schools);
                        } else {
                            schools = duSchoolMap.get(dur);
                            schools.add(school);
                            duSchoolMap.put(dur, schools);
                        }
                    }
                    TreeMap<Long, ArrayList<String>> treemap3 = duSchoolMap;
                    NavigableMap<Long, ArrayList<String>> map3 = null;
                    if (treemap3 != null) {
                        map3 = treemap3.descendingMap();
                    }
                    if (map3 != null && !map3.isEmpty()) {
                        Set<Long> durSet = map3.keySet();
                        Iterator iterator = durSet.iterator();
                        int kCount = kValue;
                        int rank = 1;
                        while (iterator.hasNext() && rank <= kCount) {
                            Long usage = (Long) iterator.next();
                            ArrayList<String> schoolList = map3.get(usage);
                            int count = 0;
                            Collections.sort(schoolList);
                            for (int i = 0; i < schoolList.size(); i++) {
                                JsonObject obj = new JsonObject();
                                obj.addProperty("rank", rank);
                                String school = schoolList.get(i);
                                obj.addProperty("school", school);
                                obj.addProperty("duration", usage);
                                results.add(obj);
                                count++;
                            }
                            rank += count;
                        }
                        jsonOutput.addProperty("status", "success");
                        jsonOutput.add("results", results);
                    }
                }
            }
            try {
                out.println(gson.toJson(jsonOutput));
            } finally {
                out.close();
                ConnectionManager.close(conn);
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (SQLException ex) {
            Logger.getLogger(top_k_most_used_schools.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (SQLException ex) {
            Logger.getLogger(top_k_most_used_schools.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
