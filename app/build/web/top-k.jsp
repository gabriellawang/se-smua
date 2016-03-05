<%-- 
    Document   : top-k
    Created on : Oct 3, 2015, 6:04:55 PM
    Author     : G4T6
--%>
<%@page import="com.app.model.User"%>
<%@include file="protect.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Top-k App Usage Report</title>
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
        <div class="panel-heading" style="font-weight: bold;">Top-k Most Used Apps</div>
        <div class="panel-body">        
        <form action="TopKAppReport">
            Choose the value of K:
            <input type="number" value='3' name="k" min="1" max="10"><br>
            Choose the school:<br>
            <input type="hidden" name="type" value="mostUsedApps"/>
            <input type="radio" name="school" value="sis" checked/>School of Information System  
            <input type="radio" name="school" value="economics" />School of Economics  
            <input type="radio" name="school" value="business" />Lee Kong Chian School of Business<br>
            <input type="radio" name="school" value="accountancy" />School of Accountancy  
            <input type="radio" name="school" value="law" />School of Law  
            <input type="radio" name="school" value="socsc" />School of Social Science<br>
            Start Date:<br>
            <input type="date" name="start-date" /><br>
            End Date:<br>
            <input type="date" name="end-date" /><br><br>
           <input type="submit" value="Generate Report" style="background-color:#1e4f8a; color:#FFFFFF; font-weight: bold; font-size:14px; border: 1px solid #0d2c52;" />
        </form>
            </div>
        </div>
        
        <div class="panel panel-primary">
        <div class="panel-heading" style="font-weight: bold;">Top-k Students With Most App Usage</div>
        <div class="panel-body">        
        <form action="TopKStudentReport">
            Choose the value of K:
            <input type="number" value='3' name="k" min="1" max="10"><br>
            Choose the category:<br>
            <input type="hidden" name="type" value="studentsWithMostUsage"/>
            <input type="radio" name="category" value="Books" checked/>Books  
            <input type="radio" name="category" value="Social" />Social  
            <input type="radio" name="category" value="Education" />Education  
            <input type="radio" name="category" value="Entertainment" />Entertainment  
            <input type="radio" name="category" value="Information" />Information  
            <input type="radio" name="category" value="Library" />Library<br>
            <input type="radio" name="category" value="Local" />Local  
            <input type="radio" name="category" value="Tools" />Tools  
            <input type="radio" name="category" value="Fitness" />Fitness  
            <input type="radio" name="category" value="Games" />Games  
            <input type="radio" name="category" value="Others" />Others<br>
            Start Date:<br>
            <input type="date" name="start-date" /><br>
            End Date:<br>
            <input type="date" name="end-date" /><br><br>
            <input type="submit" value="Generate Report" style="background-color:#1e4f8a; color:#FFFFFF; font-weight: bold; font-size:14px; border: 1px solid #0d2c52;" />
        </form>
        </div>
        </div>
        
        <div class="panel panel-primary">
        <div class="panel-heading" style="font-weight: bold;">Top-k Schools With Most App Usage</div>
        <div class="panel-body"> 
        <form action="TopKSchoolReport">
            Choose the value of K:
            <input type="number" value='3' name="k" min="1" max="6"><br>
            Choose the category:<br>
            <input type="hidden" name="type" value="schoolsWithMostUsage"/>
            <input type="radio" name="category" value="Books" checked/>Books  
            <input type="radio" name="category" value="Social" />Social  
            <input type="radio" name="category" value="Education" />Education  
            <input type="radio" name="category" value="Entertainment" />Entertainment  
            <input type="radio" name="category" value="Information" />Information  
            <input type="radio" name="category" value="Library" />Library<br>
            <input type="radio" name="category" value="Local" />Local  
            <input type="radio" name="category" value="Tools" />Tools  
            <input type="radio" name="category" value="Fitness" />Fitness  
            <input type="radio" name="category" value="Games" />Games  
            <input type="radio" name="category" value="Others" />Others<br>
            Start Date:<br>
            <input type="date" name="start-date" /><br>
            End Date:<br>
            <input type="date" name="end-date" /><br><br>
            <input type="submit" value="Generate Report" style="background-color:#1e4f8a; color:#FFFFFF; font-weight: bold; font-size:14px; border: 1px solid #0d2c52;" />
        </form>
        </div>
        </div>
    </body>
</html>
