package com.uan.vsearch;

public class PinyinBlock {

    private final int mStartCode;

    private final int mEndCode;

    private final String[] mPinyinArray;

    public PinyinBlock(int start, int end, String[] array) {
        mStartCode = start;
        mEndCode = end;
        mPinyinArray = array;
    }

    public int getStartCode() {
        return mStartCode;
    }

    public int getEndCode() {
        return mEndCode;
    }

    public boolean contained(int code) {
        if (code >= mStartCode && code <= mEndCode) {
            return true;
        }
        return false;
    }

    public String getPinyin(int code) {
        if (contained(code)) {
            return mPinyinArray[code - mStartCode];
        } else {
            return null;
        }
    }
}
