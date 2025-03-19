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
import android.widget.CheckBox;
import android.widget.CompoundButton;
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

public class FlashcardActivity extends AppCompatActivity {
    private int currentPosition = 0;
    private TextView tvWord, tvDefinition, tvStudyProgress;
    private CheckBox cbAutoPass;
    private Handler autoPassHandler = new Handler();
    private Button btnFinish;
    private ImageView imgbSpeak;
    private CardView flashcard;
    private boolean isShowingWord = true;
    private TextToSpeech textToSpeech;
    private boolean isShuffleEnabled = false;
    private boolean isAutoSpeakEnabled = true;
    private boolean isReverseLanguages = false;
    private boolean isTTSInitialized = false;
    private List<Vocabulary> vocabularies;
    private boolean isAutoPassing = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);

        flashcard = findViewById(R.id.flashcard);
        tvWord = findViewById(R.id.tvWord);
        tvDefinition = findViewById(R.id.tvDefinition);
        imgbSpeak = findViewById(R.id.imgbSpeak);
        tvStudyProgress = findViewById(R.id.tvStudyProgress);
        cbAutoPass = findViewById(R.id.cbAutoPass);
        btnFinish = findViewById(R.id.btnFinish);

        vocabularies = new ArrayList<>();


        Intent intent = getIntent();
        if(intent.hasExtra("vocabList")) {
            vocabularies = (List<Vocabulary>) intent.getSerializableExtra("vocabList");
        }

        if (vocabularies == null || vocabularies.isEmpty()) {
            Toast.makeText(this, "Không có dữ liệu từ vựng.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        isReverseLanguages = intent.getBooleanExtra("isReverseLanguages", false);

        isShuffleEnabled = intent.getBooleanExtra("isShuffleEnabled", false);

        if (isShuffleEnabled) {
            shuffleVocabulary();
        }

        showVocabulary(currentPosition); // hiển thị từ đầu tiên
        if (isAutoSpeakEnabled) {
            autoSpeak();
        }

        isShuffleEnabled = intent.getBooleanExtra("isShuffleEnabled", false);
        isAutoSpeakEnabled = intent.getBooleanExtra("isAutoSpeakEnabled", true);
        isReverseLanguages = intent.getBooleanExtra("isReverseLanguages", false);
        boolean isStarredOnly = intent.getBooleanExtra("isStarredOnly", false);

        setupFlashcard();

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.US);
                    if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                        isTTSInitialized = true; // flag TextToSpeech đã sẵn sàng
                        autoSpeak();
                    }
                } else {
                    Log.e("TextToSpeech", "Initialization failed");
                }
            }
        });
        imgbSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoSpeak();
            }
        });
        cbAutoPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    autoPassHandler.post(autoPassRunnable); // bắt đầu chu trình tự động lật card
                } else {
                    autoPassHandler.removeCallbacks(autoPassRunnable); // dừng chu trình tự động lật card
                }
            }
        });
        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FlashcardActivity.this, TopicManagement.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private Runnable autoPassRunnable = new Runnable() {
        @Override
        public void run() {
            if (!cbAutoPass.isChecked() || isAutoPassing) {
                return;
            }
            isAutoPassing = true; // flag bắt đầu lật

            toggleCard();
            autoPassHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (cbAutoPass.isChecked()) {
                        nextCard();
                    }
                    isAutoPassing = false; // flag ket thúc
                }
            }, 2000);
        }
    };
    private void setupFlashcard() {
        flashcard.setOnTouchListener(new OnSwipeTouchListener(FlashcardActivity.this) {
            public boolean onSwipeTop() {
                toggleCard();
                return true;
            }
            public boolean onSwipeBottom() {
                toggleCard();
                return true;
            }
            public boolean onSwipeLeft() {
                nextCard();
                return true;
            }
            public boolean onSwipeRight() {
                previousCard();
                return true;
            }
            public void onTap() {
                toggleCard();
            }
        });
    }
    private void toggleCard() {
        if (isShowingWord) {
            showDefinition();
            if (isAutoSpeakEnabled) {
                autoSpeak();
            }
        } else {
            showWord();
            if (isAutoSpeakEnabled) {
                autoSpeak();
            }
        }
        isShowingWord = !isShowingWord;
    }

    // Hàm để hiển thị từ tiếp theo
    private void nextCard() {
        // nếu đang ở card cuối và auto pass checked, quay lại card đầu
        if (currentPosition < vocabularies.size() - 1) {
            currentPosition++;
        } else {
            currentPosition = 0; // reset nếu đến card cuối
        }
        showVocabulary(currentPosition);
        updateStudyProgress();
        //autoSpeak();

        if (cbAutoPass.isChecked()) {
            autoPassHandler.postDelayed(autoPassRunnable, 3000); // Tiếp tục lặp nếu auto pass checked
        }
    }

    private void previousCard() {
        if (currentPosition > 0) {
            currentPosition--;
            if (isReverseLanguages) {
                isShowingWord = true;
            }
            showVocabulary(currentPosition);
            autoSpeak();
        }
    }
    private void updateStudyProgress() {
        String progressText = (currentPosition + 1) + "/" + vocabularies.size();
        tvStudyProgress.setText(progressText);
    }
    private void showVocabulary(int position) {
        if (position >= 0 && position < vocabularies.size()) {
            Vocabulary vocabulary = vocabularies.get(position);
            tvWord.setText(vocabulary.getWord());
            tvDefinition.setText(vocabulary.getDefinition());
            updateStudyProgress();
            applyReverseLanguageState();

            if (isReverseLanguages && !isShowingWord) {
                showDefinition();
            } else {
                showWord();
            }

            if (isAutoSpeakEnabled) {
                autoSpeak();
            }
        } else {
            Toast.makeText(FlashcardActivity.this, "Lỗi, không thể hiển thị từ vựng.", Toast.LENGTH_LONG).show();
        }
    }

    private void showDefinition() {
        tvDefinition.setVisibility(View.VISIBLE);
        tvWord.setVisibility(View.INVISIBLE);
        imgbSpeak.setVisibility(View.INVISIBLE);
    }

    private void showWord() {
        tvWord.setVisibility(View.VISIBLE);
        tvDefinition.setVisibility(View.INVISIBLE);
        imgbSpeak.setVisibility(View.VISIBLE);
        if (isAutoSpeakEnabled) {
            autoSpeak();
        }
    }

    private void shuffleVocabulary() {
        if (vocabularies != null && !vocabularies.isEmpty()) {
            Collections.shuffle(vocabularies);
        }
    }

    private void autoSpeak() {
        if (isAutoSpeakEnabled && isTTSInitialized) {
            String textToSpeak = tvWord.getText().toString();
            textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    protected void onDestroy() {
        // xóa callbacks + tắt TextToSpeech
        autoPassHandler.removeCallbacks(autoPassRunnable);
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        isAutoPassing = false;
        super.onDestroy();
    }

    private void applyReverseLanguageState() {
        if (isReverseLanguages) {
            tvDefinition.setVisibility(View.VISIBLE);
            tvWord.setVisibility(View.GONE);
            imgbSpeak.setVisibility(View.GONE);
            isShowingWord = false;
        } else {
            tvDefinition.setVisibility(View.GONE);
            tvWord.setVisibility(View.VISIBLE);
            imgbSpeak.setVisibility(View.VISIBLE);
            isShowingWord = true;
        }
    }

}