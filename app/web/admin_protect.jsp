<%
    if (session.getAttribute("currentAdmin") == null) {
        response.sendRedirect("admin_login.jsp");
        return;
    }
%>
