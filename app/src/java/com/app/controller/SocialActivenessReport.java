/*
 * This servlet returns the SocialActiveness Report given a specific date.
 */
package com.app.controller;

import com.app.model.Activity;
import com.app.model.AppDAO;
import com.app.model.AppUsage;
import com.app.model.AppUsageDAO;
import com.app.model.ConnectionManager;
import com.app.model.LocationRecord;
import com.app.model.LocationRecordDAO;
import com.app.model.Pair;
import com.app.model.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
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
public class SocialActivenessReport extends HttpServlet {

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
        Connection conn = ConnectionManager.getConnection();
        try {
            String date = request.getParameter("date");
            User user = (User) request.getSession().getAttribute("currentUser");
            if (date == null || date.isEmpty()) {
                request.setAttribute("error", "Please enter the date.");
                request.getRequestDispatcher("social-activeness-report.jsp").forward(request, response);
                return;
            }
            //the 1st function: online activeness
            ArrayList<Integer> socialID = AppDAO.retrieveID("social", conn);
            ArrayList<AppUsage> usages = AppUsageDAO.retrieve(date, conn, user.getMacAddress());
            if (usages.isEmpty()) {
                request.getSession().setAttribute("totalDuration", (long) 0);
            } else {
                HashMap<Integer, Long> appIdDuration = SocialActivenessUtility.getAppDuration(usages,date,socialID) ;
                long duration = SocialActivenessUtility.getDuration(appIdDuration);
                TreeMap<String, Float> appDuration = new TreeMap<>();
                Set<Integer> idSet = appIdDuration.keySet();
                Iterator ite = idSet.iterator();
                while (ite.hasNext()) {
                    int id = (Integer) ite.next();
                    long d = appIdDuration.get(id);
                    String name = AppDAO.retrieveName(id, conn);
                    appDuration.put(name, (float) d / duration * 100);
                }
                request.getSession().setAttribute("totalDuration", duration);
                request.getSession().setAttribute("individualDuration", appDuration);
            }

            //the 2nd function: physical activeness
            ArrayList<LocationRecord> records = LocationRecordDAO.retrieveByDate(date, conn);
            HashMap<String, ArrayList<LocationRecord>> userRecordMap = SocialActivenessUtility.sortRecordByUser(records);
            if (userRecordMap.get(user.getMacAddress()) == null) {
                request.getSession().setAttribute("totalTime", (float) 0);
                request.getSession().setAttribute("groupTime", (float) 0);
            } else {
                HashMap<String, ArrayList<Activity>> userActivityMap = SocialActivenessUtility.convert(userRecordMap, date, conn);
                ArrayList<Pair<Date, Date>> groupLog = new ArrayList<>();
                ArrayList<Activity> currentUserActivity = userActivityMap.get(user.getMacAddress());
                userActivityMap.remove(user.getMacAddress());
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
                request.getSession().setAttribute("totalTime", (float) totalTime);
                request.getSession().setAttribute("groupTime", (float) groupTime);
            }
            //redirect to jsp page
            response.sendRedirect("social-activeness-report.jsp");
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
            Logger.getLogger(SocialActivenessReport.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(SocialActivenessReport.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(SocialActivenessReport.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(SocialActivenessReport.class.getName()).log(Level.SEVERE, null, ex);
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
