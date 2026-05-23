package com.example.learnquest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GameOption extends AppCompatActivity {

    ImageButton buttonMath, buttonWord, buttonPuzzle, buttonScience;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game_option);

        // CONNECT BUTTONS
        buttonMath = findViewById(R.id.buttonMath);
        buttonWord = findViewById(R.id.buttonWord);
        buttonPuzzle = findViewById(R.id.buttonPuzzle);
        buttonScience = findViewById(R.id.buttonScience);


        // MATH BUTTON
        buttonMath.setOnClickListener(v -> {

            Intent intent = new Intent(GameOption.this, Math_Level.class);
            startActivity(intent);

        });


        // WORD HUNT BUTTON (temporary redirect example)
        buttonWord.setOnClickListener(v -> {

            Intent intent = new Intent(GameOption.this, Addition_Game.class);
            startActivity(intent);

        });


        // PUZZLE WORLD BUTTON
        buttonPuzzle.setOnClickListener(v -> {

            Intent intent = new Intent(GameOption.this, Addition_Game.class);
            startActivity(intent);

        });


        // SCIENCE BUTTON
        buttonScience.setOnClickListener(v -> {

            Intent intent = new Intent(GameOption.this, science_category.class);
            startActivity(intent);

        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {

            Insets systemBars =
                    insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );

            return insets;

        });

    }
}