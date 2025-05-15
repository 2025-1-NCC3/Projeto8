package br.fecap.pi.ubersafestart;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
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
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.StreetViewPanoramaView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.StreetViewSource;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import br.fecap.pi.ubersafestart.model.SimulatedUser;
import br.fecap.pi.ubersafestart.utils.StaticUserManager;

public class DriverHomeActivity extends AppCompatActivity {

    private static final String TAG = "DriverHomeActivity";
    private static final String MAPVIEW_BUNDLE_KEY = "mapViewBundle";
    private static final String USER_LOGIN_PREFS = "userPrefs";
    private static final String USER_LOCAL_PREFERENCES = "UserPreferences";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_SAME_GENDER_PAIRING = "sameGenderPairingEnabled";

    // Componentes da UI existentes
    private TextView textViewGreeting, textViewDriverName, textViewStatus;
    private Button buttonStart;
    private LinearLayout navAccount, navHomeDriverLayout, navEarningsLayout, navAchievementsDriverLayout; // Renomeado para clareza

    private boolean isOnline = false;
    private AlertDialog currentPassengerDialog = null;
    private AlertDialog loadingDialogInstance = null;
    private boolean isLoadingDialogActive = false;
    private Handler simulationHandler = new Handler(Looper.getMainLooper());
    private Runnable passengerFoundRunnable, showInfoRunnable;

    private StreetViewPanoramaView streetViewPickupView;
    private StreetViewPanoramaView streetViewDestinationView;
    private LatLng pickupLatLng;
    private LatLng destinationLatLng;
    private Bundle mapViewBundle = null;
    private ExecutorService geocodingExecutor = Executors.newSingleThreadExecutor();
    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private List<String> predefinedAddresses = new ArrayList<>();
    private Random random = new Random();

