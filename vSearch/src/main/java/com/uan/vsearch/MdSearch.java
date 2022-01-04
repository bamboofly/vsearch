package com.uan.vsearch;

import android.content.Context;

import com.uan.vsearch.participle.StringMap;
import com.uan.vsearch.pinyin.NearPinyinGraph;
import com.uan.vsearch.pinyin.PinyinStore;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class MdSearch implements IFastMdSearch {

    private static final float NEAR_SEARCH_DEPTH_DEFAULT = 0.3f;

    private final IFastSearch mFastSearch;
    private final ICommonSearch mCommonSearch;

    private final float mNearSearchDepth;

    private final Comparator<SearchResult> mResultComparator = (o1, o2) -> {
        if (o1.getScore() > o2.getScore()) {
            return -1;
        } else if (o1.getScore() < o2.getScore()) {
            return 1;
        }
        return 0;
    };

    private MdSearch(ICommonSearch commonSearch, IFastSearch fastMdSearch, float nearSearchDepth) {
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
            List<SearchResult> results = mFastSearch.search(key, nearDepth);
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
        List<SearchResult> results = mCommonSearch.search(list, key, nearDepth);
        if (mFastSearch != null) {
            results.addAll(mFastSearch.search(key, nearDepth));
        }
        results.sort(mResultComparator);
        return results;
    }

    public static class Builder {


        private Context context;

        private float depth = NEAR_SEARCH_DEPTH_DEFAULT;

        public Builder context(Context c) {
            context = c;
            return this;
        }

        public Builder nearSearchDepth(float n) {
            if (n < 0) {
                depth = 0f;
            } else depth = Math.min(n, 0.3f);

            return this;
        }

        public IFastMdSearch build(List<String> stringList) {
            checkParams();

            PinyinStore pinyinStore = new PinyinStore();
            pinyinStore.initPinyin(context);

            NearPinyinGraph nearPinyinGraph = new NearPinyinGraph();
            nearPinyinGraph.buildPinyinGraph(context);

            StringMap stringMap = new StringMap(pinyinStore);
            stringMap.put(stringList);

            FastSearchImpl fastSearch = new FastSearchImpl(pinyinStore, nearPinyinGraph, stringMap);

            CommonSearchImpl commonSearch = new CommonSearchImpl(pinyinStore, nearPinyinGraph);

            return new MdSearch(commonSearch, fastSearch, depth);
        }

        public IMdSearch build() {
            checkParams();

            PinyinStore pinyinStore = new PinyinStore();
            pinyinStore.initPinyin(context);

            NearPinyinGraph nearPinyinGraph = new NearPinyinGraph();
            nearPinyinGraph.buildPinyinGraph(context);

            CommonSearchImpl commonSearch = new CommonSearchImpl(pinyinStore, nearPinyinGraph);

            return new MdSearch(commonSearch, null, depth);
        }

        private void checkParams() {
            if (context == null) {
                throw new RuntimeException("need set context first!");
            }
        }
    }
}
