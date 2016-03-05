/*
 * This is the servlet controller for break down by time usage
 */
package com.app.controller;

import com.app.model.ConnectionManager;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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
 * @author G4T6
 */
@WebServlet(name = "BreakDownTimeUsage", urlPatterns = {"/BreakDownTimeUsage"})
public class BreakDownTimeUsage extends HttpServlet {

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
        Connection conn = null;
        try (PrintWriter out = response.getWriter()) {
            conn = ConnectionManager.getConnection();
            String startDate = request.getParameter("startDate");
            String endDate = request.getParameter("endDate");

            if (startDate.equals("")
                    && endDate.equals("")) {
                String error = "Please select a start date and an end date";
                request.setAttribute("error", error);
                RequestDispatcher view = request.getRequestDispatcher("breakdownUsageTime.jsp");
                view.forward(request, response);
                return;
            }
            if (startDate == null || startDate.equals("")) {
                String error = "Please select a start date";
                request.setAttribute("error", error);
                RequestDispatcher view = request.getRequestDispatcher("breakdownUsageTime.jsp");
                view.forward(request, response);
                return;
            }
            if (endDate == null || endDate.equals("")) {
                String error = "Please select an end date";
                request.setAttribute("error", error);
                RequestDispatcher view = request.getRequestDispatcher("breakdownUsageTime.jsp");
                view.forward(request, response);
                return;
            }
            
            request.getSession().setAttribute("sd", startDate);
            request.getSession().setAttribute("ed", endDate);
            
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            startDate += " 00:00:00";
            endDate += " 23:59:59";
            
            Date startDateFmt = null;
            Date endDateFmt = null;
            try {
                startDateFmt = fmt.parse(startDate);
                endDateFmt = fmt.parse(endDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (startDateFmt.after(endDateFmt)) {
                String error = "Start date must be before end date";
                request.setAttribute("error", error);
                RequestDispatcher view = request.getRequestDispatcher("breakdownUsageTime.jsp");
                view.forward(request, response);
                return;
            }
            
            HashMap<String, Integer> results = AppUsageUtility.breakdownByUsageTimeCategory(startDate, endDate, conn);
            request.getSession().setAttribute("results", results);
            response.sendRedirect("breakdownUsageTime.jsp"); 
        }finally{
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
            Logger.getLogger(BreakDownTimeUsage.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(BreakDownTimeUsage.class.getName()).log(Level.SEVERE, null, ex);
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
