package com.example.tesknotam;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

public class WelcomeActivity extends AppCompatActivity {

    private Button mbuttonDalej;
    private Button mbuttonWstecz;
    private TextView mtextMessage;

    private final int TEXT_MESSAGES_NUMBER = 4;
    private int welcomeTextIndex = 0;
    private Typeface defaultTypeface;
    private String[] welcomeTextArray = {
        "Aplikacja TĘSKNOTAM",
        "łączy wrażenia wizualne i dźwiękowe, umożliwiając pełen odbiór całego projektu – jest więc niezbędna do zrozumienia naszego pomysłu",
        "Do każdego ze zdjęć dopasowano odpowiednie nagranie dźwiękowe. Aplikacja lokalizuje Twoje urządzenie i odtwarza dźwięk przez słuchawki.",
        "Podziwiaj kolejne fotografie i wsłuchaj się w dźwięki codzienności.\nZapraszamy."
    };


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mtextMessage = (TextView) findViewById(R.id.wTextMessage);
        defaultTypeface = mtextMessage.getTypeface();
        mtextMessage.setText(welcomeTextArray[welcomeTextIndex]);
        mtextMessage.setTextSize(40);
        mtextMessage.setTypeface(mtextMessage.getTypeface(), Typeface.BOLD_ITALIC);

        mbuttonDalej = (Button) findViewById(R.id.wButtondalej);
        mbuttonDalej.setText("Dalej (" + String.valueOf(welcomeTextIndex + 1) + "/"
                + String.valueOf(TEXT_MESSAGES_NUMBER) + ")");

        mbuttonWstecz = (Button) findViewById(R.id.wButtonWstecz);
        mbuttonWstecz.setText("Wstecz");
        mbuttonWstecz.setVisibility(View.INVISIBLE);


        mbuttonDalej.setOnClickListener(v -> {
            if (++welcomeTextIndex == TEXT_MESSAGES_NUMBER)
            {
                Intent intent = new Intent (this, MainActivity.class);
                startActivity(intent);
                finish();
            }
            else
            {
                mtextMessage.setText(welcomeTextArray[welcomeTextIndex]);
                mtextMessage.setTextSize(20);
                mtextMessage.setTypeface(ResourcesCompat.getFont(this, R.font.artifika));
                mbuttonDalej.setText("Dalej (" + String.valueOf(welcomeTextIndex + 1) + "/"
                        + String.valueOf(TEXT_MESSAGES_NUMBER) + ")");
                mbuttonWstecz.setVisibility(View.VISIBLE);
            }
        });

        mbuttonWstecz.setOnClickListener (v -> {
            if (--welcomeTextIndex == 0)
            {
                mbuttonWstecz.setVisibility(View.INVISIBLE);
                mtextMessage.setTextSize(40);
                mtextMessage.setTypeface(mtextMessage.getTypeface(), Typeface.BOLD_ITALIC);
            }
            mtextMessage.setText(welcomeTextArray[welcomeTextIndex]);
            mbuttonDalej.setText("Dalej (" + String.valueOf(welcomeTextIndex + 1) + "/"
                    + String.valueOf(welcomeTextIndex + TEXT_MESSAGES_NUMBER) + ")");
        });
    }
}