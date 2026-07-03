package com.sportrivia.sdk.internal.models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data-capture configuration embedded in the answer key by the SporTrivia
 * portal (the question's Data Capture step). Drives which fields the
 * player-info screen shows and which answers are uploaded with results.
 */
public final class CollectFields {

    public final boolean name;
    public final boolean email;
    public final boolean phone;
    public final boolean over18;
    public final List<CustomQuestion> customQuestions;

    public CollectFields(boolean name, boolean email, boolean phone, boolean over18,
                         List<CustomQuestion> customQuestions) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.over18 = over18;
        this.customQuestions = Collections.unmodifiableList(new ArrayList<>(customQuestions));
    }

    /**
     * Answer keys written before data capture existed have no collect_fields —
     * keep the original name/email/phone behavior for them.
     */
    public static CollectFields legacyDefault() {
        return new CollectFields(true, true, true, false, new ArrayList<CustomQuestion>());
    }

    /** Parse the collect_fields object from an answer key JSON. */
    public static CollectFields fromJson(JSONObject json) {
        List<CustomQuestion> questions = new ArrayList<>();
        JSONArray rawQuestions = json.optJSONArray("custom_questions");
        if (rawQuestions != null) {
            for (int i = 0; i < rawQuestions.length(); i++) {
                JSONObject item = rawQuestions.optJSONObject(i);
                if (item == null) {
                    continue;
                }
                String label = item.optString("label", "").trim();
                if (label.isEmpty()) {
                    continue;
                }
                String id = item.optString("id", "").trim();
                questions.add(new CustomQuestion(
                        id.isEmpty() ? label : id,
                        label,
                        item.optString("placeholder", "").trim(),
                        item.optBoolean("required", false)
                ));
            }
        }
        return new CollectFields(
                json.optBoolean("name", false),
                json.optBoolean("email", false),
                json.optBoolean("phone", false),
                json.optBoolean("over_18", false),
                questions
        );
    }

    public boolean hasAnythingToCollect() {
        return name || email || phone || over18 || !customQuestions.isEmpty();
    }

    /** A custom data-collection question configured in the portal. */
    public static final class CustomQuestion {
        public final String id;
        public final String label;
        public final String placeholder;
        public final boolean required;

        public CustomQuestion(String id, String label, String placeholder, boolean required) {
            this.id = id;
            this.label = label;
            this.placeholder = placeholder;
            this.required = required;
        }
    }
}
