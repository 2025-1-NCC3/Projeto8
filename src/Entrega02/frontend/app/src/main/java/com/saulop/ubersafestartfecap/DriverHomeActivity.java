package com.saulop.ubersafestartfecap;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;


import java.util.Calendar;

public class DriverHomeActivity extends AppCompatActivity {

    private TextView textViewGreeting;
    private TextView textViewDriverName;
    private TextView textViewStatus;
    private Button buttonStart;
    private LinearLayout navAccount;
    private LinearLayout navHome;
    private LinearLayout navEarnings;
    private LinearLayout navActivity;

    private boolean isOnline = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);

        initViews();
        setGreetingByTime();
        loadDriverData();
        setupNavigationListeners();
        setupStartButton();
    }

    private void initViews() {
        textViewGreeting = findViewById(R.id.textViewGreeting);
        textViewDriverName = findViewById(R.id.textViewDriverName);
        textViewStatus = findViewById(R.id.textViewStatus);
        buttonStart = findViewById(R.id.buttonStart);

        navAccount = findViewById(R.id.navAccount);
        navHome = findViewById(R.id.navHome);
        navEarnings = findViewById(R.id.navEarnings);
        navActivity = findViewById(R.id.navActivity);
    }

    private void setGreetingByTime() {
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (hourOfDay >= 5 && hourOfDay < 12) {
            greeting = "Bom dia,";
        } else if (hourOfDay >= 12 && hourOfDay < 18) {
            greeting = "Boa tarde,";
        } else {
            greeting = "Boa noite,";
        }

        textViewGreeting.setText(greeting);
    }

    private void loadDriverData() {
        SharedPreferences prefs = getSharedPreferences("userPrefs", MODE_PRIVATE);
        String fullName = prefs.getString("username", "Motorista");

        String firstName = fullName;
        if (fullName.contains(" ")) {
            firstName = fullName.substring(0, fullName.indexOf(" "));
        }

        textViewDriverName.setText(firstName);
    }

    private void setupNavigationListeners() {
        navAccount.setOnClickListener(v -> {
            Intent intent = new Intent(DriverHomeActivity.this, ProfileActivity.class);
            intent.putExtra("USER_TYPE", "driver");
            intent.putExtra("USER_NAME", textViewDriverName.getText().toString());
            startActivity(intent);
        });

        navHome.setOnClickListener(v -> {
            Toast.makeText(DriverHomeActivity.this, "Você já está na página inicial", Toast.LENGTH_SHORT).show();
        });

        navEarnings.setOnClickListener(v -> {
            Toast.makeText(DriverHomeActivity.this, "Ganhos em breve", Toast.LENGTH_SHORT).show();
        });

        navActivity.setOnClickListener(v -> {
            Toast.makeText(DriverHomeActivity.this, "Atividade em breve", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupStartButton() {
        buttonStart.setOnClickListener(v -> {
            if (!isOnline) {
                isOnline = true;
                textViewStatus.setText("Procurando passageiros...");
                buttonStart.setText("PARAR");
                showSearchingPassengerDialog();
            } else {
                isOnline = false;
                textViewStatus.setText("Você está offline");
                buttonStart.setText("INICIAR");
            }
        });
    }

    private void showSearchingPassengerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DriverHomeActivity.this);
        View loadingView = LayoutInflater.from(this).inflate(R.layout.dialog_ride_loading, null);
        builder.setView(loadingView);
        builder.setCancelable(false);

        final AlertDialog loadingDialog = builder.create();
        if (loadingDialog.getWindow() != null) {
            loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        loadingDialog.show();

        ProgressBar progressBar = loadingView.findViewById(R.id.progressBar);
        TextView statusText = loadingView.findViewById(R.id.textViewStatus);
        ImageView checkmarkImage = loadingView.findViewById(R.id.imageViewCheckmark);

        statusText.setText("Procurando passageiros próximos...");

        new Handler().postDelayed(() -> {
            statusText.setText("Passageiro encontrado!");
            progressBar.setVisibility(View.GONE);
            checkmarkImage.setVisibility(View.VISIBLE);

            new Handler().postDelayed(() -> {
                loadingDialog.dismiss();
                showPassengerInfoDialog();
            }, 1000);
        }, 5000);
    }

    private void showPassengerInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DriverHomeActivity.this);
        View passengerInfoView = LayoutInflater.from(this).inflate(R.layout.dialog_passenger_info, null);
        builder.setView(passengerInfoView);
        builder.setCancelable(true);

        TextView tvPassengerName = passengerInfoView.findViewById(R.id.textViewPassengerName);
        TextView tvPickupLocation = passengerInfoView.findViewById(R.id.textViewPickupLocation);
        TextView tvDestination = passengerInfoView.findViewById(R.id.textViewDestination);
        TextView tvPrice = passengerInfoView.findViewById(R.id.textViewPrice);
        RatingBar ratingBar = passengerInfoView.findViewById(R.id.ratingBarPassenger);
        RatingBar safeScoreBar = passengerInfoView.findViewById(R.id.ratingBarSafeScore);
        Button btnAcceptRide = passengerInfoView.findViewById(R.id.buttonAcceptRide);

        String passengerName = "Maria Santos";
        String pickupLocation = "Avenida Paulista, 1000";
        String destination = "Shopping Center Norte";
        String ridePrice = "R$ 23,50";

        tvPassengerName.setText(passengerName);
        tvPickupLocation.setText(pickupLocation);
        tvDestination.setText("Destino: " + destination);
        tvPrice.setText(ridePrice);
        ratingBar.setRating(4.8f);
        safeScoreBar.setRating(4.9f);

        final AlertDialog passengerDialog = builder.create();
        if (passengerDialog.getWindow() != null) {
            passengerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        passengerDialog.show();

        btnAcceptRide.setOnClickListener(v -> {
            passengerDialog.dismiss();
            navigateToSafetyChecklist(passengerName, pickupLocation, destination, ridePrice);
        });
    }

    private void navigateToSafetyChecklist(String passengerName, String pickupLocation, String destination, String ridePrice) {
        Intent intent = new Intent(DriverHomeActivity.this, DriverSafetyChecklistActivity.class);
        intent.putExtra("PASSENGER_NAME", passengerName);
        intent.putExtra("PICKUP_LOCATION", pickupLocation);
        intent.putExtra("DESTINATION", destination);
        intent.putExtra("RIDE_PRICE", ridePrice);
        startActivity(intent);

        Toast.makeText(DriverHomeActivity.this, "Corrida aceita! Verifique os itens de segurança.", Toast.LENGTH_LONG).show();
    }
}