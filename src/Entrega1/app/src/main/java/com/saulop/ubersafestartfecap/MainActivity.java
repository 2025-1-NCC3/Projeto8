package com.saulop.ubersafestartfecap;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Redireciona imediatamente para a tela de Login
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish(); // Fecha MainActivity para não ficar na pilha
    }
}
