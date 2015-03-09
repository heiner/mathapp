package org.kuettler.mathapp;

import java.util.List;
import java.util.LinkedList;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import android.util.Log;

import android.support.v7.app.ActionBarActivity;

public class GameActivity extends ActionBarActivity {
    private TextView mTimerView;
    private TextView mExerciseView;
    private EditText mAnswerEditText;
    private CountDownTimer mCountDownTimer;
    private TextView mRightAnswersView;
    private TextView mWrongAnswersView;

    private Exercise.Level level;
    private final List<Exercise> exercises = new LinkedList<Exercise>();
    private Exercise exercise;
    private int rightAnswers = 0;
    private int wrongAnswers = 0;
    private int msecondsLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String message = intent.getStringExtra(MathActivity.EXTRA_MESSAGE);

        // Set the text view as the activity layout
        setContentView(R.layout.game);

        mTimerView = (TextView) findViewById(R.id.timer_textview);
        mExerciseView = (TextView) findViewById(R.id.exercise_textview);
        mAnswerEditText = (EditText) findViewById(R.id.answer_edittext);
        mRightAnswersView = (TextView) findViewById(R.id.right_answers_number);
        mWrongAnswersView = (TextView) findViewById(R.id.wrong_answers_number);

        // Make mAnswerEditText's send call sendAnswer()
        mAnswerEditText.setOnEditorActionListener(
            new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    boolean handled = false;
                    if (actionId == EditorInfo.IME_ACTION_SEND) {
                        sendAnswer(v);
                        handled = true;
                    }
                    return handled;
                }
            });

        level = Exercise.Level.EASY;
        newExercise();
        newCountDownTimer(60200);
    }

    protected CountDownTimer newCountDownTimer(long msec) {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        mCountDownTimer = new CountDownTimer(msec, 1000) {
            @Override
            public void onTick(long msecUntilFinished) {
                msecondsLeft = (int)msecUntilFinished;
                mTimerView.setText(String.format("%d:%tS",
                                                 msecondsLeft / 1000 / 60,
                                                 msecUntilFinished));
                if (msecondsLeft / 1000 <= 10) {
                    mTimerView.setTextColor(Color.RED);
                }
                Log.d(MathActivity.TAG, Long.toString(msecUntilFinished));
            }

            @Override
            public void onFinish() {
                msecondsLeft = 0;
                mTimerView.setText("0:00");
            }
        };
        return mCountDownTimer.start();
    }

    protected void newExercise() {
        exercise = Exercise.Operation.newRandomExercise(level);
        mExerciseView.setText(exercise.question());
        exercises.add(exercise);
    }

    public void sendAnswer(View view) {
        String answer = mAnswerEditText.getText().toString();

        if (answer.isEmpty() || msecondsLeft == 0) {
            return;
        }

        mAnswerEditText.setText("");

        if (exercise.answer(answer)) {
            newCountDownTimer(msecondsLeft + 2040);
            ++rightAnswers;
        } else {
            ++wrongAnswers;
        }
        mRightAnswersView.setText(Integer.toString(rightAnswers));
        mWrongAnswersView.setText(Integer.toString(wrongAnswers));

        Log.d(MathActivity.TAG,
              String.format("%d - %d points", rightAnswers, wrongAnswers));
        newExercise();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.

    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() { }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                  Bundle savedInstanceState) {
              View rootView = inflater.inflate(R.layout.fragment_display_message,
                      container, false);
              return rootView;
        }
    }
     */
}