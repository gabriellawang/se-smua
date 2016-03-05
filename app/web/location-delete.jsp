<%-- 
    Document   : location-delete
    Created on : Nov 2, 2015, 5:51:10 PM
    Author     : G4T6
--%>

<%@page import="com.app.model.LocationRecord"%>
<%@page import="java.util.ArrayList"%>
<%@page import="com.app.model.Admin"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Welcome to SMUA</title>
        <link href="css/bootstrap.css" rel="stylesheet">
        <link href="css/bootstrap.min.css" rel="stylesheet">
        
        <script src="../js/jquery.min.js"></script>
        <script src="../js/bootstrap.min.js"></script>
    </head>
    <body>
        <%
            Admin currentAdmin = (Admin) session.getAttribute("currentAdmin");
        %>
        <nav class="navbar navbar-default">
            <div class="container-fluid">
                <div class="navbar-header">
                    <a class="navbar-brand" href="admin_welcome.jsp">
                        <p>
                            <font color="#1e4f8a"><b>SMU </b></font>
                            <font color="#DAA520"><b>Analytics </b></font>
                            <font color="#B40404"><b>[</b></font>
                            <font color="#1e4f8a"><b>Administration Page</b></font>
                            <font color="#B40404"><b>]</b></font>
                        </p>
                    </a>
                </div>
                <p class="navbar-text" style="margin-right:10px">
                    <b><font colour="black">Welcome, <%= currentAdmin.getUsername() %></font></b>
                </p>
                <button type="button" class="navbar-btn navbar-right btn btn-primary" style="margin-right:10px"><a href="logout.jsp"><font color="white">Logout</font></a></button>
            </div>
        </nav>
        
        <div class="panel panel-primary">
        <div class="panel-heading" style="font-weight: bold;">Bootstrap: Deletion of Location Data</div>
        <div class="panel-body">
        <form action='location-delete'>
            <p>
            <b>Start Date: </b>
            <input type='date' name='start-date'/><br>
            <b>Start Time: </b>
            <input type='time' name='start-time'/><br>
            <b>End Date: </b>
            <input type='date' name='end-date'/><br>
            <b>End Time: </b>
            <input type='time' name='end-time'/><br>
            <b>MAC Address: </b>
            <input type='text' name='mac-address'/><br>
            <b>Location ID: </b>
            <input type='number' name='location-id' min='0'/><br>
            <b>Semantic Place: </b>&nbsp; 
            Floor <select name="floor" id='list1' onchange="semantic()">
                <option>--</option>
                <option value="B1">B1</option>
                <option value="L1">L1</option>
                <option value="L2">L2</option>
                <option value="L3">L3</option>
                <option value="L4">L4</option>
                <option value="L5">L5</option>
            </select>
            Location <select name="location" id='list2'>
                <option>--</option>
            </select>
            </p>

            <script>
                function semantic(){
                    var v1 = document.getElementById("list1").value;
                    var opt = '';
                    if(v1 === 'B1'){
                        opt += '<option value=\'CORRIDORTOSOE\'>CORRIDORTOSOE</option>';
                        opt += '<option value=\'LIFTLOBBY\'>LIFTLOBBY</option>';
                        opt += '<option value=\'STUDYAREA\'>STUDYAREA</option>';
                        opt += '<option value=\'NEAROSL\'>NEAROSL</option>';
                        opt += '<option value=\'CORRIDORTOLKS\'>CORRIDORTOLKS</option>';
                    }else if(v1 === 'L1'){
                        opt += '<option value=\'LOBBY\'>LOBBY</option>';
                        opt += '<option value=\'WAITINGAREA\'>WAITINGAREA</option>';
                        opt += '<option value=\'RECEPTION\'>RECEPTION</option>';
                    }else if(v1 === 'L2'){
                        opt += '<option value=\'LOBBY\'>LOBBY</option>';
                        opt += '<option value=\'STUDYAREA1\'>STUDYAREA1</option>';
                        opt += '<option value=\'STUDYAREA2\'>STUDYAREA2</option>';
                        opt += '<option value=\'SR2-4\'>SR2-4</option>';
                        opt += '<option value=\'SR2-3\'>SR2-3</option>';
                        opt += '<option value=\'SR2-2\'>SR2-2</option>';
                        opt += '<option value=\'SR2-1\'>SR2-1</option>';
                    }else if(v1 === 'L3'){
                        opt += '<option value=\'LOBBY\'>LOBBY</option>';
                        opt += '<option value=\'STUDYAREA1\'>STUDYAREA1</option>';
                        opt += '<option value=\'STUDYAREA2\'>STUDYAREA2</option>';
                        opt += '<option value=\'SR3-4\'>SR3-4</option>';
                        opt += '<option value=\'SR3-3\'>SR3-3</option>';
                        opt += '<option value=\'SR3-2\'>SR3-2</option>';
                        opt += '<option value=\'SR3-1\'>SR3-1</option>';
                        opt += '<option value=\'CLSRM\'>CLSRM</option>';
                    }else if(v1 === 'L4'){
                        opt += '<option value=\'LOBBY\'>LOBBY</option>';
                        opt += '<option value=\'STUDYAREA1\'>STUDYAREA1</option>';
                        opt += '<option value=\'STUDYAREA2\'>STUDYAREA2</option>';
                        opt += '<option value=\'STUDYAREA3\'>STUDYAREA3</option>';
                        opt += '<option value=\'STUDYAREA4\'>STUDYAREA4</option>';
                        opt += '<option value=\'CADOFFICE\'>CADOFFICE</option>';
                    }else if(v1 === 'L5'){
                        opt += '<option value=\'LOBBY\'>LOBBY</option>';
                        opt += '<option value=\'STUDYAREA1\'>STUDYAREA1</option>';
                        opt += '<option value=\'STUDYAREA2\'>STUDYAREA2</option>';
                        opt += '<option value=\'CADOFFICE\'>CADOFFICE</option>';
                    }
                    document.getElementById('list2').innerHTML = opt;
                }
            </script>

            <input type='submit' value='Delete Data' style="background-color:#1e4f8a; color:#FFFFFF; font-weight: bold; font-size:14px; border: 1px solid #0d2c52;" />
        </form>
        </div>
        </div>
        <%
            ArrayList<String> errorMsg = (ArrayList<String>)request.getAttribute("errorMsg");
            if(errorMsg != null && !errorMsg.isEmpty()){
                for(String err : errorMsg){
                    out.println("<div class='alert alert-danger' role='alert'><center><strong>Error! </strong>" + err + "</center></div>");
                    out.println("<br>");
                }
            }
        %>
        
        <%
            ArrayList<LocationRecord> result = (ArrayList<LocationRecord>) session.getAttribute("result");
            if(result != null){
                //out.println("<p><b>Result: </b><ul>");
                if(result.isEmpty()){
                    %>
                        <div class="panel panel-warning">
                        <div class="panel-heading" style="font-weight: bold;">Number of data deleted:  <%= result.size() %></div>
                        <div class="panel-body">  
                    <%
                    out.println("No data found.");
                    %>
                        </div>
                        </div>
                    <%
                }else{
                    %>
                        <div class="panel panel-success">
                        <div class="panel-heading" style="font-weight: bold;">Number of data deleted:  <%= result.size() %></div>
                        <div class="panel-body">  
                    <%
                    for(LocationRecord lr : result){
                        out.println("Timestamp: " + lr.getTimestamp() + "<br>");
                        out.println("MAC address: " + lr.getMacAddress() + "<br>");
                        out.println("Location ID: " + lr.getLocationId() + "<br>");
                    }
                }
                //out.println("</ul></p>");
                    %>
                        </div>
                        </div>
                    <%
            }
        %>
    </body>
</html>
