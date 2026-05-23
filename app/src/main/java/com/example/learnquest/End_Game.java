package com.example.learnquest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class End_Game extends AppCompatActivity {

    TextView resultScore;

    Button buttonTryAgain;
    Button buttonChangeCategory;
    Button buttonMainMenu;

    int score;
    String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_game);


        resultScore = findViewById(R.id.resultScore);

        buttonTryAgain = findViewById(R.id.buttonTryAgain);
        buttonChangeCategory = findViewById(R.id.buttonChangeCategory);
        buttonMainMenu = findViewById(R.id.buttonMainMenu);


        score = getIntent().getIntExtra("FINAL_SCORE",0);
        category = getIntent().getStringExtra("CATEGORY");


        resultScore.setText("Total Score: " + score + "/10");



        buttonTryAgain.setOnClickListener(v -> {

            Intent intent = new Intent(this, science_quiz.class);

            intent.putExtra("CATEGORY", category);

            startActivity(intent);

            finish();

        });



        buttonChangeCategory.setOnClickListener(v -> {

            Intent intent = new Intent(this, science_category.class);

            startActivity(intent);

            finish();

        });



        buttonMainMenu.setOnClickListener(v -> {

            Intent intent = new Intent(this, GameOption.class);

            startActivity(intent);

            finish();

        });

    }
}