package com.uan.search.score;

/**
 * 标记关系类
 */
public class Mark {

    /**
     * 输入搜索字符串相似字的索引
     */
    public final int vIndex;

    /**
     * 被搜索字符串相似字的索引
     */
    public final int wIndex;

    /**
     * 相似度
     */
    public final float alike;

    public Mark(int v, int w, float a) {
        vIndex = v;
        wIndex = w;
        alike = a;
    }
}
