package com.library;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class LibraryWebApp {
    private static Library library = new Library();

    public static void main(String[] args) throws IOException {
        //Добавление тестовых книг
        library.addBook(new Book("Война и мир", "Лев Толстой", 1869, "978-5-123456-78-9"));
        library.addBook(new Book("Преступление и наказание", "Фёдор Достоевский", 1866, "978-5-987654-32-1"));
        library.addBook(new Book("Мастер и Маргарита", "Михаил Булгаков", 1967, "978-5-111111-11-1"));
        library.addBook(new Book("1984", "Джордж Оруэлл", 1949, "978-5-222222-22-2"));
        library.addBook(new Book("Маленький принц", "Антуан де Сент-Экзюпери", 1943, "978-5-333333-33-3"));
        library.addBook(new Book("Гарри Поттер и философский камень", "Джоан Роулинг", 1997, "978-5-444444-44-4"));

        //Создание и настройка сервера
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        //Обработчики
        server.createContext("/", new HomeHandler());
        server.createContext("/add", new AddBookHandler());
        server.createContext("/search-title", new SearchTitleHandler());
        server.createContext("/search-author", new SearchAuthorHandler());
        server.createContext("/borrow", new BorrowHandler());
        server.createContext("/return", new ReturnHandler());
        server.createContext("/available", new AvailableHandler());
        server.createContext("/remove", new RemoveHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("Сервер запущен на http://localhost:8080/");
        System.out.println("Нажмите Ctrl+C для остановки сервера");
    }

    //Главная страница
    static class HomeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Библиотека</title>
                    <meta charset="UTF-8">
                    <style>
                        body {
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            margin: 40px;
                            background-color: #f5f5f5;
                        }
                        .container {
                            max-width: 1200px;
                            margin: 0 auto;
                            background: white;
                            padding: 30px;
                            border-radius: 10px;
                            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                        }
                        h1 {
                            color: #2c3e50;
                            border-bottom: 2px solid #3498db;
                            padding-bottom: 10px;
                        }
                        .menu {
                            background: #3498db;
                            padding: 15px;
                            border-radius: 5px;
                            margin: 20px 0;
                        }
                        .menu a {
                            color: white;
                            text-decoration: none;
                            margin-right: 25px;
                            padding: 8px 15px;
                            border-radius: 4px;
                            transition: background 0.3s;
                        }
                        .menu a:hover {
                            background: #2980b9;
                        }
                        table {
                            border-collapse: collapse;
                            width: 100%;
                            margin-top: 20px;
                        }
                        th {
                            background-color: #2c3e50;
                            color: white;
                            padding: 12px;
                            text-align: left;
                        }
                        td {
                            border: 1px solid #ddd;
                            padding: 10px;
                            text-align: left;
                        }
                        tr:nth-child(even) {
                            background-color: #f9f9f9;
                        }
                        tr:hover {
                            background-color: #f1f1f1;
                        }
                        .available {
                            color: #27ae60;
                            font-weight: bold;
                        }
                        .unavailable {
                            color: #e74c3c;
                            font-weight: bold;
                        }
                        .actions a {
                            margin-right: 10px;
                            color: #3498db;
                        }
                        .error {
                            color: #e74c3c;
                            background: #fdf2f2;
                            padding: 10px;
                            border-radius: 5px;
                            margin: 10px 0;
                        }
                        .success {
                            color: #27ae60;
                            background: #f0f9f0;
                            padding: 10px;
                            border-radius: 5px;
                            margin: 10px 0;
                        }
                        form {
                            background: #f9f9f9;
                            padding: 20px;
                            border-radius: 5px;
                            margin: 20px 0;
                        }
                        input[type="text"], input[type="number"] {
                            padding: 8px;
                            margin: 5px 0 15px 0;
                            width: 300px;
                            border: 1px solid #ddd;
                            border-radius: 4px;
                        }
                        input[type="submit"] {
                            background: #3498db;
                            color: white;
                            border: none;
                            padding: 10px 20px;
                            border-radius: 4px;
                            cursor: pointer;
                        }
                        input[type="submit"]:hover {
                            background: #2980b9;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1> Система учёта библиотеки</h1>

                        <div class="menu">
                            <a href='/'> Главная</a>
                            <a href='/add'> Добавить книгу</a>
                            <a href='/search-title'> Найти по названию</a>
                            <a href='/search-author'> Найти по автору</a>
                            <a href='/available'> Доступные книги</a>
                        </div>

                        <h2> Все книги в библиотеке:</h2>
                """ + library.getAllBooksHtmlTable() + """
                    </div>
                </body>
                </html>
                """;
            sendHtml(exchange, response);
        }
    }

    //Добавление книги
    static class AddBookHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String form = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <title>Добавить книгу</title>
                        <meta charset="UTF-8">
                        <link rel="stylesheet" type="text/css" href="/style">
                    </head>
                    <body>
                        <div class="container">
                            <h1> Добавить новую книгу</h1>
                            <div class="menu">
                                <a href='/'> На главную</a>
                            </div>

                            <form method='POST'>
                                <label>Название:</label><br>
                                <input type='text' name='title' required placeholder="Введите название книги"><br><br>

                                <label>Автор:</label><br>
                                <input type='text' name='author' required placeholder="Введите автора"><br><br>

                                <label>Год издания:</label><br>
                                <input type='number' name='year' required min="1000" max="2024" placeholder="Год"><br><br>

                                <label>ISBN:</label><br>
                                <input type='text' name='isbn' required placeholder="Введите ISBN"><br><br>

                                <input type='submit' value='Добавить книгу'>
                            </form>
                        </div>
                    </body>
                    </html>
                    """;
                sendHtml(exchange, form);
            } else if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> params = parseParams(readBody(exchange));
                String title = params.get("title");
                String author = params.get("author");
                String yearStr = params.get("year");
                String isbn = params.get("isbn");

                String error = "";
                int year = 0;

                if (title == null || title.trim().isEmpty()) {
                    error = "Название книги не может быть пустым";
                } else if (author == null || author.trim().isEmpty()) {
                    error = "Автор не может быть пустым";
                } else if (yearStr == null || yearStr.trim().isEmpty()) {
                    error = "Год издания не может быть пустым";
                } else {
                    try {
                        year = Integer.parseInt(yearStr);
                        if (year < 1000 || year > 2024) {
                            error = "Год должен быть между 1000 и 2024";
                        }
                    } catch (NumberFormatException e) {
                        error = "Год должен быть числом";
                    }
                }

                if (isbn == null || isbn.trim().isEmpty()) {
                    error = "ISBN не может быть пустым";
                }

                if (!error.isEmpty()) {
                    String errorPage = """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <title>Ошибка</title>
                            <meta charset="UTF-8">
                            <link rel="stylesheet" type="text/css" href="/style">
                        </head>
                        <body>
                            <div class="container">
                                <h1> Ошибка добавления книги</h1>
                                <div class="error">%s</div>
                                <br>
                                <a href='/add'>← Вернуться к форме добавления</a>
                            </div>
                        </body>
                        </html>
                        """.formatted(error);
                    sendHtml(exchange, errorPage);
                } else {
                    // Проверяем, существует ли книга с таким ISBN
                    Book existingBook = library.findBookByIsbn(isbn);
                    if (existingBook != null) {
                        String errorPage = """
                            <!DOCTYPE html>
                            <html>
                            <head>
                                <title>Ошибка</title>
                                <meta charset="UTF-8">
                                <link rel="stylesheet" type="text/css" href="/style">
                            </head>
                            <body>
                                <div class="container">
                                    <h1> Ошибка добавления книги</h1>
                                    <div class="error">Книга с ISBN %s уже существует</div>
                                    <br>
                                    <a href='/add'>← Вернуться к форме добавления</a>
                                </div>
                            </body>
                            </html>
                            """.formatted(isbn);
                        sendHtml(exchange, errorPage);
                    } else {
                        library.addBook(new Book(title.trim(), author.trim(), year, isbn.trim()));
                        redirect(exchange, "/");
                    }
                }
            }
        }
    }

    //Поиск по названию
    static class SearchTitleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String form = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <title>Поиск по названию</title>
                        <meta charset="UTF-8">
                        <link rel="stylesheet" type="text/css" href="/style">
                    </head>
                    <body>
                        <div class="container">
                            <h1> Поиск книги по названию</h1>
                            <div class="menu">
                                <a href='/'> На главную</a>
                            </div>

                            <form method='POST'>
                                <label>Введите название или часть названия:</label><br>
                                <input type='text' name='title' placeholder="Название книги">
                                <input type='submit' value='Искать'>
                            </form>
                        </div>
                    </body>
                    </html>
                    """;
                sendHtml(exchange, form);
            } else if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> params = parseParams(readBody(exchange));
                String title = params.get("title");
                String result = library.getBooksByTitleHtmlTable(title);

                String page = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <title>Результаты поиска</title>
                        <meta charset="UTF-8">
                        <link rel="stylesheet" type="text/css" href="/style">
                    </head>
                    <body>
                        <div class="container">
                            <h1> Результаты поиска по названию: "%s"</h1>
                            <div class="menu">
                                <a href='/'> На главную</a>
                                <a href='/search-title'> Новый поиск</a>
                            </div>

                            %s

                            <br>
                            <a href='/search-title'>← Вернуться к поиску</a>
                        </div>
                    </body>
                    </html>
                    """.formatted(title != null ? title : "", result);
                sendHtml(exchange, page);
            }
        }
    }

    //Поиск по автору
    static class SearchAuthorHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String form = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <title>Поиск по автору</title>
                        <meta charset="UTF-8">
                        <link rel="stylesheet" type="text/css" href="/style">
                    </head>
                    <body>
                        <div class="container">
                            <h1> Поиск книги по автору</h1>
                            <div class="menu">
                                <a href='/'> На главную</a>
                            </div>

                            <form method='POST'>
                                <label>Введите имя автора или часть имени:</label><br>
                                <input type='text' name='author' placeholder="Имя автора">
                                <input type='submit' value='Искать'>
                            </form>
                        </div>
                    </body>
                    </html>
                    """;
                sendHtml(exchange, form);
            } else if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> params = parseParams(readBody(exchange));
                String author = params.get("author");
                String result = library.getBooksByAuthorHtmlTable(author);

                String page = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <title>Результаты поиска</title>
                        <meta charset="UTF-8">
                        <link rel="stylesheet" type="text/css" href="/style">
                    </head>
                    <body>
                        <div class="container">
                            <h1> Результаты поиска по автору: "%s"</h1>
                            <div class="menu">
                                <a href='/'> На главную</a>
                                <a href='/search-author'> Новый поиск</a>
                            </div>

                            %s

                            <br>
                            <a href='/search-author'>← Вернуться к поиску</a>
                        </div>
                    </body>
                    </html>
                    """.formatted(author != null ? author : "", result);
                sendHtml(exchange, page);
            }
        }
    }

    //Взятие книги
    static class BorrowHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);
            String isbn = params.get("isbn");
            String message = library.borrowBook(isbn);
            System.out.println("Взятие книги ISBN " + isbn + ": " + message);
            redirect(exchange, "/");
        }
    }

    //Возврат книги
    static class ReturnHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);
            String isbn = params.get("isbn");
            String message = library.returnBook(isbn);
            System.out.println("Возврат книги ISBN " + isbn + ": " + message);
            redirect(exchange, "/");
        }
    }

    //Доступные книги
    static class AvailableHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Доступные книги</title>
                    <meta charset="UTF-8">
                    <link rel="stylesheet" type="text/css" href="/style">
                </head>
                <body>
                    <div class="container">
                        <h1>✅ Доступные книги</h1>
                        <div class="menu">
                            <a href='/'>🏠 На главную</a>
                        </div>

                        %s
                    </div>
                </body>
                </html>
                """.formatted(library.getAvailableBooksHtmlTable());
            sendHtml(exchange, response);
        }
    }

    //Удаление книги
    static class RemoveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);
            String isbn = params.get("isbn");
            String message = library.removeBook(isbn);
            System.out.println("Удаление книги ISBN " + isbn + ": " + message);
            redirect(exchange, "/");
        }
    }

    //Вспомогательные методы ---

    private static void sendHtml(HttpExchange exchange, String html) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private static void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(303, 0);
        exchange.getResponseBody().close();
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    private static Map<String, String> parseParams(String body) throws UnsupportedEncodingException {
        Map<String, String> params = new HashMap<>();
        if (body != null && !body.isEmpty()) {
            String[] pairs = body.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx > 0) {
                    String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                    String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                    params.put(key, value);
                }
            }
        }
        return params;
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx > 0) {
                    String key = pair.substring(0, idx);
                    String value = pair.substring(idx + 1);
                    params.put(key, value);
                }
            }
        }
        return params;
    }
}