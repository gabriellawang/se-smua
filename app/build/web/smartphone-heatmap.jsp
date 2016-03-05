<%-- 
    Document   : smartphone-heatmap
    Created on : Oct 27, 2015, 3:18:24 PM
    Author     : G4T6
--%>

<%@page import="com.app.model.HashMapSorting"%>
<%@page import="com.app.model.User"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.HashMap"%>
<%@include file="protect.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Smartphone Usage Heatmap</title>
        <link href="css/bootstrap.css" rel="stylesheet">
        <link href="css/bootstrap.min.css" rel="stylesheet">
        
        <script src="../js/jquery.min.js"></script>
        <script src="../js/bootstrap.min.js"></script>
    </head>
    <body>
        <%
            User currentUser = (User) session.getAttribute("currentUser");
            String level = request.getParameter("level");
            String cDate = request.getParameter("currentdate");
            String cTime = request.getParameter("currenttime");
            if(level == null)
            {
                level="";
            }
            if(cDate == null)
            {
                cDate = "";
            }
            if(cTime == null)
            {
                cTime = "";
            }
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
                    <font color="black">Welcome, <%= currentUser.getName()%></font>
                </p>
                <button type="button" class="navbar-btn navbar-right btn btn-primary" style="margin-right:10px"><a href="logout.jsp"><font color="white">Logout</font></a></button>
            </div>
        </nav>
        
        <div class="panel panel-primary">
        <div class="panel-heading" style="font-weight: bold;">Smartphone Usage Heatmap</div>
        <div class="panel-body">
        <form action="SmartphoneUsageHeatReport">
            Select current level: 
            <select name = "level">
                <% if(level.equals("")){ %>
                    <option value="" selected="selected">--Select level--</option>
                    <option value="B1">Basement 1</option>
                    <option value="L1">Level 1</option>
                    <option value="L2">Level 2</option>
                    <option value="L3">Level 3</option>
                    <option value="L4">Level 4</option>
                    <option value="L5">Level 5</option>
                <% } %>
                
                <% if(level.equals("B1")){ %>
                    <option value="">--Select level--</option>
                    <option value="B1" selected="selected">Basement 1</option>
                    <option value="L1">Level 1</option>
                    <option value="L2">Level 2</option>
                    <option value="L3">Level 3</option>
                    <option value="L4">Level 4</option>
                    <option value="L5">Level 5</option>
                <% } %>
                
                <% if(level.equals("L1")){ %>
                    <option value="">--Select level--</option>
                    <option value="B1">Basement 1</option>
                    <option value="L1" selected="selected">Level 1</option>
                    <option value="L2">Level 2</option>
                    <option value="L3">Level 3</option>
                    <option value="L4">Level 4</option>
                    <option value="L5">Level 5</option>
                <% } %>
                
                <% if(level.equals("L2")){ %>
                    <option value="">--Select level--</option>
                    <option value="B1">Basement 1</option>
                    <option value="L1">Level 1</option>
                    <option value="L2" selected="selected">Level 2</option>
                    <option value="L3">Level 3</option>
                    <option value="L4">Level 4</option>
                    <option value="L5">Level 5</option>
                <% } %>
                
                <% if(level.equals("L3")){ %>
                    <option value="">--Select level--</option>
                    <option value="B1">Basement 1</option>
                    <option value="L1">Level 1</option>
                    <option value="L2">Level 2</option>
                    <option value="L3" selected="selected">Level 3</option>
                    <option value="L4">Level 4</option>
                    <option value="L5">Level 5</option>
                <% } %>
                
                <% if(level.equals("L4")){ %>
                    <option value="">--Select level--</option>
                    <option value="B1">Basement 1</option>
                    <option value="L1">Level 1</option>
                    <option value="L2">Level 2</option>
                    <option value="L3">Level 3</option>
                    <option value="L4" selected="selected">Level 4</option>
                    <option value="L5">Level 5</option>
                <% } %>
                
                <% if(level.equals("L5")){ %>
                    <option value="">--Select level--</option>
                    <option value="B1">Basement 1</option>
                    <option value="L1">Level 1</option>
                    <option value="L2">Level 2</option>
                    <option value="L3">Level 3</option>
                    <option value="L4">Level 4</option>
                    <option value="L5" selected="selected">Level 5</option>
                <% } %>
            </select><br>
            Select date:  
            <input type="date" name = "currentdate" value="<%= cDate %>" /><br>
            Select time: 
            <input type="time" step = "1" name ="currenttime" value="<%= cTime %>" /><br><br>
            <input type="submit" value="Generate Report" style="background-color:#1e4f8a; color:#FFFFFF; font-weight: bold; font-size:14px; border: 1px solid #0d2c52;" />
        </form>
        </div>
        </div>
        
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
            ArrayList<String> errors = (ArrayList<String>)request.getAttribute("errors");
            if(errors != null && errors.size() > 0)
            {
                out.println("<div class='alert alert-danger' role='alert'>");
                for(String error:errors)
                {
                    out.println("<center><strong>Error! </strong>" + error + "</center>");
                }
                out.println("</div>");
            }
        
            HashMap<String, Integer> sematicCountMap = (HashMap<String, Integer>)session.getAttribute("sematicCountMap");
            if(sematicCountMap != null)
            {
                %>
                    <div class="panel panel-success">
                    <div class="panel-heading" style="font-weight: bold;">Results</div>
                    <div class="panel-body">
                <%
                HashMap sortedsematicCountMap = new HashMap();
                sortedsematicCountMap = HashMapSorting.sortByValue(sematicCountMap);
                Set sematicCountSet = sortedsematicCountMap.keySet();
                Iterator iter = sematicCountSet.iterator();
                out.println("<table border='1'>");
                out.println("<tr class='d0'>");
                out.println("<th>Sematic Place</th>"); 
                out.println("<th>Density</th>");
                out.println("<th>Number of people using smartphones</th>");
                out.println("</tr>");
                while(iter.hasNext()){
                    String sematicPlace = (String)iter.next();
                    out.println("<tr>");
                    out.println("<td>" + sematicPlace + "</td>");
                    int count = sematicCountMap.get(sematicPlace);
                    out.println("<td><center>");
                    if(count==0){
                        out.print("0");
                    }else if(count<=3){
                        out.print("1");
                    }else if(count<=7){
                        out.print("2");
                    }else if(count<=13){
                        out.print("3");
                    }else if(count<=20){
                        out.print("4");
                    }else{
                        out.print("5");
                    }
                    out.print("</center></td>"); 
                    out.println("<td><center>" + count + "</center></td>");
                    out.println("</tr>");
                }
                out.println("</table>");
                
                session.removeAttribute("sematicCountMap");
                
                %></div>
                    </div><%
            }                
        %>
        
    </body>
</html>
