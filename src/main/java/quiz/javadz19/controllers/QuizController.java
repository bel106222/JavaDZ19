package quiz.javadz19.controllers;

import java.io.*;
import java.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import quiz.javadz19.models.Question;
import quiz.javadz19.models.Quiz;

/**
 * Контроллер викторины.
 * Обрабатывает показ вопросов по одному с таймером 30 секунд.
 * Поддерживает вопросы с вариантами ответов и текстовым вводом.
 * Состояние викторины хранится в HTTP-сессии.
 */
@WebServlet(name = "quizController", value = "/quiz")
public class QuizController extends HttpServlet {

    /**
     * Обрабатывает GET-запросы.
     * Если пришёл параметр category – начинается новая викторина (сохраняется в сессию).
     * Иначе продолжается текущая: показывается очередной вопрос или результаты.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Защита: только для авторизованных пользователей
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }

        String category = request.getParameter("category");

        // --- Ветка 1: Начало новой викторины (пришёл параметр category) ---
        if (category != null && !category.isEmpty()) {
            // Получаем вопросы для выбранной категории через модель
            List<Question> categoryQuestions = Quiz.getQuestionsByCategory(category);
            // Сохраняем время начала всей викторины и список для длительностей ответов
            session.setAttribute("quizStartTime", System.currentTimeMillis());
            session.setAttribute("questionDurations", new ArrayList<Long>());
            // Если категория не найдена – показываем страницу с ошибкой
            if (categoryQuestions.isEmpty()) {
                request.setAttribute("error", "Вопросы категории \"" + category + "\" не найдены.");
                request.getRequestDispatcher("/WEB-INF/views/start.jsp").forward(request, response);
                return;
            }

            // Сохраняем состояние новой викторины в сессии
            session.setAttribute("quizQuestions", categoryQuestions);
            session.setAttribute("currentIndex", 0);
            session.setAttribute("results", new boolean[categoryQuestions.size()]);
            session.setAttribute("category", category);

            // Перенаправляем на этот же сервлет без параметров, чтобы показать первый вопрос
            response.sendRedirect(request.getContextPath() + "/quiz");
            return;
        }

        // --- Ветка 2: Продолжение существующей викторины ---
        List<Question> quizQuestions = (List<Question>) session.getAttribute("quizQuestions");
        Integer currentIndex = (Integer) session.getAttribute("currentIndex");
        String quizCategory = (String) session.getAttribute("category");

        // Если состояние отсутствует – перенаправляем на выбор категории
        if (quizQuestions == null || currentIndex == null) {
            response.sendRedirect(request.getContextPath() + "/start");
            return;
        }

        // Если все вопросы пройдены – показываем результаты и очищаем сессию
        if (currentIndex >= quizQuestions.size()) {
            boolean[] results = (boolean[]) session.getAttribute("results");
            // Подсчёт правильных ответов
            int correctCount = 0;
            for (boolean b : results) {
                if (b) correctCount++;
            }
            // Подсчёт времени
            List<Long> durations = (List<Long>) session.getAttribute("questionDurations");
            Long quizStartTime = (Long) session.getAttribute("quizStartTime");

            double totalTimeSec = 0.0;
            double avgTimeSec = 0.0;

            if (durations != null && quizStartTime != null) {
                long totalMillis = 0;
                for (long d : durations) {
                    totalMillis += d;
                }
                totalTimeSec = totalMillis / 1000.0;
                if (!durations.isEmpty()) {
                    avgTimeSec = totalTimeSec / durations.size();
                }
            }
            request.setAttribute("totalTimeSeconds", totalTimeSec);
            request.setAttribute("averageTimeSeconds", avgTimeSec);

            // Передаём данные в JSP результатов
            request.setAttribute("category", quizCategory);
            request.setAttribute("questions", quizQuestions);
            request.setAttribute("results", results);
            request.setAttribute("correctCount", correctCount);
            request.setAttribute("total", quizQuestions.size());
            request.setAttribute("incorrectCount", quizQuestions.size() - correctCount);

            // Очистка состояния викторины (но не сессии пользователя), чтобы не показать повторно
            session.removeAttribute("quizQuestions");
            session.removeAttribute("currentIndex");
            session.removeAttribute("results");
            session.removeAttribute("category");
            session.removeAttribute("quizStartTime");
            session.removeAttribute("questionDurations");
            session.removeAttribute("questionStartTime");

            request.getRequestDispatcher("/WEB-INF/views/result.jsp").forward(request, response);
            return;
        }

        // Иначе отображаем текущий вопрос
        Question currentQuestion = quizQuestions.get(currentIndex);
        request.setAttribute("question", currentQuestion);
        request.setAttribute("currentIndex", currentIndex);
        request.setAttribute("totalQuestions", quizQuestions.size());
        request.setAttribute("category", quizCategory);
        // Перед forward на question.jsp запоминаем время начала показа вопроса.
        session.setAttribute("questionStartTime", System.currentTimeMillis());
        request.getRequestDispatcher("/WEB-INF/views/question.jsp").forward(request, response);
    }

    /**
     * Обрабатывает POST-запросы – приход ответа от пользователя или автоматическая отправка при таймауте.
     * Определяет правильность ответа с помощью модели, сохраняет результат, увеличивает индекс и перенаправляет на GET.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Защита POST-запросов
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }

        List<Question> quizQuestions = (List<Question>) session.getAttribute("quizQuestions");
        Integer currentIndex = (Integer) session.getAttribute("currentIndex");
        boolean[] results = (boolean[]) session.getAttribute("results");

        // Если состояние потеряно – возвращаем на выбор категории
        if (quizQuestions == null || currentIndex == null || results == null) {
            response.sendRedirect(request.getContextPath() + "/start");
            return;
        }

        // Получаем параметры из формы
        String answerParam = request.getParameter("answer");
        String timeoutParam = request.getParameter("timeout");
        // проверяем, равна ли строка, полученная из HTTP-параметра timeoutParam, строке "true",
        // и присваивает результат этого сравнения переменной isTimeout (защита от null)
        boolean isTimeout = "true".equals(timeoutParam);

        Question currentQ = quizQuestions.get(currentIndex);

        // Получаем время начала вопроса и вычисляем длительность ответа
        Long questionStartTime = (Long) session.getAttribute("questionStartTime");
        if (questionStartTime != null) {
            long duration = System.currentTimeMillis() - questionStartTime;
            List<Long> durations = (List<Long>) session.getAttribute("questionDurations");
            if (durations != null) {
                durations.add(duration);
            }
        }

        // Используем модель для проверки ответа
        boolean isCorrect = Quiz.checkAnswer(currentQ, answerParam, isTimeout);

        // Сохраняем результат и переходим к следующему вопросу
        results[currentIndex] = isCorrect;
        session.setAttribute("currentIndex", currentIndex + 1);
        session.setAttribute("results", results);

        // PRG-паттерн: POST -> Redirect -> GET
        // Это защищает от случайного дублирования отправки ответа при обновлении
        // страницы и обеспечивает корректное состояние приложения
        response.sendRedirect(request.getContextPath() + "/quiz");
    }
}