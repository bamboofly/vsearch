package com.uan.vsearch.pinyin;

/**
 * 这个类保存了汉字对应的拼音的索引值{@link PinyinIndex}。
 * 用汉字的unicode值作为数组的索引，数组的值就是拼音的索引值
 */
class PinyinBlock {

    private final int mStartCode;

    private final int mEndCode;

    private final int[] mPinyinArray;

    public PinyinBlock(int start, int end, int[] array) {
        mStartCode = start;
        mEndCode = end;
        mPinyinArray = array;
    }

    /**
     * 获取该Block的大小
     *
     * @return int block的大小
     */
    public int getBlockSize() {
        return mEndCode - mStartCode + 1;
    }

    /**
     * 获取这个Block对应的起始汉字unicode值
     *
     * @return unicode值
     */
    public int getStartCode() {
        return mStartCode;
    }

    /**
     * 获取这个Block对应结束汉字的unicode值
     *
     * @return unicode值
     */
    public int getEndCode() {
        return mEndCode;
    }

    /**
     * 该unicode值是否在这个Block中
     *
     * @param code 需要查询的unicode值
     * @return true存在；false 不存在
     */
    public boolean contained(int code) {
        if (code >= mStartCode && code <= mEndCode) {
            return true;
        }
        return false;
    }

    /**
     * 返回该Block中对应unicode值对应的拼音索引{@link PinyinIndex}
     *
     * @param code 需要查询的unicode值
     * @return 拼音索引 {@link PinyinIndex}
     */
    public int getPinyinIndex(int code) {
        if (contained(code)) {
            return mPinyinArray[code - mStartCode];
        } else {
            return PinyinIndex.INDEX_NOT_FOUND;
        }
    }
}
