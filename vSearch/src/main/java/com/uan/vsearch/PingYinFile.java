package com.uan.vsearch;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class PingYinFile {

    private static final String FILE_NAME = "pingyin.txt";

    private final Context mContext;

    private HashMap<String, String> mHanzi2PingyinMap;

    public PingYinFile(Context context) {
        mContext = context;
    }


    public String getPingyin(String hanzi) {
        if (mHanzi2PingyinMap == null) {
            return null;
        }

        return mHanzi2PingyinMap.get(hanzi);
    }

    public boolean load() {

        HashMap<String, String> hashMap = new HashMap<>();
        try {
            AssetFileDescriptor openFd = mContext.getAssets().openFd(FILE_NAME);
            FileReader fileReader = new FileReader(openFd.getFileDescriptor());
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = null;
            bufferedReader.lines().filter(s -> {
                if (s == null) {
                    return false;
                } else {
                    return !s.trim().isEmpty();
                }
            }).forEach(s -> parseAndAddMap(hashMap, s));

        } catch (IOException e) {
            e.printStackTrace();
        }
        mHanzi2PingyinMap = hashMap;
        return true;
    }

    private void parseAndAddMap(HashMap<String, String> hashMap, String line) {


        line = line.trim();
        line = line.substring(0, 1);
        line = line.substring(line.length() - 2, line.length() - 1);

        String[] split = line.split(", ");
        if (split == null || split.length < 2) {
            return;
        }

        String pingyin = split[0].replaceAll("\"", "");
        for (int i = 1; i < split.length; ++i) {
            String hanzi = split[i];
            hanzi = hanzi.replaceAll("\"", "");
            hashMap.put(hanzi, pingyin);
        }

    }

}
