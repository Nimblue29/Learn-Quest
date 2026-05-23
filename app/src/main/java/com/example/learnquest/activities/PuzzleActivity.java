package com.example.learnquest.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.learnquest.R;

public class PuzzleActivity extends AppCompatActivity {

    // ── Puzzle Data ───────────────────────────────────────────────────────────
    // Each puzzle: { title, instruction, correct_answer, choices... }
    private static final String[][][] PATTERNS = {
        { {"What comes next?","Find the pattern!",  "🍊"}, {"🍎","🍊","🍎","🍊","🍎","❓"}, {"🍊","🍇","🍋","🍓"} },
        { {"What comes next?","Look at the order!", "🌙"}, {"⭐","⭐","🌙","⭐","⭐","❓"}, {"⭐","🌙","☀️","🌟"} },
        { {"What comes next?","Which is next?",     "🐶"}, {"🐱","🐶","🐱","🐶","🐱","❓"}, {"🐱","🐶","🐸","🐰"} },
        { {"What comes next?","Colors in order!",   "🟡"}, {"🔴","🔵","🟡","🔴","🔵","❓"}, {"🔴","🔵","🟡","🟢"} }
    };

    private static final String[][][] MATCHES = {
        { {"Find the match!","Which one matches?", "🐱"}, {"🐱","🐶","🐸","?"}, {"🐶","🐸","🐱","🐰"} },
        { {"Find the match!","Find the pair!",     "🌈"}, {"🌈","⛅","?","⛅"}, {"⛅","🌈","⛄","🌊"} },
        { {"Find the match!","Complete it!",       "🍕"}, {"?","🍔","🍕","🍔"}, {"🍔","🌮","🍕","🍦"} },
        { {"Find the match!","Which fits here?",   "✈️"}, {"🚗","?","🚗","✈️"}, {"🚗","🚢","✈️","🚂"} }
    };

    private static final String[][][] ODD_ONES = {
        { {"Odd one out!","Which does NOT belong?", "🚗"}, {"🍎","🍊","🍇","🚗"} },
        { {"Odd one out!","Find the different one!", "🌸"}, {"🐱","🐶","🦁","🌸"} },
        { {"Odd one out!","Not a color?",            "🐸"}, {"🔴","🔵","🟡","🐸"} },
        { {"Odd one out!","Not a vehicle?",          "🍕"}, {"🚗","🚌","✈️","🍕"} }
    };

    // ── State ─────────────────────────────────────────────────────────────────
    private int currentMode    = 0; // 0=patterns, 1=match, 2=oddone
    private int currentPuzzle  = 0;
    private int stars          = 0;
    private int lives          = 3;
    private int hintsUsed      = 0;
    private int streak         = 0;
    private int bestStreak     = 0;
    private int solved         = 0;
    private boolean puzzleDone = false;

    // ── Views ─────────────────────────────────────────────────────────────────
    private TextView tvStars, heart1, heart2, heart3;
    private TextView tvPuzzleTitle, tvInstruction;
    private TextView tabPatterns, tabMatch, tabOdd;
    private LinearLayout puzzleContentLayout, choicesRow, progressDotsLayout;
    private LinearLayout choicesSection, puzzleCard, resultCard;
    private TextView btnHint, btnReset, btnBack, btnPlayAgain;
    private TextView tvREmoji, tvRTitle, tvRSub;
    private TextView tvRSolved, tvRStars, tvRHints, tvRStreak;

