package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class RegisterNumberActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_number);

        TextInputEditText numberEditText1 = findViewById(R.id.numberEdit1);
        TextInputEditText numberEditText2 = findViewById(R.id.numberEdit2);
        Button saveButton = findViewById(R.id.saveNumberButton);

        saveButton.setOnClickListener(v -> {
            String number1 = numberEditText1.getText().toString();
            String number2 = numberEditText2.getText().toString();

            // The first number is required, the second is optional
            if (number1.length() != 10) {
                Toast.makeText(this, "Please enter a valid 10-digit number for the 1st contact", Toast.LENGTH_SHORT).show();
                return; // Stop the function here
            }

            // Save both numbers (number2 might be empty, which is okay)
            SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
            SharedPreferences.Editor myEdit = sharedPreferences.edit();
            myEdit.putString("ENUM1", number1); // Key for the first number
            myEdit.putString("ENUM2", number2); // Key for the second number
            myEdit.apply();

            // Go to the MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}