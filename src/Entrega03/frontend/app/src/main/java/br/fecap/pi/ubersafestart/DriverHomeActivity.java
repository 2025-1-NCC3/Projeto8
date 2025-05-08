package br.fecap.pi.ubersafestart;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;
import com.google.android.gms.maps.model.StreetViewSource;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays; // Mantenha se usar para outras listas
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

// Importações necessárias para a lógica de pareamento
import br.fecap.pi.ubersafestart.model.SimulatedUser;
import br.fecap.pi.ubersafestart.utils.StaticUserManager;


public class DriverHomeActivity extends AppCompatActivity {

    private static final String TAG = "DriverHomeActivity";
    private static final String MAPVIEW_BUNDLE_KEY = "mapViewBundle";

    // Constantes para SharedPreferences (USAR AS MESMAS DEFINIDAS EM ProfileActivity)
    private static final String USER_LOGIN_PREFS = "userPrefs"; // Para dados de login (token, nome, genero do backend)
    private static final String USER_LOCAL_PREFERENCES = "UserPreferences"; // Para preferências locais (pareamento)
    private static final String KEY_GENDER = "genero"; // Chave para gênero do usuário logado
    private static final String KEY_SAME_GENDER_PAIRING = "sameGenderPairingEnabled"; // Chave para preferência local

    // Componentes da UI
    private TextView textViewGreeting, textViewDriverName, textViewStatus;
    private Button buttonStart;
    private LinearLayout navAccount, navHome, navEarnings, navActivity;

    // Controle de estado e simulação
    private boolean isOnline = false;
    private AlertDialog currentPassengerDialog = null;
    private AlertDialog loadingDialogInstance = null;
    private boolean isLoadingDialogActive = false;
    private Handler simulationHandler = new Handler(Looper.getMainLooper());
    private Runnable passengerFoundRunnable, showInfoRunnable; // Runnables para controlar a simulação

