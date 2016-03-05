<%-- 
    Document   : top-k-school-display
    Created on : Oct 6, 2015, 4:41:40 PM
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
            TreeMap<Long, ArrayList<String>> treemap3 = (TreeMap<Long, ArrayList<String>>) session.getAttribute("top-k-school");
            NavigableMap<Long, ArrayList<String>> map3 = null;

            if (treemap3 != null) {
                map3 = treemap3.descendingMap();
            }
            if (map3 != null && !map3.isEmpty() && session.getAttribute("k-value") != null) {
                Set<Long> durSet = map3.keySet();
                Iterator iter = durSet.iterator();
                int k = (Integer) session.getAttribute("k-value");
                int rank = 1;
        %>

        <div class="panel panel-success">
            <div class="panel-heading" style="font-weight: bold;">Top-<%=k%> School Report</div>
            <div class="panel-body">

                Category: <%=session.getAttribute("category")%><br>
                Start Date: <%=session.getAttribute("startDate")%><br>
                End Date: <%=session.getAttribute("endDate")%><br>
                <table border="1">
                    <tr><th>Rank</th><th>School</th><th>Usage(second)</th></tr>
                            <%
                                while (iter.hasNext() && rank <= k) {
                                    out.println("<tr><td>" + rank + "</td><td>");
                                    long usage = (Long) iter.next();
                                    ArrayList<String> schoolList = map3.get(usage);

                                    for (int i = 0; i < schoolList.size(); i++) {
                                        String school = schoolList.get(i);
                                        out.println(school);
                                        rank++;
                                        if (i != schoolList.size() - 1) {
                                            out.println(",");
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
                    session.removeAttribute("top-k-school");
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
