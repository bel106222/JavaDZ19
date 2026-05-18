<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page isELIgnored="false" %>   <!-- убедимся, что EL включён (по умолчанию true в современных контейнерах) -->
<%@ page import="java.util.List, quiz.javadz19.models.Question" %>
<%@ page import="quiz.javadz19.models.User" %>
<%
  String category = (String) request.getAttribute("category");
  List<Question> questions = (List<Question>) request.getAttribute("questions");
  boolean[] results = (boolean[]) request.getAttribute("results");
  int correctCount = (Integer) request.getAttribute("correctCount");
  int total = (Integer) request.getAttribute("total");
  int incorrectCount = (Integer) request.getAttribute("incorrectCount");
  User user = (User) session.getAttribute("user");
  double totalTimeSeconds = (Double) request.getAttribute("totalTimeSeconds");
  double averageTimeSeconds = (Double) request.getAttribute("averageTimeSeconds");
%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Результаты – <%= category %></title>
</head>
<body>
<%-- Блок пользователя и выхода --%>
<% if (user != null) { %>
<p>
  Пользователь: <strong><%= user.getUsername() %></strong> |
  <a href="${pageContext.request.contextPath}/auth?action=logout">Выйти</a>
</p>
<hr>
<% } %>
<h1>Викторина завершена!</h1>
<h2>Категория: <%= category %></h2>
<p><b>Правильных ответов:</b> <%= correctCount %> из <%= total %></p>
<p><b>Неправильных ответов (или пропущено):</b> <%= incorrectCount %></p>
<hr>
<%
  totalTimeSeconds = (Double) request.getAttribute("totalTimeSeconds");
  averageTimeSeconds = (Double) request.getAttribute("averageTimeSeconds");
%>
<p><b>Общее время викторины:</b> <%= String.format("%.1f", totalTimeSeconds) %> сек.</p>
<p><b>Среднее время на вопрос:</b> <%= String.format("%.1f", averageTimeSeconds) %> сек.</p>
<hr>
<h3>Детализация:</h3>
<ol>
  <% for (int i = 0; i < total; i++) {
    Question q = questions.get(i); %>
  <li>
    <%= q.getText() %><br>
    <% if (q.isTextInput()) { %>
    <% if (results[i]) { %>
    ✅ Верно
    <% } else { %>
    ❌ Неверно (правильный ответ: <%= q.getCorrectText() %>)
    <% } %>
    <% } else { %>
    <% if (results[i]) { %>
    ✅ Верно
    <% } else { %>
    ❌ Неверно (правильный: <%= q.getCorrect() %>)
    <% } %>
    <% } %>
  </li><br>
  <% } %>
</ol>
<p><a href="${pageContext.request.contextPath}/start">Пройти ещё раз</a></p>
</body>
</html>