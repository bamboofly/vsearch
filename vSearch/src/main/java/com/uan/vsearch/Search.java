package com.uan.vsearch;

import android.content.Context;
import android.text.TextUtils;

import com.uan.vsearch.score.AdvanceAlikeRater;
import com.uan.vsearch.score.IAlikeRater;
import com.uan.vsearch.score.MarkDistanceRater;
import com.uan.vsearch.score.Scores;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.IntConsumer;

public class Search {

    private ArrayList<String> mSearchSource = new ArrayList<>();

    private HashMap<String, ArrayList<WordTarget>> mPingyinMap = new HashMap<>();

    private PinyinStore mPinyinStore;

    private NearPinyinGraph mNearPinyinGraph;

    private final IAlikeRater mScoring = new MarkDistanceRater();

    private final AdvanceAlikeRater mAdvanceScore = new AdvanceAlikeRater();

    public void init(Context context) {
        mPinyinStore = new PinyinStore();
        mPinyinStore.buildPinyin(context);

        mNearPinyinGraph = new NearPinyinGraph();
        mNearPinyinGraph.buildPinyinGraph(context);
    }

    public List<SearchResult> search(String voice, float dis) {

        HashMap<Integer, Scores> hashMap = new HashMap<>();
        ArrayList<Scores> scoresList = new ArrayList<>();

        // 标记
        int[] unicodeArray = voice.codePoints().toArray();

        int length = unicodeArray.length;
        for (int i = 0; i < length; i++) {

            String pinyin = mPinyinStore.getPinyin(unicodeArray[i]);
            if (TextUtils.isEmpty(pinyin)) {
                continue;
            }

            LinkedList<NearPinyin> nearPinyinList = mNearPinyinGraph.getNearPinyin(pinyin, dis);

            for (NearPinyin nearPinyin : nearPinyinList) {

                ArrayList<WordTarget> arrayList = mPingyinMap.get(nearPinyin.pinyin);
                if (arrayList == null) {
                    continue;
                }

                for (int j = 0; j < arrayList.size(); j++) {
                    WordTarget target = arrayList.get(j);
                    Scores scores = hashMap.get(target.nameIndex);
                    if (scores == null) {
                        scores = new Scores(target.nameIndex, target.nameLength, voice.length());
                        hashMap.put(target.nameIndex, scores);
                        scoresList.add(scores);
                    }
                    Hit hit = new Hit();
                    hit.alike = nearPinyin.alike;
                    hit.vIndex = i;
                    hit.target = target;
                    scores.hits.addLast(hit);
                }
            }

        }

        // 计算评分
        for (Scores hit : scoresList) {
            mScoring.scoring(hit);
//            mAdvanceScore.scoring2(hit);
        }

        // 按评分降序
        scoresList.sort((o1, o2) -> {
            if (o1.score > o2.score) {
                return -1;
            } else if (o1.score < o2.score) {
                return 1;
            }
            return 0;
        });

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
                int u = zi.charAt(0);
                String pingyin = mPinyinStore.getPinyin(zi);
                WordTarget target = hashMap.get(pingyin);
                if (target == null) {
                    target = new WordTarget(u, j, length);
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
