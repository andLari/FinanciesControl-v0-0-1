package com.example.financiescontrolbyandlari;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ShowDataActivity extends AppCompatActivity implements View.OnClickListener, ConfirmationDialogFragment.OnConfirmationListener {


    private boolean isChartMode = false;
    private Button showDataChartButton;
    private EditText showDataDateEditText;
    private TableLayout dataTableLayout;
    private List<Transaction> dataList;
    private DataAdapter adapter;
    private DatabaseHelper databaseHelper;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_data);

        showDataDateEditText = findViewById(R.id.showDataDateEditText);
        dataTableLayout = findViewById(R.id.dataTableLayout);
        dataList = getSampleData();  // Используем метод для получения примерных данных
        adapter = new DataAdapter(dataList, this); // передаем this в конструктор

        // Установка OnClickListener для showDataDateEditText
        showDataDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(v);
            }
        });

        // Инициализация DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        showDataChartButton = findViewById(R.id.showDataChartButton);
        showDataChartButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.showDataDateEditText) {
            showDatePicker(null);
        } else if (v.getId() == R.id.showDataButton) {
            String selectedDate = showDataDateEditText.getText().toString();
            showDataForSelectedDate(selectedDate);
        } else if (v.getId() == R.id.showDataChartButton) {
            String selectedDate = showDataDateEditText.getText().toString();
            if (dataExistsForSelectedDate(selectedDate)) {
                List<Transaction> transactions = getTransactionsFromDatabase(selectedDate);
                showDataAsGraph(transactions);
                // Показать график только после нажатия на кнопку
                findViewById(R.id.pieChart).setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "Нет данных за выбранную дату", Toast.LENGTH_SHORT).show();
            }
        }
    }





    public boolean dataExistsForSelectedDate(String selectedDate) {
        // Получение данных для выбранной даты
        List<Transaction> transactions = getTransactionsFromDatabase(selectedDate);

        // Проверка наличия данных
        return transactions != null && !transactions.isEmpty();
    }





    public void showDatePicker(View view) {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // Обработка выбранной даты
                String selectedDate = String.format("%02d/%02d/%d", day, month + 1, year);
                showDataDateEditText.setText(selectedDate);
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

    public String getSelectedDate() {
        return showDataDateEditText.getText().toString();
    }


    public void showDataForSelectedDate(String selectedDate) {
        List<Transaction> filteredData = null;

        if (selectedDate.isEmpty()) {
            // Если дата не выбрана, скрыть заголовки и данные
            hideDataAndGraph();
            Toast.makeText(this, "Выберите дату", Toast.LENGTH_SHORT).show();
            return;
        }

        // Получите данные для выбранной даты
        filteredData = getTransactionsFromDatabase(selectedDate);

        if (filteredData.isEmpty()) {
            // Если данных за выбранную дату нет, скрыть заголовки и данные
            hideDataAndGraph();
            Toast.makeText(this, "Нет данных за выбранную дату", Toast.LENGTH_SHORT).show();
            return;
        }

        // Показать заголовки и данные
        dataTableLayout.setVisibility(View.VISIBLE);

        // Добавление заголовков в TableLayout
        TableRow headerRow = createHeaderRow();
        dataTableLayout.removeAllViews();
        dataTableLayout.addView(headerRow);

        // Добавление данных в TableLayout
        for (Transaction transaction : filteredData) {
            TableRow dataRow = createDataRow(transaction);
            dataTableLayout.addView(dataRow);
        }

        // Показать график только после нажатия на кнопку
        findViewById(R.id.pieChart).setVisibility(View.GONE);
        findViewById(R.id.showDataChartButton).setVisibility(View.VISIBLE);
    }






    private void showDataAsGraph(List<Transaction> transactions) {
        // Очищаю предыдущие данные графика
        PieChart pieChart = findViewById(R.id.pieChart);
        pieChart.clear();

        // Создание списка входных данных
        List<PieEntry> entries = new ArrayList<>();

        // Переменные для общей суммы доходов и расходов
        double totalIncome = 0;
        double totalExpense = 0;

        // Получение данных для круговой диаграммы (название, сумма, тип)
        for (Transaction transaction : transactions) {
            // Добавляем текущую сумму к общей сумме доходов или расходов
            if ("income".equals(transaction.getType())) {
                totalIncome += transaction.getAmount();
            } else if ("expense".equals(transaction.getType())) {
                totalExpense += transaction.getAmount();
            }
        }

        // Получение данных для круговой диаграммы (название, сумма, тип)
        for (Transaction transaction : transactions) {
            // Создаем объект PieEntry для текущей транзакции
            float percent = 0;
            if ("income".equals(transaction.getType())) {
                percent = (float) (transaction.getAmount() / totalIncome);
            } else if ("expense".equals(transaction.getType())) {
                percent = (float) (transaction.getAmount() / totalExpense);
            }

            // Создаем подписи для каждой транзакции
            PieEntry entry = new PieEntry(percent, generateTransactionLabel(transaction));

            // Добавляем объект PieEntry в список entries
            entries.add(entry);
        }

        // Создание набора данных для круговой диаграммы
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.JOYFUL_COLORS); // Установка цветов

        // Создание объекта PieData и установка данных
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        // Настройка графика и отображение
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false); // Отключаем стандартные подписи
        pieChart.setDrawSlicesUnderHole(true); // Добавляем подписи на сегменты
        pieChart.setHoleRadius(40f); // Увеличиваем радиус внутренней части
        pieChart.setDrawCenterText(true); // Включаем центральный текст

        // Устанавливаем текст в центре круговой диаграммы
        pieChart.setCenterText(generateCenterText(totalIncome, totalExpense));

        // Настройка подписей сегментов
        pieChart.setEntryLabelTextSize(12f); // Устанавливаем размер подписей
        pieChart.setEntryLabelColor(Color.BLACK); // Устанавливаем цвет подписей

        Legend legend = pieChart.getLegend();
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);

        legend.setFormSize(12f);
        legend.setXEntrySpace(16f);
        legend.setYEntrySpace(8f);
        legend.setFormToTextSpace(5f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);





        // Устанавливаем тип анимации
        pieChart.animateY(1000, Easing.EaseInOutCubic);

        pieChart.invalidate(); // Обновляем диаграмму
    }

    // Генерация текста для отображения каждой транзакции в круговой диаграмме
    private String generateTransactionLabel(Transaction transaction) {
        return String.format(Locale.getDefault(), "%s - %s", transaction.getName(), transaction.getAmount());
    }

    // Генерация текста для центра круговой диаграммы
    private SpannableString generateCenterText(double totalIncome, double totalExpense) {
        SpannableString s = new SpannableString("Общий Доход:\n" + totalIncome + "\nОбщий Расход:\n" + totalExpense);
        s.setSpan(new StyleSpan(Typeface.BOLD), 0, s.length(), 0);
        s.setSpan(new RelativeSizeSpan(1.3f), 0, s.length(), 0);
        return s;
    }












    private void hideDataAndGraph() {
        findViewById(R.id.pieChart).setVisibility(View.GONE);
        dataTableLayout.setVisibility(View.GONE);
        findViewById(R.id.showDataChartButton).setVisibility(View.GONE);
    }









    // Метод для создания строки заголовка
    private TableRow createHeaderRow() {
        TableRow headerRow = (TableRow) getLayoutInflater().inflate(R.layout.table_row, null);
        ((TextView) headerRow.findViewById(R.id.column1)).setText("Дата");
        ((TextView) headerRow.findViewById(R.id.column2)).setText("Тип");
        ((TextView) headerRow.findViewById(R.id.column3)).setText("Сумма");
        ((TextView) headerRow.findViewById(R.id.column4)).setText("Название");
        return headerRow;
    }

    // Метод для создания строки данных
    private TableRow createDataRow(Transaction transaction) {
        TableRow dataRow = (TableRow) getLayoutInflater().inflate(R.layout.table_row, null);
        ((TextView) dataRow.findViewById(R.id.column1)).setText(transaction.getDate());
        ((TextView) dataRow.findViewById(R.id.column2)).setText(getTypeInRussian(transaction.getType()));
        ((TextView) dataRow.findViewById(R.id.column3)).setText(String.valueOf(transaction.getAmount()));
        ((TextView) dataRow.findViewById(R.id.column4)).setText(transaction.getName());

        // Добавление ImageView для иконки удаления
        ImageView deleteIcon = new ImageView(this);
        deleteIcon.setImageResource(android.R.drawable.ic_menu_delete);
        deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDeleteButtonClick(v, transaction);
            }
        });

        // Добавление иконки удаления в строку данных
        dataRow.addView(deleteIcon);

        return dataRow;
    }





    private void deleteTransactionFromDatabase(Transaction transaction) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        String whereClause = DatabaseHelper.KEY_ID + " = ?";
        String[] whereArgs = {String.valueOf(transaction.getId())};

        // Получаем тип транзакции (расход или доход)
        String type = transaction.getType();

        // Получаем сумму транзакции
        double amount = transaction.getAmount();

        // Удаление записи из базы данных
        db.delete(DatabaseHelper.TABLE_TRANSACTIONS, whereClause, whereArgs);
        db.close();
    }



    public void onDeleteButtonClick(View view, Transaction transaction) {
        // Получаем позицию элемента, который нужно удалить
        int position = dataTableLayout.indexOfChild((TableRow) view.getParent());

        if (position != -1) {
            // Создаем диалог подтверждения
            ConfirmationDialogFragment confirmationDialog = ConfirmationDialogFragment.newInstance("Вы действительно хотите удалить запись?");
            confirmationDialog.setOnConfirmationListener(new ConfirmationDialogFragment.OnConfirmationListener() {
                @Override
                public void onConfirm() {
                    // Пользователь подтвердил удаление, добавляем код удаления из базы данных
                    deleteTransactionFromDatabase(transaction);

                    showDataForSelectedDate(getSelectedDate());
                }

                @Override
                public void onCancel() {
                    // Пользователь отменил удаление
                }
            });

            // Показываем диалог
            FragmentManager fragmentManager = getSupportFragmentManager();
            confirmationDialog.show(fragmentManager, "ConfirmationDialog");
        }
    }



    // Получение данных для выбранной даты из базы данных
    private List<Transaction> getTransactionsFromDatabase(String selectedDate) {
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

    // Метод для получения типа на русском языке
    private String getTypeInRussian(String type) {
        if ("expense".equals(type)) {
            return "Расход";
        } else if ("income".equals(type)) {
            return "Доход";
        }
        return type;
    }

    // Метод для получения примерных данных
    private List<Transaction> getSampleData() {
        List<Transaction> sampleData = new ArrayList<>();
        sampleData.add(new Transaction("2024-01-01", "expense", 50.0, "Описание расхода"));
        sampleData.add(new Transaction("2024-01-02", "income", 100.0, "Описание дохода"));
        return sampleData;
    }

    @Override
    public void onConfirm() {

    }



    @Override
    public void onCancel() {

    }
}
