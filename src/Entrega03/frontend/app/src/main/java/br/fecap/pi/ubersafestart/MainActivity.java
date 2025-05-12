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

import br.fecap.pi.ubersafestart.utils.AchievementTracker;
import br.fecap.pi.ubersafestart.utils.SafeScoreHelper;
import nl.dionsegijn.konfetti.KonfettiView;
import br.fecap.pi.ubersafestart.utils.ConfettiManager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private CardView cardIdentity, cardSeatbelt, cardRoute, cardVehicle;
    private ImageView checkIdentity, checkSeatbelt, checkRoute, checkVehicle;
    private Button btnStartRide, btnSkipChecklist;
    private TextView textViewRideDestination, textViewDriverInfo, textViewSafetyTitle;
    private MaterialToolbar toolbar;
    private KonfettiView konfettiView;

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
        toolbar = findViewById(R.id.toolbarChecklist);

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

        konfettiView = findViewById(R.id.konfettiView);

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
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            });
        } else {
            Log.e(TAG, "Toolbar não encontrada no layout.");
        }

        if (cardIdentity != null) {
            cardIdentity.setOnClickListener(v -> {
                isIdentityChecked = !isIdentityChecked;
                updateUI();

                // Mostrar efeito de confete se o item foi marcado
                if (isIdentityChecked && konfettiView != null) {
                    ConfettiManager.showConfetti(konfettiView);
                }
            });
        } else {
            Log.e(TAG, "CardView cardIdentity não encontrado.");
        }

        if (cardSeatbelt != null) {
            cardSeatbelt.setOnClickListener(v -> {
                isSeatbeltChecked = !isSeatbeltChecked;
                updateUI();

                if (isSeatbeltChecked && konfettiView != null) {
                    ConfettiManager.showConfetti(konfettiView);
                }
            });
        } else {
            Log.e(TAG, "CardView cardSeatbelt não encontrado.");
        }

        if (cardRoute != null) {
            cardRoute.setOnClickListener(v -> {
                isRouteChecked = !isRouteChecked;
                updateUI();

                // Mostrar efeito de confete se o item foi marcado
                if (isRouteChecked && konfettiView != null) {
                    ConfettiManager.showConfetti(konfettiView);
                }
            });
        } else {
            Log.e(TAG, "CardView cardRoute não encontrado.");
        }

        if (cardVehicle != null) {
            cardVehicle.setOnClickListener(v -> {
                isVehicleChecked = !isVehicleChecked;
                updateUI();

                // Mostrar efeito de confete se o item foi marcado
                if (isVehicleChecked && konfettiView != null) {
                    ConfettiManager.showConfetti(konfettiView);
                }
            });
        } else {
            Log.e(TAG, "CardView cardVehicle não encontrado.");
        }

        if (btnStartRide != null) {
            btnStartRide.setOnClickListener(v -> {
                if (allChecksCompleted()) {
                    SafeScoreHelper.updateSafeScore(MainActivity.this, 5);
                    AchievementTracker.trackAchievement(MainActivity.this, "checklist", 1);
                    Toast.makeText(MainActivity.this, "Iniciando corrida com segurança...", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, RideInProgressActivity.class);
                    intent.putExtra("IS_DRIVER_MODE", false);
                    intent.putExtra("DESTINATION", destination);
                    intent.putExtra("DRIVER_NAME", driverName);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Complete todos os itens de segurança primeiro", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e(TAG, "Button btnStartRide não encontrado.");
        }

        if (btnSkipChecklist != null) {
            btnSkipChecklist.setOnClickListener(v -> {
                SafeScoreHelper.updateSafeScore(MainActivity.this, -5);
                Toast.makeText(MainActivity.this, "Checklist ignorado! -5 pontos de SafeScore.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, RideInProgressActivity.class);
                intent.putExtra("IS_DRIVER_MODE", false);
                intent.putExtra("DESTINATION", destination);
                intent.putExtra("DRIVER_NAME", driverName);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            });
        } else {
            Log.e(TAG, "Button btnSkipChecklist não encontrado.");
        }
    }

    private void updateUI() {
        if (cardIdentity != null && checkIdentity != null)
            updateCardUI(cardIdentity, checkIdentity, isIdentityChecked);
        if (cardSeatbelt != null && checkSeatbelt != null)
            updateCardUI(cardSeatbelt, checkSeatbelt, isSeatbeltChecked);
        if (cardRoute != null && checkRoute != null)
            updateCardUI(cardRoute, checkRoute, isRouteChecked);
        if (cardVehicle != null && checkVehicle != null)
            updateCardUI(cardVehicle, checkVehicle, isVehicleChecked);

        boolean allCompleted = allChecksCompleted();
        if (btnStartRide != null) {
            btnStartRide.setEnabled(allCompleted);
        }

        // Mostrar confete de sucesso se todos os itens foram marcados
        if (allCompleted && konfettiView != null) {
            ConfettiManager.showSuccessConfetti(konfettiView);
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
        return isIdentityChecked && isSeatbeltChecked && isRouteChecked && isVehicleChecked;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}