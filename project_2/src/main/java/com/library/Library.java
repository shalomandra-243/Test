package com.library;

import java.util.ArrayList;
import java.util.List;

public class Library {
    private List<Book> books = new ArrayList<>();

    public void addBook(Book book) {
        books.add(book);
    }

    public List<Book> findBookByTitle(String title) {
        List<Book> result = new ArrayList<>();
        for (Book book : books) {
            if (book.getTitle().toLowerCase().contains(title.toLowerCase())) {
                result.add(book);
            }
        }
        return result;
    }

    public List<Book> findBookByAuthor(String author) {
        List<Book> result = new ArrayList<>();
        for (Book book : books) {
            if (book.getAuthor().toLowerCase().contains(author.toLowerCase())) {
                result.add(book);
            }
        }
        return result;
    }

    public Book findBookByIsbn(String isbn) {
        for (Book book : books) {
            if (book.getIsbn().equals(isbn)) {
                return book;
            }
        }
        return null;
    }

    public String borrowBook(String isbn) {
        Book book = findBookByIsbn(isbn);
        if (book == null) {
            return "Книга не найдена";
        }
        if (book.isAvailable()) {
            book.setAvailable(false);
            return "Книга успешно взята";
        } else {
            return "Книга уже взята";
        }
    }

    public String returnBook(String isbn) {
        Book book = findBookByIsbn(isbn);
        if (book == null) {
            return "Книга не найдена";
        }
        if (!book.isAvailable()) {
            book.setAvailable(true);
            return "Книга успешно возвращена";
        } else {
            return "Книга уже доступна";
        }
    }

    public List<Book> getAllBooks() {
        return new ArrayList<>(books);
    }

    public List<Book> getAvailableBooks() {
        List<Book> available = new ArrayList<>();
        for (Book book : books) {
            if (book.isAvailable()) {
                available.add(book);
            }
        }
        return available;
    }

    //HTML-таблицы для веб-интерфейса
    public String getAllBooksHtmlTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("<table border='1'><tr><th>Название</th><th>Автор</th><th>Год</th><th>ISBN</th><th>Статус</th><th>Действия</th></tr>");
        for (Book book : books) {
            sb.append(book.toHtmlRowWithActions());
        }
        sb.append("</table>");
        return sb.toString();
    }

    public String getAvailableBooksHtmlTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("<table border='1'><tr><th>Название</th><th>Автор</th><th>Год</th><th>ISBN</th><th>Статус</th><th>Действия</th></tr>");
        for (Book book : getAvailableBooks()) {
            sb.append(book.toHtmlRowWithActions());
        }
        sb.append("</table>");
        return sb.toString();
    }

    public String getBooksByTitleHtmlTable(String title) {
        List<Book> found = findBookByTitle(title);
        if (found.isEmpty()) {
            return "<p>Книги не найдены</p>";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<table border='1'><tr><th>Название</th><th>Автор</th><th>Год</th><th>ISBN</th><th>Статус</th><th>Действия</th></tr>");
        for (Book book : found) {
            sb.append(book.toHtmlRowWithActions());
        }
        sb.append("</table>");
        return sb.toString();
    }

    public String getBooksByAuthorHtmlTable(String author) {
        List<Book> found = findBookByAuthor(author);
        if (found.isEmpty()) {
            return "<p>Книги не найдены</p>";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<table border='1'><tr><th>Название</th><th>Автор</th><th>Год</th><th>ISBN</th><th>Статус</th><th>Действия</th></tr>");
        for (Book book : found) {
            sb.append(book.toHtmlRowWithActions());
        }
        sb.append("</table>");
        return sb.toString();
    }

    public String removeBook(String isbn) {
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getIsbn().equals(isbn)) {
                books.remove(i);
                return "Книга удалена";
            }
        }
        return "Книга не найдена";
    }
}