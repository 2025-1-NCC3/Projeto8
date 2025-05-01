package br.fecap.pi.ubersafestart; // Ajuste o package se for br.fecap...

import android.content.Intent;
import android.content.res.ColorStateList;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.util.Log; // Importar Log
import android.view.View;
import android.widget.Button; // Ou MaterialButton
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
// Importar MaterialToolbar
import com.google.android.material.appbar.MaterialToolbar;
// Importar MaterialButton se for usar
// import com.google.android.material.button.MaterialButton;

import br.fecap.pi.ubersafestart.utils.SafeScoreHelper; // Ajuste o package se necessário

public class DriverSafetyChecklistActivity extends AppCompatActivity {

    private static final String TAG = "DriverChecklist"; // Tag para Log

    private CardView cardVehicleCondition, cardLicenseInsurance, cardRespectCode, cardRecording;
    private ImageView checkVehicleCondition, checkLicenseInsurance, checkRespectCode, checkRecording;
    private Button btnStartRide, btnSkipChecklist; // Ou MaterialButton
    private TextView textViewPassengerDestination, textViewPassengerInfo;
    // REMOVIDO: private ImageView menuButton, notificationButton, profileButton;
    private MaterialToolbar toolbar; // ADICIONADO

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
        // Certifique-se que este é o layout XML refatorado
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
        // Encontra a nova Toolbar pelo ID definido no XML refatorado
        toolbar = findViewById(R.id.toolbarDriverChecklist); // ID da Toolbar no XML

        // Views do checklist
        cardVehicleCondition = findViewById(R.id.cardVehicleCondition);
        cardLicenseInsurance = findViewById(R.id.cardLicenseInsurance);
        cardRespectCode = findViewById(R.id.cardRespectCode);
        cardRecording = findViewById(R.id.cardRecording);

        checkVehicleCondition = findViewById(R.id.checkVehicleCondition);
        checkLicenseInsurance = findViewById(R.id.checkLicenseInsurance);
        checkRespectCode = findViewById(R.id.checkRespectCode);
        checkRecording = findViewById(R.id.checkRecording);

        // Botões
        btnStartRide = findViewById(R.id.btnStartRide);
        btnSkipChecklist = findViewById(R.id.btnSkipChecklist);

        // Textos de informação da corrida
        textViewPassengerDestination = findViewById(R.id.textViewPassengerDestination);
        textViewPassengerInfo = findViewById(R.id.textViewPassengerInfo);

        // REMOVIDO: findViewById para menuButton, notificationButton, profileButton

        updateUI(); // Chama após inicializar todas as views necessárias
    }

    private void displayRideInfo() {
        // Verifica se os TextViews não são nulos antes de usar
        if (textViewPassengerDestination != null) {
            textViewPassengerDestination.setText("Destino: " + destination);
        }
        if (textViewPassengerInfo != null) {
            textViewPassengerInfo.setText("Passageiro: " + passengerName + " | Embarque: " + pickupLocation);
        }
    }

    private void setupClickListeners() {
        // Configura o botão de voltar (navigation icon) da nova Toolbar
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish()); // Fecha a activity
        } else {
            Log.e(TAG, "Toolbar não encontrada no layout.");
        }

        // REMOVIDO: Listeners dos botões da toolbar antiga

        // Listeners dos cards do checklist (com verificações de nulidade)
        if (cardVehicleCondition != null) {
            cardVehicleCondition.setOnClickListener(v -> {
                isVehicleConditionChecked = !isVehicleConditionChecked;
                updateUI();
            });
        } else { Log.e(TAG, "CardView cardVehicleCondition não encontrado."); }

        if (cardLicenseInsurance != null) {
            cardLicenseInsurance.setOnClickListener(v -> {
                isLicenseInsuranceChecked = !isLicenseInsuranceChecked;
                updateUI();
            });
        } else { Log.e(TAG, "CardView cardLicenseInsurance não encontrado."); }

        if (cardRespectCode != null) {
            cardRespectCode.setOnClickListener(v -> {
                isRespectCodeChecked = !isRespectCodeChecked;
                updateUI();
            });
        } else { Log.e(TAG, "CardView cardRespectCode não encontrado."); }

        if (cardRecording != null) {
            cardRecording.setOnClickListener(v -> {
                isRecordingChecked = !isRecordingChecked;
                updateUI();
            });
        } else { Log.e(TAG, "CardView cardRecording não encontrado."); }


        // Listeners dos botões de ação (com verificações de nulidade)
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
                    finish(); // Fecha a tela de checklist após iniciar
                } else {
                    Toast.makeText(DriverSafetyChecklistActivity.this, "Complete todos os itens de segurança primeiro", Toast.LENGTH_SHORT).show();
                }
            });
        } else { Log.e(TAG, "Button btnStartRide não encontrado."); }

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
                finish(); // Fecha a tela de checklist após iniciar
            });
        } else { Log.e(TAG, "Button btnSkipChecklist não encontrado."); }
    }

    private void updateUI() {
        // Atualiza a aparência dos cards e checkmarks
        if(cardVehicleCondition != null && checkVehicleCondition != null)
            updateCardUI(cardVehicleCondition, checkVehicleCondition, isVehicleConditionChecked);
        if(cardLicenseInsurance != null && checkLicenseInsurance != null)
            updateCardUI(cardLicenseInsurance, checkLicenseInsurance, isLicenseInsuranceChecked);
        if(cardRespectCode != null && checkRespectCode != null)
            updateCardUI(cardRespectCode, checkRespectCode, isRespectCodeChecked);
        if(cardRecording != null && checkRecording != null)
            updateCardUI(cardRecording, checkRecording, isRecordingChecked);

        // Habilita/Desabilita o botão principal
        boolean allCompleted = allChecksCompleted();
        if (btnStartRide != null) {
            btnStartRide.setEnabled(allCompleted);
            // A aparência habilitado/desabilitado é controlada pelo MaterialButton ou por um seletor de drawable
            // A linha abaixo que usava setBackgroundResource pode ser removida se usar MaterialButton
            // btnStartRide.setBackgroundResource(allCompleted ? R.drawable.button_enabled : R.drawable.button_disabled);
        }
    }

    private void updateCardUI(CardView card, ImageView checkmark, boolean isChecked) {
        // Usa as cores neutras/azuis definidas
        int cardBgColor = ContextCompat.getColor(this, R.color.gray_very_dark); // Fundo do card interno
        ColorStateList checkmarkTint = isChecked ?
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.uber_blue)) : // Azul para checado
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray_medium)); // Cinza para não checado
        // Troca o drawable entre preenchido e contorno
        int checkmarkDrawable = isChecked ? R.drawable.ic_check_circle : R.drawable.ic_check_circle_outline;

        // Verifica se checkmark não é nulo antes de usar
        if (checkmark != null) {
            if (card != null) card.setCardBackgroundColor(cardBgColor);
            checkmark.setImageResource(checkmarkDrawable); // Define o ícone correto
            checkmark.setImageTintList(checkmarkTint); // Define a cor correta
        } else {
            Log.e(TAG, "ImageView checkmark é nulo dentro de updateCardUI para o card com ID: " + (card != null ? card.getId() : "desconhecido"));
        }
    }

    private boolean allChecksCompleted() {
        return isVehicleConditionChecked && isLicenseInsuranceChecked && isRespectCodeChecked && isRecordingChecked;
    }
}
