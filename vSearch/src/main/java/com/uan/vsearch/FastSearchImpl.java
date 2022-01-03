package com.uan.vsearch;

import android.text.TextUtils;

import com.uan.vsearch.participle.StringMap;
import com.uan.vsearch.participle.WordTarget;
import com.uan.vsearch.pinyin.NearPinyin;
import com.uan.vsearch.pinyin.NearPinyinGraph;
import com.uan.vsearch.pinyin.PinyinStore;
import com.uan.vsearch.score.MRecord;
import com.uan.vsearch.score.Mark;
import com.uan.vsearch.score.MdRaterImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

class FastSearchImpl implements IFastSearch {


    private final PinyinStore mPinyinStore;

    private final NearPinyinGraph mNearPinyinGraph;

    private final StringMap mStringMap;

    private final MdRaterImpl mMdRater = new MdRaterImpl();

    FastSearchImpl(PinyinStore p, NearPinyinGraph n, StringMap s) {
        mPinyinStore = p;

        mNearPinyinGraph = n;

        mStringMap = s;
    }


    @Override
    public List<SearchResult> search(String voice, float dis) {
        return search(mStringMap, voice, dis);
    }


    public List<SearchResult> search(StringMap stringMap, String voice, float dis) {

        HashMap<Integer, FastScore> hashMap = new HashMap<>();
        ArrayList<FastScore> scoresList = new ArrayList<>();

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
                    FastScore scores = hashMap.get(target.nameIndex);
                    if (scores == null) {
                        scores = new FastScore(target.nameIndex, voice.length(), target.nameLength);
                        hashMap.put(target.nameIndex, scores);
                        scoresList.add(scores);
                    }
                    float alike;
                    if (target.unicode == u) {
                        alike = NearPinyinGraph.FULL_ALIKE_SCORE;
                    } else {
                        alike = nearPinyin.alike;
                    }
                    LinkedList<Integer> wordIndexList = target.getWordIndexList();
                    for (Integer wordIndex : wordIndexList) {
                        Mark mark = new Mark(i, wordIndex, alike);
                        scores.marks.add(mark);
                    }
                }
            }

        }

        // 计算评分
        int size = scoresList.size();
        for (int i = 0; i < size; i++) {
            FastScore fastScore = scoresList.get(i);
            fastScore.score = mMdRater.scoring(fastScore);
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

        List<SearchResult> searchList = new ArrayList<>(scoresList.size());

        for (int i = 0; i < scoresList.size(); i++) {
            FastScore scores = scoresList.get(i);
            String s = stringMap.getSourceString(scores.stringMapIndex);
            searchList.add(new SearchResult(s, scores.stringMapIndex, scores.score));
        }

        return searchList;
    }

    static class FastScore extends MRecord {

        public final int stringMapIndex;

        public float score;

        public FastScore(int i, int v, int w) {
            super(v, w);
            stringMapIndex = i;
        }
    }
}
