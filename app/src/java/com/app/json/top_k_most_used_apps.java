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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
public class top_k_most_used_apps extends HttpServlet {

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
            //retrieves the user
            String[] schools = {"sis", "economics", "socsc", "law", "accountancy", "business"};
            List<String> schoolList = (List<String>) Arrays.asList(schools);
            String startdate = request.getParameter("startdate");
            String enddate = request.getParameter("enddate");
            String token = request.getParameter("token");
            String k = request.getParameter("k");
            String school = request.getParameter("school");
            Date startDateFmt = null;
            Date endDateFmt = null;
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            df.setLenient(false);
            JsonObject temp = new JsonObject();
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
            int kInt = 0;
            if (k == null || k.equals("")) {
                kInt = 3;
            } else {
                try {
                    kInt = Integer.parseInt(k);
                    if (kInt < 1 || kInt > 10) {
                        errorJsonList.add(new JsonPrimitive("invalid k"));
                    }
                } catch (NumberFormatException e) {
                    errorJsonList.add(new JsonPrimitive("invalid k"));
                }
            }
            String sharedSecret = "is203g4t6luvjava";
            String username = "";
            try {
                username = JWTUtility.verify(token, sharedSecret);
            } catch (JWTException ex) {
                errorJsonList.add(new JsonPrimitive("invalid token"));
            }
            if (!schoolList.contains(school)) {
                errorJsonList.add(new JsonPrimitive("invalid school"));
            }
            if (errorJsonList.size() > 0) {
                jsonOutput.addProperty("status", "error");
                jsonOutput.add("messages", errorJsonList);
            } else {
                JsonArray results = new JsonArray();
                ArrayList<String> macList = UserDAO.retrieveMacAddress(school, conn);
                ArrayList<AppUsage> usages = AppUsageDAO.retrieve(macList, startdate, enddate, conn);
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                HashMap<String, TreeMap<Long, Integer>> usageMap = new HashMap<>();
                for (AppUsage usage : usages) {
                    String mac = usage.getMacAddress();
                    String timestamp = usage.getTimestamp();
                    Date date = new Date();
                    try {
                        date = fmt.parse(timestamp);
                    } catch (ParseException ex) {
                        Logger.getLogger(top_k_most_used_apps.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    int appid = usage.getId();
                    if (usageMap.get(mac) == null) {
                        TreeMap<Long, Integer> tm = new TreeMap<>();
                        tm.put(date.getTime(), appid);
                        usageMap.put(mac, tm);
                    } else {
                        TreeMap<Long, Integer> tm = usageMap.get(mac);
                        tm.put(date.getTime(), appid);
                        usageMap.put(mac, tm);
                    }
                }
                HashMap<Integer, Long> appDuMap = new HashMap<>();
                Set<String> macSet = usageMap.keySet();
                Iterator ite = macSet.iterator();
                while (ite.hasNext()) {
                    String mac = (String) ite.next();
                    TreeMap<Long, Integer> tm = usageMap.get(mac);
                    Set<Long> timeSet = tm.keySet();
                    Iterator timeIte = timeSet.iterator();
                    long time1 = (long) timeIte.next();
                    long time2;
                    int id1 = tm.get(time1);
                    int id2;
                    long duration = 0;
                    if (!timeIte.hasNext()) {//the last record

                        Date end = new Date();
                        try {
                            end = fmt.parse(enddate + " 23:59:59");
                        } catch (ParseException ex) {
                            Logger.getLogger(top_k_most_used_apps.class.getName()).log(Level.SEVERE, null, ex);
                        }
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

                                Date end = new Date();
                                try {
                                    end = fmt.parse(enddate + " 23:59:59");
                                } catch (ParseException ex) {
                                    Logger.getLogger(top_k_most_used_apps.class.getName()).log(Level.SEVERE, null, ex);
                                }

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
                }
                TreeMap<Long, ArrayList<String>> duAppMap = new TreeMap<>();
                Set<Integer> aSet = appDuMap.keySet();
                Iterator iter = aSet.iterator();
                while (iter.hasNext()) {
                    int id = (int) iter.next();
                    String name = AppDAO.retrieveName(id, conn);
                    long dur = appDuMap.get(id);
                    if (duAppMap.get(dur) == null) {
                        ArrayList<String> iList = new ArrayList<>();
                        iList.add(name);
                        duAppMap.put(dur, iList);
                    } else {
                        ArrayList<String> iList = duAppMap.get(dur);
                        iList.add(name);
                        duAppMap.put(dur, iList);
                    }
                }
                NavigableMap<Long, ArrayList<String>> map1 = null;
                TreeMap<Long, ArrayList<String>> treemap1 = duAppMap;
                if (treemap1 != null) {
                    map1 = treemap1.descendingMap();
                }
                if (map1 != null && !map1.isEmpty()) {
                    Set<Long> durSet = map1.keySet();
                    Iterator it = durSet.iterator();
                    int kCount = kInt;
                    int rank = 1;
                    while (it.hasNext() && rank <= kCount) {
                        Long usage = (Long) it.next();
                        ArrayList<String> apps = map1.get(usage);
                        Collections.sort(apps);
                        int count = 0;
                        for (int i = 0; i < apps.size(); i++) {
                            JsonObject obj = new JsonObject();
                            obj.addProperty("rank", rank);
                            obj.addProperty("app-name", apps.get(i));
                            obj.addProperty("duration", usage);
                            results.add(obj);
                            count++;
                        }
                        rank += count;
                    }
                }

                jsonOutput.addProperty("status", "success");
                jsonOutput.add("results", results);
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
            Logger.getLogger(top_k_most_used_apps.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(top_k_most_used_apps.class.getName()).log(Level.SEVERE, null, ex);
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
