package com.saulop.ubersafestartfecap;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.saulop.ubersafestartfecap.api.ApiClient;
import com.saulop.ubersafestartfecap.api.AuthService;
import com.saulop.ubersafestartfecap.model.User;
import com.saulop.ubersafestartfecap.model.ApiResponse;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {
    private EditText editTextFullName, editTextEmailSignUp, editTextPasswordSignUp, editTextConfirmPasswordSignUp;
    private Button buttonSignUp;
    private MaterialCardView cardViewPassenger, cardViewDriver;
    private AuthService authService;
    private String selectedAccountType = "passenger";

    // Password validation pattern
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         // at least 1 digit
                    "(?=.*[a-zA-Z])" +      // at least 1 letter
                    "(?=.*[!@#$%^&*])" +    // at least 1 special character
                    "(?=\\S+$)" +           // no whitespace
                    ".{8,}" +               // at least 8 characters
                    "$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize UI components
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextEmailSignUp = findViewById(R.id.editTextEmailSignUp);
        editTextPasswordSignUp = findViewById(R.id.editTextPasswordSignUp);
        editTextConfirmPasswordSignUp = findViewById(R.id.editTextConfirmPasswordSignUp);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        cardViewPassenger = findViewById(R.id.cardViewPassenger);
        cardViewDriver = findViewById(R.id.cardViewDriver);

        // Initialize auth service
        authService = ApiClient.getClient().create(AuthService.class);

        // Set up account type selection
        setupAccountTypeSelection();

        // Configura o link de login
        TextView textViewLoginLink = findViewById(R.id.textViewLoginLink);
        textViewLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Set up sign up button
        buttonSignUp.setOnClickListener(v -> signUpUser());
    }

    private void setupAccountTypeSelection() {
        // Default passenger as selected
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

    private void signUpUser() {
        String fullName = editTextFullName.getText().toString().trim();
        String email = editTextEmailSignUp.getText().toString().trim();
        String password = editTextPasswordSignUp.getText().toString().trim();
        String confirmPassword = editTextConfirmPasswordSignUp.getText().toString().trim();

        // Validate inputs
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate password strength
        if (!isPasswordValid(password)) {
            Toast.makeText(this, "Password must be at least 8 characters long and contain letters, numbers, and special characters (!@#$%^&*)",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Create user object
        User user = new User(fullName, email, password, selectedAccountType, "");

        // Make API call
        authService.registerUser(user).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(SignUpActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    // Redirect to LoginActivity
                    Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish(); // Close SignUpActivity
                } else {
                    String errorMsg = "Registration failed";
                    if (response.body() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    Toast.makeText(SignUpActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(SignUpActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}