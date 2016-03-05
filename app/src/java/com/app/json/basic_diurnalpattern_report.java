/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.app.json;

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
public class basic_diurnalpattern_report extends HttpServlet {

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

            //creats a new json object for printing the desired json output
            JsonObject jsonOutput = new JsonObject();
            JsonArray errorJsonList = new JsonArray();
            //retrieves parameters
            String date = request.getParameter("date");
            String yearfilter = request.getParameter("yearfilter");
            String genderfilter = request.getParameter("genderfilter");
            String schoolfilter = request.getParameter("schoolfilter");
            String token = request.getParameter("token");
            Date dateFmt = null;

            String[] schools = {"sis", "economics", "socsc", "law", "accountancy", "business"};
            String[] years = {"2011", "2012", "2013", "2014", "2015"};
            String[] genders = {"M", "F"};
            List<String> yearList = (List<String>) Arrays.asList(years);
            List<String> genderList = (List<String>) Arrays.asList(genders);
            List<String> schoolList = (List<String>) Arrays.asList(schools);

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            df.setLenient(false);
            JsonObject temp = new JsonObject();
            if (date == null || date.equals("")) {
                // error output              
                errorJsonList.add(new JsonPrimitive("invalid date"));
            } else {
                try {
                    dateFmt = df.parse(date);
                } catch (ParseException ex) {
                    errorJsonList.add(new JsonPrimitive("invalid date"));
                }
            }
            if (yearfilter==null ||yearfilter.equals("NA") || yearfilter.equals("")) {
                yearfilter = "";
            } else if (!yearList.contains(yearfilter)) {
                errorJsonList.add(new JsonPrimitive("invalid year filter"));
            }
            if (genderfilter==null ||genderfilter.equals("NA") || genderfilter.equals("")) {
                genderfilter = "";
            } else if (!genderList.contains(genderfilter)) {
                errorJsonList.add(new JsonPrimitive("invalid gender filter"));
            } 
            if (schoolfilter==null || schoolfilter.equals("NA") || schoolfilter.equals("")) {
                schoolfilter = "";
            } else if (!schoolList.contains(schoolfilter)) {
                errorJsonList.add(new JsonPrimitive("invalid school filter"));
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

                ArrayList<String> criteria = new ArrayList<String>();

                JsonArray output = new JsonArray();

                ArrayList<AppUsage> usageList = AppUsageDAO.retrieveAppUsageGivenCriteria(yearfilter, schoolfilter, genderfilter, date, conn);

                HashMap<String, ArrayList<AppUsage>> usageMap = new HashMap();
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                // Results to be sent back
                HashMap<Integer, Integer> results = new HashMap();
                for (int i = 0; i < 24; i++) {
                    results.put(i, 0);
                }

                for (AppUsage usage : usageList) {
                    String mac = usage.getMacAddress();

                    ArrayList<AppUsage> uList;
                    if (usageMap.get(mac) == null) {
                        uList = new ArrayList<>();
                        uList.add(usage);
                        usageMap.put(mac, uList);
                    } else {
                        uList = usageMap.get(mac);
                        uList.add(usage);
                        usageMap.put(mac, uList);
                    }
                }

                int noOfUsers = usageMap.size();
                Iterator it = usageMap.keySet().iterator();

                while (it.hasNext()) {
                    String macAddress = (String) it.next();
                    ArrayList<AppUsage> userAppUsage = usageMap.get(macAddress);
                    Collections.sort(userAppUsage);

                    long duration = 0;

                    Date d1 = userAppUsage.get(0).getDate();
                    for (int i = 1; i < userAppUsage.size(); i++) {
                        AppUsage b = userAppUsage.get(i);

                        Date d2 = b.getDate();
                        long t1 = d1.getTime();
                        long t2 = d2.getTime();
                        if (d1.getHours() != d2.getHours()) {
                            Date tempd = d1;
                            tempd.setMinutes(59);
                            tempd.setSeconds(59);
                            long diff = ((tempd.getTime() - t1) / 1000) + 1;
                            if (diff > 120) {
                                diff = 10;
                            }
                            duration += diff;
                            results.put(d1.getHours(), (int) duration);
                            duration = 0;
                        } else {
                            long diff = (t2 - t1) / 1000;
                            if (diff > 120) {
                                diff = 10;
                            }
                            duration += diff;
                            results.put(d1.getHours(), (int) duration);
                            // Last record
                            if (i == (userAppUsage.size() - 1)) {
                                Date tempd = d2;
                                tempd.setMinutes(59);
                                tempd.setSeconds(59);
                                long tempDiff = ((tempd.getTime() - t2) / 1000) + 1;
                                long lastDiff = 10;
                                if (tempDiff < 10) {
                                    lastDiff = tempDiff;
                                }
                                duration += lastDiff;
                                results.put(d1.getHours(), (int) duration);
                            }
                        }
                        d1 = d2;
                        t1 = t2;
                    }
                }
                for (int i = 0; i < 24; i++) {
                    int totalHours = results.get(i);

                    if (noOfUsers == 0) {
                        noOfUsers = 1;
                    }
                    float avg = (float) totalHours / noOfUsers;
                    int averageHours = Math.round(avg);
                    results.put(i, averageHours);
                }
                HashMap<Integer, String> outputHour = new HashMap<>();
                outputHour.put(0,"00:00-01:00");
                outputHour.put(1,"01:00-02:00");
                outputHour.put(2,"02:00-03:00");
                outputHour.put(3,"03:00-04:00");
                outputHour.put(4,"04:00-05:00");
                outputHour.put(5,"05:00-06:00");
                outputHour.put(6,"06:00-07:00");
                outputHour.put(7,"07:00-08:00");
                outputHour.put(8,"08:00-09:00");
                outputHour.put(9,"09:00-10:00");
                outputHour.put(10,"10:00-11:00");
                outputHour.put(11,"11:00-12:00");
                outputHour.put(12,"12:00-13:00");
                outputHour.put(13,"13:00-14:00");
                outputHour.put(14,"14:00-15:00");
                outputHour.put(15,"15:00-16:00");
                outputHour.put(16,"16:00-17:00");
                outputHour.put(17,"17:00-18:00");
                outputHour.put(18,"18:00-19:00");
                outputHour.put(19,"19:00-20:00");
                outputHour.put(20,"20:00-21:00");
                outputHour.put(21,"21:00-22:00");
                outputHour.put(22,"22:00-23:00");
                outputHour.put(23,"23:00-00:00");
                
                for (int i= 0; i < 24; i++) {
                    String period = outputHour.get(i);
                    int duration = results.get(i);
                    JsonObject obj = new JsonObject();
                    obj.addProperty("period", period);
                    obj.addProperty("duration", duration);
                    output.add(obj);
                }
               
                jsonOutput.addProperty("status", "success");
                jsonOutput.add("breakdown", output);
            }
            //writes the output as a response (but not html)
            try {
                out.println(gson.toJson(jsonOutput));
                System.out.println(gson.toJson(jsonOutput));
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
         *
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (SQLException ex) {
            Logger.getLogger(basic_diurnalpattern_report.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(basic_diurnalpattern_report.class.getName()).log(Level.SEVERE, null, ex);
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
