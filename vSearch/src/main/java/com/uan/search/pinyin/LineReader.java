package com.uan.search.pinyin;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 文件读取包装类。用来读取Asset文本文件的内容
 */
public class LineReader {

    private final AssetManager assetManager;
    private final String fileName;

    /**
     * 构造一个Asset文本文件读取包装类
     *
     * @param a 资源管理器
     * @param f 文本文件名称
     */
    public LineReader(AssetManager a, String f) {
        assetManager = a;
        fileName = f;
    }

    /**
     * 遍历文本文件中的每一行字符串
     *
     * @param lineListener 行字符串监听器
     */
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

    /**
     * 文本文件中读取每一行字符串监听接口
     */
    public interface Line {
        /**
         * 读到文本文件的一行字符串
         *
         * @param l 字符串
         */
        void line(String l);
    }
}
