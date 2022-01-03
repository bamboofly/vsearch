package com.uan.vsearch;

import android.content.Context;

import com.uan.vsearch.participle.StringMap;
import com.uan.vsearch.pinyin.NearPinyinGraph;
import com.uan.vsearch.pinyin.PinyinStore;

import java.util.List;

public class MdSearch {

    public static class Builder {


        private Context context;

        public Builder context(Context c) {
            context = c;
            return this;
        }

        public IFastSearch build(List<String> stringList) {
            checkParams();

            PinyinStore pinyinStore = new PinyinStore();
            pinyinStore.initPinyin(context);

            NearPinyinGraph nearPinyinGraph = new NearPinyinGraph();
            nearPinyinGraph.buildPinyinGraph(context);

            StringMap stringMap = new StringMap(pinyinStore);
            stringMap.put(stringList);

            return new FastSearchImpl(pinyinStore, nearPinyinGraph, stringMap);
        }

        public ISearch build() {
            checkParams();

            PinyinStore pinyinStore = new PinyinStore();
            pinyinStore.initPinyin(context);

            NearPinyinGraph nearPinyinGraph = new NearPinyinGraph();
            nearPinyinGraph.buildPinyinGraph(context);

            return new SearchImpl(pinyinStore, nearPinyinGraph);
        }

        private void checkParams() {
            if (context == null) {
                throw new RuntimeException("need set context first!");
            }
        }
    }
}
