/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.app.json;

import com.app.controller.SmartphoneOveruseUtility;
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
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
public class overuse_report extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws java.sql.SQLException
     * @throws java.text.ParseException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException, ParseException {
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
            String macAdd = request.getParameter("macaddress");
            Date startDateFmt = new Date();
            Date endDateFmt = new Date();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            df.setLenient(false);
            
            if (macAdd == null) {
                errorJsonList.add(new JsonPrimitive("missing macaddress"));
            } else if (macAdd.equals("")) {
                errorJsonList.add(new JsonPrimitive("blank macaddress"));
            } else {
                if (UserDAO.retrieveUserByMacAddress(macAdd, conn) == null) {
                    errorJsonList.add(new JsonPrimitive("invalid macaddress"));
                }
            }
            
            if (startdate == null) {
                errorJsonList.add(new JsonPrimitive("missing startdate"));
            } else if (startdate.equals("")) {
                errorJsonList.add(new JsonPrimitive("blank startdate"));
            } else {
                try {
                    startDateFmt = df.parse(startdate);
                } catch (ParseException ex) {
                    errorJsonList.add(new JsonPrimitive("invalid startdate"));
                }
            }
            
            if (enddate == null) {
                errorJsonList.add(new JsonPrimitive("missing enddate"));
            } else if (enddate.equals("")) {
                errorJsonList.add(new JsonPrimitive("blank enddate"));
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
            String sharedSecret = "is203g4t6luvjava";
            String username = "";
            if (token == null) {
                errorJsonList.add(new JsonPrimitive("missing token"));
            } else if (token.equals("")) {
                errorJsonList.add(new JsonPrimitive("blank token"));
            } else {
                try {
                    username = JWTUtility.verify(token, sharedSecret);
                } catch (JWTException ex) {
                    errorJsonList.add(new JsonPrimitive("invalid token"));
                }
            }
            
            if (errorJsonList.size() > 0) {
                jsonOutput.addProperty("status", "error");
                jsonOutput.add("messages", errorJsonList);
            } else {
                ArrayList<String> macaddress = new ArrayList<>();
                macaddress.add(macAdd);
                ArrayList<AppUsage> usages = AppUsageDAO.retrieve(macaddress, startdate, enddate, conn);
                char duration;
                char gameDuration;
                char frequency;
                long days = 0;
                float totalDuration = 0;
                days = SmartphoneOveruseUtility.getDays(startdate, enddate);
                totalDuration = (float) SmartphoneOveruseUtility.getDuration(usages, enddate);

                float durationValue = totalDuration / days;
                int durationValueSecs = Math.round(durationValue);
                if (durationValue >= 5 * 3600) {
                    duration = 'S';
                } else if (durationValue >= 3 * 3600) {
                    duration = 'M';
                } else {
                    duration = 'L';
                }

                ArrayList<Integer> gameID = AppDAO.retrieveID("games", conn);
                ArrayList<AppUsage> gameUsage = AppUsageDAO.retrieve(macaddress.get(0), startdate, enddate, gameID, conn);
                float totalGameDuration = 0;
                int gameDurationValueSec = 0;
                try {
                    totalGameDuration = (float) SmartphoneOveruseUtility.getDuration(gameUsage, enddate);

                } catch (ParseException e) {

                }
                float gameDurationValue = totalGameDuration / days;
                gameDurationValueSec = Math.round(gameDurationValue);
                if (gameDurationValue >= 2 * 3600) {
                    gameDuration = 'S';
                } else if (gameDurationValue >= 1 * 3600) {
                    gameDuration = 'M';
                } else {
                    gameDuration = 'L';
                }

                float frequencyValue = 0;
                float frequencyFormatValue = 0;

                frequencyValue = SmartphoneOveruseUtility.getFrequency(usages, enddate, days);
                frequencyFormatValue = Math.round(frequencyValue * 10) / 10;

                if (frequencyValue >= 5) {
                    frequency = 'S';
                } else if (frequencyValue >= 3) {
                    frequency = 'M';
                } else {
                    frequency = 'L';
                }
                JsonObject result = new JsonObject();

                HashMap<Character, String> metrics = new HashMap<Character, String>();
                metrics.put('S', "Severe");
                metrics.put('L', "Light");
                metrics.put('M', "Moderate");
                if (!(duration != 'S' && gameDuration != 'S' && frequency != 'S')) {
                    JsonObject usage = new JsonObject();
                    JsonObject gaming = new JsonObject();
                    JsonObject accessF = new JsonObject();
                    JsonArray metricsArr = new JsonArray();
                    result.addProperty("overuse-index", "Overusing");
                    String usageCat = metrics.get(duration);
                    usage.addProperty("usage-category", usageCat);
                    usage.addProperty("usage-duration", durationValueSecs);
                    metricsArr.add(usage);
                    String gamingCat = metrics.get(gameDuration);
                    gaming.addProperty("gaming-category", gamingCat);
                    gaming.addProperty("gaming-duration", gameDurationValueSec);
                    metricsArr.add(gaming);
                    String accessFrequencyCat = metrics.get(frequency);
                    accessF.addProperty("accessfrequency-category", accessFrequencyCat);
                    accessF.addProperty("accessfrequency", frequencyFormatValue);
                    metricsArr.add(accessF);
                    result.add("metrics", metricsArr);
                } else if (duration == 'L' && gameDuration == 'L' && frequency == 'L') {
                    JsonObject usage = new JsonObject();
                    JsonObject gaming = new JsonObject();
                    JsonObject accessF = new JsonObject();
                    JsonArray metricsArr = new JsonArray();
                    result.addProperty("overuse-index", "Normal");
                    String usageCat = metrics.get(duration);
                    usage.addProperty("usage-category", usageCat);
                    usage.addProperty("usage-duration", durationValueSecs);
                    metricsArr.add(usage);
                    String gamingCat = metrics.get(gameDuration);
                    gaming.addProperty("gaming-category", gamingCat);
                    gaming.addProperty("gaming-duration", gameDurationValueSec);
                    metricsArr.add(gaming);
                    String accessFrequencyCat = metrics.get(frequency);
                    accessF.addProperty("accessfrequency-category", accessFrequencyCat);
                    accessF.addProperty("accessfrequency", frequencyFormatValue);
                    metricsArr.add(accessF);
                    result.add("metrics", metricsArr);
                } else {
                    JsonObject usage = new JsonObject();
                    JsonObject gaming = new JsonObject();
                    JsonObject accessF = new JsonObject();
                    JsonArray metricsArr = new JsonArray();
                    result.addProperty("overuse-index", "ToBeCautious");
                    String usageCat = metrics.get(duration);
                    usage.addProperty("usage-category", usageCat);
                    usage.addProperty("usage-duration", durationValueSecs);
                    metricsArr.add(usage);
                    String gamingCat = metrics.get(gameDuration);
                    gaming.addProperty("gaming-category", gamingCat);
                    gaming.addProperty("gaming-duration", gameDurationValueSec);
                    metricsArr.add(gaming);
                    String accessFrequencyCat = metrics.get(frequency);
                    accessF.addProperty("accessfrequency-category", accessFrequencyCat);
                    accessF.addProperty("accessfrequency", frequencyFormatValue);
                    metricsArr.add(accessF);
                    result.add("metrics", metricsArr);
                }
                jsonOutput.addProperty("status", "success");
                jsonOutput.add("results", result);
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
            Logger.getLogger(overuse_report.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(overuse_report.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(overuse_report.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(overuse_report.class.getName()).log(Level.SEVERE, null, ex);
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
