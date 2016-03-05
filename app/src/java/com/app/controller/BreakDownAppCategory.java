/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.app.controller;

import com.app.model.AppUsage;
import com.app.model.AppUsageDAO;
import com.app.model.ConnectionManager;
import com.app.model.User;
import com.app.model.UserDAO;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
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
@WebServlet(name = "BreakDownAppCategory", urlPatterns = {"/BreakDownAppCategory"})
public class BreakDownAppCategory extends HttpServlet {

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

            if (startDate.equals("") && endDate.equals("")) {
                String error = "Please select a start date and an end date";
                request.setAttribute("error", error);
                RequestDispatcher view = request.getRequestDispatcher("breakdownAppCategory.jsp");
                view.forward(request, response);
                return;
            }
            if (startDate == null || startDate.equals("")) {
                String error = "Please select a start date";
                request.setAttribute("error", error);
                RequestDispatcher view = request.getRequestDispatcher("breakdownAppCategory.jsp");
                view.forward(request, response);
                return;
            }
            if (endDate == null || endDate.equals("")) {
                String error = "Please select an end date";
                request.setAttribute("error", error);
                RequestDispatcher view = request.getRequestDispatcher("breakdownAppCategory.jsp");
                view.forward(request, response);
                return;
            }

            request.getSession().setAttribute("startDate", startDate);
            request.getSession().setAttribute("endDate", endDate);

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
                RequestDispatcher view = request.getRequestDispatcher("breakdownAppCategory.jsp");
                view.forward(request, response);
                return;
            }

            TreeMap<String, Integer> appMap = new TreeMap<String, Integer>();
            ArrayList<AppUsage> fullAppusageList = AppUsageDAO.retrieveAllAppUsageOnDates(startDateFmt, endDateFmt, conn);
            ArrayList<User> userList = UserDAO.retrieveAll(conn);

            ArrayList<String> categoryList = new ArrayList<>();
            categoryList.add("books");
            categoryList.add("social");
            categoryList.add("education");
            categoryList.add("entertainment");
            categoryList.add("information");
            categoryList.add("library");
            categoryList.add("local");
            categoryList.add("tools");
            categoryList.add("fitness");
            categoryList.add("games");
            categoryList.add("others");
            for (String category : categoryList) {
                appMap.put(category, 0);
            }

            if (fullAppusageList == null || fullAppusageList.isEmpty()) {
                request.getSession().setAttribute("appMap", appMap);
                response.sendRedirect("breakdownAppCategory.jsp");
                return;
            }

            ArrayList<String> resultList = new ArrayList<>();
            int totalTimeSpent = 0;
            long days = 0;
            for (int i = 0; i < userList.size(); i++) {
                User user = userList.get(i);
                String userMacAddr = user.getMacAddress();

                for (int x = 0; x < fullAppusageList.size(); x++) {
                    AppUsage appusage = fullAppusageList.get(x);
                    String appMacAddr = appusage.getMacAddress();

                    if (userMacAddr.equals(appMacAddr)) {
                        ArrayList<String> matchList = AppUsageDAO.retrieveTimestampAndCategory(startDateFmt, endDateFmt, userMacAddr, appMacAddr, conn);
                        for (String result : matchList) {
                            resultList.add(result);
                        }
                        break;
                    }
                }
            }
            Collections.sort(resultList);
            
            long daysTp = endDateFmt.getTime() - startDateFmt.getTime();
            days = TimeUnit.MILLISECONDS.toDays(daysTp);
            days += 1;
            String noOfDays = days + "";
            request.getSession().setAttribute("days", noOfDays);
            
            String beforeTp = resultList.get(0).substring(0, (resultList.get(0).indexOf(",") - 1));
            String firstCategory = resultList.get(0).substring(resultList.get(0).indexOf(",") + 1, resultList.get(0).length());
            Date timeBeforeUsed = null;
            try {
                timeBeforeUsed = fmt.parse(beforeTp);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            int countSecs = 0;
            for (int z = 1; z < resultList.size(); z++) {
                String category = resultList.get(z).substring(resultList.get(z).indexOf(",") + 1, resultList.get(z).length());
                String afterTp = resultList.get(z).substring(0, (resultList.get(z).indexOf(",") - 1));
                Date timeAfterUsed = null;
                try {
                    timeAfterUsed = fmt.parse(afterTp);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                try {
                    if (category.equals(firstCategory)) {
                        if (timeBeforeUsed.getHours() == 23 && timeAfterUsed.getHours() == 0) {
                            Date temp = timeBeforeUsed;
                            String tempDate = fmt.format(temp);
                            tempDate = tempDate.substring(0, tempDate.indexOf(" "));
                            tempDate += " 23:59:59";
                            temp = fmt.parse(tempDate);
                            long diff = (temp.getTime() - timeBeforeUsed.getTime());
                            long diffSecs = (TimeUnit.MILLISECONDS.toSeconds(diff)) + 1;
                            if (diffSecs < 121) {
                                countSecs = (int) diffSecs;
                            } else {
                                countSecs = 10;
                            }
                        } else {
                            long diff = timeAfterUsed.getTime() - timeBeforeUsed.getTime();
                            long diffSecs = TimeUnit.MILLISECONDS.toSeconds(diff);

                            if (diffSecs < 121) {
                                countSecs = (int) diffSecs;
                            } else {
                                countSecs = 10;
                            }
                        }
                        int valueSecs = (int) appMap.get(firstCategory);
                        valueSecs += countSecs;
                        appMap.put(firstCategory, valueSecs);
                        totalTimeSpent += countSecs;

                    } else {
                        if (timeBeforeUsed.getHours() == 23 && timeAfterUsed.getHours() == 0) {
                            Date temp = timeBeforeUsed;
                            String tempDate = fmt.format(temp);
                            tempDate = tempDate.substring(0, tempDate.indexOf(" "));
                            tempDate += " 23:59:59";
                            temp = fmt.parse(tempDate);
                            long diff = (temp.getTime() - timeBeforeUsed.getTime());
                            long diffSecs = (TimeUnit.MILLISECONDS.toSeconds(diff)) + 1;
                            if (diffSecs < 121) {
                                countSecs = (int) diffSecs;
                            } else {
                                countSecs = 10;
                            }
                        } else {
                            long diff = timeAfterUsed.getTime() - timeBeforeUsed.getTime();
                            long diffSecs = TimeUnit.MILLISECONDS.toSeconds(diff);

                            if (diffSecs < 121) {
                                countSecs = (int) diffSecs;
                            } else {
                                countSecs = 10;
                            }
                        }
                        int valueSecs = (int) appMap.get(firstCategory);
                        valueSecs += countSecs;
                        appMap.put(firstCategory, valueSecs);
                        totalTimeSpent += countSecs;
                    }

                    if (z == (resultList.size() - 1)) {
                        if (timeAfterUsed.getHours() == 23 && timeAfterUsed.getMinutes() >= 58) {
                            Date temp = timeAfterUsed;
                            String tempDate = fmt.format(temp);
                            tempDate = tempDate.substring(0, tempDate.indexOf(" "));
                            tempDate += " 23:59:59";
                            temp = fmt.parse(tempDate);
                            long diff = (temp.getTime() - timeAfterUsed.getTime());
                            long diffSecs = TimeUnit.MILLISECONDS.toSeconds(diff) + 1;
                            countSecs = (int) diffSecs;
                        } else {
                            countSecs = 10;
                        }
                        int valueSecs = (int) appMap.get(category);
                        valueSecs += countSecs;
                        appMap.put(category, valueSecs);
                        totalTimeSpent += countSecs;
                    }
                    timeBeforeUsed = timeAfterUsed;
                    firstCategory = category;
                    countSecs = 0;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
                        
            String totalSecs = totalTimeSpent + "";
            request.getSession().setAttribute("totalSecs", totalSecs);
            request.getSession().setAttribute("appMap", appMap);
            response.sendRedirect("breakdownAppCategory.jsp");
            out.close();
        } finally {
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
            Logger.getLogger(BreakDownAppCategory.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(BreakDownAppCategory.class.getName()).log(Level.SEVERE, null, ex);
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
