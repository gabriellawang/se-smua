/*
 * This servlet breaks down the display report by the students' demographics after retreving the users' usage duration data
 * The user can select year and/or gender and/or school to break down the display of the report
 * Upon processing, the information will be redirected over to the jsp page and display the result.
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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
@WebServlet(name = "BreakdownTimeDemoReport", urlPatterns = {"/BreakdownTimeDemoReport"})
public class BreakdownTimeDemoReport extends HttpServlet {

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
        Connection conn = null;
        PrintWriter out = response.getWriter();
        try {
            conn = ConnectionManager.getConnection();
            ArrayList<String> errors = new ArrayList<>();
            //get four choices
            String choice1 = request.getParameter("first");
            String choice2 = request.getParameter("second");
            String choice3 = request.getParameter("third");
            String choice4 = request.getParameter("forth");
            //get dates
            String startDate = request.getParameter("start-date");
            String endDate = request.getParameter("end-date");
            //error messages
            String errorMsg = null;
            ArrayList<AppUsage> usages = new ArrayList<>();
            //check if the user did not choose category
            if(startDate.equals("") || endDate.equals(""))
            {
                String dateError = "Invalid Start Date and/or End Date";
                //request.setAttribute("dateError", dateError);
                errors.add(dateError);
                //request.getRequestDispatcher("breakdownDemographics.jsp").forward(request, response);
            }
            
            if(!startDate.equals("") && !endDate.equals(""))
            {
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
                Date startDateFmt = null;
                Date endDateFmt = null;
                try {
                    startDateFmt = fmt.parse(startDate);
                    endDateFmt = fmt.parse(endDate);
                } catch (ParseException e) {
                }

                if (startDateFmt.after(endDateFmt)) {
                    String dateError = "Start date must be before end date";
                    errors.add(dateError);
                }
            }
            
            if (choice1.equals("--Select a category--") && choice2.equals("--Select a category--")
                    && choice3.equals("--Select a category--") && choice4.equals("--Select a category--")) {
                errorMsg = "No category was selected.";
                errors.add(errorMsg);
            } else {//check if there are any duplicate category
                HashSet<String> choices = new HashSet<>();
                int count = 0;
                if (!choice1.equals("--Select a category--")) {
                    choices.add(choice1);
                    count++;
                }
                if (!choice2.equals("--Select a category--")) {
                    choices.add(choice2);
                    count++;
                }
                if (!choice3.equals("--Select a category--")) {
                    choices.add(choice3);
                    count++;
                }
                if (!choice4.equals("--Select a category--")) {
                    choices.add(choice4);
                    count++;
                }
                if (choices.size() < count) {
                    errorMsg = "Duplicate choice. All choices must be different.";
                    errors.add(errorMsg);
                }
            }
            if (errorMsg == null || errorMsg.length() == 0) {
                usages = AppUsageDAO.retrieve(startDate, endDate, conn);
                if (usages.isEmpty()) {
                    errorMsg = "No record during this period.";
                    errors.add(errorMsg);
                }
            }
            if (errors != null && errors.size() > 0) {
                //request.setAttribute("time-demo-error", errorMsg);
                request.setAttribute("errors", errors);
                request.getRequestDispatcher("breakdownDemographics.jsp").forward(request, response);
            } else {
                HashMap<String, Float> individualDuration = BreakdownTimeDemoUtility.getIndividualDuration(usages, startDate, endDate);
                HashMap<User, String> individualCate = BreakdownTimeDemoUtility.convert(individualDuration, conn);
                int totalUser = individualCate.size();
                String[] schools = {"sis", "economics", "socsc", "law", "accountancy", "business"};
                String[] years = {"2011", "2012", "2013", "2014", "2015"};
                String[] genders = {"M", "F"};
                String[] ccas = UserDAO.retrieveCCA(conn);
                String[] empty = {"nil"};
                String[] first;
                String[] second;
                String[] third;
                String[] forth;
                if (choice1.equals("--Select a category--")) {
                    first = empty;
                } else {
                    if (choice1.equals("School")) {
                        first = schools;
                    } else if (choice1.equals("Year")) {
                        first = years;
                    } else if (choice1.equals("Gender")) {
                        first = genders;
                    } else {
                        first = ccas;
                    }
                }
                if (choice2.equals("--Select a category--")) {
                    second = empty;
                } else {
                    if (choice2.equals("School")) {
                        second = schools;
                    } else if (choice2.equals("Year")) {
                        second = years;
                    } else if (choice2.equals("Gender")) {
                        second = genders;
                    } else {
                        second = ccas;
                    }
                }
                if (choice3.equals("--Select a category--")) {
                    third = empty;
                } else {
                    if (choice3.equals("School")) {
                        third = schools;
                    } else if (choice3.equals("Year")) {
                        third = years;
                    } else if (choice3.equals("Gender")) {
                        third = genders;
                    } else {
                        third = ccas;
                    }
                }
                if (choice4.equals("--Select a category--")) {
                    forth = empty;
                } else {
                    if (choice4.equals("School")) {
                        forth = schools;
                    } else if (choice4.equals("Year")) {
                        forth = years;
                    } else if (choice4.equals("Gender")) {
                        forth = genders;
                    } else {
                        forth = ccas;
                    }
                }
                HashMap<String, HashMap<User, String>> cateUserMap = new HashMap<>();
                for (String str1 : first) {
                    for (String str2 : second) {
                        for (String str3 : third) {
                            for (String str4 : forth) {
                                String key = str1 + "," + str2 + "," + str3 + "," + str4;
                                HashMap<User, String> values = new HashMap<>();
                                cateUserMap.put(key, values);
                            }

                        }
                    }
                }
                Set<User> users = individualCate.keySet();
                Iterator uIte = users.iterator();
                while (uIte.hasNext()) {
                    User u = (User) uIte.next();
                    String cate1;
                    String cate2;
                    String cate3;
                    String cate4;
                    if (choice1.equals("--Select a category--")) {
                        cate1 = "nil";
                    } else {
                        if (choice1.equals("School")) {
                            cate1 = u.getSchool();
                        } else if (choice1.equals("Year")) {
                            cate1 = "" + u.getYear();
                        } else if (choice1.equals("Gender")) {
                            cate1 = "" + u.getGender();
                        } else {
                            cate1 = u.getCCA();
                        }
                    }
                    if (choice2.equals("--Select a category--")) {
                        cate2 = "nil";
                    } else {
                        if (choice2.equals("School")) {
                            cate2 = u.getSchool();
                        } else if (choice2.equals("Year")) {
                            cate2 = "" + u.getYear();
                        } else if (choice2.equals("Gender")) {
                            cate2 = "" + u.getGender();
                        } else {
                            cate2 = u.getCCA();
                        }
                    }
                    if (choice3.equals("--Select a category--")) {
                        cate3 = "nil";
                    } else {
                        if (choice3.equals("School")) {
                            cate3 = u.getSchool();
                        } else if (choice3.equals("Year")) {
                            cate3 = "" + u.getYear();
                        } else if (choice3.equals("Gender")) {
                            cate3 = "" + u.getGender();
                        } else {
                            cate3 = u.getCCA();
                        }
                    }
                    if (choice4.equals("--Select a category--")) {
                        cate4 = "nil";
                    } else {
                        if (choice4.equals("School")) {
                            cate4 = u.getSchool();
                        } else if (choice4.equals("Year")) {
                            cate4 = "" + u.getYear();
                        } else if (choice4.equals("Gender")) {
                            cate4 = "" + u.getGender();
                        } else {
                            cate4 = u.getCCA();
                        }
                    }
                    String key = cate1 + "," + cate2 + "," + cate3 + "," + cate4;
                    HashMap<User, String> values = cateUserMap.get(key);
                    if (values != null) {
                        values.put(u, individualCate.get(u));
                    }

                    cateUserMap.put(key, values);
                }
                ArrayList<String> layer1 = new ArrayList<>();
                ArrayList<String> layer2 = new ArrayList<>();
                ArrayList<String> layer3 = new ArrayList<>();
                ArrayList<String> layer4 = new ArrayList<>();
                ArrayList<String> results = new ArrayList<>();

                for (String str1 : first) {
                    int total1 = 0;
                    layer2 = new ArrayList<>();
                    for (String str2 : second) {
                        int total2 = 0;
                        layer3 = new ArrayList<>();
                        for (String str3 : third) {
                            int total3 = 0;
                            layer4 = new ArrayList<>();
                            layer4.add("<ul>");
                            for (String str4 : forth) {
                                String key = str1 + "," + str2 + "," + str3 + "," + str4;
                                HashMap<User, String> values = cateUserMap.get(key);
                                HashMap<String, Integer> counts = BreakdownTimeDemoUtility.convertToCount(values);
                                int iCount = counts.get("Intense");
                                int nCount = counts.get("Normal");
                                int mCount = counts.get("Mild");
                                int total4 = iCount + nCount + mCount;
                                total3 += total4;
                                layer4.add("<li>");
                                if (!str4.equals("nil")) {
                                    layer4.add(str4 + " : " + total4 + "(" + Math.round(100 * ((total4+0.0) / (totalUser+0.0))) + "%)--> ");
                                }
                                layer4.add("Intense User: " + iCount + "(" + Math.round(100 * ((iCount+0.0) / (totalUser+0.0))) + "%), "
                                        + "Normal User: " + nCount + "(" + Math.round(100 * ((nCount+0.0) / (totalUser+0.0))) + "%), "
                                        + "Mild User: " + mCount + "(" + Math.round(100 * ((mCount+0.0) / (totalUser+0.0))) + "%)</li>");
                            }
                            layer4.add("</ul>");
                            total2 += total3;
                            if(!str3.equals("nil")){
                                layer3.add("<ul><li>" + str3 + " : " + total3 + "(" + Math.round(100 * ((total3+0.0) / (totalUser+0.0))) + "%)</li>");
                            }
                            layer3.addAll(layer4);
                            if(!str3.equals("nil")){
                                layer3.add("</ul>");
                            }
                        }
                        total1 += total2;
                        if (!str2.equals("nil")) {
                            layer2.add("<ul><li>" + str2 + " : " + total2 + "(" + Math.round(100 * ((total2+0.0) / (totalUser+0.))) + "%)</li>");
                        }
                        layer2.addAll(layer3);
                        if (!str2.equals("nil")) {
                            layer2.add("</ul>");
                        }
                    }
                    layer1 = new ArrayList<>();
                    if (!str1.equals("nil")) {
                        layer1.add("<ul><li>" + str1 + " : " + total1 + "(" + Math.round(100 * ((total1+0.0) / (totalUser+0.0))) + "%)</li>");
                    }
                    layer1.addAll(layer2);
                    if (!str1.equals("nil")) {
                        layer1.add("</ul>");
                    }
                    results.addAll(layer1);
                }
                request.getSession().setAttribute("results", results);
                response.sendRedirect("breakdownDemographics.jsp");
            }//end

        } finally {
            out.close();
            ConnectionManager.close(conn);
        }//end of finally
    }//end of method

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
            Logger.getLogger(BreakdownTimeDemoReport.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(BreakdownTimeDemoReport.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(BreakdownTimeDemoReport.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(BreakdownTimeDemoReport.class.getName()).log(Level.SEVERE, null, ex);
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
