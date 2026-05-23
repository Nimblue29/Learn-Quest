package com.example.learnquest.activities;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.learnquest.R;
import com.example.learnquest.utils.SoundManager;
import androidx.appcompat.app.AlertDialog;

/**
 * HomeActivity — game selection hub.
 *
 * Changes vs. original:
 *  • SoundManager click sounds on every card / button
 *  • QuizActivity is now wired up (was a TODO)
 *  • Streak days loaded from SharedPreferences instead of hardcoded
 *  • Greeting shows saved player name
 *  • onResume refreshes stars AND streak
 */
public class HomeActivity extends AppCompatActivity {

    private TextView tvTotalStars, tvGreeting, tvStreakCount, tvStreakDay;
    private LinearLayout cardSpelling, cardMath, cardPuzzle, cardQuiz;
    private TextView btnProgress;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        prefs = getSharedPreferences("learnquest_prefs", MODE_PRIVATE);

        bindViews();
        loadStats();
        setClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats(); // refresh stars + streak when returning from any game
    }

    // ── Bind ──────────────────────────────────────────────────────────────────
    private void bindViews() {
        tvTotalStars  = findViewById(R.id.tvTotalStars);
        tvGreeting    = findViewById(R.id.tvGreeting);
        tvStreakCount = findViewById(R.id.tvStreakDayCount); // optional: may be null if layout unchanged
        tvStreakDay   = findViewById(R.id.tvStreakDayLabel); // optional
        cardSpelling  = findViewById(R.id.cardSpelling);
        cardMath      = findViewById(R.id.cardMath);
        cardPuzzle    = findViewById(R.id.cardPuzzle);
        cardQuiz      = findViewById(R.id.cardQuiz);
        btnProgress   = findViewById(R.id.btnProgress);
    }

    // ── Data ──────────────────────────────────────────────────────────────────
    private void loadStats() {
        int totalStars = prefs.getInt("total_stars", 0);
        int streak     = prefs.getInt("streak_days", 0);
        String name    = prefs.getString("player_name", "Explorer");

        tvTotalStars.setText(String.valueOf(totalStars));

        // Update greeting with player name
        if (tvGreeting != null) {
            tvGreeting.setText("Hi, " + name + "! 👋");
        }

        // Update streak pill text if the view exists in layout
        if (tvStreakCount != null) {
            tvStreakCount.setText(streak + "-Day Streak!");
        }
        if (tvStreakDay != null) {
            tvStreakDay.setText("Day " + streak);
        }
    }

    // ── Click Listeners ───────────────────────────────────────────────────────
    private void setClickListeners() {

        cardSpelling.setOnClickListener(v -> {
            SoundManager.get(this).playClick();
            pulseCard(v);
            
            android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_difficulty, null);
            AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
                
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
            
            dialogView.findViewById(R.id.btnEasy).setOnClickListener(btn -> {
                dialog.dismiss();
                Intent intent = new Intent(this, SpellingActivity.class);
                intent.putExtra("difficulty", "easy");
                startActivity(intent);
            });
            dialogView.findViewById(R.id.btnMedium).setOnClickListener(btn -> {
                dialog.dismiss();
                Intent intent = new Intent(this, SpellingActivity.class);
                intent.putExtra("difficulty", "medium");
                startActivity(intent);
            });
            dialogView.findViewById(R.id.btnHard).setOnClickListener(btn -> {
                dialog.dismiss();
                Intent intent = new Intent(this, SpellingActivity.class);
                intent.putExtra("difficulty", "hard");
                startActivity(intent);
            });
            
            dialog.show();
        });

        cardMath.setOnClickListener(v -> {
            SoundManager.get(this).playClick();
            pulseCard(v);
            v.postDelayed(() -> startActivity(
                    new Intent(this, MathBattleActivity.class)), 150);
        });

        cardPuzzle.setOnClickListener(v -> {
            SoundManager.get(this).playClick();
            pulseCard(v);
            v.postDelayed(() -> startActivity(
                    new Intent(this, PuzzleActivity.class)), 150);
        });

        // ✅ QuizActivity is now wired up
        cardQuiz.setOnClickListener(v -> {
            SoundManager.get(this).playClick();
            pulseCard(v);
            v.postDelayed(() -> startActivity(
                    new Intent(this, QuizActivity.class)), 150);
        });

        btnProgress.setOnClickListener(v -> {
            SoundManager.get(this).playClick();
            startActivity(new Intent(this, DashboardActivity.class));
        });
    }

    // ── Animations ────────────────────────────────────────────────────────────
    private void pulseCard(android.view.View v) {
        ObjectAnimator sx = ObjectAnimator.ofFloat(v, "scaleX", 1f, 0.92f, 1f);
        ObjectAnimator sy = ObjectAnimator.ofFloat(v, "scaleY", 1f, 0.92f, 1f);
        android.animation.AnimatorSet set = new android.animation.AnimatorSet();
        set.playTogether(sx, sy);
        set.setDuration(200);
        set.start();
    }
}
