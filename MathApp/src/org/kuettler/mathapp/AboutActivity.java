package org.kuettler.mathapp;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.text.method.LinkMovementMethod;

public class AboutActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        TextView hattip
            = (TextView) findViewById(R.id.about_preplounge_hattip_textview);
        hattip.setMovementMethod(LinkMovementMethod.getInstance());
        TextView github
            = (TextView) findViewById(R.id.about_github_link_textview);
        github.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void copyBitcoinAddress(View view) {
        ClipboardManager cm
            = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setText(getResources().getString(R.string.bitcoin_address));
        Toast toast =
            Toast.makeText(getApplicationContext(),
                           getResources().getString(R.string.about_copied_bitcoin_address),
                           Toast.LENGTH_SHORT);
        toast.show();
    }
}
