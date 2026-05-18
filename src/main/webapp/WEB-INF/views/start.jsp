<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="quiz.javadz19.models.User" %>
<%@ page isELIgnored="false" %>   <!-- убедимся, что EL включён (по умолчанию true в современных контейнерах) -->
<%
    // Получаем данные, переданные контроллером
    List<String> categories = (List<String>) request.getAttribute("categories");
    String error = (String) request.getAttribute("error");
    User user = (User) session.getAttribute("user");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Выбор категории</title>
</head>
<body>
<%-- Блок пользователя и выхода --%>
<%-- Блок пользователя и выхода --%>
<% if (user != null) { %>
<p>
    Пользователь: <strong><%= user.getUsername() %></strong> |
    <a href="${pageContext.request.contextPath}/auth?action=logout">Выйти</a>
</p>
<% } %>

<% if (error != null) { %>
<p style="color: red;"><%= error %></p>
<% } %>
<h1>Выберите категорию вопросов</h1>
<%-- Если есть ошибка, выводим её --%>
<% if (error != null) { %>
    <p style="color: red;"><%= error %></p>
<% } %>

<%-- Форма отправляется на /start (POST) --%>
<form method="post" action="${pageContext.request.contextPath}/start">
    <% for (String category : categories) { %>
        <label>
            <input type="radio" name="category" value="<%= category %>"> <%= category %>
        </label><br>
    <% } %>
    <br>
    <input type="submit" value="Начать викторину">
</form>
</body>
</html>