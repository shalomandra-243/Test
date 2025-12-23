package com.library;

import java.io.Serializable;

public class Book implements Serializable {
    private String title;
    private String author;
    private int year;
    private String isbn;
    private boolean isAvailable;

    public Book(String title, String author, int year, String isbn) {
        this.title = title;
        this.author = author;
        this.year = year;
        this.isbn = isbn;
        this.isAvailable = true;
    }

    //Геттеры и сеттеры
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    @Override
    public String toString() {
        return title + " | " + author + " | " + year + " | " + isbn + " | " + (isAvailable ? "Доступна" : "Взята");
    }

    public String toHtmlRow() {
        String color = isAvailable ? "green" : "red";
        String status = isAvailable ? "Доступна" : "Взята";
        return "<tr><td>" + title + "</td><td>" + author + "</td><td>" + year + "</td><td>" + isbn
                + "</td><td><span style='color:" + color + ";'>" + status + "</span></td></tr>";
    }

    public String toHtmlRowWithActions() {
        String color = isAvailable ? "green" : "red";
        String status = isAvailable ? "Доступна" : "Взята";
        String borrowReturnBtn = isAvailable
                ? "<a href='/borrow?isbn=" + isbn + "'>Взять</a>"
                : "<a href='/return?isbn=" + isbn + "'>Вернуть</a>";

        return "<tr><td>" + title + "</td><td>" + author + "</td><td>" + year + "</td><td>" + isbn
                + "</td><td><span style='color:" + color + ";'>" + status + "</span></td>"
                + "<td>" + borrowReturnBtn + " | <a href='/remove?isbn=" + isbn + "'>Удалить</a></td></tr>";
    }
}