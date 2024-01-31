package com.example.financiescontrolbyandlari;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "transactions_db";
    public static final int DATABASE_VERSION = 1;

    // Таблица транзакций
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

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Создание таблицы транзакций
        db.execSQL(CREATE_TABLE_TRANSACTIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Удаление старой таблицы при обновлении
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        // Создание новой таблицы
        onCreate(db);
    }

    // Сохранение транзакции
    public long saveTransaction(Transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DATE, transaction.getDate());
        values.put(KEY_TYPE, transaction.getType());
        values.put(KEY_AMOUNT, transaction.getAmount());
        values.put(KEY_NAME, transaction.getName());

        // Вставка строки в таблицу
        long id = db.insert(TABLE_TRANSACTIONS, null, values);

        // Закрытие базы данных
        db.close();

        return id;
    }

    // Получение списка всех транзакций
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
                transaction.setType(cursor.getString(cursor.getColumnIndex(KEY_TYPE)));
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
                    @SuppressLint("Range") String type = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
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
