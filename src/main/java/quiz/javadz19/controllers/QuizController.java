package quiz.javadz19.controllers;

import java.io.*;
import java.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import quiz.javadz19.models.Question;
import quiz.javadz19.models.QuizModel;

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

        String category = request.getParameter("category");
        HttpSession session = request.getSession();

        // --- Ветка 1: Начало новой викторины (пришёл параметр category) ---
        if (category != null && !category.isEmpty()) {
            // Получаем вопросы для выбранной категории через модель
            List<Question> categoryQuestions = QuizModel.getQuestionsByCategory(category);

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
            // Передаём данные в JSP результатов
            request.setAttribute("category", quizCategory);
            request.setAttribute("questions", quizQuestions);
            request.setAttribute("results", results);
            request.setAttribute("correctCount", correctCount);
            request.setAttribute("total", quizQuestions.size());
            request.setAttribute("incorrectCount", quizQuestions.size() - correctCount);

            // Очищаем сессию, чтобы результаты не показывались повторно
            session.removeAttribute("quizQuestions");
            session.removeAttribute("currentIndex");
            session.removeAttribute("results");
            session.removeAttribute("category");

            request.getRequestDispatcher("/WEB-INF/views/result.jsp").forward(request, response);
            return;
        }

        // Иначе отображаем текущий вопрос
        Question currentQuestion = quizQuestions.get(currentIndex);
        request.setAttribute("question", currentQuestion);
        request.setAttribute("currentIndex", currentIndex);
        request.setAttribute("totalQuestions", quizQuestions.size());
        request.setAttribute("category", quizCategory);

        request.getRequestDispatcher("/WEB-INF/views/question.jsp").forward(request, response);
    }

    /**
     * Обрабатывает POST-запросы – приход ответа от пользователя или автоматическая отправка при таймауте.
     * Определяет правильность ответа с помощью модели, сохраняет результат, увеличивает индекс и перенаправляет на GET.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
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

        // Используем модель для проверки ответа
        boolean isCorrect = QuizModel.checkAnswer(currentQ, answerParam, isTimeout);

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