<%-- 
    Document   : bootstrap
    Created on : Nov 2, 2015, 5:03:30 PM
    Author     : G4T6
--%>

<%@page import="java.util.Arrays"%>
<%@page import="com.app.model.ErrorMessage"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.HashMap"%>
<%@page import="com.app.model.Admin"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Welcome to SMUA</title>
        
        <link href="css/bootstrap.css" rel="stylesheet">
        <link href="css/bootstrap.min.css" rel="stylesheet">
        
        <script src="../js/jquery.min.js"></script>
        <script src="../js/bootstrap.min.js"></script>
    </head>
    <body>
        <%
            Admin currentAdmin = (Admin) session.getAttribute("currentAdmin");
            String type = request.getParameter("type");
            if(type == null){
                response.sendRedirect("admin_welcome.jsp");
                return;
            }
        %>
     <nav class="navbar navbar-default">
            <div class="container-fluid">
                <div class="navbar-header">
                    <a class="navbar-brand" href="admin_welcome.jsp">
                        <p>
                            <font color="#1e4f8a"><b>SMU </b></font>
                            <font color="#DAA520"><b>Analytics </b></font>
                            <font color="#B40404"><b>[</b></font>
                            <font color="#1e4f8a"><b>Administration Page</b></font>
                            <font color="#B40404"><b>]</b></font>
                        </p>
                    </a>
                </div>
                <p class="navbar-text" style="margin-right:10px">
                    <b><font colour="black">Welcome, <%= currentAdmin.getUsername() %></font></b>
                </p>
                <button type="button" class="navbar-btn navbar-right btn btn-primary" style="margin-right:10px"><a href="logout.jsp"><font color="white">Logout</font></a></button>
            </div>
        </nav>
        
        <div class="panel panel-primary">
        <% if(type.equals("bootstrap")){ %>
            <div class="panel-heading" style="font-weight: bold;">Bootstrap</div>
        <% } %>
        <% if(type.equals("add-addition")){ %>
            <div class="panel-heading" style="font-weight: bold;">Bootstrap: Add Additional Data</div>
        <% } %>
        <% if(type.equals("delete")){ %>
            <div class="panel-heading" style="font-weight: bold;">Bootstrap: Deletion of Data</div>
        <% } %>
            <div class="panel-body">
                <form method="post" enctype="multipart/form-data" action="bootstrap">
                    <input type="hidden" name="type" value="<%=type%>"/>
                    Upload a file:<br/>
                    <input type="file" accept="application/zip" name="zipfile" /><br/>
                    <input type="submit" value='Process Data' style="background-color:#1e4f8a; color:#FFFFFF; font-weight: bold; font-size:14px; border: 1px solid #0d2c52;" />
                </form>
            </div>
        </div>
        <%
            HashMap<Boolean, Integer> deleteResult = (HashMap<Boolean, Integer>) session.getAttribute("delete");
            if (deleteResult != null && deleteResult.size() > 0) {
                %>
                    <div class="panel panel-success">
                    <div class="panel-heading" style="font-weight: bold;">Number of rows deleted</div>
                    <div class="panel-body">
                <%
                int numNotFound = deleteResult.get(false);
                int numDeleted = deleteResult.get(true);
                out.println("<p>" + numDeleted + " records are valid and actually deleted.<br>");
                out.println(numNotFound + " records are valid but not found in the database.<br></p>");
                session.removeAttribute("delete");
                %>
                    </div>
                    </div>
                <%
            }
            
            HashMap<String, Integer> numLoaded = (HashMap<String, Integer>) session.getAttribute("numLoaded");
            if(numLoaded != null && !numLoaded.isEmpty()){
                %>
                    <div class="panel panel-success">
                    <div class="panel-heading" style="font-weight: bold;">Number of rows loaded</div>
                    <div class="panel-body">
                <%
                //out.println("<p><b>Number of rows loaded:</b><ul>");
                if(numLoaded.get("demographics.csv") != null){
                    out.println("<li>demographics.csv: " + numLoaded.get("demographics.csv") + "</li>");
                }
                if(numLoaded.get("app-lookup.csv") != null){
                    out.println("<li>app-lookup.csv: " + numLoaded.get("app-lookup.csv") + "</li>");
                }
                if(numLoaded.get("app.csv") != null){
                    out.println("<li>app.csv: " + numLoaded.get("app.csv") + "</li>");
                }
                if(numLoaded.get("location-lookup.csv") != null){
                    out.println("<li>location-lookup.csv: " + numLoaded.get("location-lookup.csv") + "</li>");
                }
                if(numLoaded.get("location.csv") != null){
                    out.println("<li>location.csv: " + numLoaded.get("location.csv") + "</li>");
                }
                out.println("</ul></p>");
                %>
                    </div>
                    </div>
                <%
            }
            session.removeAttribute("numLoaded");
        %>
        
        <%
            ArrayList<ErrorMessage> errorMsgs = (ArrayList<ErrorMessage>) session.getAttribute("errorMsgs");
            if (errorMsgs != null) {
                if (!errorMsgs.isEmpty()){
        %>
        <div class="panel panel-danger">
        <div class="panel-heading" style="font-weight: bold;">The errors detected during data uploading</div>
        <div class="panel-body">       
        <table border="1">
            <tr>
                <th>FileName</th>
                <th>Row Number</th>
                <th>Message</th>
            </tr>
            <%
                for (int i = 0; i < errorMsgs.size(); i++) {
                    ErrorMessage msg = errorMsgs.get(i);
                    String name = msg.getFileName();
                    int num = msg.getRowNo();
                    String[] messages = msg.getMessage();
                    String str = Arrays.toString(messages);
                    String message = str.substring(1, str.length() - 1);
            %>
            <tr>
                <td><%=name%></td>
                <td><%=num%></td>
                <td><%=message%></td>
            </tr>
            <%
                }
            %>
        </table>
        </div>
        </div>
        <%
                }
                session.removeAttribute("errorMsgs");
            }
        %>
    </body>
</html>
