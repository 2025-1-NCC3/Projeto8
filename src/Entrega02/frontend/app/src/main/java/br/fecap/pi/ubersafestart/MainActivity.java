package br.fecap.pi.ubersafestart; // <<< Verifique se este é o seu package correto

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log; // Importar Log
import android.widget.Button; // Ou MaterialButton
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
// Importar MaterialToolbar
import com.google.android.material.appbar.MaterialToolbar;
// Importar MaterialButton se for usar
// import com.google.android.material.button.MaterialButton;

import br.fecap.pi.ubersafestart.utils.SafeScoreHelper; // <<< Verifique o package
import br.fecap.pi.ubersafestart.R; // <<< Import R

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity"; // Tag para Logs

    private CardView cardIdentity, cardSeatbelt, cardRoute, cardVehicle;
    private ImageView checkIdentity, checkSeatbelt, checkRoute, checkVehicle;
    private Button btnStartRide, btnSkipChecklist; // Ou MaterialButton
    // REMOVIDO: private TextView textViewTripInfo;
    private TextView textViewRideDestination, textViewDriverInfo, textViewSafetyTitle;
    // REMOVIDO: private ImageView menuButton, notificationButton, profileButton;
    private MaterialToolbar toolbar; // ADICIONADO

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
        // Certifique-se que este é o layout XML refatorado
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
        // Encontra a nova Toolbar pelo ID definido no XML refatorado
        toolbar = findViewById(R.id.toolbarChecklist); // ID da Toolbar no activity_main.xml

        // REMOVIDO: findViewById para menuButton, notificationButton, profileButton, textViewTripInfo

        textViewRideDestination = findViewById(R.id.textViewRideDestination);
        textViewDriverInfo = findViewById(R.id.textViewDriverInfo);
        textViewSafetyTitle = findViewById(R.id.textViewSafetyTitle); // ID do título dentro do card

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

        updateUI(); // Chama após inicializar as views
    }

    private void displayRideInfo() {
        // Adiciona verificações de nulidade
        if (textViewRideDestination != null) {
            textViewRideDestination.setText("Destino: " + destination);
        }
        if (textViewDriverInfo != null) {
            textViewDriverInfo.setText("Motorista: " + driverName + " | Valor: " + ridePrice);
        }
    }

    private void setupClickListeners() {
        // Configura o botão de voltar da nova Toolbar
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish()); // Fecha a activity
        } else {
            Log.e(TAG, "Toolbar não encontrada no layout.");
        }

        // REMOVIDO: Listeners para menuButton, notificationButton, profileButton

        // Listeners dos cards do checklist (com verificações de nulidade)
        if (cardIdentity != null) {
            cardIdentity.setOnClickListener(v -> {
                isIdentityChecked = !isIdentityChecked;
                updateUI();
            });
        } else { Log.e(TAG, "CardView cardIdentity não encontrado."); }

        if (cardSeatbelt != null) {
            cardSeatbelt.setOnClickListener(v -> {
                isSeatbeltChecked = !isSeatbeltChecked;
                updateUI();
            });
        } else { Log.e(TAG, "CardView cardSeatbelt não encontrado."); }

        if (cardRoute != null) {
            cardRoute.setOnClickListener(v -> {
                isRouteChecked = !isRouteChecked;
                updateUI();
            });
        } else { Log.e(TAG, "CardView cardRoute não encontrado."); }

        if (cardVehicle != null) {
            cardVehicle.setOnClickListener(v -> {
                isVehicleChecked = !isVehicleChecked;
                updateUI();
            });
        } else { Log.e(TAG, "CardView cardVehicle não encontrado."); }

        // Listeners dos botões (com verificações de nulidade)
        if (btnStartRide != null) {
            btnStartRide.setOnClickListener(v -> {
                if (allChecksCompleted()) {
                    SafeScoreHelper.updateSafeScore(MainActivity.this, 5);
                    Toast.makeText(MainActivity.this, "Iniciando corrida com segurança...", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, RideInProgressActivity.class);
                    intent.putExtra("IS_DRIVER_MODE", false); // Passageiro
                    intent.putExtra("DESTINATION", destination);
                    intent.putExtra("DRIVER_NAME", driverName);
                    startActivity(intent);
                    finish(); // Fecha o checklist
                } else {
                    Toast.makeText(MainActivity.this, "Complete todos os itens de segurança primeiro", Toast.LENGTH_SHORT).show();
                }
            });
        } else { Log.e(TAG, "Button btnStartRide não encontrado."); }

        if (btnSkipChecklist != null) {
            btnSkipChecklist.setOnClickListener(v -> {
                SafeScoreHelper.updateSafeScore(MainActivity.this, -5);
                Toast.makeText(MainActivity.this, "Checklist ignorado! -5 pontos de SafeScore.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, RideInProgressActivity.class);
                intent.putExtra("IS_DRIVER_MODE", false); // Passageiro
                intent.putExtra("DESTINATION", destination);
                intent.putExtra("DRIVER_NAME", driverName);
                startActivity(intent);
                finish(); // Fecha o checklist
            });
        } else { Log.e(TAG, "Button btnSkipChecklist não encontrado."); }
    }

    private void updateUI() {
        // Atualiza a aparência dos cards e checkmarks
        if (cardIdentity != null && checkIdentity != null)
            updateCardUI(cardIdentity, checkIdentity, isIdentityChecked);
        if (cardSeatbelt != null && checkSeatbelt != null)
            updateCardUI(cardSeatbelt, checkSeatbelt, isSeatbeltChecked);
        if (cardRoute != null && checkRoute != null)
            updateCardUI(cardRoute, checkRoute, isRouteChecked);
        if (cardVehicle != null && checkVehicle != null)
            updateCardUI(cardVehicle, checkVehicle, isVehicleChecked);

        // Habilita/Desabilita o botão principal
        boolean allCompleted = allChecksCompleted();
        if (btnStartRide != null) {
            btnStartRide.setEnabled(allCompleted);
            // A aparência habilitado/desabilitado é controlada pelo MaterialButton ou por um seletor de drawable
            // A linha abaixo que usava setBackgroundResource pode ser removida se usar MaterialButton
            // btnStartRide.setBackgroundResource(allCompleted ? R.drawable.button_enabled : R.drawable.button_disabled);
        }
    }

    // Método atualizado para usar ContextCompat e trocar drawables
    private void updateCardUI(CardView card, ImageView checkmark, boolean isChecked) {
        int cardBgColor = ContextCompat.getColor(this, R.color.gray_very_dark);
        ColorStateList checkmarkTint = isChecked ?
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.uber_blue)) :
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray_medium));
        int checkmarkDrawable = isChecked ? R.drawable.ic_check_circle : R.drawable.ic_check_circle_outline;

        // Verifica se checkmark não é nulo antes de usar
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
}
