package com.example.financiescontrolbyandlari;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.Currency;


public class InvestmentCalculatorActivity extends AppCompatActivity {

    private EditText amountEditText, interestRateEditText, termEditText;
    private Spinner termUnitSpinner;
    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_investment_calculator);

        amountEditText = findViewById(R.id.amountEditText);
        interestRateEditText = findViewById(R.id.interestRateEditText);
        termEditText = findViewById(R.id.termEditText);
        termUnitSpinner = findViewById(R.id.termUnitSpinner);
        resultTextView = findViewById(R.id.resultTextView);
    }

    public void calculateInvestment(View view) {
        try {
            // Получаем введенные значения
            double amount = Double.parseDouble(amountEditText.getText().toString());
            double interestRate = Double.parseDouble(interestRateEditText.getText().toString());
            int term = Integer.parseInt(termEditText.getText().toString());
            String termUnit = termUnitSpinner.getSelectedItem().toString();

            // Рассчитываем прибыль
            double profit = calculateProfit(amount, interestRate, term, termUnit);

            // Форматируем сумму в рублях
            String formattedAmount = formatAmountInRubles(amount);

            // Выводим результат в TextView
            String resultText = String.format("Внесенная сумма: %s\nПроцентная ставка: %.2f%%\nПрибыль: %.2f ₽", formattedAmount, interestRate, profit);
            resultTextView.setText(resultText);
        } catch (NumberFormatException e) {
            // Выводим сообщение об ошибке с использованием Toast
            Toast.makeText(this, "Введите корректные числовые значения", Toast.LENGTH_SHORT).show();
        }
    }

    private double calculateProfit(double amount, double interestRate, int term, String termUnit) {
        double rate = interestRate / 100;

        if (termUnit.equals("Годы")) {
            return amount * Math.pow(1 + rate, term) - amount;
        } else {
            return amount * Math.pow(1 + rate / 12, term) - amount;
        }
    }

    private String formatAmountInRubles(double amount) {
        // Форматируем сумму в рублях с помощью NumberFormat
        NumberFormat rubleFormat = NumberFormat.getCurrencyInstance();
        rubleFormat.setCurrency(Currency.getInstance("RUB"));

        // Получаем форматированную строку и убираем символ валюты
        String formattedAmount = rubleFormat.format(amount);

        // Извлекаем число и убираем лишние нули после десятичной точки
        formattedAmount = formattedAmount.replaceAll("[^\\d.,]", "");
        formattedAmount = formattedAmount.replace(",", "."); // Заменяем запятую на точку для парсинга

        // Парсим строку в число и возвращаем как строку
        double parsedAmount = Double.parseDouble(formattedAmount);
        return String.format("%.2f \u20BD", parsedAmount); // Отформатированное число с двумя знаками после запятой и символом рубля
    }


}
