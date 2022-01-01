package com.uan.vsearch.score;

import com.uan.vsearch.Hit;
import com.uan.vsearch.WordTarget;

import java.util.LinkedList;

public class MarkDistanceRater implements IAlikeRater {

    /**
     * 最大搜索输入字符数
     */
    public static final int MAX_INPUT = 256;


    private static class HistoryStack {
        private final int[] mMarArray;

        private int mTop;

        public HistoryStack(int size) {
            mMarArray = new int[(size + 1) * 2];
            clear();
        }

        public void clear() {
            mMarArray[0] = -1;
            mMarArray[1] = -1;
            mTop = 0;
        }

        public int find(int wIndex) {

            int i = mTop;
            for (; i >= 0; i -= 2) {
                if (mMarArray[i + 1] < wIndex) {
                    break;
                }
            }

            return i;
        }

        public void put(int vIndex, int wIndex) {
            mTop += 2;
            mMarArray[mTop] = vIndex;
            mMarArray[mTop + 1] = wIndex;
        }

        public int getV(int index) {
            return mMarArray[index];
        }

        public int getW(int index) {
            return mMarArray[index + 1];
        }
    }

    private final HistoryStack mHistoryStack = new HistoryStack(MAX_INPUT);

    private void advanceScore(Scores scores) {

        LinkedList<Hit> hits = scores.hits;

        int inputLength = scores.searchLength;
        int nameLength = scores.nameLength;

        if (inputLength > MAX_INPUT) {
            return;
        }

        mHistoryStack.clear();

        float sumScore = 0;
        MBitmap wMarkBitmap = new MBitmap(nameLength);

        float maxScore = 0;
        int maxWIndex = -1;

        int preVIndex = 0;

        for (Hit hit : hits) {
            int vIndex = hit.vIndex;
            if (preVIndex != vIndex) {
                sumScore += maxScore;

                if (maxWIndex >= 0) {
                    mHistoryStack.put(preVIndex, maxWIndex);
                    wMarkBitmap.mark(maxWIndex);
                }

                maxScore = 0;
                maxWIndex = -1;
            }

            WordTarget wordTarget = hit.target;
            LinkedList<Integer> wordIndexList = wordTarget.getWordIndexList();

            for (Integer wIndex : wordIndexList) {

                int findIndex = mHistoryStack.find(wIndex);
                int startVIndex = mHistoryStack.getV(findIndex);
                int startWIndex = mHistoryStack.getW(findIndex);

                int wForward = wIndex - startWIndex;
                int vForward = vIndex - startVIndex;
                int d = Math.abs(vForward - wForward);

                int d2 = (wForward == 1 && wForward == vForward) ? 5 : (5 + wForward + vForward);

                float s = (2.0f / (2 + d)) * hit.alike * (5.f / (d2));

                if (s > maxScore) {
                    maxScore = s;
                    maxWIndex = wIndex;
                } else if (s == maxScore && !wMarkBitmap.isMark(wIndex)) {
                    maxScore = s;
                    maxWIndex = wIndex;
                }
            }

            preVIndex = vIndex;

        }

        if (maxWIndex >= 0) {
            sumScore += maxScore;
            wMarkBitmap.mark(maxWIndex);
        }

        int unMarkSize = (nameLength - wMarkBitmap.markSize);

        float finalScore = (sumScore / inputLength) * wMarkBitmap.markSize - (0.5f - (1f / (2 + unMarkSize)))/* / nameLength*/;

        scores.score = finalScore;
    }

    @Override
    public void scoring(Scores scores) {
        advanceScore(scores);
    }
}
