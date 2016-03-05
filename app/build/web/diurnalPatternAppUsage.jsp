<%-- 
    Document   : diurnalPatternAppUsage
    Created on : Oct 7, 2015, 3:22:45 PM
    Author     : G4T6
--%>
<%@include file="protect.jsp" %>
<%@page import="com.app.controller.AppUsageUtility"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.ArrayList"%>
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
        <h1>
            <%                
                User currentUser = (User) session.getAttribute("currentUser");  
            %>
        </h1>
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
            String selectGender = request.getParameter("gender");
            String selectSchool = request.getParameter("school");
            String selectYear = request.getParameter("year");
            String selectDate = request.getParameter("date");
            
            if(selectGender == null)
            {
                selectGender = "";
            }
            if(selectSchool == null)
            {
                selectSchool = "";
            }
            if(selectYear == null)
            {
                selectYear = "";
            }
            if(selectDate == null)
            {
                selectDate = "";
            }
                    
            final String[] gender = {"M", "F"};
            final String[] school = {"sis", "accountancy", "business", "economics", "law", "socsc"};
            final String[] year = {"2011", "2012", "2013", "2014", "2015"};
        %>
        
        <div class="panel panel-primary">
            <div class="panel-heading" style="font-weight: bold;">Basic App Usage Report: Breakdown by Diurnal Pattern</div>
            <div class="panel-body">
        <form action="BreakDownDiurnalPattern">

            Select Gender:
            <select name="gender">
                <option value="">-- Select a choice --</option>
                <%                             
                    if(selectGender.equals("")){
                        for (String g : gender) {
                            out.println("<option value='" + g + "' >");
                            out.println(g);
                            out.println("</option>");                            
                        }
                    }
            
                    if(!selectGender.equals("")){
                        for (String g : gender) {
                            if(selectGender.equalsIgnoreCase(g)){
                                out.println("<option value='" + g + "' selected='selected'>");
                                out.println(g);
                                out.println("</option>");
                            }else{
                                out.println("<option value='" + g + "' >");
                                out.println(g);
                                out.println("</option>");
                            }                               
                        }
                    }
                %>
            </select><p><p>
            
                Select School:
                <select name="school">
                    <option value="">-- Select a choice --</option>
                    <%                             
                        if(selectSchool.equals("")){
                            for (String s : school) {
                                out.println("<option value='" + s + "' >");
                                out.println(s);
                                out.println("</option>");                            
                            }
                        }

                        if(!selectSchool.equals("")){
                            for (String s : school) {
                                if(selectSchool.equalsIgnoreCase(s)){
                                    out.println("<option value='" + s + "' selected='selected'>");
                                    out.println(s);
                                    out.println("</option>");
                                }else{
                                    out.println("<option value='" + s + "' >");
                                    out.println(s);
                                    out.println("</option>");
                                }                               
                            }
                        }
                    %> 
                </select><p>

                Select Year:
                <select name="year">
                    <option value="">-- Select a choice --</option>
                    <%                             
                        if(selectYear.equals("")){
                            for (String y : year) {
                                out.println("<option value='" + y + "' >");
                                out.println(y);
                                out.println("</option>");                            
                            }
                        }

                        if(!selectYear.equals("")){
                            for (String y : year) {
                                if(selectYear.equalsIgnoreCase(y)){
                                    out.println("<option value='" + y + "' selected='selected'>");
                                    out.println(y);
                                    out.println("</option>");
                                }else{
                                    out.println("<option value='" + y + "' >");
                                    out.println(y);
                                    out.println("</option>");
                                }                               
                            }
                        }
                    %> 
                </select><p>
                 Start Date: 
                <input type="date" name="date" /><br><br>
                <input type="submit" value="Generate Report" style="background-color:#1e4f8a; color:#FFFFFF; font-weight: bold; font-size:14px; border: 1px solid #0d2c52;" />
        </form>
        </div>
        </div>
                
        <style type="text/css">
            tr.d0 th {
                background-color: green;
                color: whitesmoke;
            }
        </style>
        
        <%
            // Print errors
            String errorMsg = (String)request.getAttribute("error");
            if (errorMsg!=null) {
                out.println("<div class='alert alert-danger' role='alert'><center><strong>Error! </strong>" + errorMsg + "</center></div>");
                session.removeAttribute("error");
            }
            
            // Print output 
            HashMap<Integer,Integer> results = (HashMap<Integer,Integer>) session.getAttribute("results");
            if (results != null && results.size() > 0) {
                %>
                    <div class="panel panel-success">
                    <div class="panel-heading" style="font-weight: bold;">Results</div>
                    <div class="panel-body">
                <%
                Iterator it = results.keySet().iterator();
                HashMap<Integer, String> outputHour = new HashMap<Integer,String>(); 
                outputHour.put(0,"00:00 - 01:00");
                outputHour.put(1,"01:00 - 02:00");
                outputHour.put(2,"02:00 - 03:00");
                outputHour.put(3,"03:00 - 04:00");
                outputHour.put(4,"04:00 - 05:00");
                outputHour.put(5,"05:00 - 06:00");
                outputHour.put(6,"06:00 - 07:00");
                outputHour.put(7,"07:00 - 08:00");
                outputHour.put(8,"08:00 - 09:00");
                outputHour.put(9,"09:00 - 10:00");
                outputHour.put(10,"10:00 - 11:00");
                outputHour.put(11,"11:00 - 12:00");
                outputHour.put(12,"12:00 - 13:00");
                outputHour.put(13,"13:00 - 14:00");
                outputHour.put(14,"14:00 - 15:00");
                outputHour.put(15,"15:00 - 16:00");
                outputHour.put(16,"16:00 - 17:00");
                outputHour.put(17,"17:00 - 18:00");
                outputHour.put(18,"18:00 - 19:00");
                outputHour.put(19,"19:00 - 20:00");
                outputHour.put(20,"20:00 - 21:00");
                outputHour.put(21,"21:00 - 22:00");
                outputHour.put(22,"22:00 - 23:00");
                outputHour.put(23,"23:00 - 00:00");
                
                out.println("<table style='text-align:center' border='1'>");
                out.println("<th>Hour</th>");
                out.println("<th>Usage Time (Seconds)</th>");
                
                for (int key = 0; key < 24; key++) {
                    out.println("<tr>");
                    out.println("<td>"+outputHour.get(key)+"</td>");
                    out.println("<td>"+results.get(key)+"</td>");
                    out.println("</tr>");
                }
                out.println("</table>");
                session.removeAttribute("results");
            }
            %>
                </div>
                </div>
            <%
        %>
    </body>
</html>
