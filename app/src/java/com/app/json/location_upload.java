package com.app.json;

import com.app.model.AdminDAO;
import com.app.model.AppUsageValidation;
import com.app.model.AppValidation;
import com.app.model.ConnectionManager;
import com.app.model.ErrorMessage;
import com.app.model.LocationRecordValidation;
import com.app.model.LocationValidation;
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
@WebServlet(name = "location_upload", urlPatterns = {"/json/location-upload"})
public class location_upload extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws java.sql.SQLException
     * @throws is203.JWTException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException, JWTException {
        response.setContentType("application/JSON");
        Connection conn = null;
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);
            ArrayList<ErrorMessage> errorMsgs = new ArrayList<>();
            HashMap<Boolean, Integer> deleteResult = new HashMap<>();

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
            if (AdminDAO.retrieveAdmin(username) == null) {
                errorList.add(new JsonPrimitive("invalid token"));
            }
            if (errorList.size() > 0) {
                jsonOutput.addProperty("status", "error");
                jsonOutput.add("message", errorList);
            } else {
                if (filepart != null) {
                    ZipFileReader zfr = new ZipFileReader();
                    HashMap<String, List<String[]>> content = null;
                    List<List<String[]>> deletion = null;
                    Pair<HashMap<String, List<String[]>>, List<List<String[]>>> pair;
                    pair = zfr.readFile(filepart);
                    content = pair.getFirst();
                    deletion = pair.getSecond();
                    //validation
                    if (content != null) {
                        int locationLookupSize = 0;
                        int locationSize = 0;
                        int appLookupSize = 0;
                        int appSize = 0;
                        int demoSize = 0;
                        List<String[]> demographics = content.get("demographics.csv");
                        if (demographics != null) {
                            UserValidation uv = new UserValidation(demographics, conn);
                            errorMsgs.addAll(uv.bootstrap());
                            demoSize = uv.getSize();
                        }
                        List<String[]> appLookup = content.get("app-lookup.csv");
                        if (appLookup != null) {
                            AppValidation av = new AppValidation(appLookup, conn);
                            errorMsgs.addAll(av.bootstrap());
                            appLookupSize = av.getSize();
                        }
                        List<String[]> app = content.get("app.csv");
                        if (app != null) {
                            AppUsageValidation auv = new AppUsageValidation(app, conn);
                            errorMsgs.addAll(auv.bootstrap());
                            appSize = auv.getSize();
                        }
                        List<String[]> locationLookup = content.get("location-lookup.csv");
                        if (locationLookup != null) {
                            LocationValidation lv = new LocationValidation(locationLookup, conn);
                            errorMsgs.addAll(lv.bootstrap());
                            locationLookupSize = lv.getSize();
                        }
                        List<String[]> location = content.get("location.csv");
                        if (location != null) {
                            LocationRecordValidation lrv = new LocationRecordValidation(location, conn);
                            errorMsgs.addAll(lrv.bootstrap());
                            locationSize = lrv.getSize();
                        }

                        if (deletion != null && !deletion.isEmpty()) {
                            LocationRecordValidation lrv = new LocationRecordValidation(deletion.get(0), conn);
                            deleteResult = lrv.deleteLocationData();
                        }

                        JsonArray numArray = new JsonArray();
                        JsonObject applookup = new JsonObject();
                        applookup.addProperty("app-lookup.csv", appLookupSize);
                        JsonObject appObj = new JsonObject();
                        appObj.addProperty("app.csv", appSize);
                        JsonObject demo = new JsonObject();
                        demo.addProperty("demographics.csv", demoSize);
                        JsonObject locationLookupObj = new JsonObject();
                        locationLookupObj.addProperty("location-lookup.csv", locationLookupSize);
                        JsonObject locationObj = new JsonObject();
                        locationObj.addProperty("location.csv", locationSize);
                        numArray.add(applookup);
                        numArray.add(appObj);
                        numArray.add(demo);
                        numArray.add(locationLookupObj);
                        numArray.add(locationObj);

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
                        jsonOutput.addProperty("num-record-deleted", deleteResult.get(true));
                        jsonOutput.addProperty("num-record-not-found", deleteResult.get(false));
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
            Logger.getLogger(location_upload.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JWTException ex) {
            Logger.getLogger(location_upload.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(location_upload.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JWTException ex) {
            Logger.getLogger(location_upload.class.getName()).log(Level.SEVERE, null, ex);
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
