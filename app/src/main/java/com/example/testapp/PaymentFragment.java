package com.example.testapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;


public class PaymentFragment extends Fragment {


    EditText et_amount;
    Button btn_pay, btn_add_100, btn_add_500, btn_add_1000;
    boolean pay_btn_clicked;

    public PaymentFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // TODO
        // switch
        // fuel checkbox
        // disable car switch
        // multi-car spinner

        pay_btn_clicked = false;

        // xml init
        et_amount = view.findViewById(R.id.editTextAmount);
        btn_pay = view.findViewById(R.id.pay_button);
        btn_add_100 = view.findViewById(R.id.btn_add_100);
        btn_add_500 = view.findViewById(R.id.btn_add_500);
        btn_add_1000 = view.findViewById(R.id.btn_add_1000);


        btn_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.e("payment button", "pressed");

                Activity act = getActivity();
                if (act != null) {
                    if (isAmountValid()) {
                        Log.e("payment button", "proceed");
                        String amount = et_amount.getText().toString().trim();
                        requestPendingTransaction(amount, "petrol", "");
                    } else {
                        Log.e("payment button", "invalid");
                    }
                }
//                ((MainActivity) act).invokePaytm();
//                ((MainActivity) act).loadPendingQR();
            }
        });

        btn_add_100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addAmount(100);
            }
        });

        btn_add_500.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addAmount(500);
            }
        });

        btn_add_1000.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addAmount(1000);
            }
        });
    }


    private boolean isAmountValid() {
        String amount = et_amount.getText().toString();
        // empty edit-text
        if (amount.isEmpty()) {
            Log.e("isAmountValid", "invalid");
            return false;
        }
        try {
            int intAmount = Integer.parseInt(et_amount.getText().toString());
            if (intAmount < 0) {
                Log.e("isAmountValid", "less than zero");
                return false;
            }
        } catch (Exception e) {
            Log.e("isAmountValid", e.toString());
            return false;
        }
        // amount is valid
        return true;
    }

    private void addAmount(int intAmount) {
        String stringAmount = et_amount.getText().toString();

        if (stringAmount.isEmpty()) {
            et_amount.setText(String.valueOf(intAmount));
        } else {
            // not empty
            try {
                int curAmount = Integer.parseInt(stringAmount);
                et_amount.setText(String.valueOf(curAmount + intAmount));
            } catch (Exception e) {
                Log.e("addAmount", e.toString());
            }
        }
    }


    private void requestPendingTransaction(final String amount, final String fuel_type, String car_id) {

        if (!pay_btn_clicked) {
            pay_btn_clicked = true;

            Activity act = getActivity();
            if (act != null) {
                SharedPreferences loginPreferences = act.getApplicationContext().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
                String auth = loginPreferences.getString("auth", "");
                String url = String.format(AppConstants.NEW_TRANSACTION_URL + "?token=%1$s",
                        auth);
                Log.e("tokenURL", url);


                // Initialize a new JsonObjectRequest instance
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("amount", amount);
                params.put("fuel_type", fuel_type);
                params.put("car_id", car_id);
                JSONObject jsonObject = new JSONObject(params);
                Log.e("post param", jsonObject.toString());

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, jsonObject,
                        new Response.Listener<JSONObject>() {
                            @SuppressLint("ApplySharedPref")
                            @Override
                            public void onResponse(JSONObject response) {

                                pay_btn_clicked = false;

                                Log.e("NEW_TRANS_URL", "" + response);
                                try {
                                    //Do stuff here
                                    String transaction_status = response.getString("pending_transaction");

                                    if (transaction_status.equals("success")) {
                                        String trans_qr = response.getString("trans_qr");
                                        boolean hasCarQR = response.getBoolean("hasCarQR");

                                        // check shared prefs
                                        Activity act = getActivity();
                                        if (act != null) {

                                            if (!hasCarQR) {
//                                                ((MainActivity) act).loadHomeFragment(cars, pending);
                                                Log.e("NEW_TRANS_URL", "load pending single QR");

                                                ((MainActivity) act).loadPendingQRLarge(amount, fuel_type, trans_qr);

                                            }
                                            // load list here
                                        }
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
//                            Snackbar.make(layout, message, Snackbar.LENGTH_SHORT).show();
                            } catch (UnsupportedEncodingException | JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        Log.e("NEW_TRANS_URL", error.toString());
                        pay_btn_clicked = false;
                    }
                });

                MySingleton.getInstance(getActivity()).addToRequestQueue(jsonObjectRequest);

            }
        }
    }
}