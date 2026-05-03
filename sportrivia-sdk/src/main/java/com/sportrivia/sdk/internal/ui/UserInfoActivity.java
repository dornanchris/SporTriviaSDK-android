package com.sportrivia.sdk.internal.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.sportrivia.sdk.R;
import com.sportrivia.sdk.internal.logic.GameEngine;
import com.sportrivia.sdk.public_api.Sport;
import com.sportrivia.sdk.public_api.SporTriviaSDK;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Collects user information and downloads game data before starting the game.
 */
public class UserInfoActivity extends AppCompatActivity {
    private EditText editFirstName, editLastName, editEmail, editPhone;
    private CheckBox checkSaveInfo;
    private Button buttonSubmit;
    private String gameId;
    private String sportCode;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean dataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sportrivia_sdk_user_info);

        editFirstName = findViewById(R.id.sportrivia_sdk_edit_first_name);
        editLastName = findViewById(R.id.sportrivia_sdk_edit_last_name);
        editEmail = findViewById(R.id.sportrivia_sdk_edit_email);
        editPhone = findViewById(R.id.sportrivia_sdk_edit_phone);
        checkSaveInfo = findViewById(R.id.sportrivia_sdk_check_save);
        buttonSubmit = findViewById(R.id.sportrivia_sdk_btn_submit);
        Button buttonCancel = findViewById(R.id.sportrivia_sdk_btn_cancel);

        gameId = getIntent().getStringExtra("gameId");
        sportCode = getIntent().getStringExtra("sport");

        SharedPreferences prefs = getSharedPreferences("sportrivia_sdk", MODE_PRIVATE);
        if (prefs.getBoolean("saveUserInfo", false)) {
            editFirstName.setText(prefs.getString("firstName", ""));
            editLastName.setText(prefs.getString("lastName", ""));
            editEmail.setText(prefs.getString("email", ""));
            editPhone.setText(prefs.getString("phoneNumber", ""));
            checkSaveInfo.setChecked(true);
        }

        TextWatcher watcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) { updateSubmitButton(); }
        };
        editFirstName.addTextChangedListener(watcher);
        editLastName.addTextChangedListener(watcher);
        editEmail.addTextChangedListener(watcher);
        editPhone.addTextChangedListener(watcher);

        buttonSubmit.setEnabled(false);

        executor.execute(() -> {
            try {
                GameEngine engine = SporTriviaSession.getOrCreateEngine();
                Sport sport = Sport.fromCode(sportCode);
                engine.loadGame(gameId, sport);

                try {
                    byte[] imageBytes = engine.downloadTeamImage();
                    SporTriviaSession.setTeamImageBytes(imageBytes);
                } catch (Exception e) {
                    // Non-fatal: team image is optional
                }

                dataLoaded = true;
                runOnUiThread(this::updateSubmitButton);
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    if (SporTriviaSDK.getActiveDelegate() != null) {
                        SporTriviaSDK.getActiveDelegate().onGameError(e);
                    }
                    finish();
                });
            }
        });

        buttonSubmit.setOnClickListener(v -> onSubmit());
        buttonCancel.setOnClickListener(v -> {
            if (SporTriviaSDK.getActiveDelegate() != null) {
                SporTriviaSDK.getActiveDelegate().onGameCancelled();
            }
            SporTriviaSession.clear();
            finish();
        });
    }

    private void updateSubmitButton() {
        boolean formValid = !editFirstName.getText().toString().trim().isEmpty()
            && !editLastName.getText().toString().trim().isEmpty()
            && !editEmail.getText().toString().trim().isEmpty()
            && !editPhone.getText().toString().trim().isEmpty();
        buttonSubmit.setEnabled(formValid && dataLoaded);
    }

    private void onSubmit() {
        String first = editFirstName.getText().toString().trim();
        String last = editLastName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();

        SharedPreferences prefs = getSharedPreferences("sportrivia_sdk", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (checkSaveInfo.isChecked()) {
            editor.putBoolean("saveUserInfo", true);
            editor.putString("firstName", first);
            editor.putString("lastName", last);
            editor.putString("email", email);
            editor.putString("phoneNumber", phone);
        } else {
            editor.putBoolean("saveUserInfo", false);
            editor.remove("firstName");
            editor.remove("lastName");
            editor.remove("email");
            editor.remove("phoneNumber");
        }
        editor.apply();

        GameEngine engine = SporTriviaSession.getEngine();
        if (engine != null) {
            engine.setUserInfo(first, last, email, phone);
            engine.startGame();
        }

        Intent intent = new Intent(this, CustomGameActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
