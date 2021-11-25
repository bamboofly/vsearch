package com.uan.contacts;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        String s = "㐁";
        char charAt = s.charAt(0);
        int i = charAt;
        Log.e("lianghuan", "charAt " + i);

        String s2 = "㐄";
        char charAt2 = s2.charAt(0);
        int i2 = charAt2;
        Log.e("lianghuan", "charAt2 " + i2);

        String s3 = "ì";
        char charAt3 = s3.charAt(0);
        int i3 = charAt3;
        Log.e("lianghuan", "charAt3 " + i3);

        String s4 = "á";
        char charAt4 = s4.charAt(0);
        int i4 = charAt4;
        Log.e("lianghuan", "charAt4 " + i4);

        String s5 = "ē";
        char charAt5 = s5.charAt(0);
        int i5 = charAt5;
        Log.e("lianghuan", "chartAt5 " + i5);

        String s6 = "ú";
        char charAt6 = s6.charAt(0);
        int i6 = charAt6;
        Log.e("lianghuan", "charAt6 " + i6);
    }
}