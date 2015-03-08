package org.kuettler.mathapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SpinnerAdapter;
import android.widget.ArrayAdapter;

import android.util.Log;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;

public class MathActivity extends ActionBarActivity
{
    static final String TAG = "MathApp";
    public final static String EXTRA_MESSAGE = "org.kuettler.mathapp.MESSAGE";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        SpinnerAdapter mSpinnerAdapter =
            ArrayAdapter.createFromResource(this, R.array.action_list,
                                            android.R.layout.simple_spinner_dropdown_item);
        ActionBar.OnNavigationListener mOnNavigationListener =
            new ActionBar.OnNavigationListener() {
                @Override
                public boolean onNavigationItemSelected(int position, long itemId) {
                    return true;
                }
            };
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setListNavigationCallbacks(mSpinnerAdapter,
                                                         mOnNavigationListener);
    }

    public void startGame(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, GameActivity.class);
        //Log.d(TAG, Integer.toString(R.id.edit_message));
        //EditText editText = (EditText) findViewById(R.id.edit_message);
        //if (editText == null) {
        //    Log.d(TAG, "findViewById(R.id.edit_message) returned null");
        //}
        //String message = editText.getText().toString();
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
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
