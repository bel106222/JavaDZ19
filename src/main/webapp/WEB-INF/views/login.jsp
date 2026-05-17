<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    // Получаем сообщение об ошибке, если есть
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Вход в систему</title>
</head>
<body>
<h1>Вход в викторину</h1>

<% if (error != null) { %>
<p style="color: red;"><%= error %></p>
<% } %>

<%-- Форма входа отправляется на /auth (POST) с параметром action=login --%>
<form method="post" action="${pageContext.request.contextPath}/auth">
    <input type="hidden" name="action" value="login">
    <label>
        Логин:
        <input type="text" name="username" required>
    </label><br><br>
    <label>
        Пароль:
        <input type="password" name="password" required>
    </label><br><br>
    <input type="submit" value="Войти">
</form>

<p>
    Нет аккаунта? <a href="${pageContext.request.contextPath}/auth?action=register">Зарегистрироваться</a>
</p>
<p><a href="${pageContext.request.contextPath}/">На главную</a></p>
</body>
</html>