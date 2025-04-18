package com.saulop.ubersafestartfecap;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MainActivity extends AppCompatActivity {

    private CardView cardIdentity, cardSeatbelt, cardRoute, cardVehicle;
    private ImageView checkIdentity, checkSeatbelt, checkRoute, checkVehicle;
    private Button btnStartRide;

    private boolean isIdentityChecked = false;
    private boolean isSeatbeltChecked = false;
    private boolean isRouteChecked = false;
    private boolean isVehicleChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        cardIdentity = findViewById(R.id.cardIdentity);
        cardSeatbelt = findViewById(R.id.cardSeatbelt);
        cardRoute = findViewById(R.id.cardRoute);
        cardVehicle = findViewById(R.id.cardVehicle);

        checkIdentity = findViewById(R.id.checkIdentity);
        checkSeatbelt = findViewById(R.id.checkSeatbelt);
        checkRoute = findViewById(R.id.checkRoute);
        checkVehicle = findViewById(R.id.checkVehicle);

        btnStartRide = findViewById(R.id.btnStartRide);

        updateUI();
    }

    private void setupClickListeners() {
        cardIdentity.setOnClickListener(v -> {
            isIdentityChecked = !isIdentityChecked;
            updateUI();
        });

        cardSeatbelt.setOnClickListener(v -> {
            isSeatbeltChecked = !isSeatbeltChecked;
            updateUI();
        });

        cardRoute.setOnClickListener(v -> {
            isRouteChecked = !isRouteChecked;
            updateUI();
        });

        cardVehicle.setOnClickListener(v -> {
            isVehicleChecked = !isVehicleChecked;
            updateUI();
        });

        btnStartRide.setOnClickListener(v -> {
            if (allChecksCompleted()) {
                Toast.makeText(MainActivity.this, "Iniciando corrida...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        updateCardUI(cardIdentity, checkIdentity, isIdentityChecked);
        updateCardUI(cardSeatbelt, checkSeatbelt, isSeatbeltChecked);
        updateCardUI(cardRoute, checkRoute, isRouteChecked);
        updateCardUI(cardVehicle, checkVehicle, isVehicleChecked);

        boolean allCompleted = allChecksCompleted();
        btnStartRide.setEnabled(allCompleted);
        btnStartRide.setBackgroundResource(allCompleted ? R.drawable.button_enabled : R.drawable.button_disabled);
    }

    private void updateCardUI(CardView card, ImageView checkmark, boolean isChecked) {
        if (isChecked) {
            card.setCardBackgroundColor(Color.parseColor("#004D21")); // Dark Green
            checkmark.setImageTintList(ColorStateList.valueOf(Color.parseColor("#00C853"))); // Bright Green
        } else {
            card.setCardBackgroundColor(Color.parseColor("#262626")); // Dark Gray
            checkmark.setImageTintList(ColorStateList.valueOf(Color.parseColor("#777777"))); // Light Gray
        }
    }

    private boolean allChecksCompleted() {
        return isIdentityChecked && isSeatbeltChecked && isRouteChecked && isVehicleChecked;
    }
}