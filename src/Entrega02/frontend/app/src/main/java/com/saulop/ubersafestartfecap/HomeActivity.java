package com.saulop.ubersafestartfecap;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class HomeActivity extends AppCompatActivity {

    private TextView textViewLocationName1;
    private TextView textViewLocationAddress1;
    private TextView textViewLocationName2;
    private TextView textViewLocationAddress2;
    private EditText editTextSearch;
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
        loadLocationHistoryData();
        setupSearchListener();
        setupNavigationListeners();
        setupRecentLocationsListeners();
    }

    private void initViews() {
        textViewLocationName1 = findViewById(R.id.textViewLocationName1);
        textViewLocationAddress1 = findViewById(R.id.textViewLocationAddress1);
        textViewLocationName2 = findViewById(R.id.textViewLocationName2);
        textViewLocationAddress2 = findViewById(R.id.textViewLocationAddress2);
        editTextSearch = findViewById(R.id.editTextSearch);

        navAccount = findViewById(R.id.navAccount);
        navHome = findViewById(R.id.navHome);
        navServices = findViewById(R.id.navServices);
        navActivity = findViewById(R.id.navActivity);

        cardViewRecentLocation1 = findViewById(R.id.cardViewRecentLocation1);
        cardViewRecentLocation2 = findViewById(R.id.cardViewRecentLocation2);
    }

    private void loadLocationHistoryData() {
        String location1Name = "Avenida das Rosas";
        String location1Address = "Av. das Rosas - Jardim Primavera";
        String location2Name = "Rua Bento Silveira, 1234";
        String location2Address = "Nova Esperança - MG, 35700-000";

        textViewLocationName1.setText(location1Name);
        textViewLocationAddress1.setText(location1Address);
        textViewLocationName2.setText(location2Name);
        textViewLocationAddress2.setText(location2Address);
    }

    private void setupSearchListener() {
        editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                String destination = editTextSearch.getText().toString().trim();
                if (!destination.isEmpty()) {
                    showLoadingDialog(destination);
                    return true;
                }
            }
            return false;
        });
    }

    private void setupNavigationListeners() {
        navAccount.setOnClickListener(v -> openProfileActivity());

        navHome.setOnClickListener(v -> {
            Toast.makeText(HomeActivity.this, "Você já está na página inicial", Toast.LENGTH_SHORT).show();
        });

        navServices.setOnClickListener(v -> {
            Toast.makeText(HomeActivity.this, "Opções em breve", Toast.LENGTH_SHORT).show();
        });

        navActivity.setOnClickListener(v -> {
            Toast.makeText(HomeActivity.this, "Atividade em breve", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRecentLocationsListeners() {
        cardViewRecentLocation1.setOnClickListener(v -> {
            String destination = textViewLocationName1.getText().toString();
            showLoadingDialog(destination);
        });

        cardViewRecentLocation2.setOnClickListener(v -> {
            String destination = textViewLocationName2.getText().toString();
            showLoadingDialog(destination);
        });
    }

    private void showLoadingDialog(final String destination) {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        View loadingView = LayoutInflater.from(this).inflate(R.layout.dialog_ride_loading, null);
        builder.setView(loadingView);
        builder.setCancelable(false);

        final AlertDialog loadingDialog = builder.create();
        if (loadingDialog.getWindow() != null) {
            loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        loadingDialog.show();

        ProgressBar progressBar = loadingView.findViewById(R.id.progressBar);
        TextView statusText = loadingView.findViewById(R.id.textViewStatus);
        ImageView checkmarkImage = loadingView.findViewById(R.id.imageViewCheckmark);

        new Handler().postDelayed(() -> {
            statusText.setText("Motorista encontrado!");
            progressBar.setVisibility(View.GONE);
            checkmarkImage.setVisibility(View.VISIBLE);

            new Handler().postDelayed(() -> {
                loadingDialog.dismiss();
                showDriverInfoDialog(destination);
            }, 1000);
        }, 3000);
    }
    private void showDriverInfoDialog(String destination) {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        View driverInfoView = LayoutInflater.from(this).inflate(R.layout.dialog_driver_info, null);
        builder.setView(driverInfoView);
        builder.setCancelable(true);

        TextView tvDriverName = driverInfoView.findViewById(R.id.textViewDriverName);
        TextView tvCarInfo = driverInfoView.findViewById(R.id.textViewCarInfo);
        TextView tvPrice = driverInfoView.findViewById(R.id.textViewPrice);
        TextView tvDestination = driverInfoView.findViewById(R.id.textViewDestination);
        RatingBar ratingBar = driverInfoView.findViewById(R.id.ratingBarDriver);
        RatingBar safeScoreBar = driverInfoView.findViewById(R.id.ratingBarSafeScore);
        Button btnPayment = driverInfoView.findViewById(R.id.buttonPayment);

        String driverName = "João Silva";
        String ridePrice = "R$ 23,50";
        tvDriverName.setText(driverName);
        tvCarInfo.setText("Toyota Corolla - Preto - ABC-1234");
        tvPrice.setText(ridePrice);
        tvDestination.setText("Para: " + destination);
        ratingBar.setRating(4.7f);
        safeScoreBar.setRating(4.9f);

        final AlertDialog driverDialog = builder.create();
        if (driverDialog.getWindow() != null) {
            driverDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        driverDialog.show();

        btnPayment.setOnClickListener(v -> {
            driverDialog.dismiss();
            navigateToSafetyChecklist(destination, driverName, ridePrice);
        });
    }
    private void navigateToSafetyChecklist(String destination, String driverName, String ridePrice) {
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.putExtra("DESTINATION", destination);
        intent.putExtra("RIDE_PRICE", ridePrice);
        intent.putExtra("DRIVER_NAME", driverName);
        startActivity(intent);

        Toast.makeText(HomeActivity.this, "Pagamento aceito! Verifique os itens de segurança.", Toast.LENGTH_LONG).show();
    }

    private void openProfileActivity() {
        Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
        startActivity(intent);
    }
}