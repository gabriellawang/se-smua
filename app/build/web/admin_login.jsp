<%-- 
    Document   : adminLogin
    Created on : Sep 30, 2015, 5:25:22 PM
    Author     : G4-T6
--%>
<%
    if(session.getAttribute("currentAdmin") != null){
        response.sendRedirect("admin_welcome.jsp");
        return;
    }
%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>SMU Analytics Application(Administrator)</title>
        <link href="css/bootstrap.min.css" rel="stylesheet">   
    </head>
    <body style="background-image: url(css/images/smu.jpg); background-repeat:no-repeat; background-size:cover;">
        <br><br>
        <div align="center">           
        <h1>
            <font color="white" style="font-weight: bold">SMU </font>
            <font color="#DAA520" style="font-weight: bold">Analytics</font>
        </h1>  
        <h3><font color="white">Administrator Login</font></h3>
 
        <form action="ValidateAdmin" method="POST">
            <table>
                <tr>
                    <td>
                        <input type="text" name="username" placeholder='Username' class="form-control" style="background-color:#1e4f8a; color:#ebebeb; width: 350px; padding: 10px 4px 6px 3px; border: 1px solid #0d2c52;" />
                    </td>
                </tr>
                <tr>
                    <td>
                        <input type="password" name="password" placeholder='Password' class="form-control" style="background-color:#1e4f8a; color:#ebebeb; width: 350px; padding: 10px 4px 6px 3px; border: 1px solid #0d2c52;" />
                    </td>
                </tr>
                <tr><td><br></td></tr>
                <tr>
                    <td>
                        <p align="left"><input type="submit" value="Login" style="background-color:#1e4f8a; color:#FFFFFF; font-weight: bold; font-size:14px; border: 1px solid #0d2c52;" /></p>
                    </td>
                </tr>
            </table>
        </form>
        <br>
        <font color="white">Click </font>
        <a href="login.jsp"><font color="white"><u>HERE </u></font></a>
        <font color="white">to login as a Student</font>
        </div>
        <%
            String errorMsg = (String) request.getAttribute("errorMsg");
            if(errorMsg != null){
                out.println("<div class='alert alert-danger' role='alert'><center><strong>Error! </strong>" + errorMsg + "</center></div>");
            }
        %>
    </body>
</html>
