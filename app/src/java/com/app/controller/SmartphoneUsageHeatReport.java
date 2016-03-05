/*
 * This servlet returns the Smartphone Usage Heatmap Report given a selected floor,date and time.
 */
package com.app.controller;

import com.app.model.AppUsage;
import com.app.model.AppUsageDAO;
import com.app.model.ConnectionManager;
import com.app.model.LocationDAO;
import com.app.model.LocationRecord;
import com.app.model.LocationRecordDAO;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author G4-T6
 */
@WebServlet(name = "SmartphoneUsageHeatReport", urlPatterns = {"/SmartphoneUsageHeatReport"})
public class SmartphoneUsageHeatReport extends HttpServlet {

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
            ArrayList<String> errors = new ArrayList<>();
            String level = request.getParameter("level");
            String cDate = request.getParameter("currentdate");
            String cTime = request.getParameter("currenttime");
            if(cTime.length() < 7) {
                cTime = cTime + ":00";
            }
            
            if(level == null || level.equals(""))
            {
                String error = "Level cannot be empty";
                errors.add(error);
            }
            if(cDate == null || cDate.equals(""))
            {
                String error = "Current date cannot be empty";
                errors.add(error);
            }
            if(cTime == null || cTime.equals(""))
            {
                String error = "Current time cannot be empty";
                errors.add(error);
            }
            
            if(errors != null && errors.size() > 0)
            {
                request.setAttribute("errors", errors);
                RequestDispatcher view = request.getRequestDispatcher("smartphone-heatmap.jsp");
                view.forward(request, response);
                return;
            }
            
            if(!level.equals("--Select level--")) {
                String date = cDate + " " + cTime;
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date endDateTemp = fmt.parse(date);
                Date endDate = new Date(endDateTemp.getTime() - 1000);
                Date startDate = new Date();
                startDate.setTime(endDate.getTime()-60*15*1000);
                
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
                request.getSession().setAttribute("sematicCountMap", sematicCountMap);
                response.sendRedirect("smartphone-heatmap.jsp");
            }
        } catch(Exception e) {
            System.out.println("Error occurs");
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
            Logger.getLogger(SmartphoneUsageHeatReport.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(SmartphoneUsageHeatReport.class.getName()).log(Level.SEVERE, null, ex);
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
