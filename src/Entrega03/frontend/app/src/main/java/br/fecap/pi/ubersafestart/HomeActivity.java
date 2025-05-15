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
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.fecap.pi.ubersafestart.model.SimulatedUser;
import br.fecap.pi.ubersafestart.utils.StaticUserManager;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    // Constantes para SharedPreferences
    private static final String USER_LOGIN_PREFS = "userPrefs";
    private static final String USER_LOCAL_PREFERENCES = "UserPreferences";
    private static final String KEY_GENDER_PASSENGER = "gender";
    private static final String KEY_SAME_GENDER_PAIRING_PASSENGER = "sameGenderPairingEnabled";

    // Componentes da UI
    private TextView textViewLocationName1, textViewLocationAddress1;
    private TextView textViewLocationName2, textViewLocationAddress2;
    private LinearLayout layoutSearchClickable;
    private TextView textViewSearchHint;
    private EditText editTextDestinationInput;
    private ImageButton buttonSubmitDestination;
    private View searchDivider;
    private LinearLayout layoutSchedule;
    private LinearLayout navAccount, navHome, navServices, navAchievements;
    private CardView cardViewRecentLocation1, cardViewRecentLocation2;

    private String currentDestinationFormattedAddress = "";
    private String currentDestinationQuery = "";

    private final int[] navIconIds = {R.id.iconHome, R.id.iconServices, R.id.iconAchievements, R.id.iconAccount};
    private final int[] navTextIds = {R.id.textHome, R.id.textServices, R.id.textAchievements, R.id.textAccount};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // Seu XML activity_home.xml

        initViews();
        loadLocationHistoryData();
        setupClickListeners();
        updateBottomNavigationSelection(R.id.navHome);
    }

    private void initViews() {
        textViewLocationName1 = findViewById(R.id.textViewLocationName1);
        textViewLocationAddress1 = findViewById(R.id.textViewLocationAddress1);
        textViewLocationName2 = findViewById(R.id.textViewLocationName2);
        textViewLocationAddress2 = findViewById(R.id.textViewLocationAddress2);

        layoutSearchClickable = findViewById(R.id.layoutSearchClickable);
        textViewSearchHint = findViewById(R.id.textViewSearchHint);
        editTextDestinationInput = findViewById(R.id.editTextDestinationInput);
        buttonSubmitDestination = findViewById(R.id.buttonSubmitDestination);
        searchDivider = findViewById(R.id.searchDivider);

        layoutSchedule = findViewById(R.id.layoutSchedule);
        navAccount = findViewById(R.id.navAccount);
        navHome = findViewById(R.id.navHome);
        navServices = findViewById(R.id.navServices);
        navAchievements = findViewById(R.id.navAchievements);
        cardViewRecentLocation1 = findViewById(R.id.cardViewRecentLocation1);
        cardViewRecentLocation2 = findViewById(R.id.cardViewRecentLocation2);

        if (layoutSearchClickable == null) Log.e(TAG, "layoutSearchClickable não encontrado!");
        if (textViewSearchHint == null) Log.e(TAG, "textViewSearchHint não encontrado!");
        if (editTextDestinationInput == null) Log.e(TAG, "editTextDestinationInput não encontrado!");
        if (buttonSubmitDestination == null) Log.e(TAG, "buttonSubmitDestination não encontrado!");
        if (searchDivider == null) Log.e(TAG, "searchDivider não encontrado!");
    }

    private void loadLocationHistoryData() {
        String location1Name = "Avenida Paulista, 1578";
        String location1Address = "Bela Vista, São Paulo - SP";
        String location2Name = "Parque Ibirapuera";
        String location2Address = "Av. Pedro Álvares Cabral";

        if (textViewLocationName1 != null) textViewLocationName1.setText(location1Name);
        if (textViewLocationAddress1 != null) textViewLocationAddress1.setText(location1Address);
        if (textViewLocationName2 != null) textViewLocationName2.setText(location2Name);
        if (textViewLocationAddress2 != null) textViewLocationAddress2.setText(location2Address);
        if (cardViewRecentLocation1 != null) cardViewRecentLocation1.setVisibility(View.VISIBLE);
        if (cardViewRecentLocation2 != null) cardViewRecentLocation2.setVisibility(View.VISIBLE);
    }

    private void setupClickListeners() {
        if (layoutSearchClickable != null) {
            layoutSearchClickable.setOnClickListener(v -> switchToSearchMode());
        }
        if (textViewSearchHint != null) {
            textViewSearchHint.setOnClickListener(v -> switchToSearchMode());
        }

        if (buttonSubmitDestination != null) {
            buttonSubmitDestination.setOnClickListener(v -> {
                if (editTextDestinationInput != null) {
                    currentDestinationQuery = editTextDestinationInput.getText().toString().trim();
                    if (currentDestinationQuery.isEmpty()) {
                        Toast.makeText(HomeActivity.this, "Por favor, insira um destino.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Log.d(TAG, "Destino submetido: " + currentDestinationQuery);
                    hideKeyboard();
                    processDestinationSearch(currentDestinationQuery);
                }
            });
        }

        if (editTextDestinationInput != null) {
            editTextDestinationInput.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    if (buttonSubmitDestination != null && buttonSubmitDestination.getVisibility() == View.VISIBLE) {
                        buttonSubmitDestination.performClick();
                    }
                    return true;
                }
                return false;
            });
        }

        setupNavigationListeners();
        setupRecentLocationsListeners();
        setupScheduleButtonListener();
    }

    private void switchToSearchMode() {
        if (textViewSearchHint != null) textViewSearchHint.setVisibility(View.GONE);
        if (editTextDestinationInput != null) {
            editTextDestinationInput.setVisibility(View.VISIBLE);
            editTextDestinationInput.requestFocus();
            showKeyboard(editTextDestinationInput);
        }
        if (buttonSubmitDestination != null) buttonSubmitDestination.setVisibility(View.VISIBLE);
        if (searchDivider != null) searchDivider.setVisibility(View.GONE);
        if (layoutSchedule != null) layoutSchedule.setVisibility(View.GONE);
    }

    private void switchToDisplayMode(String searchedText) {
        if (textViewSearchHint != null) {
            if (!TextUtils.isEmpty(searchedText)) {
                textViewSearchHint.setText(searchedText);
                textViewSearchHint.setTextColor(ContextCompat.getColor(this, R.color.white_fff));
            } else {
                textViewSearchHint.setText(R.string.para_onde);
                textViewSearchHint.setTextColor(ContextCompat.getColor(this, R.color.gray_light));
            }
            textViewSearchHint.setVisibility(View.VISIBLE);
        }
        if (editTextDestinationInput != null) {
            editTextDestinationInput.setText("");
            editTextDestinationInput.setVisibility(View.GONE);
        }
        if (buttonSubmitDestination != null) buttonSubmitDestination.setVisibility(View.GONE);
        if (searchDivider != null) searchDivider.setVisibility(View.VISIBLE);
        if (layoutSchedule != null) layoutSchedule.setVisibility(View.VISIBLE);
        hideKeyboard();
    }

    private void showKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    // Função para capitalizar nomes de rua (simplificada)
    private String capitalizeStreetName(String streetQuery) {
        if (streetQuery == null || streetQuery.isEmpty()) {
            return streetQuery;
        }
        String[] words = streetQuery.toLowerCase().split("\\s+");
        StringBuilder capitalizedString = new StringBuilder();
        String[] articlesAndPrepositions = {"de", "da", "do", "dos", "das", "a", "o", "e"}; // Adicione mais se necessário

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.length() > 0) {
                boolean isArticleOrPreposition = false;
                for (String ap : articlesAndPrepositions) {
                    if (word.equals(ap) && i > 0) { // Não capitaliza se for a primeira palavra
                        isArticleOrPreposition = true;
                        break;
                    }
                }
                if (isArticleOrPreposition) {
                    capitalizedString.append(word);
                } else {
                    capitalizedString.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
                }
            }
            if (i < words.length - 1) {
                capitalizedString.append(" ");
            }
        }
        return capitalizedString.toString();
    }


    private void processDestinationSearch(String destinationQuery) {
        String queryLower = destinationQuery.toLowerCase();
        String numero = "";
        String ruaApenas = destinationQuery; // Para guardar a parte da rua sem o número

        // Tenta extrair um número da query
        Pattern pattern = Pattern.compile("\\b(\\d+)\\b");
        Matcher matcher = pattern.matcher(destinationQuery);
        if (matcher.find()) {
            numero = matcher.group(1);
            // Remove o número e vírgulas adjacentes da string da rua para capitalização
            ruaApenas = destinationQuery.replaceAll(",?\\s*\\b" + numero + "\\b,?", "").trim();
        } else {
            // Se não houver número, a rua é a query inteira
            ruaApenas = destinationQuery.replaceAll(",$", "").trim(); // Remove vírgula no final, se houver
        }

        String ruaCapitalizada = capitalizeStreetName(ruaApenas);

        // SIMULAÇÃO: Obter endereço formatado.
        if (queryLower.contains("rua alfredo pujol") && (queryLower.contains("1358") || numero.equals("1358"))) {
            currentDestinationFormattedAddress = "Rua Alfredo Pujol, 1358 - Santana, São Paulo - SP, Brasil";
        } else if (queryLower.contains("torres da barra") && (queryLower.contains("75") || numero.equals("75"))) {
            currentDestinationFormattedAddress = "Rua Torres da Barra, 75 - Barra Funda, São Paulo - SP, Brasil";
        } else if (queryLower.contains("rua itajobi") && (queryLower.contains("75") || numero.equals("75"))) {
            currentDestinationFormattedAddress = "Rua Itajobi, 75 - Pacaembu, São Paulo - SP, 01246-110, Brasil";
        } else if (queryLower.contains("joaquim carlos") && (queryLower.contains("655") || numero.equals("655"))) {
            currentDestinationFormattedAddress = "Rua Joaquim Carlos, 655 - Brás, São Paulo - SP, 03019-000, Brasil";
        } else if (queryLower.contains("avenida paulista") && (queryLower.contains("1912") || numero.equals("1912"))) {
            currentDestinationFormattedAddress = "Avenida Paulista, 1912 - Bela Vista, São Paulo - SP, Brasil";
        } else if (queryLower.contains("museu do ipiranga")) {
            currentDestinationFormattedAddress = "Parque da Independência - Ipiranga, São Paulo - SP, 04263-000, Brasil";
        } else if (queryLower.contains("paulista") && (queryLower.contains("1578") || numero.equals("1578"))) {
            currentDestinationFormattedAddress = "Avenida Paulista, 1578 - Bela Vista, São Paulo - SP, Brasil";
        } else if (queryLower.contains("paulista")) { // Regra mais genérica
            currentDestinationFormattedAddress = capitalizeStreetName(destinationQuery.replaceAll(numero, "").trim()) + (!numero.isEmpty() ? ", " + numero : "") + " - Bela Vista, São Paulo - SP, Brasil";
        } else if (queryLower.contains("ibirapuera")) {
            currentDestinationFormattedAddress = "Parque Ibirapuera, Av. Pedro Álvares Cabral - Vila Mariana, São Paulo - SP, Brasil";
        } else {
            // Fallback: Usa a rua capitalizada, número (se houver) e adiciona cidade/estado/país genéricos
            currentDestinationFormattedAddress = ruaCapitalizada;
            if (!numero.isEmpty()) {
                currentDestinationFormattedAddress += ", " + numero;
            }
            // Adiciona sufixo genérico se não parecer já estar completo
            String sufixoGenerico = ", São Paulo - SP, Brasil";
            if (!currentDestinationFormattedAddress.toLowerCase().contains("são paulo")) {
                currentDestinationFormattedAddress += sufixoGenerico;
            } else if (!currentDestinationFormattedAddress.toLowerCase().contains("brasil")){
                currentDestinationFormattedAddress += ", Brasil";
            }
            Log.w(TAG, "Simulação: Endereço não coberto por regra específica. Usando: " + currentDestinationFormattedAddress);
        }
        Log.d(TAG, "Endereço formatado (simulado): " + currentDestinationFormattedAddress);

        switchToDisplayMode(currentDestinationFormattedAddress);
        showSearchingDriverDialog(currentDestinationFormattedAddress);
    }


    private void showSearchingDriverDialog(final String destinationFormattedAddress) {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this, R.style.AlertDialogTheme);
        View loadingView = LayoutInflater.from(this).inflate(R.layout.dialog_ride_loading, null);
        builder.setView(loadingView);
        builder.setCancelable(false);

        final AlertDialog searchingDialog = builder.create();
        if (searchingDialog.getWindow() != null) {
            searchingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        searchingDialog.show();

        ProgressBar progressBar = loadingView.findViewById(R.id.progressBar);
        TextView statusText = loadingView.findViewById(R.id.textViewStatus);
        ImageView checkmarkImage = loadingView.findViewById(R.id.imageViewCheckmark);

        if (statusText != null) statusText.setText(getString(R.string.dialog_loading_status_searching));
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (checkmarkImage != null) checkmarkImage.setVisibility(View.GONE);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isFinishing() || isDestroyed() || !searchingDialog.isShowing()) return;

            SharedPreferences loginPrefs = getSharedPreferences(USER_LOGIN_PREFS, MODE_PRIVATE);
            String currentUserGender = loginPrefs.getString(KEY_GENDER_PASSENGER, "").toUpperCase(Locale.ROOT);
            SharedPreferences localPrefs = getSharedPreferences(USER_LOCAL_PREFERENCES, Context.MODE_PRIVATE);
            boolean preferSameGender = localPrefs.getBoolean(KEY_SAME_GENDER_PAIRING_PASSENGER, false);

            final SimulatedUser driver = StaticUserManager.getRandomDriver(currentUserGender, preferSameGender);

            if (driver != null) {
                if (statusText != null) statusText.setText(getString(R.string.dialog_loading_status_found));
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (checkmarkImage != null) checkmarkImage.setVisibility(View.VISIBLE);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (!isFinishing() && !isDestroyed() && searchingDialog.isShowing()) {
                        searchingDialog.dismiss();
                        String ridePrice = generateRandomPrice();
                        showDriverInfoDialog(driver, ridePrice, destinationFormattedAddress);
                    }
                }, 1000);
            } else {
                if (statusText != null) statusText.setText(getString(R.string.dialog_loading_status_not_found));
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(HomeActivity.this, "Nenhum motorista compatível no momento.", Toast.LENGTH_LONG).show();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (!isFinishing() && !isDestroyed() && searchingDialog.isShowing()) {
                        searchingDialog.dismiss();
                    }
                    switchToDisplayMode("");
                }, 2000);
            }
        }, 3000);
    }

    private void setupScheduleButtonListener() {
        if (layoutSchedule != null) {
            layoutSchedule.setOnClickListener(v -> Toast.makeText(HomeActivity.this, "Agendamento em desenvolvimento.", Toast.LENGTH_SHORT).show());
        }
    }

    private void setupNavigationListeners() {
        View.OnClickListener listener = v -> {
            int id = v.getId();
            if (id == R.id.navAccount) {
                updateBottomNavigationSelection(id);
                openProfileActivity();
            } else if (id == R.id.navHome) {
                updateBottomNavigationSelection(id);
            } else if (id == R.id.navServices) {
                updateBottomNavigationSelection(id);
                Toast.makeText(HomeActivity.this, "Opções em desenvolvimento", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.navAchievements) {
                Intent intent = new Intent(HomeActivity.this, AchievementsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        };
        if (navHome != null) navHome.setOnClickListener(listener);
        if (navServices != null) navServices.setOnClickListener(listener);
        if (navAchievements != null) navAchievements.setOnClickListener(listener);
        if (navAccount != null) navAccount.setOnClickListener(listener);
    }

    private void updateBottomNavigationSelection(int selectedItemId) {
        LinearLayout[] navItems = {navHome, navServices, navAchievements, navAccount};
        int activeColor = ContextCompat.getColor(this, R.color.white_fff);
        int inactiveColor = ContextCompat.getColor(this, R.color.gray_light);

        for (int i = 0; i < navItems.length; i++) {
            LinearLayout item = navItems[i];
            if (item == null) continue;
            ImageView icon = item.findViewById(navIconIds[i]);
            TextView text = item.findViewById(navTextIds[i]);
            if (icon == null || text == null) continue;
            boolean isActive = (item.getId() == selectedItemId);
            icon.setImageTintList(ColorStateList.valueOf(isActive ? activeColor : inactiveColor));
            text.setTextColor(isActive ? activeColor : inactiveColor);
        }
    }

    private void setupRecentLocationsListeners() {
        View.OnClickListener recentLocationListener = v -> {
            String destinationName = "";
            String destinationFullAddress = "";
            int id = v.getId();

            if (id == R.id.cardViewRecentLocation1 && textViewLocationName1 != null && textViewLocationAddress1 != null) {
                destinationName = textViewLocationName1.getText().toString();
                destinationFullAddress = destinationName + ", " + textViewLocationAddress1.getText().toString();
            } else if (id == R.id.cardViewRecentLocation2 && textViewLocationName2 != null && textViewLocationAddress2 != null) {
                destinationName = textViewLocationName2.getText().toString();
                destinationFullAddress = destinationName + ", " + textViewLocationAddress2.getText().toString();
            }

            if (!destinationName.isEmpty()) {
                Log.d(TAG, "Local recente clicado: " + destinationName);
                currentDestinationQuery = destinationName;
                currentDestinationFormattedAddress = destinationFullAddress;
                Log.d(TAG, "Endereço formatado do local recente: " + currentDestinationFormattedAddress);

                switchToDisplayMode(currentDestinationFormattedAddress);
                showSearchingDriverDialog(currentDestinationFormattedAddress);
            }
        };
        if (cardViewRecentLocation1 != null) cardViewRecentLocation1.setOnClickListener(recentLocationListener);
        if (cardViewRecentLocation2 != null) cardViewRecentLocation2.setOnClickListener(recentLocationListener);
    }

    private void showDriverInfoDialog(SimulatedUser driver, String price, String destinationAddress) {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this, R.style.AlertDialogTheme);
        View driverInfoView = LayoutInflater.from(this).inflate(R.layout.dialog_driver_info, null);
        builder.setView(driverInfoView);
        builder.setCancelable(true);

        TextView tvDialogTitle = driverInfoView.findViewById(R.id.textViewRideFound);
        TextView tvDestinationInfo = driverInfoView.findViewById(R.id.textViewDestination);
        ImageView driverProfileImage = driverInfoView.findViewById(R.id.imageViewDriver);
        TextView tvDriverName = driverInfoView.findViewById(R.id.textViewDriverName);
        TextView tvCarInfo = driverInfoView.findViewById(R.id.textViewCarInfo);
        RatingBar ratingBarDriver = driverInfoView.findViewById(R.id.ratingBarDriver);
        TextView tvSafeScoreLabel = driverInfoView.findViewById(R.id.textViewSafeScore);
        RatingBar ratingBarSafeScore = driverInfoView.findViewById(R.id.ratingBarSafeScore);
        TextView tvPrice = driverInfoView.findViewById(R.id.textViewPrice);
        Button btnConfirmRide = driverInfoView.findViewById(R.id.buttonPayment);

        if (tvDialogTitle != null) tvDialogTitle.setText(getString(R.string.dialog_driver_info_title));
        if (tvDriverName != null && driver != null) tvDriverName.setText(driver.getName());
        if (tvCarInfo != null && driver != null) tvCarInfo.setText(driver.getCarModel() + " - " + driver.getLicensePlate());
        if (tvPrice != null) tvPrice.setText("R$ " + price);

        if (tvDestinationInfo != null) {
            if (destinationAddress != null && !destinationAddress.isEmpty()) {
                tvDestinationInfo.setText("Para: " + destinationAddress);
            } else {
                tvDestinationInfo.setText(getString(R.string.dialog_driver_info_destination_placeholder));
            }
        }

        if (ratingBarDriver != null && driver != null) ratingBarDriver.setRating(driver.getDriverRating());
        if (ratingBarSafeScore != null && driver != null) {
            ratingBarSafeScore.setRating(driver.getDriverRating());
        }
        if (tvSafeScoreLabel != null) tvSafeScoreLabel.setText(getString(R.string.dialog_driver_info_safescore_label));
        if (driverProfileImage != null) {
            driverProfileImage.setImageResource(R.drawable.ic_account);
            driverProfileImage.setColorFilter(ContextCompat.getColor(this, R.color.white_fff));
        }

        final AlertDialog driverDialog = builder.create();
        if (driverDialog.getWindow() != null) {
            driverDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        driverDialog.show();

        if (btnConfirmRide != null) {
            btnConfirmRide.setOnClickListener(v -> {
                driverDialog.dismiss();
                if (driver != null) {
                    Log.d(TAG, "Motorista " + driver.getName() + " confirmado para: " + destinationAddress);
                    navigateToSafetyChecklist(destinationAddress, driver.getName(), price, driver);
                } else {
                    Log.e(TAG, "Objeto Driver nulo ao confirmar.");
                    Toast.makeText(HomeActivity.this, "Erro ao confirmar motorista.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String generateRandomPrice() {
        double price = 10.0 + new Random().nextDouble() * 40.0;
        return String.format(new Locale("pt", "BR"), "%.2f", price);
    }

    private void navigateToSafetyChecklist(String destination, String driverName, String ridePrice, SimulatedUser driver) {
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.putExtra("DESTINATION", destination);
        intent.putExtra("RIDE_PRICE", ridePrice);
        intent.putExtra("DRIVER_NAME", driverName);
        if (driver != null) {
            intent.putExtra("DRIVER_CAR_MODEL", driver.getCarModel());
            intent.putExtra("DRIVER_LICENSE_PLATE", driver.getLicensePlate());
            intent.putExtra("DRIVER_RATING_FLOAT", driver.getDriverRating());
        }
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void openProfileActivity() {
        Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBottomNavigationSelection(R.id.navHome);
        switchToDisplayMode("");
    }

    @Override
    public void onBackPressed() {
        if (editTextDestinationInput != null && editTextDestinationInput.getVisibility() == View.VISIBLE) {
            switchToDisplayMode("");
        } else {
            new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                    .setMessage(getString(R.string.confirm_exit_app))
                    .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                        finishAffinity();
                    })
                    .setNegativeButton(getString(R.string.no), null)
                    .show();
        }
    }
}
