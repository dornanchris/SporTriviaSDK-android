package com.sportrivia.sdk.internal.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.sportrivia.sdk.R;
import com.sportrivia.sdk.internal.logic.GameEngine;
import com.sportrivia.sdk.internal.models.CollectFields;
import com.sportrivia.sdk.public_api.Sport;
import com.sportrivia.sdk.public_api.SporTriviaSDK;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Collects player information and downloads game data before starting the game.
 *
 * <p>The form is built dynamically from the question's Data Capture
 * configuration in the SporTrivia portal ({@code collect_fields} in the
 * answer key JSON): standard name/email/phone fields, an over-18 checkbox,
 * and any custom questions. Legacy answer keys without a configuration show
 * the original name/email/phone form. If the question collects nothing, this
 * screen is skipped and the game starts immediately.
 */
public class UserInfoActivity extends AppCompatActivity {
    private EditText editFirstName, editLastName, editEmail, editPhone;
    private CheckBox checkSaveInfo, checkOver18;
    private Button buttonSubmit;
    private LinearLayout formContainer, customFieldsContainer;
    private TextView formLoading;
    private String gameId;
    private String sportCode;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean dataLoaded = false;
    private CollectFields collectFields;
    private final Map<CollectFields.CustomQuestion, EditText> customInputs = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sportrivia_sdk_user_info);

        editFirstName = findViewById(R.id.sportrivia_sdk_edit_first_name);
        editLastName = findViewById(R.id.sportrivia_sdk_edit_last_name);
        editEmail = findViewById(R.id.sportrivia_sdk_edit_email);
        editPhone = findViewById(R.id.sportrivia_sdk_edit_phone);
        checkSaveInfo = findViewById(R.id.sportrivia_sdk_check_save);
        checkOver18 = findViewById(R.id.sportrivia_sdk_check_over18);
        buttonSubmit = findViewById(R.id.sportrivia_sdk_btn_submit);
        formContainer = findViewById(R.id.sportrivia_sdk_form_container);
        customFieldsContainer = findViewById(R.id.sportrivia_sdk_custom_fields);
        formLoading = findViewById(R.id.sportrivia_sdk_form_loading);
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
                runOnUiThread(() -> configureForm(engine));
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

    /** Build the form from the answer key's data-capture configuration. */
    private void configureForm(GameEngine engine) {
        collectFields = engine.getCollectFields();

        if (!collectFields.hasAnythingToCollect()) {
            // Nothing configured to collect: skip this screen entirely.
            engine.setUserInfo("", "", "", "", false, new HashMap<String, String>());
            engine.startGame();
            startActivity(new Intent(this, CustomGameActivity.class));
            finish();
            return;
        }

        editFirstName.setVisibility(collectFields.name ? View.VISIBLE : View.GONE);
        editLastName.setVisibility(collectFields.name ? View.VISIBLE : View.GONE);
        editEmail.setVisibility(collectFields.email ? View.VISIBLE : View.GONE);
        editPhone.setVisibility(collectFields.phone ? View.VISIBLE : View.GONE);
        checkOver18.setVisibility(collectFields.over18 ? View.VISIBLE : View.GONE);

        boolean hasStandardFields = collectFields.name || collectFields.email || collectFields.phone;
        checkSaveInfo.setVisibility(hasStandardFields ? View.VISIBLE : View.GONE);

        TextWatcher watcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) { updateSubmitButton(); }
        };

        customFieldsContainer.removeAllViews();
        customInputs.clear();
        float density = getResources().getDisplayMetrics().density;
        for (CollectFields.CustomQuestion question : collectFields.customQuestions) {
            TextView label = new TextView(this);
            label.setText(question.required ? question.label + " *" : question.label);
            label.setTextColor(ContextCompat.getColor(this, R.color.sportrivia_sdk_text));
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            labelParams.bottomMargin = (int) (4 * density);
            customFieldsContainer.addView(label, labelParams);

            EditText input = new EditText(this);
            input.setHint(question.placeholder.isEmpty() ? question.label : question.placeholder);
            input.setBackgroundResource(R.color.sportrivia_sdk_input_bg);
            int padding = (int) (16 * density);
            input.setPadding(padding, padding, padding, padding);
            input.addTextChangedListener(watcher);
            LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            inputParams.bottomMargin = (int) (12 * density);
            customFieldsContainer.addView(input, inputParams);

            customInputs.put(question, input);
        }

        formLoading.setVisibility(View.GONE);
        formContainer.setVisibility(View.VISIBLE);
        updateSubmitButton();
    }

    private void updateSubmitButton() {
        if (collectFields == null) {
            buttonSubmit.setEnabled(false);
            return;
        }
        boolean formValid = true;
        if (collectFields.name) {
            formValid = !editFirstName.getText().toString().trim().isEmpty()
                    && !editLastName.getText().toString().trim().isEmpty();
        }
        if (formValid && collectFields.email) {
            formValid = !editEmail.getText().toString().trim().isEmpty();
        }
        if (formValid && collectFields.phone) {
            formValid = !editPhone.getText().toString().trim().isEmpty();
        }
        if (formValid) {
            for (Map.Entry<CollectFields.CustomQuestion, EditText> entry : customInputs.entrySet()) {
                if (entry.getKey().required
                        && entry.getValue().getText().toString().trim().isEmpty()) {
                    formValid = false;
                    break;
                }
            }
        }
        buttonSubmit.setEnabled(formValid && dataLoaded);
    }

    private void onSubmit() {
        String first = collectFields.name ? editFirstName.getText().toString().trim() : "";
        String last = collectFields.name ? editLastName.getText().toString().trim() : "";
        String email = collectFields.email ? editEmail.getText().toString().trim() : "";
        String phone = collectFields.phone ? editPhone.getText().toString().trim() : "";
        boolean over18 = collectFields.over18 && checkOver18.isChecked();

        // Keyed by question label so downstream consumers (portal contacts
        // view, CSV export) show the question text, not an internal id.
        Map<String, String> customAnswers = new LinkedHashMap<>();
        for (Map.Entry<CollectFields.CustomQuestion, EditText> entry : customInputs.entrySet()) {
            String answer = entry.getValue().getText().toString().trim();
            if (!answer.isEmpty()) {
                customAnswers.put(entry.getKey().label, answer);
            }
        }

        boolean hasStandardFields = collectFields.name || collectFields.email || collectFields.phone;
        SharedPreferences prefs = getSharedPreferences("sportrivia_sdk", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (checkSaveInfo.isChecked() && hasStandardFields) {
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
            engine.setUserInfo(first, last, email, phone, over18, customAnswers);
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
