<%-- 
    Document   : breakdownUsageTime
    Created on : Sep 28, 2015, 7:25:26 PM
    Author     : G4T6
--%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.HashMap"%>
<%@include file="protect.jsp" %>
<%@page import="com.app.model.User"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.DecimalFormat"%>
<%@page import="java.util.ArrayList"%>
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
           
           if(startDate == null)
           {
               startDate = "";
           }
           if(endDate == null)
           {
               endDate = "";
           }
                
        %>
        
        <link rel="stylesheet" href="//code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">
        <script src="//code.jquery.com/jquery-1.10.2.js"></script>
        <script src="//code.jquery.com/ui/1.11.4/jquery-ui.js"></script>
        <link rel="stylesheet" href="/resources/demos/style.css">
        
        <%--
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
        --%>
        
        <div class="panel panel-primary">
            <div class="panel-heading" style="font-weight: bold;">Basic Usage App Report: Breakdown by Usage Time Category</div>
            <div class="panel-body">
                <form action="BreakDownTimeUsage">
                    <p>Select Start Date: 
                        <input type="date" name="startDate" value="<%=startDate%>" /></p>
                        <%--- <input type="text" name="startDate" id="datepicker" readonly value="<%=startDate%>" /></p> ---%>
                    <p>Select End Date:  
                        <input type="date" name="endDate" value="<%=endDate%>" /></p>
                        <%--- <input type="text" name="endDate" id="datepicker2" readonly value="<%=endDate%>" /></p> ---%>
                        <input type="submit" value="Generate Report" style="background-color:#1e4f8a; color:#FFFFFF; font-weight: bold; font-size:14px; border: 1px solid #0d2c52;" />
                </form>
            </div>
        </div>
        <br>
        
        <%
            String error = (String)request.getAttribute("error");
            if(error != null && error.length() > 0)
            {
                out.println("<div class='alert alert-danger' role='alert'><center><strong>Error! </strong>" + error + "</center></div>");
            }
        %>
        
        <%
            HashMap<String, Integer> results = (HashMap<String, Integer>)session.getAttribute("results");
            String start = (String)session.getAttribute("sd");
            String end = (String)session.getAttribute("ed");
            if(results == null)
            {
                session.removeAttribute("results");
                return;
            }
            
            int intenseUser = 0;
            int normalUser = 0;
            int mildUser = 0;     
            int total = 0;
            
            Iterator<String> it = results.keySet().iterator();  
            while (it.hasNext()) {
                String category = it.next();
                int count = results.get(category);
                if(category.equals("intenseUser"))
                {
                    intenseUser = count;
                }else if(category.equals("normalUser"))
                {
                    normalUser = count;
                }else if(category.equals("mildUser"))
                {
                    mildUser = count;
                }      
            }
            
            total = intenseUser + normalUser + mildUser;
            int perIU = 0;
            int perNU = 0;
            int perMU = 0;
            
            if(intenseUser > 0)
            {
                double tpPerIU = ((intenseUser+0.0)/total)*100;
                perIU = (int)Math.round(tpPerIU);
            }
            
            if(normalUser > 0)
            {
                double tpPerNU = ((normalUser+0.0)/total)*100;
                perNU = (int)Math.round(tpPerNU);
            }
            
            if(mildUser > 0)
            {
                double tpPerMU = ((mildUser+0.0)/total)*100;
                perMU = (int)Math.round(tpPerMU);
            }
            
        %>
       
        <div class="panel panel-success">
        <div class="panel-heading" style="font-weight: bold;">Results</div>
        <div class="panel-body">
            <table>
                <tr>
                    <td>Start Date: <%= start %></td>
                </tr>
                <tr>
                    <td>End Date: <%= end %></td>
                </tr>
                <tr>
                    <td><br><br></td>
                </tr>
                <tr>
                    <td>Total User: <%=total %>(100%) ==>&nbsp;</td>
                    <td>Intense User: <%=intenseUser %>(<%= perIU %>%),&nbsp;</td>
                    <td>Normal User: <%=normalUser %>(<%= perNU %>%),&nbsp;</td>
                    <td>Mild User: <%=mildUser %>(<%= perMU %>%)</td>
                </tr>
            </table>
        </div>
        </div>
        <%
                session.removeAttribute("results"); 
                session.removeAttribute("sd"); 
                session.removeAttribute("ed"); 
        %>
    </body>
</html>
