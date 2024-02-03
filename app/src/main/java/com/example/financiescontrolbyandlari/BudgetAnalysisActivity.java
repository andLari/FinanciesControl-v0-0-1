package com.example.financiescontrolbyandlari;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BudgetAnalysisActivity extends AppCompatActivity {

    private TextView initialBalanceLabel;
    private EditText initialBalanceEditText;
    private Button editButton;

    private DatabaseHelper databaseHelper;  // Объявление databaseHelper

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_analysis);

        initialBalanceLabel = findViewById(R.id.initialBalanceLabel);
        initialBalanceEditText = findViewById(R.id.initialBalanceEditText);
        editButton = findViewById(R.id.editButton);

        // Скрываем поле ввода и кнопку сохранения при создании активити
        initialBalanceEditText.setVisibility(View.GONE);

        // Инициализация базы данных
        databaseHelper = new DatabaseHelper(this);

        double savedInitialBalance = getIntent().getDoubleExtra("newBalance", 0);
        if (savedInitialBalance == 0) {
            // Если нового баланса нет, получим баланс из базы данных
            savedInitialBalance = databaseHelper.getInitialBalance();
        }
        setInitialBalance(savedInitialBalance);
    }

    public void toggleEditSection(View view) {
        // Отключаем кнопку, чтобы предотвратить повторные нажатия
        editButton.setEnabled(false);

        // Переключение видимости поля ввода и кнопки сохранения
        if (initialBalanceEditText.getVisibility() == View.VISIBLE) {
            // Если поле ввода и кнопка сохранения видны, скрываем их
            initialBalanceEditText.setVisibility(View.GONE);

            try {
                // Обновляем начальный счет в базе данных
                double newInitialBalance = Double.parseDouble(initialBalanceEditText.getText().toString());
                databaseHelper.updateInitialBalance(newInitialBalance);

                // Сохраняем начальный счет в SharedPreferences
                databaseHelper.saveInitialBalance(newInitialBalance);

                // Обновляем анализ и возвращаемся в BudgetAnalysisActivity
                updateAndReturnToBudgetAnalysisActivity(newInitialBalance);
            } catch (NumberFormatException e) {
                showToast("Ошибка: введите корректное число");
            }
        } else {
            // Если скрыты, показываем
            initialBalanceEditText.setVisibility(View.VISIBLE);
        }

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);

        // Включаем кнопку после выполнения операций
        editButton.setEnabled(true);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Проверяем, что результат пришел от TransactionsActivity и операция была успешной
        if (requestCode == 529412315 && resultCode == RESULT_OK) {
            // Извлекаем новый баланс из Intent
            double newBalance = data.getDoubleExtra("newBalance", 0);
            // Обновляем отображение счета
            setInitialBalance(newBalance);
        }
    }


    private void updateAnalysis() {
        // Метод для обновления текста анализа
        // Вместо этого метода добавьте вашу логику анализа бюджета
        String initialBalanceText = initialBalanceEditText.getText().toString();
        if (!initialBalanceText.isEmpty()) {
            try {
                double initialBalance = Double.parseDouble(initialBalanceText);
                setInitialBalance(initialBalance);
            } catch (NumberFormatException e) {
                // Если введено не число, показываем сообщение об ошибке
                showToast("Ошибка: введите корректное число");
            }
        } else {
            // Если поле ввода не заполнено, выводим сообщение об ошибке
            showToast("Ошибка: введите начальный счет");
        }
    }

    // Метод для установки начального счета
    private void setInitialBalance(double balance) {
        initialBalanceLabel.setText("Ваш счет: " + balance + " ₽");
        initialBalanceEditText.setText(String.valueOf(balance));
    }




    // Ваш код в BudgetAnalysisActivity.java
    public void openTransactionsActivity(View view) {
        Intent intent = new Intent(this, TransactionsActivity.class);
        startActivityForResult(intent, 529412315);
    }

    public void openDataActivity(View view) {
        Intent intent = new Intent(this, ShowDataActivity.class);
        startActivity(intent);
    }

    private void updateAndReturnToBudgetAnalysisActivity(double newBalance) {
        // Обновляем анализ в BudgetAnalysisActivity
        setInitialBalance(newBalance);

        // Создаем Intent для возврата в BudgetAnalysisActivity
        Intent returnIntent = new Intent();
        // Помещаем новый баланс в Intent
        returnIntent.putExtra("newBalance", newBalance);
        // Устанавливаем результат выполнения, указывая, что операция прошла успешно
        setResult(RESULT_OK, returnIntent);

    }


    private void showToast(String message) {
        // Метод для вывода сообщений в Toast
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
