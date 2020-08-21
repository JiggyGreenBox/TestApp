package com.example.testapp;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
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

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.paytm.pgsdk.PaytmUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements MySMSBroadcastReceiver.otpCallBack {

    private static int RESOLVE_HINT = 1001;
    private static int PaytmActivityRequestCode = 201;
    private FragmentManager fragmentManager;
    private static String PHONE_NUM_FRAGMENT = "PHONE_NUM_FRAGMENT";
    private static String PAYMENT_FRAGMENT = "PAYMENT_FRAGMENT";
    private static String SPLASH_FRAGMENT = "SPLASH_FRAGMENT";
    private static String HOME_FRAGMENT = "HOME_FRAGMENT";
    private static String LOGIN_FRAGMENT = "LOGIN_FRAGMENT";
    private static String HISTORY_FRAGMENT = "HISTORY_FRAGMENT";
    private static String PENDING_FRAGMENT = "PENDING_FRAGMENT";
    private static String LOGO_FRAGMENT = "LOGO_FRAGMENT";


    private static String url_test = "https://fuelmaster.greenboxinnovations.in/api/cars/1/11";

    // Declare the cb interface static in your activity
    private static MySMSBroadcastReceiver.otpCallBack otpCallBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // createNotificationChannel
        createNotificationChannel();

        // register receiver for token update
        registerReceiver(myReceiver, new IntentFilter(MyFirebaseMessagingService.UPDATE_FCM_TOKEN));

        // frag manager for routing
        fragmentManager = getSupportFragmentManager();


        // check shared prefs
        routeSharedPrefs();

        fragmentManager.beginTransaction()
                .add(R.id.fl_pending, new LogoFragment(), LOGO_FRAGMENT)
                .commit();

//        fragmentManager.beginTransaction()
//                .add(R.id.fl_pending, new PendingSingle(), PENDING_FRAGMENT)
//                .commit();

        fragmentManager.beginTransaction()
                .add(R.id.fl_pay, new PaymentFragment(), PAYMENT_FRAGMENT)
                .commit();


        fragmentManager.beginTransaction()
                .add(R.id.fl_history, new HistoryFragment(), HISTORY_FRAGMENT)
                .commit();


        //======== OTP STUFF ===============================================================
        // run to get app hash for message
        // ArrayList<String> arrayList = new AppSignatureHelper(this).getAppSignatures();
        // Log.e("hash", "" + arrayList);
        // otp callback interface
        otpCallBack = this;
        MySMSBroadcastReceiver.registerCallback(otpCallBack);
        //=======================================================================


    }


    public void loadPendingQRLarge(String amount, String fuel_type, String trans_qr) {

        PendingSingleFragment pendingSingleFragment = PendingSingleFragment.newInstance(amount, fuel_type, trans_qr);

        fragmentManager.beginTransaction()
                .add(R.id.fl_pending, pendingSingleFragment, PENDING_FRAGMENT)
                .commit();
    }

    private void routeSharedPrefs() {
        SharedPreferences loginPreferences = getApplicationContext().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);

        Log.e("fb_changed", "fb_changed");
        String fb = MyFirebaseMessagingService.getToken(this);
        Log.e("fb_changed", fb);


        if (loginPreferences.contains("auth")) {
            Log.e("routeSharedPrefs", "contains");

            // check auth validity
            long auth_time = loginPreferences.getLong("auth_timestamp", 0);
            if (auth_time != 0) {
                long now = System.currentTimeMillis();
                long validTill = auth_time + AppConstants.AUTH_VALID_TIME;

                // expired
                if (now > validTill) {
                    Log.e("TAGME", "auth expired");
                    String ref = loginPreferences.getString("ref", "");
                    if (ref != null && !ref.equals("")) {
                        Log.e("ref", ref);

                        verifyRef(ref);
                    }
                }
                // valid
                else {
                    Log.e("routeSharedPrefs", "auth valid");
                    String auth = loginPreferences.getString("auth", "");
                    Log.e("auth", auth);
                    getCarsAndPending(auth);
                }
            }

        } else {
            // auth not found go to login
            Log.e("routeSharedPrefs", "auth not found");
            loadLoginActivity();
        }
    }

    private void verifyRef(String ref) {

        // get firebase id
        String fb_token = MyFirebaseMessagingService.getToken(getApplicationContext());

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("ref", ref);
        params.put("fb_token", fb_token);
        JSONObject jsonObject = new JSONObject(params);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(AppConstants.REF_VERIFY_URL, jsonObject,
                new Response.Listener<JSONObject>() {
                    @SuppressLint("ApplySharedPref")
                    @Override
                    public void onResponse(JSONObject response) {


                        Log.e("REF_VERIFY_URL", "" + response);
                        try {
                            // get updated strings
                            String auth = response.getString("auth");

                            // get cars
                            JSONArray cars = response.getJSONArray("cars");
                            updateCarsPaymentFragment(cars);

                            // check shared prefs
                            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            long timestamp = System.currentTimeMillis();


                            editor.putString("auth", auth);
                            editor.putLong("auth_timestamp", timestamp);
                            editor.commit();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Handle Errors here
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null && networkResponse.statusCode == 409) {
                    // HTTP Status Code: 409 Client error
                    try {
                        String jsonString = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers));
                        JSONObject obj = new JSONObject(jsonString);
                        String message = obj.getString("message");
                        if (message.equals("Token Invalid")) {

                            // erase shared prefs
                            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.clear();
                            editor.apply();

                            loadLoginActivity();
                            finish();
                        }
                        Log.e("NetworkResponse", message);
                        //Snackbar.make(layout, message, Snackbar.LENGTH_SHORT).show();
                    } catch (UnsupportedEncodingException | JSONException e) {
                        e.printStackTrace();
                    }
                }

                Log.e("REF_VERIFY_URL", error.toString());
            }
        });

        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }


    private void getCarsAndPending(String auth) {

        String url = String.format(AppConstants.CARS_PENDING_URL + "?token=%1$s",
                auth);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                Log.e("cars_pending", response.toString());

                try {
                    JSONArray cars = response.getJSONArray("cars");
                    updateCarsPaymentFragment(cars);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("cars_pending", "Error: " + error.getMessage());

            }
        }) {

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                //headers.put("Content-Type", "application/json");
                headers.put("key", "Value");
                return headers;
            }
        };


        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
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
                    LoginFragment loginFragment = (LoginFragment) fragmentManager.findFragmentByTag(LOGIN_FRAGMENT);
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
        LoginFragment loginFragment = (LoginFragment) fragmentManager.findFragmentByTag(LOGIN_FRAGMENT);
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


    // update cars in payment fragment
    private void updateCarsPaymentFragment(JSONArray cars) {
        if (fragmentManager != null) {

            if (cars != null && cars.length() > 0) {
                PaymentFragment paymentFragment = (PaymentFragment) fragmentManager.findFragmentByTag(PAYMENT_FRAGMENT);
                assert paymentFragment != null;
                paymentFragment.updateCars(cars);
            }
        }

    }


    public void loadLoginActivity() {

        Intent myIntent = new Intent(MainActivity.this, LoginActivity.class);
        MainActivity.this.startActivity(myIntent);
        finish();

    }

    public void loadHomeFragment(String cars, String pending) {

        HomeFragment homeFragment = new HomeFragment();
        fragmentManager.beginTransaction().replace(R.id.fragment_container_view_tag, homeFragment, HOME_FRAGMENT)
                .commit();
    }


    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("update fcm token", "update");
            // TODO remove manifest false cache, and make route to update FCM {auth,ref, token}
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myReceiver);
    }

    // required for notifications to work properly
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = AppConstants.channel_name;
            String description = AppConstants.description;
            //int importance = NotificationManager.IMPORTANCE_DEFAULT;
            // heads up attempt
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(AppConstants.CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


}