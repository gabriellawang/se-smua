<%-- 
    Document   : breakdownAppCategory
    Created on : Oct 6, 2015, 4:18:54 PM
    Author     : Jason
--%>
<%@page import="java.util.TreeMap"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.ArrayList"%>
<%@include file="protect.jsp" %>
<%@page import="java.text.DecimalFormat"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@include file="protect.jsp" %>
<%@page import="com.app.model.User"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Basic App Usage Report</title>
        
        <link href="css/bootstrap.css" rel="stylesheet">
        <link href="css/bootstrap.min.css" rel="stylesheet">
        
        <script src="../js/jquery.min.js"></script>
        <script src="../js/bootstrap.min.js"></script>
        
    </head>
    <body>
        <%               
            User currentUser = (User) session.getAttribute("currentUser");
        %>
        <nav class="navbar navbar-default">
            <div class="container-fluid">
                <div class="navbar-header">
                    <a class="navbar-brand" href="welcome.jsp">
                        <p>
                            <font color="#1e4f8a"><b>SMU </b></font>
                            <font color="#DAA520"><b>Analytics</b></font>
                        </p>
                    </a>
                </div>
                <p class="navbar-text" style="margin-right:10px">
                    <font colour="black">Welcome, <%= currentUser.getName()%></font>
                </p>
                <button type="button" class="navbar-btn navbar-right btn btn-primary" style="margin-right:10px"><a href="logout.jsp"><font color="white">Logout</font></a></button>
            </div>
        </nav>

        <%
            String startDate = request.getParameter("startDate");
            String endDate = request.getParameter("endDate");

            if (startDate == null) {
                startDate = "";
            }
            if (endDate == null) {
                endDate = "";
            }

        %>

        <link rel="stylesheet" href="//code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">
        <script src="//code.jquery.com/jquery-1.10.2.js"></script>
        <script src="//code.jquery.com/ui/1.11.4/jquery-ui.js"></script>
        <link rel="stylesheet" href="/resources/demos/style.css">
 
        <script>
            $(function () {
                $("#datepicker").datepicker({dateFormat: "yy-mm-dd 00:00:00"});
            });
        </script>
        <script>
            $(function ()
            {
                $("#datepicker2").datepicker({dateFormat: "yy-mm-dd 23:59:59"});
            });
        </script>
        
        <div class="panel panel-primary">
        <div class="panel-heading" style="font-weight: bold;">Basic Usage App Report: Breakdown by App Category</div>
        <div class="panel-body">
            <form action="BreakDownAppCategory">
                <p>Select Start Date: 
                    <input type="date" name="startDate" value="<%=startDate%>" /></p>
                    <%-- <input type="text" name="startDate" id="datepicker" readonly value="<%=startDate%>" /></p> --%>
                <p>Select End Date:&nbsp; 
                    <input type="date" name="endDate" value="<%=endDate%>" /></p> 
                     <%-- <input type="text" name="endDate" id="datepicker2" readonly value="<%=endDate%>" /></p> --%>
                <input type="submit" value="Generate Report" style="background-color:#1e4f8a; color:#FFFFFF; font-weight: bold; font-size:14px; border: 1px solid #0d2c52;" />
            </form>
        </div>
        </div>
        <br><br>
        
        <style type="text/css">
            tr.d0 th {
                background-color: green;
                color: whitesmoke;
            }
            
            tr.d1 th {
                background-color: #985f0d;
                color: whitesmoke;
            }
        </style>
        <%
            String error = (String) request.getAttribute("error");
            if (error != null && error.length() > 0) {
                out.println("<div class='alert alert-danger' role='alert'><center><strong>Error! </strong>" + error + "</center></div>");
            }
        %>
     
        <%
            TreeMap<String, Integer> appMap = (TreeMap)session.getAttribute("appMap");
            String totalSecsTp = (String)session.getAttribute("totalSecs");
            String daysTp = (String)session.getAttribute("days");
            
            if(appMap == null){
                return;
            }
            
            double totalSecs = 0.0;
            if(totalSecsTp != null)
            {
                totalSecs = Double.parseDouble(totalSecsTp);
            }
            
            int days = 0;
            if(daysTp != null)
            {
                days = Integer.parseInt(daysTp);
            }
                
            if (appMap != null) {
                %>
                    <div class="panel panel-success">
                    <div class="panel-heading" style="font-weight: bold;">Results</div>
                    <div class="panel-body">
                    Start date: <%= session.getAttribute("startDate") %><br>
                    End date: <%= session.getAttribute("endDate") %><br><br>
                <%
                out.println("<table border=1>");
                out.println("<tr class='d0'>");
                out.println("<th>");
                out.println("S/No");
                out.println("</th>");
                out.println("<th>");
                out.println("App Category");
                out.println("</th>");
                out.println("<th>");
                out.println("Daily Usage Duration(In sec)");
                out.println("</th>");
                out.println("<th>");
                out.println("Percentage(%)");
                out.println("</th>");
                out.println("</tr>");
                
                int count = 1;
                Iterator it = appMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry mapEntry = (Map.Entry)it.next();
                    int value = (Integer)mapEntry.getValue();
                    double valuePer = (value/totalSecs)*100;
                    
                    out.println("<tr>");
                    out.println("<td>");
                    out.println(count);
                    out.println("</td>");
                    out.println("<td>");
                    out.println(mapEntry.getKey());
                    out.println("</td>");
                    out.println("<td><center>");
                    out.println(Math.round((value+0.0)/(days+0.0)));
                    out.println("</center></td>");
                    out.println("<td><center>");
                    out.println(Math.round(valuePer));
                    out.println("</center></td>");
                    out.println("</tr>");
                    
                    count++;
                }
                //out.println("</tr>");
                out.println("</table>");
                %>
                    </div>
                    </div>
                <%
                session.removeAttribute("appMap");
                session.removeAttribute("totalSecs");
                session.removeAttribute("days");
                session.removeAttribute("startDate");
                session.removeAttribute("endDate");
            }
        %>
    </body>
</html>
