package com.sportrivia.sdk.internal.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.sportrivia.sdk.R;
import com.sportrivia.sdk.internal.logic.GameEngine;
import com.sportrivia.sdk.public_api.SporTriviaSDK;

/**
 * Game over screen — shows final score and navigation options.
 */
public class GameOverActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sportrivia_sdk_game_over);

        GameEngine engine = SporTriviaSession.getEngine();
        if (engine == null) {
            finish();
            return;
        }

        TextView textStreak = findViewById(R.id.sportrivia_sdk_text_streak);
        TextView textCorrect = findViewById(R.id.sportrivia_sdk_text_correct);
        TextView textIncorrect = findViewById(R.id.sportrivia_sdk_text_incorrect);
        TextView textMaxStreak = findViewById(R.id.sportrivia_sdk_text_max_streak);
        Button btnShare = findViewById(R.id.sportrivia_sdk_btn_share);
        Button btnAnswers = findViewById(R.id.sportrivia_sdk_btn_answers);
        Button btnDone = findViewById(R.id.sportrivia_sdk_btn_done);

        textStreak.setText("Streak: " + engine.getCurrentStreak());
        textCorrect.setText("Correct: " + engine.getCorrect());
        textIncorrect.setText("Incorrect: " + engine.getIncorrect());
        textMaxStreak.setText("Max Streak: " + engine.getMaxStreak());

        btnShare.setOnClickListener(v -> {
            String message = "I scored a Streak of " + engine.getCurrentStreak()
                + " in SporTrivia! Can you beat that?";
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(Intent.createChooser(shareIntent, "Share your Score"));
        });

        btnAnswers.setOnClickListener(v -> {
            startActivity(new Intent(this, AnswersActivity.class));
        });

        btnDone.setOnClickListener(v -> {
            if (SporTriviaSDK.getActiveDelegate() != null) {
                SporTriviaSDK.getActiveDelegate().onGameComplete(engine.buildResult());
            }
            SporTriviaSession.clear();
            SporTriviaSDK.clearActiveDelegate();
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        if (SporTriviaSDK.getActiveDelegate() != null) {
            SporTriviaSDK.getActiveDelegate().onGameComplete(
                SporTriviaSession.getEngine().buildResult()
            );
        }
        SporTriviaSession.clear();
        SporTriviaSDK.clearActiveDelegate();
        super.onBackPressed();
    }
}
