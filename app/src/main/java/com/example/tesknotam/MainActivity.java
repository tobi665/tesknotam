package com.example.tesknotam;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.os.Process;

public class MainActivity extends AppCompatActivity {
    Button mbuttonStart;
    Button mbuttonExit;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mbuttonStart = (Button) findViewById(R.id.button_start);
        mbuttonExit = (Button) findViewById(R.id.button_exit);

        mbuttonStart.setOnClickListener(v -> {
            Intent paintingIntent = new Intent(this, PaintingActivity.class);
            startActivity(paintingIntent);
        });

        mbuttonExit.setOnClickListener (v -> {
            finish();
            System.exit(0);
        });
    }
}