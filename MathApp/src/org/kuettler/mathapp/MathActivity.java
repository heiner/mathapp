package org.kuettler.mathapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.SpinnerAdapter;
import android.widget.ArrayAdapter;

import android.util.Log;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;

public class MathActivity extends ActionBarActivity
{
    static final String TAG = "MathApp";
    public final static String LEVEL = "org.kuettler.mathapp.LEVEL";
    public final static String OPERATION = "org.kuettler.mathapp.OPERATION";

    private Intent intent;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        intent = new Intent(this, GameActivity.class);

        // Default values
        intent.putExtra(LEVEL, R.id.radio_easy);
        intent.putExtra(OPERATION, R.id.radio_random);

        setButtonText();

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

    public void setLevel(View view) {
        intent.putExtra(LEVEL, view.getId());
        setButtonText();
    }

    public void setOperation(View view) {
        //Log.d(TAG, "setOperation " + view.getId());
        intent.putExtra(OPERATION, view.getId());
        setButtonText();
    }

    public void startGame(View view) {
        //if (intent.hasExtra(LEVEL) && intent.hasExtra(OPERATION)) { }
        startActivity(intent);
    }

    private void setButtonText() {
        TextView button = (TextView) findViewById(R.id.button_start_game);
        if (intent.getIntExtra(OPERATION, R.id.radio_random) == R.id.radio_random) {
            button.setText("Start new random game");
        } else {
            button.setText(R.string.button_start_game);
        }
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
        case R.id.action_search:
            //openSearch();
            return true;
        case R.id.action_settings:
            //openSettings();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
