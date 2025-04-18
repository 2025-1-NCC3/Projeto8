package com.saulop.ubersafestartfecap;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private TextView textViewLocationName1;
    private TextView textViewLocationAddress1;
    private TextView textViewLocationName2;
    private TextView textViewLocationAddress2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();

        loadLocationHistoryData();
    }

    private void initViews() {
        textViewLocationName1 = findViewById(R.id.textViewLocationName1);
        textViewLocationAddress1 = findViewById(R.id.textViewLocationAddress1);
        textViewLocationName2 = findViewById(R.id.textViewLocationName2);
        textViewLocationAddress2 = findViewById(R.id.textViewLocationAddress2);
    }

    private void loadLocationHistoryData() {
        String location1Name = "Avenida das Rosas";
        String location1Address = "Av. das Rosas - Jardim Primavera";
        String location2Name = "Rua Bento Silveira, 1234 - Nova Esperança";
        String location2Address = "Nova Esperança - MG, 35700-000";

        textViewLocationName1.setText(location1Name);
        textViewLocationAddress1.setText(location1Address);
        textViewLocationName2.setText(location2Name);
        textViewLocationAddress2.setText(location2Address);
    }
}