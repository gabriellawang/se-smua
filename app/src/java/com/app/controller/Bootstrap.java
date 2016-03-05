/*
 * Firstly, a zip file selected by the user would be uploaded to a local temp file. Data
 * in the csv files of zip file would be read into the local sql tables. Then, validations
 * are conducted based on the category of the data. After all validations, an ArrayList of
 * the errorMessage Object would be forward to admin_welcome.jsp for display
 */
package com.app.controller;

import com.app.model.AppUsageValidation;
import com.app.model.AppValidation;
import com.app.model.ConnectionManager;
import com.app.model.ErrorMessage;
import com.app.model.LocationRecordValidation;
import com.app.model.LocationValidation;
import com.app.model.Pair;
import com.app.model.UserValidation;
import com.app.model.ZipFileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 *
 * @author G4T6
 */
@MultipartConfig
@WebServlet(name = "Bootstrap", urlPatterns = {"/bootstrap"})
public class Bootstrap extends HttpServlet {

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
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);
            ArrayList<ErrorMessage> errorMsgs = new ArrayList<>();
            HashMap<Boolean, Integer> deleteResult = new HashMap<>();
            String type = request.getParameter("type");
            Part filepart = request.getPart("zipfile");
            int appSize = -1;
            int appLookupSize = -1;
            int demographicsSize = -1;
            int locationSize = -1;
            int locationLookupSize = -1;
            HashMap<String, Integer> numLoaded = new HashMap<>();
            if (type != null && filepart != null) {
                ZipFileReader zfr = new ZipFileReader();
                HashMap<String, List<String[]>> content = null;
                List<List<String[]>> deletion = null;
                Pair<HashMap<String, List<String[]>>, List<List<String[]>>> pair;
                pair = zfr.readFile(filepart);
                content = pair.getFirst();
                deletion = pair.getSecond();
                //validation
                if (type.equals("bootstrap") && content != null) {
                    List<String[]> demographics = content.get("demographics.csv");
                    if (demographics != null) {
                        UserValidation uv = new UserValidation(demographics, conn);
                        errorMsgs.addAll(uv.bootstrap());
                        demographicsSize = uv.getSize();
                    }
                    List<String[]> appLookup = content.get("app-lookup.csv");
                    if (appLookup != null) {
                        AppValidation av = new AppValidation(appLookup, conn);
                        errorMsgs.addAll(av.bootstrap());
                        appLookupSize = av.getSize();
                    }
                    List<String[]> app = content.get("app.csv");
                    if (app != null) {
                        AppUsageValidation auv = new AppUsageValidation(app, conn);
                        errorMsgs.addAll(auv.bootstrap());
                        appSize = auv.getSize();
                    }
                    List<String[]> locationLookup = content.get("location-lookup.csv");
                    if (locationLookup != null) {
                        LocationValidation lv = new LocationValidation(locationLookup, conn);
                        errorMsgs.addAll(lv.bootstrap());
                        locationLookupSize = lv.getSize();
                    }
                    List<String[]> location = content.get("location.csv");
                    if (location != null) {
                        LocationRecordValidation lrv = new LocationRecordValidation(location, conn);
                        errorMsgs.addAll(lrv.bootstrap());
                        locationSize = lrv.getSize();
                    }
                    request.getSession().setAttribute("errorMsgs", errorMsgs);
                    //delete location data
                    if (deletion != null && !deletion.isEmpty()) {
                        LocationRecordValidation lrv = new LocationRecordValidation(deletion.get(0), conn);
                        deleteResult = lrv.deleteLocationData();
                        request.getSession().setAttribute("delete", deleteResult);
                    }
                } else if (type.equals("add-addition") && content != null) {
                    List<String[]> app = content.get("app.csv");
                    if (app != null) {
                        AppUsageValidation auv = new AppUsageValidation(app, conn);
                        errorMsgs.addAll(auv.addAdditionalData());
                        appSize = auv.getAdditionalSize();
                    }

                    List<String[]> demographics = content.get("demographics.csv");
                    if (demographics != null) {
                        UserValidation uv = new UserValidation(demographics, conn);
                        errorMsgs.addAll(uv.addAdditionalData());
                        demographicsSize = uv.getSize();
                    }

                    List<String[]> location = content.get("location.csv");
                    if (location != null) {
                        LocationRecordValidation lrv = new LocationRecordValidation(location, conn);
                        errorMsgs.addAll(lrv.addAdditionalData());
                        locationSize = lrv.getAdditionalSize();
                    }

                    if (deletion != null && !deletion.isEmpty()) {
                        LocationRecordValidation lrv = new LocationRecordValidation(deletion.get(0), conn);
                        deleteResult = lrv.deleteLocationData();
                        request.getSession().setAttribute("delete", deleteResult);
                    }

                    request.getSession().setAttribute("errorMsgs", errorMsgs);
                } else {//deletion
                    if (deletion != null && !deletion.isEmpty()) {
                        LocationRecordValidation lrv = new LocationRecordValidation(deletion.get(0), conn);
                        deleteResult = lrv.deleteLocationData();
                        request.getSession().setAttribute("delete", deleteResult);
                    }
                }
            }
            if(demographicsSize > -1){
                numLoaded.put("demographics.csv", demographicsSize);
            }
            if(appSize > -1){
                numLoaded.put("app.csv", appSize);
            }
            if(appLookupSize > -1){
                numLoaded.put("app-lookup.csv", appLookupSize);
            }
            if(locationSize > -1){
                numLoaded.put("location.csv", locationSize);
            }
            if(locationLookupSize > -1){
                numLoaded.put("location-lookup.csv", locationLookupSize);
            }
            request.getSession().setAttribute("numLoaded", numLoaded);
            response.sendRedirect("bootstrap.jsp?type=" + type);
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
            Logger.getLogger(Bootstrap.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(Bootstrap.class.getName()).log(Level.SEVERE, null, ex);
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
