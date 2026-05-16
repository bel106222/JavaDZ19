package quiz.javadz19.controllers;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import quiz.javadz19.models.QuizModel;

/**
 * Контроллер выбора категории.
 * GET – показывает форму выбора (forward на start.jsp).
 * POST – получает выбранную категорию и перенаправляет на QuizController.
 */
@WebServlet(name = "startController", value = "/start")
public class StartController extends HttpServlet {

    /**
     * Отображает страницу выбора категории.
     * Передаёт список категорий в атрибут "categories".
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Получаем список категорий из модели
        request.setAttribute("categories", QuizModel.getCategories());
        // Передаём управление на JSP-представление
        request.getRequestDispatcher("/WEB-INF/views/start.jsp").forward(request, response);
    }

    /**
     * Обрабатывает отправку формы с выбранной категорией.
     * Если категория не выбрана – снова показывает форму с ошибкой.
     * Иначе перенаправляет на QuizController с параметром category.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String selectedCategory = request.getParameter("category");

        if (selectedCategory == null || selectedCategory.isEmpty()) {
            // Категория не выбрана: добавляем сообщение об ошибке и возвращаем форму
            request.setAttribute("error", "Пожалуйста, выберите категорию.");
            request.setAttribute("categories", QuizModel.getCategories());
            request.getRequestDispatcher("/WEB-INF/views/start.jsp").forward(request, response);
            return;
        }

        // Категория выбрана – кодируем для безопасной передачи в URL
        String encodedCategory = URLEncoder.encode(selectedCategory, StandardCharsets.UTF_8);
        // Перенаправляем на QuizController (PRG-паттерн: POST -> Redirect -> GET)
        response.sendRedirect(request.getContextPath() + "/quiz?category=" + encodedCategory);
    }
}