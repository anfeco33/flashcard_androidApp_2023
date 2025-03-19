package com.example.myapplication.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.model.Vocabulary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TypeWordActivity extends AppCompatActivity {

    private TextView tvStudyProgress, tvWordOrDefinition, tvFeedback, tvSuggest;
    private CardView cardview;
    private EditText etAnswer;
    private Button btnNextWord;
    private TextToSpeech textToSpeech;
    private int currentWordIndex = 0;
    private boolean feedbackEnabled;
    private boolean displayEnglishAskVietnamese;
    private boolean isAutoSpeakEnabled;
    private boolean textToSpeechInitialized = false;
    private List<Vocabulary> vocabularies;
    private boolean autoMoveNext;
    private int score = 0;
    private void speak(String textToSpeak) {
        if (textToSpeechInitialized) {
            textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_word);

        tvStudyProgress = findViewById(R.id.tvStudyProgress);
        tvWordOrDefinition = findViewById(R.id.tvWordOrDefinition);
        tvFeedback = findViewById(R.id.tvFeedback);
        tvSuggest = findViewById(R.id.tvSuggest);
        etAnswer = findViewById(R.id.etAnswer);
        btnNextWord = findViewById(R.id.btnNextWord);
        cardview = findViewById(R.id.cardview);

        vocabularies = new ArrayList<>();

        Intent intent = getIntent();
        if(intent.hasExtra("vocabList")) {
            vocabularies = (List<Vocabulary>) intent.getSerializableExtra("vocabList");
        }
        autoMoveNext = intent.getBooleanExtra("autoMoveNext", true);
        if (vocabularies == null || vocabularies.isEmpty()) {
            Toast.makeText(this, "Không có dữ liệu từ vựng.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        feedbackEnabled = intent.getBooleanExtra("feedbackEnabled", true);
        displayEnglishAskVietnamese = intent.getBooleanExtra("displayEnglishAskVietnamese", false);
        isAutoSpeakEnabled = intent.getBooleanExtra("isAutoSpeakEnabled", false);
        boolean isShuffleEnabled = getIntent().getBooleanExtra("isShuffleEnabled", false);
        if (isShuffleEnabled) {
            Collections.shuffle(vocabularies);
        }

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TextToSpeech", "This Language is not supported");
                } else {
                    textToSpeechInitialized = true;
                    if (isAutoSpeakEnabled && vocabularies.size() > 0) {
                        Vocabulary firstVocabulary = vocabularies.get(0);
                        String toSpeak = firstVocabulary.getWord();
                        speak(toSpeak);
                    }
                }
            } else {
                Log.e("TextToSpeech", "Initialization failed");
            }
        });
        displayNextWord();
        btnNextWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userAnswer = etAnswer.getText().toString().trim();
                checkAnswer(userAnswer);
            }
        });
    }
    private void updateProgress() {
        if (vocabularies != null && !vocabularies.isEmpty()) {
            String progressText = (currentWordIndex + 1) + "/" + vocabularies.size();
            tvStudyProgress.setText(progressText);
        }
    }

    private void displayNextWord() {
        if (currentWordIndex >= vocabularies.size()) {
            endActivity();
            return;
        }
        Vocabulary currentVocabulary = vocabularies.get(currentWordIndex);
        String questionText = displayEnglishAskVietnamese
                ? currentVocabulary.getDefinition() + " viết thế nào?"
                : "Nghĩa của từ '" + currentVocabulary.getWord() + "' là gì?";

        tvWordOrDefinition.setText(questionText);

        String toDisplay = displayEnglishAskVietnamese ? currentVocabulary.getDefinition() : currentVocabulary.getWord();
        tvWordOrDefinition.setText(toDisplay);

        updateProgress();

        if (isAutoSpeakEnabled && textToSpeechInitialized) {
            textToSpeech.speak(currentVocabulary.getWord(), TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }
    private void checkAnswer(String userAnswer) {
        Vocabulary currentVocabulary = vocabularies.get(currentWordIndex);
        String correctAnswer = displayEnglishAskVietnamese ? currentVocabulary.getWord() : currentVocabulary.getDefinition();

        if (userAnswer.equalsIgnoreCase(correctAnswer)) {
            score++;
            if (feedbackEnabled) {
                tvFeedback.setText("Chúc mừng bạn, bạn trả lời đúng rồi!");
                tvFeedback.setVisibility(View.VISIBLE);
            }
        } else {
            if (feedbackEnabled) {
                tvFeedback.setText("Rất tiếc! Đáp án đúng là: " + correctAnswer);
                tvFeedback.setVisibility(View.VISIBLE);
            }
        }
        etAnswer.setText(""); // Xóa trường nhập để sẵn sàng cho từ tiếp theo

        // Tự động chuyển sang từ tiếp theo sau mỗi câu trả lời
        if (autoMoveNext && currentWordIndex < vocabularies.size() - 1) {
            currentWordIndex++;
            new Handler().postDelayed(this::displayNextWord, 2000);
        } else if (currentWordIndex >= vocabularies.size() - 1) {
            endActivity();
        }
    }

    private void endActivity() {
        tvWordOrDefinition.setText("Kết thúc bài trắc nghiệm!");
        tvWordOrDefinition.setTextSize(24);
        tvFeedback.setText("Điểm của bạn là: " + score + "/" + vocabularies.size());
        tvFeedback.setVisibility(View.VISIBLE);
        double percentage = (double) score / vocabularies.size() * 100;

        if (percentage < 50) {
            tvSuggest.setText("Bạn cần ôn tập thêm!");
        } else {
            tvSuggest.setText("Làm rất tốt! Bạn có thể thêm nhiều từ nâng cao hơn.");
        }
        tvSuggest.setVisibility(View.VISIBLE);
        // hide
        tvStudyProgress.setVisibility(View.GONE);
        cardview.setVisibility(View.GONE);
        etAnswer.setVisibility(View.GONE);
        btnNextWord.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
