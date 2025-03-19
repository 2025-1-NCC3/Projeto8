package com.saulop.ubersafestartfecap;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        TextView textViewLoginLink = findViewById(R.id.textViewLoginLink);
        textViewLoginLink.setText(Html.fromHtml(getString(R.string.already_have_account)), TextView.BufferType.SPANNABLE);
    }
}
