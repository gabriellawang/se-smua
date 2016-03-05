<%-- 
    Document   : basicAppReport
    Created on : Sep 29, 2015, 12:58:44 AM
    Author     : G4T6
--%>
<%@include file="protect.jsp" %>
<%@page import="java.util.*"%>
<%@page import="com.app.model.User"%>
<%@include file="protect.jsp"%>
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
            User currentUser = (User)session.getAttribute("currentUser");
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
            <div class="panel-heading" style="font-weight: bold;">Basic App Usage Report</div>
            <div class="panel-body">
                <ul class="nav nav-pills nav-justified">
                    <li role="display"><a href="breakdownUsageTime.jsp">Breakdown by usage time category</a></li> 
                    <li role="display"><a href="breakdownDemographics.jsp">Breakdown by usage time and demographics category</a></li>  
                    <li role="display"><a href="breakdownAppCategory.jsp">Breakdown app category</a></li>  
                    <li role="display"><a href="diurnalPatternAppUsage.jsp">Diurnal pattern of app usage time</a></li>  
                </ul>
            </div>
        </div>
    </body>
</html>
