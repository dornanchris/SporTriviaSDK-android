package com.sportrivia.sdk.internal.ui;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.sportrivia.sdk.R;
import com.sportrivia.sdk.internal.logic.GameEngine;
import com.sportrivia.sdk.internal.models.PlayerInfo;

/**
 * Answers screen — displays the remaining correct answers after the game.
 */
public class AnswersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sportrivia_sdk_answers);

        GameEngine engine = SporTriviaSession.getEngine();
        if (engine == null) {
            finish();
            return;
        }

        TextView textTeamName = findViewById(R.id.sportrivia_sdk_text_team_name);
        ImageView imageTeam = findViewById(R.id.sportrivia_sdk_image_team);
        TableLayout tableLayout = findViewById(R.id.sportrivia_sdk_table_answers);
        Button btnBack = findViewById(R.id.sportrivia_sdk_btn_back);

        textTeamName.setText(engine.getTeam1Name());

        byte[] imageBytes = SporTriviaSession.getTeamImageBytes();
        if (imageBytes != null) {
            imageTeam.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
        }

        for (PlayerInfo player : engine.getRemainingCorrectPlayers()) {
            TableRow row = new TableRow(this);
            TextView nameView = new TextView(this);
            nameView.setText(player.playerName);
            nameView.setPadding(16, 12, 16, 12);
            nameView.setTextSize(16);
            row.addView(nameView);

            TextView yearsView = new TextView(this);
            yearsView.setText(player.yearsPlayed);
            yearsView.setPadding(16, 12, 16, 12);
            yearsView.setTextSize(14);
            row.addView(yearsView);

            tableLayout.addView(row);
        }

        btnBack.setOnClickListener(v -> finish());
    }
}
