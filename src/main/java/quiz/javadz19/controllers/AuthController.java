package quiz.javadz19.controllers;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import quiz.javadz19.models.User;

/**
 * Контроллер аутентификации.
 * GET – показывает форму входа (или регистрации, в зависимости от параметра 'action').
 * POST – обрабатывает вход (action=login) или регистрацию (action=register).
 */
@WebServlet(name = "authController", value = "/auth")
public class AuthController extends HttpServlet {

    /**
     * Обрабатывает GET-запросы: отображает форму входа или регистрации.
     * Параметры:
     *   action=login – форма входа (по умолчанию)
     *   action=register – форма регистрации
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String action = request.getParameter("action");

        // --- Обработка выхода (logout) ---
        if ("logout".equals(action)) {
            // Если сессия существует – завершаем её
            if (session != null) {
                session.invalidate(); // удаляет все атрибуты и разрушает сессию
            }
            // Перенаправляем на страницу входа (можно на главную, но логичнее на вход)
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return; // обязательно, чтобы не продолжалась обработка
        }

        // Если пользователь уже залогинен – сразу перенаправляем на страницу викторины
        if (session != null && session.getAttribute("user") != null) {
            response.sendRedirect(request.getContextPath() + "/start");
            return;
        }

        // По умолчанию показываем форму входа
        if (action == null || action.isEmpty()) {
            action = "login";
        }

        // Передаём параметр action в JSP, чтобы корректно отобразить нужную форму
        request.setAttribute("action", action);

        // Передаём возможное сообщение об ошибке (из предыдущей попытки)
        // Будет установлено при POST-ошибках и форварде обратно на эту же страницу
        // Для GET обычно ошибок нет, но атрибут может быть передан из doPost через forward

        if ("register".equals(action)) {
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
        } else {
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        }
    }

    /**
     * Обрабатывает POST-запросы: вход или регистрация.
     * Параметры формы:
     *   username, password, action (login или register)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Базовая валидация заполнения полей
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            // Возвращаем на форму с ошибкой
            request.setAttribute("error", "Логин и пароль обязательны для заполнения.");
            forwardToForm(request, response, action);
            return;
        }

        username = username.trim();
        password = password.trim();

        if ("register".equals(action)) {
            // Попытка регистрации
            boolean success = User.register(username, password);
            if (success) {
                // Регистрация успешна – автоматически входим
                User user = User.authenticate(username, password); // гарантированно вернёт пользователя
                HttpSession session = request.getSession();
                session.setAttribute("user", user);
                response.sendRedirect(request.getContextPath() + "/start");
            } else {
                // Пользователь с таким именем уже существует
                request.setAttribute("error", "Пользователь с таким логином уже зарегистрирован.");
                forwardToForm(request, response, action);
            }
        } else { // action == "login"
            // Попытка входа
            User user = User.authenticate(username, password);
            if (user != null) {
                HttpSession session = request.getSession();
                session.setAttribute("user", user);
                response.sendRedirect(request.getContextPath() + "/start");
            } else {
                request.setAttribute("error", "Неверный логин или пароль.");
                forwardToForm(request, response, action);
            }
        }
    }

    /**
     * Вспомогательный метод: пересылает запрос обратно на форму с сохранением текущего action и ошибки.
     */
    private void forwardToForm(HttpServletRequest request, HttpServletResponse response, String action)
            throws ServletException, IOException {
        request.setAttribute("action", action);
        if ("register".equals(action)) {
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
        } else {
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        }
    }
}