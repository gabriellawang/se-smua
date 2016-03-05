<%-- 
    Document   : welcome
    Created on : Sep 30, 2015, 5:12:42 PM
    Author     : G4T6
--%>
<%@include file="protect.jsp" %>
<%@page import="com.app.model.User"%>
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
        <div class="panel panel-primary">
        <div class="panel-heading" style="font-weight: bold;">SMU Analytics Reports</div>
        <div class="panel-body">
            <ul class="nav nav-pills nav-justified">
                <li><center><select onChange="window.location.href=this.value" style="background-color:white; color:#337ab7; font-size:14px; border: 0px solid #0d2c52;">
                        <option value="">------------- Basic App Usage Report ------------</option>
                        <option value="breakdownUsageTime.jsp">Breakdown by Time Usage</option>
                        <option value="breakdownDemographics.jsp">Breakdown by Time Usage & Demographics</option>
                        <option value="breakdownAppCategory.jsp">Breakdown by App Category</option>
                        <option value="diurnalPatternAppUsage.jsp">Breakdown by Diurnal Pattern</option>
                </select></center></li>
                <%-- <li role="display" ><a href="basic-usage.jsp">Basic App Usage Report</a></li> --%>
                <li role="display"><a href="top-k.jsp">Top-k App Usage Report</a></li>
                <li role="display"><a href="smartphone-overuse.jsp">Smartphone Overuse Report</a></li>
                <li role="display"><a href="smartphone-heatmap.jsp">Smartphone Usage Heatmap</a></li>
                <li role="display"><a href="social-activeness-report.jsp">Social Activeness Report</a></li>

            </ul>
        </div>
        </div>
    </body>   
    </div>
</html>
