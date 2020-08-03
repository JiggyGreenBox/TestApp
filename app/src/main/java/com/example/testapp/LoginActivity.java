package com.example.testapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.HintRequest;

public class LoginActivity extends AppCompatActivity {

    private static int RESOLVE_HINT = 1001;
    private static String LOGIN_FRAGMENT = "LOGIN_FRAGMENT";
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // frag manager for routing
        fragmentManager = getSupportFragmentManager();
        LoginFragment loginFragment = new LoginFragment();
        fragmentManager.beginTransaction()
                .add(R.id.container_login, loginFragment, LOGIN_FRAGMENT)
                .commit();
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
                    LoginFragment loginFragment = (LoginFragment) fragmentManager.findFragmentByTag(LOGIN_FRAGMENT);
                    if (loginFragment != null) {
                        String ph_no = credential.getId().replace("+91", "");
                        loginFragment.setPhoneNumber(ph_no);
                    }
                }
            }
        }
    }


    public void loginSuccess() {
        Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
        LoginActivity.this.startActivity(myIntent);
        finish();
    }
}