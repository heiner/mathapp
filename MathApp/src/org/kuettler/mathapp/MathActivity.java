package org.kuettler.mathapp;

import java.util.Map;
import java.util.HashMap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.SpinnerAdapter;
import android.widget.ArrayAdapter;

import android.util.Log;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;

public class MathActivity extends ActionBarActivity
{
    static final String TAG = "MathApp";

    private Intent intent;

    private Exercise.Level level = Exercise.Level.getDefault();
    private Mode mode = Mode.getDefault();

    public enum Mode {
        PLUS(R.id.radio_plus, "addition"),
        MINUS(R.id.radio_minus, "subtraction"),
        TIMES(R.id.radio_times, "multiplication"),
        DIVIDE(R.id.radio_divide, "division"),
        PERCENT_OF(R.id.radio_percent, "percentage"),
        RANDOM(R.id.radio_random, "random") {
            @Override
            public Exercise.Operation toOperation() {
                return Exercise.Operation.randomOperation();
            }
        };

        public final static String TAG =
            Mode.class.getCanonicalName();

        public static Mode getDefault() {
            return RANDOM;
        }

        private final int radioId;
        private final String modeName;
        Mode(int radioId, String modeName) {
            this.radioId = radioId;
            this.modeName = modeName;
        }

        public int getRadioId() {
            return radioId;
        }

        @Override
        public String toString() {
            return modeName;
        }

        private static final Map<Integer, Mode> idToMode
            = new HashMap<Integer, Mode>();
        static {
            for (Mode m : values()) {
                idToMode.put(m.getRadioId(), m);
            }
        }
        public static Mode fromRadioId(int radioId) {
            return idToMode.get(radioId);
        }

        public Exercise.Operation toOperation() {
            return Exercise.Operation.valueOf(name());
        }

        public Exercise newExercise(Exercise.Level level) {
            return toOperation().newExercise(level);
        }
    }

    private Animation animFadeIn;
    private Animation animFadeOut;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        intent = new Intent(this, GameActivity.class);

        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(),
                                                  R.anim.fade_in);
        animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(),
                                                   R.anim.fade_out);

        animFadeOut.setDuration(100);
        animFadeIn.setDuration(700);

        updateLevelAndMode();

        Stats.getInstance().load(getApplicationContext());
        try {
            Log.d(TAG, Stats.getInstance().toJSONObject().toString(2));
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }

        // SpinnerAdapter mSpinnerAdapter =
        //     ArrayAdapter.createFromResource(this, R.array.action_list,
        //                                     android.R.layout.simple_spinner_dropdown_item);
        // ActionBar.OnNavigationListener mOnNavigationListener =
        //     new ActionBar.OnNavigationListener() {
        //         @Override
        //         public boolean onNavigationItemSelected(int position, long itemId) {
        //             return true;
        //         }
        //     };
        // getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        // getSupportActionBar().setListNavigationCallbacks(mSpinnerAdapter,
        //                                                  mOnNavigationListener);
    }

    private static final Map<Integer, Exercise.Level> idsToLevels =
        new HashMap<Integer, Exercise.Level>();
    static {
        idsToLevels.put(R.id.radio_easy, Exercise.Level.EASY);
        idsToLevels.put(R.id.radio_medium, Exercise.Level.MEDIUM);
        idsToLevels.put(R.id.radio_hard, Exercise.Level.HARD);
        idsToLevels.put(R.id.radio_estimation, Exercise.Level.ESTIMATION);
    }
    public void setLevel(View view) {
        level = idsToLevels.get(view.getId());
        updateLevelAndMode();
    }

    public void setMode(View view) {
        mode = Mode.fromRadioId(view.getId());
        updateLevelAndMode();
    }

    public void startGame(View view) {
        intent.putExtra(Exercise.Level.TAG, level);
        intent.putExtra(Mode.TAG, mode);
        startActivity(intent);
        //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void updateLevelAndMode() {
        TextView button = (TextView) findViewById(R.id.button_start_game);
        String text = "Start new " + level.toString() + " " +
            mode.toString() + " game";

        if (!text.equals(button.getText())) {
            button.startAnimation(animFadeOut);
            button.setText(text);
            button.startAnimation(animFadeIn);
        }

        StatsView statsView = (StatsView) findViewById(R.id.statsview);
        statsView.setLevel(level);
        statsView.setMode(mode);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
        case R.id.action_about:
            Intent aboutIntent = new Intent(this, AboutActivity.class);
            startActivity(aboutIntent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
