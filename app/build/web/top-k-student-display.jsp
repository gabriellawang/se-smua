<%-- 
    Document   : top-k-student-display
    Created on : Oct 6, 2015, 4:41:21 PM
    Author     : G4T6
--%>
<%@page import="com.app.model.User"%>
<%@include file="protect.jsp" %>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.NavigableMap"%>
<%@page import="java.util.TreeMap"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.ArrayList"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Top-k Report</title>
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
            TreeMap<Long, ArrayList<HashMap<String, String>>> treemap2 = (TreeMap<Long, ArrayList<HashMap<String, String>>>) session.getAttribute("top-k-student");
            NavigableMap<Long, ArrayList<HashMap<String, String>>> map2 = null;

            if (treemap2 != null) {
                map2 = treemap2.descendingMap();
            }
            if (map2 != null && !map2.isEmpty() && session.getAttribute("k-value") != null) {
                Set<Long> durSet = map2.keySet();
                Iterator iter = durSet.iterator();
                int k = (Integer) session.getAttribute("k-value");
                int rank = 1;
        %>
        
        <div class="panel panel-success">
        <div class="panel-heading" style="font-weight: bold;">Top-<%=k%> Student Report</div>
        <div class="panel-body">

        Category: <%=session.getAttribute("category")%><br>
        Start Date: <%=session.getAttribute("startDate")%><br>
        End Date: <%=session.getAttribute("endDate")%><br>
        <table border="1">
            <tr><th>Rank</th><th>Student name(Mac Address)</th><th>Usage(second)</th></tr>
                    <%
                        while (iter.hasNext() && rank <= k) {
                            out.println("<tr><td>" + rank + "</td><td>");
                            long usage = (Long) iter.next();
                            ArrayList<HashMap<String, String>> nameMacList = map2.get(usage);

                            for (int i = 0; i < nameMacList.size(); i++) {
                                HashMap<String, String> nameMac = nameMacList.get(i);
                                Set<String> nameSet = nameMac.keySet();
                                Iterator nameIte = nameSet.iterator();
                                while (nameIte.hasNext()) {
                                    String n = (String) nameIte.next();
                                    String m = nameMac.get(n);
                                    out.println(n + "(" + m + ")");
                                    rank++;
                                    if (i != nameMacList.size() - 1) {
                                        out.println(",<br>");
                                    }
                                }
                            }
                            out.println("</td><td>" + usage + "</td></tr>");
                        }
                    %>
        </table>
                    <%
                        session.removeAttribute("category");
                        session.removeAttribute("start-Date");
                        session.removeAttribute("end-Date");
                        session.removeAttribute("top-k-student");
                        session.removeAttribute("k-value");
                    %>
            </div>
        </div>
            
        <%
        }
            ArrayList<String> errorList = (ArrayList<String>) request.getAttribute("errorList");
            if (errorList != null && !errorList.isEmpty()) {
        %>
        <br><br><br>
        <div class='alert alert-danger' role='alert'>
            <center>
                <h1>Error Message:</h1>
                <ul>
                    <%
                        for (String s : errorList) {
                            out.println(s + "<br>");
                        }
                    %>
                </ul>
            </center>
        </div>
        <%
            }
        %>       
        <br><br>
        <center><strong><font color="#1e4f8a">Click </font>
        <a href="top-k.jsp">
            <font color="#1e4f8a"><u>HERE </u></font>
        </a>
            <font color="#1e4f8a">to return back to Top-K App Usage Report</font></strong></center>
    </body>
</html>
