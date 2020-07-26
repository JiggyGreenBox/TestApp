package com.example.testapp;

public interface AppConstants {
    String BASE_URL = "http://192.168.0.102/slim_test";
    String OTP_REQUEST_URL = BASE_URL+"/otp_request";
    long OTP_VALID_TIME = 20000; // in milliseconds (min*60*1000)
}
