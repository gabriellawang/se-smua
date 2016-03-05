/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.app.json;

import com.app.model.AppUsage;
import com.app.model.AppUsageDAO;
import com.app.model.ConnectionManager;
import com.app.model.LocationDAO;
import com.app.model.LocationRecord;
import com.app.model.LocationRecordDAO;
import com.app.model.UserDAO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
public class usage_heatmap extends HttpServlet {

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
            String date = request.getParameter("date");
            String floor = request.getParameter("floor");
            String token = request.getParameter("token");
            String time = request.getParameter("time");
            Date dateF = null;
            Date timeF = null;
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            df.setLenient(false);
            SimpleDateFormat timeDf = new SimpleDateFormat("HH:mm:ss");
            timeDf.setLenient(false);
            JsonObject temp = new JsonObject();
            int floorNum = 0;
            if (floor == null) {
                errorJsonList.add(new JsonPrimitive("missing floor"));
            } else if (floor.equals("")) {
                errorJsonList.add(new JsonPrimitive("blank floor"));
            } else {
                try {
                    floorNum = Integer.parseInt(floor);
                    if (floorNum > 5 || floorNum < 0) {
                        errorJsonList.add(new JsonPrimitive("invalid floor"));
                    }
                } catch (NumberFormatException e) {
                    errorJsonList.add(new JsonPrimitive("invalid floor"));
                }
            }
            if (date == null ) {
                errorJsonList.add(new JsonPrimitive("missing date"));
            } else if (date.equals("")) {
                errorJsonList.add(new JsonPrimitive("blank date"));
            } else {
                try {
                    dateF = df.parse(date);
                } catch (ParseException ex) {
                    errorJsonList.add(new JsonPrimitive("invalid date"));
                }
            }
            if (time == null) {
                errorJsonList.add(new JsonPrimitive("missing time"));
            } else if (time.equals("")) {
                errorJsonList.add(new JsonPrimitive("blank time"));
            } else {
                try {
                    timeF = timeDf.parse(time);
                } catch (ParseException ex) {
                    errorJsonList.add(new JsonPrimitive("invalid time"));
                }
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
                conn = ConnectionManager.getConnection();
                HashMap<Integer, String> floorMap = new HashMap<Integer, String>();
                floorMap.put(0, "B1");
                floorMap.put(1, "L1");
                floorMap.put(2, "L2");
                floorMap.put(3, "L3");
                floorMap.put(4, "L4");
                floorMap.put(5, "L5");
                String level = floorMap.get(floorNum);
                String fullDateFormat = date + " " + time;
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date endDate = null;
                try {
                     Date endDateTemp = fmt.parse(fullDateFormat);
                     endDate = new Date(endDateTemp.getTime() - 1000);
                } catch (ParseException ex) {
                    Logger.getLogger(usage_heatmap.class.getName()).log(Level.SEVERE, null, ex);
                }
                Date startDate = new Date();
                startDate.setTime(endDate.getTime() - 60 * 15 * 1000);

                HashMap<String, ArrayList<Integer>> sematicMap = LocationDAO.retrieveSemanticplaces(level, conn);
                ArrayList<LocationRecord> locationRecordList = LocationRecordDAO.retrieveAllLocationsOnDates(startDate, endDate, conn);
                ArrayList<AppUsage> appUsageList = AppUsageDAO.retrieveAllAppUsageOnDates(startDate, endDate, conn);
                List<String> macAdressList = new ArrayList<String>();
                for(AppUsage a : appUsageList) {
                    macAdressList.add(a.getMacAddress());
                }
                Iterator itre = locationRecordList.iterator();
                while(itre.hasNext()) {
                    LocationRecord locationRecord = (LocationRecord)itre.next();
                    String mac_address = locationRecord.getMacAddress();
                    if(!macAdressList.contains(mac_address)){
                        itre.remove();
                    }
                }
                HashMap<String, Integer> locationRecordMap = new HashMap<String, Integer>();
                //remove repeated mac-address
                for(LocationRecord a: locationRecordList) {
                    locationRecordMap.put(a.getMacAddress(), a.getLocationId());
                }
                Collection<Integer> locationIds = locationRecordMap.values();
                System.out.println(locationIds.size());
                Set locations = sematicMap.keySet();
                Iterator itr = locations.iterator();
                HashMap<String, Integer> sematicCountMap = new HashMap<String, Integer>();
                while(itr.hasNext()) {
                    int count = 0;
                    String sematic = (String)itr.next();
                    ArrayList<Integer> locationList = sematicMap.get(sematic);
                    for(Integer id: locationIds) {
                        if(locationList.contains(id)) {
                            count++;
                        }
                    }
                    sematicCountMap.put(sematic, count);
                }
                //=========Display==============================//
                JsonArray results = new JsonArray();
                Set sematicCountSet = sematicCountMap.keySet();
                Iterator iter = sematicCountSet.iterator();
                while (iter.hasNext()) {
                    JsonObject heatMap = new JsonObject();
                    String sematicPlace = (String) iter.next();
                    heatMap.add("semantic-place", new JsonPrimitive(sematicPlace));
                    int count = sematicCountMap.get(sematicPlace);
                    heatMap.add("num-people-using-phone", new JsonPrimitive(count));
                    if (count == 0) {
                        heatMap.add("crowd-density", new JsonPrimitive(0));
                    } else if (count <= 3) {
                        heatMap.add("crowd-density", new JsonPrimitive(1));
                    } else if (count <= 7) {
                        heatMap.add("crowd-density", new JsonPrimitive(2));
                    } else if (count <= 13) {
                        heatMap.add("crowd-density", new JsonPrimitive(3));
                    } else if (count <= 20) {
                        heatMap.add("crowd-density", new JsonPrimitive(4));
                    } else {
                        heatMap.add("crowd-density", new JsonPrimitive(5));
                    }
                    results.add(heatMap);
                }
                List<JsonObject> jsonValues = new ArrayList<JsonObject>();
                JsonArray sortedJsonArray = new JsonArray();
                for (JsonElement jo : results) {
                    jsonValues.add((JsonObject) jo);
                }
                Collections.sort(jsonValues, new Comparator<JsonObject>() {
                    //You can change "Name" with "ID" if you want to sort by ID
                    private static final String KEY_NAME = "semantic-place";

                    @Override
                    public int compare(JsonObject a, JsonObject b) {
                        String valA = a.get(KEY_NAME).toString();
                        String valB = b.get(KEY_NAME).toString();

                        return valA.compareTo(valB);
                    }
                });
                for (int i = 0; i < results.size(); i++) {
                    sortedJsonArray.add(jsonValues.get(i));
                }
                jsonOutput.addProperty("status", "success");
                jsonOutput.add("heatmap", sortedJsonArray);
            }
            //writes the output as a response (but not html)

            out.println(gson.toJson(jsonOutput));
            System.out.println(gson.toJson(jsonOutput));
        }  finally {
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
            Logger.getLogger(usage_heatmap.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(usage_heatmap.class.getName()).log(Level.SEVERE, null, ex);
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
