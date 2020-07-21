package com.example.testapp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class FirstFragment extends Fragment {

    EditText et_phone_no;
    EditText et_OTP;

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

        et_OTP = view.findViewById(R.id.editTextOTP);

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

        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                NavHostFragment.findNavController(FirstFragment.this)
//                        .navigate(R.id.action_FirstFragment_to_SecondFragment);

                showEditTextOTP();

                Activity act = getActivity();
                assert act != null;
                ((MainActivity) act).getSMS();
            }
        });
    }

    public void setPhoneNumber(String phoneNumber) {
        et_phone_no.setText(phoneNumber);
    }

    public void showEditTextOTP() {
        et_OTP.setVisibility(View.VISIBLE);
    }

    public void setOTP(String otp) {
        et_OTP.setText(otp);
    }


}