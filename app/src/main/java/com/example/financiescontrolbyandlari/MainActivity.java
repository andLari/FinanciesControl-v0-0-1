package com.example.financiescontrolbyandlari;// MainActivity.java

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void openInvestmentCalculator(View view) {
        Intent intent = new Intent(this, InvestmentCalculatorActivity.class);
        startActivity(intent);
    }

    public void openBudgetAnalysis(View view) {
        Intent intent = new Intent(this, BudgetAnalysisActivity.class);
        startActivity(intent);
    }
}
