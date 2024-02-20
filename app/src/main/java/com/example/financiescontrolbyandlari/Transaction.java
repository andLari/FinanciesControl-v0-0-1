package com.example.financiescontrolbyandlari;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Transaction {
    private int id;
    private String date;
    private String type; // "expense" или "income"
    private double amount;
    private String name;

    // Конструктор без параметров
    public Transaction() {
        // Конструктор без параметров (необязательно)

        this.date = date;
    }

    public float getDateAsXValue() {
        // Преобразование строки даты в числовое значение (возможно, UNIX-время, если такое нужно)
        // Пример для возвращения таймстампа:
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = dateFormat.parse(this.date);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public float getAmountAsYValue() {
        return (float) amount; // Предполагается, что `amount` - это float или double
    }

    // Конструктор с параметрами (без id)
    public Transaction(String date, String type, double amount, String name) {
        this.date = date;
        this.type = type;
        this.amount = amount;
        this.name = name;
    }

    // Конструктор с параметрами (включая id)
    public Transaction(int id, String date, String type, double amount, String name) {
        this.id = id;
        this.date = date;
        this.type = type;
        this.amount = amount;
        this.name = name;
    }

    // Геттеры и сеттеры (необязательно)

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
