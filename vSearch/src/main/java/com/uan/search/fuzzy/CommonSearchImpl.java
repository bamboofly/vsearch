package com.uan.search.fuzzy;

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

class CommonSearchImpl implements ICommonSearch {


    private final PinyinStore mPinyinStore;

    private final NearPinyinGraph mNearPinyinGraph;

    private final MdRaterImpl mMdRater = new MdRaterImpl();

    CommonSearchImpl(PinyinStore p, NearPinyinGraph n) {
        mPinyinStore = p;

        mNearPinyinGraph = n;
    }


    @Override
    public List<SearchResult> search(List<String> sourceList, VoiceList voiceList, float dis) {


        HashMap<Integer, ArrayList<TargetNode>> hashMap = new HashMap<>();
        int voiceLength = voiceList.length();

        voiceList.eachVoice((index, voice) -> {
            int unicode = voice.unicode;
            String pinyin = voice.pinyin;
            LinkedList<NearPinyin> nearPinyin = mNearPinyinGraph.getNearPinyin(pinyin, dis);

            for (NearPinyin n : nearPinyin) {
                int pinyinIndex = mPinyinStore.getPinyinIndex(n.pinyin);
                ArrayList<TargetNode> targetNodes = hashMap.get(pinyinIndex);
                if (targetNodes == null) {
                    targetNodes = new ArrayList<>();
                    hashMap.put(pinyinIndex, targetNodes);
                }

                targetNodes.add(new TargetNode(unicode, index, n.alike));
            }
        });

        ArrayList<Scores> scoresArrayList = new ArrayList<>();

        for (String l : sourceList) {
            int[] codeArray = l.codePoints().toArray();
            int arrayLen = codeArray.length;

            Scores scores = new Scores(l, voiceLength, arrayLen);

            for (int i = 0; i < arrayLen; i++) {
                int pinyinIndex = mPinyinStore.getPinyinIndex(codeArray[i]);

                ArrayList<TargetNode> targetNodes = hashMap.get(pinyinIndex);

                if (targetNodes == null) {
                    continue;
                }

                int size = targetNodes.size();
                for (int j = 0; j < size; j++) {
                    TargetNode t = targetNodes.get(j);
                    float alike;
                    if (t.unicode == codeArray[i]) {
                        alike = 1f;
                    } else {
                        alike = t.alike;
                    }
                    Mark mark = new Mark(t.vIndex, i, alike);
                    scores.marks.add(mark);
                }
            }

            if (scores.marks.size() == 0) {
                continue;
            }

            scores.marks.sort((o1, o2) -> {
                if (o1.vIndex > o2.vIndex) {
                    return 1;
                } else if (o1.vIndex < o2.vIndex) {
                    return -1;
                }
                return 0;
            });


            scores.score = mMdRater.scoring(scores);
            scoresArrayList.add(scores);
        }

        List<SearchResult> searchList = new ArrayList<>(scoresArrayList.size());

        for (int i = 0; i < scoresArrayList.size(); i++) {
            Scores scores = scoresArrayList.get(i);
            searchList.add(new SearchResult(scores.searchString, 0, scores.score));
        }

        return searchList;
    }


    private static class TargetNode {

        public final int unicode;

        public final int vIndex;

        public final float alike;

        TargetNode(int u, int v, float a) {
            unicode = u;
            vIndex = v;
            alike = a;
        }
    }

    private static class Scores extends MRecord {
        public final String searchString;

        public float score;

        public Scores(String s, int v, int w) {
            super(v, w);
            searchString = s;
        }
    }
}
