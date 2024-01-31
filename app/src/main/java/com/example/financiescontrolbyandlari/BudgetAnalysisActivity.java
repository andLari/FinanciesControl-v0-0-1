package com.example.financiescontrolbyandlari;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class BudgetAnalysisActivity extends AppCompatActivity {

    private TextView initialBalanceLabel;
    private EditText initialBalanceEditText;
    private Button editButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_analysis);

        initialBalanceLabel = findViewById(R.id.initialBalanceLabel);
        initialBalanceEditText = findViewById(R.id.initialBalanceEditText);
        editButton = findViewById(R.id.editButton);

        // Скрываем поле ввода и кнопку сохранения при создании активити
        initialBalanceEditText.setVisibility(View.GONE);
        // Устанавливаем начальный счет в 0 рублей при открытии
        setInitialBalance(0.0);
    }

    public void toggleEditSection(View view) {
        // Отключаем кнопку, чтобы предотвратить повторные нажатия
        editButton.setEnabled(false);

        // Переключение видимости поля ввода и кнопки сохранения
        if (initialBalanceEditText.getVisibility() == View.VISIBLE) {
            // Если поле ввода и кнопка сохранения видны, скрываем их
            initialBalanceEditText.setVisibility(View.GONE);
            // Обновляем анализ при скрытии
            updateAnalysis();
        } else {
            // Если скрыты, показываем
            initialBalanceEditText.setVisibility(View.VISIBLE);
        }

        // Включаем кнопку после выполнения операций
        editButton.setEnabled(true);
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
        startActivity(intent);
    }

    public void openDataActivity(View view) {
        Intent intent = new Intent(this, ShowDataActivity.class);
        startActivity(intent);
    }

    private void showToast(String message) {
        // Метод для вывода сообщений в Toast
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
