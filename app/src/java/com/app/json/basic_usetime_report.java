/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.app.json;

import com.app.controller.AppUsageUtility;
import com.app.model.ConnectionManager;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
public class basic_usetime_report extends HttpServlet {

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
        response.setContentType("application/JSON");
        Connection conn = null;
        try (PrintWriter out = response.getWriter()) {
            conn = ConnectionManager.getConnection();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            //creats a new json object for printing the desired json output
            JsonObject jsonOutput = new JsonObject();
            JsonArray errorJsonList = new JsonArray();
            //retrieves the user
            String startdate = request.getParameter("startdate");
            String enddate = request.getParameter("enddate");
            String token = request.getParameter("token");
            Date startDateFmt = null;
            Date endDateFmt = null;

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            df.setLenient(false);
            JsonObject temp = new JsonObject();

            if (startdate == null) {
                errorJsonList.add(new JsonPrimitive("missing startdate"));
            } else if (startdate.equals("")) {
                errorJsonList.add(new JsonPrimitive("blank startdate"));
            } else {
                try {
                    startdate += " 00:00:00";
                    startDateFmt = df.parse(startdate);
                } catch (ParseException ex) {
                    errorJsonList.add(new JsonPrimitive("invalid startdate"));
                }
            }
            if (enddate == null) {
                // error output
                errorJsonList.add(new JsonPrimitive("missing enddate"));
            } else if (enddate.equals("")) {
                errorJsonList.add(new JsonPrimitive("blank enddate"));
            } else {
                try {
                    enddate += " 23:59:59";
                    endDateFmt = df.parse(enddate);
                } catch (ParseException ex) {
                    errorJsonList.add(new JsonPrimitive("invalid enddate"));
                }
            }
            if (startDateFmt != null && endDateFmt != null && startDateFmt.after(endDateFmt)) {
                errorJsonList.add(new JsonPrimitive("invalid startdate"));
            }

            String sharedSecret = "is203g4t6luvjava";
            String username = "";
            try {
                username = JWTUtility.verify(token, sharedSecret);
            } catch (JWTException ex) {
                errorJsonList.add(new JsonPrimitive("invalid token"));
            }

            if (errorJsonList.size() > 0) {
                jsonOutput.addProperty("status", "error");
                jsonOutput.add("messages", errorJsonList);
            } else {
                JsonArray results = new JsonArray();
                JsonObject intense = new JsonObject();
                JsonObject normal = new JsonObject();
                JsonObject mild = new JsonObject();

//----------------------------------BREAKDWN TIME USAGE CALCULATION--------------------------------------//
                HashMap<String, Integer> hashresults = AppUsageUtility.breakdownByUsageTimeCategory(startdate, enddate, conn);
                Iterator<String> it = hashresults.keySet().iterator();
                int total = 0;
                int noOfIntenseUsers = 0;
                int noOfNormalUsers = 0;
                int noOfMildUsers = 0;
                while (it.hasNext()) {
                    String category = it.next();
                    int count = hashresults.get(category);
                    if (category.equals("intenseUser")) {
                        noOfIntenseUsers = count;
                        intense.add("intense-count", new JsonPrimitive(count));
                    }
                    if (category.equals("normalUser")) {
                        noOfNormalUsers = count;
                        normal.add("normal-count", new JsonPrimitive(count));
                    }
                    if (category.equals("mildUser")) {
                        noOfMildUsers = count;
                        mild.add("mild-count", new JsonPrimitive(count));
                    }
                    total += count;
                }

                intense.add("intense-percent", new JsonPrimitive(Math.round(noOfIntenseUsers / (double) total * 100)));
                normal.add("normal-percent", new JsonPrimitive(Math.round(noOfNormalUsers / (double) total * 100)));
                mild.add("mild-percent", new JsonPrimitive(Math.round(noOfMildUsers / (double) total * 100)));

                results.add(intense);
                results.add(normal);
                results.add(mild);
                jsonOutput.addProperty("status", "success");
                jsonOutput.add("breakdown", results);
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
            Logger.getLogger(basic_usetime_report.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(basic_usetime_report.class.getName()).log(Level.SEVERE, null, ex);
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
