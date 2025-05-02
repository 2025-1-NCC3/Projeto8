package br.fecap.pi.ubersafestart;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import br.fecap.pi.ubersafestart.api.ApiClient;
import br.fecap.pi.ubersafestart.api.AuthService;
import br.fecap.pi.ubersafestart.model.ApiResponse;
import br.fecap.pi.ubersafestart.model.User;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {
    private EditText editTextFullName, editTextEmailSignUp, editTextPasswordSignUp, editTextConfirmPasswordSignUp, editTextPhoneSignUp;
    private Button buttonSignUp;
    private MaterialCardView cardViewPassenger, cardViewDriver;
    private AuthService authService;
    private String selectedAccountType = "passenger";

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +
                    "(?=.*[a-zA-Z])" +
                    "(?=.*[!@#$%^&*])" +
                    "(?=\\S+$)" +
                    ".{8,}" +
                    "$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9]{10,15}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        editTextFullName = findViewById(R.id.editTextFullName);
        editTextEmailSignUp = findViewById(R.id.editTextEmailSignUp);
        editTextPasswordSignUp = findViewById(R.id.editTextPasswordSignUp);
        editTextConfirmPasswordSignUp = findViewById(R.id.editTextConfirmPasswordSignUp);
        editTextPhoneSignUp = findViewById(R.id.editTextPhoneSignUp);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        cardViewPassenger = findViewById(R.id.cardViewPassenger);
        cardViewDriver = findViewById(R.id.cardViewDriver);

        authService = ApiClient.getClient().create(AuthService.class);

        setupAccountTypeSelection();

        View buttonBack = findViewById(R.id.buttonBackSignUp);
        if (buttonBack != null) {
            buttonBack.setOnClickListener(v -> {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            });
        }

        TextView textViewLoginLink = findViewById(R.id.textViewLoginLink);
        textViewLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });

        buttonSignUp.setOnClickListener(v -> signUpUser());
    }

    private void setupAccountTypeSelection() {
        cardViewPassenger.setStrokeColor(getResources().getColor(R.color.primary_color));
        cardViewDriver.setStrokeColor(getResources().getColor(R.color.gray_color));

        cardViewPassenger.setOnClickListener(v -> {
            selectedAccountType = "passenger";
            cardViewPassenger.setStrokeColor(getResources().getColor(R.color.primary_color));
            cardViewDriver.setStrokeColor(getResources().getColor(R.color.gray_color));
        });

        cardViewDriver.setOnClickListener(v -> {
            selectedAccountType = "driver";
            cardViewDriver.setStrokeColor(getResources().getColor(R.color.primary_color));
            cardViewPassenger.setStrokeColor(getResources().getColor(R.color.gray_color));
        });
    }

    private boolean isPasswordValid(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    private boolean isPhoneValid(String phone) {
        return PHONE_PATTERN.matcher(phone).matches();
    }

    private void signUpUser() {
        String fullName = editTextFullName.getText().toString().trim();
        String email = editTextEmailSignUp.getText().toString().trim();
        String password = editTextPasswordSignUp.getText().toString().trim();
        String confirmPassword = editTextConfirmPasswordSignUp.getText().toString().trim();
        String phone = editTextPhoneSignUp.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isPasswordValid(password)) {
            Toast.makeText(this, "Password must be at least 8 characters long and contain letters, numbers, and special characters (!@#$%^&*)",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (!isPhoneValid(phone)) {
            Toast.makeText(this, "Please enter a valid phone number (10-15 digits)", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User(fullName, email, password, selectedAccountType, phone);

        authService.registerUser(user).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(SignUpActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                                    finish();
                                } catch (Exception e) {
                                    Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        }, 1000);
                    } else {
                        String errorMsg = "Registration failed";
                        if (response.body() != null && response.body().getMessage() != null) {
                            errorMsg = response.body().getMessage();
                        }
                        Toast.makeText(SignUpActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(SignUpActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(SignUpActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}