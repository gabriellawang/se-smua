/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.app.json;

import com.app.controller.BreakdownTimeDemoUtility;
import com.app.model.AppUsage;
import com.app.model.AppUsageDAO;
import com.app.model.ConnectionManager;
import com.app.model.User;
import com.app.model.UserDAO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import is203.JWTException;
import is203.JWTUtility;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author G4-T6
 */
public class basic_usetime_demographics_report extends HttpServlet {

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
    @SuppressWarnings("empty-statement")
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException, ParseException {
        response.setContentType("application/JSON");
        Connection conn = null;
        try (PrintWriter out = response.getWriter()) {
            conn = ConnectionManager.getConnection();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            //creats a new json object for printing the desired json output
            JsonObject jsonOutput = new JsonObject();
            JsonArray errorJsonList = new JsonArray();
            //retrieves the user
            String token = request.getParameter("token");
            //check token
            String sharedSecret = "is203g4t6luvjava";
            String username = "";

            if (token == null) {
                errorJsonList.add(new JsonPrimitive("missing token"));
            } else if (token.equals("")) {
                errorJsonList.add(new JsonPrimitive("blank token"));
            } else {
                try {
                    username = JWTUtility.verify(token, sharedSecret);
                } catch (JWTException ex) {
                    errorJsonList.add(new JsonPrimitive("invalid token"));
                }
            }

            //check start date and end date input
            String startdate = request.getParameter("startdate");
            String enddate = request.getParameter("enddate");

            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
            fmt.setLenient(false);
            Date startDateFmt = null;
            Date endDateFmt = null;
            if (startdate == null || startdate.equals("")) {
                // error output              
                errorJsonList.add(new JsonPrimitive("invalid startdate"));

            } else {
                try {
                    startDateFmt = fmt.parse(startdate);
                } catch (ParseException ex) {
                    errorJsonList.add(new JsonPrimitive("invalid startdate"));
                }
                if (enddate == null || enddate.equals("")) {
                    // error output
                    errorJsonList.add(new JsonPrimitive("invalid enddate"));
                } else {
                    try {
                        endDateFmt = fmt.parse(enddate);
                    } catch (ParseException ex) {
                        errorJsonList.add(new JsonPrimitive("invalid enddate"));
                    }
                }

                if (startDateFmt != null && endDateFmt != null && startDateFmt.after(endDateFmt)) {
                    errorJsonList.add(new JsonPrimitive("invalid startdate"));
                }
            }
            //check the order input
            String order = request.getParameter("order");
            JsonPrimitive jp = new JsonPrimitive("invalid order");
            String[] orderArr = null;
            String choice1 = "";
            String choice2 = "";
            String choice3 = "";
            String choice4 = "";
            String[] checker = {"year", "gender", "school", "cca", ""};
            ArrayList<String> checkList = new ArrayList<>(Arrays.asList(checker));
            if (order.isEmpty()) {
                errorJsonList.add(jp);//empty order
            } else {
                try {
                    orderArr = order.split(",");
                    ArrayList<String> orders = new ArrayList<>(Arrays.asList(orderArr));
                    ArrayList<String> empty = new ArrayList<>();
                    empty.add("");
                    orders.removeAll(empty);
                    orderArr = (String[]) orders.toArray(new String[orders.size()]);
                    if (orderArr.length == 0) {
                        errorJsonList.add(jp);
                    } else if (orderArr.length == 1) {
                        choice4 = orderArr[0];
                    } else if (orderArr.length == 2) {
                        choice3 = orderArr[0];
                        choice4 = orderArr[1];
                    } else if (orderArr.length == 3) {
                        choice2 = orderArr[0];
                        choice3 = orderArr[1];
                        choice4 = orderArr[2];
                    } else if (orderArr.length == 4) {
                        choice1 = orderArr[0];
                        choice2 = orderArr[1];
                        choice3 = orderArr[2];
                        choice4 = orderArr[3];
                    }
                } catch (PatternSyntaxException e) {
                    errorJsonList.add(jp);//wrong format
                    e.printStackTrace();
                }

                if (!errorJsonList.contains(jp)) {
                    if (choice4.equals("")) {
                        errorJsonList.add(jp);
                    } else if (!checkList.contains(choice1) || !checkList.contains(choice2) || !checkList.contains(choice3) || !checkList.contains(choice4)) {
                        errorJsonList.add(jp);
                    }
                }
                if (!errorJsonList.contains(jp)) {//check if there are any duplicate category
                    HashSet<String> choices = new HashSet<>();
                    int count = 0;
                    if (!choice1.equals("")) {
                        choices.add(choice1);
                        count++;
                    }
                    if (!choice2.equals("")) {
                        choices.add(choice2);
                        count++;
                    }
                    if (!choice3.equals("")) {
                        choices.add(choice3);
                        count++;
                    }
                    if (!choice4.equals("")) {
                        choices.add(choice4);
                        count++;
                    }
                    if (choices.size() < count) {
                        errorJsonList.add(jp);
                    }
                }
            }
            ArrayList<AppUsage> usages = new ArrayList<>();
            //check status
            if (errorJsonList.size() > 0) {
                jsonOutput.addProperty("status", "error");
                jsonOutput.add("messages", errorJsonList);
            } else {
                jsonOutput.addProperty("status", "success");
                usages = AppUsageDAO.retrieve(startdate, enddate, conn);

                HashMap<String, Float> individualDuration = BreakdownTimeDemoUtility.getIndividualDuration(usages, startdate, enddate);
                HashMap<User, String> individualCate = BreakdownTimeDemoUtility.convert(individualDuration, conn);
                int totalUser = individualCate.size();
                if (totalUser == 0) {
                    totalUser = 1;
                }
                TreeSet<String> sch = new TreeSet<>();
                String[] schools = {"sis", "economics", "socsc", "law", "accountancy", "business"};
                sch.addAll(new ArrayList<String>(Arrays.asList(schools)));
                schools = (String[]) sch.toArray(new String[sch.size()]);
                String[] years = {"2011", "2012", "2013", "2014", "2015"};
                String[] genders = {"F", "M"};
                String[] ccas = UserDAO.retrieveCCA(conn);
                Arrays.sort(ccas);
                String[] empty = {""};
                String[] first;
                String[] second;
                String[] third;
                String[] forth;
                if (choice1.equals("")) {
                    first = empty;
                } else {
                    if (choice1.equals("school")) {
                        first = schools;
                    } else if (choice1.equals("year")) {
                        first = years;
                    } else if (choice1.equals("gender")) {
                        first = genders;
                    } else {
                        first = ccas;
                    }
                }

                if (choice2.equals("")) {
                    second = empty;
                } else {
                    if (choice2.equals("school")) {
                        second = schools;
                    } else if (choice2.equals("year")) {
                        second = years;
                    } else if (choice2.equals("gender")) {
                        second = genders;
                    } else {
                        second = ccas;
                    }
                }

                if (choice3.equals("")) {
                    third = empty;
                } else {
                    if (choice3.equals("school")) {
                        third = schools;
                    } else if (choice3.equals("year")) {
                        third = years;
                    } else if (choice3.equals("gender")) {
                        third = genders;
                    } else {
                        third = ccas;
                    }
                }

                if (choice4.equals("")) {
                    forth = empty;
                } else {
                    if (choice4.equals("school")) {
                        forth = schools;
                    } else if (choice4.equals("year")) {
                        forth = years;
                    } else if (choice4.equals("gender")) {
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
                    if (choice1.equals("")) {
                        cate1 = "";
                    } else {
                        if (choice1.equals("school")) {
                            cate1 = u.getSchool();
                        } else if (choice1.equals("year")) {
                            cate1 = "" + u.getYear();
                        } else if (choice1.equals("gender")) {
                            cate1 = "" + u.getGender();
                        } else {
                            cate1 = u.getCCA();
                        }
                    }
                    if (choice2.equals("")) {
                        cate2 = "";
                    } else {
                        if (choice2.equals("school")) {
                            cate2 = u.getSchool();
                        } else if (choice2.equals("year")) {
                            cate2 = "" + u.getYear();
                        } else if (choice2.equals("gender")) {
                            cate2 = "" + u.getGender();
                        } else {
                            cate2 = u.getCCA();
                        }
                    }
                    if (choice3.equals("")) {
                        cate3 = "";
                    } else {
                        if (choice3.equals("school")) {
                            cate3 = u.getSchool();
                        } else if (choice3.equals("year")) {
                            cate3 = "" + u.getYear();
                        } else if (choice3.equals("gender")) {
                            cate3 = "" + u.getGender();
                        } else {
                            cate3 = u.getCCA();
                        }
                    }
                    if (choice4.equals("")) {
                        cate4 = "";
                    } else {
                        if (choice4.equals("school")) {
                            cate4 = u.getSchool();
                        } else if (choice4.equals("year")) {
                            cate4 = "" + u.getYear();
                        } else if (choice4.equals("gender")) {
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
                JsonArray j1 = new JsonArray();
                JsonArray j2 = new JsonArray();
                JsonArray j3 = new JsonArray();
                JsonArray j4 = new JsonArray();
                String[] keys = null;
                for (String str1 : first) {
                    int total1 = 0;
                    j2 = new JsonArray();
                    for (String str2 : second) {
                        int total2 = 0;
                        j3 = new JsonArray();
                        for (String str3 : third) {
                            int total3 = 0;
                            j4 = new JsonArray();
                            for (String str4 : forth) {
                                String key = str1 + "," + str2 + "," + str3 + "," + str4;
                                keys = new String[4];
                                keys[0] = str1;
                                keys[1] = str2;
                                keys[2] = str3;
                                keys[3] = str4;
                                ArrayList<String> kList = new ArrayList<>(Arrays.asList(keys));
                                ArrayList<String> e = new ArrayList<>();
                                e.add("");
                                kList.removeAll(e);
                                keys = (String[]) kList.toArray(new String[kList.size()]);
                                HashMap<User, String> values = cateUserMap.get(key);
                                HashMap<String, Integer> counts = BreakdownTimeDemoUtility.convertToCount(values);
                                int iCount;
                                int nCount;
                                int mCount;
                                if (counts.isEmpty()) {
                                    iCount = 0;
                                    nCount = 0;
                                    mCount = 0;
                                } else {
                                    iCount = counts.get("Intense");
                                    nCount = counts.get("Normal");
                                    mCount = counts.get("Mild");
                                }
                                int total4 = iCount + nCount + mCount;
                                total3 += total4;
                                JsonObject o4 = new JsonObject();
                                o4.addProperty(orderArr[orderArr.length - 1], keys[keys.length - 1]);
                                float percentValue = 100 * total4 / (float)totalUser;
                                int roundOffValue = Math.round(percentValue);
                                o4.addProperty("count", total4);
                                o4.addProperty("percent", roundOffValue);

                                JsonObject intense = new JsonObject();
                                intense.addProperty("intense-count", iCount);
                                
                                percentValue = 100 * iCount / (float)totalUser;
                                roundOffValue = Math.round(percentValue);
                                
                                intense.addProperty("intense-percent", roundOffValue);
                                JsonObject normal = new JsonObject();
                                normal.addProperty("normal-count", nCount);
                                
                                percentValue = 100 * nCount / (float)totalUser;
                                roundOffValue = Math.round(percentValue);
                                
                                normal.addProperty("normal-percent", roundOffValue);
                                JsonObject mild = new JsonObject();
                                mild.addProperty("mild-count", mCount);
                                
                                percentValue = 100 * mCount / (float)totalUser;
                                roundOffValue = Math.round(percentValue);
                                
                                mild.addProperty("mild-percent", roundOffValue);
                                JsonArray innermost = new JsonArray();
                                innermost.add(intense);
                                innermost.add(normal);
                                innermost.add(mild);
                                o4.add("breakdown", innermost);
                                j4.add(o4);
                            }
                            total2 += total3;
                            if (keys != null && keys.length > 1 && orderArr.length > 1) {
                                JsonObject o3 = new JsonObject();
                                o3.addProperty(orderArr[orderArr.length - 2], keys[keys.length - 2]);
                                o3.addProperty("count", total3);
                                
                                float percentValue = 100 * total3 / (float)totalUser;
                                int roundOffValue = Math.round(percentValue);
                                
                                o3.addProperty("percent", roundOffValue);
                                o3.add("breakdown", j4);
                                j3.add(o3);
                            } else {
                                j3.addAll(j4);
                            }
                        }
                        total1 += total2;
                        if (keys != null && keys.length > 2 && orderArr.length > 2) {
                            JsonObject o2 = new JsonObject();
                            o2.addProperty(orderArr[orderArr.length - 3], keys[keys.length - 3]);
                            o2.addProperty("count", total2);
                            
                            float percentValue = 100 * total2 / (float)totalUser;
                            int roundOffValue = Math.round(percentValue);
                            
                            o2.addProperty("percent", roundOffValue);
                            o2.add("breakdown", j3);
                            j2.add(o2);
                        } else {
                            j2.addAll(j3);
                        }
                    }
                    if (keys != null && keys.length > 3 && orderArr.length > 3) {
                        JsonObject o1 = new JsonObject();
                        o1.addProperty(orderArr[orderArr.length - 4], keys[keys.length - 4]);
                        o1.addProperty("count", total1);
                        
                        float percentValue =  100 * total1 / (float)totalUser;
                        int roundOffValue = Math.round(percentValue);
                        
                        o1.addProperty("percent", roundOffValue);
                        o1.add("breakdown", j2);
                        j1.add(o1);
                    } else {
                        j1.addAll(j2);
                    }
                }

                jsonOutput.add("breakdown", j1);

            }

            //writes the output as a response (but not html)
            try {
                out.println(gson.toJson(jsonOutput));
                System.out.println(gson.toJson(jsonOutput));
            } finally {
                out.close();
                ConnectionManager.close(conn);
            }
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
            Logger.getLogger(basic_usetime_demographics_report.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(basic_usetime_demographics_report.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(basic_usetime_demographics_report.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(basic_usetime_demographics_report.class.getName()).log(Level.SEVERE, null, ex);
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
