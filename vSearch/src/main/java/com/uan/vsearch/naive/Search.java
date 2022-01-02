package com.uan.vsearch.naive;

import android.text.TextUtils;

import com.uan.vsearch.SearchResult;
import com.uan.vsearch.participle.StringMap;
import com.uan.vsearch.participle.WordTarget;
import com.uan.vsearch.pinyin.NearPinyin;
import com.uan.vsearch.pinyin.NearPinyinGraph;
import com.uan.vsearch.pinyin.PinyinStore;
import com.uan.vsearch.naive.score.IAlikeRater;
import com.uan.vsearch.naive.score.MarkTimesRater;
import com.uan.vsearch.naive.score.Scores;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Search {

    private final PinyinStore mPinyinStore;

    private final NearPinyinGraph mNearPinyinGraph;

    private final IAlikeRater mScoring = new MarkTimesRater();

    public Search(PinyinStore pinyinStore, NearPinyinGraph nearPinyinGraph) {
        mPinyinStore = pinyinStore;

        mNearPinyinGraph = nearPinyinGraph;
    }

    public List<SearchResult> search(StringMap stringMap, String voice, float dis) {

        HashMap<Integer, Scores> hashMap = new HashMap<>();
        ArrayList<Scores> scoresList = new ArrayList<>();

        // 标记
        int[] unicodeArray = voice.codePoints().toArray();

        int length = unicodeArray.length;
        for (int i = 0; i < length; i++) {

            int u = unicodeArray[i];
            String pinyin = mPinyinStore.getPinyin(u);
            if (TextUtils.isEmpty(pinyin)) {
                continue;
            }

            LinkedList<NearPinyin> nearPinyinList = mNearPinyinGraph.getNearPinyin(pinyin, dis);

            for (NearPinyin nearPinyin : nearPinyinList) {

                ArrayList<WordTarget> arrayList = stringMap.getTargetList(nearPinyin.pinyin);
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
                    if (target.unicode == u) {
                        hit.alike = NearPinyinGraph.FULL_ALIKE_SCORE;
                    } else {
                        hit.alike = nearPinyin.alike;
                    }
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
            searchList = new ArrayList<>(scoresList.size());
        } else {
            searchList = new LinkedList<>();
        }

        for (int i = 0; i < scoresList.size(); i++) {
            Scores scores = scoresList.get(i);
            String s = stringMap.getSourceString(scores.index);
            searchList.add(new SearchResult(s, scores.index, scores.score));
        }

        return searchList;
    }


}
