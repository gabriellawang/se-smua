/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.app.json;

import com.app.model.AppUsage;
import com.app.model.AppUsageDAO;
import com.app.model.ConnectionManager;
import com.app.model.User;
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
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
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
public class basic_appcategory_report extends HttpServlet {

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
            //retrieves the user
            String startdate = request.getParameter("startdate");
            String enddate = request.getParameter("enddate");
            String token = request.getParameter("token");
            Date startDateFmt = null;
            Date endDateFmt = null;

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            df.setLenient(false);
            JsonObject temp = new JsonObject();

            if (startdate == null) {
                errorJsonList.add(new JsonPrimitive("missing startdate"));
            } else if (startdate.equals("")) {
                errorJsonList.add(new JsonPrimitive("blank startdate"));
            } else {
                try {
                    startdate += " 00:00:00";
                    startDateFmt = df.parse(startdate);
                } catch (ParseException ex) {
                    errorJsonList.add(new JsonPrimitive("invalid startdate"));
                }
            }
            if (enddate == null) {
                // error output
                errorJsonList.add(new JsonPrimitive("missing enddate"));
            } else if (enddate.equals("")) {
                errorJsonList.add(new JsonPrimitive("blank enddate"));
            } else {
                try {
                    enddate += " 23:59:59";
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
            try {
                username = JWTUtility.verify(token, sharedSecret);
            } catch (JWTException ex) {
                errorJsonList.add(new JsonPrimitive("invalid token"));
            }

            if (errorJsonList.size() > 0) {
                jsonOutput.addProperty("status", "error");
                jsonOutput.add("messages", errorJsonList);
            } else {

                //----------------------------------BREAKDWN APP CATEGORY CALCULATION--------------------------------------//
                TreeMap<String, Integer> appMap = new TreeMap<String, Integer>();
                ArrayList<AppUsage> fullAppusageList = AppUsageDAO.retrieveAllAppUsageOnDates(startDateFmt, endDateFmt, conn);
                ArrayList<User> userList = UserDAO.retrieveAll(conn);
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                ArrayList<String> categoryList = new ArrayList<>();
                categoryList.add("books");
                categoryList.add("social");
                categoryList.add("education");
                categoryList.add("entertainment");
                categoryList.add("information");
                categoryList.add("library");
                categoryList.add("local");
                categoryList.add("tools");
                categoryList.add("fitness");
                categoryList.add("games");
                categoryList.add("others");
                for (String category : categoryList) {
                    appMap.put(category, 0);
                }

                if (fullAppusageList == null || fullAppusageList.isEmpty()) {
                    JsonArray results = new JsonArray();
                    Set appMapSet = appMap.keySet();
                    Iterator iter = appMapSet.iterator();
                    while (iter.hasNext()) {
                        String cat = (String) iter.next();
                        JsonObject category = new JsonObject();
                        category.add("category-name", new JsonPrimitive(cat));
                        category.add("category-duration", new JsonPrimitive(0));
                        category.add("category-percent", new JsonPrimitive(0));
                        results.add(category);
                    }

                    jsonOutput.addProperty("status", "success");
                    jsonOutput.add("breakdown", results);
                    out.println(gson.toJson(jsonOutput));
                    return;
                }

                ArrayList<String> resultList = new ArrayList<>();
                int totalTimeSpent = 0;
                long days = 0;
                for (int i = 0; i < userList.size(); i++) {
                    User user = userList.get(i);
                    String userMacAddr = user.getMacAddress();

                    for (int x = 0; x < fullAppusageList.size(); x++) {
                        AppUsage appusage = fullAppusageList.get(x);
                        String appMacAddr = appusage.getMacAddress();

                        if (userMacAddr.equals(appMacAddr)) {
                            ArrayList<String> matchList = AppUsageDAO.retrieveTimestampAndCategory(startDateFmt, endDateFmt, userMacAddr, appMacAddr, conn);
                            for (String result : matchList) {
                                resultList.add(result);
                            }
                            break;
                        }
                    }
                }

                Collections.sort(resultList);

                for (String s : resultList) {
                    System.out.println(s);
                }

                long daysTp = endDateFmt.getTime() - startDateFmt.getTime();
                days = TimeUnit.MILLISECONDS.toDays(daysTp);
                days += 1;

                String beforeTp = resultList.get(0).substring(0, (resultList.get(0).indexOf(",") - 1));
                String firstCategory = resultList.get(0).substring(resultList.get(0).indexOf(",") + 1, resultList.get(0).length());
                Date timeBeforeUsed = null;
                try {
                    timeBeforeUsed = fmt.parse(beforeTp);
                } catch (ParseException e) {
                    System.out.println("An error occurred");
                }

                int countSecs = 0;
                for (int z = 1; z < resultList.size(); z++) {
                    String category = resultList.get(z).substring(resultList.get(z).indexOf(",") + 1, resultList.get(z).length());
                    String afterTp = resultList.get(z).substring(0, (resultList.get(z).indexOf(",") - 1));
                    Date timeAfterUsed = null;
                    try {
                        timeAfterUsed = fmt.parse(afterTp);
                    } catch (ParseException e) {
                        System.out.println("An error occurred");
                    }

                    try {
                        if (category.equals(firstCategory)) {
                            if (timeBeforeUsed.getHours() == 23 && timeAfterUsed.getHours() == 0) {
                                Date anotherTemp = timeBeforeUsed;
                                String tempDate = fmt.format(anotherTemp);
                                tempDate = tempDate.substring(0, tempDate.indexOf(" "));
                                tempDate += " 23:59:59";
                                anotherTemp = fmt.parse(tempDate);
                                long diff = (anotherTemp.getTime() - timeBeforeUsed.getTime());
                                long diffSecs = (TimeUnit.MILLISECONDS.toSeconds(diff)) + 1;
                                if (diffSecs < 121) {
                                    countSecs = (int) diffSecs;
                                } else {
                                    countSecs = 10;
                                }
                            } else {
                                long diff = timeAfterUsed.getTime() - timeBeforeUsed.getTime();
                                long diffSecs = TimeUnit.MILLISECONDS.toSeconds(diff);

                                if (diffSecs < 121) {
                                    countSecs = (int) diffSecs;
                                } else {
                                    countSecs = 10;
                                }
                            }
                            int valueSecs = (int) appMap.get(firstCategory);
                            valueSecs += countSecs;
                            appMap.put(firstCategory, valueSecs);
                            totalTimeSpent += countSecs;

                        } else {
                            if (timeBeforeUsed.getHours() == 23 && timeAfterUsed.getHours() == 0) {
                                Date anotherTemp = timeBeforeUsed;
                                String tempDate = fmt.format(anotherTemp);
                                tempDate = tempDate.substring(0, tempDate.indexOf(" "));
                                tempDate += " 23:59:59";
                                anotherTemp = fmt.parse(tempDate);
                                long diff = (anotherTemp.getTime() - timeBeforeUsed.getTime());
                                long diffSecs = (TimeUnit.MILLISECONDS.toSeconds(diff)) + 1;
                                if (diffSecs < 121) {
                                    countSecs = (int) diffSecs;
                                } else {
                                    countSecs = 10;
                                }
                            } else {
                                long diff = timeAfterUsed.getTime() - timeBeforeUsed.getTime();
                                long diffSecs = TimeUnit.MILLISECONDS.toSeconds(diff);

                                if (diffSecs < 121) {
                                    countSecs = (int) diffSecs;
                                } else {
                                    countSecs = 10;
                                }
                            }
                            int valueSecs = (int) appMap.get(firstCategory);
                            valueSecs += countSecs;
                            appMap.put(firstCategory, valueSecs);
                            totalTimeSpent += countSecs;
                        }

                        if (z == (resultList.size() - 1)) {
                            if (timeAfterUsed.getHours() == 23 && timeAfterUsed.getMinutes() >= 58) {
                                Date anotherTemp = timeAfterUsed;
                                String tempDate = fmt.format(anotherTemp);
                                tempDate = tempDate.substring(0, tempDate.indexOf(" "));
                                tempDate += " 23:59:59";
                                anotherTemp = fmt.parse(tempDate);
                                long diff = (anotherTemp.getTime() - timeAfterUsed.getTime());
                                long diffSecs = TimeUnit.MILLISECONDS.toSeconds(diff) + 1;
                                countSecs = (int) diffSecs;
                            } else {
                                countSecs = 10;
                            }
                            int valueSecs = (int) appMap.get(category);
                            valueSecs += countSecs;
                            appMap.put(category, valueSecs);
                            totalTimeSpent += countSecs;
                        }
                        timeBeforeUsed = timeAfterUsed;
                        firstCategory = category;
                        countSecs = 0;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                //---------------------------------JSON DISPLAY-----------------------------------------//
                JsonArray results = new JsonArray();
                Set appMapSet = appMap.keySet();
                Iterator iter = appMapSet.iterator();

                while (iter.hasNext()) {
                    String cat = (String) iter.next();
                    JsonObject category = new JsonObject();
                    category.add("category-name", new JsonPrimitive(cat));
                    int dur = appMap.get(cat);
                    double secs = ((dur + 0.0) / days);
                    double percentage = ((dur + 0.0) / (totalTimeSpent + 0.0)) * 100;
                    category.add("category-duration", new JsonPrimitive(Math.round(secs)));
                    category.add("category-percent", new JsonPrimitive(Math.round(percentage)));
                    results.add(category);
                    System.out.println(cat);
                    System.out.println(secs);
                    System.out.println(percentage);
                }

                jsonOutput.addProperty("status", "success");
                jsonOutput.add("breakdown", results);
                //writes the output as a response (but not html)
            }
            try {
                out.println(gson.toJson(jsonOutput));
            } finally {
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
            Logger.getLogger(basic_usetime_report.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(basic_usetime_report.class.getName()).log(Level.SEVERE, null, ex);
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
