package com.uan.search.fuzzy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 * 拼音列表
 */
public class VoiceList {

    /**
     * 单个拼音
     */
    public static class Voice {
        /**
         * 拼音对应字的unicode码
         */
        public final int unicode;
        /**
         * 拼音
         */
        public final String pinyin;

        public Voice(int u, String p) {
            unicode = u;
            pinyin = p;
        }
    }

    /**
     * 单个拼音消费者
     */
    public interface IVoiceConsumer {
        /**
         * 遍历每个拼音到来，拼音不为空
         *
         * @param index 拼音在列表中的索引
         * @param voice 拼音
         */
        void to(int index, @NonNull Voice voice);
    }

    private final ArrayList<Voice> mList = new ArrayList<>();

    /**
     * 拼音列表的长度
     *
     * @return 长度
     */
    public int length() {
        return mList.size();
    }

    /**
     * 遍历每一个拼音，如果拼音为空将跳过不传给消费者
     *
     * @param consumer 拼音消费者
     */
    public void eachVoice(IVoiceConsumer consumer) {
        int size = mList.size();
        for (int i = 0; i < size; i++) {
            Voice voice = mList.get(i);
            if (voice == null) {
                continue;
            }
            consumer.to(i, voice);
        }
    }

    /**
     * 添加拼音到列表中
     *
     * @param v 拼音，允许为空
     */
    public void add(@Nullable Voice v) {
        mList.add(v);
    }
}
