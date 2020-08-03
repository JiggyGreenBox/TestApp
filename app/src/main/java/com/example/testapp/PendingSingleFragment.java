package com.example.testapp;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;


public class PendingSingleFragment extends Fragment {

    ImageView imageView;
    TextView tv_amount, tv_fuel_type;

    public final static int WIDTH = 800;
    public final static int HEIGHT = 800;

    public PendingSingleFragment() {
        // Required empty public constructor
    }

    public static PendingSingleFragment newInstance(String amount, String fuel_type, String trans_qr) {

        Bundle args = new Bundle();
        args.putString("amount", amount);
        args.putString("fuel_type", fuel_type);
        args.putString("trans_qr", trans_qr);

        PendingSingleFragment fragment = new PendingSingleFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_pending_single, container, false);

        ViewGroup.LayoutParams params = v.getLayoutParams();
        params.height = getScreenDimens();
        Log.e("height", "" + getScreenDimens());
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageView = view.findViewById(R.id.im_pending_qr);

        tv_amount = view.findViewById(R.id.tv_amount);
        tv_fuel_type = view.findViewById(R.id.tv_fuel_type);

        String qr = "invalid";
        String amount = "invalid";
        String fuel_type = "invalid";
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            qr = bundle.getString("trans_qr", "invalid");
            amount = bundle.getString("amount", "invalid");
            fuel_type = bundle.getString("fuel_type", "invalid");

            tv_amount.setText(amount);
            tv_fuel_type.setText(fuel_type);
        }

        if (!qr.equals("invalid")) {

            try {
                Bitmap bitmap = encodeAsBitmap(qr);
                imageView.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
    }

    Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, WIDTH, WIDTH, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }

    public int getScreenDimens() {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        // bottom offset
        float offset = 100 * displayMetrics.density;

        Log.e("height1", "" + dpHeight);

        return (int) (displayMetrics.heightPixels - offset);
    }
}