package org.kuettler.mathapp;

import android.os.Bundle;
import android.content.Intent;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.app.Fragment;

import android.support.v7.app.ActionBarActivity;

public class GameActivity extends ActionBarActivity {
    private TextView mTimerView;
    private TextView mExerciseView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String message = intent.getStringExtra(MathActivity.EXTRA_MESSAGE);

        // Set the text view as the activity layout
        setContentView(R.layout.game);
        mTimerView = (TextView) findViewById(R.id.timer_textview);
        mExerciseView = (TextView) findViewById(R.id.exercise_textview);

        Exercise exercise = Exercise.Operation.newRandomExercise(Exercise.Level.EASY);
        mExerciseView.setText(exercise.question());
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
