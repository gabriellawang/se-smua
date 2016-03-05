/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.app.json;

import com.app.model.Admin;
import com.app.model.AdminDAO;
import com.app.model.ConnectionManager;
import com.app.model.User;
import com.app.model.UserDAO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import is203.JWTUtility;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
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
@WebServlet(name = "authenticate", urlPatterns = {"/json/authenticate"})
public class authenticate extends HttpServlet {

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
            /* TODO output your page here. You may use following sample code. */
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            conn = ConnectionManager.getConnection();
            //creats a new json object for printing the desired json output
            JsonObject jsonOutput = new JsonObject();
            //retrieves the user
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String errorMsg = "invalid username/password";
            JsonArray errorArray = new JsonArray();
            errorArray.add(new JsonPrimitive(errorMsg));

            User currentUser = UserDAO.retrieveUser(username, conn);
            Admin admin = AdminDAO.retrieveAdmin(username);
            if (admin != null) {
                if (!admin.getPassword().equals(password) || password==null) {
                    jsonOutput.addProperty("status", "error");
                    jsonOutput.add("messages", errorArray);
                    try {
                        out.println(gson.toJson(jsonOutput));
                    } finally {
                        out.close();
                        ConnectionManager.close(conn);
                    }
                } else {
                    jsonOutput.addProperty("status", "success");
                    String sharedSecret = "is203g4t6luvjava";
                    String token = JWTUtility.sign(sharedSecret, username);
                    jsonOutput.addProperty("token", token);
                    try {
                        out.println(gson.toJson(jsonOutput));
                    } finally {
                        out.close();
                        ConnectionManager.close(conn);
                    }
                }
                return;
            }

           
            if (username.equals("")) {
                // error output
                jsonOutput.addProperty("status", "error");
                errorArray.add(new JsonPrimitive("blank username"));
                jsonOutput.add("messages", errorArray);
            }
            if (password.equals("")) {
                // error output
                jsonOutput.addProperty("status", "error");
                errorArray.add(new JsonPrimitive("blank password"));
                jsonOutput.add("messages", errorArray);
            }
            if (currentUser == null) {
                // error output
                jsonOutput.addProperty("status", "error");
                jsonOutput.add("messages", errorArray);
            } else {
                if (currentUser.getPassword().equals(password)) {
                    // success output
                    jsonOutput.addProperty("status", "success");
                    String sharedSecret = "is203g4t6luvjava";
                    String token = JWTUtility.sign(sharedSecret, username);
                    jsonOutput.addProperty("token", token);
                } else {
                    // error output
                    jsonOutput.addProperty("status", "error");
                    jsonOutput.add("messages", errorArray);
                }
            }
            //writes the output as a response (but not html)
            try {
                out.println(gson.toJson(jsonOutput));
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
            Logger.getLogger(authenticate.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(authenticate.class.getName()).log(Level.SEVERE, null, ex);
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
