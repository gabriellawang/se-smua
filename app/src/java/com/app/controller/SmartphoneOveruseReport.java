/*
 * This generates the SmartphoneOveruseReport by analysing the user's
 * Average daily smartphone usage duration, Average daily gaming duration and Smartphone access frequency
 */
package com.app.controller;

import com.app.model.AppDAO;
import com.app.model.AppUsage;
import com.app.model.AppUsageDAO;
import com.app.model.ConnectionManager;
import com.app.model.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
@WebServlet(name = "SmartphoneOveruseReport", urlPatterns = {"/SmartphoneOveruseReport"})
public class SmartphoneOveruseReport extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws java.text.ParseException
     * @throws java.sql.SQLException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, ParseException, SQLException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            User user = (User) request.getSession().getAttribute("currentUser");
            ArrayList<String> macaddress = new ArrayList<>();
            macaddress.add(user.getMacAddress());
            String startDate = request.getParameter("start-date");
            request.getSession().setAttribute("startDate", startDate);
            String endDate = request.getParameter("end-date");
            request.getSession().setAttribute("endDate", endDate);
            if(startDate.isEmpty() || endDate.isEmpty()){
                request.setAttribute("error", "Invalid start date and/or end date");
                request.getRequestDispatcher("smartphone-overuse.jsp").forward(request, response);
            }
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
            Date start = fmt.parse(startDate);
            Date end = fmt.parse(endDate);
            if(start.after(end)){
                request.setAttribute("error", "Start date must be before end date");
                request.getRequestDispatcher("smartphone-overuse.jsp").forward(request, response);
                return;
            }
            ArrayList<AppUsage> usages = AppUsageDAO.retrieve(macaddress, startDate, endDate, conn);
            System.out.println("usages size" + usages.size());
            char duration;
            char gameDuration;
            char frequency;
            
            long days = SmartphoneOveruseUtility.getDays(startDate, endDate);
            
            float totalDuration = (float)SmartphoneOveruseUtility.getDuration(usages, endDate);
            float durationValue = totalDuration / days;
            int durationValueSecs= Math.round(durationValue); 
            request.getSession().setAttribute("durationValue", durationValueSecs);
            if(durationValue >= 5*3600){
                duration = 'S';
            }else if(durationValue >= 3*3600){
                duration = 'M';
            }else{
                duration = 'L'; 
            }
            request.getSession().setAttribute("duration", duration);
            
            ArrayList<Integer> gameID = AppDAO.retrieveID("games", conn);
            ArrayList<AppUsage> gameUsage = AppUsageDAO.retrieve(macaddress.get(0), startDate, endDate, gameID, conn);
            System.out.println("game usage: " + gameUsage.size());
            float totalGameDuration = (float)SmartphoneOveruseUtility.getDuration(gameUsage, endDate);
            float gameDurationValue = totalGameDuration / days;
            int gameDurationValueSec = Math.round(gameDurationValue);
            request.getSession().setAttribute("gameDurationValue", gameDurationValueSec);
            if(gameDurationValue >= 2*3600){
                gameDuration = 'S';
            }else if(gameDurationValue >= 1*3600){
                gameDuration = 'M';
            }else{
                gameDuration = 'L'; 
            }
            request.getSession().setAttribute("gameDuration", gameDuration);
            
            float frequencyValue = SmartphoneOveruseUtility.getFrequency(usages, endDate, days);
            request.getSession().setAttribute("frequencyValue", frequencyValue);
            if(frequencyValue >= 5){
                frequency = 'S';
            }else if(frequencyValue >= 3){
                frequency = 'M';
            }else{
                frequency = 'L'; 
            }
            request.getSession().setAttribute("frequency", frequency);
            response.sendRedirect("smartphone-overuse.jsp");
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
        } catch (ParseException ex) {
            Logger.getLogger(SmartphoneOveruseReport.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(SmartphoneOveruseReport.class.getName()).log(Level.SEVERE, null, ex);
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
        } catch (ParseException ex) {
            Logger.getLogger(SmartphoneOveruseReport.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(SmartphoneOveruseReport.class.getName()).log(Level.SEVERE, null, ex);
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
