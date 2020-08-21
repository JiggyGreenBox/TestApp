package com.example.testapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import static android.view.View.NO_ID;


public class PaymentFragment extends Fragment {


    EditText et_amount;
    MaterialButton btn_pay, btn_add_100, btn_add_500, btn_add_1000, btn_radio_diesel, btn_radio_petrol;
    MaterialButtonToggleGroup radio_fuel_type;

    AutoCompleteTextView editTextFilledExposedDropdown;


    boolean pay_btn_clicked;

    private final String phone_qr = "Use Phone QR";

    private final String CAR_NOT_SELECTED = "CAR_NOT_SELECTED";
    private final String FUEL_NOT_SELECTED = "FUEL_NOT_SELECTED";

    JSONArray cars;

    Context ctx;

    private RelativeLayout rl_select_cars;

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
        rl_select_cars = view.findViewById(R.id.rl_select_cars);

        radio_fuel_type = view.findViewById(R.id.radio_fuel_type);
        btn_radio_diesel = view.findViewById(R.id.btn_radio_diesel);
        btn_radio_petrol = view.findViewById(R.id.btn_radio_petrol);

        editTextFilledExposedDropdown = view.findViewById(R.id.filled_exposed_dropdown);

        btn_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Activity act = getActivity();
                if (act != null) {
                    if (isAmountValid()) {

                        String amount = et_amount.getText().toString().trim();

                        // if cars present
                        // make sure some car/ phone qr is selected
                        String car_id = getCarID();
                        if (car_id.equals(CAR_NOT_SELECTED)) {
                            Log.e("car", "please select car");
                        }
                        // car id is valid, or phone qr is selected
                        else {
                            // check fuel type here
                            String fuel_type = getSelectedFuelType();
                            if (fuel_type.equals(FUEL_NOT_SELECTED)) {
                                Log.e("fuel", "fuel not selected");
                            }
                            // fuel type is valid
                            else {
                                Log.e("payment button", "proceed");
                                requestPendingTransaction(amount, fuel_type, car_id);
                            }
                        }
                    } else {
                        Log.e("payment button", "invalid");
                    }
                }
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
                //selectCar();
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


    public void updateCars(JSONArray sentCars) {
        cars = sentCars;
        Log.e("payment frag", cars.toString());

        // empty check
        int length = cars.length();

        // make sure cars exist
        if (length != 0) {

            final String[] CARS = getCarStringArray(cars);
//            for (int jj = 0; jj < CARS.length; jj++) {
//                Log.e("car", CARS[jj]);
//            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(ctx, R.layout.car_select_single, CARS);
            View view = getView();
            assert view != null;
            AutoCompleteTextView editTextFilledExposedDropdown = view.findViewById(R.id.filled_exposed_dropdown);
            editTextFilledExposedDropdown.setAdapter(adapter);
            editTextFilledExposedDropdown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    //Log.e("selected", CARS[i]);
                    String fuel_type = getFuelTypeByPlateNo(CARS[i]);

                    btn_radio_petrol.setEnabled(true);
                    btn_radio_diesel.setEnabled(true);

                    if (fuel_type.equals("diesel")) {
                        radio_fuel_type.check(R.id.btn_radio_diesel);
                        btn_radio_petrol.setEnabled(false);
                        btn_radio_diesel.setEnabled(false);
                    } else if (fuel_type.equals("petrol")) {
                        radio_fuel_type.check(R.id.btn_radio_petrol);
                        btn_radio_petrol.setEnabled(false);
                        btn_radio_diesel.setEnabled(false);
                    } else if (fuel_type.equals("")) {
                        radio_fuel_type.clearChecked();
                    }
                }
            });

            // 1 car
            if (length == 1) {
                rl_select_cars.setVisibility(View.VISIBLE);
                editTextFilledExposedDropdown.setText(CARS[0], false);
                String fuel_type = getFuelTypeByPlateNo(CARS[0]);
                if (fuel_type.equals("diesel")) {
                    radio_fuel_type.check(R.id.btn_radio_diesel);
                    btn_radio_petrol.setEnabled(false);
                    btn_radio_diesel.setEnabled(false);
                } else if (fuel_type.equals("petrol")) {
                    radio_fuel_type.check(R.id.btn_radio_petrol);
                    btn_radio_petrol.setEnabled(false);
                    btn_radio_diesel.setEnabled(false);
                }
            }
            // 1+ car
            else if (length > 1) {
                rl_select_cars.setVisibility(View.VISIBLE);
            }
        }
    }

    private String[] getCarStringArray(JSONArray sentCars) {

        // create array with "USE PHONE QR" option
        int length = sentCars.length();
        String[] ret = new String[(length + 1)];

        try {

            for (int i = 0; i < length; i++) {

                JSONObject object = sentCars.getJSONObject(i);

                ret[i] = object.getString("car_no_plate");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ret[length] = phone_qr;

        return ret;
    }

    private String getFuelTypeByPlateNo(String plate_no) {

        String fuel_type = "";

        if (plate_no.equals(phone_qr)) {
            return fuel_type;
        }

        try {
            for (int i = 0; i < cars.length(); i++) {

                JSONObject object = cars.getJSONObject(i);
                if (object.getString("car_no_plate").equals(plate_no)) {
                    return object.getString("car_fuel_type");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return fuel_type;
    }

    private String getSelectedFuelType() {
        int checked = radio_fuel_type.getCheckedButtonId();
        if (checked == NO_ID) {
            return FUEL_NOT_SELECTED;
        } else if (checked == R.id.btn_radio_diesel) {
            return "diesel";
        } else if (checked == R.id.btn_radio_petrol) {
            return "petrol";
        }
        return FUEL_NOT_SELECTED;
    }

    private String getCarID() {

        // no cars
        if (cars == null) {
            return "";
        }

        // no cars
        if (cars.length() == 0) {
            return "";
        }

        // user has cars
        if (editTextFilledExposedDropdown.getVisibility() == View.VISIBLE) {

            // get car selected
            String plate_no = editTextFilledExposedDropdown.getText().toString().trim();

            // user has cars but none were selected
            if (plate_no.equals("")) {
                return CAR_NOT_SELECTED;
            }
            // user has selected
            // could be phone qr option
            else {

                // return blank for phone qr
                if (plate_no.equals(phone_qr)) {
                    return "";
                }
                // not blank, or phone option
                // retrieve the car id from json array
                try {
                    for (int i = 0; i < cars.length(); i++) {
                        JSONObject object = cars.getJSONObject(i);
                        if (object.getString("car_no_plate").equals(plate_no)) {
                            return object.getString("car_id");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

    private void selectCar() {

        CharSequence[] choices = {"Choice1", "Choice2", "Choice3"};


        if (ctx != null) {
            new MaterialAlertDialogBuilder(ctx)
                    .setTitle("Select Car")
                    .setPositiveButton("Ok", null)
                    .setNeutralButton("Cancel", null)
                    .setSingleChoiceItems(choices, -1, null)
                    .show();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        ctx = this.getActivity();
    }
}