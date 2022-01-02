package com.uan.vsearch.score;

import java.util.ArrayList;

/**
 * 两个字符串之间相似字符的标记记录
 */
public class MRecord {

    /**
     * 输入字符串的长度
     */
    public final int vLen;

    /**
     * 被搜索的字符串长度
     */
    public final int wLen;

    /**
     * 相似字符标记记录 {@link Mark}，该集合需要按输入字符串的索引递增排序
     */
    public final ArrayList<Mark> marks = new ArrayList<>();

    public MRecord(int v, int w) {
        vLen = v;
        wLen = w;
    }
}
