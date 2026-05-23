package com.example.learnquest.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.learnquest.R;
import com.example.learnquest.utils.SoundManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * QuizActivity — Short Quizzes mini-game.
 *
 * Shows 7 multiple-choice questions (mix of Math, Spelling, General Knowledge).
 * Each question has 4 options. Immediate feedback is given after each pick.
 * A progress dot row tracks correct/wrong results.
 * Final result card shows score, stars earned, and a Play Again button.
 *
 * Matching design: same colors/drawables as MathBattleActivity and SpellingActivity.
 */
public class QuizActivity extends AppCompatActivity {

    // ── Quiz data ─────────────────────────────────────────────────────────────
    private static final Object[][] QUESTIONS = {
        // { question, correct_answer, wrong1, wrong2, wrong3, category_emoji }
        { "What is 5 + 7?",           "12",      "11",     "13",     "10",     "🔢" },
        { "Which animal says 'moo'?",  "Cow 🐄",  "Dog 🐶", "Cat 🐱", "Hen 🐔", "🐾" },
        { "What color is the sky?",    "Blue",    "Green",  "Red",    "Yellow", "🌤️" },
        { "Spell the missing letter: _OG (rhymes with frog)?",
                                       "D",       "B",      "F",      "L",      "🔤" },
        { "What is 9 × 3?",            "27",      "24",     "30",     "21",     "🔢" },
        { "Which shape has 3 sides?",  "Triangle","Circle", "Square", "Oval",   "🔷" },
        { "What comes after Wednesday?","Thursday","Tuesday","Friday", "Monday", "📅" },
        { "What is 20 – 8?",           "12",      "14",     "10",     "11",     "🔢" },
        { "Which fruit is yellow?",    "Banana",  "Apple",  "Grape",  "Orange", "🍎" },
        { "How many days in a week?",  "7",       "5",      "8",      "6",      "📅" },
    };

    private static final int TOTAL_QUESTIONS = 7;
    private static final int STARS_PER_CORRECT = 3;
    private static final int TIME_PER_Q = 15; // seconds

    // ── State ─────────────────────────────────────────────────────────────────
    private int currentQ     = 0;
    private int correctCount = 0;
    private int starsEarned  = 0;
    private boolean answered = false;
    private long qStartTime;
    private List<Object[]> questionOrder;
    private CountDownTimer countDownTimer;
    private final Handler handler = new Handler();

    // ── Views ─────────────────────────────────────────────────────────────────
    private TextView tvTopScore, tvQNum, tvTimerNum, tvCategory;
    private TextView tvQuestion;
    private TextView btnOpt1, btnOpt2, btnOpt3, btnOpt4;
    private View timerFill;
    private LinearLayout progressDotsLayout;
    private TextView tvFeedback;
    private LinearLayout resultCard;
    private TextView tvResultEmoji, tvResultTitle, tvResultSub;
    private TextView tvStatCorrect, tvStatStars, tvStatTime;
    private TextView btnPlayAgain, btnBack;
    private boolean[] dotResults;
    private long totalTimeMs = 0;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        prefs = getSharedPreferences("learnquest_prefs", MODE_PRIVATE);

        // Shuffle and take TOTAL_QUESTIONS from the pool
        List<Object[]> pool = new ArrayList<>(Arrays.asList(QUESTIONS));
        Collections.shuffle(pool);
        questionOrder = pool.subList(0, Math.min(TOTAL_QUESTIONS, pool.size()));
        dotResults    = new boolean[questionOrder.size()];

