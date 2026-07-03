package com.sportrivia.sdk.internal.ui;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.sportrivia.sdk.R;
import com.sportrivia.sdk.internal.logic.GameEngine;
import com.sportrivia.sdk.internal.logic.PlayerInfoService;
import com.sportrivia.sdk.internal.models.PlayerInfo;
import com.sportrivia.sdk.public_api.SporTriviaSDK;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main game screen — player input, autocomplete, and score tracking.
 */
public class CustomGameActivity extends AppCompatActivity {
    private TextView textCorrect, textPlayersLeft, textQuestion;
    private ImageView imageTeam1, imageCheck, imageX;
    private AutoCompleteTextView autoComplete;
    private Button buttonSubmit, buttonGiveUp;
    private ArrayAdapter<String> autoCompleteAdapter;

    private GameEngine engine;
    private PlayerInfo selectedPlayerInfo;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sportrivia_sdk_custom_game);

        textCorrect = findViewById(R.id.sportrivia_sdk_text_correct);
        textPlayersLeft = findViewById(R.id.sportrivia_sdk_text_players_left);
        textQuestion = findViewById(R.id.sportrivia_sdk_text_question);
        imageTeam1 = findViewById(R.id.sportrivia_sdk_image_team1);
        imageCheck = findViewById(R.id.sportrivia_sdk_image_check);
        imageX = findViewById(R.id.sportrivia_sdk_image_x);
        autoComplete = findViewById(R.id.sportrivia_sdk_auto_complete);
        buttonSubmit = findViewById(R.id.sportrivia_sdk_btn_submit);
        buttonGiveUp = findViewById(R.id.sportrivia_sdk_btn_give_up);

        engine = SporTriviaSession.getEngine();
        if (engine == null) {
            finish();
            return;
        }

        setupQuestion();
        setupTeamImage();
        SponsorBanner.bind(this,
                findViewById(R.id.sportrivia_sdk_sponsor_banner),
                findViewById(R.id.sportrivia_sdk_text_sponsor),
                engine);
        updateScoreDisplay();
        setupAutoComplete();

        buttonSubmit.setOnClickListener(v -> onSubmit());
        buttonGiveUp.setOnClickListener(v -> endGame());
    }

    private void setupQuestion() {
        String question = engine.getCustomQuestion();
        if (question != null && !question.isEmpty()) {
            textQuestion.setText(question);
            textQuestion.setVisibility(View.VISIBLE);
            imageTeam1.setVisibility(View.GONE);
        } else {
            textQuestion.setVisibility(View.GONE);
            imageTeam1.setVisibility(View.VISIBLE);
        }
    }

    private void setupTeamImage() {
        byte[] imageBytes = SporTriviaSession.getTeamImageBytes();
        if (imageBytes != null) {
            imageTeam1.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
        }
    }

    private void setupAutoComplete() {
        autoCompleteAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        autoComplete.setAdapter(autoCompleteAdapter);
        autoComplete.setThreshold(1);

        autoComplete.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) {
                selectedPlayerInfo = null;
                String input = s.toString();
                if (input.length() >= 1) {
                    List<PlayerInfo> filtered = PlayerInfoService.filterPlayers(input, engine.getAllPlayers(), 10);
                    List<String> names = new ArrayList<>();
                    for (PlayerInfo p : filtered) {
                        names.add(p.playerName + " (" + p.yearsPlayed + ")");
                    }
                    autoCompleteAdapter.clear();
                    autoCompleteAdapter.addAll(names);
                    autoCompleteAdapter.notifyDataSetChanged();
                }
                buttonSubmit.setEnabled(!input.trim().isEmpty());
            }
        });

        autoComplete.setOnItemClickListener((parent, view, position, id) -> {
            String input = autoComplete.getText().toString();
            String name = input.contains(" (") ? input.substring(0, input.lastIndexOf(" (")) : input;
            List<PlayerInfo> filtered = PlayerInfoService.filterPlayers(name, engine.getAllPlayers(), 10);
            if (position < filtered.size()) {
                selectedPlayerInfo = filtered.get(position);
            }
        });
    }

    private void onSubmit() {
        String input = autoComplete.getText().toString().trim();
        if (input.isEmpty()) return;

        PlayerInfo player;
        if (selectedPlayerInfo != null) {
            player = selectedPlayerInfo;
        } else {
            player = PlayerInfoService.selectPlayerFromInput(input, engine.getAllPlayers());
        }

        if (engine.isPlayerUsed(player.playerId)) {
            showFeedback("Player Already Used!", null);
            clearInput();
            return;
        }

        boolean correct = engine.checkAnswer(player);
        if (correct) {
            showFeedback(null, true);
            if (engine.allAnswersFound()) {
                handler.postDelayed(this::endGame, 1200);
            }
        } else {
            showFeedback(null, false);
        }

        updateScoreDisplay();
        clearInput();
    }

    private void updateScoreDisplay() {
        textCorrect.setText("Correct: " + engine.getCorrect());
        textPlayersLeft.setText("Players Left: " + engine.getRemainingCorrectPlayers().size());
    }

    private void showFeedback(String message, Boolean isCorrect) {
        if (isCorrect != null) {
            if (isCorrect) {
                imageCheck.setVisibility(View.VISIBLE);
                handler.postDelayed(() -> imageCheck.setVisibility(View.GONE), 1000);
            } else {
                imageX.setVisibility(View.VISIBLE);
                handler.postDelayed(() -> imageX.setVisibility(View.GONE), 1000);
            }
        }
    }

    private void clearInput() {
        autoComplete.setText("");
        selectedPlayerInfo = null;
        buttonSubmit.setEnabled(false);
    }

    private void endGame() {
        executor.execute(() -> engine.uploadResults());

        Intent intent = new Intent(this, GameOverActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
