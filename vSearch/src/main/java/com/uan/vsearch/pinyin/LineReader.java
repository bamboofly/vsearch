package com.uan.vsearch.pinyin;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LineReader {

    private final AssetManager assetManager;
    private final String fileName;

    public LineReader(AssetManager a, String f) {
        assetManager = a;
        fileName = f;
    }

    public void eachLine(Line lineListener) {
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (inputStream == null) {
            throw new RuntimeException("can not open file with name " + fileName + ", please check again");
        }

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String line = null;
        try {
            line = bufferedReader.readLine();
            while (line != null) {
                line = line.trim();
                lineListener.line(line);

                line = bufferedReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public interface Line {
        void line(String l);
    }
}
