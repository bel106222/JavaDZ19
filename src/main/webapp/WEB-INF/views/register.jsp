<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
  String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Регистрация</title>
</head>
<body>
<h1>Регистрация нового пользователя</h1>

<% if (error != null) { %>
<p style="color: red;"><%= error %></p>
<% } %>

<%-- Форма регистрации отправляется на /auth (POST) с параметром action=register --%>
<form method="post" action="${pageContext.request.contextPath}/auth">
  <input type="hidden" name="action" value="register">
  <label>
    Придумайте логин:
    <input type="text" name="username" required>
  </label><br><br>
  <label>
    Придумайте пароль:
    <input type="password" name="password" required>
  </label><br><br>
  <input type="submit" value="Зарегистрироваться">
</form>

<p>
  Уже есть аккаунт? <a href="${pageContext.request.contextPath}/auth?action=login">Войти</a>
</p>
<p><a href="${pageContext.request.contextPath}/">На главную</a></p>
</body>
</html>