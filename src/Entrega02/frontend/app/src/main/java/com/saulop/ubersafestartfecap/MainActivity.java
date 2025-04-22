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

public class MainActivity extends AppCompatActivity {

    private CardView cardIdentity, cardSeatbelt, cardRoute, cardVehicle;
    private ImageView checkIdentity, checkSeatbelt, checkRoute, checkVehicle;
    private Button btnStartRide, btnSkipChecklist;
    private TextView textViewRideDestination, textViewDriverInfo, textViewSafetyTitle, textViewTripInfo;
    private ImageView menuButton, notificationButton, profileButton;

    private boolean isIdentityChecked = false;
    private boolean isSeatbeltChecked = false;
    private boolean isRouteChecked = false;
    private boolean isVehicleChecked = false;

    private String destination = "";
    private String ridePrice = "";
    private String driverName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            destination = extras.getString("DESTINATION", "");
            ridePrice = extras.getString("RIDE_PRICE", "");
            driverName = extras.getString("DRIVER_NAME", "");
        }

        initViews();
        setupClickListeners();
        displayRideInfo();
    }

    private void initViews() {
        menuButton = findViewById(R.id.menuButton);
        notificationButton = findViewById(R.id.notificationButton);
        profileButton = findViewById(R.id.profileButton);
        textViewTripInfo = findViewById(R.id.textViewTripInfo);

        textViewRideDestination = findViewById(R.id.textViewRideDestination);
        textViewDriverInfo = findViewById(R.id.textViewDriverInfo);
        textViewSafetyTitle = findViewById(R.id.textViewSafetyTitle);

        cardIdentity = findViewById(R.id.cardIdentity);
        cardSeatbelt = findViewById(R.id.cardSeatbelt);
        cardRoute = findViewById(R.id.cardRoute);
        cardVehicle = findViewById(R.id.cardVehicle);

        checkIdentity = findViewById(R.id.checkIdentity);
        checkSeatbelt = findViewById(R.id.checkSeatbelt);
        checkRoute = findViewById(R.id.checkRoute);
        checkVehicle = findViewById(R.id.checkVehicle);

        btnStartRide = findViewById(R.id.btnStartRide);
        btnSkipChecklist = findViewById(R.id.btnSkipChecklist);

        updateUI();
    }

    private void displayRideInfo() {
        if (textViewRideDestination != null) {
            textViewRideDestination.setText("Destino: " + destination);
        }

        if (textViewDriverInfo != null) {
            textViewDriverInfo.setText("Motorista: " + driverName + " | Valor: " + ridePrice);
        }
    }

    private void setupClickListeners() {
        menuButton.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Menu", Toast.LENGTH_SHORT).show();
        });

        notificationButton.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Notificações", Toast.LENGTH_SHORT).show();
        });

        profileButton.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Perfil", Toast.LENGTH_SHORT).show();
        });

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
                // Atualizar o SafeScore quando todos os checks estão completos
                SafeScoreHelper.updateSafeScore(MainActivity.this, 5);

                Toast.makeText(MainActivity.this, "Iniciando corrida com segurança...", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(MainActivity.this, RideInProgressActivity.class);
                intent.putExtra("IS_DRIVER_MODE", false);
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "Complete todos os itens de segurança primeiro", Toast.LENGTH_SHORT).show();
            }
        });

        // Novo botão para ignorar o checklist com penalidade no SafeScore
        btnSkipChecklist.setOnClickListener(v -> {
            // Reduzir o SafeScore em 5 pontos
            SafeScoreHelper.updateSafeScore(MainActivity.this, -5);

            Toast.makeText(MainActivity.this, "Checklist ignorado! -5 pontos de SafeScore.", Toast.LENGTH_SHORT).show();

            // Mesmo assim, iniciar a corrida
            Intent intent = new Intent(MainActivity.this, RideInProgressActivity.class);
            intent.putExtra("IS_DRIVER_MODE", false);
            startActivity(intent);
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
            card.setCardBackgroundColor(Color.parseColor("#004D21")); // Verde escuro
            checkmark.setImageTintList(ColorStateList.valueOf(Color.parseColor("#00C853"))); // Verde brilhante
        } else {
            card.setCardBackgroundColor(Color.parseColor("#262626")); // Cinza escuro
            checkmark.setImageTintList(ColorStateList.valueOf(Color.parseColor("#777777"))); // Cinza claro
        }
    }

    private boolean allChecksCompleted() {
        return isIdentityChecked && isSeatbeltChecked && isRouteChecked && isVehicleChecked;
    }
}