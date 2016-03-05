/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.app.json;

import com.app.controller.TopKAppReport;
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
public class top_k_most_used_students extends HttpServlet {

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
            String startdate = request.getParameter("startdate");
            String enddate = request.getParameter("enddate");
            String token = request.getParameter("token");
            String k = request.getParameter("k");
            if (k == null || k.length() == 0) {
                k = "3";
            }
            int kValue = Integer.parseInt(k);
            String appcategory = request.getParameter("appcategory");
            Date startDateFmt = null;
            Date endDateFmt = null;
            if (appcategory == null || appcategory.length() == 0) {
                errorJsonList.add(new JsonPrimitive("invalid app category"));
            } else {
                ArrayList<String> appCategories = AppDAO.retrieveAllAppCategory(conn);
                boolean appCategoryExist = false;
                for (String a : appCategories) {
                    if (a.equals(appcategory)) {
                        appCategoryExist = true;
                    }
                }
                if (!appCategoryExist) {
                    errorJsonList.add(new JsonPrimitive("invalid app category"));
                }
            }
            if (kValue > 10 || kValue < 1) {
                errorJsonList.add(new JsonPrimitive("invalid k"));
            }
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
            if (startDateFmt.after(endDateFmt)) {
                errorJsonList.add(new JsonPrimitive("invalid startdate"));
            }
            String sharedSecret = "is203g4t6luvjava";
            String username = "";
            if (token == null) {
                token = "";
            }
            try {
                username = JWTUtility.verify(token, sharedSecret);
            } catch (JWTException ex) {
                errorJsonList.add(new JsonPrimitive("invalid token"));
            }
            if (errorJsonList.size() > 0) {
                jsonOutput.addProperty("status", "error");
                jsonOutput.add("messages", errorJsonList);
            } else {
                try {
                    ArrayList<Integer> idList = AppDAO.retrieveID(appcategory, conn);
                    ArrayList<AppUsage> usages = AppUsageDAO.retrieve(startdate, enddate, idList, conn);
                    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    HashMap<String, TreeMap<Long, Integer>> usageMap = new HashMap<>();

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
                        if (!timeIte.hasNext()) {//the last record
                            Date end = fmt.parse(enddate + " 23:59:59");
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
                                    Date end = fmt.parse(enddate + " 23:59:59");
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
                        Collection<Long> duCollect = appDuMap.values();
                        Iterator duIte = duCollect.iterator();
                        long totalDu = 0;
                        while (duIte.hasNext()) {
                            long dur = (long) duIte.next();
                            totalDu += dur;
                        }
                        macDuMap.put(mac, totalDu);

                    }

                    TreeMap<Long, ArrayList<HashMap<String, String>>> duNameMap = new TreeMap<>();
                    Set<String> mSet = macDuMap.keySet();
                    Iterator iter = mSet.iterator();
                    JsonArray results = new JsonArray();
                    while (iter.hasNext()) {
                        String macAdr = (String) iter.next();
                        String name = UserDAO.retrieveName(macAdr, conn);
                        long dur = macDuMap.get(macAdr);
                        if (duNameMap.get(dur) == null) {
                            ArrayList<HashMap<String, String>> nList = new ArrayList<>();
                            HashMap<String, String> nameMacMap = new HashMap<>();
                            nameMacMap.put(name, macAdr);
                            nList.add(nameMacMap);
                            duNameMap.put(dur, nList);
                        } else {
                            ArrayList<HashMap<String, String>> nList = duNameMap.get(dur);
                            HashMap<String, String> nameMacMap = new HashMap<>();
                            nameMacMap.put(name, macAdr);
                            nList.add(nameMacMap);
                            duNameMap.put(dur, nList);
                        }
                    }
                    NavigableMap<Long, ArrayList<HashMap<String, String>>> map2 = duNameMap.descendingMap();
                    Set<Long> durSet = map2.keySet();
                    Iterator iter1 = durSet.iterator();
                    int rank1 = 1;
                    while (iter1.hasNext() && rank1 <= kValue) {
                        Long usage = (Long) iter1.next();
                        ArrayList<HashMap<String, String>> nameMacList = map2.get(usage);
                        int count = 0;
                        for (int i = 0; i < nameMacList.size(); i++) {
                            JsonObject rank = new JsonObject();
                            rank.addProperty("rank", rank1);
                            HashMap<String, String> nameMac = nameMacList.get(i);
                            Set<String> nameSet = nameMac.keySet();
                            Iterator nameIte = nameSet.iterator();
                            while (nameIte.hasNext()) {
                                String n = (String) nameIte.next();
                                String m = nameMac.get(n);
                                rank.addProperty("name", n);
                                rank.addProperty("mac-address", m);
                                rank.addProperty("duration", usage);
                                count++;
                            }
                            results.add(rank);
                        }
                        rank1 += count;
                    }
                    jsonOutput.addProperty("status", "success");
                    jsonOutput.add("results", results);
                } catch (ParseException ex) {
                    Logger.getLogger(TopKAppReport.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(top_k_most_used_students.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(top_k_most_used_students.class.getName()).log(Level.SEVERE, null, ex);
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
