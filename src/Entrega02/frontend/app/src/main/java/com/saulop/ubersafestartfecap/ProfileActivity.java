package com.saulop.ubersafestartfecap;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView textViewName;
    private TextView textViewRating;
    private TextView textViewEmail;
    private TextView textViewPhone;
    private TextView textViewAccountType;
    private TextView textViewSafeScore;
    private ProgressBar progressBarSafeScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();

        loadUserData();
    }

    private void initViews() {
        textViewName = findViewById(R.id.textViewName);
        textViewRating = findViewById(R.id.textViewRating);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewPhone = findViewById(R.id.textViewPhone);
        textViewAccountType = findViewById(R.id.textViewAccountType);
        textViewSafeScore = findViewById(R.id.textViewSafeScore);
        progressBarSafeScore = findViewById(R.id.progressBarSafeScore);
    }

    private void loadUserData() {
        String userName = "Lucas Andrade";
        String rating = "4.8";
        String email = "lucas.andrade@email.com";
        String phone = "+55 21 91234-5678";

        String accountType = getResources().getString(R.string.passenger); // ou driver
        int safeScore = 95;

        textViewName.setText(userName);
        textViewRating.setText(rating);
        textViewEmail.setText(email);
        textViewPhone.setText(phone);
        textViewAccountType.setText(accountType);

        progressBarSafeScore.setProgress(safeScore);
    }
}