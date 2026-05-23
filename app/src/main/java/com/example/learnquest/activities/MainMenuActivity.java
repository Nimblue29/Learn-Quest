package com.example.learnquest.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.learnquest.R;
import com.example.learnquest.utils.SoundManager;

/**
 * MainMenuActivity — the landing/splash screen for LearnQuest.
 *
 * Shows the app logo, player greeting, a big PLAY button, a SETTINGS
 * row (sound toggle + about), and animated entry on first launch.
 *
 * To wire this as the launcher, update AndroidManifest.xml:
 *   1. Change MainMenuActivity intent-filter to MAIN / LAUNCHER
 *   2. Remove the LAUNCHER filter from HomeActivity (keep it exported=false)
 */
public class MainMenuActivity extends AppCompatActivity {

    private static final long ANIM_DURATION = 400L;

    // Views
    private TextView tvLogo, tvLogoSub, tvTagline;
    private TextView btnPlay, btnDashboard, btnSoundToggle, btnAbout;
    private LinearLayout menuCard, logoSection;
    private TextView tvVersion, tvPlayerChip;

    private SharedPreferences prefs;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        prefs = getSharedPreferences("learnquest_prefs", MODE_PRIVATE);

        bindViews();
        updateSoundIcon();
        loadPlayerGreeting();
        animateEntry();
        setClickListeners();

        // Play start-up sound after short delay
        handler.postDelayed(() -> SoundManager.get(this).playStart(), 600);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlayerGreeting(); // refresh star count etc.
        updateSoundIcon();
    }

    // ── Bind ──────────────────────────────────────────────────────────────────
    private void bindViews() {
        tvLogo        = findViewById(R.id.tvMainLogo);
        tvLogoSub     = findViewById(R.id.tvMainLogoSub);
        tvTagline     = findViewById(R.id.tvMainTagline);
        btnPlay       = findViewById(R.id.btnMainPlay);
        btnDashboard  = findViewById(R.id.btnMainDashboard);
        btnSoundToggle= findViewById(R.id.btnSoundToggle);
        btnAbout      = findViewById(R.id.btnAbout);
        menuCard      = findViewById(R.id.menuCard);
        logoSection   = findViewById(R.id.logoSection);
        tvVersion     = findViewById(R.id.tvVersion);
        tvPlayerChip  = findViewById(R.id.tvPlayerChip);
    }

    // ── Data ──────────────────────────────────────────────────────────────────
    private void loadPlayerGreeting() {
        String name  = prefs.getString("player_name", "Player");
        int stars    = prefs.getInt("total_stars", 0);
        int streak   = prefs.getInt("streak_days", 0);
        String chip  = "👤 " + name + "  ⭐ " + stars + "  🔥 " + streak;
        tvPlayerChip.setText(chip);
    }

    // ── Animations ────────────────────────────────────────────────────────────
    private void animateEntry() {
        // Logo: fade + slide down
        logoSection.setAlpha(0f);
        logoSection.setTranslationY(-60f);
        ObjectAnimator logoFade  = ObjectAnimator.ofFloat(logoSection, "alpha", 0f, 1f);
        ObjectAnimator logoSlide = ObjectAnimator.ofFloat(logoSection, "translationY", -60f, 0f);
        AnimatorSet logoAnim = new AnimatorSet();
        logoAnim.playTogether(logoFade, logoSlide);
        logoAnim.setDuration(ANIM_DURATION + 100);
        logoAnim.setInterpolator(new OvershootInterpolator(1.1f));

        // Menu card: fade + slide up
        menuCard.setAlpha(0f);
        menuCard.setTranslationY(80f);
        ObjectAnimator cardFade  = ObjectAnimator.ofFloat(menuCard, "alpha", 0f, 1f);
        ObjectAnimator cardSlide = ObjectAnimator.ofFloat(menuCard, "translationY", 80f, 0f);
        AnimatorSet cardAnim = new AnimatorSet();
        cardAnim.playTogether(cardFade, cardSlide);
        cardAnim.setDuration(ANIM_DURATION + 150);
        cardAnim.setStartDelay(200);
        cardAnim.setInterpolator(new OvershootInterpolator(1.1f));

        // Play button: bounce pop
        btnPlay.setScaleX(0.6f);
        btnPlay.setScaleY(0.6f);
        ObjectAnimator btnScaleX = ObjectAnimator.ofFloat(btnPlay, "scaleX", 0.6f, 1f);
        ObjectAnimator btnScaleY = ObjectAnimator.ofFloat(btnPlay, "scaleY", 0.6f, 1f);
        AnimatorSet btnAnim = new AnimatorSet();
        btnAnim.playTogether(btnScaleX, btnScaleY);
        btnAnim.setDuration(500);
        btnAnim.setStartDelay(450);
        btnAnim.setInterpolator(new BounceInterpolator());

        logoAnim.start();
        cardAnim.start();
        btnAnim.start();
    }

    private void pulseButton(View v) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(v, "scaleX", 1f, 0.93f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(v, "scaleY", 1f, 0.93f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(180);
        set.start();
    }

    // ── Click Listeners ───────────────────────────────────────────────────────
    private void setClickListeners() {

        btnPlay.setOnClickListener(v -> {
            SoundManager.get(this).playClick();
            pulseButton(v);
            handler.postDelayed(() ->
                startActivity(new Intent(this, HomeActivity.class)), 180);
        });

        btnDashboard.setOnClickListener(v -> {
            SoundManager.get(this).playClick();
            startActivity(new Intent(this, DashboardActivity.class));
        });

        btnSoundToggle.setOnClickListener(v -> {
            SoundManager.get(this).toggleSound();
            updateSoundIcon();
            // Play a quick click so user hears if sound just turned on
            if (SoundManager.get(this).isSoundEnabled()) {
                SoundManager.get(this).playClick();
            }
        });

        btnAbout.setOnClickListener(v -> {
            SoundManager.get(this).playClick();
            showAboutDialog();
        });

        // Tap the player chip to "set name" (simple input dialog)
        tvPlayerChip.setOnClickListener(v -> showNameDialog());
    }

    // ── Sound icon ────────────────────────────────────────────────────────────
    private void updateSoundIcon() {
        boolean on = SoundManager.get(this).isSoundEnabled();
        btnSoundToggle.setText(on ? "🔊  Sound On" : "🔇  Sound Off");
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────
    private void showAboutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("About LearnQuest")
            .setMessage(
                "LearnQuest is a game-based mobile learning app for children.\n\n" +
                "📚 Spelling · ⚔️ Math · 🧩 Puzzles · 📝 Quizzes\n\n" +
                "Learn through play — every correct answer earns stars and badges!\n\n" +
                "Version 1.0 · BSIT-2C · Paul Ace Ocampo"
            )
            .setPositiveButton("Let's Play! 🎮", null)
            .show();
    }

    private void showNameDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Enter your name");
        input.setText(prefs.getString("player_name", ""));
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                           android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setPadding(50, 30, 50, 10);

        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Change Player Name")
            .setView(input)
            .setPositiveButton("Save", (d, w) -> {
                String name = input.getText().toString().trim();
                if (!name.isEmpty()) {
                    prefs.edit().putString("player_name", name).apply();
                    loadPlayerGreeting();
                    SoundManager.get(this).playCorrect();
                    Toast.makeText(this, "Hi, " + name + "! 👋", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
