package com.uan.vsearch;

import android.content.Context;

import com.uan.vsearch.participle.StringMap;
import com.uan.vsearch.pinyin.NearPinyinGraph;
import com.uan.vsearch.pinyin.PinyinStore;

import java.util.List;

public class MdSearch {


//
//    public List<SearchResult> search(List<String> sourceList, String key, float dis) {
//
//        int[] unicodeArray = key.codePoints().toArray();
//
//        int length = unicodeArray.length;
//
//        HashMap<Integer, LinkedList<Search.TargetNode>> hashMap = new HashMap<>();
//
//        for (int i = 0; i < length; i++) {
//            String pinyin = mPinyinStore.getPinyin(unicodeArray[i]);
//
//            if (TextUtils.isEmpty(pinyin)) {
//                continue;
//            }
//
//            LinkedList<NearPinyin> nearPinyin = mNearPinyinGraph.getNearPinyin(pinyin, dis);
//
//            for (NearPinyin n : nearPinyin) {
//                int pinyinIndex = mPinyinStore.getPinyinIndex(n.pinyin);
//                LinkedList<Search.TargetNode> targetNodes = hashMap.get(i);
//                if (targetNodes == null) {
//                    targetNodes = new LinkedList<>();
//                    hashMap.put(pinyinIndex, targetNodes);
//                }
//
//                targetNodes.add(new Search.TargetNode(i, n.alike));
//            }
//        }
//
//        ArrayList<Hit> hits = new ArrayList<>();
//        for (String l : sourceList) {
//            int[] codeArray = l.codePoints().toArray();
//            int arrayLen = codeArray.length;
//
//            for (int i = 0; i < arrayLen; i++) {
//                int pinyinIndex = mPinyinStore.getPinyinIndex(codeArray[i]);
//
//                LinkedList<Search.TargetNode> targetNodes = hashMap.get(pinyinIndex);
//
//                if (targetNodes == null) {
//                    continue;
//                }
//
//                for (Search.TargetNode t : targetNodes) {
//                    Hit hit = new Hit();
//
//                }
//            }
//        }
//
//        return null;
//    }


    private static class TargetNode {

        public final int vIndex;

        public final float alike;

        TargetNode(int v, float a) {
            vIndex = v;
            alike = a;
        }
    }


    public static class Builder {


        private Context context;

        public Builder context(Context c) {
            context = c;
            return this;
        }

        public IFastSearch create(List<String> stringList) {
            if (context == null) {
                throw new RuntimeException("need set context first!");
            }

            PinyinStore pinyinStore = new PinyinStore();
            pinyinStore.buildPinyin(context);

            NearPinyinGraph nearPinyinGraph = new NearPinyinGraph();
            nearPinyinGraph.buildPinyinGraph(context);

            StringMap stringMap = new StringMap(pinyinStore);
            stringMap.put(stringList);

            return new FastSearchImpl(pinyinStore, nearPinyinGraph, stringMap);
        }

    }
}
