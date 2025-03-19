package com.example.myapplication.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.model.Vocabulary;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class QuizActivity extends AppCompatActivity {

    private TextView tvQuestion, tvFeedback, tvStudyProgress, tvSuggest;
    private Button btnOption1, btnOption2, btnOption3, btnOption4;
    private List<Vocabulary> vocabularies;
    private int currentQuestionIndex = 0;
    private ImageView imgbSpeak;

    private int score = 0;
    private boolean feedbackEnabled;
    private boolean displayEnglishAskVietnamese;
    private boolean isAutoSpeakEnabled;
    private TextToSpeech textToSpeech;
    private boolean textToSpeechInitialized = false;
    private void speak(String textToSpeak) {
        if (textToSpeechInitialized) {
            textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        tvQuestion = findViewById(R.id.tvQuestion);
        tvFeedback = findViewById(R.id.tvFeedback);
        tvStudyProgress = findViewById(R.id.tvStudyProgress);
        tvSuggest = findViewById(R.id.tvSuggest);
        btnOption1 = findViewById(R.id.btnOption1);
        btnOption2 = findViewById(R.id.btnOption2);
        btnOption3 = findViewById(R.id.btnOption3);
        btnOption4 = findViewById(R.id.btnOption4);
        imgbSpeak = findViewById(R.id.imgbSpeak);


        Intent intent = getIntent();
        if(intent.hasExtra("vocabList")) {
            vocabularies = (List<Vocabulary>) intent.getSerializableExtra("vocabList");
        }
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

        btnOption1.setOnClickListener(v -> checkAnswer(btnOption1.getText().toString()));
        btnOption2.setOnClickListener(v -> checkAnswer(btnOption2.getText().toString()));
        btnOption3.setOnClickListener(v -> checkAnswer(btnOption3.getText().toString()));
        btnOption4.setOnClickListener(v -> checkAnswer(btnOption4.getText().toString()));
        showNextQuestion();

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

        imgbSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vocabulary currentVocabulary = vocabularies.get(currentQuestionIndex);

                if (textToSpeechInitialized && isAutoSpeakEnabled) {
                    String textToSpeak = currentVocabulary.getWord();
                    speak(textToSpeak);
                }
            }
        });
    }

    private void showNextQuestion() {
        if (currentQuestionIndex < vocabularies.size()) {
            Vocabulary currentVocabulary = vocabularies.get(currentQuestionIndex);

            if (textToSpeechInitialized && isAutoSpeakEnabled) {
                String textToSpeak = currentVocabulary.getWord();
                speak(textToSpeak);
            }

            String questionText = displayEnglishAskVietnamese
                    ? currentVocabulary.getDefinition() + " viết thế nào?"
                    : "Nghĩa của từ '" + currentVocabulary.getWord() + "' là gì?";

            tvQuestion.setText(questionText);
            setOptionsForQuestion(currentVocabulary);
            tvStudyProgress.setText((currentQuestionIndex + 1) + "/" + vocabularies.size());
        } else {
            endQuiz();
        }
    }

    private void setOptionsForQuestion(Vocabulary correctAnswer) {
        List<String> options = new ArrayList<>();
        List<Vocabulary> wrongVocabularies = new ArrayList<>(vocabularies);
        wrongVocabularies.remove(correctAnswer); // bỏ từ đúng ra đáp án

        while (options.size() < 3 && wrongVocabularies.size() > 0) {
            Vocabulary wrongVocabulary = wrongVocabularies.remove(0);
            options.add(displayEnglishAskVietnamese ? wrongVocabulary.getWord() : wrongVocabulary.getDefinition());
        }

        options.add(displayEnglishAskVietnamese ? correctAnswer.getWord() : correctAnswer.getDefinition());
        Collections.shuffle(options); // trộn đáp án

        btnOption1.setText(options.get(0));
        btnOption2.setText(options.get(1));
        btnOption3.setText(options.get(2));
        btnOption4.setText(options.get(3));
    }

    private void checkAnswer(String selectedOption) {
        Vocabulary currentVocabulary = vocabularies.get(currentQuestionIndex);
        boolean isCorrect = displayEnglishAskVietnamese
                ? selectedOption.equalsIgnoreCase(currentVocabulary.getWord())
                : selectedOption.equalsIgnoreCase(currentVocabulary.getDefinition());

        if (isCorrect) {
            score++;
            if (feedbackEnabled) {
                tvFeedback.setText("Chúc mừng bạn, bạn trả lời đúng rồi!");
                tvFeedback.setVisibility(View.VISIBLE);
            }
        } else {
            if (feedbackEnabled) {
                String correctAnswer = displayEnglishAskVietnamese
                        ? currentVocabulary.getWord()
                        : currentVocabulary.getDefinition();
                tvFeedback.setText("Rất tiếc! Đáp án đúng là: " + correctAnswer);
                tvFeedback.setVisibility(View.VISIBLE);
            }
        }

        new Handler().postDelayed(() -> {
            tvFeedback.setVisibility(View.GONE);
            currentQuestionIndex++;
            showNextQuestion();
        }, feedbackEnabled ? 2000 : 500);
    }

    private void endQuiz() {
        tvQuestion.setText("Kết thúc bài trắc nghiệm!");
        tvQuestion.setTextSize(24);
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
        btnOption1.setVisibility(View.GONE);
        btnOption2.setVisibility(View.GONE);
        btnOption3.setVisibility(View.GONE);
        btnOption4.setVisibility(View.GONE);
        imgbSpeak.setVisibility(View.GONE);
    }
}
