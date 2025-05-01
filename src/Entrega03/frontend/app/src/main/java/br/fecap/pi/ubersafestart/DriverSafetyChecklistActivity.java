package br.fecap.pi.ubersafestart;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import br.fecap.pi.ubersafestart.utils.SafeScoreHelper;
import nl.dionsegijn.konfetti.KonfettiView;
import br.fecap.pi.ubersafestart.utils.ConfettiManager;

public class DriverSafetyChecklistActivity extends AppCompatActivity {

    private static final String TAG = "DriverChecklist";

    private CardView cardVehicleCondition, cardLicenseInsurance, cardRespectCode, cardRecording;
    private ImageView checkVehicleCondition, checkLicenseInsurance, checkRespectCode, checkRecording;
    private Button btnStartRide, btnSkipChecklist;
    private TextView textViewPassengerDestination, textViewPassengerInfo;
    private MaterialToolbar toolbar;
    private KonfettiView konfettiViewDriver;

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
        toolbar = findViewById(R.id.toolbarDriverChecklist);

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

        konfettiViewDriver = findViewById(R.id.konfettiViewDriver);

        updateUI();
    }

    private void displayRideInfo() {
        if (textViewPassengerDestination != null) {
            textViewPassengerDestination.setText("Destino: " + destination);
        }
        if (textViewPassengerInfo != null) {
            textViewPassengerInfo.setText("Passageiro: " + passengerName + " | Embarque: " + pickupLocation);
        }
    }

    private void setupClickListeners() {
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            });
        } else {
            Log.e(TAG, "Toolbar não encontrada no layout.");
        }

        if (cardVehicleCondition != null) {
            cardVehicleCondition.setOnClickListener(v -> {
                isVehicleConditionChecked = !isVehicleConditionChecked;
                updateUI();

                // Mostrar efeito de confete se o item foi marcado
                if (isVehicleConditionChecked && konfettiViewDriver != null) {
                    ConfettiManager.showConfetti(konfettiViewDriver);
                }
            });
        } else {
            Log.e(TAG, "CardView cardVehicleCondition não encontrado.");
        }

        if (cardLicenseInsurance != null) {
            cardLicenseInsurance.setOnClickListener(v -> {
                isLicenseInsuranceChecked = !isLicenseInsuranceChecked;
                updateUI();

                // Mostrar efeito de confete se o item foi marcado
                if (isLicenseInsuranceChecked && konfettiViewDriver != null) {
                    ConfettiManager.showConfetti(konfettiViewDriver);
                }
            });
        } else {
            Log.e(TAG, "CardView cardLicenseInsurance não encontrado.");
        }

        if (cardRespectCode != null) {
            cardRespectCode.setOnClickListener(v -> {
                isRespectCodeChecked = !isRespectCodeChecked;
                updateUI();

                // Mostrar efeito de confete se o item foi marcado
                if (isRespectCodeChecked && konfettiViewDriver != null) {
                    ConfettiManager.showConfetti(konfettiViewDriver);
                }
            });
        } else {
            Log.e(TAG, "CardView cardRespectCode não encontrado.");
        }

        if (cardRecording != null) {
            cardRecording.setOnClickListener(v -> {
                isRecordingChecked = !isRecordingChecked;
                updateUI();

                // Mostrar efeito de confete se o item foi marcado
                if (isRecordingChecked && konfettiViewDriver != null) {
                    ConfettiManager.showConfetti(konfettiViewDriver);
                }
            });
        } else {
            Log.e(TAG, "CardView cardRecording não encontrado.");
        }

        if (btnStartRide != null) {
            btnStartRide.setOnClickListener(v -> {
                if (allChecksCompleted()) {
                    SafeScoreHelper.updateSafeScore(DriverSafetyChecklistActivity.this, 5);
                    Toast.makeText(DriverSafetyChecklistActivity.this, "Viagem iniciada com segurança!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(DriverSafetyChecklistActivity.this, RideInProgressActivity.class);
                    intent.putExtra("IS_DRIVER_MODE", true);
                    intent.putExtra("PASSENGER_NAME", passengerName);
                    intent.putExtra("DESTINATION", destination);
                    intent.putExtra("RIDE_PRICE", ridePrice);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                } else {
                    Toast.makeText(DriverSafetyChecklistActivity.this, "Complete todos os itens de segurança primeiro", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e(TAG, "Button btnStartRide não encontrado.");
        }

        if (btnSkipChecklist != null) {
            btnSkipChecklist.setOnClickListener(v -> {
                SafeScoreHelper.updateSafeScore(DriverSafetyChecklistActivity.this, -5);
                Toast.makeText(DriverSafetyChecklistActivity.this, "Checklist ignorado! -5 pontos de SafeScore.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(DriverSafetyChecklistActivity.this, RideInProgressActivity.class);
                intent.putExtra("IS_DRIVER_MODE", true);
                intent.putExtra("PASSENGER_NAME", passengerName);
                intent.putExtra("DESTINATION", destination);
                intent.putExtra("RIDE_PRICE", ridePrice);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            });
        } else {
            Log.e(TAG, "Button btnSkipChecklist não encontrado.");
        }
    }

    private void updateUI() {
        if (cardVehicleCondition != null && checkVehicleCondition != null)
            updateCardUI(cardVehicleCondition, checkVehicleCondition, isVehicleConditionChecked);
        if (cardLicenseInsurance != null && checkLicenseInsurance != null)
            updateCardUI(cardLicenseInsurance, checkLicenseInsurance, isLicenseInsuranceChecked);
        if (cardRespectCode != null && checkRespectCode != null)
            updateCardUI(cardRespectCode, checkRespectCode, isRespectCodeChecked);
        if (cardRecording != null && checkRecording != null)
            updateCardUI(cardRecording, checkRecording, isRecordingChecked);

        boolean allCompleted = allChecksCompleted();
        if (btnStartRide != null) {
            btnStartRide.setEnabled(allCompleted);
        }

        // Mostrar confete de sucesso se todos os itens foram marcados
        if (allCompleted && konfettiViewDriver != null) {
            ConfettiManager.showSuccessConfetti(konfettiViewDriver);
        }
    }

    private void updateCardUI(CardView card, ImageView checkmark, boolean isChecked) {
        int cardBgColor = ContextCompat.getColor(this, R.color.gray_very_dark);
        ColorStateList checkmarkTint = isChecked ?
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.uber_blue)) :
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray_medium));
        int checkmarkDrawable = isChecked ? R.drawable.ic_check_circle : R.drawable.ic_check_circle_outline;

        if (checkmark != null) {
            if (card != null) card.setCardBackgroundColor(cardBgColor);
            checkmark.setImageResource(checkmarkDrawable);
            checkmark.setImageTintList(checkmarkTint);
        } else {
            Log.e(TAG, "ImageView checkmark é nulo dentro de updateCardUI para o card com ID: " + (card != null ? card.getId() : "desconhecido"));
        }
    }

    private boolean allChecksCompleted() {
        return isVehicleConditionChecked && isLicenseInsuranceChecked && isRespectCodeChecked && isRecordingChecked;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}