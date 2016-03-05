<%-- 
    Document   : top-k-display
    Created on : Oct 3, 2015, 11:13:33 PM
    Author     : Gabriella
--%>
<%@page import="com.app.model.User"%>
<%@include file="protect.jsp" %>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.NavigableMap"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.TreeMap"%>
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
        <%            User currentUser = (User) session.getAttribute("currentUser");
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
            TreeMap<Long, ArrayList<String>> treemap1 = (TreeMap<Long, ArrayList<String>>) session.getAttribute("top-k-popApp");
            NavigableMap<Long, ArrayList<String>> map1 = null;
            if (treemap1 != null) {
                map1 = treemap1.descendingMap();
            }

            if (map1 != null && !map1.isEmpty() && session.getAttribute("k-value") != null) {
                Set<Long> durSet = map1.keySet();
                Iterator iter = durSet.iterator();
                int k = (Integer) session.getAttribute("k-value");
                int rank = 1;
        %>

        <div class="panel panel-success">
            <div class="panel-heading" style="font-weight: bold;">Top-<%=k%> App Usage Report</div>
            <div class="panel-body">

                School: <%=session.getAttribute("school")%><br>
                Start Date: <%=session.getAttribute("startDate")%><br>
                End Date: <%=session.getAttribute("endDate")%><br>
                <table border="1">
                    <tr><th>Rank(for most popular apps)</th><th>App names</th><th>Usage(second)</th></tr>
                            <%
                                while (iter.hasNext() && rank <= k) {
                                    out.println("<tr><td>" + rank + "</td><td>");
                                    long usage = (Long) iter.next();
                                    ArrayList<String> apps = map1.get(usage);

                                    for (int i = 0; i < apps.size(); i++) {
                                        out.println(apps.get(i));
                                        rank++;
                                        if (i != apps.size() - 1) {
                                            out.println(",");
                                        }
                                    }
                                    out.println("</td><td>" + usage + "</td></tr>");
                                }
                            %>
                </table>
                <%
                    session.removeAttribute("school");
                    session.removeAttribute("start-Date");
                    session.removeAttribute("end-Date");
                    session.removeAttribute("top-k-popApp");
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
