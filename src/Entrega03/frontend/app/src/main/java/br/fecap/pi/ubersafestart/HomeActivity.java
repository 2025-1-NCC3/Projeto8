package br.fecap.pi.ubersafestart;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import java.util.Locale;
import java.util.Random;

// Importações necessárias para a lógica de pareamento
import br.fecap.pi.ubersafestart.model.SimulatedUser;
import br.fecap.pi.ubersafestart.utils.StaticUserManager;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    // Constantes para SharedPreferences (USAR AS MESMAS DEFINIDAS EM ProfileActivity)
    private static final String USER_LOGIN_PREFS = "userPrefs"; // Para dados de login (token, nome, genero do backend)
    private static final String USER_LOCAL_PREFERENCES = "UserPreferences"; // Para preferências locais (pareamento)
    private static final String KEY_GENDER = "genero"; // Chave para gênero do usuário logado
    private static final String KEY_SAME_GENDER_PAIRING = "sameGenderPairingEnabled"; // Chave para preferência local

    // Componentes da UI
    private TextView textViewLocationName1;
    private TextView textViewLocationAddress1;
    private TextView textViewLocationName2;
    private TextView textViewLocationAddress2;
    private LinearLayout layoutSearchClickable;
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
        setContentView(R.layout.activity_home);

        initViews();
        loadLocationHistoryData(); // Carrega locais recentes (manter se for útil)
        setupClickListeners();
        updateBottomNavigationSelection(R.id.navHome); // Marcar Home como ativa ao iniciar
    }

    // Inicializa as views da Activity
    private void initViews() {
        textViewLocationName1 = findViewById(R.id.textViewLocationName1);
        textViewLocationAddress1 = findViewById(R.id.textViewLocationAddress1);
        textViewLocationName2 = findViewById(R.id.textViewLocationName2);
        textViewLocationAddress2 = findViewById(R.id.textViewLocationAddress2);
        layoutSearchClickable = findViewById(R.id.layoutSearchClickable);
        layoutSchedule = findViewById(R.id.layoutSchedule);
        navAccount = findViewById(R.id.navAccount);
        navHome = findViewById(R.id.navHome);
        navServices = findViewById(R.id.navServices);
        navActivity = findViewById(R.id.navActivity);
        cardViewRecentLocation1 = findViewById(R.id.cardViewRecentLocation1);
        cardViewRecentLocation2 = findViewById(R.id.cardViewRecentLocation2);
    }

    // Carrega dados de exemplo para locais recentes (pode ser adaptado ou removido)
    private void loadLocationHistoryData() {
        String location1Name = "Avenida Paulista, 1578";
        String location1Address = "Bela Vista, São Paulo - SP";
        String location2Name = "Parque Ibirapuera";
        String location2Address = "Av. Pedro Álvares Cabral";

        if (textViewLocationName1 != null) textViewLocationName1.setText(location1Name);
        if (textViewLocationAddress1 != null) textViewLocationAddress1.setText(location1Address);
        if (textViewLocationName2 != null) textViewLocationName2.setText(location2Name);
        if (textViewLocationAddress2 != null) textViewLocationAddress2.setText(location2Address);
        // Controla visibilidade dos cards de locais recentes
        if (cardViewRecentLocation1 != null) cardViewRecentLocation1.setVisibility(View.VISIBLE);
        if (cardViewRecentLocation2 != null) cardViewRecentLocation2.setVisibility(View.VISIBLE);
    }

    // Configura todos os listeners de clique da tela
    private void setupClickListeners() {
        setupSearchListener();
        setupNavigationListeners();
        setupRecentLocationsListeners();
        setupScheduleButtonListener();
    }

    // Listener para o campo de busca (simula início de busca de corrida)
    private void setupSearchListener() {
        if (layoutSearchClickable != null) {
            layoutSearchClickable.setOnClickListener(v -> {
                Log.d(TAG, "Busca iniciada pelo campo de pesquisa.");
                showLoadingDialog("Destino da Busca"); // Simula busca para um destino genérico
            });
        } else {
            Log.e(TAG, "LinearLayout layoutSearchClickable não encontrado.");
        }
    }

    // Listener para o botão de agendamento (atualmente mostra um Toast)
    private void setupScheduleButtonListener() {
        if (layoutSchedule != null) {
            layoutSchedule.setOnClickListener(v -> {
                Toast.makeText(HomeActivity.this, "Funcionalidade de agendamento em desenvolvimento.", Toast.LENGTH_SHORT).show();
            });
        } else {
            Log.e(TAG, "LinearLayout layoutSchedule não encontrado.");
        }
    }

    // Configura listeners para a barra de navegação inferior
    private void setupNavigationListeners() {
        View.OnClickListener listener = v -> {
            int id = v.getId();
            Log.d(TAG, "Item de navegação clicado: " + getResources().getResourceEntryName(id));
            updateBottomNavigationSelection(id); // Atualiza a seleção visual

            if (id == R.id.navAccount) {
                openProfileActivity();
            } else if (id == R.id.navHome) {
                // Já está na Home, nenhuma ação necessária
            } else if (id == R.id.navServices) {
                Toast.makeText(HomeActivity.this, "Opções em desenvolvimento", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.navActivity) {
                Toast.makeText(HomeActivity.this, "Atividade em desenvolvimento", Toast.LENGTH_SHORT).show();
            }
        };

        // Atribui o listener a cada item de navegação
        if (navHome != null) navHome.setOnClickListener(listener); else Log.e(TAG, "navHome nulo");
        if (navServices != null) navServices.setOnClickListener(listener); else Log.e(TAG, "navServices nulo");
        if (navActivity != null) navActivity.setOnClickListener(listener); else Log.e(TAG, "navActivity nulo");
        if (navAccount != null) navAccount.setOnClickListener(listener); else Log.e(TAG, "navAccount nulo");
    }

    // Atualiza a aparência da barra de navegação inferior para destacar o item selecionado
    private void updateBottomNavigationSelection(int selectedItemId) {
        LinearLayout[] navItems = {navHome, navServices, navActivity, navAccount};
        // IDs dos ícones e textos DENTRO de cada LinearLayout da navegação
        // **IMPORTANTE:** Você PRECISA ter IDs únicos para estes elementos no seu activity_home.xml
        int[] iconIds = {R.id.iconHome, R.id.iconServices, R.id.iconActivity, R.id.iconAccount}; // Exemplo de IDs, ajuste aos seus!
        int[] textIds = {R.id.textHome, R.id.textServices, R.id.textActivity, R.id.textAccount}; // Exemplo de IDs, ajuste aos seus!

        int activeColor = ContextCompat.getColor(this, R.color.white); // Cor para item ativo
        int inactiveColor = ContextCompat.getColor(this, R.color.gray_light); // Cor para item inativo

        for (int i = 0; i < navItems.length; i++) {
            LinearLayout item = navItems[i];
            if (item == null) {
                Log.w(TAG, "Item de navegação na posição " + i + " é nulo.");
                continue;
            }

            ImageView icon = item.findViewById(iconIds[i]);
            TextView text = item.findViewById(textIds[i]);

            if (icon == null) Log.w(TAG, "Ícone não encontrado para item " + i + " com ID " + getResources().getResourceEntryName(iconIds[i]));
            if (text == null) Log.w(TAG, "Texto não encontrado para item " + i + " com ID " + getResources().getResourceEntryName(textIds[i]));
            if (icon == null || text == null) continue; // Pula se não encontrar ícone ou texto

            boolean isActive = (item.getId() == selectedItemId);

            // Define a cor do ícone e do texto baseado na seleção
            icon.setImageTintList(ColorStateList.valueOf(isActive ? activeColor : inactiveColor));
            text.setTextColor(isActive ? activeColor : inactiveColor);
        }
    }

    // Configura listeners para os cards de locais recentes
    private void setupRecentLocationsListeners() {
        View.OnClickListener recentLocationListener = v -> {
            String destination = "";
            int id = v.getId();
            if (id == R.id.cardViewRecentLocation1) {
                if (textViewLocationName1 != null) destination = textViewLocationName1.getText().toString();
            } else if (id == R.id.cardViewRecentLocation2) {
                if (textViewLocationName2 != null) destination = textViewLocationName2.getText().toString();
            }

            if (!destination.isEmpty()) {
                Log.d(TAG, "Local recente clicado: " + destination);
                showLoadingDialog(destination); // Inicia a busca de corrida para este destino
            } else {
                Log.w(TAG, "Nome do local recente está vazio.");
            }
        };

        if (cardViewRecentLocation1 != null) cardViewRecentLocation1.setOnClickListener(recentLocationListener);
        if (cardViewRecentLocation2 != null) cardViewRecentLocation2.setOnClickListener(recentLocationListener);
    }

    // Exibe um diálogo de "carregando" enquanto simula a busca por motorista
    private void showLoadingDialog(final String destination) {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this, R.style.AlertDialogTheme);
        View loadingView = LayoutInflater.from(this).inflate(R.layout.dialog_ride_loading, null);
        builder.setView(loadingView);
        builder.setCancelable(false); // Impede fechar o diálogo clicando fora

        final AlertDialog loadingDialog = builder.create();
        if (loadingDialog.getWindow() != null) {
            loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        loadingDialog.show();

        ProgressBar progressBar = loadingView.findViewById(R.id.progressBar);
        TextView statusText = loadingView.findViewById(R.id.textViewStatus);
        ImageView checkmarkImage = loadingView.findViewById(R.id.imageViewCheckmark);

        // Configuração inicial do diálogo de loading
        if(statusText != null) statusText.setText(R.string.dialog_loading_status_searching);
        if(progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if(checkmarkImage != null) checkmarkImage.setVisibility(View.GONE);

        // Simula a busca por motorista (com delay)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Verifica se a activity ainda existe e o diálogo está visível antes de continuar
            if (isFinishing() || isDestroyed() || !loadingDialog.isShowing()) {
                return;
            }

            // 1. Obtém gênero e preferência do usuário atual (Passageiro)
            SharedPreferences loginPrefs = getSharedPreferences(USER_LOGIN_PREFS, MODE_PRIVATE);
            String currentUserGender = loginPrefs.getString(KEY_GENDER, "").toUpperCase(); // Gênero do backend

            SharedPreferences localPrefs = getSharedPreferences(USER_LOCAL_PREFERENCES, Context.MODE_PRIVATE);
            boolean preferSameGender = localPrefs.getBoolean(KEY_SAME_GENDER_PAIRING, false); // Preferência local

            Log.d(TAG, "Buscando motorista. Gênero do passageiro: " + currentUserGender + ", Prefere mesmo gênero: " + preferSameGender);

            // 2. Busca um motorista estático usando o StaticUserManager
            SimulatedUser driver = StaticUserManager.getRandomDriver(currentUserGender, preferSameGender);

            if (driver != null) {
                // Motorista encontrado! Atualiza UI do diálogo de loading
                Log.d(TAG, "Motorista encontrado: " + driver.getNome() + " (" + driver.getGenero() + ")");
                if (statusText != null) statusText.setText("Motorista encontrado!");
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (checkmarkImage != null) checkmarkImage.setVisibility(View.VISIBLE);

                // Aguarda um pouco para mostrar a mensagem e então exibe o diálogo do motorista
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (!isFinishing() && !isDestroyed() && loadingDialog.isShowing()) {
                        loadingDialog.dismiss(); // Fecha o diálogo de loading
                        showDriverInfoDialog(destination, driver); // Abre o diálogo com info do motorista
                    }
                }, 1000); // Delay para mostrar o checkmark

            } else {
                // Nenhum motorista compatível encontrado
                Log.w(TAG, "Nenhum motorista compatível encontrado com os critérios.");
                if (statusText != null) statusText.setText("Nenhum motorista compatível.");
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                // Opcional: Mostrar ícone de erro
                // if(checkmarkImage != null) checkmarkImage.setImageResource(R.drawable.ic_error);
                // if(checkmarkImage != null) checkmarkImage.setVisibility(View.VISIBLE);

                Toast.makeText(HomeActivity.this, "Nenhum motorista compatível disponível no momento.", Toast.LENGTH_LONG).show();

                // Fecha o diálogo de loading após um tempo
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (!isFinishing() && !isDestroyed() && loadingDialog.isShowing()) {
                        loadingDialog.dismiss();
                    }
                }, 2000); // Delay para mostrar a mensagem de erro
            }
        }, 3000); // Delay total simulando a busca
    }

    // Exibe o diálogo com as informações do motorista encontrado
    private void showDriverInfoDialog(String destination, SimulatedUser driver) {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this, R.style.AlertDialogTheme);
        // Infla o layout dialog_driver_info.xml
        View driverInfoView = LayoutInflater.from(this).inflate(R.layout.dialog_driver_info, null);
        builder.setView(driverInfoView);
        builder.setCancelable(true); // Permite fechar clicando fora

        // Encontra os componentes dentro do layout inflado
        TextView tvDialogTitle = driverInfoView.findViewById(R.id.textViewRideFound);
        TextView tvDriverName = driverInfoView.findViewById(R.id.textViewDriverName);
        TextView tvCarInfo = driverInfoView.findViewById(R.id.textViewCarInfo);
        TextView tvPrice = driverInfoView.findViewById(R.id.textViewPrice);
        TextView tvDestinationDialog = driverInfoView.findViewById(R.id.textViewDestination);
        RatingBar ratingBarDriver = driverInfoView.findViewById(R.id.ratingBarDriver);
        RatingBar ratingBarSafeScore = driverInfoView.findViewById(R.id.ratingBarSafeScore); // RatingBar para SafeScore do motorista
        Button btnConfirmRide = driverInfoView.findViewById(R.id.buttonPayment); // Botão de confirmação

        String ridePrice = generateRandomPriceHome(); // Gera um preço aleatório para a corrida

        // Popula os componentes com os dados do motorista simulado
        if (tvDialogTitle != null) tvDialogTitle.setText("Motorista a caminho!");
        if (tvDriverName != null) tvDriverName.setText(driver.getNome());
        if (tvCarInfo != null) tvCarInfo.setText(driver.getModeloCarro() + " - " + driver.getPlacaCarro());
        if (tvPrice != null) tvPrice.setText(ridePrice);
        if (tvDestinationDialog != null) tvDestinationDialog.setText("Para: " + destination);
        if (ratingBarDriver != null) ratingBarDriver.setRating(driver.getNotaMotorista());
        // Exibe a nota do motorista como "Safe Score" (adapte se tiver um campo específico)
        if (ratingBarSafeScore != null) ratingBarSafeScore.setRating(driver.getNotaMotorista());
        if (btnConfirmRide != null) btnConfirmRide.setText("Confirmar Motorista"); // Ajusta texto do botão

        final AlertDialog driverDialog = builder.create();
        if (driverDialog.getWindow() != null) {
            driverDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        driverDialog.show();

        // Ação do botão de confirmação
        if (btnConfirmRide != null) {
            btnConfirmRide.setOnClickListener(v -> {
                driverDialog.dismiss();
                Log.d(TAG, "Motorista " + driver.getNome() + " confirmado para destino: " + destination);
                // Navega para a próxima tela (ex: checklist ou tela de corrida em progresso)
                navigateToSafetyChecklist(destination, driver.getNome(), ridePrice);
                Toast.makeText(HomeActivity.this, "Viagem com " + driver.getNome() + " confirmada!", Toast.LENGTH_SHORT).show();
            });
        } else {
            Log.e(TAG, "Botão de confirmação (buttonPayment) não encontrado no dialog_driver_info.");
        }
    }

    // Gera um preço aleatório para a corrida
    private String generateRandomPriceHome() {
        double price = 10.0 + new Random().nextDouble() * 40.0; // Preço entre R$10 e R$49.99
        return String.format(new Locale("pt", "BR"), "R$ %.2f", price);
    }

    // Navega para a tela de checklist (ou outra tela após confirmar motorista)
    private void navigateToSafetyChecklist(String destination, String driverName, String ridePrice) {
        // **CONFIRME:** MainActivity é a tela correta para o checklist do PASSAGEIRO?
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.putExtra("DESTINATION", destination);
        intent.putExtra("RIDE_PRICE", ridePrice);
        intent.putExtra("DRIVER_NAME", driverName);
        // Adicione mais dados do motorista se necessário (placa, modelo, etc.)
        // intent.putExtra("DRIVER_CAR_MODEL", driver.getModeloCarro());
        // intent.putExtra("DRIVER_LICENSE_PLATE", driver.getPlacaCarro());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    // Abre a tela de perfil
    private void openProfileActivity() {
        Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        // finish(); // Não finalizar para poder voltar
    }

    // Lida com o botão "Voltar" do Android
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setMessage("Deseja sair do aplicativo?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    finishAffinity(); // Fecha todas as activities do app
                })
                .setNegativeButton("Não", null) // Simplesmente fecha o diálogo
                .show();
    }
}
