package com.example.financiescontrolbyandlari;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ShowDataActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText showDataDateEditText;
    private RecyclerView dataRecyclerView;
    private TextView selectedDateTextView;
    private TextView dataInfoTextView; // Новое TextView для вывода информации
    private List<Transaction> dataList;
    private DataAdapter adapter;
    private DatabaseHelper databaseHelper;



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_data);

        showDataDateEditText = findViewById(R.id.showDataDateEditText);
        dataRecyclerView = findViewById(R.id.dataRecyclerView);
        selectedDateTextView = findViewById(R.id.selectedDateTextView);


        dataList = new ArrayList<>();
        dataList.add(new Transaction("2024-01-01", "expense", 50.0, "Описание расхода"));
        dataList.add(new Transaction("2024-01-02", "income", 100.0, "Описание дохода"));

        // Установка OnClickListener для showDataDateEditText
        showDataDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(v);
            }
        });

        // Инициализация DatabaseHelper
        databaseHelper = new DatabaseHelper(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.showDataDateEditText) {
            showDatePicker(null);
        } else if (v.getId() == R.id.showDataButton) {
            String selectedDate = showDataDateEditText.getText().toString();
            showDataForSelectedDate(selectedDate);
        }
    }


    public void showData(View view) {
        String selectedDate = showDataDateEditText.getText().toString();
        showDataForSelectedDate(selectedDate);
    }


    public void showDatePicker(View view) {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // Обработка выбранной даты
                String selectedDate = String.format("%02d/%02d/%d", day, month + 1, year);

                if (selectedDateTextView != null) {
                    selectedDateTextView.setText(selectedDate);
                }

                if (showDataDateEditText != null) {
                    showDataDateEditText.setText(selectedDate);
                }

                if (dataInfoTextView != null) {
                    dataInfoTextView.setText("");
                }

                // Вызов метода для отображения данных после выбора даты
                showDataForSelectedDate(selectedDate);
            }

        };

        // Получение текущей даты
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Создание и отображение диалога для выбора даты
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, dateSetListener, year, month, day);
        datePickerDialog.show();
    }



    // Метод для отображения данных за выбранную дату
    private void showDataForSelectedDate(String selectedDate) {
        if (selectedDate.isEmpty()) {
            // Если дата не выбрана, выведите сообщение об ошибке
            Toast.makeText(this, "Выберите дату", Toast.LENGTH_SHORT).show();
            return;
        }

        // Получите данные для выбранной даты
        List<Transaction> filteredData = getDataForDate(selectedDate);

        if (filteredData.isEmpty()) {
            // Если данных за выбранную дату нет, выведите соответствующее сообщение
            Toast.makeText(this, "Нет данных за выбранную дату", Toast.LENGTH_SHORT).show();
            return;
        }

        // Добавление заголовков в TableLayout
        TableLayout tableLayout = findViewById(R.id.dataTableLayout);
        TableRow headerRow = (TableRow) getLayoutInflater().inflate(R.layout.table_row, null);
        ((TextView) headerRow.findViewById(R.id.column1)).setText("Дата");
        ((TextView) headerRow.findViewById(R.id.column2)).setText("Тип");
        ((TextView) headerRow.findViewById(R.id.column3)).setText("Сумма");
        ((TextView) headerRow.findViewById(R.id.column4)).setText("Название");
        tableLayout.removeAllViews();
        tableLayout.addView(headerRow);

        // Добавление данных в TableLayout
        for (Transaction transaction : filteredData) {
            TableRow dataRow = (TableRow) getLayoutInflater().inflate(R.layout.table_row, null);
            ((TextView) dataRow.findViewById(R.id.column1)).setText(transaction.getDate());
            ((TextView) dataRow.findViewById(R.id.column2)).setText(getTypeInRussian(transaction.getType()));
            ((TextView) dataRow.findViewById(R.id.column3)).setText(String.valueOf(transaction.getAmount()));
            ((TextView) dataRow.findViewById(R.id.column4)).setText(transaction.getName());
            tableLayout.addView(dataRow);
        }

        // Создание адаптера и установка его в RecyclerView
        adapter = new DataAdapter(filteredData);
        dataRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dataRecyclerView.setAdapter(adapter);
    }





    // Получение данных для выбранной даты из базы данных
    private List<Transaction> getDataForDate(String selectedDate) {
        List<Transaction> transactions = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Запрос данных из базы данных за выбранную дату
            String query = "SELECT * FROM " + DatabaseHelper.TABLE_TRANSACTIONS + " WHERE " + DatabaseHelper.KEY_DATE + " = ?";
            cursor = db.rawQuery(query, new String[]{selectedDate});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Извлечение данных из курсора
                    @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_ID));
                    @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_DATE));
                    @SuppressLint("Range") String type = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_TYPE));
                    @SuppressLint("Range") double amount = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.KEY_AMOUNT));
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_NAME));

                    // Учтем отсутствие названия при выводе
                    if (name == null || name.isEmpty()) {
                        name = "Без названия";
                    }

                    Transaction transaction = new Transaction(id, date, type, amount, name);
                    transactions.add(transaction);
                } while (cursor.moveToNext());
            }
        } finally {
            // Закрытие курсора и базы данных
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return transactions;
    }

    // Метод для отображения информации о данных в TextView
    private void displayDataInfo(List<Transaction> transactions) {
        // Добавление заголовков
        String header = "Дата\t\t\t\tТип\t\t\tСумма\t\t\tНазвание\n";
        StringBuilder infoBuilder = new StringBuilder(header);

        for (Transaction transaction : transactions) {
            // Добавление данных
            String info = String.format("%s\t\t\t\t%s\t\t\t%.2f\t\t\t%s\n",
                    transaction.getDate(), getTypeInRussian(transaction.getType()), transaction.getAmount(), transaction.getName());
            infoBuilder.append(info);
        }

        // Установка собранной информации в TextView
        if (dataInfoTextView != null) {
            dataInfoTextView.setText(infoBuilder.toString());
        }
    }



    // Метод для получения типа на русском языке
    private String getTypeInRussian(String type) {
        if ("expense".equals(type)) {
            return "Расход";
        } else if ("income".equals(type)) {
            return "Доход";
        }
        return type;
    }
}

