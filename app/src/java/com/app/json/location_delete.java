package com.app.json;

import com.app.model.AdminDAO;
import com.app.model.ConnectionManager;
import com.app.model.LocationDAO;
import com.app.model.LocationRecord;
import com.app.model.LocationRecordDAO;
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
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author G4-T6
 */
@WebServlet(name = "location_delete", urlPatterns = {"/json/location-delete"})
public class location_delete extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws java.sql.SQLException
     * @throws is203.JWTException
     * @throws java.text.ParseException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException, JWTException, ParseException {
        response.setContentType("application/JSON");
        PrintWriter out = response.getWriter();
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            JsonObject jsonOutput = new JsonObject();
            JsonArray errorJsonList = new JsonArray();
            
            String token = request.getParameter("token");
            String sharedSecret = "is203g4t6luvjava";
            String username = "";
            try {
                if (token == null) {
                    errorJsonList.add(new JsonPrimitive("invalid token"));
                } else {
                    username = JWTUtility.verify(token, sharedSecret);
                    if (AdminDAO.retrieveAdmin(username) == null) {
                        errorJsonList.add(new JsonPrimitive("invalid token"));
                    }
                }
            } catch (JWTException ex) {
                errorJsonList.add(new JsonPrimitive("invalid token"));
            }
            String startdate = request.getParameter("startdate");
            String enddate = request.getParameter("enddate");
            String starttime = request.getParameter("starttime");
            String endtime = request.getParameter("endtime");
            String mac = request.getParameter("macaddress");
            String idValue = request.getParameter("locationid");
            String semanticPlace = request.getParameter("semanticplace");

            Date start = null;
            Date end = null;
            int locationID = -1;
            if (idValue != null && !idValue.isEmpty()) {
                locationID = Integer.parseInt(idValue);
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            sdf.setLenient(false);
            
            if (startdate == null || startdate.isEmpty()) {//check the mandatory input
                errorJsonList.add(new JsonPrimitive("invalid startdate"));
            } else {
                if (starttime != null && !starttime.isEmpty()) {
                    start = sdf.parse(startdate + " " + starttime);

                } else {
                    start = sdf.parse(startdate + " 00:00");
                }
                if (enddate != null && !enddate.isEmpty()) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    format.setLenient(false);
                    Date s = format.parse(startdate);
                    Date e = format.parse(enddate);
                    if (s.after(e)) {
                        errorJsonList.add(new JsonPrimitive("invalid startdate"));
                    } else {
                        if (endtime == null || endtime.isEmpty()) {
                            end = sdf.parse(enddate + " 00:00");
                        } else {
                            end = sdf.parse(enddate + " " + endtime);
                        }
                        if (start.after(end)) {//check start and end 
                            errorJsonList.add(new JsonPrimitive("invalid starttime"));
                        }
                    }
                } else {
                    if (endtime != null && !endtime.isEmpty()) {//check end time and end date
                        errorJsonList.add(new JsonPrimitive("invalid endtime"));
                    }else{
                        end = sdf.parse(startdate + " 23:59");
                    }
                }

                if (mac != null && !mac.isEmpty()) {
                    if (!mac.matches("[a-fA-F0-9]{40}")) {//check format
                        errorJsonList.add(new JsonPrimitive("invalid macaddress"));
                    } else {//check whether exists
                        if (UserDAO.retrieveUserByMac(mac, conn) == null) {
                            errorJsonList.add(new JsonPrimitive("invalid macaddress"));
                        }
                    }
                }

                if (locationID >= 0) {//user entered the location id
                    String sp = LocationDAO.retrieveSemanticPlace(locationID, conn);
                    if (sp == null) {
                        errorJsonList.add(new JsonPrimitive("invalid location-id"));
                    } else {
                        if (semanticPlace != null && !sp.equals(semanticPlace)) {
                            errorJsonList.add(new JsonPrimitive("invalid location-id"));
                        }
                    }
                }
                
                if(semanticPlace != null && !semanticPlace.isEmpty()){
                    if(!LocationDAO.hasSemanticPlace(semanticPlace, conn)){
                        errorJsonList.add(new JsonPrimitive("invalid semantic place"));
                    }
                }
            }
            if (errorJsonList.size() > 0) {
                jsonOutput.addProperty("status", "error");
                jsonOutput.add("messages", errorJsonList);
            } else {
                JsonArray results = new JsonArray();
                
                HashMap<String, String> input = new HashMap<>();
                input.put("start", sdf.format(start));
                if(end != null){
                    input.put("end", sdf.format(end));
                }
                if(mac != null){
                    input.put("mac", mac);
                }
                if(locationID >= 0){
                    input.put("id", idValue);
                }
                if(semanticPlace != null){
                    input.put("semantic", semanticPlace);
                }
                ArrayList<LocationRecord> deleteRecord = LocationRecordDAO.delete(input, conn);
                for(LocationRecord lr: deleteRecord){
                    int id = lr.getLocationId();
                    String adrs = lr.getMacAddress();
                    String ts = lr.getTimestamp();
                    JsonObject obj = new JsonObject();
                    obj.addProperty("location-id", id);
                    obj.addProperty("mac-address", adrs);
                    obj.addProperty("timestamp", ts);
                    results.add(obj);
                }
                int num = deleteRecord.size();
                jsonOutput.addProperty("status", "success");
                jsonOutput.addProperty("num-record-deleted", num);
                jsonOutput.add("rows-deleted", results);
            }
            out.println(gson.toJson(jsonOutput));
        }finally{
            out.close();
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
            Logger.getLogger(location_delete.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JWTException ex) {
            Logger.getLogger(location_delete.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(location_delete.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(location_delete.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JWTException ex) {
            Logger.getLogger(location_delete.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(location_delete.class.getName()).log(Level.SEVERE, null, ex);
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
