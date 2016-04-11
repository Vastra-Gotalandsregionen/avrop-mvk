<%@ page import="java.util.Enumeration" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
    <%
        Enumeration<String> attributeNames = request.getAttributeNames();

        out.write("<table><thead><tr><td>AttrName</td><td>Value</td></tr></thead><tbody>");

        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();

            out.write("<tr>");

            out.write("<td>" + name + "</td>");
            out.write("<td>" + request.getAttribute(name) + "</td>");

            out.write("</tr>");
        }

        out.write("</tbody></table>");

        Enumeration<String> parameterNames = request.getParameterNames();

        out.write("<table><thead><tr><td>AttrName</td><td>Value</td></tr></thead><tbody>");

        while (parameterNames.hasMoreElements()) {
            String name = parameterNames.nextElement();

            out.write("<tr>");

            out.write("<td>" + name + "</td>");
            out.write("<td>" + request.getParameter(name) + "</td>");

            out.write("</tr>");
        }

        out.write("</tbody></table>");

        out.write("<p>" + request.getUserPrincipal() + "</p>");
        if (request.getUserPrincipal() != null) {
            out.write("<p>" + request.getUserPrincipal().getClass() + "</p>");
        }

        if (request.getRemoteUser() != null) {
            out.write(request.getRemoteUser());
        }

        out.write("<h3>Headers</h3>");

        Enumeration<String> headerNames = request.getHeaderNames();

        out.write("<table><thead><tr><td>Header</td><td>Value</td></tr></thead><tbody>");

        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();

            out.write("<tr>");

            out.write("<td>" + name + "</td>");
            out.write("<td>" + request.getHeader(name) + "</td>");

            out.write("</tr>");
        }

        out.write("</tbody></table>");
    %>
</body>
</html>