        bindViews();
        buildProgressDots();
        setClickListeners();
        loadQuestion();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }

    // ── Bind ──────────────────────────────────────────────────────────────────
    private void bindViews() {
        tvTopScore         = findViewById(R.id.tvQuizScore);
        tvQNum             = findViewById(R.id.tvQNum);
        tvTimerNum         = findViewById(R.id.tvQuizTimer);
        tvCategory         = findViewById(R.id.tvQuizCategory);
        tvQuestion         = findViewById(R.id.tvQuizQuestion);
        btnOpt1            = findViewById(R.id.btnOpt1);
        btnOpt2            = findViewById(R.id.btnOpt2);
        btnOpt3            = findViewById(R.id.btnOpt3);
        btnOpt4            = findViewById(R.id.btnOpt4);
        timerFill          = findViewById(R.id.quizTimerFill);
        progressDotsLayout = findViewById(R.id.quizProgressDots);
        tvFeedback         = findViewById(R.id.tvQuizFeedback);
        resultCard         = findViewById(R.id.quizResultCard);
        tvResultEmoji      = findViewById(R.id.tvQuizResultEmoji);
        tvResultTitle      = findViewById(R.id.tvQuizResultTitle);
        tvResultSub        = findViewById(R.id.tvQuizResultSub);
        tvStatCorrect      = findViewById(R.id.tvQuizStatCorrect);
        tvStatStars        = findViewById(R.id.tvQuizStatStars);
        tvStatTime         = findViewById(R.id.tvQuizStatTime);
        btnPlayAgain       = findViewById(R.id.btnQuizPlayAgain);
        btnBack            = findViewById(R.id.btnQuizBack);

        tvTopScore.setText("⭐ " + prefs.getInt("total_stars", 0));
    }

    // ── Progress Dots ─────────────────────────────────────────────────────────
    private void buildProgressDots() {
        progressDotsLayout.removeAllViews();
        int dp = dpToPx(10);
        for (int i = 0; i < questionOrder.size(); i++) {
            TextView dot = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp, dp);
            lp.setMargins(dpToPx(4), 0, dpToPx(4), 0);
            dot.setLayoutParams(lp);
            dot.setBackgroundResource(R.drawable.bg_progress_track);
            dot.setTag("dot_" + i);
            progressDotsLayout.addView(dot);
        }
    }

    private void setDot(int idx, boolean correct) {
        View dot = progressDotsLayout.findViewWithTag("dot_" + idx);
        if (dot != null) {
            dot.setBackgroundResource(correct
                    ? R.drawable.bg_slot_correct
                    : R.drawable.bg_slot_wrong);
        }
    }

    // ── Load Question ─────────────────────────────────────────────────────────
    private void loadQuestion() {
        if (currentQ >= questionOrder.size()) {
            showResult();
            return;
        }

        answered   = false;
        qStartTime = System.currentTimeMillis();
        Object[] q = questionOrder.get(currentQ);

        String question = (String) q[0];
        String correct  = (String) q[1];
        String category = (String) q[5];

        // Shuffle options
        List<String> opts = new ArrayList<>(Arrays.asList(
                (String) q[1], (String) q[2], (String) q[3], (String) q[4]));
        Collections.shuffle(opts);

        tvQNum.setText("Q " + (currentQ + 1) + " / " + questionOrder.size());
        tvCategory.setText(category + " Question");
        tvQuestion.setText(question);
        tvFeedback.setVisibility(View.INVISIBLE);

        TextView[] btns = { btnOpt1, btnOpt2, btnOpt3, btnOpt4 };
        for (int i = 0; i < btns.length; i++) {
            btns[i].setText(opts.get(i));
            btns[i].setBackgroundResource(R.drawable.bg_answer_btn);
            btns[i].setTextColor(Color.parseColor("#222222"));
            btns[i].setEnabled(true);
            btns[i].setTag(opts.get(i).equals(correct) ? "correct" : "wrong");
        }

        startTimer();

        // Animate question in
        tvQuestion.setAlpha(0f);
        tvQuestion.setTranslationX(40f);
        tvQuestion.animate().alpha(1f).translationX(0f).setDuration(250).start();
    }

    // ── Timer ─────────────────────────────────────────────────────────────────
    private void startTimer() {
        if (countDownTimer != null) countDownTimer.cancel();

        // Animate timer bar
        if (timerFill != null) {
            timerFill.post(() -> {
                int fullWidth = ((View) timerFill.getParent()).getWidth();
                ObjectAnimator anim = ObjectAnimator.ofFloat(timerFill, "scaleX", 1f, 0f);
                anim.setDuration(TIME_PER_Q * 1000L);
                anim.start();
            });
        }

        countDownTimer = new CountDownTimer(TIME_PER_Q * 1000L, 1000) {
            @Override public void onTick(long ms) {
                int sec = (int) (ms / 1000);
                tvTimerNum.setText(String.valueOf(sec));
                if (sec <= 5) tvTimerNum.setTextColor(Color.parseColor("#FF4757"));
                else          tvTimerNum.setTextColor(Color.parseColor("#222222"));
            }
            @Override public void onFinish() {
                if (!answered) timeOut();
            }
        }.start();
    }

    private void timeOut() {
        answered = true;
        totalTimeMs += TIME_PER_Q * 1000L;
        showFeedback(false, "⏰ Time's up!");
        disableOptions();
        setDot(currentQ, false);
        handler.postDelayed(this::nextQuestion, 1800);
    }

    // ── Answer Handling ───────────────────────────────────────────────────────
    private void handleAnswer(TextView btn) {
        if (answered) return;
        answered = true;
        if (countDownTimer != null) countDownTimer.cancel();
        totalTimeMs += System.currentTimeMillis() - qStartTime;

        boolean isCorrect = "correct".equals(btn.getTag());

        // Visual feedback on button
        btn.setBackgroundResource(isCorrect
                ? R.drawable.bg_answer_correct
                : R.drawable.bg_answer_wrong);
        btn.setTextColor(Color.WHITE);

        // Highlight correct if wrong was chosen
        if (!isCorrect) {
            TextView[] all = { btnOpt1, btnOpt2, btnOpt3, btnOpt4 };
            for (TextView b : all) {
                if ("correct".equals(b.getTag())) {
                    b.setBackgroundResource(R.drawable.bg_answer_correct);
                    b.setTextColor(Color.WHITE);
                }
            }
        }

        disableOptions();
        dotResults[currentQ] = isCorrect;
        setDot(currentQ, isCorrect);

        if (isCorrect) {
            correctCount++;
            starsEarned += STARS_PER_CORRECT;
            SoundManager.get(this).playCorrect();
            showFeedback(true, "✅ Correct! +" + STARS_PER_CORRECT + "⭐");
            shakeOrBounce(btn, true);
        } else {
            SoundManager.get(this).playWrong();
            showFeedback(false, "❌ Not quite!");
            shakeOrBounce(btn, false);
        }

        // Update header score preview
        tvTopScore.setText("⭐ " + (prefs.getInt("total_stars", 0) + starsEarned));

        handler.postDelayed(this::nextQuestion, 1600);
    }

    private void showFeedback(boolean correct, String msg) {
        tvFeedback.setText(msg);
        tvFeedback.setTextColor(correct
                ? Color.parseColor("#2ECC40")
                : Color.parseColor("#FF4757"));
        tvFeedback.setVisibility(View.VISIBLE);
        tvFeedback.setAlpha(0f);
        tvFeedback.animate().alpha(1f).setDuration(200).start();
    }

    private void disableOptions() {
        btnOpt1.setEnabled(false);
        btnOpt2.setEnabled(false);
        btnOpt3.setEnabled(false);
        btnOpt4.setEnabled(false);
    }

    private void shakeOrBounce(View v, boolean bounce) {
        if (bounce) {
            ObjectAnimator sx = ObjectAnimator.ofFloat(v, "scaleX", 1f, 1.12f, 1f);
            ObjectAnimator sy = ObjectAnimator.ofFloat(v, "scaleY", 1f, 1.12f, 1f);
            AnimatorSet a = new AnimatorSet();
            a.playTogether(sx, sy);
            a.setDuration(250);
            a.start();
        } else {
            ObjectAnimator shake = ObjectAnimator.ofFloat(v, "translationX",
                    0f, -10f, 10f, -8f, 8f, -4f, 4f, 0f);
            shake.setDuration(300);
            shake.start();
        }
    }

    private void nextQuestion() {
        currentQ++;
        loadQuestion();
    }

    // ── Result Screen ─────────────────────────────────────────────────────────
    private void showResult() {
        if (countDownTimer != null) countDownTimer.cancel();

        // Save to prefs
        int prevStars   = prefs.getInt("total_stars", 0);
        int prevGames   = prefs.getInt("games_played", 0);
        int prevCorrect = prefs.getInt("quiz_correct_total", 0);
        int prevTotal   = prefs.getInt("quiz_total_questions", 0);

        int newStars    = prevStars + starsEarned;
        int totalCorrect= prevCorrect + correctCount;
        int totalQuestions = prevTotal + questionOrder.size();
        int correctRate = totalQuestions > 0
                ? (int) Math.round(100.0 * totalCorrect / totalQuestions) : 0;

        prefs.edit()
            .putInt("total_stars",          newStars)
            .putInt("games_played",          prevGames + 1)
            .putInt("quiz_correct_total",    totalCorrect)
            .putInt("quiz_total_questions",  totalQuestions)
            .putInt("correct_rate",          correctRate)
            .apply();

        // Choose result message
        float pct = (float) correctCount / questionOrder.size();
        String emoji, title, sub;
        if (pct == 1f) {
            emoji = "🏆"; title = "Perfect Score!";
            sub   = "You answered every question correctly!";
            SoundManager.get(this).playWin();
        } else if (pct >= 0.7f) {
            emoji = "🌟"; title = "Great Job!";
            sub   = "Almost perfect — keep practicing!";
            SoundManager.get(this).playWin();
        } else if (pct >= 0.5f) {
            emoji = "👍"; title = "Good Try!";
            sub   = "You're getting better — try again!";
            SoundManager.get(this).playStreak();
        } else {
            emoji = "💪"; title = "Keep Going!";
            sub   = "Practice makes perfect. Don't give up!";
            SoundManager.get(this).playLose();
        }

        long avgSecPerQ = questionOrder.size() > 0
                ? (totalTimeMs / 1000) / questionOrder.size() : 0;

        tvResultEmoji.setText(emoji);
        tvResultTitle.setText(title);
        tvResultSub.setText(sub);
        tvStatCorrect.setText(correctCount + "/" + questionOrder.size() + " Correct");
        tvStatStars.setText("+" + starsEarned + " ⭐ Stars");
        tvStatTime.setText(avgSecPerQ + "s avg per Q");

        resultCard.setVisibility(View.VISIBLE);
        resultCard.setAlpha(0f);
        resultCard.setScaleX(0.85f);
        resultCard.setScaleY(0.85f);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(resultCard, "alpha", 0f, 1f);
        ObjectAnimator sx     = ObjectAnimator.ofFloat(resultCard, "scaleX", 0.85f, 1f);
        ObjectAnimator sy     = ObjectAnimator.ofFloat(resultCard, "scaleY", 0.85f, 1f);
        AnimatorSet a = new AnimatorSet();
        a.playTogether(fadeIn, sx, sy);
        a.setDuration(350);
        a.start();
    }

    // ── Click Listeners ───────────────────────────────────────────────────────
    private void setClickListeners() {
        btnOpt1.setOnClickListener(v -> handleAnswer(btnOpt1));
        btnOpt2.setOnClickListener(v -> handleAnswer(btnOpt2));
        btnOpt3.setOnClickListener(v -> handleAnswer(btnOpt3));
        btnOpt4.setOnClickListener(v -> handleAnswer(btnOpt4));

        btnPlayAgain.setOnClickListener(v -> {
            SoundManager.get(this).playClick();
            // Reset and restart
            currentQ     = 0;
            correctCount = 0;
            starsEarned  = 0;
            totalTimeMs  = 0;
            List<Object[]> pool = new ArrayList<>(Arrays.asList(QUESTIONS));
            Collections.shuffle(pool);
            questionOrder = pool.subList(0, Math.min(TOTAL_QUESTIONS, pool.size()));
            dotResults    = new boolean[questionOrder.size()];
            resultCard.setVisibility(View.GONE);
            tvTopScore.setText("⭐ " + prefs.getInt("total_stars", 0));
            buildProgressDots();
            loadQuestion();
        });

        btnBack.setOnClickListener(v -> {
            SoundManager.get(this).playClick();
            finish();
        });
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
