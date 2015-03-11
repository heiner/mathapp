package org.kuettler.mathapp;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

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
import android.view.animation.Animation;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;

import android.util.Log;

import android.support.v7.app.ActionBarActivity;

public class GameActivity extends ActionBarActivity {
    private TextView mGameTypeView;
    private TextView mExtraTimeView;
    private TextView mTimerView;
    private TextView mExerciseView;
    private EditText mAnswerEditText;
    private CountDownTimer mCountDownTimer;
    private TextView mRightAnswersView;
    private TextView mWrongAnswersView;

    private View mSendButton;
    private View mAcceptImage;
    private View mRejectImage;

    private final List<Exercise> exercises = new LinkedList<Exercise>();
    private Exercise exercise;
    private int rightAnswers = 0;
    private int wrongAnswers = 0;
    private int msecondsLeft;

    private Exercise.Level level;
    private MathActivity.Mode mode;

    private Animation animFadeIn;
    private Animation animFadeOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        level = (Exercise.Level) intent.getSerializableExtra(Exercise.Level.TAG);
        mode = (MathActivity.Mode) intent.getSerializableExtra(MathActivity.Mode.TAG);

        // Set the text view as the activity layout
        setContentView(R.layout.game);

        mGameTypeView = (TextView) findViewById(R.id.gametype_textview);
        mExtraTimeView = (TextView) findViewById(R.id.extratime_textview);
        mTimerView = (TextView) findViewById(R.id.timer_textview);
        mExerciseView = (TextView) findViewById(R.id.exercise_textview);
        mAnswerEditText = (EditText) findViewById(R.id.answer_edittext);
        mRightAnswersView = (TextView) findViewById(R.id.right_answers_number);
        mWrongAnswersView = (TextView) findViewById(R.id.wrong_answers_number);

        mSendButton = findViewById(R.id.send_button);
        mAcceptImage = findViewById(R.id.accept_image);
        mRejectImage = findViewById(R.id.reject_image);

        animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(),
                                                   R.anim.fade_out);
        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(),
                                                  R.anim.fade_in);
        animFadeOut.setDuration(1000);
        animFadeIn.setDuration(500);

        mGameTypeView.setText(level.toString() + "\n" + mode.toString());

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

        newExercise();
        newCountDownTimer(60200);
    }

    protected CountDownTimer newCountDownTimer(long msec) {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        if (msec / 1000 > 10) {
            mTimerView.setTextColor(R.color.textColor);
        }
        mCountDownTimer = new CountDownTimer(msec, 1000) {
            @Override
            public void onTick(long msecUntilFinished) {
                msecondsLeft = (int)msecUntilFinished;
                mTimerView.setText(String.format("%d:%tS",
                                                 msecondsLeft / 1000 / 60,
                                                 msecUntilFinished));
                if (msecondsLeft / 1000 <= 10) {
                    mTimerView.setTextColor(getResources().getColor(R.color.redTimer));
                }
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
        mExerciseView.startAnimation(animFadeOut);
        exercise = mode.newExercise(level);
        mExerciseView.setText(exercise.question());
        exercises.add(exercise);
        mExerciseView.startAnimation(animFadeIn);
    }

    public void sendAnswer(View view) {
        String answer = mAnswerEditText.getText().toString();

        if (answer.isEmpty() || msecondsLeft == 0) {
            return;
        }

        mAnswerEditText.setText("");

        Animation imageFadeIn = new AlphaAnimation(0.0f, 1.0f);
        Animation imageFadeOut = new AlphaAnimation(1.0f, 0.0f);

        imageFadeIn.setDuration(1000);
        imageFadeOut.setDuration(1000);

        if (exercise.answer(answer)) {
            newCountDownTimer(msecondsLeft + 2040);
            ++rightAnswers;
            mExtraTimeView.setVisibility(View.VISIBLE);
            mExtraTimeView.startAnimation(imageFadeOut);
            mExtraTimeView.setVisibility(View.INVISIBLE);

            mSendButton.startAnimation(imageFadeOut);
            mAcceptImage.setVisibility(View.VISIBLE);
            mAcceptImage.startAnimation(imageFadeOut);
            mAcceptImage.setVisibility(View.INVISIBLE);
            mSendButton.startAnimation(imageFadeIn);
        } else {
            ++wrongAnswers;

            mSendButton.startAnimation(imageFadeOut);
            mRejectImage.setVisibility(View.VISIBLE);
            mRejectImage.startAnimation(imageFadeOut);
            mRejectImage.setVisibility(View.INVISIBLE);
            mSendButton.startAnimation(imageFadeIn);
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
