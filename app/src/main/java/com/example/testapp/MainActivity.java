package com.example.testapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;


public class MainActivity extends AppCompatActivity {

    private static int RESOLVE_HINT = 1001;

//    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


//        googleApiClient = new GoogleApiClient.Builder(this)
//                .addApi(Auth.CREDENTIALS_API)
//                .build();

//        requestHint();
//        requestEmailHint();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // Construct a request for phone numbers and show the picker
    public void requestHint() {
        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build();

//        PendingIntent intent = Auth.CredentialsApi.getHintPickerIntent(
//                googleApiClient, hintRequest);

        PendingIntent intent = Credentials.getClient(this).getHintPickerIntent(hintRequest);

        try {
            startIntentSenderForResult(intent.getIntentSender(),
                    RESOLVE_HINT, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }


    private void requestEmailHint() {
        HintRequest hintRequest = new HintRequest.Builder()
                .setEmailAddressIdentifierSupported(true)
                .build();

//        PendingIntent intent = Auth.CredentialsApi.getHintPickerIntent(
//                googleApiClient, hintRequest);

        PendingIntent intent = Credentials.getClient(this).getHintPickerIntent(hintRequest);

        try {
            startIntentSenderForResult(intent.getIntentSender(),
                    RESOLVE_HINT, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    // Obtain the phone number from the result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESOLVE_HINT) {
            if (resultCode == RESULT_OK) {
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);

                if (credential != null) {
//                    if (TextUtils.isEmpty(credential.getId())) {

                    Log.e("tag", "" + credential.getId());
                    // credential.getId();  <-- will need to process phone number string


                    // set phone number in edit text
                    Fragment navHostFragment = getSupportFragmentManager().getPrimaryNavigationFragment();
                    assert navHostFragment != null;
                    Fragment fragment = navHostFragment.getChildFragmentManager().getFragments().get(0);

                    ((FirstFragment) fragment).setPhoneNumber(credential.getId());
//                    }
                }
            }
        }
    }

}