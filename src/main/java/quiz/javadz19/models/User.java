package quiz.javadz19.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Модель пользователя викторины.
 * Хранит логин и пароль. Реализует Serializable для сохранения в сессии.
 * Содержит статические методы для регистрации и аутентификации на основе списка в памяти.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    // Статический список всех зарегистрированных пользователей (in-memory)
    private static final List<User> users = new ArrayList<>();

    // Поля пользователя
    private String username;
    private String password; // В реальном проекте пароль хранится в хэшированном виде

    /**
     * Конструктор пользователя.
     * @param username логин
     * @param password пароль (в демо-версии без хэширования)
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Геттеры
    public String getUsername() { return username; }
    public String getPassword() { return password; }

    // Сеттер для пароля (необязателен, но оставлен для возможного расширения)
    public void setPassword(String password) { this.password = password; }

    /**
     * Регистрирует нового пользователя.
     * @param username логин
     * @param password пароль
     * @return true – регистрация успешна, false – пользователь с таким именем уже существует
     */
    public static synchronized boolean register(String username, String password) {
        // Проверяем уникальность имени (без учёта регистра)
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return false;
            }
        }
        users.add(new User(username, password));
        return true;
    }

    /**
     * Аутентификация пользователя.
     * @param username логин
     * @param password пароль
     * @return объект User при успехе, иначе null
     */
    public static synchronized User authenticate(String username, String password) {
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Возвращает копию списка пользователей (для отладки).
     */
    public static synchronized List<User> getAllUsers() {
        return new ArrayList<>(users);
    }
}