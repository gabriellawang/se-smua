/*
 * This is the servlet controller of deleting location data
 */
package com.app.controller;

import com.app.model.ConnectionManager;
import com.app.model.LocationDAO;
import com.app.model.LocationRecord;
import com.app.model.LocationRecordDAO;
import com.app.model.UserDAO;
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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author G4-T6
 */
public class LocationDelete extends HttpServlet {

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
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);
            String startdate = request.getParameter("start-date");
            String enddate = request.getParameter("end-date");
            String starttime = request.getParameter("start-time");
            String endtime = request.getParameter("end-time");
            String mac = request.getParameter("mac-address");
            String idValue = request.getParameter("location-id");
            String floor = request.getParameter("floor");
            String location = request.getParameter("location");
            String semanticPlace = null;
            if (!floor.equals("--") && !location.equals("--")) {
                semanticPlace = "SMUSIS" + floor + location;
            }
            ArrayList<String> errorMsg = new ArrayList<>();
            Date start = null;
            Date end = null;
            int locationID = -1;
            if (!idValue.isEmpty()) {
                locationID = Integer.parseInt(idValue);
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            out.println(idValue);
            if (startdate.isEmpty()) {//check the mandatory input
                errorMsg.add("Invalid startdate.");
            } else {
                if (!starttime.isEmpty()) {
                    start = sdf.parse(startdate + " " + starttime);

                } else {
                    start = sdf.parse(startdate + " 00:00");
                }
                if (!enddate.isEmpty()) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    Date s = format.parse(startdate);
                    Date e = format.parse(enddate);
                    if (s.after(e)) {
                        errorMsg.add("Invalid startdate");
                    } else {
                        if (endtime.isEmpty()) {
                            end = sdf.parse(enddate + " 23:59");
                        } else {
                            end = sdf.parse(enddate + " " + endtime);
                        }
                        if (start.after(end)) {//check start and end 
                            errorMsg.add("Invalid starttime");
                        }
                    }

                } else {
                    if (!endtime.isEmpty()) {//check end time and end date
                        errorMsg.add("Invalid endtime");
                    }else{
                        end = sdf.parse(startdate + " 23:59");
                    }
                }

                if (!mac.isEmpty()) {
                    if (!mac.matches("[a-fA-F0-9]{40}")) {//check format
                        errorMsg.add("Invalid macaddress.");
                    } else {//check whether exists
                        if (UserDAO.retrieveUserByMac(mac, conn) == null) {
                            errorMsg.add("Invalid macaddress");
                        }
                    }
                }

                if (locationID >= 0) {//user entered the location id
                    String sp = LocationDAO.retrieveSemanticPlace(locationID, conn);
                    if (sp == null) {
                        errorMsg.add("Invalid location-id");
                    } else {
                        if (semanticPlace != null && !sp.equals(semanticPlace)) {
                            errorMsg.add("Invalid location-id");
                        }
                    }
                }
                /*
                 * for json of location delete, there should be another validation for semantic place.
                 */
            }
            if (!errorMsg.isEmpty()) {
                request.setAttribute("errorMsg", errorMsg);
                request.getRequestDispatcher("location-delete.jsp").forward(request, response);
            } else {
                //start to delete data
                HashMap<String, String> input = new HashMap<>();
                input.put("start", sdf.format(start));
                if(end != null){
                    input.put("end", sdf.format(end));
                }
                if(!mac.isEmpty()){
                    input.put("mac", mac);
                }
                if(locationID >= 0){
                    input.put("id", idValue);
                }
                if(semanticPlace != null){
                    input.put("semantic", semanticPlace);
                }
                ArrayList<LocationRecord> deleteRecord = LocationRecordDAO.delete(input, conn);
                request.getSession().setAttribute("result", deleteRecord);
                response.sendRedirect("location-delete.jsp");
            }
        } finally {
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
            Logger.getLogger(LocationDelete.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(LocationDelete.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(LocationDelete.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(LocationDelete.class.getName()).log(Level.SEVERE, null, ex);
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
