package com.example.myapplication;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.github.tbouron.shakedetector.library.ShakeDetector;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

public class ServiceMine extends Service {

    private static final String TAG = "ServiceMine";
    boolean isRunning = false;
    FusedLocationProviderClient fusedLocationClient;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ShakeDetector.create(this, () -> {
            Log.d(TAG, "Shake detected! Attempting to send SOS.");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Location permission is NOT granted.");
                return;
            }

            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, new CancellationTokenSource().getToken())
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            Log.d(TAG, "Successfully got location: " + location.getLatitude() + "," + location.getLongitude());
                            String myLocation = "http://maps.google.com/maps?q=" + location.getLatitude() + "," + location.getLongitude();
                            sendSms(myLocation);
                        } else {
                            Log.e(TAG, "Failed to get location, it was null.");
                            sendSms("Unable to Find Location :(");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting location", e);
                        sendSms("Unable to Find Location :(");
                    });
        });
    }

    //  CHANGED: This method now handles sending SMS to two numbers.
    private void sendSms(String location) {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);

        // Get both numbers from storage
        String number1 = sharedPreferences.getString("ENUM1", "NONE");
        String number2 = sharedPreferences.getString("ENUM2", "");

        String message = "I'm in Trouble!\nSending My Location:\n" + location;

        boolean smsSent = false;
        // Send to the first number if it exists
        if (!number1.equalsIgnoreCase("NONE")) {
            sendSmsToNumber(number1, message);
            smsSent = true;
        }

        // Send to the second number if it exists and is not empty
        if (number2 != null && !number2.isEmpty()) {
            sendSmsToNumber(number2, message);
            smsSent = true;
        }

        if(smsSent) {
            Toast.makeText(this, "SOS Sent!", Toast.LENGTH_SHORT).show();
        }
    }

    // ADDED: A helper method to avoid repeating code
    private void sendSmsToNumber(String phoneNumber, String message) {
        try {
            SmsManager manager = SmsManager.getDefault();
            Log.d(TAG, "Sending SMS to: " + phoneNumber);
            manager.sendTextMessage(phoneNumber, null, message, null, null);
            Log.d(TAG, "SMS sent successfully to " + phoneNumber);
        } catch (Exception e) {
            Toast.makeText(this, "SOS failed to send to " + phoneNumber, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "SMS failed to send to " + phoneNumber, e);
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equalsIgnoreCase("STOP")) {
                if (isRunning) {
                    ShakeDetector.stop();
                    stopForeground(true);

                    stopSelf();
                    isRunning = false;
                }
            } else { // Start action
                Intent notificationIntent = new Intent(this, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

                Notification notification = new NotificationCompat.Builder(this, "MYID")
                        .setContentTitle("Safety Service Running")
                        .setContentText("Shake device to send SOS alert")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentIntent(pendingIntent)
                        .build();

                startForeground(115, notification);
                ShakeDetector.start();
                isRunning = true;
                return START_STICKY;
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        ShakeDetector.destroy();
        super.onDestroy();
    }
}