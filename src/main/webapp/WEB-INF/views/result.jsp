<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, quiz.javadz19.models.Question" %>
<%
  String category = (String) request.getAttribute("category");
  List<Question> questions = (List<Question>) request.getAttribute("questions");
  boolean[] results = (boolean[]) request.getAttribute("results");
  int correctCount = (Integer) request.getAttribute("correctCount");
  int total = (Integer) request.getAttribute("total");
  int incorrectCount = (Integer) request.getAttribute("incorrectCount");
%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Результаты – <%= category %></title>
</head>
<body>
<h1>Викторина завершена!</h1>
<h2>Категория: <%= category %></h2>
<p><b>Правильных ответов:</b> <%= correctCount %> из <%= total %></p>
<p><b>Неправильных ответов (или пропущено):</b> <%= incorrectCount %></p>
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