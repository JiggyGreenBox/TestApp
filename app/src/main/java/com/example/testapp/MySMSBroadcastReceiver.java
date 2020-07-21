package com.example.testapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

public class MySMSBroadcastReceiver extends BroadcastReceiver {


    interface otpCallBack {
        void otpRead(String otp);
    }

    // Also declare the interface in your BroadcastReceiver as static
    private static otpCallBack otpCallBack;


    @Override
    public void onReceive(Context context, Intent intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);

            assert status != null;
            switch (status.getStatusCode()) {
                case CommonStatusCodes.SUCCESS:
                    // Get SMS message contents
                    String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                    // Extract one-time code from the message and complete verification
                    // by sending the code back to your server.

                    if (message != null) {
                        String[] separated = message.split("\n");


                        String otp = separated[0].replace("<#> SampleApp: Your verification code is ", "");
                        Log.e("SMS message", otp);

                        // send to UI
                        otpCallBack.otpRead(otp);
                    }
                    break;
                case CommonStatusCodes.TIMEOUT:
                    // Waiting for SMS timed out (5 minutes)
                    // Handle the error ...
                    break;
            }
        }
    }


    public static void registerCallback(otpCallBack otpCallBack) {
        MySMSBroadcastReceiver.otpCallBack = otpCallBack;
    }
}
