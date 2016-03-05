
package com.app.controller;

import com.app.model.AppDAO;
import com.app.model.AppUsage;
import com.app.model.AppUsageDAO;
import com.app.model.ConnectionManager;
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
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
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
@WebServlet(name = "TopKAppReport", urlPatterns = {"/TopKAppReport"})
public class TopKAppReport extends HttpServlet {

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
            String kValue = request.getParameter("k");

            String school = request.getParameter("school");
            request.getSession().setAttribute("school", school);
            String startDate = request.getParameter("start-date");
            request.getSession().setAttribute("startDate", startDate);
            String endDate = request.getParameter("end-date");
            request.getSession().setAttribute("endDate", endDate);
            ArrayList<String> errorList = new ArrayList<>();
            ArrayList<String> macList = UserDAO.retrieveMacAddress(school, conn);
            ArrayList<AppUsage> usages = null;

            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (!endDate.equals("") && !startDate.equals("")) {
                Date start = sdf.parse(startDate);
                Date end = sdf.parse(endDate);
                if (start.after(end)) {
                    errorList.add("Invalid date. Start date cannot be after end date.");
                }
            } else {
                errorList.add("Empty date. Please enter the date.");
            }
            if (errorList.isEmpty()) {
                HashMap<String, TreeMap<Long, Integer>> usageMap = new HashMap<>();
                usages = AppUsageDAO.retrieve(macList, startDate, endDate, conn);
                for (AppUsage usage : usages) {
                    String mac = usage.getMacAddress();
                    String timestamp = usage.getTimestamp();
                    Date date = fmt.parse(timestamp);
                    int appid = usage.getId();
                    if (usageMap.get(mac) == null) {
                        TreeMap<Long, Integer> tm = new TreeMap<>();
                        tm.put(date.getTime(), appid);
                        usageMap.put(mac, tm);
                    } else {
                        TreeMap<Long, Integer> tm = usageMap.get(mac);
                        tm.put(date.getTime(), appid);
                        usageMap.put(mac, tm);
                    }
                }

                HashMap<Integer, Long> appDuMap = new HashMap<>();
                Set<String> macSet = usageMap.keySet();
                Iterator ite = macSet.iterator();
                while (ite.hasNext()) {
                    String mac = (String) ite.next();
                    TreeMap<Long, Integer> tm = usageMap.get(mac);
                    Set<Long> timeSet = tm.keySet();
                    Iterator timeIte = timeSet.iterator();
                    long time1 = (long) timeIte.next();
                    long time2;
                    int id1 = tm.get(time1);
                    int id2;
                    long duration = 0;

                    if (!timeIte.hasNext()) {//the last record
                        Date end = fmt.parse(endDate + " 23:59:59");
                        long lastDif = (end.getTime() - time1) / 1000;
                        if (lastDif > 120) {
                            lastDif = 10;
                        }
                        duration += lastDif;
                        if (appDuMap.get(id1) == null) {
                            appDuMap.put(id1, duration);
                        } else {
                            long d = appDuMap.get(id1) + duration;
                            appDuMap.put(id1, d);

                        }
                    } else {
                        while (timeIte.hasNext()) {//go through the treemap<date, appid> of one mac address
                            time2 = (long) timeIte.next();
                            id2 = tm.get(time2);
                            long dif = (time2 - time1) / 1000; // seconds
                            if (dif > 120) {
                                dif = 10;
                            }
                            duration += dif;
                            if (id1 != id2) {
                                if (appDuMap.get(id1) == null) {
                                    appDuMap.put(id1, duration);
                                } else {
                                    long d = appDuMap.get(id1) + duration;
                                    appDuMap.put(id1, d);
                                }
                                duration = 0;
                            }

                            if (!timeIte.hasNext()) {//the last record
                                Date end = fmt.parse(endDate + " 23:59:59");
                                long lastDif = (end.getTime() - time2) / 1000;
                                if (lastDif > 120) {
                                    lastDif = 10;
                                }
                                duration += lastDif;
                                if (appDuMap.get(id2) == null) {
                                    appDuMap.put(id2, duration);
                                } else {
                                    long d = appDuMap.get(id2) + duration;
                                    appDuMap.put(id2, d);
                                }
                            }
                            id1 = id2;
                            time1 = time2;
                        }
                    }
                }
                TreeMap<Long, ArrayList<String>> duAppMap = new TreeMap<>();
                Set<Integer> aSet = appDuMap.keySet();
                Iterator iter = aSet.iterator();
                while (iter.hasNext()) {
                    int id = (int) iter.next();
                    String name = AppDAO.retrieveName(id, conn);
                    long dur = appDuMap.get(id);
                    if (duAppMap.get(dur) == null) {
                        ArrayList<String> iList = new ArrayList<>();
                        iList.add(name);
                        duAppMap.put(dur, iList);
                    } else {
                        ArrayList<String> iList = duAppMap.get(dur);
                        iList.add(name);
                        duAppMap.put(dur, iList);
                    }
                }
                if (duAppMap.isEmpty()) {
                    errorList.add("No data found");
                } else {
                    request.getSession().setAttribute("top-k-popApp", duAppMap);
                    request.getSession().setAttribute("k-value", Integer.parseInt(kValue));
                    response.sendRedirect("top-k-app-display.jsp");
                    return;
                }
            }
            if (errorList.size() > 0) {
                request.setAttribute("errorList", errorList);
                request.getRequestDispatcher("top-k-app-display.jsp").forward(request, response);
            }

        } catch (ParseException ex) {
            Logger.getLogger(TopKAppReport.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(TopKAppReport.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(TopKAppReport.class.getName()).log(Level.SEVERE, null, ex);
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
