package com.example.learnquest.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.learnquest.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SpellingActivity extends AppCompatActivity {

    // ── Data ──────────────────────────────────────────────────────────────────
    private static final String[][] EASY_WORDS = {
        {"CAT",  "🐱", "What is this?"},
        {"DOG",  "🐶", "What is this?"},
        {"SUN",  "☀️", "What is in the sky?"},
        {"BEE",  "🐝", "What is this bug?"},
        {"COW",  "🐄", "What lives on a farm?"},
        {"HEN",  "🐔", "What lays eggs?"},
        {"PIG",  "🐷", "What oinks?"},
        {"BAT",  "🦇", "What flies at night?"},
        {"ANT",  "🐜", "What is this bug?"},
        {"FOX",  "🦊", "What is this animal?"},
    };

    private static final String[][] MEDIUM_WORDS = {
        {"BIRD", "🐦", "What flies?"},
        {"FISH", "🐟", "What swims in water?"},
        {"FROG", "🐸", "What says ribbit?"},
        {"DUCK", "🦆", "What quacks?"},
        {"LION", "🦁", "King of the jungle?"},
        {"BEAR", "🐻", "What loves honey?"},
        {"WOLF", "🐺", "What howls at the moon?"},
        {"CRAB", "🦀", "What has claws?"},
    };

    private static final String[][] HARD_WORDS = {
        {"TIGER", "🐯", "Big striped cat?"},
        {"HORSE", "🐴", "What do you ride?"},
        {"ZEBRA", "🦓", "What has stripes?"},
        {"SHARK", "🦈", "Big fish in the sea?"},
        {"SNAKE", "🐍", "What slithers?"},
        {"SHEEP", "🐑", "What gives wool?"},
        {"PANDA", "🐼", "What eats bamboo?"},
    };

    private String[][] currentWords;

    private static final int[] TILE_COLORS = {
        R.drawable.bg_tile_pink,
        R.drawable.bg_tile_blue,
        R.drawable.bg_tile_purple,
        R.drawable.bg_tile_orange,
        R.drawable.bg_tile_teal,
        R.drawable.bg_tile_green,
    };

    // ── State ─────────────────────────────────────────────────────────────────
    private int currentWordIdx = 0;
    private int lives          = 3;
    private int stars          = 0;
    private char[] filled;
    private int[]  tileSlotMap;   // which slot each pool tile is placed in (-1 = none)
    private List<Character> shuffledLetters = new ArrayList<>();

    // ── Views ─────────────────────────────────────────────────────────────────
    private TextView tvStars, tvWordNum, tvProgressPct, tvLevelBadge;
    private TextView tvWordEmoji, tvWordLabel;
    private TextView heart1, heart2, heart3;
    private LinearLayout dropSlotsLayout, progressDotsLayout;
    private GridLayout letterPoolLayout;
    private View progressFill;
    private TextView btnHint, btnClear, btnCheck, btnBack;
    
    private LinearLayout spellingContent, resultCard;
    private TextView tvREmoji, tvRTitle, tvRSub, tvRSpelled, tvRStars, btnPlayAgain;

    private final Handler handler = new Handler();
    private SharedPreferences prefs;

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spelling);
        prefs = getSharedPreferences("learnquest_prefs", MODE_PRIVATE);
        bindViews();
        setClickListeners();
        String difficulty = getIntent().getStringExtra("difficulty");
        if (difficulty == null) difficulty = "easy";
        
        tvLevelBadge.setText("🌟 DIFFICULTY: " + difficulty.toUpperCase());
        
        List<String[]> wordList;
        if (difficulty.equals("hard")) {
            wordList = new ArrayList<>(Arrays.asList(HARD_WORDS));
        } else if (difficulty.equals("medium")) {
            wordList = new ArrayList<>(Arrays.asList(MEDIUM_WORDS));
        } else {
            wordList = new ArrayList<>(Arrays.asList(EASY_WORDS));
        }
        
        Collections.shuffle(wordList);
        int numWords = Math.min(5, wordList.size());
        currentWords = new String[numWords][3];
        for(int i = 0; i < numWords; i++) {
            currentWords[i] = wordList.get(i);
        }

        loadWord();
    }

    private void bindViews() {
        tvStars           = findViewById(R.id.tvStars);
        tvWordNum         = findViewById(R.id.tvWordNum);
        tvProgressPct     = findViewById(R.id.tvProgressPct);
        tvLevelBadge      = findViewById(R.id.tvLevelBadge);
        tvWordEmoji       = findViewById(R.id.tvWordEmoji);
        tvWordLabel       = findViewById(R.id.tvWordLabel);
        heart1            = findViewById(R.id.heart1);
        heart2            = findViewById(R.id.heart2);
        heart3            = findViewById(R.id.heart3);
        dropSlotsLayout   = findViewById(R.id.dropSlotsLayout);
        letterPoolLayout  = findViewById(R.id.letterPoolLayout);
        progressDotsLayout= findViewById(R.id.progressDotsLayout);
        progressFill      = findViewById(R.id.progressFill);
        btnHint           = findViewById(R.id.btnHint);
        btnClear          = findViewById(R.id.btnClear);
        btnCheck          = findViewById(R.id.btnCheck);
        btnBack           = findViewById(R.id.btnBack);

        spellingContent   = findViewById(R.id.spellingContent);
        resultCard        = findViewById(R.id.resultCard);
        tvREmoji          = findViewById(R.id.tvREmoji);
        tvRTitle          = findViewById(R.id.tvRTitle);
        tvRSub            = findViewById(R.id.tvRSub);
        tvRSpelled        = findViewById(R.id.tvRSpelled);
        tvRStars          = findViewById(R.id.tvRStars);
        btnPlayAgain      = findViewById(R.id.btnPlayAgain);
    }

    private void setClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnHint.setOnClickListener(v -> useHint());
        btnClear.setOnClickListener(v -> clearAll());
        btnCheck.setOnClickListener(v -> checkAnswer());
        btnPlayAgain.setOnClickListener(v -> restartAll());
    }

    // ── Word Loading ──────────────────────────────────────────────────────────
    private void loadWord() {
        if (currentWordIdx >= currentWords.length) {
            showCompletionDialog();
            return;
        }

        String word  = currentWords[currentWordIdx][0];
        String emoji = currentWords[currentWordIdx][1];
        String label = currentWords[currentWordIdx][2];

        filled     = new char[word.length()];
        tileSlotMap = new int[0]; // reset later after shuffle

        tvWordEmoji.setText(emoji);
        tvWordLabel.setText(label);
        tvWordNum.setText("WORD " + (currentWordIdx + 1) + " OF " + currentWords.length);

        int pct = (currentWordIdx * 100) / currentWords.length;
        tvProgressPct.setText(pct + "%");
        animateProgressBar(pct);

        buildProgressDots();
        buildDropSlots(word);
        buildLetterPool(word);
    }

    private void animateProgressBar(int pct) {
        FrameLayout track = (FrameLayout) progressFill.getParent();
        int fullWidth = track.getWidth();
        if (fullWidth == 0) {
            track.post(() -> animateProgressBar(pct));
            return;
        }
        int targetWidth = (fullWidth * pct) / 100;
        ObjectAnimator anim = ObjectAnimator.ofInt(progressFill,
                "right", progressFill.getLeft(), progressFill.getLeft() + targetWidth);
        anim.setDuration(500);
        anim.start();
    }

    // ── Progress Dots ──────────────────────────────────────────────────────────
    private void buildProgressDots() {
        progressDotsLayout.removeAllViews();
        int dp10 = dpToPx(10);
        int dp4  = dpToPx(4);
        for (int i = 0; i < currentWords.length; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp10, dp10);
            lp.setMargins(dp4, 0, dp4, 0);
            dot.setLayoutParams(lp);
            if (i < currentWordIdx)
                dot.setBackgroundResource(R.drawable.bg_tile_green);
            else if (i == currentWordIdx)
                dot.setBackgroundResource(R.drawable.bg_pill_pink);
            else
                dot.setBackgroundResource(R.drawable.bg_progress_track);
            progressDotsLayout.addView(dot);
        }
    }

    // ── Drop Slots ─────────────────────────────────────────────────────────────
    private void buildDropSlots(String word) {
        dropSlotsLayout.removeAllViews();
        filled = new char[word.length()];
        int dp52 = dpToPx(52);
        int dp58 = dpToPx(58);
        int dp8  = dpToPx(8);
        for (int i = 0; i < word.length(); i++) {
            final int idx = i;
            TextView slot = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp52, dp58);
            lp.setMargins(dp4(), dp4(), dp4(), dp4());
            slot.setLayoutParams(lp);
            slot.setGravity(android.view.Gravity.CENTER);
            slot.setTextSize(22);
            slot.setTextColor(getResources().getColor(R.color.text_dark));
            slot.setBackgroundResource(R.drawable.bg_slot_empty);
            slot.setTag("slot_" + i);
            slot.setOnClickListener(v -> removeFromSlot(idx));
            dropSlotsLayout.addView(slot);
        }
    }

    private TextView getSlot(int idx) {
        return (TextView) dropSlotsLayout.findViewWithTag("slot_" + idx);
    }

    // ── Letter Pool ────────────────────────────────────────────────────────────
    private void buildLetterPool(String word) {
        letterPoolLayout.removeAllViews();
        shuffledLetters.clear();

        // Add word letters
        for (char c : word.toCharArray()) shuffledLetters.add(c);

        // Add extra filler letters
        String extras = "AEIOURSTLMND";
        List<Character> pool = new ArrayList<>();
        for (char c : extras.toCharArray()) {
            if (word.indexOf(c) < 0) pool.add(c);
        }
        Collections.shuffle(pool);
        int needed = Math.max(0, 8 - word.length());
        for (int i = 0; i < Math.min(needed, pool.size()); i++) {
            shuffledLetters.add(pool.get(i));
        }
        Collections.shuffle(shuffledLetters);

        tileSlotMap = new int[shuffledLetters.size()];
        Arrays.fill(tileSlotMap, -1);

        int dp54 = dpToPx(54);
        int dp60 = dpToPx(60);
        for (int i = 0; i < shuffledLetters.size(); i++) {
            final int tileIdx = i;
            TextView tile = new TextView(this);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = dp54;
            lp.height = dp60;
            lp.setMargins(dp4(), dp4(), dp4(), dp4());
            tile.setLayoutParams(lp);
            tile.setGravity(android.view.Gravity.CENTER);
            tile.setText(String.valueOf(shuffledLetters.get(i)));
            tile.setTextSize(24);
            tile.setTextColor(getResources().getColor(R.color.white));
            tile.setTypeface(null, android.graphics.Typeface.BOLD);
            tile.setBackgroundResource(TILE_COLORS[i % TILE_COLORS.length]);
            tile.setTag("tile_" + i);
            tile.setClickable(true);
            tile.setFocusable(true);
            tile.setOnClickListener(v -> tapLetter(tileIdx));
            letterPoolLayout.addView(tile);
        }
    }

    // ── Tap Letter ─────────────────────────────────────────────────────────────
    private void tapLetter(int tileIdx) {
        // Find first empty slot
        int emptySlot = -1;
        for (int i = 0; i < filled.length; i++) {
            if (filled[i] == 0) { emptySlot = i; break; }
        }
        if (emptySlot < 0) return;

        char letter = shuffledLetters.get(tileIdx);
        filled[emptySlot] = letter;
        tileSlotMap[tileIdx] = emptySlot;

        TextView slot = getSlot(emptySlot);
        slot.setText(String.valueOf(letter));
        slot.setBackgroundResource(R.drawable.bg_slot_filled);
        animatePop(slot);

        // Hide tile
        View tile = letterPoolLayout.findViewWithTag("tile_" + tileIdx);
        if (tile != null) tile.setAlpha(0f);
    }

    // ── Remove From Slot ───────────────────────────────────────────────────────
    private void removeFromSlot(int slotIdx) {
        if (filled[slotIdx] == 0) return;
        filled[slotIdx] = 0;

        TextView slot = getSlot(slotIdx);
        slot.setText("");
        slot.setBackgroundResource(R.drawable.bg_slot_empty);

        // Restore tile
        for (int i = 0; i < tileSlotMap.length; i++) {
            if (tileSlotMap[i] == slotIdx) {
                tileSlotMap[i] = -1;
                View tile = letterPoolLayout.findViewWithTag("tile_" + i);
                if (tile != null) tile.setAlpha(1f);
                break;
            }
        }
    }

    // ── Clear All ──────────────────────────────────────────────────────────────
    private void clearAll() {
        Arrays.fill(filled, (char) 0);
        Arrays.fill(tileSlotMap, -1);
        for (int i = 0; i < currentWords[currentWordIdx][0].length(); i++) {
            TextView slot = getSlot(i);
            slot.setText("");
            slot.setBackgroundResource(R.drawable.bg_slot_empty);
        }
        for (int i = 0; i < shuffledLetters.size(); i++) {
            View tile = letterPoolLayout.findViewWithTag("tile_" + i);
            if (tile != null) tile.setAlpha(1f);
        }
    }

    // ── Hint ───────────────────────────────────────────────────────────────────
    private void useHint() {
        String word = currentWords[currentWordIdx][0];
        if (filled[0] != 0) return; // first slot already filled

        // Fill first slot with correct letter
        filled[0] = word.charAt(0);
        TextView slot = getSlot(0);
        slot.setText(String.valueOf(word.charAt(0)));
        slot.setBackgroundResource(R.drawable.bg_slot_filled);
        animatePop(slot);

        // Hide matching tile
        for (int i = 0; i < shuffledLetters.size(); i++) {
            if (shuffledLetters.get(i) == word.charAt(0) && tileSlotMap[i] < 0) {
                tileSlotMap[i] = 0;
                View tile = letterPoolLayout.findViewWithTag("tile_" + i);
                if (tile != null) tile.setAlpha(0f);
                break;
            }
        }
    }

    // ── Check Answer ───────────────────────────────────────────────────────────
    private void checkAnswer() {
        String word = currentWords[currentWordIdx][0];
        // Check all slots filled
        for (char c : filled) {
            if (c == 0) { highlightEmptySlots(); return; }
        }

        String answer = new String(filled);
        if (answer.equals(word)) {
            markSlotsCorrect();
            stars += (lives == 3) ? 3 : (lives == 2) ? 2 : 1;
            tvStars.setText(String.valueOf(stars));
            handler.postDelayed(this::showWordSuccess, 600);
        } else {
            markSlotsWrong();
            lives = Math.max(0, lives - 1);
            updateLives();
            if (lives == 0) {
                handler.postDelayed(this::showGameOverDialog, 600);
            }
        }
    }

    private void highlightEmptySlots() {
        for (int i = 0; i < filled.length; i++) {
            if (filled[i] == 0) {
                TextView slot = getSlot(i);
                slot.setBackgroundResource(R.drawable.bg_slot_filled);
                handler.postDelayed(() -> slot.setBackgroundResource(R.drawable.bg_slot_empty), 500);
            }
        }
    }

    private void markSlotsCorrect() {
        for (int i = 0; i < filled.length; i++) {
            TextView slot = getSlot(i);
            slot.setBackgroundResource(R.drawable.bg_slot_correct);
        }
    }

    private void markSlotsWrong() {
        for (int i = 0; i < filled.length; i++) {
            TextView slot = getSlot(i);
            slot.setBackgroundResource(R.drawable.bg_slot_wrong);
            animateShake(slot);
        }
        handler.postDelayed(() -> {
            for (int i = 0; i < filled.length; i++) {
                TextView s = getSlot(i);
                s.setBackgroundResource(filled[i] != 0
                        ? R.drawable.bg_slot_filled : R.drawable.bg_slot_empty);
            }
        }, 500);
    }

    // ── Success / Game Over Dialogs ────────────────────────────────────────────
    private void showGameOverDialog() {
        tvREmoji.setText("💔");
        tvRTitle.setText("Game Over!");
        tvRTitle.setTextColor(getResources().getColor(R.color.pink_primary));
        tvRSub.setText("You ran out of lives! Better luck next time!");
        showResult();
    }

    private void showWordSuccess() {
        String word  = currentWords[currentWordIdx][0];
        String emoji = currentWords[currentWordIdx][1];
        String starsEarned = lives == 3 ? "⭐⭐⭐" : lives == 2 ? "⭐⭐" : "⭐";

        new AlertDialog.Builder(this)
            .setTitle(emoji + "  Awesome!")
            .setMessage("You spelled:  " + word + "\n\nEarned: " + starsEarned)
            .setPositiveButton("Next Word →", (d, w) -> {
                currentWordIdx++;
                loadWord();
            })
            .setCancelable(false)
            .show();
    }

    private void showCompletionDialog() {
        tvREmoji.setText("🏆");
        tvRTitle.setText("Spelling Bee!");
        tvRTitle.setTextColor(getResources().getColor(R.color.pink_primary));
        tvRSub.setText("You spelled all the words!");
        showResult();
    }

    private void showResult() {
        int saved = prefs.getInt("total_stars", 0);
        prefs.edit().putInt("total_stars", saved + stars).apply();

        tvRSpelled.setText(String.valueOf(currentWordIdx));
        tvRStars.setText(String.valueOf(stars));

        spellingContent.setVisibility(View.GONE);
        resultCard.setVisibility(View.VISIBLE);
    }
    
    private void restartAll() {
        currentWordIdx = 0; lives = 3; stars = 0;
        tvStars.setText("0");
        updateLives();
        spellingContent.setVisibility(View.VISIBLE);
        resultCard.setVisibility(View.GONE);
        loadWord();
    }

    // ── Lives ──────────────────────────────────────────────────────────────────
    private void updateLives() {
        heart1.setText(lives >= 1 ? "❤️" : "🖤");
        heart2.setText(lives >= 2 ? "❤️" : "🖤");
        heart3.setText(lives >= 3 ? "❤️" : "🖤");
    }

    // ── Animations ─────────────────────────────────────────────────────────────
    private void animatePop(View v) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(v, "scaleX", 0.8f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(v, "scaleY", 0.8f, 1.1f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(250);
        set.setInterpolator(new OvershootInterpolator());
        set.start();
    }

    private void animateShake(View v) {
        ObjectAnimator shake = ObjectAnimator.ofFloat(v, "translationX",
                0f, -12f, 12f, -8f, 8f, 0f);
        shake.setDuration(300);
        shake.start();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private int dp4() { return dpToPx(4); }
}
