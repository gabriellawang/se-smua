/*
 * This is the servlet controller for break down by diurnal pattern.
 */
package com.app.controller;

import com.app.model.AppUsage;
import com.app.model.AppUsageDAO;
import com.app.model.ConnectionManager;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
 * @author G4t6
 */
@WebServlet(name = "BreakDownDiurnalPattern", urlPatterns = {"/BreakDownDiurnalPattern"})
public class BreakDownDiurnalPattern extends HttpServlet {

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
        Connection conn = null;
        try (PrintWriter out = response.getWriter()) {
            conn = ConnectionManager.getConnection();
            String gender = request.getParameter("gender");
            String school = request.getParameter("school");
            String year = request.getParameter("year");
            String date = request.getParameter("date");
            if (date==null || date.equals("")) {
                String errorMsg = "Please select a date.";
                request.setAttribute("error", errorMsg);
                RequestDispatcher view = request.getRequestDispatcher("diurnalPatternAppUsage.jsp");
                view.forward(request, response);
                return;
            }

            ArrayList<AppUsage> usageList = AppUsageDAO.retrieveAppUsageGivenCriteria(year, school, gender, date, conn);
            HashMap<String, ArrayList<AppUsage>> usageMap = new HashMap();
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            // Results to be sent back
            HashMap<Integer, Integer> results = new HashMap();
            for (int i = 0 ; i < 24; i++) {
                results.put(i, 0);
            }
            
            for (AppUsage usage: usageList) {
                String mac = usage.getMacAddress();

                ArrayList<AppUsage> uList;
                if (usageMap.get(mac) == null) {
                    uList = new ArrayList<>();
                    uList.add(usage);
                    usageMap.put(mac, uList);
                } else {
                    uList = usageMap.get(mac);
                    uList.add(usage);
                    usageMap.put(mac, uList);
                }
            }
            
            
            int noOfUsers = usageMap.size();
            Iterator it = usageMap.keySet().iterator();
            
            
            while (it.hasNext()) {
                String macAddress = (String) it.next();
                ArrayList<AppUsage> userAppUsage = usageMap.get(macAddress);
                Collections.sort(userAppUsage);
                
                
                long duration = 0;
                
                Date d1 = userAppUsage.get(0).getDate();
                for (int i = 1; i < userAppUsage.size(); i++) {
                    AppUsage b = userAppUsage.get(i);

                    Date d2 = b.getDate();
                    long t1 = d1.getTime();
                    long t2 = d2.getTime();
                    if (d1.getHours()!=d2.getHours()) {
                        Date temp = d1;
                        temp.setMinutes(59);
                        temp.setSeconds(59);
                        long diff = ((temp.getTime() - t1)/1000)+1;
                        if (diff > 120) {
                            diff = 10;
                        }
                        duration+=diff;
                        //long hourTemp = results.get(d1.getHours());
                        //hourTemp += duration;
                        results.put(d1.getHours(),(int)duration);
                        duration = 0;
                    } else {
                        long diff = (t2 - t1)/1000;
                        if (diff > 120) {
                            diff = 10;
                        }
                        duration+=diff;
                        //long hourTemp = results.get(d1.getHours());
                        //hourTemp += duration;
                        results.put(d1.getHours(),(int)duration);
                        // Last record
                        if (i==(userAppUsage.size()-1)) {
                            Date temp = d2;
                            temp.setMinutes(59);
                            temp.setSeconds(59);
                            long tempDiff = ((temp.getTime() - t2)/1000)+1;
                            long lastDiff = 10;
                            if (tempDiff < 10) {
                                lastDiff = tempDiff;
                            }
     
                            duration+=lastDiff;
                            //long lastHourTemp = results.get(d1.getHours());
                            //lastHourTemp += duration;
                            results.put(d1.getHours(),(int)duration);
                        }   
                    }
                    d1 = d2;
                    t1 = t2;
                }
            }
            
            for (int i = 0; i < 24; i++) {
                int totalHours = results.get(i);

                if (noOfUsers==0) {
                    noOfUsers = 1;
                }
                float avg = (float)totalHours / noOfUsers;
                int averageHours = Math.round(avg);
                results.put(i, averageHours);
            }
            
            request.getSession().setAttribute("results", results);
            response.sendRedirect("diurnalPatternAppUsage.jsp");
            //RequestDispatcher view = request.getRequestDispatcher("diurnalPatternAppUsage.jsp");
            //view.forward(request, response);
            
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
            Logger.getLogger(BreakDownDiurnalPattern.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(BreakDownDiurnalPattern.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(BreakDownDiurnalPattern.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(BreakDownDiurnalPattern.class.getName()).log(Level.SEVERE, null, ex);
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
