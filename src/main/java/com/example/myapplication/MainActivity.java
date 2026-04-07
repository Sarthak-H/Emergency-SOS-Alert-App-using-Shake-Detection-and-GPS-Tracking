package com.example.myapplication;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<String[]> multiplePermissions;

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);

        // CHANGED: Now reads both numbers from storage
        String number1 = sharedPreferences.getString("ENUM1", "NONE");
        String number2 = sharedPreferences.getString("ENUM2", "");

        // If the first number is missing, go to the register screen
        if (number1.equalsIgnoreCase("NONE")) {
            startActivity(new Intent(this, RegisterNumberActivity.class));
            finish();
        } else {
            TextView textView = findViewById(R.id.textNum); // Make sure your activity_main.xml has this ID

            // CHANGED: Builds a string to display one or both numbers
            StringBuilder displayText = new StringBuilder("SOS Will Be Sent To:\n" + number1);
            if (number2 != null && !number2.isEmpty()) {
                displayText.append("\nand\n").append(number2);
            }
            textView.setText(displayText.toString());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Make sure you have buttons with onClick attributes here

        multiplePermissions = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            List<String> deniedPermissions = new ArrayList<>();
            for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                if (!entry.getValue()) {
                    deniedPermissions.add(entry.getKey());
                }
            }
            if (!deniedPermissions.isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content), "Some permissions must be granted.", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Grant", v -> multiplePermissions.launch(deniedPermissions.toArray(new String[0])))
                        .show();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("MYID", "CHANNELFOREGROUND", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager m = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            m.createNotificationChannel(channel);
        }
    }

    public void stopService(View view) {
        Intent notificationIntent = new Intent(this, ServiceMine.class);
        notificationIntent.setAction("STOP");
        startService(notificationIntent);
        Snackbar.make(findViewById(android.R.id.content), "Service Stopped!", Snackbar.LENGTH_LONG).show();
    }

    public void startServiceV(View view) {
        Toast.makeText(this, "Start Button Clicked!", Toast.LENGTH_SHORT).show();
        if (hasAllPermissions()) {
            Intent notificationIntent = new Intent(this, ServiceMine.class);
            notificationIntent.setAction("Start");
            startService(notificationIntent);
            Snackbar.make(findViewById(android.R.id.content), "Service Started!", Snackbar.LENGTH_LONG).show();
        } else {
            requestPermissions();
        }
    }

    private boolean hasAllPermissions() {
        boolean hasSms = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
        boolean hasFineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean hasNotifications = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotifications = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return hasSms && hasFineLocation && hasNotifications;
    }

    private void requestPermissions() {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        permissionsToRequest.add(Manifest.permission.SEND_SMS);
        permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        multiplePermissions.launch(permissionsToRequest.toArray(new String[0]));
    }
}