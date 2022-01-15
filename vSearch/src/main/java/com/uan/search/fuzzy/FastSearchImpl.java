package com.uan.search.fuzzy;

import com.uan.search.participle.StringMap;
import com.uan.search.participle.WordTarget;
import com.uan.search.pinyin.NearPinyin;
import com.uan.search.pinyin.NearPinyinGraph;
import com.uan.search.pinyin.PinyinStore;
import com.uan.search.score.MRecord;
import com.uan.search.score.Mark;
import com.uan.search.score.MdRaterImpl;

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
    public List<SearchResult> search(VoiceList voiceList, float dis) {
        return search(mStringMap, voiceList, dis);
    }


    public List<SearchResult> search(StringMap stringMap, VoiceList voiceList, float dis) {

        HashMap<Integer, FastScore> hashMap = new HashMap<>();
        ArrayList<FastScore> scoresList = new ArrayList<>();

        // 标记
        int length = voiceList.length();
        voiceList.eachVoice((index, voice) -> {
            int u = voice.unicode;
            String pinyin = voice.pinyin;
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
                        scores = new FastScore(target.nameIndex, length, target.nameLength);
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
                        Mark mark = new Mark(index, wordIndex, alike);
                        scores.marks.add(mark);
                    }
                }
            }
        });

        // 计算评分
        int size = scoresList.size();
        for (int i = 0; i < size; i++) {
            FastScore fastScore = scoresList.get(i);
            fastScore.score = mMdRater.scoring(fastScore);
        }

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
