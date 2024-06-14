package com.example.buggyalarm;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class CreateAlarmActivity extends AppCompatActivity {

    Button button;
    TimePicker timePicker;
    boolean notificationSet = false;
    private MediaPlayer mediaPlayer;
    final String[] melodie_selectata = new String[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_alarm);

        button = findViewById(R.id.btnNotifications);
        timePicker = findViewById(R.id.timePicker);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (ContextCompat.checkSelfPermission(CreateAlarmActivity.this,
                    Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CreateAlarmActivity.this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        Button btnMelodie1 = findViewById(R.id.btnMelodie1);
        Button btnMelodie2 = findViewById(R.id.btnMelodie2);
        Button btnMelodie3 = findViewById(R.id.btnMelodie3);

        btnMelodie1.setOnClickListener(v -> melodie_selectata[0] = "Pan Jabi");
        btnMelodie2.setOnClickListener(v -> melodie_selectata[0] = "Vivaldi");
        btnMelodie3.setOnClickListener(v -> melodie_selectata[0] = "AC/DC");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notificationSet = true;
                checkNotificationTime();
                Toast.makeText(CreateAlarmActivity.this, "Alarm set", Toast.LENGTH_SHORT).show();
            }
        });

        startCheckingTime();
    }

    public void checkNotificationTime() {
        int hour, minute;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hour = timePicker.getHour();
            minute = timePicker.getMinute();
        } else {
            hour = timePicker.getCurrentHour();
            minute = timePicker.getCurrentMinute();
        }

        int currentHour, currentMinute;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            currentHour = java.time.LocalTime.now().getHour();
            currentMinute = java.time.LocalTime.now().getMinute();
        } else {
            currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
            currentMinute = java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE);
        }

        if (hour == currentHour && minute == currentMinute) {
            makeNotification();
            notificationSet = false;
        }
    }

    private void startCheckingTime() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        if (notificationSet) {
                            checkNotificationTime();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void makeNotification() {
        String channelId = "CHANNEL_ID_NOTIFICATION";
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), channelId);
        builder.setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("alarma programatori")
                .setContentText(" alarma canta")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent intent = new Intent(getApplicationContext(), NotificationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("data", "alarma canta");
        intent.putExtra("playMusic", true);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_MUTABLE);
        builder.setContentIntent(pendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel =
                    notificationManager.getNotificationChannel(channelId);
            if (notificationChannel == null) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                notificationChannel = new NotificationChannel(channelId, "Some description", importance);
                notificationChannel.setLightColor(Color.GREEN);
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        Intent musicIntent = new Intent(this, MediaPlayerService.class);
        musicIntent.setAction("PLAY MELODIE");
        musicIntent.putExtra("melodie", melodie_selectata[0]);
        startService(musicIntent);

        notificationManager.notify(0, builder.build());
    }
}
