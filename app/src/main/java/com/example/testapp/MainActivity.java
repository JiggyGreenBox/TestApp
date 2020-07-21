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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements MySMSBroadcastReceiver.otpCallBack {

    private static int RESOLVE_HINT = 1001;
    private FragmentManager fragmentManager;
    private static String PHONE_NUM_FRAGMENT = "PHONE_NUM_FRAGMENT";

    //Declare the cb interface static in your activity
    private static MySMSBroadcastReceiver.otpCallBack otpCallBack;

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


//        requestHint();
//        requestEmailHint();


        // run to get app hash for message
//        ArrayList<String> arrayList = new AppSignatureHelper(this).getAppSignatures();
//        Log.e("hash", "" + arrayList);

        // otp callback interface
        otpCallBack = this;
        MySMSBroadcastReceiver.registerCallback(otpCallBack);


        // check if user is signed in
        // else show phone number input
        fragmentManager = getSupportFragmentManager();

        FirstFragment firstFragment = new FirstFragment();
        fragmentManager.beginTransaction()
                .add(R.id.fragment_container_view_tag, firstFragment, PHONE_NUM_FRAGMENT)
                .commit();

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

        // noinspection SimplifiableIfStatement
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
                    Log.e("tag", "" + credential.getId());

                    // set phone number in edit text
                    FirstFragment firstFragment = (FirstFragment) fragmentManager.findFragmentByTag(PHONE_NUM_FRAGMENT);
                    if (firstFragment != null) {
                        firstFragment.setPhoneNumber(credential.getId());
                    }
                }
            }
        }
    }


    public void getSMS() {
        // Get an instance of SmsRetrieverClient, used to start listening for a matching
        // SMS message.
        SmsRetrieverClient client = SmsRetriever.getClient(this /* context */);

        // Starts SmsRetriever, which waits for ONE matching SMS message until timeout
        // (5 minutes). The matching SMS message will be sent via a Broadcast Intent with
        // action SmsRetriever#SMS_RETRIEVED_ACTION.
        Task<Void> task = client.startSmsRetriever();

        // Listen for success/failure of the start Task. If in a background thread, this
        // can be made blocking using Tasks.await(task, [timeout]);
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Successfully started retriever, expect broadcast intent
                // ...
                Log.e("getSMS", "success");
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed to start retriever, inspect Exception for more details
                // ...
                Log.e("getSMS", "onFailure");
            }
        });
    }


    @Override
    public void otpRead(String otp) {
        FirstFragment firstFragment = (FirstFragment) fragmentManager.findFragmentByTag(PHONE_NUM_FRAGMENT);
        if (firstFragment != null) {
            firstFragment.setOTP(otp);
        }
    }
}