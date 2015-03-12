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
import android.widget.ScrollView;
import android.widget.RelativeLayout;

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
    private TextView mPointsView;

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
        mPointsView = (TextView) findViewById(R.id.points_number);

        mSendButton = findViewById(R.id.send_button);
        mAcceptImage = findViewById(R.id.accept_image);
        mRejectImage = findViewById(R.id.reject_image);

        if (level == Exercise.Level.ESTIMATION) {
            View kButton = findViewById(R.id.k_button);
            View mButton = findViewById(R.id.m_button);
            kButton.setVisibility(View.VISIBLE);
            mButton.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams lp =
                (RelativeLayout.LayoutParams) mRightAnswersView.getLayoutParams();
            lp.addRule(RelativeLayout.BELOW, kButton.getId());
            lp.topMargin = 10;
            mRightAnswersView.setLayoutParams(lp);
        }

        animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(),
                                                   R.anim.fade_out);
        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(),
                                                  R.anim.fade_in);
        animFadeOut.setDuration(800);
        animFadeIn.setDuration(500);

        mGameTypeView.setText(level.toString() + "\n" + mode.toString());

        updateRightWrongPoints();

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
                gameOver();
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
        if (answer.length() > 10) {
            answer = answer.substring(0,10) + "...";
        }

        addToResults(rightAnswers + wrongAnswers, exercise.getPlainQuestion(),
                     exercise.formatedSolution(), answer,
                     exercise.givenAnswerCorrect() ?
                     R.color.acceptGreen : R.color.redTimer);

        updateRightWrongPoints();
        newExercise();
    }

    private void addToResults(int entryNo, String question, String answer,
                              String userAnswer, int colorId) {
        final RelativeLayout resultsLayout =
            (RelativeLayout) findViewById(R.id.rolling_results_relativelayout);

        TextView entry = new TextView(this);

        int entryId = entryNo;
        entry.setId(entryId);

        final RelativeLayout.LayoutParams lp_left =
            new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                            RelativeLayout.LayoutParams.WRAP_CONTENT);

        if (entryId == 1) {
            lp_left.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        } else {
            lp_left.addRule(RelativeLayout.BELOW, entryId - 1);
        }

        final RelativeLayout.LayoutParams lp_middle = new RelativeLayout.LayoutParams(lp_left);
        final RelativeLayout.LayoutParams lp_right = new RelativeLayout.LayoutParams(lp_left);

        if (entryId == 1) {
            lp_left.addRule(RelativeLayout.ALIGN_LEFT, R.id.fake_question_entry);
            lp_middle.addRule(RelativeLayout.ALIGN_RIGHT, R.id.fake_answer_entry);
                    lp_right.addRule(RelativeLayout.ALIGN_RIGHT, R.id.fake_user_answer_entry);
        } else {
            lp_left.addRule(RelativeLayout.ALIGN_LEFT, 1);
            lp_middle.addRule(RelativeLayout.ALIGN_RIGHT, 10001);
            lp_right.addRule(RelativeLayout.ALIGN_RIGHT, 20001);
        }

        entry.setLayoutParams(lp_left);
        entry.setText(question);
        resultsLayout.addView(entry);

        entry = new TextView(this);
        entry.setId(entryId + 10000);
        entry.setLayoutParams(lp_middle);
        entry.setText(answer);
        resultsLayout.addView(entry);

        if (userAnswer.isEmpty())
            return;

        entry = new TextView(this);
        entry.setId(entryId + 20000);
        entry.setLayoutParams(lp_right);

        entry.setTextColor(getResources().getColor(colorId));
        entry.setText(userAnswer);
        resultsLayout.addView(entry);

        final ScrollView resultsScrollView =
            (ScrollView) findViewById(R.id.rolling_results_scrollview);
        // scrolling to very bottom harder than expected:
        resultsScrollView.post(new Runnable() {
             @Override
             public void run() {
                 resultsScrollView.fullScroll(ScrollView.FOCUS_DOWN);
             }
        });
    }

    private void updateRightWrongPoints() {
        Log.d(MathActivity.TAG,
              String.format("%d - %d points", rightAnswers, wrongAnswers));

        mRightAnswersView.setText(Integer.toString(rightAnswers));
        mWrongAnswersView.setText(Integer.toString(wrongAnswers));
        int points = Math.max(0, rightAnswers - wrongAnswers);
        mPointsView.setText(Integer.toString(points));

        pluralifyView(R.id.right_answers_text, rightAnswers, R.string.right_answers_text);
        pluralifyView(R.id.wrong_answers_text, wrongAnswers, R.string.wrong_answers_text);
        pluralifyView(R.id.points_text, points, R.string.points_text);
    }

    private void pluralifyView(int id, int n, int singularId) {
        TextView view = (TextView) findViewById(id);
        String text = getResources().getString(singularId);

        if (n != 1) {
            text += "s";
        }
        view.setText(text);
    }

    public void insertSuffix(View view) {
        String suffix;
        switch (view.getId()) {
        case R.id.k_button:
            suffix = "K";
            break;
        case R.id.m_button:
            suffix = "M";
            break;
        default:
            throw new IllegalArgumentException();
        }
        CharSequence answer = mAnswerEditText.getText();
        if (answer.length() == 0) {
            answer = "1";
        } else if (Character.isLetter(answer.charAt(answer.length() - 1))) {
            answer = answer.subSequence(0, answer.length() - 1);
        }
        mAnswerEditText.setTextKeepState(answer.toString() + suffix);
    }

    void gameOver() {
        Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(),
                                                         R.anim.slide_up);
        final View newGameButton = findViewById(R.id.restart_button);
        addToResults(rightAnswers + wrongAnswers + 1, exercise.getPlainQuestion(),
                     exercise.formatedSolution(), "", 0);

        slideUp.setFillAfter(true);
        slideUp.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationEnd(Animation animation) {
                mExerciseView.setVisibility(View.GONE);
                mAnswerEditText.setVisibility(View.GONE);
                mSendButton.setVisibility(View.GONE);
            }
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationStart(Animation animation) {}
        });
        if (level == Exercise.Level.ESTIMATION) {
            View kButton = findViewById(R.id.k_button);
            View mButton = findViewById(R.id.m_button);
            kButton.startAnimation(slideUp);
            mButton.startAnimation(slideUp);
        }
        mExerciseView.startAnimation(slideUp);
        mAnswerEditText.startAnimation(slideUp);
        mSendButton.startAnimation(slideUp);
        mTimerView.startAnimation(animFadeOut);
        newGameButton.startAnimation(animFadeIn);
    }

    public void restart(View view) {
        mAnswerEditText.setText(""); // why do I need that?
        recreate();
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
