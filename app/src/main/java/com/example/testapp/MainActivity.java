package com.example.testapp;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.paytm.pgsdk.PaytmUtility;

import org.json.JSONArray;


public class MainActivity extends AppCompatActivity implements MySMSBroadcastReceiver.otpCallBack {

    private static int RESOLVE_HINT = 1001;
    private static int PaytmActivityRequestCode = 201;
    private FragmentManager fragmentManager;
    private static String PHONE_NUM_FRAGMENT = "PHONE_NUM_FRAGMENT";
    private static String PAYMENT_FRAGMENT = "PAYMENT_FRAGMENT";
    private static String SPLASH_FRAGMENT = "SPLASH_FRAGMENT";
    private static String HOME_FRAGMENT = "HOME_FRAGMENT";


    private static String url_test = "https://fuelmaster.greenboxinnovations.in/api/cars/1/11";

    // Declare the cb interface static in your activity
    private static MySMSBroadcastReceiver.otpCallBack otpCallBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        setTheme(R.style.Splash);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("GPS PRESENCE SYSTEM");
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        // check shared prefs


        // run to get app hash for message
//        ArrayList<String> arrayList = new AppSignatureHelper(this).getAppSignatures();
//        Log.e("hash", "" + arrayList);

        // otp callback interface
        otpCallBack = this;
        MySMSBroadcastReceiver.registerCallback(otpCallBack);


        // check if user is signed in
        // else show phone number input
        fragmentManager = getSupportFragmentManager();

        // load splash fragment
        SplashFragment splashFragment = new SplashFragment();
        fragmentManager.beginTransaction()
                .add(R.id.fragment_container_view_tag, splashFragment, SPLASH_FRAGMENT)
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
                    LoginFragment loginFragment = (LoginFragment) fragmentManager.findFragmentByTag(PHONE_NUM_FRAGMENT);
                    if (loginFragment != null) {
                        String ph_no = credential.getId().replace("+91", "");
                        loginFragment.setPhoneNumber(ph_no);
                    }
                }
            }
        }


        if (requestCode == PaytmActivityRequestCode && data != null) {
            Toast.makeText(this, data.getStringExtra("nativeSdkForMerchantMessage") + data.getStringExtra("response"), Toast.LENGTH_SHORT).show();
            Log.e("PAYTM RESPONSE", "" + data.getStringExtra("response"));
        }
    }


    public void startSMSListener() {
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
        LoginFragment loginFragment = (LoginFragment) fragmentManager.findFragmentByTag(PHONE_NUM_FRAGMENT);
        if (loginFragment != null) {
            loginFragment.setOTP(otp);
        }
    }


    // Check current Paytm app version
    private String getPaytmVersion(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            String PAYTM_APP_PACKAGE = "net.one97.paytm";
            PackageInfo pkgInfo = pm.getPackageInfo(PAYTM_APP_PACKAGE, PackageManager.GET_ACTIVITIES);
            return pkgInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            PaytmUtility.debugLog("Paytm app not installed");
        }
        return null;
    }


    private int versionCompare(String str1, String str2) {
        if (TextUtils.isEmpty(str1) || TextUtils.isEmpty(str2)) {
            return 1;
        }
        String[] vals1 = str1.split("\\.");
        String[] vals2 = str2.split("\\.");
        int i = 0;
        // set index to first non - equal ordinal or length of shortest version string
        while (i < vals1.length && i < vals2.length && vals1[i].equalsIgnoreCase(vals2[i])) {
            i++;
        }
        // compare first non - equal ordinal number
        if (i < vals1.length && i < vals2.length) {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff);
        }
        // the strings are equal or one string is a substring of the other
        // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
        return Integer.signum(vals1.length - vals2.length);
    }


    public void invokePaytm() {
        Log.e("TEST", "INVOKE PAYTM");
        if (versionCompare(getPaytmVersion(this), "8.6.0") < 0) {
            // Full screen App Invoke flow
//            Intent paytmIntent = new Intent();
//            Bundle bundle = new Bundle();gues
//            bundle.putDouble("nativeSdkForMerchantAmount", Amount);
//            bundle.putString("orderid", OrderID);
//            bundle.putString("txnToken", txnToken);
//            bundle.putString("mid", MID);
//            paytmIntent.setComponent(new ComponentName("net.one97.paytm", "net.one97.paytm.AJRJarvisSplash"));
//            paytmIntent.putExtra("paymentmode", 2); // You must have to pass hard coded 2 here, Else your transaction would not proceed.
//            paytmIntent.putExtra("bill", bundle);
//            startActivityForResult(paytmIntent, ActivityRequestCode);


            Log.e("TEST", "LESS THAN 8.6.0");
        } else {
            // New App Invoke flow
            Intent paytmIntent = new Intent();
            paytmIntent.setComponent(new ComponentName("net.one97.paytm", "net.one97.paytm.AJRRechargePaymentActivity"));
            paytmIntent.putExtra("paymentmode", 2);
            paytmIntent.putExtra("enable_paytm_invoke", true);
            paytmIntent.putExtra("paytm_invoke", true);
            paytmIntent.putExtra("price", "1"); //this is string amount
            paytmIntent.putExtra("nativeSdkEnabled", true);
            paytmIntent.putExtra("orderid", "ORDERID_987");
            paytmIntent.putExtra("txnToken", "f0445369ef63486ea16e4fa740f70eef1595691460508");
            paytmIntent.putExtra("mid", "UaOfbG68292644480647");
            this.startActivityForResult(paytmIntent, PaytmActivityRequestCode);

            Log.e("TEST", "MORE THAN 8.6.0");
        }
    }


    public void loadLoginFragment() {

        LoginFragment loginFragment = new LoginFragment();
        fragmentManager.beginTransaction().replace(R.id.fragment_container_view_tag, loginFragment, PHONE_NUM_FRAGMENT)
                .commit();
    }

    public void loadHomeFragment() {

        HomeFragment homeFragment = new HomeFragment();
        fragmentManager.beginTransaction().replace(R.id.fragment_container_view_tag, homeFragment, HOME_FRAGMENT)
                .commit();
    }


    public void saveTokens(String auth, String ref) {

        HomeFragment homeFragment = new HomeFragment();
        fragmentManager.beginTransaction().replace(R.id.fragment_container_view_tag, homeFragment, HOME_FRAGMENT)
                .commit();
    }


    private void testVolley() {
        // Initialize a new JsonArrayRequest instance
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url_test,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Do something with response
                        //mTextView.setText(response.toString());
                        Log.e("volley", "" + response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Do something when error occurred
                        Log.e("volley error", error.toString());
                    }
                }
        );

        MySingleton.getInstance(this.getApplicationContext()).addToRequestQueue(jsonArrayRequest);
    }

}