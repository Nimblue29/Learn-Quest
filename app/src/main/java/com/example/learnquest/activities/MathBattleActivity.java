package com.example.learnquest.activities;

import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.learnquest.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MathBattleActivity extends AppCompatActivity {

    // ── Constants ─────────────────────────────────────────────────────────────
    private static final int TOTAL_QUESTIONS = 7;
    private static final int TIME_PER_Q      = 10; // seconds
    private static final String[] OPS        = {"+", "-", "×"};

    // ── State ─────────────────────────────────────────────────────────────────
    private int currentQ     = 0;
    private int playerPts    = 0;
    private int roboPts      = 0;
    private int lives        = 3;
    private int streak       = 0;
    private int bestStreak   = 0;
    private int correctCount = 0;
    private long totalTimeMs = 0;
    private boolean answered = false;
    private long qStartTime  = 0;
    private int correctAnswer;
    private int[] choiceValues = new int[4];
    private CountDownTimer countDownTimer;
    private final Random random = new Random();
    private final Handler handler = new Handler();

    // ── Views ─────────────────────────────────────────────────────────────────
    private TextView tvTotalScore, tvPlayerScore, tvRoboScore;
    private TextView tvTimeNum, tvQuestion, tvHintRow, tvFeedbackToast;
    private TextView tvStreakBadge;
    private TextView heart1, heart2, heart3;
    private View timerFill;
    private TextView btnAns1, btnAns2, btnAns3, btnAns4;
    private LinearLayout progressDotsLayout;
    private LinearLayout resultCard;
    private TextView tvResultEmoji, tvResultTitle, tvResultSub;
    private TextView tvStatCorrect, tvStatScore, tvStatStreak, tvStatTime;
    private TextView btnPlayAgain, btnBack;
    private boolean[] dotResults;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math_battle);
        prefs = getSharedPreferences("learnquest_prefs", MODE_PRIVATE);
        bindViews();
        dotResults = new boolean[TOTAL_QUESTIONS];
        setClickListeners();
        startGame();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }

    private void bindViews() {
        tvTotalScore    = findViewById(R.id.tvTotalScore);
        tvPlayerScore   = findViewById(R.id.tvPlayerScore);
        tvRoboScore     = findViewById(R.id.tvRoboScore);
        tvTimeNum       = findViewById(R.id.tvTimeNum);
        tvQuestion      = findViewById(R.id.tvQuestion);
        tvHintRow       = findViewById(R.id.tvHintRow);
        tvFeedbackToast = findViewById(R.id.tvFeedbackToast);
        tvStreakBadge   = findViewById(R.id.tvStreakBadge);
        heart1          = findViewById(R.id.heart1);
        heart2          = findViewById(R.id.heart2);
        heart3          = findViewById(R.id.heart3);
        timerFill       = findViewById(R.id.timerFill);
        btnAns1         = findViewById(R.id.btnAns1);
        btnAns2         = findViewById(R.id.btnAns2);
        btnAns3         = findViewById(R.id.btnAns3);
        btnAns4         = findViewById(R.id.btnAns4);
        progressDotsLayout = findViewById(R.id.progressDotsLayout);
        resultCard      = findViewById(R.id.resultCard);
        tvResultEmoji   = findViewById(R.id.tvResultEmoji);
        tvResultTitle   = findViewById(R.id.tvResultTitle);
        tvResultSub     = findViewById(R.id.tvResultSub);
        tvStatCorrect   = findViewById(R.id.tvStatCorrect);
        tvStatScore     = findViewById(R.id.tvStatScore);
        tvStatStreak    = findViewById(R.id.tvStatStreak);
        tvStatTime      = findViewById(R.id.tvStatTime);
        btnPlayAgain    = findViewById(R.id.btnPlayAgain);
        btnBack         = findViewById(R.id.btnBack);
    }

    private void setClickListeners() {
        btnBack.setOnClickListener(v -> { if (countDownTimer != null) countDownTimer.cancel(); finish(); });
        btnAns1.setOnClickListener(v -> submitAnswer(btnAns1, choiceValues[0]));
        btnAns2.setOnClickListener(v -> submitAnswer(btnAns2, choiceValues[1]));
        btnAns3.setOnClickListener(v -> submitAnswer(btnAns3, choiceValues[2]));
        btnAns4.setOnClickListener(v -> submitAnswer(btnAns4, choiceValues[3]));
        btnPlayAgain.setOnClickListener(v -> restartGame());
    }

    // ── Game Flow ─────────────────────────────────────────────────────────────
    private void startGame() {
        buildProgressDots();
        loadQuestion();
    }

    private void loadQuestion() {
        if (currentQ >= TOTAL_QUESTIONS) { endGame(); return; }
        answered   = false;
        qStartTime = System.currentTimeMillis();
        tvHintRow.setText("");
        setAnswerButtonsEnabled(true);
        resetAnswerButtonStyles();

        // Generate question
        String op   = OPS[random.nextInt(OPS.length)];
        int a, b;
        if (op.equals("+")) {
            a = random.nextInt(9) + 1; b = random.nextInt(9) + 1;
            correctAnswer = a + b;
        } else if (op.equals("-")) {
            a = random.nextInt(8) + 2; b = random.nextInt(a) + 1;
            correctAnswer = a - b;
        } else { // ×
            a = random.nextInt(5) + 1; b = random.nextInt(5) + 1;
            correctAnswer = a * b;
        }

        tvQuestion.setText(a + " " + op + " " + b + " = ?");

        // Generate choices
        List<Integer> choices = generateChoices(correctAnswer);
        choiceValues[0] = choices.get(0);
        choiceValues[1] = choices.get(1);
        choiceValues[2] = choices.get(2);
        choiceValues[3] = choices.get(3);
        btnAns1.setText(String.valueOf(choiceValues[0]));
        btnAns2.setText(String.valueOf(choiceValues[1]));
        btnAns3.setText(String.valueOf(choiceValues[2]));
        btnAns4.setText(String.valueOf(choiceValues[3]));

        startTimer();
        updateDot(currentQ, "active");
    }

    private List<Integer> generateChoices(int correct) {
        List<Integer> choices = new ArrayList<>();
        choices.add(correct);
        while (choices.size() < 4) {
            int offset = random.nextInt(6) + 1;
            int wrong  = random.nextBoolean() ? correct + offset : Math.max(0, correct - offset);
            if (!choices.contains(wrong)) choices.add(wrong);
        }
        Collections.shuffle(choices);
        return choices;
    }

    // ── Timer ─────────────────────────────────────────────────────────────────
    private void startTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        countDownTimer = new CountDownTimer(TIME_PER_Q * 1000L, 1000) {
            @Override
            public void onTick(long ms) {
                int secsLeft = (int) (ms / 1000);
                tvTimeNum.setText(secsLeft + "s");
                updateTimerBar(secsLeft);
                if (secsLeft <= 3) tvHintRow.setText("Hurry up! ⏰");
            }
            @Override
            public void onFinish() {
                if (!answered) timeUp();
            }
        }.start();
    }

    private void updateTimerBar(int secsLeft) {
        float pct = (float) secsLeft / TIME_PER_Q;

        // Animate width
        FrameLayout track = (FrameLayout) timerFill.getParent();
        int targetW = (int) (track.getWidth() * pct);
        ObjectAnimator.ofInt(timerFill, "right",
                timerFill.getLeft(), timerFill.getLeft() + targetW)
                .setDuration(800).start();

        // Change color
        if (secsLeft <= 3)
            timerFill.setBackgroundResource(R.drawable.bg_timer_fill_red);
        else if (secsLeft <= 6)
            timerFill.setBackgroundResource(R.drawable.bg_timer_fill_orange);
        else
            timerFill.setBackgroundResource(R.drawable.bg_timer_fill_blue);
    }

    private void timeUp() {
        answered = true;
        countDownTimer.cancel();
        streak = 0; updateStreakBadge();
        lives = Math.max(0, lives - 1); updateLives();
        roboPts += 10; tvRoboScore.setText(String.valueOf(roboPts));
        updateDot(currentQ, "wrong");
        showToast("Time Up!", false);
        setAnswerButtonsEnabled(false);
        handler.postDelayed(() -> { currentQ++; if (lives <= 0) endGame(); else loadQuestion(); }, 1100);
    }

    // ── Submit Answer ─────────────────────────────────────────────────────────
    private void submitAnswer(TextView btn, int chosen) {
        if (answered) return;
        answered = true;
        countDownTimer.cancel();
        long elapsed = System.currentTimeMillis() - qStartTime;
        totalTimeMs += elapsed;
        setAnswerButtonsEnabled(false);

        if (chosen == correctAnswer) {
            btn.setBackgroundResource(R.drawable.bg_answer_correct);
            streak++;
            bestStreak = Math.max(bestStreak, streak);
            correctCount++;
            int bonus = streak >= 3 ? 20 : streak >= 2 ? 15 : 10;
            playerPts += bonus;
            tvPlayerScore.setText(String.valueOf(playerPts));
            tvTotalScore.setText(String.valueOf(playerPts));
            updateStreakBadge();
            showToast((streak >= 2 ? "🔥" : "") + "+" + bonus, true);
            updateDot(currentQ, "correct");
        } else {
            btn.setBackgroundResource(R.drawable.bg_answer_wrong);
            // Highlight correct
            highlightCorrectButton();
            streak = 0; updateStreakBadge();
            lives = Math.max(0, lives - 1); updateLives();
            roboPts += 10; tvRoboScore.setText(String.valueOf(roboPts));
            showToast("Oops!", false);
            updateDot(currentQ, "wrong");
        }

        handler.postDelayed(() -> {
            currentQ++;
            if (lives <= 0) endGame();
            else loadQuestion();
        }, 1200);
    }

    private void highlightCorrectButton() {
        TextView[] btns = {btnAns1, btnAns2, btnAns3, btnAns4};
        for (int i = 0; i < btns.length; i++) {
            if (choiceValues[i] == correctAnswer) {
                btns[i].setBackgroundResource(R.drawable.bg_answer_correct);
                break;
            }
        }
    }

    // ── Toast Feedback ────────────────────────────────────────────────────────
    private void showToast(String text, boolean positive) {
        tvFeedbackToast.setText(text);
        tvFeedbackToast.setBackgroundResource(positive
                ? R.drawable.bg_pill_green : R.drawable.bg_pill_pink);
        tvFeedbackToast.setVisibility(View.VISIBLE);
        tvFeedbackToast.setAlpha(0f);
        tvFeedbackToast.setScaleX(0.5f);
        tvFeedbackToast.setScaleY(0.5f);
        tvFeedbackToast.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(200).start();
        handler.postDelayed(() -> tvFeedbackToast.setVisibility(View.GONE), 900);
    }

    // ── End Game ──────────────────────────────────────────────────────────────
    private void endGame() {
        if (countDownTimer != null) countDownTimer.cancel();
        // Save stars
        int saved = prefs.getInt("total_stars", 0);
        prefs.edit().putInt("total_stars", saved + playerPts).apply();

        boolean won = playerPts >= roboPts;
        tvResultEmoji.setText(won ? "🏆" : "🤖");
        tvResultTitle.setText(won ? "You Win!" : "Robo Wins!");
        tvResultSub.setText(won ? "Amazing math skills!" : "Better luck next time!");
        tvStatCorrect.setText(correctCount + "/" + TOTAL_QUESTIONS);
        tvStatScore.setText(String.valueOf(playerPts));
        tvStatStreak.setText(String.valueOf(bestStreak));
        int avgSec = currentQ > 0 ? (int) (totalTimeMs / currentQ / 1000) : 0;
        tvStatTime.setText(avgSec + "s");

        // Hide game views, show result
        tvQuestion.setText("");
        btnAns1.setVisibility(View.GONE);
        btnAns2.setVisibility(View.GONE);
        btnAns3.setVisibility(View.GONE);
        btnAns4.setVisibility(View.GONE);
        resultCard.setVisibility(View.VISIBLE);
    }

    private void restartGame() {
        currentQ = 0; playerPts = 0; roboPts = 0; lives = 3;
        streak = 0; bestStreak = 0; correctCount = 0; totalTimeMs = 0;
        dotResults = new boolean[TOTAL_QUESTIONS];
        tvPlayerScore.setText("0"); tvRoboScore.setText("0"); tvTotalScore.setText("0");
        tvStreakBadge.setVisibility(View.GONE);
        updateLives();
        resultCard.setVisibility(View.GONE);
        btnAns1.setVisibility(View.VISIBLE); btnAns2.setVisibility(View.VISIBLE);
        btnAns3.setVisibility(View.VISIBLE); btnAns4.setVisibility(View.VISIBLE);
        timerFill.setBackgroundResource(R.drawable.bg_timer_fill_blue);
        buildProgressDots();
        loadQuestion();
    }

    // ── Progress Dots ─────────────────────────────────────────────────────────
    private void buildProgressDots() {
        progressDotsLayout.removeAllViews();
        int dp10 = dpToPx(10);
        int dp4  = dpToPx(4);
        for (int i = 0; i < TOTAL_QUESTIONS; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp10, dp10);
            lp.setMargins(dp4, 0, dp4, 0);
            dot.setLayoutParams(lp);
            dot.setTag("dot_" + i);
            dot.setBackgroundResource(R.drawable.bg_progress_track);
            progressDotsLayout.addView(dot);
        }
    }

    private void updateDot(int idx, String state) {
        View dot = progressDotsLayout.findViewWithTag("dot_" + idx);
        if (dot == null) return;
        switch (state) {
            case "correct": dot.setBackgroundResource(R.drawable.bg_tile_green); break;
            case "wrong":   dot.setBackgroundResource(R.drawable.bg_tile_pink);  break;
            case "active":  dot.setBackgroundResource(R.drawable.bg_pill_blue);  break;
        }
    }

    // ── UI Helpers ────────────────────────────────────────────────────────────
    private void updateLives() {
        heart1.setText(lives >= 1 ? "❤️" : "🖤");
        heart2.setText(lives >= 2 ? "❤️" : "🖤");
        heart3.setText(lives >= 3 ? "❤️" : "🖤");
    }

    private void updateStreakBadge() {
        if (streak >= 2) {
            tvStreakBadge.setText("🔥 x" + streak);
            tvStreakBadge.setVisibility(View.VISIBLE);
        } else {
            tvStreakBadge.setVisibility(View.GONE);
        }
    }

    private void setAnswerButtonsEnabled(boolean enabled) {
        btnAns1.setEnabled(enabled); btnAns2.setEnabled(enabled);
        btnAns3.setEnabled(enabled); btnAns4.setEnabled(enabled);
    }

    private void resetAnswerButtonStyles() {
        btnAns1.setBackgroundResource(R.drawable.bg_answer_btn);
        btnAns2.setBackgroundResource(R.drawable.bg_answer_btn);
        btnAns3.setBackgroundResource(R.drawable.bg_answer_btn);
        btnAns4.setBackgroundResource(R.drawable.bg_answer_btn);
        setAnswerButtonsEnabled(true);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
