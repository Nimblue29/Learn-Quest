package com.example.learnquest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class science_category extends AppCompatActivity {

    Button buttonAnimals, buttonPlants, buttonEarth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_science_category);

        // CONNECT BUTTONS
        buttonAnimals = findViewById(R.id.buttonAnimals);
        buttonPlants = findViewById(R.id.buttonPlants);
        buttonEarth = findViewById(R.id.buttonEarth);

        // ANIMALS BUTTON
        buttonAnimals.setOnClickListener(v -> {

            Intent intent = new Intent(this, science_quiz.class);
            intent.putExtra("CATEGORY", "animals");
            startActivity(intent);

        });

        // PLANTS BUTTON
        buttonPlants.setOnClickListener(v -> {

            Intent intent = new Intent(this, science_quiz.class);
            intent.putExtra("CATEGORY", "plants");
            startActivity(intent);

        });

        // EARTH BUTTON
        buttonEarth.setOnClickListener(v -> {

            Intent intent = new Intent(this, science_quiz.class);
            intent.putExtra("CATEGORY", "earth");
            startActivity(intent);

        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {

            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

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