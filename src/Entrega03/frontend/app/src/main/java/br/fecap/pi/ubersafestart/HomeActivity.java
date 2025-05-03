package br.fecap.pi.ubersafestart;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color; // Import necessário
import android.graphics.drawable.ColorDrawable; // Import necessário
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window; // Import pode ser necessário dependendo do seu SDK mínimo
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

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

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
        loadLocationHistoryData();
        setupClickListeners();
    }

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

    private void loadLocationHistoryData() {
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

    private void setupClickListeners() {
        setupSearchListener();
        setupNavigationListeners();
        setupRecentLocationsListeners();
        setupScheduleButtonListener();
    }

    private void setupSearchListener() {
        if (layoutSearchClickable != null) {
            layoutSearchClickable.setOnClickListener(v -> {
                Toast.makeText(HomeActivity.this, "Abrir tela de busca...", Toast.LENGTH_SHORT).show();
            });
        } else {
            Log.e(TAG, "LinearLayout layoutSearchClickable não encontrado.");
        }
    }

    private void setupScheduleButtonListener() {
        if (layoutSchedule != null) {
            layoutSchedule.setOnClickListener(v -> {
                Toast.makeText(HomeActivity.this, "Abrir agendamento...", Toast.LENGTH_SHORT).show();
            });
        } else {
            Log.e(TAG, "LinearLayout layoutSchedule não encontrado.");
        }
    }

    private void setupNavigationListeners() {
        View.OnClickListener listener = v -> {
            int id = v.getId();
            updateBottomNavigationSelection(id);

            if (id == R.id.navAccount) {
                openProfileActivity();
            } else if (id == R.id.navHome) {
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

        updateBottomNavigationSelection(R.id.navHome); // Mantém Home selecionada inicialmente
    }

    private void updateBottomNavigationSelection(int selectedItemId) {
        LinearLayout[] navItems = {navHome, navServices, navActivity, navAccount};
        int[] iconIds = {R.id.iconHome, R.id.iconServices, R.id.iconActivity, R.id.iconAccount};
        int[] textIds = {R.id.textHome, R.id.textServices, R.id.textActivity, R.id.textAccount};
        int[] icons = {R.drawable.ic_home, R.drawable.ic_grid, R.drawable.ic_activity, R.drawable.ic_account};

        int activeColor = ContextCompat.getColor(this, R.color.white_fff);
        int inactiveColor = ContextCompat.getColor(this, R.color.gray_light);

        for (int i = 0; i < navItems.length; i++) {
            LinearLayout item = navItems[i];
            if (item == null) continue;

            ImageView icon = item.findViewById(iconIds[i]);
            TextView text = item.findViewById(textIds[i]);
            if (icon == null || text == null) continue;

            boolean isActive = (item.getId() == selectedItemId);

            icon.setImageResource(icons[i]);
            icon.setImageTintList(ColorStateList.valueOf(isActive ? activeColor : inactiveColor));
            text.setTextColor(isActive ? activeColor : inactiveColor);
        }
    }

    private void setupRecentLocationsListeners() {
        if (cardViewRecentLocation1 != null) {
            cardViewRecentLocation1.setOnClickListener(v -> {
                if (textViewLocationName1 != null) {
                    String destination = textViewLocationName1.getText().toString();
                    if (!destination.isEmpty()) {
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

    private void showLoadingDialog(final String destination) {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this, R.style.AlertDialogTheme);
        View loadingView = LayoutInflater.from(this).inflate(R.layout.dialog_ride_loading, null);
        builder.setView(loadingView);
        builder.setCancelable(false);

        final AlertDialog loadingDialog = builder.create();

        if (loadingDialog.getWindow() != null) {
            loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        loadingDialog.show();

        ProgressBar progressBar = loadingView.findViewById(R.id.progressBar);
        TextView statusText = loadingView.findViewById(R.id.textViewStatus);
        ImageView checkmarkImage = loadingView.findViewById(R.id.imageViewCheckmark);

        if(statusText != null) statusText.setText(R.string.dialog_loading_status_searching);
        if(progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if(checkmarkImage != null) checkmarkImage.setVisibility(View.GONE);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing() && !isDestroyed() && loadingDialog.isShowing()) {
                if(statusText != null) statusText.setText("Motorista encontrado!");
                if(progressBar != null) progressBar.setVisibility(View.GONE);
                if(checkmarkImage != null) checkmarkImage.setVisibility(View.VISIBLE);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (!isFinishing() && !isDestroyed() && loadingDialog.isShowing()) {
                        loadingDialog.dismiss();
                        showDriverInfoDialog(destination);
                    }
                }, 1000);
            }
        }, 3000);
    }

    private void showDriverInfoDialog(String destination) {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this, R.style.AlertDialogTheme);
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
        if (tvDriverName != null) tvDriverName.setText(driverName);
        if (tvCarInfo != null) tvCarInfo.setText("Toyota Corolla - Preto - ABC-1234");
        if (tvPrice != null) tvPrice.setText(ridePrice);
        if (tvDestination != null) tvDestination.setText("Para: " + destination);
        if (ratingBar != null) ratingBar.setRating(4.7f);
        if (safeScoreBar != null) safeScoreBar.setRating(4.9f); // Exemplo de Safe Score

        final AlertDialog driverDialog = builder.create();

        if (driverDialog.getWindow() != null) {
            driverDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        driverDialog.show();

        if (btnPayment != null) {
            btnPayment.setOnClickListener(v -> {
                driverDialog.dismiss();
                navigateToSafetyChecklist(destination, driverName, ridePrice);
            });
        } else { Log.e(TAG, "Button buttonPayment não encontrado no dialog_driver_info."); }
    }

    private void navigateToSafetyChecklist(String destination, String driverName, String ridePrice) {
        Intent intent = new Intent(HomeActivity.this, MainActivity.class); // MainActivity é o Checklist? Confirme o nome.
        intent.putExtra("DESTINATION", destination);
        intent.putExtra("RIDE_PRICE", ridePrice);
        intent.putExtra("DRIVER_NAME", driverName);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void openProfileActivity() {
        Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setMessage("Deseja sair do aplicativo?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    finishAffinity();
                    overridePendingTransition(0, R.anim.fade_out);
                })
                .setNegativeButton("Não", null)
                .show();
    }
}