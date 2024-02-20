package com.example.financiescontrolbyandlari;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "transactions_db";
    public static final int DATABASE_VERSION = 2;

    // Таблица транзакций
    private Context context;

    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String KEY_ID = "id";
    public static final String KEY_DATE = "date";
    public static final String KEY_TYPE = "type";
    public static final String KEY_AMOUNT = "amount";
    public static final String KEY_NAME = "name";

    // Создание таблицы транзакций
    private static final String CREATE_TABLE_TRANSACTIONS = "CREATE TABLE " + TABLE_TRANSACTIONS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_DATE + " TEXT,"
            + KEY_TYPE + " TEXT,"
            + KEY_AMOUNT + " REAL,"
            + KEY_NAME + " TEXT" + ")";

    private static final String TABLE_SETTINGS = "settings";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_INITIAL_BALANCE = "initial_balance";

    private static final String CREATE_TABLE_SETTINGS = "CREATE TABLE " + TABLE_SETTINGS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_INITIAL_BALANCE + " REAL DEFAULT 0" + ")";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Создание таблицы транзакций
        db.execSQL(CREATE_TABLE_TRANSACTIONS);
        db.execSQL(CREATE_TABLE_SETTINGS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Удаление старой таблицы при обновлении
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        // Создание новой таблицы
        onCreate(db);
    }

    // В DatabaseHelper
    public double updateInitialBalance(double transactionAmount, String transactionType) {
        SQLiteDatabase db = this.getWritableDatabase();
        double currentBalance = getInitialBalance();
        double newBalance;

        // Добавим лог для отслеживания изменения счета
        Log.d("DatabaseHelper", "Before Update - Current Balance: " + currentBalance);

        if ("income".equals(transactionType)) {
            // Если транзакция - доход, добавляем к текущему балансу
            newBalance = currentBalance + transactionAmount;
        } else if ("expense".equals(transactionType)) {
            // Если транзакция - расход, вычитаем из текущего баланса
            newBalance = currentBalance - transactionAmount;
        } else {
            // Неизвестный тип транзакции, не вносим изменений
            newBalance = currentBalance;
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_INITIAL_BALANCE, newBalance);

        db.update(TABLE_SETTINGS, values, null, null);
        db.close();

        // Добавим лог для отслеживания изменения счета
        Log.d("DatabaseHelper", "After Update - New Balance: " + newBalance);

        return newBalance;
    }

    public void deleteTransaction(int transactionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TRANSACTIONS, KEY_ID + " = ?", new String[]{String.valueOf(transactionId)});
        db.close();
    }


    public double updateAndGetCurrentBalance(double amount) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Получаем текущий баланс из базы данных
        double currentBalance = getInitialBalance();

        // Вычисляем новый баланс
        double newBalance = currentBalance + amount;

        // Обновляем значение в базе данных
        updateInitialBalance(newBalance);

        // Возвращаем новый баланс
        return newBalance;
    }


    public void updateInitialBalance(double newBalance) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_INITIAL_BALANCE, newBalance);
        db.update(TABLE_SETTINGS, values, null, null);
    }



    public void saveInitialBalance(double initialBalance) {
        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("initialBalance", (float) initialBalance);
        editor.apply();
    }

    public double getInitialBalance() {
        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return preferences.getFloat("initialBalance", 0);
    }

    public double getAndIncrementInitialBalance(double transactionAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        double currentBalance = getInitialBalance();
        double newBalance = currentBalance + transactionAmount;

        ContentValues values = new ContentValues();
        values.put(COLUMN_INITIAL_BALANCE, newBalance);

        db.update(TABLE_SETTINGS, values, null, null);
        db.close();

        return newBalance;
    }

    // Сохранение транзакции
    public long saveTransaction(Transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DATE, transaction.getDate());
        values.put(KEY_TYPE, transaction.getType().toLowerCase());
        values.put(KEY_AMOUNT, transaction.getAmount());
        values.put(KEY_NAME, transaction.getName());

        // Вставка строки в таблицу
        long id = db.insert(TABLE_TRANSACTIONS, null, values);

        // Обновление счета в зависимости от типа транзакции
        double transactionAmount = transaction.getAmount();
        updateInitialBalance(transactionAmount, transaction.getType());

        // Закрытие базы данных
        db.close();

        return id;
    }





    private double getUpdatedInitialBalance(Transaction transaction) {
        double currentBalance = getInitialBalance();
        if ("income".equals(transaction.getType())) {
            // Если транзакция - доход, увеличиваем счет
            currentBalance += transaction.getAmount();
        } else {
            // Если транзакция - расход, уменьшаем счет
            currentBalance -= transaction.getAmount();
        }
        return currentBalance;
    }

    @SuppressLint("Range")
    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Запрос всех строк таблицы
        String selectQuery = "SELECT * FROM " + TABLE_TRANSACTIONS;
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Перебор всех строк и добавление в список
        if (cursor.moveToFirst()) {
            do {
                Transaction transaction = new Transaction();
                transaction.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
                transaction.setDate(cursor.getString(cursor.getColumnIndex(KEY_DATE)));
                transaction.setType(cursor.getString(cursor.getColumnIndex(KEY_TYPE)).toLowerCase());
                transaction.setAmount(cursor.getDouble(cursor.getColumnIndex(KEY_AMOUNT)));
                transaction.setName(cursor.getString(cursor.getColumnIndex(KEY_NAME)));

                transactions.add(transaction);
            } while (cursor.moveToNext());
        }

        // Закрытие курсора и базы данных
        cursor.close();
        db.close();

        return transactions;
    }

    public List<Transaction> getDataForDate(String selectedDate) {
        List<Transaction> transactions = new ArrayList<>();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT * FROM " + TABLE_TRANSACTIONS + " WHERE " + KEY_DATE + " = ?";
            cursor = db.rawQuery(query, new String[]{selectedDate});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                    @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex(KEY_DATE));
                    @SuppressLint("Range") String type = cursor.getString(cursor.getColumnIndex(KEY_TYPE)).toLowerCase();
                    @SuppressLint("Range") double amount = cursor.getDouble(cursor.getColumnIndex(KEY_AMOUNT));
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));

                    Transaction transaction = new Transaction(id, date, type, amount, name);
                    transactions.add(transaction);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return transactions;
    }

    // Удаление транзакции по ID
    public void deleteTransaction(long transactionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TRANSACTIONS, KEY_ID + " = ?", new String[]{String.valueOf(transactionId)});
        db.close();
    }
}
