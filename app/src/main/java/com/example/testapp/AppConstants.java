package com.example.testapp;

public interface AppConstants {
    String BASE_URL = "http://192.168.0.103/slim_test";

    String OTP_REQUEST_URL = BASE_URL + "/otp_request";
    String OTP_VERIFY_URL = BASE_URL + "/otp_verify";
    String REF_VERIFY_URL = BASE_URL + "/ref_verify";

    String CARS_PENDING_URL = BASE_URL + "/cars_pending";
    long OTP_VALID_TIME = 20000; // in milliseconds (min*60*1000)

    long AUTH_VALID_TIME = 180000; // in milliseconds (min*60*1000)
}