    // --- Novas variáveis para Detecção Facial Simulada (Verificação) ---
    private PreviewView previewViewFaceVerificationDriver; // ID: @+id/previewViewFaceVerificationDriverHome
    private ListenableFuture<ProcessCameraProvider> cameraProviderFutureVerificationDriver;
    private ProcessCameraProvider cameraProviderVerificationDriver;
    private FaceDetector faceDetectorVerificationDriver;
    private static final int REQUEST_CAMERA_PERMISSION_DRIVER_HOME = 103; // Request code específico
    private boolean isProcessingFaceVerificationDriver = false;
    private ExecutorService cameraExecutorVerificationDriver;
    // Para guardar dados da corrida durante a verificação facial
    private SimulatedUser tempPassengerForVerification;
    private String tempPickupAddressForVerification;
    private String tempDestinationAddressForVerification;
    private String tempRidePriceForVerification;
    // --- Fim das novas variáveis ---


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);

        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        initializeAddressList();
        if (predefinedAddresses.size() < 2) {
            Log.e(TAG, "onCreate: ERRO CRÍTICO - Lista de endereços predefinidos tem menos de 2 itens!");
            Toast.makeText(this, "Erro: Dados de endereço insuficientes para simulação.", Toast.LENGTH_LONG).show();
        }

        initViews();
        initFaceVerificationViewsDriver();

        setGreetingByTime();
        loadDriverData();
        setupNavigationListeners();
        setupStartButton();

        cameraExecutorVerificationDriver = Executors.newSingleThreadExecutor();
        initFaceDetectorSdkForVerificationDriver();

        Log.d(TAG, "onCreate concluído.");
    }

    private void initializeAddressList() {
        predefinedAddresses.clear();
        predefinedAddresses.add("Alameda Santos, 1165, Jardim Paulista, São Paulo, SP");
        predefinedAddresses.add("Rua Haddock Lobo, 1626, Jardins, São Paulo, SP");
        predefinedAddresses.add("Avenida Brigadeiro Faria Lima, 4440, Itaim Bibi, São Paulo, SP");
        predefinedAddresses.add("Rua da Consolação, 2200, Consolação, São Paulo, SP");
        predefinedAddresses.add("Avenida Paulista, 1578, Bela Vista, São Paulo, SP");
        predefinedAddresses.add("Rua Augusta, 950, Consolação, São Paulo, SP");
        predefinedAddresses.add("Avenida Rebouças, 600, Pinheiros, São Paulo, SP");
    }

    private void initViews() {
        textViewGreeting = findViewById(R.id.textViewGreeting);
        textViewDriverName = findViewById(R.id.textViewDriverName);
        textViewStatus = findViewById(R.id.textViewStatus);
        buttonStart = findViewById(R.id.buttonStart);
        // IDs para LinearLayouts da navegação conforme activity_driver_home.xml
        navAccount = findViewById(R.id.navAccount); // O ID é navAccount
        navHomeDriverLayout = findViewById(R.id.navHome); // O ID é navHome
        navEarningsLayout = findViewById(R.id.navEarnings);
        navAchievementsDriverLayout = findViewById(R.id.navAchievements);
    }

    private void initFaceVerificationViewsDriver() {
        previewViewFaceVerificationDriver = findViewById(R.id.previewViewFaceVerificationDriverHome);
        if (previewViewFaceVerificationDriver == null) {
            Log.e(TAG, "PreviewView (previewViewFaceVerificationDriverHome) não encontrado no layout activity_driver_home.xml.");
        }
    }

    private void initFaceDetectorSdkForVerificationDriver() {
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .setMinFaceSize(0.25f)
                        .build();
        faceDetectorVerificationDriver = FaceDetection.getClient(options);
    }

    private void startFaceVerificationFlowDriver(SimulatedUser passenger, String pickupAddress, String destinationAddress, String ridePrice) {
        Log.d(TAG, "Iniciando fluxo de verificação facial para motorista.");
        this.tempPassengerForVerification = passenger;
        this.tempPickupAddressForVerification = pickupAddress;
        this.tempDestinationAddressForVerification = destinationAddress;
        this.tempRidePriceForVerification = ridePrice;

        SharedPreferences prefs = getSharedPreferences(USER_LOGIN_PREFS, MODE_PRIVATE);
        boolean isFaceRegistered = prefs.getBoolean(ProfileActivity.KEY_FACE_REGISTERED_PROTOTYPE, false);

        if (!isFaceRegistered) {
            Log.w(TAG, "Tentativa de verificação facial (motorista), mas nenhum rosto registrado (simulado).");
            new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                    .setTitle("Configuração Facial Necessária")
                    .setMessage("Para sua segurança e do passageiro, configure a verificação facial no seu perfil antes de aceitar corridas.")
                    .setPositiveButton("Ir para Perfil", (dialog, which) -> {
                        Intent intent = new Intent(DriverHomeActivity.this, ProfileActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    })
                    .setNegativeButton("Cancelar Viagem", (dialog, which) -> { // Mudado para Cancelar Viagem
                        Toast.makeText(DriverHomeActivity.this, "Registro facial necessário. Corrida não iniciada.", Toast.LENGTH_LONG).show();
                        // Reverter estado online, se aplicável
                        isOnline = false;
                        if(textViewStatus!=null) textViewStatus.setText("Você está offline");
                        if(buttonStart!=null) {
                            buttonStart.setText("INICIAR");
                            try {
                                buttonStart.setBackgroundColor(getResources().getColor(R.color.uber_blue, getTheme()));
                            } catch (Exception e) {
                                buttonStart.setBackgroundColor(0xFF2979FF); // Azul fallback
                            }
                        }
                    })
                    .setCancelable(false)
                    .show();
            return;
        }

        if (checkCameraPermission()) {
            isProcessingFaceVerificationDriver = true;
            startCameraForFaceVerificationDriver();
        } else {
            requestCameraPermission();
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_DRIVER_HOME);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION_DRIVER_HOME) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permissão de câmera concedida para DriverHomeActivity.");
                if (isProcessingFaceVerificationDriver) {
                    startCameraForFaceVerificationDriver();
                }
            } else {
                Toast.makeText(this, "Permissão de câmera negada. Não é possível verificar o rosto.", Toast.LENGTH_SHORT).show();
                isProcessingFaceVerificationDriver = false;
            }
        }
    }


    private void startCameraForFaceVerificationDriver() {
        if (previewViewFaceVerificationDriver == null) {
            Log.e(TAG, "PreviewView (previewViewFaceVerificationDriver) é nulo.");
            Toast.makeText(this, "Erro: Componente da câmera não encontrado.", Toast.LENGTH_SHORT).show();
            isProcessingFaceVerificationDriver = false;
            return;
        }
        Log.d(TAG, "Iniciando câmera para verificação facial do motorista...");
        previewViewFaceVerificationDriver.setVisibility(View.VISIBLE);
        View centerContent = findViewById(R.id.centerContent);
        if (centerContent != null) centerContent.setVisibility(View.GONE);


        cameraProviderFutureVerificationDriver = ProcessCameraProvider.getInstance(this);
        cameraProviderFutureVerificationDriver.addListener(() -> {
            try {
                cameraProviderVerificationDriver = cameraProviderFutureVerificationDriver.get();
                bindCameraUseCasesForVerificationDriver();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Falha ao obter ProcessCameraProvider para verificação (motorista).", e);
                runOnUiThread(() -> Toast.makeText(DriverHomeActivity.this, "Erro ao iniciar câmera para verificação.", Toast.LENGTH_SHORT).show());
                isProcessingFaceVerificationDriver = false;
                stopCameraAndHidePreviewVerificationDriver();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @androidx.camera.core.ExperimentalGetImage // Necessário para imageProxy.getImage()
    private void bindCameraUseCasesForVerificationDriver() {
        if (cameraProviderVerificationDriver == null) {
            Log.e(TAG, "CameraProviderVerificationDriver não inicializado.");
            isProcessingFaceVerificationDriver = false;
            return;
        }
        Preview preview = new Preview.Builder()
                .setTargetResolution(new Size(previewViewFaceVerificationDriver.getWidth() > 0 ? previewViewFaceVerificationDriver.getWidth() : 640,
                        previewViewFaceVerificationDriver.getHeight() > 0 ? previewViewFaceVerificationDriver.getHeight() : 480))
                .build();
        preview.setSurfaceProvider(previewViewFaceVerificationDriver.getSurfaceProvider());

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(640, 480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutorVerificationDriver, imageProxy -> {
            android.media.Image mediaImage = imageProxy.getImage();
            if (mediaImage != null && isProcessingFaceVerificationDriver) {
                InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                faceDetectorVerificationDriver.process(image)
                        .addOnSuccessListener(faces -> {
                            if (!faces.isEmpty() && isProcessingFaceVerificationDriver) {
                                Face face = faces.get(0);
                                boolean eyesOpen = (face.getLeftEyeOpenProbability() != null && face.getLeftEyeOpenProbability() > 0.3) &&
                                        (face.getRightEyeOpenProbability() != null && face.getRightEyeOpenProbability() > 0.3);
                                if (eyesOpen) {
                                    isProcessingFaceVerificationDriver = false; // Para o processamento imediato
                                    imageProxy.close(); // Fecha o proxy atual antes do delay

                                    runOnUiThread(() -> {
                                        Toast.makeText(DriverHomeActivity.this, "Rosto detectado. Verificando...", Toast.LENGTH_SHORT).show();
                                    });

                                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                        // Este bloco roda após o delay
                                        runOnUiThread(() -> {
                                            Toast.makeText(DriverHomeActivity.this, "Verificação facial (motorista - simulada) OK!", Toast.LENGTH_SHORT).show();
                                            stopCameraAndHidePreviewVerificationDriver();

                                            Log.d(TAG, "Verificação facial do motorista OK. Navegando para checklist.");
                                            if (tempPassengerForVerification != null) {
                                                actuallyNavigateToSafetyChecklist(
                                                        tempPassengerForVerification.getName(),
                                                        tempPickupAddressForVerification,
                                                        tempDestinationAddressForVerification,
                                                        tempRidePriceForVerification
                                                );
                                            } else {
                                                Log.e(TAG, "Erro: Dados temporários da corrida nulos (motorista).");
                                                // Tratar erro, talvez voltar ao estado offline
                                                isOnline = false;
                                                if(textViewStatus!=null) textViewStatus.setText("Você está offline");
                                                if(buttonStart!=null) buttonStart.setText("INICIAR");
                                            }
                                        });
                                        // Não precisa resetar isProcessingFaceVerificationDriver aqui, já foi resetado
                                    }, 2500); // Atraso de 2.5 segundos
                                    return; // Sai do listener do success
                                }
                            }
                            imageProxy.close();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Detecção facial (verificação motorista) falhou.", e);
                            imageProxy.close();
                            runOnUiThread(() -> {
                                Toast.makeText(DriverHomeActivity.this, "Falha na detecção facial.", Toast.LENGTH_SHORT).show();
                                stopCameraAndHidePreviewVerificationDriver();
                            });
                            isProcessingFaceVerificationDriver = false;
                        });
            } else {
                if(isProcessingFaceVerificationDriver) {
                    imageProxy.close();
                }
            }
        });

        try {
            cameraProviderVerificationDriver.unbindAll();
            cameraProviderVerificationDriver.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
        } catch (Exception e) {
            Log.e(TAG, "Falha ao vincular casos de uso (verificação motorista).", e);
            runOnUiThread(() -> Toast.makeText(DriverHomeActivity.this, "Não foi possível usar a câmera para verificação.", Toast.LENGTH_SHORT).show());
            stopCameraAndHidePreviewVerificationDriver();
            isProcessingFaceVerificationDriver = false;
        }
    }

    private void stopCameraAndHidePreviewVerificationDriver() {
        if (cameraProviderVerificationDriver != null) {
            cameraProviderVerificationDriver.unbindAll();
        }
        if (previewViewFaceVerificationDriver != null) {
            previewViewFaceVerificationDriver.setVisibility(View.GONE);
        }
        View centerContent = findViewById(R.id.centerContent);
        if (centerContent != null) centerContent.setVisibility(View.VISIBLE);
        isProcessingFaceVerificationDriver = false;
    }
    // --- Fim dos Métodos para Detecção Facial ---

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
        if(textViewGreeting != null) textViewGreeting.setText(greeting);
    }

    private void loadDriverData() {
        SharedPreferences prefs = getSharedPreferences(USER_LOGIN_PREFS, MODE_PRIVATE);
        String fullName = prefs.getString("username", "Motorista");
        String firstName = fullName.contains(" ") ? fullName.substring(0, fullName.indexOf(" ")) : fullName;
        if(textViewDriverName != null) textViewDriverName.setText(firstName);
    }

    private void setupNavigationListeners() {
        // Usando as variáveis de membro corretas para os LinearLayouts da navegação
        if (navAchievementsDriverLayout != null) {
            navAchievementsDriverLayout.setOnClickListener(v -> {
                Intent intent = new Intent(DriverHomeActivity.this, AchievementsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }

        if (navAccount != null) { // navAccount é o ID do LinearLayout para "Conta"
            navAccount.setOnClickListener(v -> {
                Intent intent = new Intent(DriverHomeActivity.this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }
        if (navHomeDriverLayout != null) { // navHomeDriverLayout refere-se ao LinearLayout com ID "navHome"
            navHomeDriverLayout.setOnClickListener(v -> Toast.makeText(DriverHomeActivity.this, "Você já está na página inicial", Toast.LENGTH_SHORT).show());
        }
        if (navEarningsLayout != null) {
            navEarningsLayout.setOnClickListener(v -> Toast.makeText(DriverHomeActivity.this, "Ganhos em desenvolvimento", Toast.LENGTH_SHORT).show());
        }
    }


    private void setupStartButton() {
        if (buttonStart == null) {
            Log.e(TAG, "setupStartButton: Botão Iniciar é nulo.");
            return;
        }
        buttonStart.setOnClickListener(v -> {
            if (!isOnline) {
                isOnline = true;
                if(textViewStatus!=null) textViewStatus.setText("Procurando passageiros...");
                buttonStart.setText("PARAR");
                try {
                    buttonStart.setBackgroundColor(ContextCompat.getColor(this, R.color.button_negative));
                } catch (Exception e) {
                    buttonStart.setBackgroundColor(0xFFFF0000); // Vermelho fallback
                }
                showSearchingPassengerDialog();
            } else {
                isOnline = false;
                if(textViewStatus!=null) textViewStatus.setText("Você está offline");
                buttonStart.setText("INICIAR");
                try {
                    buttonStart.setBackgroundColor(ContextCompat.getColor(this, R.color.uber_blue));
                } catch (Exception e) {
                    buttonStart.setBackgroundColor(0xFF2979FF); // Azul fallback
                }
                simulationHandler.removeCallbacksAndMessages(null);
                if (loadingDialogInstance != null && loadingDialogInstance.isShowing()) {
                    loadingDialogInstance.dismiss();
                }
                isLoadingDialogActive = false;
                if (currentPassengerDialog != null && currentPassengerDialog.isShowing()) {
                    currentPassengerDialog.dismiss();
                }
            }
        });
    }

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
        TextView statusTextDialog = loadingView.findViewById(R.id.textViewStatus); // Renomeado para evitar conflito
        ImageView checkmarkImage = loadingView.findViewById(R.id.imageViewCheckmark);

        if (statusTextDialog != null) statusTextDialog.setText("Procurando passageiros próximos...");
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (checkmarkImage != null) checkmarkImage.setVisibility(View.GONE);

        passengerFoundRunnable = () -> {
            if (!isOnline || !isLoadingDialogActive || loadingDialogInstance == null || !loadingDialogInstance.isShowing()) {
                isLoadingDialogActive = false;
                return;
            }

            SharedPreferences loginPrefs = getSharedPreferences(USER_LOGIN_PREFS, MODE_PRIVATE);
            String currentUserGender = loginPrefs.getString(KEY_GENDER, "").toUpperCase(Locale.ROOT);
            SharedPreferences localPrefs = getSharedPreferences(USER_LOCAL_PREFERENCES, Context.MODE_PRIVATE);
            boolean preferSameGender = localPrefs.getBoolean(KEY_SAME_GENDER_PAIRING, false);
            final SimulatedUser passenger = StaticUserManager.getRandomPassenger(currentUserGender, preferSameGender);

            if (passenger == null) {
                if (statusTextDialog != null) statusTextDialog.setText("Nenhum passageiro compatível.");
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(DriverHomeActivity.this, "Nenhum passageiro compatível no momento.", Toast.LENGTH_LONG).show();
                simulationHandler.postDelayed(() -> {
                    if (loadingDialogInstance != null && loadingDialogInstance.isShowing()) {
                        loadingDialogInstance.dismiss();
                    }
                    isLoadingDialogActive = false;
                }, 2000);
                return;
            }

            if (statusTextDialog != null) statusTextDialog.setText("Passageiro encontrado!");
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            if (checkmarkImage != null) checkmarkImage.setVisibility(View.VISIBLE);

            final String ridePrice = generateRandomPrice();
            final String pickupAddress = getRandomAddress(null);
            final String destinationAddress = getRandomAddress(pickupAddress);

            if (pickupAddress == null || destinationAddress == null) {
                Toast.makeText(this, "Erro ao gerar endereços.", Toast.LENGTH_SHORT).show();
                if (loadingDialogInstance.isShowing()) loadingDialogInstance.dismiss();
                isLoadingDialogActive = false;
                return;
            }

            showInfoRunnable = () -> {
                if (!isOnline || !isLoadingDialogActive) return;
                if (loadingDialogInstance != null && loadingDialogInstance.isShowing()) {
                    loadingDialogInstance.dismiss();
                }
                isLoadingDialogActive = false;
                if (isOnline) {
                    showPassengerInfoDialog(passenger, pickupAddress, destinationAddress, ridePrice);
                }
            };
            simulationHandler.postDelayed(showInfoRunnable, 1500);
        };
        simulationHandler.postDelayed(passengerFoundRunnable, 4000);
    }

    private void showPassengerInfoDialog(SimulatedUser passenger, String pickupAddress, String destinationAddress, String ridePrice) {
        if (currentPassengerDialog != null && currentPassengerDialog.isShowing()) {
            currentPassengerDialog.dismiss();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(DriverHomeActivity.this, R.style.AlertDialogTheme);
        View passengerInfoView = LayoutInflater.from(this).inflate(R.layout.dialog_passenger_info, null);
        builder.setView(passengerInfoView).setCancelable(true);

        TextView tvPassengerName = passengerInfoView.findViewById(R.id.textViewPassengerName);
        TextView tvPickupLocation = passengerInfoView.findViewById(R.id.textViewPickupLocation);
        TextView tvDestination = passengerInfoView.findViewById(R.id.textViewDestination);
        TextView tvPriceDialog = passengerInfoView.findViewById(R.id.textViewPrice);
        RatingBar safeScoreBar = passengerInfoView.findViewById(R.id.ratingBarSafeScore);
        Button btnAcceptRide = passengerInfoView.findViewById(R.id.buttonAcceptRide);

        FrameLayout pickupContainer = passengerInfoView.findViewById(R.id.street_view_pickup_frame_container);
        FrameLayout destContainer = passengerInfoView.findViewById(R.id.street_view_destination_frame_container);
        streetViewPickupView = passengerInfoView.findViewById(R.id.street_view_pickup_preview);
        streetViewDestinationView = passengerInfoView.findViewById(R.id.street_view_destination_preview);
        View pickupOverlay = passengerInfoView.findViewById(R.id.street_view_pickup_click_overlay);
        View destOverlay = passengerInfoView.findViewById(R.id.street_view_destination_click_overlay);
        ProgressBar pickupProgress = passengerInfoView.findViewById(R.id.pickup_street_view_progress);
        ProgressBar destProgress = passengerInfoView.findViewById(R.id.destination_street_view_progress);

        if (tvPassengerName != null) tvPassengerName.setText(passenger.getName());
        if (tvPickupLocation != null) tvPickupLocation.setText("Embarque: " + pickupAddress);
        if (tvDestination != null) tvDestination.setText("Destino: " + destinationAddress);
        if (tvPriceDialog != null) tvPriceDialog.setText(ridePrice);
        if (safeScoreBar != null) safeScoreBar.setRating(passenger.getPassengerSafeScore());


        if (pickupContainer != null) pickupContainer.setVisibility(View.VISIBLE);
        if (streetViewPickupView != null) streetViewPickupView.setVisibility(View.INVISIBLE);
        if (pickupOverlay != null) pickupOverlay.setVisibility(View.INVISIBLE);
        if (pickupProgress != null) pickupProgress.setVisibility(View.VISIBLE);

        if (destContainer != null) destContainer.setVisibility(View.VISIBLE);
        if (streetViewDestinationView != null) streetViewDestinationView.setVisibility(View.INVISIBLE);
        if (destOverlay != null) destOverlay.setVisibility(View.INVISIBLE);
        if (destProgress != null) destProgress.setVisibility(View.VISIBLE);

        pickupLatLng = null;
        destinationLatLng = null;

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
                            setupStreetViewPanorama(panorama, pickupLatLng, pickupContainer, "Embarque_Geo");
                            if (streetViewPickupView != null) try { streetViewPickupView.onResume(); } catch (Exception e) {Log.e(TAG, "SV Pickup onResume", e);}
                        });
                        pickupOverlay.setOnClickListener(v -> { if (pickupLatLng != null) openFullScreenStreetView(pickupLatLng); });
                    } catch (Exception e) { Log.e(TAG, "Erro ao criar/configurar SV Pickup", e); if (pickupContainer != null) pickupContainer.setVisibility(View.GONE); }
                } else if (pickupContainer != null) pickupContainer.setVisibility(View.GONE);
            } else if (pickupContainer != null) pickupContainer.setVisibility(View.GONE);
        });

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
                            setupStreetViewPanorama(panorama, destinationLatLng, destContainer, "Destino_Geo");
                            if (streetViewDestinationView != null) try { streetViewDestinationView.onResume(); } catch (Exception e) {Log.e(TAG, "SV Dest onResume", e);}
                        });
                        destOverlay.setOnClickListener(v -> { if (destinationLatLng != null) openFullScreenStreetView(destinationLatLng); });
                    } catch (Exception e) { Log.e(TAG, "Erro ao criar/configurar SV Destino", e); if (destContainer != null) destContainer.setVisibility(View.GONE); }
                } else if (destContainer != null) destContainer.setVisibility(View.GONE);
            } else if (destContainer != null) destContainer.setVisibility(View.GONE);
        });


        if (btnAcceptRide != null) {
            btnAcceptRide.setOnClickListener(v -> {
                Log.d(TAG, "Corrida com " + passenger.getName() + " aceita. Iniciando verificação facial do motorista...");
                if (currentPassengerDialog != null) currentPassengerDialog.dismiss();
                startFaceVerificationFlowDriver(passenger, pickupAddress, destinationAddress, ridePrice);
            });
        }

        currentPassengerDialog = builder.create();
        currentPassengerDialog.setOnDismissListener(dialog -> {
            if (streetViewPickupView != null) { try { streetViewPickupView.onPause(); streetViewPickupView.onDestroy(); } catch (Exception e) {Log.e(TAG, "Erro ao limpar SV Pickup", e);} }
            if (streetViewDestinationView != null) { try { streetViewDestinationView.onPause(); streetViewDestinationView.onDestroy(); } catch (Exception e) {Log.e(TAG, "Erro ao limpar SV Destino", e);} }
            streetViewPickupView = null;
            streetViewDestinationView = null;
            currentPassengerDialog = null;
        });
        if (currentPassengerDialog.getWindow() != null) {
            currentPassengerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        currentPassengerDialog.show();
    }

    private void setupStreetViewPanorama(com.google.android.gms.maps.StreetViewPanorama panorama, LatLng position, View container, String logType) {
        if (panorama == null || position == null) {
            if (container != null) container.setVisibility(View.GONE);
            return;
        }
        panorama.setUserNavigationEnabled(false);
        panorama.setPanningGesturesEnabled(false);
        panorama.setZoomGesturesEnabled(false);
        panorama.setStreetNamesEnabled(false);
        panorama.setPosition(position, 150, StreetViewSource.OUTDOOR);
        panorama.setOnStreetViewPanoramaChangeListener(location -> {
            if (container == null) return;
            if (location == null || location.links == null || location.position == null) {
                container.setVisibility(View.GONE);
            } else {
                container.setVisibility(View.VISIBLE);
            }
        });
    }

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

    private void actuallyNavigateToSafetyChecklist(String passengerName, String pickupLocStr, String destStr, String priceStr) {
        Intent intent = new Intent(this, DriverSafetyChecklistActivity.class);
        intent.putExtra("PASSENGER_NAME", passengerName);
        intent.putExtra("PICKUP_LOCATION", pickupLocStr);
        intent.putExtra("DESTINATION", destStr);
        intent.putExtra("RIDE_PRICE", priceStr);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        Toast.makeText(this, "Corrida com " + passengerName + " aceita! Verifique o checklist.", Toast.LENGTH_LONG).show();

        isOnline = false;
        if(textViewStatus!=null) textViewStatus.setText("Você está offline");
        if(buttonStart!=null) {
            buttonStart.setText("INICIAR");
            try {
                buttonStart.setBackgroundColor(ContextCompat.getColor(this,R.color.uber_blue));
            } catch (Exception e) {
                buttonStart.setBackgroundColor(0xFF2979FF);
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Activity onResume");
        if (previewViewFaceVerificationDriver != null && previewViewFaceVerificationDriver.getVisibility() == View.GONE && !isProcessingFaceVerificationDriver) {
            View centerContent = findViewById(R.id.centerContent);
            if(centerContent != null) centerContent.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "Activity onPause");
        super.onPause();
        if (previewViewFaceVerificationDriver != null && previewViewFaceVerificationDriver.getVisibility() == View.VISIBLE) {
            stopCameraAndHidePreviewVerificationDriver();
        }
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
        if (geocodingExecutor != null && !geocodingExecutor.isShutdown()) {
            geocodingExecutor.shutdownNow();
        }
        mapViewBundle = null;

        if (faceDetectorVerificationDriver != null) {
            faceDetectorVerificationDriver.close();
        }
        if (cameraExecutorVerificationDriver != null && !cameraExecutorVerificationDriver.isShutdown()) {
            cameraExecutorVerificationDriver.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (currentPassengerDialog != null && currentPassengerDialog.isShowing()) {
            if (streetViewPickupView != null) try { streetViewPickupView.onLowMemory(); } catch (Exception e) {Log.e(TAG,"SV Pickup LowMem",e);}
            if (streetViewDestinationView != null) try { streetViewDestinationView.onLowMemory(); } catch (Exception e) {Log.e(TAG,"SV Dest LowMem",e);}
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle bundleToSave = mapViewBundle != null ? new Bundle(mapViewBundle) : new Bundle();
        if (currentPassengerDialog != null && currentPassengerDialog.isShowing()) {
            if (streetViewPickupView != null) try { streetViewPickupView.onSaveInstanceState(bundleToSave); } catch (Exception e) {Log.e(TAG,"SV Pickup SaveState",e);}
            if (streetViewDestinationView != null) try { streetViewDestinationView.onSaveInstanceState(bundleToSave); } catch (Exception e) {Log.e(TAG,"SV Dest SaveState",e);}
        }
        outState.putBundle(MAPVIEW_BUNDLE_KEY, bundleToSave);
    }


    @Override
    public void onBackPressed() {
        if (previewViewFaceVerificationDriver != null && previewViewFaceVerificationDriver.getVisibility() == View.VISIBLE) {
            stopCameraAndHidePreviewVerificationDriver();
            // Reverter para o estado anterior (ex: mostrar diálogo de passageiro se aplicável ou ficar offline)
            if (currentPassengerDialog != null && !currentPassengerDialog.isShowing() && tempPassengerForVerification != null) {
                showPassengerInfoDialog(tempPassengerForVerification, tempPickupAddressForVerification, tempDestinationAddressForVerification, tempRidePriceForVerification);
            } else if (isOnline) {
                // Se estava online, mas a verificação foi cancelada, pode voltar ao estado de busca
                // ou simplesmente ficar online sem diálogo. Para simplificar, vamos apenas ficar online.
                if(textViewStatus!=null) textViewStatus.setText("Procurando passageiros..."); // Ou estado anterior
            } else {
                if(textViewStatus!=null) textViewStatus.setText("Você está offline");
            }
            return;
        }
        if (currentPassengerDialog != null && currentPassengerDialog.isShowing()) {
            currentPassengerDialog.dismiss();
        } else if (loadingDialogInstance != null && loadingDialogInstance.isShowing()) {
            isOnline = false;
            if(textViewStatus!=null) textViewStatus.setText("Você está offline");
            if(buttonStart!=null) {
                buttonStart.setText("INICIAR");
                try { buttonStart.setBackgroundColor(ContextCompat.getColor(this, R.color.uber_blue)); } catch (Exception e) {}
            }
            simulationHandler.removeCallbacksAndMessages(null);
            loadingDialogInstance.dismiss();
            isLoadingDialogActive = false;
        } else {
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

    private String generateRandomPrice() {
        double price = 10.0 + ThreadLocalRandom.current().nextDouble(50.0);
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(price);
    }

    private String getRandomAddress(String excludeAddress) {
        if (predefinedAddresses.isEmpty()) return "Endereço Indisponível";
        if (predefinedAddresses.size() == 1 && predefinedAddresses.get(0).equals(excludeAddress)) return "Destino Igual Origem";
        if (predefinedAddresses.size() == 1) return predefinedAddresses.get(0);

        String selectedAddress;
        int attempts = 0;
        do {
            selectedAddress = predefinedAddresses.get(random.nextInt(predefinedAddresses.size()));
            attempts++;
        } while (excludeAddress != null && selectedAddress.equals(excludeAddress) && attempts < predefinedAddresses.size() * 2);
        return selectedAddress;
    }

    private LatLng geocodeAddressSync(String addressString) {
        if (addressString == null || addressString.trim().isEmpty()) return null;
        if (!Geocoder.isPresent()) {
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
        return null;
    }

    private void geocodeAddressAsync(String addressString, GeocodingResultListener listener) {
        if (listener == null) return;
        if (geocodingExecutor == null || geocodingExecutor.isShutdown()) {
            geocodingExecutor = Executors.newSingleThreadExecutor();
        }
        try {
            geocodingExecutor.execute(() -> {
                final LatLng resultLatLng = geocodeAddressSync(addressString);
                mainThreadHandler.post(() -> listener.onResult(resultLatLng));
            });
        } catch (java.util.concurrent.RejectedExecutionException e) {
            Log.e(TAG, "Geocoding tarefa rejeitada.", e);
            mainThreadHandler.post(() -> listener.onResult(null));
        }
    }

    interface GeocodingResultListener {
        void onResult(LatLng coordinates);
    }
}
