package com.uan.vsearch;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Search {

    private ArrayList<String> mSearchSource = new ArrayList<>();

    private HashMap<String, ArrayList<WordTarget>> mPingyinMap = new HashMap<>();

    private PinyinStore mPinyinStore;

    private NearPinyinGraph mNearPinyinGraph;

    private final Scoring mScoring = new Scoring();

    public void init(Context context) {
        mPinyinStore = new PinyinStore();
        mPinyinStore.buildPinyin(context);

        mNearPinyinGraph = new NearPinyinGraph();
        mNearPinyinGraph.buildPinyinGraph(context);
    }

    public List<SearchResult> search(String voice, float dis) {
        int length = voice.length();

        HashMap<Integer, Scores> hashMap = new HashMap<>();
        ArrayList<Scores> scoresList = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            String zi = voice.substring(i, i + 1);
            String pingyin = mPinyinStore.getPinyin(zi);

            if (TextUtils.isEmpty(pingyin)) {
                continue;
            }

            LinkedList<NearPinyin> nearPinyinList = mNearPinyinGraph.getNearPinyin(pingyin, dis);

            for (NearPinyin nearPinyin : nearPinyinList) {

                ArrayList<WordTarget> arrayList = mPingyinMap.get(nearPinyin.pinyin);
                if (arrayList == null) {
                    continue;
                }

                for (int j = 0; j < arrayList.size(); j++) {
                    WordTarget target = arrayList.get(j);
                    Scores scores = hashMap.get(target.nameIndex);
                    if (scores == null) {
                        scores = new Scores(target.nameIndex, target.nameLength);
                        hashMap.put(target.nameIndex, scores);
                        scoresList.add(scores);
                    }
                    Hit hit = new Hit();
                    hit.alike = nearPinyin.alike;
                    hit.vIndex = i;
                    hit.target = target;
                    scores.hits.push(hit);
                }
            }
        }

        scoringAndSort(scoresList);

        List<SearchResult> searchList;
        if (scoresList.size() > 7) {
            searchList = new ArrayList<>();
        } else {
            searchList = new LinkedList<>();
        }

        for (int i = 0; i < scoresList.size(); i++) {
            Scores scores = scoresList.get(i);
            String s = mSearchSource.get(scores.index);
            searchList.add(new SearchResult(s, scores.index, scores.score));
        }

        return searchList;
    }

    private void scoringAndSort(ArrayList<Scores> list) {

        for (Scores hit :
                list) {
            mScoring.scoring(hit);
        }

        Collections.sort(list, (o1, o2) -> {
            if (o1.score > o2.score) {
                return -1;
            } else if (o1.score < o2.score) {
                return 1;
            }
            return 0;
        });
    }


    public void addSearchSource(List<String> list) {
        if (list == null) {
            return;
        }

        mSearchSource.addAll(list);

        mPingyinMap = buildMap(mSearchSource);
    }

    private HashMap<String, ArrayList<WordTarget>> buildMap(ArrayList<String> list) {
        HashMap<String, ArrayList<WordTarget>> pinyinMap = new HashMap<>();

        for (int j = 0; j < list.size(); j++) {
            String name = list.get(j);

            int length = name.length();
            HashMap<String, WordTarget> hashMap = new HashMap<>();

            for (int i = 0; i < length; i++) {
                String zi = name.substring(i, i + 1);
                String pingyin = mPinyinStore.getPinyin(zi);
                WordTarget target = hashMap.get(pingyin);
                if (target == null) {
                    target = new WordTarget(j, length);
                    target.addIndex(i);
                    hashMap.put(pingyin, target);
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

        return pinyinMap;
    }


}
