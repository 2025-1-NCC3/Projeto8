package br.fecap.pi.ubersafestart; // Ajuste o package se for br.fecap...

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log; // Importar Log
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button; // Ou MaterialButton
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

// Imports podem precisar ser ajustados com base na sua estrutura exata

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity"; // Tag para Log

    private TextView textViewLocationName1;
    private TextView textViewLocationAddress1;
    private TextView textViewLocationName2;
    private TextView textViewLocationAddress2;
    // REMOVIDO: private EditText editTextSearch;
    private LinearLayout layoutSearchClickable; // *** ADICIONADO: Referência ao LinearLayout ***
    private LinearLayout layoutSchedule;
    private LinearLayout navAccount;
    private LinearLayout navHome;
    private LinearLayout navServices;
    private LinearLayout navActivity;
    private CardView cardViewRecentLocation1;
    private CardView cardViewRecentLocation2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Certifique-se que este é o layout XML refatorado (da resposta #15)
        setContentView(R.layout.activity_home);

        initViews();
        loadLocationHistoryData();
        setupClickListeners(); // Configura todos os listeners
    }

    private void initViews() {
        textViewLocationName1 = findViewById(R.id.textViewLocationName1);
        textViewLocationAddress1 = findViewById(R.id.textViewLocationAddress1);
        textViewLocationName2 = findViewById(R.id.textViewLocationName2);
        textViewLocationAddress2 = findViewById(R.id.textViewLocationAddress2);

        // *** ALTERADO: Encontra o LinearLayout clicável pelo seu ID ***
        layoutSearchClickable = findViewById(R.id.layoutSearchClickable);
        layoutSchedule = findViewById(R.id.layoutSchedule);

        navAccount = findViewById(R.id.navAccount);
        navHome = findViewById(R.id.navHome);
        navServices = findViewById(R.id.navServices);
        navActivity = findViewById(R.id.navActivity);

        cardViewRecentLocation1 = findViewById(R.id.cardViewRecentLocation1);
        cardViewRecentLocation2 = findViewById(R.id.cardViewRecentLocation2);
    }

    private void loadLocationHistoryData() {
        // Mantém o carregamento de dados estáticos
        String location1Name = "Avenida Paulista, 1578";
        String location1Address = "Bela Vista, São Paulo - SP";
        String location2Name = "Parque Ibirapuera";
        String location2Address = "Av. Pedro Álvares Cabral";

        if (textViewLocationName1 != null) textViewLocationName1.setText(location1Name);
        if (textViewLocationAddress1 != null) {
            textViewLocationAddress1.setText(location1Address);
            textViewLocationAddress1.setVisibility(View.VISIBLE);
        }
        if (textViewLocationName2 != null) textViewLocationName2.setText(location2Name);
        if (textViewLocationAddress2 != null) {
            textViewLocationAddress2.setText(location2Address);
            textViewLocationAddress2.setVisibility(View.VISIBLE);
        }
        if (cardViewRecentLocation1 != null) cardViewRecentLocation1.setVisibility(View.VISIBLE);
        if (cardViewRecentLocation2 != null) cardViewRecentLocation2.setVisibility(View.VISIBLE);
    }

    // Configura todos os listeners
    private void setupClickListeners() {
        setupSearchListener();
        setupNavigationListeners();
        setupRecentLocationsListeners();
        setupScheduleButtonListener();
    }

    // Configura o listener para o LinearLayout da busca
    private void setupSearchListener() {
        if (layoutSearchClickable != null) {
            layoutSearchClickable.setOnClickListener(v -> {
                // Ação ao clicar na barra de busca "Para onde?"
                Toast.makeText(HomeActivity.this, "Abrir tela de busca...", Toast.LENGTH_SHORT).show();
                // Ex: Iniciar SearchActivity
                // Intent searchIntent = new Intent(HomeActivity.this, SearchActivity.class);
                // startActivity(searchIntent);
            });
        } else {
            Log.e(TAG, "LinearLayout layoutSearchClickable não encontrado.");
        }
    }

    // Configura o listener para o botão "Mais Tarde"
    private void setupScheduleButtonListener() {
        if (layoutSchedule != null) {
            layoutSchedule.setOnClickListener(v -> {
                Toast.makeText(HomeActivity.this, "Abrir agendamento...", Toast.LENGTH_SHORT).show();
                // Implementar lógica de agendamento
            });
        } else {
            Log.e(TAG, "LinearLayout layoutSchedule não encontrado.");
        }
    }

    // Configura listeners da navegação inferior
    private void setupNavigationListeners() {
        View.OnClickListener listener = v -> {
            int id = v.getId();
            updateBottomNavigationSelection(id); // Atualiza visualmente

            if (id == R.id.navAccount) {
                openProfileActivity();
            } else if (id == R.id.navHome) {
                // Já está na Home
            } else if (id == R.id.navServices) {
                Toast.makeText(HomeActivity.this, "Opções em breve", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.navActivity) {
                Toast.makeText(HomeActivity.this, "Atividade em breve", Toast.LENGTH_SHORT).show();
            }
        };

        if (navHome != null) navHome.setOnClickListener(listener); else Log.e(TAG, "LinearLayout navHome não encontrado.");
        if (navServices != null) navServices.setOnClickListener(listener); else Log.e(TAG, "LinearLayout navServices não encontrado.");
        if (navActivity != null) navActivity.setOnClickListener(listener); else Log.e(TAG, "LinearLayout navActivity não encontrado.");
        if (navAccount != null) navAccount.setOnClickListener(listener); else Log.e(TAG, "LinearLayout navAccount não encontrado.");

        updateBottomNavigationSelection(R.id.navHome); // Define o estado inicial
    }

    // Atualiza a seleção visual da navegação inferior
    private void updateBottomNavigationSelection(int selectedItemId) {
        LinearLayout[] navItems = {navHome, navServices, navActivity, navAccount};
        int[] iconIds = {R.id.iconHome, R.id.iconServices, R.id.iconActivity, R.id.iconAccount};
        int[] textIds = {R.id.textHome, R.id.textServices, R.id.textActivity, R.id.textAccount};
        // Assume que você tem drawables base para os ícones
        int[] icons = {R.drawable.ic_home, R.drawable.ic_grid, R.drawable.ic_activity, R.drawable.ic_account};

        int activeColor = ContextCompat.getColor(this, R.color.white_fff);
        int inactiveColor = ContextCompat.getColor(this, R.color.gray_light); // Ou gray_medium

        for (int i = 0; i < navItems.length; i++) {
            LinearLayout item = navItems[i];
            if (item == null) continue;

            ImageView icon = item.findViewById(iconIds[i]);
            TextView text = item.findViewById(textIds[i]);
            if (icon == null || text == null) continue;

            boolean isActive = (item.getId() == selectedItemId);

            // Define o drawable (pode ser o mesmo, a cor muda)
            icon.setImageResource(icons[i]);
            // Define a cor (tint)
            icon.setImageTintList(ColorStateList.valueOf(isActive ? activeColor : inactiveColor));
            // Define a cor do texto
            text.setTextColor(isActive ? activeColor : inactiveColor);
        }
    }


    // Configura listeners dos cards de locais recentes
    private void setupRecentLocationsListeners() {
        if (cardViewRecentLocation1 != null) {
            cardViewRecentLocation1.setOnClickListener(v -> {
                if (textViewLocationName1 != null) {
                    String destination = textViewLocationName1.getText().toString();
                    if (!destination.isEmpty()) {
                        // Poderia pré-preencher a busca ou iniciar a rota direto
                        showLoadingDialog(destination);
                    }
                }
            });
        } else { Log.e(TAG, "CardView cardViewRecentLocation1 não encontrado."); }

        if (cardViewRecentLocation2 != null) {
            cardViewRecentLocation2.setOnClickListener(v -> {
                if (textViewLocationName2 != null) {
                    String destination = textViewLocationName2.getText().toString();
                    if (!destination.isEmpty()) {
                        showLoadingDialog(destination);
                    }
                }
            });
        } else { Log.e(TAG, "CardView cardViewRecentLocation2 não encontrado."); }
    }

    // --- Métodos de Diálogo e Navegação (Mantidos, mas usando tema) ---

    private void showLoadingDialog(final String destination) {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this, R.style.AlertDialogTheme); // Usa o tema
        View loadingView = LayoutInflater.from(this).inflate(R.layout.dialog_ride_loading, null);
        builder.setView(loadingView);
        builder.setCancelable(false);

        final AlertDialog loadingDialog = builder.create();
        loadingDialog.show();

        ProgressBar progressBar = loadingView.findViewById(R.id.progressBar);
        TextView statusText = loadingView.findViewById(R.id.textViewStatus);
        ImageView checkmarkImage = loadingView.findViewById(R.id.imageViewCheckmark);

        if(statusText != null) statusText.setText("Procurando motorista...");
        if(progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if(checkmarkImage != null) checkmarkImage.setVisibility(View.GONE);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing() && !isDestroyed() && loadingDialog.isShowing()) { // Verifica estado da Activity
                if(statusText != null) statusText.setText("Motorista encontrado!");
                if(progressBar != null) progressBar.setVisibility(View.GONE);
                if(checkmarkImage != null) checkmarkImage.setVisibility(View.VISIBLE);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (!isFinishing() && !isDestroyed() && loadingDialog.isShowing()) {
                        loadingDialog.dismiss();
                        showDriverInfoDialog(destination);
                    }
                }, 1000); // Mostra checkmark
            }
        }, 3000); // Tempo de busca
    }

    private void showDriverInfoDialog(String destination) {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this, R.style.AlertDialogTheme); // Usa o tema
        View driverInfoView = LayoutInflater.from(this).inflate(R.layout.dialog_driver_info, null);
        builder.setView(driverInfoView);
        builder.setCancelable(true); // Permitir fechar clicando fora

        TextView tvDriverName = driverInfoView.findViewById(R.id.textViewDriverName);
        TextView tvCarInfo = driverInfoView.findViewById(R.id.textViewCarInfo);
        TextView tvPrice = driverInfoView.findViewById(R.id.textViewPrice);
        TextView tvDestination = driverInfoView.findViewById(R.id.textViewDestination);
        RatingBar ratingBar = driverInfoView.findViewById(R.id.ratingBarDriver);
        RatingBar safeScoreBar = driverInfoView.findViewById(R.id.ratingBarSafeScore);
        Button btnPayment = driverInfoView.findViewById(R.id.buttonPayment); // Idealmente MaterialButton

        String driverName = "João Silva";
        String ridePrice = "R$ 23,50";
        // Adiciona verificações de nulidade antes de setar texto/rating
        if (tvDriverName != null) tvDriverName.setText(driverName);
        if (tvCarInfo != null) tvCarInfo.setText("Toyota Corolla - Preto - ABC-1234");
        if (tvPrice != null) tvPrice.setText(ridePrice);
        if (tvDestination != null) tvDestination.setText("Para: " + destination);
        if (ratingBar != null) ratingBar.setRating(4.7f);
        if (safeScoreBar != null) safeScoreBar.setRating(4.9f);

        final AlertDialog driverDialog = builder.create();
        driverDialog.show();

        if (btnPayment != null) {
            btnPayment.setOnClickListener(v -> {
                driverDialog.dismiss();
                navigateToSafetyChecklist(destination, driverName, ridePrice);
            });
        } else { Log.e(TAG, "Button buttonPayment não encontrado no dialog_driver_info."); }
    }

    private void navigateToSafetyChecklist(String destination, String driverName, String ridePrice) {
        Intent intent = new Intent(HomeActivity.this, MainActivity.class); // Assumindo MainActivity = Checklist Passageiro
        intent.putExtra("DESTINATION", destination);
        intent.putExtra("RIDE_PRICE", ridePrice);
        intent.putExtra("DRIVER_NAME", driverName);
        startActivity(intent);
    }

    private void openProfileActivity() {
        Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this, R.style.AlertDialogTheme) // Usa o tema
                .setMessage("Deseja sair do aplicativo?")
                .setPositiveButton("Sim", (dialog, which) -> finishAffinity())
                .setNegativeButton("Não", null)
                .show();
    }
}
