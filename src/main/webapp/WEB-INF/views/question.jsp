<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page isELIgnored="false" %>   <!-- убедимся, что EL включён (по умолчанию true в современных контейнерах) -->
<%@ page import="quiz.javadz19.models.Question" %>
<%
    // Получаем данные от контроллера
    Question question = (Question) request.getAttribute("question");
    int currentIndex = (Integer) request.getAttribute("currentIndex");
    int totalQuestions = (Integer) request.getAttribute("totalQuestions");
    String category = (String) request.getAttribute("category");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Викторина – <%= category %></title>
    <style>
        #timer { font-size: 24px; font-weight: bold; margin-bottom: 15px; }
        .question-text { font-size: 20px; margin-bottom: 15px; }
    </style>
</head>
<body>

<h4>Пользователь: ${sessionScope.user.username}</h4>
<%-- Ошибка через EL (если передана) --%>
${not empty error ? '<p style="color:red;">'.concat(error).concat('</p>') : ''}

<h2>Категория: <%= category %></h2>
<div id="timer">Осталось: 30 сек.</div>
<div class="question-text">
    <b>Вопрос <%= currentIndex + 1 %> из <%= totalQuestions %>:</b><br>
    <%= question.getText() %>
</div>

<%-- Форма отправляет ответ на /quiz (POST) --%>
<form id="quizForm" method="post" action="${pageContext.request.contextPath}/quiz">
    <input type="hidden" name="timeout" id="timeoutFlag" value="false">

    <%-- Тип вопроса определяет, что показывать: варианты или текстовое поле --%>
    <% if (question.isTextInput()) { %>
    <label>
        Ваш ответ: <input type="text" name="answer" id="answerInput" placeholder="Введите ответ">
    </label><br><br>
    <% } else { %>
    <label><input type="radio" name="answer" value="A"> <%= question.getOptionA() %></label><br>
    <label><input type="radio" name="answer" value="B"> <%= question.getOptionB() %></label><br>
    <label><input type="radio" name="answer" value="C"> <%= question.getOptionC() %></label><br><br>
    <% } %>

    <input type="submit" id="submitBtn" value="Ответить">
</form>

<%-- JavaScript-таймер и логика отправки --%>
<script>
    var timeLeft = 30;
    var timerDisplay = document.getElementById('timer');
    var timeoutFlag = document.getElementById('timeoutFlag');
    var form = document.getElementById('quizForm');
    var submitBtn = document.getElementById('submitBtn');

    // Автофокус на текстовое поле, если вопрос текстовый
    <% if (question.isTextInput()) { %>
    document.getElementById('answerInput').focus();
    <% } %>

    // Таймер обратного отсчёта
    var timerId = setInterval(function() {
        timeLeft--;
        timerDisplay.innerText = 'Осталось: ' + timeLeft + ' сек.';
        if (timeLeft <= 0) {
            clearInterval(timerId);
            timeoutFlag.value = 'true';   // флаг таймаута
            submitBtn.disabled = true;    // блокируем кнопку
            form.submit();                // автоматическая отправка
        }
    }, 1000);

    // При ручной отправке останавливаем таймер, чтобы избежать двойной отправки
    form.addEventListener('submit', function() {
        clearInterval(timerId);
        submitBtn.disabled = true;
    });
</script>
</body>
</html>