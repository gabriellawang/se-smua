package com.app.json;

import com.app.model.AppUsageValidation;
import com.app.model.ConnectionManager;
import com.app.model.ErrorMessage;
import com.app.model.Pair;
import com.app.model.UserValidation;
import com.app.model.ZipFileReader;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 *
 * @author G4-T6
 */
@MultipartConfig
@WebServlet(name = "update", urlPatterns = {"/json/update"})
public class update extends HttpServlet {

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
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);
            ArrayList<ErrorMessage> errorMsgs = new ArrayList<>();
            Part filepart = request.getPart("bootstrap-file");
            String token = request.getParameter("token");
            JsonObject jsonOutput = new JsonObject();
            JsonArray errorList = new JsonArray();
            String sharedSecret = "is203g4t6luvjava";
            String username = "";
            try {
                username = JWTUtility.verify(token, sharedSecret);
            } catch (JWTException ex) {
                errorList.add(new JsonPrimitive("invalid token"));
            }
            if (errorList.size() > 0) {
                jsonOutput.addProperty("status", "error");
                jsonOutput.add("message", errorList);
            } else {
                if (filepart != null) {
                    ZipFileReader zfr = new ZipFileReader();
                    HashMap<String, List<String[]>> content = null;
                    Pair<HashMap<String, List<String[]>>, List<List<String[]>>> pair;
                    pair = zfr.readFile(filepart);
                    content = pair.getFirst();
                    //validation
                    if (content != null) {
                        int appSize = -1;
                        int demoSize = -1;
                        List<String[]> app = content.get("app.csv");
                        if (app != null) {
                            AppUsageValidation auv = new AppUsageValidation(app, conn);
                            errorMsgs.addAll(auv.addAdditionalData());
                            appSize = auv.getAdditionalSize();
                        }
                        List<String[]> demographics = content.get("demographics.csv");
                        if (demographics != null) {
                            UserValidation uv = new UserValidation(demographics, conn);
                            errorMsgs.addAll(uv.addAdditionalData());
                            demoSize = uv.getSize();
                        }
                        JsonArray numArray = new JsonArray();
                        JsonObject appObj = new JsonObject();
                        if(appSize >= 0){
                            appObj.addProperty("app.csv", appSize);
                            numArray.add(appObj);
                        }
                        JsonObject demoObj = new JsonObject();
                        if(demoSize >= 0){
                            demoObj.addProperty("demographics.csv", demoSize);
                            numArray.add(demoObj);
                        }
                        if (errorMsgs.size() > 0) {
                            for (ErrorMessage msg : errorMsgs) {
                                String file = msg.getFileName();
                                int line = msg.getRowNo();
                                String[] err = msg.getMessage();
                                JsonArray error = new JsonArray();
                                for (String e : err) {
                                    error.add(new JsonPrimitive(e));
                                }
                                JsonObject obj = new JsonObject();
                                obj.addProperty("file", file);
                                obj.addProperty("line", line);
                                obj.add("message", error);
                                errorList.add(obj);
                            }
                        }
                        if (errorList.size() > 0) {
                            jsonOutput.addProperty("status", "error");
                            jsonOutput.add("num-record-loaded", numArray);
                            jsonOutput.add("error", errorList);
                        } else {
                            jsonOutput.addProperty("status", "success");
                            jsonOutput.add("num-record-loaded", numArray);
                        }
                    }
                }
                try {
                    out.println(gson.toJson(jsonOutput));
                } finally {
                    out.close();
                    ConnectionManager.close(conn);
                }
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
            Logger.getLogger(update.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(update.class.getName()).log(Level.SEVERE, null, ex);
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
