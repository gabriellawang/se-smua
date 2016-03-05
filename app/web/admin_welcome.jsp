<%-- 
    Document   : admin_welcome
    Created on : Sep 30, 2015, 5:39:15 PM
    Author     : G4T6
--%>
<%@page import="java.util.HashMap"%>
<%@include file="admin_protect.jsp" %>
<%@page import="java.util.Arrays"%>
<%@page import="com.app.model.ErrorMessage"%>
<%@page import="java.util.ArrayList"%>
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
        <div class="panel-heading" style="font-weight: bold;">SMU Analytics Data Management</div>
        <div class="panel-body">
            <ul class="nav nav-pills nav-justified">
                <li role="display" ><a href="bootstrap.jsp?type=bootstrap">Bootstrap</a></li>
                <li role="display"><a href="bootstrap.jsp?type=add-addition">Add Additional Data</a></li>
                <li role="display"><a href="bootstrap.jsp?type=delete">Delete Location Data(By uploading)</a></li>
                <li role="display"><a href="location-delete.jsp">Delete Location Data(By Web UI)</a></li>
            </ul>
        </div>
        </div>   
    </body>
</html>
