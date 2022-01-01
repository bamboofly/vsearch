package com.uan.vsearch.pinyin;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 汉字的拼音总共有一千多个，这个类提供一个功能：给每一个拼音申请一个索引，
 * 该索引在申请之后到进程结束前有效，然后可以通过索引查询拼音或通过拼音查询索引
 */
class PinyinIndex {

    private static final int INIT_LIST_SIZE = 2048;

    /**
     * 没有对应拼音的索引
     */
    public static final int INDEX_NOT_FOUND = -1;

    /**
     * 索引对应的拼音不存在
     */
    public static final String BAD_PINYIN = "";

    private final ArrayList<String> mPinyinList = new ArrayList<>(INIT_LIST_SIZE);

    private final HashMap<String, Integer> mPinyinIndexMap = new HashMap<>();

    /**
     * 获取对应索引的拼音
     *
     * @param index 拼音对应的索引，必须是{@link #getPinyinIndex(String)}
     *              和{@link #addPinyinIndex(String)}方法返回的值
     * @return 拼音
     */
    public String getPinyinByIndex(int index) {
        if (index < 0 || index > mPinyinList.size()) {
            return BAD_PINYIN;
        }

        return mPinyinList.get(index);
    }

    /**
     * 返回对应拼音的索引值
     *
     * @param pinyin 拼音字符串
     * @return 拼音索引值，当不存在该拼音时返回{@link #INDEX_NOT_FOUND}
     */
    public int getPinyinIndex(String pinyin) {
        Integer integer = mPinyinIndexMap.get(pinyin);
        if (integer == null) {
            return INDEX_NOT_FOUND;
        }
        return integer;
    }

    /**
     * 添加一个拼音并返回该拼音申请到的索引，当该拼音已经申请过索引则直接返回
     *
     * @param pinyin 拼音
     * @return 拼音的索引
     */
    public int addPinyinIndex(String pinyin) {
        int pinyinIndex = getPinyinIndex(pinyin);
        if (pinyinIndex != INDEX_NOT_FOUND) {
            return pinyinIndex;
        }

        pinyinIndex = mPinyinList.size();
        mPinyinList.add(pinyin);
        mPinyinIndexMap.put(pinyin, pinyinIndex);
        return pinyinIndex;
    }
}
