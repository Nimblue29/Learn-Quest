package com.example.learnquest.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.learnquest.R;

public class DashboardActivity extends AppCompatActivity {

    private static final String[] DAY_LABELS = {"MON","TUE","WED","THU","FRI","SAT","SUN"};
    private static final int TODAY_IDX = 6; // Sunday = last position

    private static final String[][] BADGES = {
        {"🏆","Champion",    "true"},
        {"🔥","7-Day Streak","true"},
        {"⭐","Star Collector","true"},
        {"🚀","Speed Master","false"},
        {"🧠","Big Brain",   "true"},
        {"💎","Diamond",     "false"},
        {"🌈","All Games",   "false"},
        {"👑","King/Queen",  "false"},
    };

    private TextView btnBack, tvPlayerName, tvLevel, tvXpLabel;
    private TextView tvStreak, tvWeekStars, tvCorrectRate, tvGamesPlayed;
    private LinearLayout streakDaysLayout, badgesLayout;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        prefs = getSharedPreferences("learnquest_prefs", MODE_PRIVATE);
        bindViews();
        loadData();
        buildStreakDays();
        buildBadges();
        btnBack.setOnClickListener(v -> finish());
    }

    private void bindViews() {
        btnBack         = findViewById(R.id.btnBack);
        tvPlayerName    = findViewById(R.id.tvPlayerName);
        tvLevel         = findViewById(R.id.tvLevel);
        tvXpLabel       = findViewById(R.id.tvXpLabel);
        tvStreak        = findViewById(R.id.tvStreak);
        tvWeekStars     = findViewById(R.id.tvWeekStars);
        tvCorrectRate   = findViewById(R.id.tvCorrectRate);
        tvGamesPlayed   = findViewById(R.id.tvGamesPlayed);
        streakDaysLayout= findViewById(R.id.streakDaysLayout);
        badgesLayout    = findViewById(R.id.badgesLayout);
    }

    private void loadData() {
        int totalStars  = prefs.getInt("total_stars", 0);
        int gamesPlayed = prefs.getInt("games_played", 32);
        int streakDays  = prefs.getInt("streak_days", 7);
        int correctRate = prefs.getInt("correct_rate", 91);

        // XP: 100 stars = 1 level (simple formula)
        int level = Math.max(1, totalStars / 100);
        int xp    = totalStars % 100;

        tvPlayerName.setText(prefs.getString("player_name", "Paul Ace"));
        tvLevel.setText("Lv. " + level);
        tvXpLabel.setText(xp + " / 100 XP to next level");
        tvStreak.setText(String.valueOf(streakDays));
        tvWeekStars.setText(String.valueOf(totalStars));
        tvCorrectRate.setText(correctRate + "%");
        tvGamesPlayed.setText(String.valueOf(gamesPlayed));
    }

    // ── Streak Days ────────────────────────────────────────────────────────────
    private void buildStreakDays() {
        streakDaysLayout.removeAllViews();
        int streakCount = prefs.getInt("streak_days", 7);

        for (int i = 0; i < 7; i++) {
            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.weight = 1;
            col.setLayoutParams(lp);

            // Day circle
            TextView circle = new TextView(this);
            int dp36 = dpToPx(36);
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(dp36, dp36);
            clp.setMargins(dpToPx(2), 0, dpToPx(2), dpToPx(4));
            circle.setLayoutParams(clp);
            circle.setGravity(Gravity.CENTER);
            circle.setTextSize(16);

            boolean isDone  = i < streakCount && i < TODAY_IDX;
            boolean isToday = i == TODAY_IDX;

            if (isDone) {
                circle.setText("⭐");
                circle.setBackgroundResource(R.drawable.bg_pill_orange);
            } else if (isToday) {
                circle.setText("📅");
                circle.setBackgroundResource(R.drawable.bg_progress_track);
            } else {
                circle.setText("○");
                circle.setBackgroundResource(R.drawable.bg_progress_track);
                circle.setTextColor(getResources().getColor(R.color.text_muted));
            }

            // Day label
            TextView label = new TextView(this);
            label.setText(DAY_LABELS[i]);
            label.setTextSize(10);
            label.setTextColor(getResources().getColor(R.color.text_muted));
            label.setTypeface(null, android.graphics.Typeface.BOLD);
            label.setGravity(Gravity.CENTER);

            col.addView(circle);
            col.addView(label);
            streakDaysLayout.addView(col);
        }
    }

    // ── Badges ─────────────────────────────────────────────────────────────────
    private void buildBadges() {
        badgesLayout.removeAllViews();
        badgesLayout.setOrientation(LinearLayout.HORIZONTAL);

        for (String[] badge : BADGES) {
            String emoji   = badge[0];
            String name    = badge[1];
            boolean earned = badge[2].equals("true");

            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            item.setBackgroundResource(R.drawable.bg_card_white);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.weight = 1;
            lp.setMargins(dpToPx(5), 0, dpToPx(5), 0);
            item.setLayoutParams(lp);
            item.setPadding(dpToPx(8), dpToPx(10), dpToPx(8), dpToPx(10));
            if (!earned) item.setAlpha(0.4f);

            TextView tvEmoji = new TextView(this);
            tvEmoji.setText(emoji);
            tvEmoji.setTextSize(24);
            tvEmoji.setGravity(Gravity.CENTER);

            TextView tvName = new TextView(this);
            tvName.setText(name);
            tvName.setTextSize(9);
            tvName.setTextColor(getResources().getColor(R.color.text_muted));
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);
            tvName.setGravity(Gravity.CENTER);

            item.addView(tvEmoji);
            item.addView(tvName);
            badgesLayout.addView(item);

            // Only show 4 per row — wrap into rows of 4
            if (badgesLayout.getChildCount() == 4) {
                // Move to second row (simple approach: add a new row below)
                // For full badge grid, use a custom GridLayout or RecyclerView
            }
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
