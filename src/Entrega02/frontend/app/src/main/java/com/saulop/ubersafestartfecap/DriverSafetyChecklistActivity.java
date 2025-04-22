package com.saulop.ubersafestartfecap;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.saulop.ubersafestartfecap.utils.SafeScoreHelper;

public class DriverSafetyChecklistActivity extends AppCompatActivity {

    private CardView cardVehicleCondition, cardLicenseInsurance, cardRespectCode, cardRecording;
    private ImageView checkVehicleCondition, checkLicenseInsurance, checkRespectCode, checkRecording;
    private Button btnStartRide, btnSkipChecklist;
    private TextView textViewPassengerDestination, textViewPassengerInfo;
    private ImageView menuButton, notificationButton, profileButton;

    private boolean isVehicleConditionChecked = false;
    private boolean isLicenseInsuranceChecked = false;
    private boolean isRespectCodeChecked = false;
    private boolean isRecordingChecked = false;

    private String passengerName;
    private String pickupLocation;
    private String destination;
    private String ridePrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_safety_checklist);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            passengerName = extras.getString("PASSENGER_NAME", "");
            pickupLocation = extras.getString("PICKUP_LOCATION", "");
            destination = extras.getString("DESTINATION", "");
            ridePrice = extras.getString("RIDE_PRICE", "");
        }

        initViews();
        setupClickListeners();
        displayRideInfo();
    }

    private void initViews() {
        cardVehicleCondition = findViewById(R.id.cardVehicleCondition);
        cardLicenseInsurance = findViewById(R.id.cardLicenseInsurance);
        cardRespectCode = findViewById(R.id.cardRespectCode);
        cardRecording = findViewById(R.id.cardRecording);

        checkVehicleCondition = findViewById(R.id.checkVehicleCondition);
        checkLicenseInsurance = findViewById(R.id.checkLicenseInsurance);
        checkRespectCode = findViewById(R.id.checkRespectCode);
        checkRecording = findViewById(R.id.checkRecording);

        btnStartRide = findViewById(R.id.btnStartRide);
        btnSkipChecklist = findViewById(R.id.btnSkipChecklist);
        textViewPassengerDestination = findViewById(R.id.textViewPassengerDestination);
        textViewPassengerInfo = findViewById(R.id.textViewPassengerInfo);

        menuButton = findViewById(R.id.menuButton);
        notificationButton = findViewById(R.id.notificationButton);
        profileButton = findViewById(R.id.profileButton);

        updateUI();
    }

    private void displayRideInfo() {
        textViewPassengerDestination.setText("Destino: " + destination);
        textViewPassengerInfo.setText("Passageiro: " + passengerName + " | Embarque: " + pickupLocation);
    }

    private void setupClickListeners() {
        menuButton.setOnClickListener(v -> {
            Toast.makeText(DriverSafetyChecklistActivity.this, "Menu", Toast.LENGTH_SHORT).show();
        });

        notificationButton.setOnClickListener(v -> {
            Toast.makeText(DriverSafetyChecklistActivity.this, "Notificações", Toast.LENGTH_SHORT).show();
        });

        profileButton.setOnClickListener(v -> {
            Toast.makeText(DriverSafetyChecklistActivity.this, "Perfil", Toast.LENGTH_SHORT).show();
        });

        cardVehicleCondition.setOnClickListener(v -> {
            isVehicleConditionChecked = !isVehicleConditionChecked;
            updateUI();
        });

        cardLicenseInsurance.setOnClickListener(v -> {
            isLicenseInsuranceChecked = !isLicenseInsuranceChecked;
            updateUI();
        });

        cardRespectCode.setOnClickListener(v -> {
            isRespectCodeChecked = !isRespectCodeChecked;
            updateUI();
        });

        cardRecording.setOnClickListener(v -> {
            isRecordingChecked = !isRecordingChecked;
            updateUI();
        });

        btnStartRide.setOnClickListener(v -> {
            if (allChecksCompleted()) {
                // Atualizar o SafeScore quando todos os checks estão completos
                SafeScoreHelper.updateSafeScore(DriverSafetyChecklistActivity.this, 5);

                Toast.makeText(DriverSafetyChecklistActivity.this, "Viagem iniciada com segurança!", Toast.LENGTH_SHORT).show();

                // Iniciar a atividade RideInProgressActivity com o modo motorista
                Intent intent = new Intent(DriverSafetyChecklistActivity.this, RideInProgressActivity.class);
                intent.putExtra("IS_DRIVER_MODE", true);
                intent.putExtra("PASSENGER_NAME", passengerName);
                intent.putExtra("DESTINATION", destination);
                intent.putExtra("RIDE_PRICE", ridePrice);
                startActivity(intent);
            } else {
                Toast.makeText(DriverSafetyChecklistActivity.this, "Complete todos os itens de segurança primeiro", Toast.LENGTH_SHORT).show();
            }
        });

        // Novo botão para ignorar o checklist com penalidade no SafeScore
        btnSkipChecklist.setOnClickListener(v -> {
            // Reduzir o SafeScore em 5 pontos
            SafeScoreHelper.updateSafeScore(DriverSafetyChecklistActivity.this, -5);

            Toast.makeText(DriverSafetyChecklistActivity.this, "Checklist ignorado! -5 pontos de SafeScore.", Toast.LENGTH_SHORT).show();

            // Mesmo assim, iniciar a corrida
            Intent intent = new Intent(DriverSafetyChecklistActivity.this, RideInProgressActivity.class);
            intent.putExtra("IS_DRIVER_MODE", true);
            intent.putExtra("PASSENGER_NAME", passengerName);
            intent.putExtra("DESTINATION", destination);
            intent.putExtra("RIDE_PRICE", ridePrice);
            startActivity(intent);
        });
    }

    private void updateUI() {
        updateCardUI(cardVehicleCondition, checkVehicleCondition, isVehicleConditionChecked);
        updateCardUI(cardLicenseInsurance, checkLicenseInsurance, isLicenseInsuranceChecked);
        updateCardUI(cardRespectCode, checkRespectCode, isRespectCodeChecked);
        updateCardUI(cardRecording, checkRecording, isRecordingChecked);

        boolean allCompleted = allChecksCompleted();
        btnStartRide.setEnabled(allCompleted);
        btnStartRide.setBackgroundResource(allCompleted ? R.drawable.button_enabled : R.drawable.button_disabled);
    }

    private void updateCardUI(CardView card, ImageView checkmark, boolean isChecked) {
        if (isChecked) {
            card.setCardBackgroundColor(Color.parseColor("#004D21"));
            checkmark.setImageTintList(ColorStateList.valueOf(Color.parseColor("#00C853")));
        } else {
            card.setCardBackgroundColor(Color.parseColor("#262626"));
            checkmark.setImageTintList(ColorStateList.valueOf(Color.parseColor("#777777")));
        }
    }

    private boolean allChecksCompleted() {
        return isVehicleConditionChecked && isLicenseInsuranceChecked && isRespectCodeChecked && isRecordingChecked;
    }
}