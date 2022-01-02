package com.uan.vsearch.participle;

import com.uan.vsearch.pinyin.PinyinStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class StringMap {

    private final ArrayList<String> mSourceList = new ArrayList<>();
    private final PinyinStore mPinyinStore;
    private final HashMap<String, ArrayList<WordTarget>> pinyinMap = new HashMap<>();

    public StringMap(PinyinStore store) {
        mPinyinStore = store;
    }

    public void put(ArrayList<String> list) {
        if (list == null || list.size() == 0) {
            return;
        }

        mSourceList.addAll(list);

        buildMap(list);
    }

    public ArrayList<WordTarget> getTargetList(String key) {
        return pinyinMap.get(key);
    }

    public String getSourceString(int index) {
        int size = mSourceList.size();
        if (index < 0 || index >= size) {
            return "";
        }
        return mSourceList.get(index);
    }

    private void buildMap(ArrayList<String> list) {


        for (int j = 0; j < list.size(); j++) {
            String name = list.get(j);

            int[] unicodeArray = name.codePoints().toArray();
            int length = unicodeArray.length;
            HashMap<String, WordTarget> hashMap = new HashMap<>();

            for (int i = 0; i < length; i++) {
                int u = unicodeArray[i];
                String pinyin = mPinyinStore.getPinyin(u);
                WordTarget target = hashMap.get(pinyin);
                if (target == null) {
                    target = new WordTarget(u, j, length);
                    target.addIndex(i);
                    hashMap.put(pinyin, target);
                } else {
                    target.addIndex(i);
                }
            }

            Set<String> keySet = hashMap.keySet();
            for (String next : keySet) {
                WordTarget target = hashMap.get(next);
                ArrayList<WordTarget> arrayList = pinyinMap.get(next);
                if (arrayList == null) {
                    arrayList = new ArrayList<>();
                    pinyinMap.put(next, arrayList);
                }
                arrayList.add(target);
            }
        }

    }
}