    private final Handler handler = new Handler();
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);
        prefs = getSharedPreferences("learnquest_prefs", MODE_PRIVATE);
        bindViews();
        setClickListeners();
        loadPuzzle();
    }

    private void bindViews() {
        tvStars             = findViewById(R.id.tvStars);
        heart1              = findViewById(R.id.heart1);
        heart2              = findViewById(R.id.heart2);
        heart3              = findViewById(R.id.heart3);
        tvPuzzleTitle       = findViewById(R.id.tvPuzzleTitle);
        tvInstruction       = findViewById(R.id.tvInstruction);
        tabPatterns         = findViewById(R.id.tabPatterns);
        tabMatch            = findViewById(R.id.tabMatch);
        tabOdd              = findViewById(R.id.tabOdd);
        puzzleContentLayout = findViewById(R.id.puzzleContentLayout);
        choicesRow          = findViewById(R.id.choicesRow);
        progressDotsLayout  = findViewById(R.id.progressDotsLayout);
        choicesSection      = findViewById(R.id.choicesSection);
        puzzleCard          = findViewById(R.id.puzzleCard);
        resultCard          = findViewById(R.id.resultCard);
        btnHint             = findViewById(R.id.btnHint);
        btnReset            = findViewById(R.id.btnReset);
        btnBack             = findViewById(R.id.btnBack);
        btnPlayAgain        = findViewById(R.id.btnPlayAgain);
        tvREmoji            = findViewById(R.id.tvREmoji);
        tvRTitle            = findViewById(R.id.tvRTitle);
        tvRSub              = findViewById(R.id.tvRSub);
        tvRSolved           = findViewById(R.id.tvRSolved);
        tvRStars            = findViewById(R.id.tvRStars);
        tvRHints            = findViewById(R.id.tvRHints);
        tvRStreak           = findViewById(R.id.tvRStreak);
    }

    private void setClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnHint.setOnClickListener(v -> useHint());
        btnReset.setOnClickListener(v -> loadPuzzle());
        btnPlayAgain.setOnClickListener(v -> restartAll());
        tabPatterns.setOnClickListener(v -> switchMode(0));
        tabMatch.setOnClickListener(v -> switchMode(1));
        tabOdd.setOnClickListener(v -> switchMode(2));
    }

    // ── Mode Switch ────────────────────────────────────────────────────────────
    private void switchMode(int mode) {
        currentMode   = mode;
        currentPuzzle = 0;
        updateTabStyles();
        loadPuzzle();
    }

    private void updateTabStyles() {
        tabPatterns.setBackgroundResource(currentMode == 0 ? R.drawable.bg_tab_active : R.drawable.bg_tab_inactive);
        tabMatch.setBackgroundResource(currentMode == 1 ? R.drawable.bg_tab_active : R.drawable.bg_tab_inactive);
        tabOdd.setBackgroundResource(currentMode == 2 ? R.drawable.bg_tab_active : R.drawable.bg_tab_inactive);
        int activeColor  = getResources().getColor(R.color.white);
        int inactiveColor= getResources().getColor(R.color.green_primary);
        tabPatterns.setTextColor(currentMode == 0 ? activeColor : inactiveColor);
        tabMatch.setTextColor(currentMode == 1 ? activeColor : inactiveColor);
        tabOdd.setTextColor(currentMode == 2 ? activeColor : inactiveColor);
    }

    // ── Load Puzzle ────────────────────────────────────────────────────────────
    private void loadPuzzle() {
        puzzleDone = false;
        String[][] puzzles = currentMode == 0 ? PATTERNS[currentPuzzle]
                : currentMode == 1 ? MATCHES[currentPuzzle]
                : ODD_ONES[currentPuzzle];

        tvPuzzleTitle.setText(puzzles[0][0]);
        tvInstruction.setText(puzzles[0][1]);

        buildProgressDots();
        puzzleContentLayout.removeAllViews();
        choicesRow.removeAllViews();

        if (currentMode == 0) buildPatternPuzzle(puzzles);
        else if (currentMode == 1) buildMatchPuzzle(puzzles);
        else buildOddOnePuzzle(puzzles);
    }

    // ── Pattern Puzzle ─────────────────────────────────────────────────────────
    private void buildPatternPuzzle(String[][] data) {
        // data[1] = sequence items  data[2] = choices
        String correct = data[0][2];
        String[] seq    = data[1]; // emojis including ❓
        String[] choices= data[2];

        choicesSection.setVisibility(View.VISIBLE);

        for (String em : seq) {
            TextView cell = makeEmojiCell(em.equals("❓") ? "?" : em, em.equals("❓"));
            cell.setTag(em.equals("❓") ? "blank" : null);
            puzzleContentLayout.addView(cell);
        }

        buildChoices(choices, correct);
    }

    // ── Match Puzzle ───────────────────────────────────────────────────────────
    private void buildMatchPuzzle(String[][] data) {
        String correct  = data[0][2];
        String[] grid   = data[1]; // 4 items, one is "?"
        String[] choices= data[2];
        choicesSection.setVisibility(View.VISIBLE);

        // 2×2 grid
        LinearLayout row1 = makeHRow();
        LinearLayout row2 = makeHRow();
        puzzleContentLayout.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < grid.length; i++) {
            boolean isBlank = grid[i].equals("?");
            TextView cell = makeEmojiCell(isBlank ? "?" : grid[i], isBlank);
            if (isBlank) cell.setTag("blank");
            (i < 2 ? row1 : row2).addView(cell);
        }
        puzzleContentLayout.addView(row1);
        puzzleContentLayout.addView(row2);
        buildChoices(choices, correct);
    }

    // ── Odd One Out ────────────────────────────────────────────────────────────
    private void buildOddOnePuzzle(String[][] data) {
        String correct = data[0][2];
        String[] items = data[1];
        choicesSection.setVisibility(View.GONE);

        for (String em : items) {
            TextView cell = makeEmojiCell(em, false);
            cell.setOnClickListener(v -> tapOddOne(em, cell, correct));
            puzzleContentLayout.addView(cell);
        }
    }

    private void tapOddOne(String chosen, TextView cell, String correct) {
        if (puzzleDone) return;
        puzzleDone = true;
        if (chosen.equals(correct)) {
            cell.setBackgroundResource(R.drawable.bg_slot_correct);
            onCorrect();
        } else {
            cell.setBackgroundResource(R.drawable.bg_slot_wrong);
            animateShake(cell);
            onWrong();
            handler.postDelayed(() -> { puzzleDone = false; }, 500);
        }
    }

    // ── Choices ────────────────────────────────────────────────────────────────
    private void buildChoices(String[] choices, String correct) {
        for (String c : choices) {
            TextView btn = new TextView(this);
            int dp62 = dpToPx(62);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp62, dp62);
            lp.setMargins(dpToPx(6), 0, dpToPx(6), 0);
            btn.setLayoutParams(lp);
            btn.setGravity(Gravity.CENTER);
            btn.setText(c);
            btn.setTextSize(26);
            btn.setBackgroundResource(R.drawable.bg_answer_btn);
            btn.setClickable(true);
            btn.setFocusable(true);
            btn.setOnClickListener(v -> selectChoice(c, correct, btn));
            choicesRow.addView(btn);
        }
    }

    private void selectChoice(String chosen, String correct, TextView btn) {
        if (puzzleDone) return;
        puzzleDone = true;
        disableChoices();

        if (chosen.equals(correct)) {
            btn.setBackgroundResource(R.drawable.bg_answer_correct);
            fillBlankSlot(correct);
            onCorrect();
        } else {
            btn.setBackgroundResource(R.drawable.bg_answer_wrong);
            animateShake(btn);
            onWrong();
            handler.postDelayed(() -> {
                puzzleDone = false;
                enableChoices();
                btn.setBackgroundResource(R.drawable.bg_answer_btn);
            }, 700);
        }
    }

    private void fillBlankSlot(String text) {
        View blank = puzzleContentLayout.findViewWithTag("blank");
        if (blank instanceof TextView) {
            ((TextView) blank).setText(text);
            blank.setBackgroundResource(R.drawable.bg_slot_correct);
        }
    }

    private void disableChoices() {
        for (int i = 0; i < choicesRow.getChildCount(); i++)
            choicesRow.getChildAt(i).setEnabled(false);
    }

    private void enableChoices() {
        for (int i = 0; i < choicesRow.getChildCount(); i++)
            choicesRow.getChildAt(i).setEnabled(true);
    }

    // ── Correct / Wrong ────────────────────────────────────────────────────────
    private void onCorrect() {
        streak++; bestStreak = Math.max(bestStreak, streak);
        stars += 3; solved++;
        tvStars.setText(String.valueOf(stars));
        updateDot(currentPuzzle, "correct");
        handler.postDelayed(this::nextPuzzle, 1100);
    }

    private void onWrong() {
        streak = 0;
        lives = Math.max(0, lives - 1);
        updateLives();
        updateDot(currentPuzzle, "wrong");
        if (lives == 0) {
            handler.postDelayed(this::showGameOverDialog, 500);
        }
    }

    private void nextPuzzle() {
        String[][][] data = currentMode == 0 ? PATTERNS
                : currentMode == 1 ? MATCHES : ODD_ONES;
        currentPuzzle++;
        if (currentPuzzle >= data.length) { showResult(); return; }
        loadPuzzle();
    }

    // ── Hint ───────────────────────────────────────────────────────────────────
    private void useHint() {
        if (puzzleDone) return;
        hintsUsed++;
        String correct = (currentMode == 0 ? PATTERNS : currentMode == 1 ? MATCHES : ODD_ONES)
                [currentPuzzle][0][2];
        tvInstruction.setText("💡 Hint: The answer is " + correct + "!");
        handler.postDelayed(() -> {
            String[][] p = (currentMode == 0 ? PATTERNS : currentMode == 1 ? MATCHES : ODD_ONES)[currentPuzzle];
            tvInstruction.setText(p[0][1]);
        }, 2000);
    }

    // ── Result / Game Over ─────────────────────────────────────────────────────
    private void showGameOverDialog() {
        tvREmoji.setText("💔");
        tvRTitle.setText("Game Over!");
        tvRTitle.setTextColor(getResources().getColor(R.color.pink_primary));
        tvRSub.setText("You ran out of lives! Better luck next time!");
        showResult();
    }

    private void showResult() {
        if (lives > 0) {
            tvREmoji.setText("🏆");
            tvRTitle.setText("Puzzle Master!");
            tvRTitle.setTextColor(getResources().getColor(R.color.green_primary));
            tvRSub.setText("You solved all the puzzles!");
        }

        int saved = prefs.getInt("total_stars", 0);
        prefs.edit().putInt("total_stars", saved + stars).apply();

        tvRSolved.setText(String.valueOf(solved));
        tvRStars.setText(String.valueOf(stars));
        tvRHints.setText(String.valueOf(hintsUsed));
        tvRStreak.setText(String.valueOf(bestStreak));
        puzzleCard.setVisibility(View.GONE);
        choicesSection.setVisibility(View.GONE);
        progressDotsLayout.setVisibility(View.GONE);
        resultCard.setVisibility(View.VISIBLE);
    }

    private void restartAll() {
        currentMode = 0; currentPuzzle = 0; stars = 0; lives = 3;
        hintsUsed = 0; streak = 0; bestStreak = 0; solved = 0;
        tvStars.setText("0"); updateLives(); updateTabStyles();
        puzzleCard.setVisibility(View.VISIBLE);
        choicesSection.setVisibility(View.VISIBLE);
        progressDotsLayout.setVisibility(View.VISIBLE);
        resultCard.setVisibility(View.GONE);
        loadPuzzle();
    }

    // ── Progress Dots ──────────────────────────────────────────────────────────
    private void buildProgressDots() {
        progressDotsLayout.removeAllViews();
        int total = currentMode == 0 ? PATTERNS.length
                : currentMode == 1 ? MATCHES.length : ODD_ONES.length;
        int dp10 = dpToPx(10), dp4 = dpToPx(4);
        for (int i = 0; i < total; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp10, dp10);
            lp.setMargins(dp4, 0, dp4, 0);
            dot.setLayoutParams(lp);
            dot.setTag("dot_" + i);
            dot.setBackgroundResource(i < currentPuzzle ? R.drawable.bg_tile_green
                    : i == currentPuzzle ? R.drawable.bg_pill_purple
                    : R.drawable.bg_progress_track);
            progressDotsLayout.addView(dot);
        }
    }

    private void updateDot(int idx, String state) {
        View dot = progressDotsLayout.findViewWithTag("dot_" + idx);
        if (dot == null) return;
        dot.setBackgroundResource(state.equals("correct")
                ? R.drawable.bg_tile_green : R.drawable.bg_tile_pink);
    }

    // ── Lives ──────────────────────────────────────────────────────────────────
    private void updateLives() {
        heart1.setText(lives >= 1 ? "❤️" : "🖤");
        heart2.setText(lives >= 2 ? "❤️" : "🖤");
        heart3.setText(lives >= 3 ? "❤️" : "🖤");
    }

    // ── Cell Factory ──────────────────────────────────────────────────────────
    private TextView makeEmojiCell(String text, boolean isBlank) {
        TextView cell = new TextView(this);
        int dp60 = dpToPx(60);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp60, dp60);
        lp.setMargins(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5));
        cell.setLayoutParams(lp);
        cell.setGravity(Gravity.CENTER);
        cell.setText(text);
        cell.setTextSize(28);
        cell.setBackgroundResource(isBlank ? R.drawable.bg_slot_empty : R.drawable.bg_answer_btn);
        return cell;
    }

    private LinearLayout makeHRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dpToPx(4), 0, dpToPx(4));
        row.setLayoutParams(lp);
        return row;
    }

    // ── Animation ─────────────────────────────────────────────────────────────
    private void animateShake(View v) {
        v.animate().translationX(-12f).setDuration(60)
            .withEndAction(() -> v.animate().translationX(12f).setDuration(60)
                .withEndAction(() -> v.animate().translationX(-8f).setDuration(50)
                    .withEndAction(() -> v.animate().translationX(0f).setDuration(50).start())
                    .start())
                .start())
            .start();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
