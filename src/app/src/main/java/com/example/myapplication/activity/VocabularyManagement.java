package com.example.myapplication.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.Nullable;
import android.graphics.Color;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.opencsv.CSVReader;
import android.net.Uri;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.adapter.VocabularyAdapter;
import com.example.myapplication.model.Vocabulary;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.opencsv.exceptions.CsvValidationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VocabularyManagement extends AppCompatActivity {
    private ListView lv_vocabulary;
    private List<Vocabulary> vocabularies;
    private VocabularyAdapter vocabularyAdapter;
    private TextToSpeech textToSpeech;
    private Button btnGo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_management);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        lv_vocabulary = findViewById(R.id.lv_vocabulary);
        vocabularies = new ArrayList<>();
        vocabularyAdapter = new VocabularyAdapter(this, vocabularies);
        lv_vocabulary.setAdapter(vocabularyAdapter);
        btnGo = findViewById(R.id.btnGo);

        String topicId = getIntent().getStringExtra("topicId");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference wordRef = db.collection("words");
        wordRef.whereEqualTo("topicId", topicId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    String word = (String) document.getData().get("word");
                    String definition = (String) document.getData().get("definition");
                    Vocabulary vocabulary = new Vocabulary(word, definition);
                    vocabularies.add(vocabulary);
                }
                vocabularyAdapter.notifyDataSetChanged();
            }
        });

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TextToSpeech", "Language not supported");
                    }
                } else {
                    Log.e("TextToSpeech", "Initialization failed");
                }
            }
        });
        lv_vocabulary.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Vocabulary selectedVocabulary = vocabularyAdapter.getItem(i);
                if (selectedVocabulary != null) {
                    String word = selectedVocabulary.getWord();
                    playPronunciation(word);
                }
            }
        });

        lv_vocabulary.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final CharSequence[] items = {"Học riêng", "Xóa"};
                AlertDialog.Builder builder = new AlertDialog.Builder(VocabularyManagement.this);
                builder.setTitle("Chọn hành động")
                        .setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    Vocabulary selectedVocabulary = vocabularyAdapter.getItem(i);
                                    if (selectedVocabulary != null) {
                                        String word = selectedVocabulary.getWord();
                                        wordRef.whereEqualTo("word", word).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot querySnapshot) {
                                                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                                    String word = (String) document.getData().get("word");
                                                    String definition = (String) document.getData().get("definition");
                                                    Vocabulary vocabulary = new Vocabulary(word, definition);
                                                    db.collection("privateStudyList").add(vocabulary).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            Toast.makeText(VocabularyManagement.this, "Đã thêm từ vào danh sách học riêng", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                } else if (which == 1) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(VocabularyManagement.this);
                                    builder.setTitle("Xác nhận xóa")
                                            .setMessage("Bạn có chắc chắn muốn xóa từ vựng này không?")
                                            .setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Vocabulary selectedVocabulary = vocabularyAdapter.getItem(i);
                                                    if (selectedVocabulary != null) {
                                                        String word = selectedVocabulary.getWord();
                                                        wordRef.whereEqualTo("word", word).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onSuccess(QuerySnapshot querySnapshot) {
                                                                for (QueryDocumentSnapshot document : querySnapshot) {
                                                                    String wordId = document.getId();
                                                                    wordRef.document(wordId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void unused) {
                                                                            db.collection("topics").document(topicId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                                @Override
                                                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                                    int wordCount = Integer.valueOf((String) documentSnapshot.get("wordCount"));
                                                                                    wordCount -= 1;
                                                                                    db.collection("topics").document(topicId).update("wordCount", String.valueOf(wordCount));
                                                                                }
                                                                            });
                                                                            Toast.makeText(VocabularyManagement.this, "Xóa từ vựng thành công", Toast.LENGTH_SHORT).show();
                                                                            vocabularies.remove(selectedVocabulary);
                                                                            vocabularyAdapter.notifyDataSetChanged();
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            }).setNegativeButton("Hủy", null).show();
                                }
                            }
                        })
                        .show();
                return true;
            }
        });


        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStudyOptionsDialog();
            }
        });

    }

    private void showStudyOptionsDialog() {
        final String[] studyOptions = {"Flashcard", "Trắc nghiệm", "Gõ từ"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn phương pháp học");
        builder.setItems(studyOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // flashcard
                        showFlashcardSettingsDialog();
                        break;
                    case 1: // trắc nghiệm
                        int MIN_WORDS_FOR_QUIZ = 4;
                        if (vocabularies.size() < MIN_WORDS_FOR_QUIZ) {
                            new AlertDialog.Builder(VocabularyManagement.this)
                                    .setTitle("Từ vựng không đủ")
                                    .setMessage("Chế độ trắc nghiệm yêu cầu ít nhất " + MIN_WORDS_FOR_QUIZ + " từ vựng. Vui lòng thêm từ vựng hoặc chọn topic khác.")
                                    .setPositiveButton("OK", null)
                                    .show();
                        } else {
                            showQuizType_SettingsDialog();
                        }
                        break;
                    case 2: // gõ từ
                        showTypeWord_SettingsDialog();
                        break;
                }
            }
        });
        builder.show();
    }
    private void showTypeWord_SettingsDialog() {
        final String[] typeSettings = {"Nhận feedback sau mỗi câu", "Hiển thị tiếng Việt/Trả lời bằng tiếng Anh", "Tự động phát âm", "Trộn thứ tự từ"};
        final boolean[] checkedItems = {true, false, true, false}; // cài đặt mặc định

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cài đặt chế độ phương pháp gõ từ");
        builder.setMultiChoiceItems(typeSettings, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                checkedItems[which] = isChecked;
            }
        });
        builder.setPositiveButton("Bắt đầu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!vocabularies.isEmpty()) {
                    Intent intent = new Intent(VocabularyManagement.this, TypeWordActivity.class);

                    intent.putExtra("vocabList", (Serializable) vocabularies);
                    intent.putExtra("feedbackEnabled", checkedItems[0]);
                    intent.putExtra("displayEnglishAskVietnamese", checkedItems[1]);
                    intent.putExtra("isAutoSpeakEnabled", checkedItems[2]);
                    intent.putExtra("isShuffleEnabled", checkedItems[3]);
                    startActivity(intent);
                } else {
                    Toast.makeText(VocabularyManagement.this, "Không có từ vựng nào để học.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
    private void showQuizType_SettingsDialog() {
        final String[] quizSettings = {"Nhận feedback sau mỗi câu", "Hiển thị tiếng Việt/Trả lời bằng tiếng Anh", "Tự động phát âm", "Trộn thứ tự từ"};
        final boolean[] checkedItems = {true, false, true, false}; // cài đặt mặc định

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cài đặt chế độ trắc nghiệm");
        builder.setMultiChoiceItems(quizSettings, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                checkedItems[which] = isChecked;
            }
        });
        builder.setPositiveButton("Bắt đầu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!vocabularies.isEmpty()) {
                    Intent intent = new Intent(VocabularyManagement.this, QuizActivity.class);

                    intent.putExtra("vocabList", (Serializable) vocabularies);
                    intent.putExtra("feedbackEnabled", checkedItems[0]);
                    intent.putExtra("displayEnglishAskVietnamese", checkedItems[1]);
                    intent.putExtra("isAutoSpeakEnabled", checkedItems[2]);
                    intent.putExtra("isShuffleEnabled", checkedItems[3]);
                    startActivity(intent);
                } else {
                    Toast.makeText(VocabularyManagement.this, "Không có từ vựng nào để học.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
    private void showFlashcardSettingsDialog() {
        final String[] flashcardSettings = {
                "Trộn thứ tự từ",
                "Tự động phát âm thanh",
                "Đổi vị trí giữa hai ngôn ngữ",
                "Chỉ học các từ trong danh sách đánh dấu sao"
        };

        final boolean[] checkedItems = {false, true, false, false}; // mặc định

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cài đặt Flashcard");
        builder.setMultiChoiceItems(flashcardSettings, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                checkedItems[which] = isChecked;
            }
        });

        builder.setPositiveButton("Lưu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!vocabularies.isEmpty()) {
                    Intent intent = new Intent(VocabularyManagement.this, FlashcardActivity.class);

                    intent.putExtra("vocabList", (Serializable) vocabularies);
                    intent.putExtra("isShuffleEnabled", checkedItems[0]);
                    intent.putExtra("isAutoSpeakEnabled", checkedItems[1]);
                    intent.putExtra("isReverseLanguages", checkedItems[2]);
                    if (checkedItems[3])
                    {
                        intent.putExtra("isStarredOnly", checkedItems[3]);
                    }

                    startActivity(intent);
                } else {
                    Toast.makeText(VocabularyManagement.this, "Không có từ vựng nào để học.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Hủy", null);

        builder.create().show();
    }
    private void playPronunciation(String word) {
        textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_vocab, menu);
        return true;
    }
    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*"); // Chỉ định kiểu tệp mà bạn muốn chọn, ví dụ "text/csv" cho tệp CSV
        startActivityForResult(intent, 1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_add) {
            String topicId = getIntent().getStringExtra("topicId");
            Intent intent = new Intent(VocabularyManagement.this, AddWord.class);
            intent.putExtra("topicId", topicId);
            startActivity(intent);
            return true;
        }
        if (id == R.id.menu_import) {
            pickFile();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null) {
                Uri fileUri = data.getData();
                importFile(fileUri);
            }
        }
    }

    private void importFile(Uri fileUri) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = db.collection("words");
        try {
            InputStream inputStream = this.getContentResolver().openInputStream(fileUri);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String topicId = getIntent().getStringExtra("topicId");

            CSVReader csvReader = new CSVReader(bufferedReader);
            String[] nextRecord;
            List<Vocabulary> vocabularyList = new ArrayList<>();

            while ((nextRecord = csvReader.readNext()) != null) {
                // Điền dữ liệu vào documentData từ nextRecord theo cấu trúc của tệp CSV

                // Ví dụ, giả sử cột đầu tiên trong CSV là 'name', cột thứ hai là 'age'
                String word = nextRecord[0];
                String definition = nextRecord[1];
                Vocabulary vocabulary = new Vocabulary(word, definition, topicId);
                vocabularyList.add(vocabulary);
            }

            // Import dữ liệu vào Firestore
            for (Vocabulary vocabulary : vocabularyList) {
                collectionRef.add(vocabulary).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            db.collection("topics").document(topicId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    int wordCount = Integer.valueOf((String) documentSnapshot.get("wordCount"));
                                    wordCount += vocabularyList.size();
                                    db.collection("topics").document(topicId).update("wordCount", String.valueOf(wordCount));
                                }
                            });
                            Toast.makeText(VocabularyManagement.this, "Import thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(VocabularyManagement.this, "Import thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                vocabularyAdapter.add(vocabulary);
                vocabularyAdapter.notifyDataSetChanged();
            }

        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
            Toast.makeText(VocabularyManagement.this, "Lỗi đọc tệp", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(this, "Import thành công", Toast.LENGTH_SHORT).show();
    }

}
