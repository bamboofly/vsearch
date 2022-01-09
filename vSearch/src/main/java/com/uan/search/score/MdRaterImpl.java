package com.uan.search.score;

import java.util.ArrayList;

/**
 * 标记距离相似算法实现类
 */
public class MdRaterImpl implements IMdRater {

    /**
     * 最大搜索输入字符数
     */
    public static final int MAX_INPUT = 256;

    /**
     * 标记距离历史栈，用于查找当前被标记字符对的前面最近的被标记的字符对
     */
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

    private float advanceScore(ArrayList<Mark> marks, int vLen, int wLen) {

        if (vLen > MAX_INPUT) {
            return 0;
        }

        mHistoryStack.clear();

        float sumScore = 0;
        MBitmap wMarkBitmap = new MBitmap(wLen);

        float maxScore = 0;
        int maxWIndex = -1;

        int preVIndex = 0;

        int vLenAddWLen = vLen + wLen;

        int size = marks.size();

        for (int i = 0; i < size; i++) {
            Mark mark = marks.get(i);
            int vIndex = mark.vIndex;
            int wIndex = mark.wIndex;
            if (preVIndex != vIndex) {
                sumScore += maxScore;

                if (maxWIndex >= 0) {
                    mHistoryStack.put(preVIndex, maxWIndex);
                    wMarkBitmap.mark(maxWIndex);
                }

                maxScore = 0;
                maxWIndex = -1;
            }

            int findIndex = mHistoryStack.find(wIndex);
            int startVIndex = mHistoryStack.getV(findIndex);
            int startWIndex = mHistoryStack.getW(findIndex);

            int wForward = wIndex - startWIndex;
            int vForward = vIndex - startVIndex;
            int d = Math.abs(vForward - wForward);

            int d2 = (wForward == 1 && wForward == vForward) ? vLenAddWLen : (vLenAddWLen + wForward + vForward);

            float s = (2.0f / (2 + d)) * mark.alike * ((float) vLenAddWLen / (d2));

            if (s > maxScore) {
                maxScore = s;
                maxWIndex = wIndex;
            } else if (s == maxScore && !wMarkBitmap.isMark(wIndex)) {
                maxScore = s;
                maxWIndex = wIndex;
            }

            preVIndex = vIndex;

        }

        if (maxWIndex >= 0) {
            sumScore += maxScore;
            wMarkBitmap.mark(maxWIndex);
        }

        int unMarkSize = (wLen - wMarkBitmap.markSize);

        float finalScore = (sumScore / vLen) * wMarkBitmap.markSize - (0.5f - (1f / (2 + unMarkSize)))/* / nameLength*/;

        return finalScore;
    }

    @Override
    public float scoring(MRecord record) {
        return advanceScore(record.marks, record.vLen, record.wLen);
    }
}
