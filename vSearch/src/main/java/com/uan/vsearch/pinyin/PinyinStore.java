package com.uan.vsearch.pinyin;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 汉字查询拼音功能
 */
public class PinyinStore {

    private static final String TAG = "PinyinStore";
    private final static String PINYIN_MAP_FILE_NAME = "kMandarin.txt";

    private final static int MAX_EMPTY_BUCKET_SIZE = 256;

    private final static int RADIX_HEX = 16;

    private final ArrayList<PinyinBlock> mBlockArray = new ArrayList<>();

    private final HashMap<String, String> mPinyinToneMap = new HashMap<>();

    private final PinyinIndex mPinyinIndex = new PinyinIndex();

    public PinyinStore() {
        buildToneMap();
    }

    private void buildToneMap() {
        mPinyinToneMap.put("ā", "a1");
        mPinyinToneMap.put("á", "a2");
        mPinyinToneMap.put("ǎ", "a3");
        mPinyinToneMap.put("à", "a4");

        mPinyinToneMap.put("ō", "o1");
        mPinyinToneMap.put("ó", "o2");
        mPinyinToneMap.put("ǒ", "o3");
        mPinyinToneMap.put("ò", "o4");

        mPinyinToneMap.put("ē", "e1");
        mPinyinToneMap.put("é", "e2");
        mPinyinToneMap.put("ě", "e3");
        mPinyinToneMap.put("è", "e4");

        mPinyinToneMap.put("ī", "i1");
        mPinyinToneMap.put("í", "i2");
        mPinyinToneMap.put("ǐ", "i3");
        mPinyinToneMap.put("ì", "i4");

        mPinyinToneMap.put("ū", "u1");
        mPinyinToneMap.put("ú", "u2");
        mPinyinToneMap.put("ǔ", "u3");
        mPinyinToneMap.put("ù", "u4");

        mPinyinToneMap.put("ǖ", "ü1");
        mPinyinToneMap.put("ǘ", "ü2");
        mPinyinToneMap.put("ǚ", "ü2");
        mPinyinToneMap.put("ǜ", "ü2");
    }

    public String getPinyin(String oneChinese) {
        if (oneChinese == null || oneChinese.length() < 1) {
            return "";
        }

        int unicode = oneChinese.codePointAt(0);

        return getPinyin(unicode);
    }

    public String getPinyin(int u) {
        for (PinyinBlock block : mBlockArray) {
            boolean contained = block.contained(u);
            if (contained) {
                return mPinyinIndex.getPinyinByIndex(block.getPinyinIndex(u));
            }
        }

        return "";
    }

    public int getPinyinIndex(int u) {
        int size = mBlockArray.size();
        for (int i = 0; i < size; i++) {
            PinyinBlock block = mBlockArray.get(i);
            boolean contained = block.contained(u);
            if (contained) {
                return block.getPinyinIndex(u);
            }
        }

        return -1;
    }

    public int getPinyinIndex(String pinyin) {
        return mPinyinIndex.getPinyinIndex(pinyin);
    }

    public void buildPinyin(Context context) {
        AssetManager assets = context.getAssets();
        InputStream inputStream = null;
        try {
            inputStream = assets.open(PINYIN_MAP_FILE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        ArrayList<String> lineList = new ArrayList<>();

        String line = null;
        try {
            int lastUnicode = Integer.MAX_VALUE;
            line = bufferedReader.readLine();
            while (line != null) {
                int maohaoIndex = line.indexOf(":");
                if (maohaoIndex < 2) {
                    throw new RuntimeException("pinyin map file error");
                }

                String unicodeStr = line.substring(2, maohaoIndex);
                Integer unicode = Integer.valueOf(unicodeStr, RADIX_HEX);

                if (unicode - lastUnicode > MAX_EMPTY_BUCKET_SIZE) {
                    PinyinBlock block = buildPinyinBlock(lineList);
                    addBlock(block);
                    lineList = new ArrayList<>(1024);

                }

                lineList.add(line);
                lastUnicode = unicode;


                line = bufferedReader.readLine();
            }

            if (lineList.size() > 0) {
                PinyinBlock block = buildPinyinBlock(lineList);
                addBlock(block);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    private void addBlock(PinyinBlock block) {
        int blockSize = block.getBlockSize();
        if (mBlockArray.size() > 0) {
            int addIndex = mBlockArray.size();
            for (int i = 0; i < mBlockArray.size(); i++) {
                PinyinBlock pinyinBlock = mBlockArray.get(i);
                int size = pinyinBlock.getBlockSize();
                if (size < blockSize) {
                    addIndex = i;
                    break;
                }
            }
            mBlockArray.add(addIndex, block);
        } else {
            mBlockArray.add(block);
        }
    }

    private PinyinBlock buildPinyinBlock(ArrayList<String> lineList) {
        int size = lineList.size();
        String startLine = lineList.get(0);
        int maohaoIndex = startLine.indexOf(":");
        String startUnicodeStr = startLine.substring(2, maohaoIndex);
        Integer startUnicode = Integer.valueOf(startUnicodeStr, RADIX_HEX);

        String endLine = lineList.get(size - 1);
        maohaoIndex = endLine.indexOf(":");
        String endUnicodeStr = endLine.substring(2, maohaoIndex);
        Integer endUnicode = Integer.valueOf(endUnicodeStr, RADIX_HEX);

        int arrayLength = endUnicode - startUnicode + 1;
        Log.i(TAG, "build pinyin block array length " + arrayLength);
        int[] pinyinArray = new int[arrayLength];

        for (String line : lineList) {
            String[] lineArray = line.split(" +");
            if (lineArray.length < 3) {
                throw new RuntimeException("pinyin map file error!");
            }

            String unicodeStr = lineArray[0];
            unicodeStr = unicodeStr.substring(2, unicodeStr.length() - 1);
            Integer unicode = Integer.valueOf(unicodeStr, RADIX_HEX);

            String pinyin = lineArray[1];

            pinyin = replaceTone(pinyin);

            int index = unicode - startUnicode;
            pinyinArray[index] = mPinyinIndex.addPinyinIndex(pinyin);
        }

        PinyinBlock block = new PinyinBlock(startUnicode, endUnicode, pinyinArray);
        return block;
    }

    private String replaceTone(String pinyin) {
        char[] chars = pinyin.toCharArray();
        String result = pinyin;
        int length = chars.length;
        for (int i = 0; i < length; i++) {
            String key = String.valueOf(chars[i]);
            if (mPinyinToneMap.containsKey(key)) {
                String value = mPinyinToneMap.get(key);
                result = result.replace(key, value);
            }
        }
        return result;
    }
}
