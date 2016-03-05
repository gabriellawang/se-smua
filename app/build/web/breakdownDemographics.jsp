<%-- 
    Document   : basic_usage
    Created on : Oct 7, 2015, 12:26:59 PM
    Author     : G4T6
--%>
<%@include file="protect.jsp" %>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Map"%>
<%@page import="com.app.controller.AppUsageUtility"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.HashMap"%>
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
    <%
        User currentUser = (User) session.getAttribute("currentUser");
        
        String choice1 = request.getParameter("first");
        String choice2 = request.getParameter("second");
        String choice3 = request.getParameter("third");
        String choice4 = request.getParameter("forth"); 
        if(choice1==null){
            choice1="--Select a category--";   
        }
        if(choice2==null){
            choice2="--Select a category--";   
        }
        if(choice3==null){
            choice3="--Select a category--";  
        }
        if(choice4==null){
            choice4="--Select a category--"; 
        }
            
        String startDate = request.getParameter("start-date");
        String endDate = request.getParameter("end-date");
        if(startDate==null){
            startDate="";  
        }
        if(endDate==null){
            endDate=""; 
        }
    %>
    
    <body>
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
        
        <b></b>
        <div class="panel panel-primary">
        <div class="panel-heading" style="font-weight: bold;">Basic App Usage Report: Breakdown by Time Usage & Demographics Category</div>
        <div class="panel-body">
            <form action='BreakdownTimeDemoReport'>
                Start Date: 
                <input type="date" name="start-date" value="<%= startDate %>" /><br>
                End Date: 
                &nbsp;<input type="date" name="end-date" value="<%= endDate %>" /><br><br>
                
                <p><strong><font color="red">*</font>Category (Select at least one category. Choices must be different)</strong></p>
                <p>First choice:  
                <select name="first">
                    <% if(choice1.equals("--Select a category--")){ %>
                    <option selected="selected" >--Select a category--</option>
                    <option value="Year">Year</option>
                    <option value="Gender">Gender</option>
                    <option value="School">School</option>
                    <option value="CCA">CCA</option>
                    <% } %>
                    
                    <% if(choice1.equals("Year")){ %>
                    <option>--Select a category--</option>
                    <option value="Year" selected="selected">Year</option>
                    <option value="Gender">Gender</option>
                    <option value="School">School</option>
                    <option value="CCA">CCA</option>
                    <% } %>
                    
                    <% if(choice1.equals("Gender")){ %>
                    <option>--Select a category--</option>
                    <option value="Year">Year</option>
                    <option value="Gender" selected="selected">Gender</option>
                    <option value="School">School</option>
                    <option value="CCA">CCA</option>
                    <% } %>
                    
                    <% if(choice1.equals("School")){ %>
                    <option>--Select a category--</option>
                    <option value="Year">Year</option>
                    <option value="Gender">Gender</option>
                    <option value="School" selected="selected">School</option>
                    <option value="CCA">CCA</option>
                    <% } %>
                    
                    <% if(choice1.equals("CCA")){ %>
                    <option>--Select a category--</option>
                    <option value="Year">Year</option>
                    <option value="Gender">Gender</option>
                    <option value="School">School</option>
                    <option value="CCA" selected="selected">CCA</option>
                    <% } %>
                </select>&nbsp;
                
                Second choice:  
                <select name="second">
                    <% if(choice2.equals("--Select a category--")){ %>
                    <option selected="selected" >--Select a category--</option>
                    <option value="Year">Year</option>
                    <option value="Gender">Gender</option>
                    <option value="School">School</option>
                    <option value="CCA">CCA</option>
                    <% } %>
                    
                    <% if(choice2.equals("Year")){ %>
                    <option>--Select a category--</option>
                    <option value="Year" selected="selected">Year</option>
                    <option value="Gender">Gender</option>
                    <option value="School">School</option>
                    <option value="CCA">CCA</option>
                    <% } %>
                    
                    <% if(choice2.equals("Gender")){ %>
                    <option>--Select a category--</option>
                    <option value="Year">Year</option>
                    <option value="Gender" selected="selected">Gender</option>
                    <option value="School">School</option>
                    <option value="CCA">CCA</option>
                    <% } %>
                    
                    <% if(choice2.equals("School")){ %>
                    <option>--Select a category--</option>
                    <option value="Year">Year</option>
                    <option value="Gender">Gender</option>
                    <option value="School" selected="selected">School</option>
                    <option value="CCA">CCA</option>
                    <% } %>
                    
                    <% if(choice2.equals("CCA")){ %>
                    <option>--Select a category--</option>
                    <option value="Year">Year</option>
                    <option value="Gender">Gender</option>
                    <option value="School">School</option>
                    <option value="CCA" selected="selected">CCA</option>
                    <% } %>
                </select>&nbsp;
                
                Third choice:  
                <select name="third">
                    <% if(choice3.equals("--Select a category--")){ %>
                    <option selected="selected" >--Select a category--</option>
                    <option value="Year">Year</option>
                    <option value="Gender">Gender</option>
                    <option value="School">School</option>
                    <option value="CCA">CCA</option>
                    <% } %>
                    
                    <% if(choice3.equals("Year")){ %>
                    <option>--Select a category--</option>
                    <option value="Year" selected="selected">Year</option>
                    <option value="Gender">Gender</option>
                    <option value="School">School</option>
                    <option value="CCA">CCA</option>
                    <% } %>
                    
                    <% if(choice3.equals("Gender")){ %>
                    <option>--Select a category--</option>
                    <option value="Year">Year</option>
                    <option value="Gender" selected="selected">Gender</option>
                    <option value="School">School</option>
                    <option value="CCA">CCA</option>
                    <% } %>
                    
                    <% if(choice3.equals("School")){ %>
                    <option>--Select a category--</option>
                    <option value="Year">Year</option>
                    <option value="Gender">Gender</option>
                    <option value="School" selected="selected">School</option>
                    <option value="CCA">CCA</option>
                    <% } %>
                    
                    <% if(choice3.equals("CCA")){ %>
                    <option>--Select a category--</option>
                    <option value="Year">Year</option>
                    <option value="Gender">Gender</option>
                    <option value="School">School</option>
                    <option value="CCA" selected="selected">CCA</option>
                    <% } %>
                </select>&nbsp;
                
                Forth choice: 
                <select name="forth">
                    <% if(choice4.equals("--Select a category--")){ %>
                    <option selected="selected" >--Select a category--</option>
                    <option value="Year">Year</option>
                    <option value="Gender">Gender</option>
                    <option value="School">School</option>
                    <option value="CCA">CCA</option>
                    <% } %>
                    
                    <% if(choice4.equals("Year")){ %>
                    <option>--Select a category--</option>
                    <option value="Year" selected="selected">Year</option>
                    <option value="Gender">Gender</option>
                    <option value="School">School</option>
                    <option value="CCA">CCA</option>
                    <% } %>
                    
                    <% if(choice4.equals("Gender")){ %>
                    <option>--Select a category--</option>
                    <option value="Year">Year</option>
                    <option value="Gender" selected="selected">Gender</option>
                    <option value="School">School</option>
                    <option value="CCA">CCA</option>
                    <% } %>
                    
                    <% if(choice4.equals("School")){ %>
                    <option>--Select a category--</option>
                    <option value="Year">Year</option>
                    <option value="Gender">Gender</option>
                    <option value="School" selected="selected">School</option>
                    <option value="CCA">CCA</option>
                    <% } %>
                    
                    <% if(choice4.equals("CCA")){ %>
                    <option>--Select a category--</option>
                    <option value="Year">Year</option>
                    <option value="Gender">Gender</option>
                    <option value="School">School</option>
                    <option value="CCA" selected="selected">CCA</option>
                    <% } %>
                </select><br><br>
                <input type='submit' value='Generate Report' style="background-color:#1e4f8a; color:#FFFFFF; font-weight: bold; font-size:14px; border: 1px solid #0d2c52;" /><br>
            </form>
        </div>
        </div>
        <%
            //String errorMsg = (String) request.getAttribute("time-demo-error");
            ArrayList<String> errors = (ArrayList<String>)request.getAttribute("errors");
            if (errors != null && errors.size() > 0) {
                out.println("<div class='alert alert-danger' role='alert'>");
                for(int i=0; i<errors.size(); i++)
                {
                    String errorMsg = errors.get(i);
                    out.println("<center><strong>Error! </strong>" + errorMsg + "</center>");
                }
                out.println("</div>");
            }
        %>
        
        <%
            ArrayList<String> results = (ArrayList<String>) session.getAttribute("results");
            if (results != null) {
                %>
                    <div class="panel panel-success">
                    <div class="panel-heading" style="font-weight: bold;">Results</div>
                    <div class="panel-body">
                <%
                for (String str : results) {
                    out.println(str);
                }
                %>
                    </div>
                    </div>
                <%
            }
            session.removeAttribute("results");
        %>
    </body>
</html>
