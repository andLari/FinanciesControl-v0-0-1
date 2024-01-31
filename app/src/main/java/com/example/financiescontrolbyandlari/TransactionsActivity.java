package com.example.financiescontrolbyandlari;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class TransactionsActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText expenseDateEditText;
    private TextView selectedDateTextView;
    private EditText expenseAmountEditText;
    private EditText expenseNameEditText;
    private Button addExpenseNameButton;

    private EditText incomeDateEditText;
    private EditText incomeAmountEditText;
    private EditText incomeNameEditText;
    private Button addIncomeNameButton;

    private boolean isEditingExpense = false;


    private DatabaseHelper databaseHelper;

    // Поле для сохранения ссылки на текущий dateEditText
    private EditText currentDatePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        // Для расходов
        expenseDateEditText = findViewById(R.id.expenseDateEditText);
        expenseAmountEditText = findViewById(R.id.expenseAmountEditText);
        expenseNameEditText = findViewById(R.id.expenseNameEditText);
        addExpenseNameButton = findViewById(R.id.addExpenseNameButton);

        // Для доходов
        incomeDateEditText = findViewById(R.id.incomeDateEditText);
        incomeAmountEditText = findViewById(R.id.incomeAmountEditText);
        incomeNameEditText = findViewById(R.id.incomeNameEditText);
        addIncomeNameButton = findViewById(R.id.addIncomeNameButton);

        // Инициализация selectedDateTextView
        selectedDateTextView = findViewById(R.id.selectedDateTextView);
        selectedDateTextView.setOnClickListener(this);



        // Изменения для incomeDateEditText
        incomeDateEditText.setOnClickListener(this);


        // Добавляем кнопке "Редактировать расход" обработчик onClick
        Button editExpenseButton = findViewById(R.id.editExpenseButton);
        editExpenseButton.setOnClickListener(this);


        // Инициализация базы данных
        databaseHelper = new DatabaseHelper(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.selectedDateTextView) {
            showDatePicker(v);
        } else if (v.getId() == R.id.incomeDateEditText) {
            showDatePicker(v);
        } else if (v.getId() == R.id.editExpenseButton) {
            isEditingExpense = true; // Устанавливаем флаг редактирования расхода
            showExpenseFields(v);
        }
    }


    public void showDatePicker(View view) {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // Обработка выбранной даты
                String selectedDate = String.format("%02d/%02d/%d", day, month + 1, year);
                selectedDateTextView.setText(selectedDate);

                // Проверяем, какое поле редактируется
                if (currentDatePicker != null) {
                    // Если у нас есть текущий dateEditText, устанавливаем дату
                    currentDatePicker.setText(selectedDate);
                    currentDatePicker = null; // Обнуляем после использования
                } else if (isEditingExpense && expenseDateEditText != null) {
                    // Если редактируется расход, устанавливаем дату в expenseDateEditText
                    expenseDateEditText.setText(selectedDate);
                    isEditingExpense = false; // Сбрасываем флаг редактирования расхода
                } else if (incomeDateEditText != null) {
                    // Если не редактируется расход, редактируется доход
                    incomeDateEditText.setText(selectedDate);
                }
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






    // Показать поля для расходов
    public void showExpenseFields(View view) {
        toggleVisibility(expenseDateEditText, expenseAmountEditText, expenseNameEditText, addExpenseNameButton);

        // Сохраняем ссылку на expenseDateEditText, чтобы использовать ее в showDatePicker
        if (expenseDateEditText != null) {
            currentDatePicker = expenseDateEditText;
        }
    }

    // Показать поля для доходов
    public void showIncomeFields(View view) {
        toggleVisibility(incomeDateEditText, incomeAmountEditText, incomeNameEditText, addIncomeNameButton);

        // Сохраняем ссылку на incomeDateEditText, чтобы использовать ее в showDatePicker
        if (incomeDateEditText != null) {
            currentDatePicker = incomeDateEditText;
        }
    }

    public void addExpense(View view) {
        // Проверка на заполнение даты и суммы
        String date = expenseDateEditText.getText().toString();
        String amountText = expenseAmountEditText.getText().toString();

        if (date.isEmpty() || amountText.isEmpty()) {
            showToast("Ошибка: введите дату и сумму для добавления расхода");
        } else {
            try {
                // Попытка преобразовать введенный текст в число
                double amount = Double.parseDouble(amountText);

                // Создание объекта Transaction
                Transaction expenseTransaction = new Transaction();
                expenseTransaction.setDate(date);
                expenseTransaction.setType("expense");
                expenseTransaction.setAmount(amount);
                expenseTransaction.setName(expenseNameEditText.getText().toString());

                // Сохранение транзакции в базе данных
                long transactionId = databaseHelper.saveTransaction(expenseTransaction);

                if (transactionId != -1) {
                    showToast("Расход добавлен");
                    // Очистим поля после добавления
                    clearFields(expenseDateEditText, expenseAmountEditText, expenseNameEditText);
                    // Скроем поля
                    toggleVisibility(expenseDateEditText, expenseAmountEditText, expenseNameEditText, addExpenseNameButton);
                } else {
                    showToast("Ошибка при добавлении расхода в базу данных");
                }
            } catch (NumberFormatException e) {
                showToast("Ошибка: введите корректное значение для суммы");
            }
        }
    }

    public void addIncome(View view) {
        // Проверка на заполнение даты и суммы
        String date = incomeDateEditText.getText().toString();
        String amountText = incomeAmountEditText.getText().toString();

        if (date.isEmpty() || amountText.isEmpty()) {
            showToast("Ошибка: введите дату и сумму для добавления дохода");
        } else {
            try {
                // Попытка преобразовать введенный текст в число
                double amount = Double.parseDouble(amountText);

                // Создание объекта Transaction
                Transaction incomeTransaction = new Transaction();
                incomeTransaction.setDate(date);
                incomeTransaction.setType("income");
                incomeTransaction.setAmount(amount);
                incomeTransaction.setName(incomeNameEditText.getText().toString());

                // Сохранение транзакции в базе данных
                long transactionId = databaseHelper.saveTransaction(incomeTransaction);

                if (transactionId != -1) {
                    showToast("Доход добавлен");
                    // Очистим поля после добавления
                    clearFields(incomeDateEditText, incomeAmountEditText, incomeNameEditText);
                    // Скроем поля
                    toggleVisibility(incomeDateEditText, incomeAmountEditText, incomeNameEditText, addIncomeNameButton);
                } else {
                    showToast("Ошибка при добавлении дохода в базу данных");
                }
            } catch (NumberFormatException e) {
                showToast("Ошибка: введите корректное значение для суммы");
            }
        }
    }

    private void toggleVisibility(View... views) {
        for (View view : views) {
            int visibility = view.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
            view.setVisibility(visibility);
        }
    }

    private void clearFields(EditText... editTexts) {
        for (EditText editText : editTexts) {
            editText.getText().clear();
        }
    }

    private void showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
