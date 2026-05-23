package com.example.learnquest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class science_quiz extends AppCompatActivity {

    TextView questionText;
    Button choice1, choice2, choice3, nextBtn;

    String category;

    int score = 0;
    int questionIndex = 0;

    String selectedAnswer = "";
    Button selectedButton = null;


    // ================= ANIMALS =================
    String[][] animalsQuestions = {

            {"Which animal says meow?", "Dog", "Cat", "Cow", "Cat"},
            {"Which animal says moo?", "Cow", "Bird", "Goat", "Cow"},
            {"Which animal can fly?", "Fish", "Bird", "Horse", "Bird"},
            {"Which animal lives in water?", "Fish", "Tiger", "Dog", "Fish"},
            {"Which animal has long ears?", "Rabbit", "Lion", "Snake", "Rabbit"},
            {"Which animal barks?", "Dog", "Cat", "Duck", "Dog"},
            {"Which animal has stripes?", "Zebra", "Cow", "Sheep", "Zebra"},
            {"Which animal is very big?", "Elephant", "Ant", "Mouse", "Elephant"},
            {"Which animal swims in the sea?", "Dolphin", "Chicken", "Goat", "Dolphin"},
            {"Which animal lays eggs?", "Chicken", "Dog", "Cat", "Chicken"}
    };


    // ================= PLANTS =================
    String[][] plantsQuestions = {

            {"What do plants need to grow?", "Water", "Toys", "Shoes", "Water"},
            {"What color are most leaves?", "Blue", "Green", "Black", "Green"},
            {"What grows from a seed?", "Plant", "Rock", "Chair", "Plant"},
            {"Which part is under the soil?", "Roots", "Leaves", "Flowers", "Roots"},
            {"Which part is green and flat?", "Leaf", "Root", "Stem", "Leaf"},
            {"Which part is colorful?", "Flower", "Root", "Stem", "Flower"},
            {"Plants need sunlight to grow?", "True", "False", "Maybe", "True"},
            {"Which fruit grows on a tree?", "Apple", "Candy", "Bread", "Apple"},
            {"Which part makes food for plant?", "Leaves", "Roots", "Soil", "Leaves"},
            {"Plants give us oxygen?", "True", "False", "Maybe", "True"}
    };


    // ================= EARTH =================
    String[][] earthQuestions = {

            {"What shines in daytime sky?", "Sun", "Moon", "Star", "Sun"},
            {"What shines at night?", "Moon", "Sun", "Cloud", "Moon"},
            {"What falls when it rains?", "Rain", "Leaves", "Sand", "Rain"},
            {"Where do fish live?", "Water", "Sky", "Tree", "Water"},
            {"What appears after rain and sunshine?", "Rainbow", "Snow", "Smoke", "Rainbow"},
            {"What moves leaves on trees?", "Wind", "Rock", "Chair", "Wind"},
            {"Which is part of the Earth?", "Land", "Laptop", "Phone", "Land"},
            {"What do we breathe?", "Air", "Milk", "Juice", "Air"},
            {"Which one is hot?", "Sun", "Ice", "Snow", "Sun"},
            {"Which grows in nature?", "Tree", "Car", "TV", "Tree"}
    };


    String[][] selectedQuestions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_science_quiz);

        questionText = findViewById(R.id.questionText);
        choice1 = findViewById(R.id.choice1);
        choice2 = findViewById(R.id.choice2);
        choice3 = findViewById(R.id.choice3);
        nextBtn = findViewById(R.id.nextBtn);


        category = getIntent().getStringExtra("CATEGORY");

        if (category == null)
            category = "animals";


        if (category.equals("animals"))
            selectedQuestions = animalsQuestions;

        else if (category.equals("plants"))
            selectedQuestions = plantsQuestions;

        else
            selectedQuestions = earthQuestions;


        loadQuestion();


        choice1.setOnClickListener(v -> selectAnswer(choice1));
        choice2.setOnClickListener(v -> selectAnswer(choice2));
        choice3.setOnClickListener(v -> selectAnswer(choice3));


        nextBtn.setOnClickListener(v -> {

            if(selectedAnswer.equals("")) {

                Toast.makeText(this,
                        "Please choose an answer first",
                        Toast.LENGTH_SHORT).show();
                return;
            }


            if(selectedAnswer.equals(
                    selectedQuestions[questionIndex][4])) {

                score++;

                Toast.makeText(this,
                        "Correct!",
                        Toast.LENGTH_SHORT).show();

            } else {

                Toast.makeText(this,
                        "Wrong!",
                        Toast.LENGTH_SHORT).show();
            }


            questionIndex++;
            selectedAnswer = "";


            if(questionIndex < selectedQuestions.length) {

                loadQuestion();

            } else {

                // ✅ FINAL RESULT SCREEN REDIRECT
                Intent intent = new Intent(this, End_Game.class);

                intent.putExtra("FINAL_SCORE", score);
                intent.putExtra("CATEGORY", category);

                startActivity(intent);
                finish();

            }

        });

    }


    void selectAnswer(Button button){

        selectedAnswer = button.getText().toString();

        if(selectedButton != null)
            selectedButton.setAlpha(1.0f);

        button.setAlpha(0.6f);

        selectedButton = button;

    }


    void loadQuestion(){

        questionText.setText(selectedQuestions[questionIndex][0]);

        choice1.setText(selectedQuestions[questionIndex][1]);
        choice2.setText(selectedQuestions[questionIndex][2]);
        choice3.setText(selectedQuestions[questionIndex][3]);

        choice1.setAlpha(1.0f);
        choice2.setAlpha(1.0f);
        choice3.setAlpha(1.0f);

        selectedButton = null;

    }

}