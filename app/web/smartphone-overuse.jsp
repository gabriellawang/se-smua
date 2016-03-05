<%-- 
    Document   : smartphone-overuse
    Created on : Oct 9, 2015, 1:58:27 PM
    Author     : G4T6
--%>
<%@page import="java.text.DecimalFormat"%>
<%@page import="com.app.model.User"%>
<%@include file="protect.jsp" %>
<%@page import="java.util.HashMap"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Smartphone Overuse Report</title>
       
        <link href="css/bootstrap.css" rel="stylesheet">
        <link href="css/bootstrap.min.css" rel="stylesheet">
        
        <script src="../js/jquery.min.js"></script>
        <script src="../js/bootstrap.min.js"></script>
    </head>
    <body>
        <%                
            User currentUser = (User) session.getAttribute("currentUser");
            
            String startDate = request.getParameter("start-date");
            String endDate = request.getParameter("end-date");
            
            if(startDate == null)
            {
                startDate = "";
            }
            if(endDate == null)
            {
                endDate = "";
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
                    <font colour="black">Welcome, <%= currentUser.getName()%></font>
                </p>
                <button type="button" class="navbar-btn navbar-right btn btn-primary" style="margin-right:10px"><a href="logout.jsp"><font color="white">Logout</font></a></button>
            </div>
        </nav>
                
        <div class="panel panel-primary">
        <div class="panel-heading" style="font-weight: bold;">Smartphone Overuse Report</div>
        <div class="panel-body">
        <form action="SmartphoneOveruseReport">
            Start Date: 
            <input type="date" name="start-date" value="<%= startDate %>" /><br>
            End Date: 
            <input type="date" name="end-date" value="<%= endDate %>"  /><br><br>
            <input type='submit' value='Generate Report' style="background-color:#1e4f8a; color:#FFFFFF; font-weight: bold; font-size:14px; border: 1px solid #0d2c52;" /><br>
        </form>
        </div>
        </div>
        <%
            if(request.getAttribute("error") != null){
                out.println("<div class='alert alert-danger' role='alert'><center><strong>Error! </strong>" + request.getAttribute("error") + "</center></div>");
            }
        %>
        
        <%
            if (session.getAttribute("duration") != null && session.getAttribute("gameDuration") != null && session.getAttribute("frequency") != null) {
                %>
                    <div class="panel panel-success">
                    <div class="panel-heading" style="font-weight: bold;">Results</div>
                    <div class="panel-body">
                <%
                char duration = (Character) session.getAttribute("duration");
                char gameDuration = (Character) session.getAttribute("gameDuration");
                char frequency = (Character) session.getAttribute("frequency");
                int durationValue = (Integer) session.getAttribute("durationValue");
                int gameDurationValue = (Integer) session.getAttribute("gameDurationValue");
                float fValue = (Float) session.getAttribute("frequencyValue");
                DecimalFormat df = new DecimalFormat("#.00");
                String frequencyValue=df.format(fValue); 
                HashMap<Character, String> metrics = new HashMap<Character, String>();
                metrics.put('S', "Severe");
                metrics.put('L', "Light");
                metrics.put('M', "Moderate");
                if (!(duration != 'S' && gameDuration != 'S' && frequency != 'S')) {
                    out.println("<b><font color='red'>Overusing</font></b><br>");
                    out.println("<ul><li>Average daily smartphone usage duration: " + durationValue);
                    out.println("(" + metrics.get(duration) + ")</li><br>");
                    out.println("<li>Average daily gaming duration: " + gameDurationValue);
                    out.println("(" + metrics.get(gameDuration) + ")</li><br>");
                    out.println("<li>Smartphone access frequency: " + frequencyValue);
                    out.println("(" + metrics.get(frequency) + ")</li></ul><br>");
                } else if (duration == 'L' && gameDuration == 'L' && frequency == 'L') {
                    out.println("<b><font color='green'>Normal</b></font><br>");
                    out.println("<ul><li>Average daily smartphone usage duration: " + durationValue);
                    out.println("(" + metrics.get(duration) + ")</li><br>");
                    out.println("<li>Average daily gaming duration: " + gameDurationValue);
                    out.println("(" + metrics.get(gameDuration) + ")</li><br>");
                    out.println("<li>Smartphone access frequency: " + frequencyValue);
                    out.println("(" + metrics.get(frequency) + ")</li></ul><br>");
                } else {
                    out.println("<font color='blue'><b>To Be Cautious</b></font><br>");
                    out.println("<ul><li>Average daily smartphone usage duration: " + durationValue);
                    out.println("(" + metrics.get(duration) + ")</li><br>");
                    out.println("<li>Average daily gaming duration: " + gameDurationValue);
                    out.println("(" + metrics.get(gameDuration) + ")</li><br>");
                    out.println("<li>Smartphone access frequency: " + frequencyValue);
                    out.println("(" + metrics.get(frequency) + ")</li></ul><br>");
                }
                session.removeAttribute("duration");
                session.removeAttribute("gameDuration");
                %>
                    </div>
                    </div>
                <%
            }
        %>
    </body>
</html>
