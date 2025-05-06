package br.fecap.pi.ubersafestart; // Verifique se o nome do seu pacote está correto

// --- IMPORTS NECESSÁRIOS ---

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
import android.widget.RatingBar; // Import restaurado
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;
import com.google.android.gms.maps.model.StreetViewSource;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
// --- FIM IMPORTS ---

public class DriverHomeActivity extends AppCompatActivity {

    // --- VARIÁVEIS E CONSTANTES ---
    private static final String TAG = "DriverHomeActivity";
    private static final String MAPVIEW_BUNDLE_KEY = "mapViewBundle";
    private TextView textViewGreeting, textViewDriverName, textViewStatus;
    private Button buttonStart;
    private LinearLayout navAccount, navHome, navEarnings, navActivity;
    private boolean isOnline = false;
    private StreetViewPanoramaView streetViewPickupView;
    private StreetViewPanoramaView streetViewDestinationView;
    private StreetViewPanorama streetViewPickupPanorama;
    private StreetViewPanorama streetViewDestinationPanorama;
    private LatLng pickupLatLng;
    private LatLng destinationLatLng;
    private AlertDialog currentPassengerDialog = null;
    private Bundle mapViewBundle = null;
    private Handler simulationHandler = new Handler(Looper.getMainLooper());
    private Runnable passengerFoundRunnable, showInfoRunnable;
    private AlertDialog loadingDialogInstance = null;
    private boolean isLoadingDialogActive = false;
    private List<String> predefinedAddresses = new ArrayList<>();
    private Random random = new Random();
    private ExecutorService geocodingExecutor = Executors.newSingleThreadExecutor();
    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private List<String> firstNames = Arrays.asList("Ana", "Carlos", "Beatriz", "Daniel", "Eduarda", "Fábio", "Gabriela", "Hugo", "Isabela", "João", "Larissa", "Marcos", "Natália", "Otávio", "Patrícia", "Rafael", "Sofia", "Thiago", "Vitória", "Alice", "Bruno", "Clara");
    private List<String> lastNames = Arrays.asList("Silva", "Santos", "Oliveira", "Souza", "Rodrigues", "Ferreira", "Alves", "Pereira", "Lima", "Gomes", "Ribeiro", "Martins", "Carvalho", "Almeida", "Costa", "Melo", "Araújo");
    // --- FIM VARIÁVEIS ---


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);

        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        initializeAddressList(); // Usa a lista refinada de endereços de ruas
        Log.d(TAG, "onCreate: Lista de endereços preenchida com " + predefinedAddresses.size() + " itens.");
        if (predefinedAddresses.size() < 2) {
            Log.e(TAG, "onCreate: ERRO CRÍTICO - Lista de endereços predefinidos tem menos de 2 itens! Geração de corridas falhará.");
            // Considerar desabilitar o botão "INICIAR" ou mostrar um Toast persistente
        }

        initViews();
        setGreetingByTime();
        loadDriverData();
        setupNavigationListeners();
        setupStartButton();
        Log.d(TAG, "onCreate concluído.");
    }

    // Lista de endereços de ruas
    private void initializeAddressList() {
        predefinedAddresses.clear();
        Log.d(TAG, "Inicializando lista de endereços de RUAS pré-definidos...");
        predefinedAddresses.add("Alameda Santos, 1165, Jardim Paulista, São Paulo, SP");
        predefinedAddresses.add("Rua Haddock Lobo, 1626, Jardins, São Paulo, SP");
        predefinedAddresses.add("Avenida Brigadeiro Faria Lima, 4440, Itaim Bibi, São Paulo, SP");
        predefinedAddresses.add("Rua da Consolação, 2200, Consolação, São Paulo, SP");
        predefinedAddresses.add("Avenida Paulista, 1578, Bela Vista, São Paulo, SP");
        predefinedAddresses.add("Rua Augusta, 950, Consolação, São Paulo, SP");
        predefinedAddresses.add("Avenida Rebouças, 600, Pinheiros, São Paulo, SP");
        predefinedAddresses.add("Rua Oscar Freire, 974, Jardins, São Paulo, SP");
        predefinedAddresses.add("Avenida Engenheiro Luís Carlos Berrini, 1748, Cidade Monções, São Paulo, SP");
        predefinedAddresses.add("Rua Vergueiro, 1500, Paraíso, São Paulo, SP");
        predefinedAddresses.add("Avenida Ibirapuera, 3103, Moema, São Paulo, SP");
        predefinedAddresses.add("Rua Teodoro Sampaio, 1200, Pinheiros, São Paulo, SP");
        predefinedAddresses.add("Avenida Cruzeiro do Sul, 1800, Santana, São Paulo, SP");
        predefinedAddresses.add("Rua Pamplona, 800, Jardim Paulista, São Paulo, SP");
        predefinedAddresses.add("Avenida Doutor Chucri Zaidan, 902, Vila Cordeiro, São Paulo, SP");
        predefinedAddresses.add("Rua Frei Caneca, 569, Consolação, São Paulo, SP");
        predefinedAddresses.add("Avenida Angélica, 2466, Consolação, São Paulo, SP");
        predefinedAddresses.add("Rua Bela Cintra, 1500, Consolação, São Paulo, SP");
        predefinedAddresses.add("Avenida Washington Luís, 6973, Santo Amaro, São Paulo, SP");
        predefinedAddresses.add("Rua Itapeva, 500, Bela Vista, São Paulo, SP");
        predefinedAddresses.add("Avenida Brigadeiro Luís Antônio, 4000, Jardim Paulista, São Paulo, SP");
        predefinedAddresses.add("Rua Maria Antônia, 294, Vila Buarque, São Paulo, SP");
        Log.d(TAG, "Lista de endereços de RUAS inicializada com " + predefinedAddresses.size() + " locais.");
    }

    // --- Métodos Essenciais (initViews, setGreetingByTime, etc.) ---
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
        String greeting = (hourOfDay >= 5 && hourOfDay < 12) ? "Bom dia," : (hourOfDay < 18 ? "Boa tarde," : "Boa noite,");
        textViewGreeting.setText(greeting);
    }

    private void loadDriverData() {
        SharedPreferences prefs = getSharedPreferences("userPrefs", MODE_PRIVATE);
        String fullName = prefs.getString("username", "Motorista");
        String firstName = fullName.contains(" ") ? fullName.substring(0, fullName.indexOf(" ")) : fullName;
        textViewDriverName.setText(firstName);
    }

    private void setupNavigationListeners() {
        navAccount.setOnClickListener(v -> {
            Intent intent = new Intent(DriverHomeActivity.this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        navHome.setOnClickListener(v -> Toast.makeText(DriverHomeActivity.this, "Você já está na página inicial", Toast.LENGTH_SHORT).show());
        navEarnings.setOnClickListener(v -> Toast.makeText(DriverHomeActivity.this, "Ganhos em breve", Toast.LENGTH_SHORT).show());
        navActivity.setOnClickListener(v -> Toast.makeText(DriverHomeActivity.this, "Atividade em breve", Toast.LENGTH_SHORT).show());
    }

    private void setupStartButton() {
        if (buttonStart == null) {
            Log.e(TAG, "setupStartButton: Botão Iniciar é nulo.");
            return;
        }
        buttonStart.setOnClickListener(v -> {
            if (!isOnline) {
                isOnline = true;
                textViewStatus.setText("Procurando passageiros...");
                buttonStart.setText("PARAR");
                try {
                    buttonStart.setBackgroundColor(getResources().getColor(R.color.button_negative, getTheme()));
                } catch (Exception e) {
                    buttonStart.setBackgroundColor(0xFFFF0000); // Vermelho
                }
                showSearchingPassengerDialog();
            } else {
                isOnline = false;
                textViewStatus.setText("Você está offline");
                buttonStart.setText("INICIAR");
                try {
                    buttonStart.setBackgroundColor(getResources().getColor(R.color.uber_blue, getTheme()));
                } catch (Exception e) {
                    buttonStart.setBackgroundColor(0xFF0000FF); // Azul
                }
                simulationHandler.removeCallbacksAndMessages(null); // Cancela todas as simulações
                if (loadingDialogInstance != null && loadingDialogInstance.isShowing()) {
                    loadingDialogInstance.dismiss();
                }
                if (currentPassengerDialog != null && currentPassengerDialog.isShowing()) {
                    currentPassengerDialog.dismiss();
                }
            }
            Log.d(TAG, "Botão Iniciar/Parar clicado. Novo estado isOnline=" + isOnline);
        });
    }

    // --- Lógica de Simulação de Corrida ---
    private void showSearchingPassengerDialog() {
        if (loadingDialogInstance != null && loadingDialogInstance.isShowing()) {
            loadingDialogInstance.dismiss();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(DriverHomeActivity.this, R.style.AlertDialogTheme);
        View loadingView = LayoutInflater.from(this).inflate(R.layout.dialog_ride_loading, null);
        builder.setView(loadingView).setCancelable(false);
        loadingDialogInstance = builder.create();
        if (loadingDialogInstance.getWindow() != null) {
            loadingDialogInstance.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        loadingDialogInstance.show();
        isLoadingDialogActive = true;

        ProgressBar progressBar = loadingView.findViewById(R.id.progressBar);
        TextView statusText = loadingView.findViewById(R.id.textViewStatus);
        ImageView checkmarkImage = loadingView.findViewById(R.id.imageViewCheckmark);
        statusText.setText("Procurando passageiros próximos...");
        progressBar.setVisibility(View.VISIBLE);
        checkmarkImage.setVisibility(View.GONE);

        passengerFoundRunnable = () -> {
            if (!isOnline || !isLoadingDialogActive || loadingDialogInstance == null || !loadingDialogInstance.isShowing())
                return;
            statusText.setText("Passageiro encontrado!");
            progressBar.setVisibility(View.GONE);
            checkmarkImage.setVisibility(View.VISIBLE);

            String passengerName = generateRandomName();
            String ridePrice = generateRandomPrice();
            String pickupAddress = getRandomAddress(null);
            String destinationAddress = getRandomAddress(pickupAddress);

            if (pickupAddress == null || destinationAddress == null) {
                Toast.makeText(this, "Erro ao gerar endereços.", Toast.LENGTH_SHORT).show();
                if (loadingDialogInstance.isShowing()) loadingDialogInstance.dismiss();
                // Reverter para offline
                isOnline = false;
                textViewStatus.setText("Erro");
                buttonStart.setText("INICIAR");
                try {
                    buttonStart.setBackgroundColor(getResources().getColor(R.color.uber_blue, getTheme()));
                } catch (Exception e) {
                }
                return;
            }

            showInfoRunnable = () -> {
                if (!isOnline || !isLoadingDialogActive) return;
                if (loadingDialogInstance != null && loadingDialogInstance.isShowing())
                    loadingDialogInstance.dismiss();
                isLoadingDialogActive = false; // Marcar como inativo após fechar
                if (isOnline) {
                    showPassengerInfoDialog(passengerName, pickupAddress, destinationAddress, ridePrice);
                }
            };
            simulationHandler.postDelayed(showInfoRunnable, 1500);
        };
        simulationHandler.postDelayed(passengerFoundRunnable, 4000);
    }

    private void showPassengerInfoDialog(String passengerName, String pickupAddress, String destinationAddress, String ridePrice) {
        if (currentPassengerDialog != null && currentPassengerDialog.isShowing()) {
            currentPassengerDialog.dismiss();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(DriverHomeActivity.this, R.style.AlertDialogTheme);
        View passengerInfoView = LayoutInflater.from(this).inflate(R.layout.dialog_passenger_info, null);
        builder.setView(passengerInfoView).setCancelable(true);

        TextView tvPassengerName = passengerInfoView.findViewById(R.id.textViewPassengerName);
        TextView tvPickupLocation = passengerInfoView.findViewById(R.id.textViewPickupLocation);
        TextView tvDestination = passengerInfoView.findViewById(R.id.textViewDestination);
        TextView tvPrice = passengerInfoView.findViewById(R.id.textViewPrice);
        RatingBar safeScoreBar = passengerInfoView.findViewById(R.id.ratingBarSafeScore); // Restaurado
        Button btnAcceptRide = passengerInfoView.findViewById(R.id.buttonAcceptRide);
        FrameLayout pickupContainer = passengerInfoView.findViewById(R.id.street_view_pickup_frame_container);
        FrameLayout destContainer = passengerInfoView.findViewById(R.id.street_view_destination_frame_container);
        streetViewPickupView = passengerInfoView.findViewById(R.id.street_view_pickup_preview);
        streetViewDestinationView = passengerInfoView.findViewById(R.id.street_view_destination_preview);
        View pickupOverlay = passengerInfoView.findViewById(R.id.street_view_pickup_click_overlay);
        View destOverlay = passengerInfoView.findViewById(R.id.street_view_destination_click_overlay);
        ProgressBar pickupProgress = passengerInfoView.findViewById(R.id.pickup_street_view_progress);
        ProgressBar destProgress = passengerInfoView.findViewById(R.id.destination_street_view_progress);

        tvPassengerName.setText(passengerName);
        tvPickupLocation.setText("Embarque: " + pickupAddress); // Rótulo adicionado
        tvDestination.setText("Destino: " + destinationAddress);
        tvPrice.setText(ridePrice);
        if (safeScoreBar != null) safeScoreBar.setRating(generateRandomRating()); // Restaurado

        // Configuração inicial dos StreetViews
        if (pickupContainer != null) pickupContainer.setVisibility(View.VISIBLE);
        if (streetViewPickupView != null) streetViewPickupView.setVisibility(View.INVISIBLE);
        if (pickupOverlay != null) pickupOverlay.setVisibility(View.INVISIBLE);
        if (pickupProgress != null) pickupProgress.setVisibility(View.VISIBLE);

        if (destContainer != null) destContainer.setVisibility(View.VISIBLE);
        if (streetViewDestinationView != null)
            streetViewDestinationView.setVisibility(View.INVISIBLE);
        if (destOverlay != null) destOverlay.setVisibility(View.INVISIBLE);
        if (destProgress != null) destProgress.setVisibility(View.VISIBLE);

        pickupLatLng = null;
        destinationLatLng = null;

        // Geocoding para Embarque
        geocodeAddressAsync(pickupAddress, coords -> {
            if (pickupProgress != null) pickupProgress.setVisibility(View.GONE);
            if (coords != null) {
                pickupLatLng = coords;
                if (streetViewPickupView != null && pickupContainer != null && pickupOverlay != null) {
                    try {
                        streetViewPickupView.setVisibility(View.VISIBLE);
                        pickupOverlay.setVisibility(View.VISIBLE);
                        streetViewPickupView.onCreate(mapViewBundle);
                        streetViewPickupView.getStreetViewPanoramaAsync(panorama -> {
                            streetViewPickupPanorama = panorama;
                            setupStreetViewPanorama(panorama, pickupLatLng, pickupContainer, "Embarque_Geo");
                            if (streetViewPickupView != null) try {
                                streetViewPickupView.onResume();
                            } catch (Exception e) {
                            }
                        });
                        pickupOverlay.setOnClickListener(v -> {
                            if (pickupLatLng != null) openFullScreenStreetView(pickupLatLng);
                        });
                    } catch (Exception e) {
                        if (pickupContainer != null) pickupContainer.setVisibility(View.GONE);
                    }
                } else if (pickupContainer != null) pickupContainer.setVisibility(View.GONE);
            } else if (pickupContainer != null) pickupContainer.setVisibility(View.GONE);
        });

        // Geocoding para Destino
        geocodeAddressAsync(destinationAddress, coords -> {
            if (destProgress != null) destProgress.setVisibility(View.GONE);
            if (coords != null) {
                destinationLatLng = coords;
                if (streetViewDestinationView != null && destContainer != null && destOverlay != null) {
                    try {
                        streetViewDestinationView.setVisibility(View.VISIBLE);
                        destOverlay.setVisibility(View.VISIBLE);
                        streetViewDestinationView.onCreate(mapViewBundle);
                        streetViewDestinationView.getStreetViewPanoramaAsync(panorama -> {
                            streetViewDestinationPanorama = panorama;
                            setupStreetViewPanorama(panorama, destinationLatLng, destContainer, "Destino_Geo");
                            if (streetViewDestinationView != null) try {
                                streetViewDestinationView.onResume();
                            } catch (Exception e) {
                            }
                        });
                        destOverlay.setOnClickListener(v -> {
                            if (destinationLatLng != null)
                                openFullScreenStreetView(destinationLatLng);
                        });
                    } catch (Exception e) {
                        if (destContainer != null) destContainer.setVisibility(View.GONE);
                    }
                } else if (destContainer != null) destContainer.setVisibility(View.GONE);
            } else if (destContainer != null) destContainer.setVisibility(View.GONE);
        });

        btnAcceptRide.setOnClickListener(v -> {
            navigateToSafetyChecklist(passengerName, pickupAddress, destinationAddress, ridePrice);
            if (currentPassengerDialog != null) currentPassengerDialog.dismiss();
        });

        currentPassengerDialog = builder.create();
        currentPassengerDialog.setOnDismissListener(dialog -> {
            // Limpeza dos StreetViews
            if (streetViewPickupView != null) {
                try {
                    streetViewPickupView.onPause();
                    streetViewPickupView.onDestroy();
                } catch (Exception e) {
                }
            }
            if (streetViewDestinationView != null) {
                try {
                    streetViewDestinationView.onPause();
                    streetViewDestinationView.onDestroy();
                } catch (Exception e) {
                }
            }
            streetViewPickupView = null;
            streetViewDestinationView = null;
            currentPassengerDialog = null;
            mapViewBundle = null; // Limpa o bundle também
        });
        if (currentPassengerDialog.getWindow() != null) {
            currentPassengerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        currentPassengerDialog.show();
    }

    // Configura um panorama do StreetView
    private void setupStreetViewPanorama(StreetViewPanorama panorama, LatLng position, View container, String logType) {
        if (panorama == null || position == null) {
            Log.e(TAG, "setupStreetViewPanorama: Panorama ou Posição nulos para " + logType);
            if (container != null) container.setVisibility(View.GONE);
            return;
        }
        panorama.setUserNavigationEnabled(false);
        panorama.setPanningGesturesEnabled(false);
        panorama.setZoomGesturesEnabled(false);
        panorama.setStreetNamesEnabled(false);
        panorama.setPosition(position, 50, StreetViewSource.OUTDOOR);
        panorama.setOnStreetViewPanoramaChangeListener(location -> handlePanoramaChange(location, container, logType));
        Log.d(TAG, "StreetView " + logType + " configurado para: " + position);
    }

    // Manipula mudanças no panorama (ex: se não encontrar imagem)
    private void handlePanoramaChange(StreetViewPanoramaLocation location, View container, String type) {
        if (container == null) return;
        if (location == null || location.links == null) {
            Log.w(TAG, "SVChangeListener (" + type + "): Nenhum panorama encontrado. Escondendo container.");
            container.setVisibility(View.GONE);
        } else {
            Log.d(TAG, "SVChangeListener (" + type + "): Panorama válido. Container VISIBLE.");
            container.setVisibility(View.VISIBLE);
        }
    }

    // Abre a tela cheia do StreetView
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

    // Navega para o Checklist de Segurança
    private void navigateToSafetyChecklist(String passengerName, String pickupLocStr, String destStr, String priceStr) {
        Intent intent = new Intent(this, DriverSafetyChecklistActivity.class);
        intent.putExtra("PASSENGER_NAME", passengerName);
        intent.putExtra("PICKUP_LOCATION", pickupLocStr);
        intent.putExtra("DESTINATION", destStr);
        intent.putExtra("RIDE_PRICE", priceStr);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        Toast.makeText(this, "Corrida aceita! Verifique o checklist.", Toast.LENGTH_LONG).show();
        // Volta para offline
        isOnline = false;
        textViewStatus.setText("Você está offline");
        buttonStart.setText("INICIAR");
        try {
            buttonStart.setBackgroundColor(getResources().getColor(R.color.uber_blue, getTheme()));
        } catch (Exception e) {
        }
    }

    // --- Ciclo de Vida da Activity ---
    @Override
    protected void onResume() {
        super.onResume();
        // O onResume dos StreetViewPanoramaView é chamado dentro do getStreetViewPanoramaAsync
        // para garantir que o panorama esteja pronto.
        Log.d(TAG, "Activity onResume");
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "Activity onPause");
        // Pausa os StreetViews se estiverem no diálogo e ativos
        if (currentPassengerDialog != null && currentPassengerDialog.isShowing()) {
            if (streetViewPickupView != null) {
                try {
                    streetViewPickupView.onPause();
                } catch (Exception e) {
                }
            }
            if (streetViewDestinationView != null) {
                try {
                    streetViewDestinationView.onPause();
                } catch (Exception e) {
                }
            }
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Activity onDestroy");
        simulationHandler.removeCallbacksAndMessages(null);
        if (loadingDialogInstance != null && loadingDialogInstance.isShowing()) {
            loadingDialogInstance.dismiss();
        }
        if (currentPassengerDialog != null && currentPassengerDialog.isShowing()) {
            currentPassengerDialog.dismiss();
        }
        // A limpeza dos StreetViews é feita no onDismissListener do currentPassengerDialog
        if (geocodingExecutor != null && !geocodingExecutor.isShutdown()) {
            geocodingExecutor.shutdownNow(); // Tenta parar tarefas imediatamente
        }
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (currentPassengerDialog != null && currentPassengerDialog.isShowing()) {
            if (streetViewPickupView != null) {
                try {
                    streetViewPickupView.onLowMemory();
                } catch (Exception e) {
                }
            }
            if (streetViewDestinationView != null) {
                try {
                    streetViewDestinationView.onLowMemory();
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Salva o estado dos StreetViews se estiverem no diálogo e ativos
        if (currentPassengerDialog != null && currentPassengerDialog.isShowing()) {
            Bundle bundleToSave = (mapViewBundle != null) ? mapViewBundle : new Bundle();
            if (streetViewPickupView != null) {
                try {
                    streetViewPickupView.onSaveInstanceState(bundleToSave);
                } catch (Exception e) {
                }
            }
            if (streetViewDestinationView != null) {
                try {
                    streetViewDestinationView.onSaveInstanceState(bundleToSave);
                } catch (Exception e) {
                }
            }
            outState.putBundle(MAPVIEW_BUNDLE_KEY, bundleToSave);
        }
    }

    @Override
    public void onBackPressed() {
        if (currentPassengerDialog != null && currentPassengerDialog.isShowing()) {
            currentPassengerDialog.dismiss();
        } else if (loadingDialogInstance != null && loadingDialogInstance.isShowing()) {
            isOnline = false;
            textViewStatus.setText("Você está offline");
            buttonStart.setText("INICIAR");
            try {
                buttonStart.setBackgroundColor(getResources().getColor(R.color.uber_blue, getTheme()));
            } catch (Exception e) {
            }
            simulationHandler.removeCallbacksAndMessages(null);
            loadingDialogInstance.dismiss();
        } else {
            super.onBackPressed();
        }
    }

    // --- Métodos de Geração Aleatória ---
    private String generateRandomName() {
        if (firstNames.isEmpty() || lastNames.isEmpty()) return "Passageiro";
        return firstNames.get(random.nextInt(firstNames.size())) + " " + lastNames.get(random.nextInt(lastNames.size()));
    }

    private String generateRandomPrice() {
        double price = 10.0 + ThreadLocalRandom.current().nextDouble(50.0); // Entre 10 e 59.99
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(price);
    }

    private float generateRandomRating() { // Para o SafeScore
        return 3.5f + random.nextFloat() * 1.5f; // Entre 3.5 e 5.0
    }

    // --- Métodos de Endereço e Geocoding ---
    private String getRandomAddress(String excludeAddress) {
        if (predefinedAddresses.isEmpty()) return null;
        if (predefinedAddresses.size() < 2 && excludeAddress != null && predefinedAddresses.get(0).equals(excludeAddress)) {
            return predefinedAddresses.get(0); // Retorna o único mesmo que seja para excluir
        }
        if (predefinedAddresses.size() == 1) return predefinedAddresses.get(0);

        String selectedAddress;
        do {
            selectedAddress = predefinedAddresses.get(random.nextInt(predefinedAddresses.size()));
        } while (excludeAddress != null && selectedAddress.equals(excludeAddress) && predefinedAddresses.size() > 1);
        return selectedAddress;
    }

    private LatLng geocodeAddressSync(String addressString) {
        if (addressString == null || addressString.trim().isEmpty()) return null;
        if (!Geocoder.isPresent()) {
            Log.e(TAG, "Geocoder indisponível");
            return null;
        }
        Geocoder geocoder = new Geocoder(this, new Locale("pt", "BR"));
        try {
            List<Address> addresses = geocoder.getFromLocationName(addressString, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address location = addresses.get(0);
                if (location.hasLatitude() && location.hasLongitude()) {
                    return new LatLng(location.getLatitude(), location.getLongitude());
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoding ERRO I/O para '" + addressString + "'", e);
        } catch (Exception e) {
            Log.e(TAG, "Geocoding ERRO inesperado para '" + addressString + "'", e);
        }
        Log.w(TAG, "Geocoding FALHOU para '" + addressString + "'");
        return null;
    }

    private void geocodeAddressAsync(String addressString, GeocodingResultListener listener) {
        if (listener == null) return;
        if (geocodingExecutor == null || geocodingExecutor.isShutdown()) {
            mainThreadHandler.post(() -> listener.onResult(null)); // Notifica falha se executor não estiver ok
            return;
        }
        geocodingExecutor.execute(() -> {
            final LatLng resultLatLng = geocodeAddressSync(addressString);
            mainThreadHandler.post(() -> listener.onResult(resultLatLng));
        });
    }

    interface GeocodingResultListener {
        void onResult(LatLng coordinates);
    }

}
