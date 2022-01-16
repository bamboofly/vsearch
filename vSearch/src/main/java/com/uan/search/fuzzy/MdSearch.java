package com.uan.search.fuzzy;

import android.content.Context;

import com.uan.search.participle.StringMap;
import com.uan.search.pinyin.NearPinyinGraph;
import com.uan.search.pinyin.PinyinStore;

import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * 字符串声音模糊搜索类。实现快速搜索和普通搜索两个功能。
 */
public class MdSearch implements IFastMdSearch {

    private static final float NEAR_SEARCH_DEPTH_DEFAULT = 0.3f;

    private final IFastSearch mFastSearch;
    private final ICommonSearch mCommonSearch;
    private final PinyinStore mPinyinStore;

    private final float mNearSearchDepth;

    private final Comparator<SearchResult> mResultComparator = (o1, o2) -> {
        if (o1.getScore() > o2.getScore()) {
            return -1;
        } else if (o1.getScore() < o2.getScore()) {
            return 1;
        }
        return 0;
    };

    private MdSearch(PinyinStore pinyinStore,
                     ICommonSearch commonSearch,
                     IFastSearch fastMdSearch,
                     float nearSearchDepth) {
        mPinyinStore = pinyinStore;
        mFastSearch = fastMdSearch;
        mCommonSearch = commonSearch;
        mNearSearchDepth = nearSearchDepth;
    }

    @Override
    public List<SearchResult> search(String key) {
        return search(key, mNearSearchDepth);
    }

    @Override
    public List<SearchResult> search(String key, float nearDepth) {
        if (mFastSearch != null) {
            VoiceList voiceList = VoiceConvert.anyToVoices(key, mPinyinStore);
            List<SearchResult> results = mFastSearch.search(voiceList, nearDepth);
            results.sort(mResultComparator);
            return results;
        } else {
            return new LinkedList<>();
        }
    }

    @Override
    public List<SearchResult> search(List<String> list, String key) {
        return search(list, key, mNearSearchDepth);
    }

    @Override
    public List<SearchResult> search(List<String> list, String key, float nearDepth) {
        VoiceList voiceList = VoiceConvert.anyToVoices(key, mPinyinStore);
        List<SearchResult> results = mCommonSearch.search(list, voiceList, nearDepth);
        if (mFastSearch != null) {
            results.addAll(mFastSearch.search(voiceList, nearDepth));
        }
        results.sort(mResultComparator);
        return results;
    }

    public static class Builder {


        private Context context;

        private static WeakReference<PinyinStore> mWeakPinyinStore;

        private float depth = NEAR_SEARCH_DEPTH_DEFAULT;

        private PinyinStore createPinyinStore(Context context) {
            if (mWeakPinyinStore != null) {
                PinyinStore pinyinStore = mWeakPinyinStore.get();
                if (pinyinStore != null) {
                    return pinyinStore;
                }
            }
            PinyinStore pinyinStore = new PinyinStore();
            pinyinStore.initPinyin(context);
            mWeakPinyinStore = new WeakReference<>(pinyinStore);
            return pinyinStore;
        }

        /**
         * Application Context
         *
         * @param c {@link Context}
         * @return Builder
         */
        public Builder context(Context c) {
            context = c;
            return this;
        }

        /**
         * 设置模糊搜索深度
         *
         * @param n 0～0.5 值越大搜索相似音范围越大
         * @return Builder
         */
        public Builder nearSearchDepth(float n) {
            if (n < 0) {
                depth = 0f;
            } else depth = Math.min(n, 0.3f);

            return this;
        }

        /**
         * 构建一个快速搜索实例
         *
         * @param stringList 被搜索的字符串列表
         * @return 快速搜索实例
         */
        public IFastMdSearch build(List<String> stringList) {
            checkParams();

            PinyinStore pinyinStore = createPinyinStore(context);

            NearPinyinGraph nearPinyinGraph = new NearPinyinGraph();
            nearPinyinGraph.buildPinyinGraph(context);

            StringMap stringMap = new StringMap(pinyinStore);
            stringMap.put(stringList);

            FastSearchImpl fastSearch = new FastSearchImpl(pinyinStore, nearPinyinGraph, stringMap);

            CommonSearchImpl commonSearch = new CommonSearchImpl(pinyinStore, nearPinyinGraph);

            return new MdSearch(pinyinStore, commonSearch, fastSearch, depth);
        }

        /**
         * 构建一个普通搜索实例
         *
         * @return IMdSearch
         */
        public IMdSearch build() {
            checkParams();

            PinyinStore pinyinStore = createPinyinStore(context);

            NearPinyinGraph nearPinyinGraph = new NearPinyinGraph();
            nearPinyinGraph.buildPinyinGraph(context);

            CommonSearchImpl commonSearch = new CommonSearchImpl(pinyinStore, nearPinyinGraph);

            return new MdSearch(pinyinStore, commonSearch, null, depth);
        }

        private void checkParams() {
            if (context == null) {
                throw new RuntimeException("need set context first!");
            }
        }
    }
}
