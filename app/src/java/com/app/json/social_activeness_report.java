package com.app.json;

import com.app.controller.SocialActivenessUtility;
import com.app.model.Activity;
import com.app.model.AppDAO;
import com.app.model.AppUsage;
import com.app.model.AppUsageDAO;
import com.app.model.ConnectionManager;
import com.app.model.LocationRecord;
import com.app.model.LocationRecordDAO;
import com.app.model.Pair;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
public class social_activeness_report extends HttpServlet {

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
            throws ServletException, IOException, SQLException, ParseException, Exception {
        response.setContentType("application/JSON");
        Connection conn = null;
        try (PrintWriter out = response.getWriter()) {
            conn = ConnectionManager.getConnection();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject jsonOutput = new JsonObject();
            JsonArray errorJsonList = new JsonArray();
            String date = request.getParameter("date");
            String macaddress = request.getParameter("macaddress");
            String token = request.getParameter("token");
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            df.setLenient(false);
            String sharedSecret = "is203g4t6luvjava";
            String username = "";
            
            try {
                username = JWTUtility.verify(token, sharedSecret);
            } catch (JWTException ex) {
                errorJsonList.add(new JsonPrimitive("invalid token"));
            }

            if (date == null || date.isEmpty()) {
                errorJsonList.add(new JsonPrimitive("invalid date"));
            } else {
                try {
                    Date d = df.parse(date);
                } catch (ParseException ex) {
                    errorJsonList.add(new JsonPrimitive("invalid date"));
                }
            }
            if (macaddress == null || macaddress.isEmpty()) {
                errorJsonList.add(new JsonPrimitive("invalid macaddress"));
            } else {
                if (!macaddress.matches("[a-fA-F0-9]{40}")) {
                    errorJsonList.add(new JsonPrimitive("invalid macaddress"));
                }
            }
            if (errorJsonList.size() > 0) {
                jsonOutput.addProperty("status", "error");
                jsonOutput.add("messages", errorJsonList);
            } else {
                JsonObject results = new JsonObject();
                //the 1st function: online activeness
                ArrayList<Integer> socialID = AppDAO.retrieveID("social", conn);
                ArrayList<AppUsage> usages = AppUsageDAO.retrieve(date, conn, macaddress);
                if (usages.isEmpty()) {
                    results.add("total-social-app-usage-duration", new JsonPrimitive(0));
                    results.add("individual-social-app-usage", new JsonPrimitive("empty"));
                } else {
                    HashMap<Integer, Long> appIdDuration = SocialActivenessUtility.getAppDuration(usages, date, socialID);
                    long duration = SocialActivenessUtility.getDuration(appIdDuration);
                    TreeMap<String, Float> appDuration = new TreeMap<>();
                    Set<Integer> idSet = appIdDuration.keySet();
                    Iterator ite = idSet.iterator();
                    JsonArray individual = new JsonArray();
                    while (ite.hasNext()) {
                        int id = (Integer) ite.next();
                        long d = appIdDuration.get(id);
                        String name = AppDAO.retrieveName(id, conn);
                        appDuration.put(name, (float) d / duration * 100);
                    }
                    Set<String> appNames = appDuration.keySet();
                    Iterator it = appNames.iterator();
                    while (it.hasNext()) {
                        String name = (String) it.next();
                        float percentage = appDuration.get(name);
                        int percent = Math.round(percentage);
                        JsonObject obj = new JsonObject();
                        obj.add("app-name", new JsonPrimitive(name));
                        obj.addProperty("percent", percent);
                        individual.add(obj);
                    }
                    results.add("total-social-app-usage-duration", new JsonPrimitive(duration));
                    results.add("individual-social-app-usage", individual);
                }
                //the 2nd function: physical activeness
                ArrayList<LocationRecord> records = LocationRecordDAO.retrieveByDate(date, conn);
                HashMap<String, ArrayList<LocationRecord>> userRecordMap = SocialActivenessUtility.sortRecordByUser(records);
                if (userRecordMap.get(macaddress) == null) {
                    results.add("total-time-spent-in-sis", new JsonPrimitive(0));
                    results.add("group-percent", new JsonPrimitive(0));
                    results.add("solo-percent", new JsonPrimitive(0));
                } else {
                    HashMap<String, ArrayList<Activity>> userActivityMap = SocialActivenessUtility.convert(userRecordMap, date, conn);
                    ArrayList<Pair<Date, Date>> groupLog = new ArrayList<>();
                    ArrayList<Activity> currentUserActivity = userActivityMap.get(macaddress);
                    userActivityMap.remove(macaddress);
                    Set<String> macSet = userActivityMap.keySet();
                    Iterator ite = macSet.iterator();
                    while (ite.hasNext()) {
                        String mac = (String) ite.next();
                        ArrayList<Activity> otherActivity = userActivityMap.get(mac);
                        for (int i = 0; i < currentUserActivity.size(); i++) {
                            Activity a = currentUserActivity.get(i);
                            for (int j = 0; j < otherActivity.size(); j++) {
                                Activity otherA = otherActivity.get(j);
                                if (a.stayAsGroup(otherA)) {
                                    groupLog.add(a.overlap(otherA));
                                }
                                if (a.moveTogether(otherA)) {
                                    Pair<Date, Date> p1 = a.overlap(otherA);
                                    long du1 = p1.getSecond().getTime() - p1.getFirst().getTime();
                                    a = currentUserActivity.get(i + 1);
                                    otherA = otherActivity.get(j + 1);
                                    Pair<Date, Date> p2 = a.overlap(otherA);
                                    if (p2 != null) {
                                        long du2 = p2.getSecond().getTime() - p2.getFirst().getTime();
                                        if ((du1 + du2) >= 300000) {
                                            Pair<Date, Date> pair = new Pair(p1.getFirst(), p2.getSecond());
                                            groupLog.add(pair);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    long totalTime = SocialActivenessUtility.getTotalDuration(currentUserActivity);//in second
                    long groupTime;
                    if (!groupLog.isEmpty()) {
                        groupTime = SocialActivenessUtility.getGroupDuration(date, groupLog);//in second
                    } else {
                        groupTime = 0;
                    }
                    float aloneTime = totalTime - groupTime;
                    int groupPercent = 0;
                    int soloPercent = 0;
                    if (totalTime != 0) {
                        soloPercent = Math.round(aloneTime / totalTime * 100);
                        groupPercent = 100 - soloPercent;
                    }
                    results.add("total-time-spent-in-sis", new JsonPrimitive(totalTime));
                    results.addProperty("group-percent", groupPercent);
                    results.addProperty("solo-percent", soloPercent);
                }
                jsonOutput.addProperty("status", "success");
                jsonOutput.add("results", results);
            }
            out.println(gson.toJson(jsonOutput));
            out.close();
        } finally {
            ConnectionManager.close(conn);
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
            Logger.getLogger(social_activeness_report.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(social_activeness_report.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(social_activeness_report.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(social_activeness_report.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(social_activeness_report.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(social_activeness_report.class.getName()).log(Level.SEVERE, null, ex);
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