    // StreetView e Geocoding
    private StreetViewPanoramaView streetViewPickupView;
    private StreetViewPanoramaView streetViewDestinationView;
    private LatLng pickupLatLng;
    private LatLng destinationLatLng;
    private Bundle mapViewBundle = null; // Bundle para salvar/restaurar estado do StreetView
    private ExecutorService geocodingExecutor = Executors.newSingleThreadExecutor();
    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private List<String> predefinedAddresses = new ArrayList<>(); // Lista de endereços para simulação
    private Random random = new Random();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);

        // Restaura o estado do mapViewBundle se a activity foi recriada
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        initializeAddressList(); // Preenche a lista de endereços
        // Verifica se a lista de endereços tem itens suficientes
        if (predefinedAddresses.size() < 2) {
            Log.e(TAG, "onCreate: ERRO CRÍTICO - Lista de endereços predefinidos tem menos de 2 itens!");
            // Considerar mostrar um erro permanente ou desabilitar o botão "INICIAR"
            Toast.makeText(this, "Erro: Dados de endereço insuficientes para simulação.", Toast.LENGTH_LONG).show();
        }

        initViews(); // Inicializa as referências aos componentes da UI
        setGreetingByTime(); // Define a saudação (Bom dia, Boa tarde, Boa noite)
        loadDriverData(); // Carrega o nome do motorista logado
        setupNavigationListeners(); // Configura a barra de navegação inferior
        setupStartButton(); // Configura o botão "INICIAR"/"PARAR"
        Log.d(TAG, "onCreate concluído.");
    }

    // Inicializa a lista de endereços de ruas para simulação
    private void initializeAddressList() {
        predefinedAddresses.clear();
        // Adiciona endereços de exemplo
        predefinedAddresses.add("Alameda Santos, 1165, Jardim Paulista, São Paulo, SP");
        predefinedAddresses.add("Rua Haddock Lobo, 1626, Jardins, São Paulo, SP");
        predefinedAddresses.add("Avenida Brigadeiro Faria Lima, 4440, Itaim Bibi, São Paulo, SP");
        predefinedAddresses.add("Rua da Consolação, 2200, Consolação, São Paulo, SP");
        predefinedAddresses.add("Avenida Paulista, 1578, Bela Vista, São Paulo, SP");
        predefinedAddresses.add("Rua Augusta, 950, Consolação, São Paulo, SP");
        predefinedAddresses.add("Avenida Rebouças, 600, Pinheiros, São Paulo, SP");
        // Adicione mais endereços conforme necessário
        Log.d(TAG, "Lista de endereços de RUAS inicializada com " + predefinedAddresses.size() + " locais.");
    }

    // Encontra e atribui as views do layout às variáveis da classe
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

    // Define a saudação com base na hora atual
    private void setGreetingByTime() {
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hourOfDay >= 5 && hourOfDay < 12) {
            greeting = "Bom dia,";
        } else if (hourOfDay < 18) {
            greeting = "Boa tarde,";
        } else {
            greeting = "Boa noite,";
        }
        textViewGreeting.setText(greeting);
    }

    // Carrega o nome do motorista das SharedPreferences
    private void loadDriverData() {
        SharedPreferences prefs = getSharedPreferences(USER_LOGIN_PREFS, MODE_PRIVATE);
        String fullName = prefs.getString("username", "Motorista"); // Usa a chave correta
        // Extrai o primeiro nome para exibição
        String firstName = fullName.contains(" ") ? fullName.substring(0, fullName.indexOf(" ")) : fullName;
        textViewDriverName.setText(firstName);
    }

    // Configura os listeners para a barra de navegação inferior
    private void setupNavigationListeners() {
        navAccount.setOnClickListener(v -> {
            Intent intent = new Intent(DriverHomeActivity.this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        // Não faz nada ao clicar em Home, pois já está nela
        navHome.setOnClickListener(v -> Toast.makeText(DriverHomeActivity.this, "Você já está na página inicial", Toast.LENGTH_SHORT).show());
        // Toasts para funcionalidades em desenvolvimento
        navEarnings.setOnClickListener(v -> Toast.makeText(DriverHomeActivity.this, "Ganhos em desenvolvimento", Toast.LENGTH_SHORT).show());
        navActivity.setOnClickListener(v -> Toast.makeText(DriverHomeActivity.this, "Atividade em desenvolvimento", Toast.LENGTH_SHORT).show());
    }

    // Configura o botão INICIAR/PARAR
    private void setupStartButton() {
        if (buttonStart == null) {
            Log.e(TAG, "setupStartButton: Botão Iniciar é nulo.");
            return;
        }
        buttonStart.setOnClickListener(v -> {
            if (!isOnline) { // Se está offline, fica online
                isOnline = true;
                textViewStatus.setText("Procurando passageiros...");
                buttonStart.setText("PARAR");
                // Muda a cor do botão para indicar estado online/perigo
                try {
                    buttonStart.setBackgroundColor(getResources().getColor(R.color.button_negative, getTheme()));
                } catch (Exception e) {
                    buttonStart.setBackgroundColor(0xFFFF0000); // Vermelho como fallback
                }
                showSearchingPassengerDialog(); // Inicia a busca simulada
            } else { // Se está online, fica offline
                isOnline = false;
                textViewStatus.setText("Você está offline");
                buttonStart.setText("INICIAR");
                // Restaura a cor padrão do botão
                try {
                    buttonStart.setBackgroundColor(getResources().getColor(R.color.uber_blue, getTheme()));
                } catch (Exception e) {
                    buttonStart.setBackgroundColor(0xFF0000FF); // Azul como fallback
                }
                // Cancela qualquer simulação pendente e fecha diálogos
                simulationHandler.removeCallbacksAndMessages(null);
                if (loadingDialogInstance != null && loadingDialogInstance.isShowing()) {
                    loadingDialogInstance.dismiss();
                }
                isLoadingDialogActive = false; // Marca o diálogo de loading como inativo
                if (currentPassengerDialog != null && currentPassengerDialog.isShowing()) {
                    currentPassengerDialog.dismiss();
                }
            }
            Log.d(TAG, "Botão Iniciar/Parar clicado. Novo estado isOnline=" + isOnline);
        });
    }

    // Mostra o diálogo de "Procurando passageiros..."
    private void showSearchingPassengerDialog() {
        // Garante que não haja um diálogo de loading anterior aberto
        if (loadingDialogInstance != null && loadingDialogInstance.isShowing()) {
            loadingDialogInstance.dismiss();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(DriverHomeActivity.this, R.style.AlertDialogTheme);
        View loadingView = LayoutInflater.from(this).inflate(R.layout.dialog_ride_loading, null);
        builder.setView(loadingView).setCancelable(false); // Não pode cancelar clicando fora
        loadingDialogInstance = builder.create();
        // Define fundo transparente para o diálogo
        if (loadingDialogInstance.getWindow() != null) {
            loadingDialogInstance.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        loadingDialogInstance.show();
        isLoadingDialogActive = true; // Marca que o diálogo de loading está ativo

        // Referências aos componentes do diálogo de loading
        ProgressBar progressBar = loadingView.findViewById(R.id.progressBar);
        TextView statusText = loadingView.findViewById(R.id.textViewStatus);
        ImageView checkmarkImage = loadingView.findViewById(R.id.imageViewCheckmark);

        // Configuração inicial do diálogo
        if (statusText != null) statusText.setText("Procurando passageiros próximos...");
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (checkmarkImage != null) checkmarkImage.setVisibility(View.GONE);

        // Runnable que será executado após o delay de simulação de busca
        passengerFoundRunnable = () -> {
            // Verifica se ainda deve processar (motorista online, diálogo ativo)
            if (!isOnline || !isLoadingDialogActive || loadingDialogInstance == null || !loadingDialogInstance.isShowing()) {
                Log.d(TAG,"Busca cancelada ou diálogo fechado antes de encontrar passageiro.");
                isLoadingDialogActive = false; // Garante que está inativo
                return;
            }

            // 1. Obtém gênero e preferência do usuário atual (Motorista)
            SharedPreferences loginPrefs = getSharedPreferences(USER_LOGIN_PREFS, MODE_PRIVATE);
            String currentUserGender = loginPrefs.getString(KEY_GENDER, "").toUpperCase();

            SharedPreferences localPrefs = getSharedPreferences(USER_LOCAL_PREFERENCES, Context.MODE_PRIVATE);
            boolean preferSameGender = localPrefs.getBoolean(KEY_SAME_GENDER_PAIRING, false);

            Log.d(TAG, "Buscando passageiro. Gênero do motorista: " + currentUserGender + ", Prefere mesmo gênero: " + preferSameGender);

            // 2. Busca um PASSAGEIRO estático usando o StaticUserManager
            final SimulatedUser passenger = StaticUserManager.getRandomPassenger(currentUserGender, preferSameGender);

            // Verifica se um passageiro compatível foi encontrado
            if (passenger == null) {
                Log.w(TAG, "Nenhum passageiro compatível encontrado com os critérios.");
                if (statusText != null) statusText.setText("Nenhum passageiro compatível.");
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(DriverHomeActivity.this, "Nenhum passageiro compatível no momento.", Toast.LENGTH_LONG).show();
                // Fecha o diálogo de loading após um tempo
                simulationHandler.postDelayed(() -> {
                    if (loadingDialogInstance != null && loadingDialogInstance.isShowing()) {
                        loadingDialogInstance.dismiss();
                    }
                    isLoadingDialogActive = false;
                    // Opcional: Poderia voltar para offline ou continuar buscando
                }, 2000);
                return; // Interrompe a execução aqui
            }

            // Passageiro encontrado! Atualiza UI do diálogo de loading
            Log.d(TAG, "Passageiro encontrado: " + passenger.getNome() + " (" + passenger.getGenero() + ")");
            if (statusText != null) statusText.setText("Passageiro encontrado!");
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            if (checkmarkImage != null) checkmarkImage.setVisibility(View.VISIBLE);

            // Gera dados aleatórios para a corrida simulada
            final String ridePrice = generateRandomPrice();
            final String pickupAddress = getRandomAddress(null); // Endereço de embarque aleatório
            final String destinationAddress = getRandomAddress(pickupAddress); // Endereço de destino aleatório (diferente do embarque)

            // Verifica se os endereços foram gerados corretamente
            if (pickupAddress == null || destinationAddress == null) {
                Log.e(TAG,"Erro ao gerar endereços para o passageiro.");
                Toast.makeText(this, "Erro ao gerar endereços para o passageiro.", Toast.LENGTH_SHORT).show();
                if (loadingDialogInstance.isShowing()) loadingDialogInstance.dismiss();
                isLoadingDialogActive = false;
                // Poderia reverter para offline
                return;
            }

            // Runnable para mostrar o diálogo de informações do passageiro após um pequeno delay
            showInfoRunnable = () -> {
                if (!isOnline || !isLoadingDialogActive) { // Verifica novamente se ainda é válido mostrar
                    Log.d(TAG,"Não mostrar info do passageiro: motorista offline ou loading cancelado.");
                    return;
                }
                if (loadingDialogInstance != null && loadingDialogInstance.isShowing()) {
                    loadingDialogInstance.dismiss(); // Fecha o diálogo de loading
                }
                isLoadingDialogActive = false; // Marca como inativo
                if (isOnline) { // Confirma se ainda está online antes de mostrar o diálogo final
                    showPassengerInfoDialog(passenger, pickupAddress, destinationAddress, ridePrice);
                }
            };
            // Agenda a exibição do diálogo de info do passageiro
            simulationHandler.postDelayed(showInfoRunnable, 1500); // Delay de 1.5s para mostrar o checkmark
        };
        // Agenda a execução da lógica de encontrar passageiro
        simulationHandler.postDelayed(passengerFoundRunnable, 4000); // Delay de 4s simulando a busca
    }


    // Mostra o diálogo com informações do passageiro encontrado
    private void showPassengerInfoDialog(SimulatedUser passenger, String pickupAddress, String destinationAddress, String ridePrice) {
        // Garante que não haja um diálogo anterior aberto
        if (currentPassengerDialog != null && currentPassengerDialog.isShowing()) {
            currentPassengerDialog.dismiss();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(DriverHomeActivity.this, R.style.AlertDialogTheme);
        // Infla o layout dialog_passenger_info.xml
        View passengerInfoView = LayoutInflater.from(this).inflate(R.layout.dialog_passenger_info, null);
        builder.setView(passengerInfoView).setCancelable(true); // Permite cancelar clicando fora

        // Encontra os componentes dentro do layout inflado
        TextView tvPassengerName = passengerInfoView.findViewById(R.id.textViewPassengerName);
        TextView tvPickupLocation = passengerInfoView.findViewById(R.id.textViewPickupLocation);
        TextView tvDestination = passengerInfoView.findViewById(R.id.textViewDestination);
        TextView tvPriceDialog = passengerInfoView.findViewById(R.id.textViewPrice);
        RatingBar safeScoreBar = passengerInfoView.findViewById(R.id.ratingBarSafeScore);
        Button btnAcceptRide = passengerInfoView.findViewById(R.id.buttonAcceptRide);

        // Referências para o StreetView
        FrameLayout pickupContainer = passengerInfoView.findViewById(R.id.street_view_pickup_frame_container);
        FrameLayout destContainer = passengerInfoView.findViewById(R.id.street_view_destination_frame_container);
        streetViewPickupView = passengerInfoView.findViewById(R.id.street_view_pickup_preview);
        streetViewDestinationView = passengerInfoView.findViewById(R.id.street_view_destination_preview);
        View pickupOverlay = passengerInfoView.findViewById(R.id.street_view_pickup_click_overlay);
        View destOverlay = passengerInfoView.findViewById(R.id.street_view_destination_click_overlay);
        ProgressBar pickupProgress = passengerInfoView.findViewById(R.id.pickup_street_view_progress);
        ProgressBar destProgress = passengerInfoView.findViewById(R.id.destination_street_view_progress);

        // Popula os componentes com os dados do passageiro simulado
        if (tvPassengerName != null) tvPassengerName.setText(passenger.getNome());
        if (tvPickupLocation != null) tvPickupLocation.setText("Embarque: " + pickupAddress);
        if (tvDestination != null) tvDestination.setText("Destino: " + destinationAddress);
        if (tvPriceDialog != null) tvPriceDialog.setText(ridePrice);
        if (safeScoreBar != null) safeScoreBar.setRating(passenger.getSafeScorePassageiro());

        // Configuração inicial dos previews do StreetView (invisíveis até carregar)
        if (pickupContainer != null) pickupContainer.setVisibility(View.VISIBLE);
        if (streetViewPickupView != null) streetViewPickupView.setVisibility(View.INVISIBLE);
        if (pickupOverlay != null) pickupOverlay.setVisibility(View.INVISIBLE);
        if (pickupProgress != null) pickupProgress.setVisibility(View.VISIBLE); // Mostra loading

        if (destContainer != null) destContainer.setVisibility(View.VISIBLE);
        if (streetViewDestinationView != null) streetViewDestinationView.setVisibility(View.INVISIBLE);
        if (destOverlay != null) destOverlay.setVisibility(View.INVISIBLE);
        if (destProgress != null) destProgress.setVisibility(View.VISIBLE); // Mostra loading

        pickupLatLng = null; // Reseta coordenadas
        destinationLatLng = null;

        // Inicia o geocoding assíncrono para obter coordenadas dos endereços
        geocodeAddressAsync(pickupAddress, coords -> {
            if (pickupProgress != null) pickupProgress.setVisibility(View.GONE); // Esconde loading
            if (coords != null) {
                pickupLatLng = coords;
                // Configura o StreetView de embarque se as coordenadas foram encontradas
                if (streetViewPickupView != null && pickupContainer != null && pickupOverlay != null) {
                    try {
                        streetViewPickupView.setVisibility(View.VISIBLE);
                        pickupOverlay.setVisibility(View.VISIBLE);
                        streetViewPickupView.onCreate(mapViewBundle); // Passa o bundle para o StreetView
                        streetViewPickupView.getStreetViewPanoramaAsync(panorama -> {
                            setupStreetViewPanorama(panorama, pickupLatLng, pickupContainer, "Embarque_Geo");
                            // Chama onResume após o panorama estar pronto
                            if (streetViewPickupView != null) try { streetViewPickupView.onResume(); } catch (Exception e) {Log.e(TAG, "SV Pickup onResume", e);}
                        });
                        // Listener para abrir tela cheia ao clicar no preview
                        pickupOverlay.setOnClickListener(v -> { if (pickupLatLng != null) openFullScreenStreetView(pickupLatLng); });
                    } catch (Exception e) { Log.e(TAG, "Erro ao criar/configurar SV Pickup", e); if (pickupContainer != null) pickupContainer.setVisibility(View.GONE); }
                } else if (pickupContainer != null) pickupContainer.setVisibility(View.GONE); // Esconde se algo falhar
            } else if (pickupContainer != null) pickupContainer.setVisibility(View.GONE); // Esconde se geocoding falhar
        });

        geocodeAddressAsync(destinationAddress, coords -> {
            if (destProgress != null) destProgress.setVisibility(View.GONE); // Esconde loading
            if (coords != null) {
                destinationLatLng = coords;
                // Configura o StreetView de destino se as coordenadas foram encontradas
                if (streetViewDestinationView != null && destContainer != null && destOverlay != null) {
                    try {
                        streetViewDestinationView.setVisibility(View.VISIBLE);
                        destOverlay.setVisibility(View.VISIBLE);
                        streetViewDestinationView.onCreate(mapViewBundle); // Passa o bundle
                        streetViewDestinationView.getStreetViewPanoramaAsync(panorama -> {
                            setupStreetViewPanorama(panorama, destinationLatLng, destContainer, "Destino_Geo");
                            // Chama onResume após o panorama estar pronto
                            if (streetViewDestinationView != null) try { streetViewDestinationView.onResume(); } catch (Exception e) {Log.e(TAG, "SV Dest onResume", e);}
                        });
                        // Listener para abrir tela cheia ao clicar no preview
                        destOverlay.setOnClickListener(v -> { if (destinationLatLng != null) openFullScreenStreetView(destinationLatLng); });
                    } catch (Exception e) { Log.e(TAG, "Erro ao criar/configurar SV Destino", e); if (destContainer != null) destContainer.setVisibility(View.GONE); }
                } else if (destContainer != null) destContainer.setVisibility(View.GONE); // Esconde se algo falhar
            } else if (destContainer != null) destContainer.setVisibility(View.GONE); // Esconde se geocoding falhar
        });


        // Ação do botão "Aceitar Corrida"
        if (btnAcceptRide != null) {
            btnAcceptRide.setOnClickListener(v -> {
                Log.d(TAG, "Corrida com " + passenger.getNome() + " aceita.");
                navigateToSafetyChecklist(passenger.getNome(), pickupAddress, destinationAddress, ridePrice);
                if (currentPassengerDialog != null) currentPassengerDialog.dismiss(); // Fecha o diálogo atual
            });
        } else {
            Log.e(TAG, "Botão 'Aceitar Corrida' não encontrado no layout.");
        }


        // Cria e exibe o diálogo
        currentPassengerDialog = builder.create();
        // Listener para limpar recursos do StreetView quando o diálogo for fechado
        currentPassengerDialog.setOnDismissListener(dialog -> {
            Log.d(TAG, "Diálogo de info do passageiro fechado. Limpando StreetViews.");
            if (streetViewPickupView != null) { try { streetViewPickupView.onPause(); streetViewPickupView.onDestroy(); } catch (Exception e) {Log.e(TAG, "Erro ao limpar SV Pickup", e);} }
            if (streetViewDestinationView != null) { try { streetViewDestinationView.onPause(); streetViewDestinationView.onDestroy(); } catch (Exception e) {Log.e(TAG, "Erro ao limpar SV Destino", e);} }
            streetViewPickupView = null; // Libera referências
            streetViewDestinationView = null;
            currentPassengerDialog = null; // Libera referência ao diálogo
        });
        if (currentPassengerDialog.getWindow() != null) {
            currentPassengerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        currentPassengerDialog.show();
    }

    // Configura um painel do StreetView
    private void setupStreetViewPanorama(StreetViewPanorama panorama, LatLng position, View container, String logType) {
        if (panorama == null || position == null) {
            Log.e(TAG, "setupStreetViewPanorama: Panorama ou Posição nulos para " + logType);
            if (container != null) container.setVisibility(View.GONE);
            return;
        }
        // Desabilita interações do usuário no preview
        panorama.setUserNavigationEnabled(false);
        panorama.setPanningGesturesEnabled(false);
        panorama.setZoomGesturesEnabled(false);
        panorama.setStreetNamesEnabled(false);
        // Tenta encontrar uma imagem outdoor próxima (raio de 150m)
        panorama.setPosition(position, 150, StreetViewSource.OUTDOOR);
        // Listener para verificar se uma imagem foi encontrada
        panorama.setOnStreetViewPanoramaChangeListener(location -> {
            if (container == null) return;
            // Se não encontrou imagem válida, esconde o container
            if (location == null || location.links == null || location.position == null) {
                Log.w(TAG, "SVChangeListener (" + logType + "): Nenhum panorama válido encontrado. Escondendo container.");
                container.setVisibility(View.GONE);
            } else {
                Log.d(TAG, "SVChangeListener (" + logType + "): Panorama válido. Container VISIBLE.");
                container.setVisibility(View.VISIBLE); // Garante visibilidade se encontrou
            }
        });
        Log.d(TAG, "StreetView " + logType + " configurado para: " + position);
    }

    // Abre a activity de visualização em tela cheia do StreetView
    private void openFullScreenStreetView(LatLng coordinates) {
        if (coordinates == null) {
            Toast.makeText(this, "Localização inválida", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, FullScreenStreetViewActivity.class);
        intent.putExtra("latitude", coordinates.latitude);
        intent.putExtra("longitude", coordinates.longitude);
        startActivity(intent);
    }

    // Navega para a tela de Checklist de Segurança do Motorista
    private void navigateToSafetyChecklist(String passengerName, String pickupLocStr, String destStr, String priceStr) {
        Intent intent = new Intent(this, DriverSafetyChecklistActivity.class);
        intent.putExtra("PASSENGER_NAME", passengerName);
        intent.putExtra("PICKUP_LOCATION", pickupLocStr);
        intent.putExtra("DESTINATION", destStr);
        intent.putExtra("RIDE_PRICE", priceStr);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        Toast.makeText(this, "Corrida com " + passengerName + " aceita! Verifique o checklist.", Toast.LENGTH_LONG).show();

        // Importante: Volta para o estado offline após aceitar a corrida
        isOnline = false;
        textViewStatus.setText("Você está offline");
        buttonStart.setText("INICIAR");
        try {
            buttonStart.setBackgroundColor(getResources().getColor(R.color.uber_blue, getTheme()));
        } catch (Exception e) {
            buttonStart.setBackgroundColor(0xFF0000FF); // Azul fallback
        }
    }

    // --- Métodos do Ciclo de Vida da Activity ---

    @Override
    protected void onResume() {
        super.onResume();
        // O onResume dos StreetViews é chamado dinamicamente após o panorama estar pronto
        Log.d(TAG, "Activity onResume");
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "Activity onPause");
        // A pausa dos StreetViews agora é feita no onDismissListener do diálogo
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Activity onDestroy");
        simulationHandler.removeCallbacksAndMessages(null); // Cancela timers pendentes
        // Garante que os diálogos sejam fechados e limpos
        if (loadingDialogInstance != null && loadingDialogInstance.isShowing()) {
            loadingDialogInstance.dismiss();
        }
        if (currentPassengerDialog != null && currentPassengerDialog.isShowing()) {
            currentPassengerDialog.dismiss(); // Isso chamará o onDismissListener para limpar os SVs
        }
        // Desliga o executor de geocoding
        if (geocodingExecutor != null && !geocodingExecutor.isShutdown()) {
            geocodingExecutor.shutdownNow();
        }
        mapViewBundle = null; // Limpa o bundle
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        // Tenta liberar memória dos StreetViews se o diálogo estiver aberto
        if (currentPassengerDialog != null && currentPassengerDialog.isShowing()) {
            if (streetViewPickupView != null) try { streetViewPickupView.onLowMemory(); } catch (Exception e) {Log.e(TAG,"SV Pickup LowMem",e);}
            if (streetViewDestinationView != null) try { streetViewDestinationView.onLowMemory(); } catch (Exception e) {Log.e(TAG,"SV Dest LowMem",e);}
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Salva o estado do StreetView se o diálogo estiver aberto
        Bundle bundleToSave = mapViewBundle != null ? new Bundle(mapViewBundle) : new Bundle();
        if (currentPassengerDialog != null && currentPassengerDialog.isShowing()) {
            if (streetViewPickupView != null) try { streetViewPickupView.onSaveInstanceState(bundleToSave); } catch (Exception e) {Log.e(TAG,"SV Pickup SaveState",e);}
            if (streetViewDestinationView != null) try { streetViewDestinationView.onSaveInstanceState(bundleToSave); } catch (Exception e) {Log.e(TAG,"SV Dest SaveState",e);}
        }
        // Salva o bundle no estado da activity
        outState.putBundle(MAPVIEW_BUNDLE_KEY, bundleToSave);
    }

    @Override
    public void onBackPressed() {
        // Se o diálogo de info do passageiro estiver aberto, fecha ele primeiro
        if (currentPassengerDialog != null && currentPassengerDialog.isShowing()) {
            currentPassengerDialog.dismiss();
        }
        // Se o diálogo de loading estiver aberto, cancela a busca e fecha ele
        else if (loadingDialogInstance != null && loadingDialogInstance.isShowing()) {
            Log.d(TAG, "Botão Voltar pressionado durante a busca. Cancelando...");
            isOnline = false; // Fica offline
            textViewStatus.setText("Você está offline");
            buttonStart.setText("INICIAR");
            try { buttonStart.setBackgroundColor(getResources().getColor(R.color.uber_blue, getTheme())); } catch (Exception e) {}
            simulationHandler.removeCallbacksAndMessages(null); // Cancela timers
            loadingDialogInstance.dismiss();
            isLoadingDialogActive = false;
        }
        // Senão, executa o comportamento padrão (fechar activity)
        else {
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right); // Animação de saída
        }
    }

    // --- Métodos Utilitários ---

    // Gera um preço aleatório para a corrida
    private String generateRandomPrice() {
        double price = 10.0 + ThreadLocalRandom.current().nextDouble(50.0); // Entre R$10 e R$59.99
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(price);
    }

    // Retorna um endereço aleatório da lista, opcionalmente excluindo um endereço específico
    private String getRandomAddress(String excludeAddress) {
        if (predefinedAddresses.isEmpty()) return "Endereço Indisponível";
        if (predefinedAddresses.size() == 1) return predefinedAddresses.get(0);

        String selectedAddress;
        int attempts = 0; // Para evitar loop infinito
        do {
            selectedAddress = predefinedAddresses.get(random.nextInt(predefinedAddresses.size()));
            attempts++;
            // Continua se o endereço selecionado for o excluído E houver mais de um endereço E não tentou demais
        } while (excludeAddress != null && selectedAddress.equals(excludeAddress) && predefinedAddresses.size() > 1 && attempts < predefinedAddresses.size() * 2);
        return selectedAddress;
    }

    // Converte um endereço em String para coordenadas LatLng (executado em background)
    private LatLng geocodeAddressSync(String addressString) {
        if (addressString == null || addressString.trim().isEmpty()) return null;
        if (!Geocoder.isPresent()) {
            Log.e(TAG, "Geocoder indisponível");
            return null;
        }
        Geocoder geocoder = new Geocoder(this, new Locale("pt", "BR"));
        try {
            // Tenta obter UMA coordenada para o endereço
            List<Address> addresses = geocoder.getFromLocationName(addressString, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address location = addresses.get(0);
                // Verifica se a localização encontrada tem latitude e longitude
                if (location.hasLatitude() && location.hasLongitude()) {
                    return new LatLng(location.getLatitude(), location.getLongitude());
                } else {
                    Log.w(TAG, "Endereço encontrado mas sem coordenadas para: " + addressString);
                }
            } else {
                Log.w(TAG, "Nenhum endereço encontrado para: " + addressString);
            }
        } catch (IOException e) {
            // Erro comum se houver problema de rede ou cota excedida
            Log.e(TAG, "Geocoding ERRO I/O para '" + addressString + "'", e);
        } catch (Exception e) {
            // Outros erros inesperados
            Log.e(TAG, "Geocoding ERRO inesperado para '" + addressString + "'", e);
        }
        Log.w(TAG, "Geocoding FALHOU para '" + addressString + "'");
        return null; // Retorna null se falhar
    }

    // Executa o geocoding em uma thread separada para não bloquear a UI
    private void geocodeAddressAsync(String addressString, GeocodingResultListener listener) {
        if (listener == null) return; // Não faz nada se não houver listener
        // Verifica se o executor está ativo, tenta recriar se não estiver
        if (geocodingExecutor == null || geocodingExecutor.isShutdown()) {
            Log.w(TAG, "Geocoding executor não está ativo, tentando recriar.");
            geocodingExecutor = Executors.newSingleThreadExecutor();
        }
        try {
            // Submete a tarefa de geocoding para a thread do executor
            geocodingExecutor.execute(() -> {
                final LatLng resultLatLng = geocodeAddressSync(addressString);
                // Retorna o resultado para a thread principal (UI)
                mainThreadHandler.post(() -> listener.onResult(resultLatLng));
            });
        } catch (java.util.concurrent.RejectedExecutionException e) {
            // Erro se o executor estiver desligando enquanto a tarefa é submetida
            Log.e(TAG, "Geocoding tarefa rejeitada, executor pode estar desligando.", e);
            // Notifica falha na thread principal
            mainThreadHandler.post(() -> listener.onResult(null));
        }
    }

    // Interface para retornar o resultado do geocoding assíncrono
    interface GeocodingResultListener {
        void onResult(LatLng coordinates);
    }
}
