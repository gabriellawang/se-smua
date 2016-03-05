<%-- 
    Document   : social-activeness-report
    Created on : Oct 22, 2015, 1:12:30 PM
    Author     : G4-T6
--%>

<%@page import="java.text.DecimalFormat"%>
<%@page import="com.app.model.User"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.TreeMap"%>
<%@include file="protect.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Social Activeness Report</title>
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
        <div class="panel-heading" style="font-weight: bold;">Social Activeness Report</div>
        <div class="panel-body">        
        <form action="social-activeness-report">
            Select a date: 
            <input type="date" name="date" /><br><br>
            <input type="submit" value="Generate Report" style="background-color:#1e4f8a; color:#FFFFFF; font-weight: bold; font-size:14px; border: 1px solid #0d2c52;" />
        </form>
        </div>
        </div>
        
        <%            
            String error = (String) request.getAttribute("error");
            if (error != null) {
                out.println("<div class='alert alert-danger' role='alert'><center><strong>Error! </strong>" + error + "</center></div>");
            }
            
            else {
                %>
                    <div class="panel panel-success">
                    <div class="panel-heading" style="font-weight: bold;">Results</div>
                    <div class="panel-body">    
                <%
                if (session.getAttribute("totalDuration") != null) {
                    long totalDuration = (Long) session.getAttribute("totalDuration");
                    float durationInMin = (float) totalDuration / 60;
                    int total = Math.round(totalDuration);
                    int totalMin = Math.round(durationInMin);
            %>
            <p>
                <b>The total usage time of applications in “Social” category: </b><%=total%> seconds (<%=totalMin%> min)<br>
            </p>
            <%
                session.removeAttribute("totalDuration");
            }
            TreeMap<String, Float> individualDuration = (TreeMap<String, Float>) session.getAttribute("individualDuration");
            if (individualDuration != null) {
                Set<String> appNames = individualDuration.keySet();
                Iterator ite = appNames.iterator();
                out.println("<p><b>The breakdown of individual app usage time as percentages of the total:</b>");
                out.println("<table border='1'><tr><th>Application Name</th><th>Percentage</th></tr>");
                while (ite.hasNext()) {
                    String name = (String) ite.next();
                    float percentage = individualDuration.get(name);
                    int percent = Math.round(percentage);
                    out.println("<tr><td>" + name + "</td><td>" + percent + "%</td></tr>");
                }
                out.println("</table></p>");
                session.removeAttribute("individualDuration");
            }

            if (session.getAttribute("totalTime") != null && session.getAttribute("groupTime") != null) {
                float totalTime = (Float) session.getAttribute("totalTime");
                float groupTime = (Float) session.getAttribute("groupTime");
                float aloneTime = totalTime - groupTime;
                int totalT = Math.round(totalTime);
                int groupT = Math.round(groupTime);
                int aloneT = Math.round(aloneTime);
        %>
        <p><b>The total time spent in the SIS building: </b><%=totalT%></p>
        <%
            if (totalTime != 0) {
                int groupPercent = Math.round(groupTime / totalTime * 100);
                int soloPercent = Math.round(aloneTime / totalTime * 100);
        %>
        <p>
            <b>The percentages of time spent in groups: </b><%=groupPercent%>%(<%=groupT%>s)<br>
            <b>The percentages of time spent alone: </b><%=soloPercent%>%(<%=aloneT%>s)
        </p>
        <%
                    }
            session.removeAttribute("totalTime");
            session.removeAttribute("groupTime");
                }
            %>
                    </div>
                </div>
            <%
            }
        %>
    </body>
</html>
