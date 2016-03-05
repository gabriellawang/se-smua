/*
 * This generates the report for Top-k schools with most app usage.
 * Where user is required to input the app category, start date and end date (both inclusive)
 */
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
import java.util.Collection;
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
@WebServlet(name = "TopKSchoolReport", urlPatterns = {"/TopKSchoolReport"})
public class TopKSchoolReport extends HttpServlet {

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
            String kValue = request.getParameter("k");
            conn = ConnectionManager.getConnection();
            String category = request.getParameter("category");
            request.getSession().setAttribute("category", category);
            String startDate = request.getParameter("start-date");
            request.getSession().setAttribute("startDate", startDate);
            String endDate = request.getParameter("end-date");
            request.getSession().setAttribute("endDate", endDate);
            ArrayList<Integer> idList = null;
            ArrayList<AppUsage> usages = null;
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            HashMap<String, TreeMap<Long, Integer>> usageMap = new HashMap<>();
            //error validation
            ArrayList<String> errorList = new ArrayList<>();
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
                idList = AppDAO.retrieveID(category, conn);
                usages = AppUsageDAO.retrieve(startDate, endDate, conn);
                for (AppUsage usage : usages) {
                    String mac = usage.getMacAddress();
                    String timestamp = usage.getTimestamp();
                    Date date = fmt.parse(timestamp);
                    int appid = usage.getId();
                    if (usageMap.get(mac) == null) {//this mac address hasn't been put into the hashmap
                        TreeMap<Long, Integer> tm = new TreeMap<>();
                        tm.put(date.getTime(), appid);
                        usageMap.put(mac, tm);
                    } else {//this mac address already exists in the hashmap
                        TreeMap<Long, Integer> tm = usageMap.get(mac);
                        tm.put(date.getTime(), appid);
                        usageMap.put(mac, tm);
                    }
                }

                HashMap<String, Long> macDuMap = new HashMap<>();
                //<MacAddress, Duration>
                Set<String> macSet = usageMap.keySet();
                Iterator ite = macSet.iterator();

                while (ite.hasNext()) {//for a mac address:
                    HashMap<Integer, Long> appDuMap = new HashMap<>();
                    //<appid, duration>
                    String mac = (String) ite.next();
                    TreeMap<Long, Integer> tm = usageMap.get(mac);
                    //<date, appid>
                    Set<Long> timeSet = tm.keySet();
                    Iterator timeIte = timeSet.iterator();
                    long time1 = (long) timeIte.next();
                    long time2;
                    int id1 = tm.get(time1);
                    int id2;
                    long duration = 0;
                    if (!timeIte.hasNext()) { //check if only 1 app usage
                        Date end = fmt.parse(endDate + " 23:59:59");
                        long lastDif = (end.getTime() - time1) / 1000;
                        if (lastDif > 120) {
                            lastDif = 10;
                        }
                        duration += lastDif;
                        if (idList.contains(id1)) {
                            if (appDuMap.get(id1) == null) {
                                appDuMap.put(id1, duration);
                            } else {
                                long d = appDuMap.get(id1) + duration;
                                appDuMap.put(id1, d);
                            }
                        }
                    } else { //
                        while (timeIte.hasNext()) {//go through the treemap<date, appid> of one mac address
                            time2 = (long) timeIte.next();
                            id2 = tm.get(time2);
                            long dif = (time2 - time1) / 1000; // seconds
                            if (dif > 120) {
                                dif = 10;
                            }
                            duration += dif;
                            if (id1 != id2) { // different application
                                if (idList.contains(id1)) {
                                    if (appDuMap.get(id1) == null) {
                                        appDuMap.put(id1, duration);
                                    } else {
                                        long d = appDuMap.get(id1) + duration;
                                        appDuMap.put(id1, d);
                                    }
                                    duration = 0;
                                }
                            }

                            if (!timeIte.hasNext()) {//the last record
                                Date end = fmt.parse(endDate + " 23:59:59");
                                long lastDif = (end.getTime() - time2) / 1000;
                                if (lastDif > 120) {
                                    lastDif = 10;
                                }
                                duration += lastDif;
                                if (idList.contains(id2)) {
                                    if (appDuMap.get(id2) == null) {
                                        appDuMap.put(id2, duration);
                                    } else {
                                        long d = appDuMap.get(id2) + duration;
                                        appDuMap.put(id2, d);
                                    }
                                }
                            }
                            id1 = id2;
                            time1 = time2;
                        }
                    }

                    //System.out.println(appDuMap.size());
                    Collection<Long> duCollect = appDuMap.values();
                    Iterator duIte = duCollect.iterator();
                    long totalDu = 0;
                    while (duIte.hasNext()) {
                        long dur = (long) duIte.next();
                        totalDu += dur;
                    }
                    macDuMap.put(mac, totalDu);

                }
                //convert the mac-duration map into school-duration hashmap
                Set<String> mSet = macDuMap.keySet();
                Iterator iter = mSet.iterator();
                HashMap<String, Long> schoolDuMap = new HashMap<>();//<school, duration>
                while (iter.hasNext()) {
                    String macAdr = (String) iter.next();
                    long dur = macDuMap.get(macAdr);
                    String school = UserDAO.retrieveSchool(macAdr, conn);
                    if (schoolDuMap.get(school) == null) {
                        schoolDuMap.put(school, dur);
                    } else {
                        long d = schoolDuMap.get(school);
                        schoolDuMap.put(school, dur + d);
                    }
                }
                //convert school-duration map into duration-school treemap
                TreeMap<Long, ArrayList<String>> duSchoolMap = new TreeMap<>();
                Set<String> schSet = schoolDuMap.keySet();
                Iterator schIte = schSet.iterator();
                while (schIte.hasNext()) {
                    String school = (String) schIte.next();
                    long dur = schoolDuMap.get(school);
                    ArrayList<String> schools = new ArrayList<>();
                    if (duSchoolMap.get(dur) == null) {
                        schools.add(school);
                        duSchoolMap.put(dur, schools);
                    } else {
                        schools = duSchoolMap.get(dur);
                        schools.add(school);
                        duSchoolMap.put(dur, schools);
                    }
                }
                if (!duSchoolMap.isEmpty()) {
                    request.getSession().setAttribute("top-k-school", duSchoolMap);
                    request.getSession().setAttribute("k-value", Integer.parseInt(kValue));
                    response.sendRedirect("top-k-school-display.jsp");
                }else{
                    errorList.add("No data found");
                }
            }
            if (errorList.size() > 0) {
                request.setAttribute("errorList", errorList);
                request.getRequestDispatcher("top-k-school-display.jsp").forward(request, response);
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
        } catch (ParseException ex) {
            Logger.getLogger(TopKStudentReport.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(TopKSchoolReport.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(TopKStudentReport.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(TopKSchoolReport.class.getName()).log(Level.SEVERE, null, ex);
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
