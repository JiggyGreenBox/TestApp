package com.example.testapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;


public class LoginFragment extends Fragment {

    EditText et_phone_no;
    EditText et_OTP;
    TextView tv_timer;
    private boolean btn_click, btn_login_click;
    private ConstraintLayout layout;

    private Button btn_otp;
    private Button btn_login;

    long startTime = 0;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tv_timer = view.findViewById(R.id.textview_first);
        et_OTP = view.findViewById(R.id.editTextOTP);
        layout = view.findViewById(R.id.constraintLayout_phone_fragment);

        btn_otp = view.findViewById(R.id.button_first);
        btn_login = view.findViewById(R.id.button_login);

        et_phone_no = view.findViewById(R.id.editTextPhone);
        et_phone_no.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    Activity act = getActivity();
                    assert act != null;
                    ((MainActivity) act).requestHint();
                }
            }
        });


        btn_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String ph_no = et_phone_no.getText().toString();
                if (ph_no.length() != 10) {
                    Log.e("phone",
                            "Invalid Phone Number");
                    Snackbar snackbar = Snackbar
                            .make(layout, "Invalid Phone Number", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                } else {
                    Log.e("phone",
                            "proceed");
                    btn_click = true;
                    requestOTP(ph_no);
                }
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String ph_no = et_phone_no.getText().toString();
                String otp = et_OTP.getText().toString();

                if ((ph_no.length() == 10) && (otp.length() == 4)) {
                    // input is ok
                    btn_login_click = true;
                    verifyOTP(ph_no, otp);
                } else {
                    // input is wrong
                    Snackbar snackbar = Snackbar
                            .make(layout, "Check Phone Number and OTP", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }
            }
        });
    }

    public void setPhoneNumber(String phoneNumber) {
        et_phone_no.setText(phoneNumber);
    }

    public void showEditTextOTP() {
        /*
         toggle the necessary views
         start countdown timer for OTP validity
         start SMS listener
         */

        // views
        et_OTP.setVisibility(View.VISIBLE);
        tv_timer.setVisibility(View.VISIBLE);
        btn_otp.setVisibility(View.GONE);
        btn_login.setVisibility(View.VISIBLE);

        // timer
        startTime = System.currentTimeMillis() + AppConstants.OTP_VALID_TIME;
        timerHandler.postDelayed(timerRunnable, 0);

        // sms listener
        Activity act = getActivity();
        if (act != null) {
            ((MainActivity) act).startSMSListener();
        }
    }

    public void setOTP(String otp) {
        et_OTP.setText(otp);
    }


    private void requestOTP(String ph_no) {

        if (btn_click) {
            btn_click = false;

            // Initialize a new JsonObjectRequest instance
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("ph_no", ph_no);
            JSONObject jsonObject = new JSONObject(params);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(AppConstants.OTP_REQUEST_URL, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            btn_click = true;

                            Log.e("OTP_REQUEST_URL", "" + response);
                            showEditTextOTP();
//                        try {
//                            //Do stuff here
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
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
                            Log.e("NetworkResponse", message);
                            Snackbar.make(layout, message, Snackbar.LENGTH_SHORT).show();
                        } catch (UnsupportedEncodingException | JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    Log.e("OTP_REQUEST_URL", error.toString());
                    btn_click = true;
                }
            });

            MySingleton.getInstance(getActivity()).addToRequestQueue(jsonObjectRequest);
        }
    }


    private void verifyOTP(String ph_no, String otp) {
        if (btn_login_click) {
            btn_login_click = false;

            // Initialize a new JsonObjectRequest instance
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("ph_no", ph_no);
            params.put("otp", otp);
            JSONObject jsonObject = new JSONObject(params);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(AppConstants.OTP_VERIFY_URL, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @SuppressLint("ApplySharedPref")
                        @Override
                        public void onResponse(JSONObject response) {

                            btn_login_click = true;

                            Log.e("OTP_VERIFY_URL", "" + response);
                            try {
                                //Do stuff here


                                String auth = response.getString("auth");
                                String ref = response.getString("ref");

                                // check shared prefs
                                Activity act = getActivity();
                                if (act != null) {
                                    SharedPreferences sharedPreferences = act.getApplicationContext().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);

                                    SharedPreferences.Editor editor = sharedPreferences.edit();

                                    long timestamp = System.currentTimeMillis();

                                    editor.putString("auth", auth);
                                    editor.putLong("auth_timestamp", timestamp);
                                    editor.putString("ref", ref);
                                    editor.commit();

                                    ((MainActivity) act).loadHomeFragment();
                                }


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
                            Log.e("NetworkResponse", message);
                            Snackbar.make(layout, message, Snackbar.LENGTH_SHORT).show();
                        } catch (UnsupportedEncodingException | JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    Log.e("OTP_VERIFY_URL", error.toString());
                    btn_login_click = true;
                }
            });

            MySingleton.getInstance(getActivity()).addToRequestQueue(jsonObjectRequest);

        }
    }


    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
//            long millis = System.currentTimeMillis() - startTime;
            long millis = startTime - System.currentTimeMillis();
            if (millis > 0) {
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                tv_timer.setText("OTP valid for " + String.format("%d:%02d", minutes, seconds));
                timerHandler.postDelayed(this, 500);
            } else {
                tv_timer.setText("Request a new OTP");
                timerHandler.removeCallbacks(this);

                btn_login.setVisibility(View.GONE);
                btn_otp.setVisibility(View.VISIBLE);
                et_OTP.setText("");
                et_OTP.setVisibility(View.GONE);
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();

        // resume handler if started and not elapsed
        if (startTime > 0) {
            long millis = startTime - System.currentTimeMillis();
            if (millis > 0) {
                timerHandler.postDelayed(timerRunnable, 0);
            } else {
                tv_timer.setText("Request a new OTP");

                btn_login.setVisibility(View.GONE);
                btn_otp.setVisibility(View.VISIBLE);
                et_OTP.setVisibility(View.GONE);
            }
        }

    }


}