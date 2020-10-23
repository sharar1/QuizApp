package com.example.quizapp.UI.quiz;


import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.quizapp.R;
import com.example.quizapp.UI.result.ResultActivity;
import com.example.quizapp.models.Questions;

import java.util.ArrayList;
import java.util.List;

public class QuizActivity extends AppCompatActivity implements QuizAdapter.Listener {

    public static final String EXTRA_AMOUNT = "slider";
    public static final String EXTRA_CATEGORY = "category";
    public static final String EXTRA_DIFFICULTY = "difficulty";
    RecyclerView recyclerView;

    String difficulty;
    int category;
    int amountCount;
    private QuizViewModel viewModel;
    TextView category_text, quiz_amount;
    QuizAdapter adapter;
    ProgressBar progressBar;
    Button skip, finish_btn;
    ImageView onBack;
//    LottieAnimationView lottie;
    List<Questions> list = new ArrayList<>();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        viewModel = ViewModelProviders.of(this).get(QuizViewModel.class);
        initViews();
        getData();
        load();
        recyclerSets();

        viewModel.finishEvent.observe(this, aVoid -> finish());
        viewModel.openResultEvent.observe(this, integer ->
                ResultActivity.start(QuizActivity.this, integer)
        );

        skip.setOnClickListener(view -> viewModel.onSkipClick());
    }


    @SuppressLint("ClickableViewAccessibility")
    private void recyclerSets() {
        adapter = new QuizAdapter(list, this);
        recyclerView.setAdapter(adapter);
        RecyclerView.LayoutManager manager = new LinearLayoutManager
                (this, LinearLayoutManager.HORIZONTAL, false) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        recyclerView.setLayoutManager(manager);
        recyclerView.setOnTouchListener((view, motionEvent) -> true);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
    }


    private void initViews() {
        recyclerView = findViewById(R.id.quiz_recycler);
        category_text = findViewById(R.id.quiz_category_text);
        progressBar = findViewById(R.id.progress_bar);
        quiz_amount = findViewById(R.id.quiz_amount);
        skip = findViewById(R.id.quiz_skip);
        onBack = findViewById(R.id.quiz_back);
//        lottie = findViewById(R.id.lottie_load);
        finish_btn = findViewById(R.id.quiz_finish);
        finish_btn.setOnClickListener(view -> {
        });

    }

    private void getData() {
        if (getIntent() != null) {
            amountCount = getIntent().getIntExtra(EXTRA_AMOUNT, 1);
            category = getIntent().getIntExtra(EXTRA_CATEGORY, 0);
            difficulty = getIntent().getStringExtra(EXTRA_DIFFICULTY);
            if (amountCount == 0) {
                amountCount = 5;
            }
            if (category == 8) {
                category = 0;
            }
            if (difficulty != null) {
                if (difficulty.equals("Any Difficulty")) {
                    difficulty = null;
                } else {
                    difficulty = difficulty.toLowerCase();
                }
            }
            getQuestions();
        }
    }


    @SuppressLint("SetTextI18n")
    private void getPosition() {
        viewModel.currentQuestionPosition.observe(this, integer -> {
            if (integer != null) {
                quiz_amount.setText((integer + 1) + "/" + amountCount);
                new CountDownTimer(500, 500) {
                    @Override
                    public void onTick(long l) {

                    }

                    @Override
                    public void onFinish() {
                        recyclerView.smoothScrollToPosition(integer);
                    }
                }.start();
                progressBar.setProgress(integer + 1);
                progressBar.setMax(amountCount);
                category_text.setText(list.get(integer).getCategory());
                if (category_text.getText().equals("null")) {
                    category_text.setText("Any Category");
                }
                if (integer + 1 == list.size()) {
                    skip.setText("Finish");
                }
            } else {
                skip.setText("skip");
            }
        });

    }

    public static void start(Context context, int amountCount, int category, String difficulty) {
        Intent intent = new Intent(context, QuizActivity.class);
        intent.putExtra(EXTRA_AMOUNT, amountCount);
        intent.putExtra(EXTRA_CATEGORY, category);
        intent.putExtra(EXTRA_DIFFICULTY, difficulty);
        context.startActivity(intent);
    }

    private void getQuestions() {
        viewModel.init(amountCount, category, difficulty);
        viewModel.questions.observe(this, questions -> {
            list = questions;
            adapter.setQuestions(questions);
            getPosition();
        });
    }


    @Override
    public void onAnswerClick(int position, int selectAnswerPosition) {
        viewModel.onAnswerClick(position, selectAnswerPosition);
    }

    public void onBackPressed2(View view) {
        viewModel.onBackpessed();
    }

    public void load() {
        viewModel.loading.observe(this, aBoolean -> {
            if (aBoolean) {
//                lottie.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                quiz_amount.setVisibility(View.GONE);

            } else {
//                lottie.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                quiz_amount.setVisibility(View.VISIBLE);
            }
        });
    }
}