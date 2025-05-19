package br.fecap.pi.ubersafestart;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FaceEnrollmentActivity extends AppCompatActivity {

    private static final String TAG = "FaceEnrollmentActivity";
    private static final int REQUEST_CAMERA_PERMISSION_ENROLLMENT = 104;
    private static final String USER_LOGIN_PREFS = "userPrefs";

    private PreviewView previewViewFaceEnrollment;
    private Button buttonScanFace;
    private ProgressBar progressBarFaceEnrollment;
    private Toolbar toolbarFaceEnrollment;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ProcessCameraProvider cameraProvider;
    private FaceDetector faceDetector;
    private ExecutorService cameraExecutor;
    private boolean isScanningFace = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_enrollment);

        previewViewFaceEnrollment = findViewById(R.id.previewViewFaceEnrollment);
        buttonScanFace = findViewById(R.id.buttonScanFace);
        progressBarFaceEnrollment = findViewById(R.id.progressBarFaceEnrollment);
        toolbarFaceEnrollment = findViewById(R.id.toolbarFaceEnrollment);

        setupToolbar();
        setupCameraComponents();
        setupScanButton();

        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbarFaceEnrollment);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbarFaceEnrollment.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupCameraComponents() {
        cameraExecutor = Executors.newSingleThreadExecutor();
        initFaceDetectorSdk();
    }

    private void setupScanButton() {
        buttonScanFace.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                if (!isScanningFace) {
                    Log.d(TAG, "Iniciando câmera e detecção facial.");
                    isScanningFace = true;
                    buttonScanFace.setEnabled(false);
                    buttonScanFace.setText("Escaneando...");
                    progressBarFaceEnrollment.setVisibility(View.VISIBLE);
                    startCamera();
                }
            } else {
                requestCameraPermission();
            }
        });
    }

    private void initFaceDetectorSdk() {
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .setMinFaceSize(0.25f)
                        .build();
        faceDetector = FaceDetection.getClient(options);
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_ENROLLMENT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION_ENROLLMENT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permissão de câmera concedida para FaceEnrollmentActivity.");
                startCamera();
            } else {
                Toast.makeText(this, "Permissão de câmera negada. Não é possível registrar o rosto.", Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }

    private void startCamera() {
        if (previewViewFaceEnrollment == null) {
            Log.e(TAG, "PreviewView é nulo.");
            return;
        }
        previewViewFaceEnrollment.setVisibility(View.VISIBLE);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Falha ao obter ProcessCameraProvider.", e);
                runOnUiThread(()-> Toast.makeText(this, "Erro ao iniciar câmera.", Toast.LENGTH_SHORT).show());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @androidx.camera.core.ExperimentalGetImage
    private void bindCameraUseCases() {
        if (cameraProvider == null) {
            Log.e(TAG, "CameraProvider não inicializado.");
            return;
        }

        Preview preview = new Preview.Builder()
                .setTargetResolution(new Size(previewViewFaceEnrollment.getWidth() > 0 ? previewViewFaceEnrollment.getWidth() : 640,
                        previewViewFaceEnrollment.getHeight() > 0 ? previewViewFaceEnrollment.getHeight() : 480))
                .build();
        preview.setSurfaceProvider(previewViewFaceEnrollment.getSurfaceProvider());

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(640, 480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
            if (!isScanningFace) {
                imageProxy.close();
                return;
            }

            android.media.Image mediaImage = imageProxy.getImage();
            if (mediaImage != null) {
                InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                faceDetector.process(image)
                        .addOnSuccessListener(faces -> {
                            if (!faces.isEmpty() && isScanningFace) {
                                Face face = faces.get(0);
                                boolean eyesOpen = (face.getLeftEyeOpenProbability() != null && face.getLeftEyeOpenProbability() > 0.3) &&
                                        (face.getRightEyeOpenProbability() != null && face.getRightEyeOpenProbability() > 0.3);

                                if (eyesOpen) {
                                    Log.d(TAG, "Rosto detectado com olhos abertos.");
                                    runOnUiThread(() -> {
                                        Toast.makeText(FaceEnrollmentActivity.this, "Rosto registrado com sucesso!", Toast.LENGTH_SHORT).show();
                                        SharedPreferences prefs = getSharedPreferences(USER_LOGIN_PREFS, MODE_PRIVATE);
                                        prefs.edit().putBoolean(ProfileActivity.KEY_FACE_REGISTERED_PROTOTYPE, true).apply();

                                        setResult(RESULT_OK);
                                        finishAndStopCamera();
                                    });
                                    isScanningFace = false;
                                }
                            }
                            imageProxy.close();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Detecção facial falhou no processamento.", e);
                            runOnUiThread(() -> {
                                Toast.makeText(FaceEnrollmentActivity.this, "Falha na detecção. Tente novamente.", Toast.LENGTH_SHORT).show();
                                resetScanButton();
                            });
                            imageProxy.close();
                        });
            } else {
                imageProxy.close();
            }
        });

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
            Log.d(TAG, "Casos de uso da câmera vinculados.");
        } catch (Exception e) {
            Log.e(TAG, "Falha ao vincular casos de uso da câmera.", e);
            runOnUiThread(() -> {
                Toast.makeText(this, "Não foi possível usar a câmera.", Toast.LENGTH_SHORT).show();
                resetScanButton();
            });
        }
    }

    private void resetScanButton() {
        isScanningFace = false;
        if (buttonScanFace != null) {
            buttonScanFace.setEnabled(true);
            buttonScanFace.setText("Escanear Rosto");
        }
        if (progressBarFaceEnrollment != null) {
            progressBarFaceEnrollment.setVisibility(View.GONE);
        }
    }

    private void finishAndStopCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        if (cameraExecutor != null && !cameraExecutor.isShutdown()) {
            cameraExecutor.shutdown();
        }
        if (faceDetector != null) {
            faceDetector.close();
        }
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finishAndStopCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isScanningFace || (cameraProvider != null && previewViewFaceEnrollment.getVisibility() == View.VISIBLE)) {
            stopCameraAndResetUI();
        }
    }

    private void stopCameraAndResetUI() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            Log.d(TAG, "Câmera desvinculada.");
        }
        resetScanButton();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null && !cameraExecutor.isShutdown()) {
            cameraExecutor.shutdown();
        }
        if (faceDetector != null) {
            faceDetector.close();
        }
    }
}