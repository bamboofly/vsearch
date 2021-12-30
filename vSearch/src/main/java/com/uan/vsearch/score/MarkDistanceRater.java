package com.uan.vsearch.score;

import com.uan.vsearch.Hit;
import com.uan.vsearch.WordTarget;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class MarkDistanceRater implements IAlikeRater {

    /**
     * 最大搜索输入字符数
     */
    public static final int MAX_INPUT = 256;

    private static class MBitmap {


        private final int[] array;

        private final int capacity;

        public int markSize;

        public MBitmap(int capacity) {
            this.capacity = capacity;
            int size = capacity / 32 + 1;
            array = new int[size];
        }


        public void mark(int index) {
            if (index < 0 || index >= capacity) {
                throw new RuntimeException("mark index out of range capacity " + capacity + ", index " + index);
            }

            int intIndex = index / 32;
            int offset = index % 32;

            if (((array[intIndex] >> offset) & 0x1) == 0) {
                array[intIndex] |= (1 << offset);
                markSize++;
            }
        }

        public boolean isMark(int index) {
            if (index < 0 || index >= capacity) {
                return false;
            }

            int intIndex = index / 32;
            int offset = index % 32;

            return ((array[intIndex] >> offset) & 0x1) != 0;
        }

        public MBitmap clone() {
            MBitmap bitmap = new MBitmap(capacity);
            int len = array.length;
            for (int i = 0; i < len; i++) {
                bitmap.array[i] = array[i];
            }
            bitmap.markSize = this.markSize;
            return bitmap;
        }
    }

    private static class Step {
        public final int wIndex;

        public final int vIndex;

        public final float score;

        public final MBitmap wBitmap;

        public final MBitmap vBitmap;

        public Step(int v, int w, float s, MBitmap wBitmap, MBitmap vBitmap) {
            vIndex = v;
            wIndex = w;
            score = s;
            this.wBitmap = wBitmap;
            this.vBitmap = vBitmap;
            wBitmap.mark(w);
            vBitmap.mark(v);
        }

        public Step(int wLen, int vLen) {
            vIndex = 0;
            wIndex = 0;
            score = 0;
            wBitmap = new MBitmap(wLen);
            vBitmap = new MBitmap(vLen);
        }
    }

    private static class SNode {
        private static final int STEP_LIST_INIT_SIZE = 128;

        public SNode pre;

        public SNode next;

        public int vIndex;

        public final ArrayList<Step> steps = new ArrayList<>(STEP_LIST_INIT_SIZE);
    }

    private static class MarkStack {
        private final int[] mMarArray;

        private int mTop;

        public MarkStack(int size) {
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

    private final SNode mSNode;

    public MarkDistanceRater() {
        SNode s1 = new SNode();
        SNode s2 = new SNode();
        SNode s3 = new SNode();

        s1.next = s2;
        s2.next = s3;
        s3.next = s1;

        s1.pre = s3;
        s3.pre = s2;
        s2.pre = s1;

        mSNode = s1;
    }

    private void resetSNodes(SNode sNode) {
        SNode node = sNode;
        for (int i = 0; i < 3; i++) {
            node.steps.clear();
            node.vIndex = -1;

            node = node.next;
        }
    }

    private final MarkStack mMarkStack = new MarkStack(256);

    private void advanceScore(Scores scores) {

        LinkedList<Hit> hits = scores.hits;

        int inputLength = scores.searchLength;
        int nameLength = scores.nameLength;

        if (inputLength > 256) {
            return;
        }

        mMarkStack.clear();

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
                    mMarkStack.put(preVIndex, maxWIndex);
                    wMarkBitmap.mark(maxWIndex);
                }

                maxScore = 0;
                maxWIndex = -1;
            }

            WordTarget wordTarget = hit.target;
            LinkedList<Integer> wordIndexList = wordTarget.getWordIndexList();

            for (Integer wIndex : wordIndexList) {

                int findIndex = mMarkStack.find(wIndex);
                int startVIndex = mMarkStack.getV(findIndex);
                int startWIndex = mMarkStack.getW(findIndex);

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

    private void fullScore(Scores scores) {

        LinkedList<Hit> hits = scores.hits;

        SNode curNode = mSNode;
        resetSNodes(curNode);


        int nameLength = scores.nameLength;

        int inputLength = scores.searchLength;

        curNode.vIndex = hits.getFirst().vIndex;

        curNode.pre.steps.add(
                new Step(nameLength,
                        inputLength));

        for (Hit hit : hits) {

            if (curNode.vIndex != hit.vIndex) {
                curNode = curNode.next;
                curNode.steps.clear();
                curNode.vIndex = hit.vIndex;

                List<Step> preNodeSteps = curNode.pre.steps;
                Step maxStep = findMaxScoreStep(preNodeSteps, inputLength, nameLength);
                if (maxStep != null) {
                    curNode.steps.add(maxStep);
                }
            }

            WordTarget target = hit.target;
            for (Integer i : target.getWordIndexList()) {

                float maxStepScore = 0;
                ArrayList<Step> steps = curNode.pre.steps;

                Step maxScoreStep = null;
                float maxFactorScore = Float.MIN_VALUE;
                for (Step step : steps) {
                    // 被匹配字已经被命中过，不再给加分
                    if (step.wBitmap.isMark(i)) {
                        continue;
                    }

                    int wForward = i - step.wIndex;
                    int vForward = curNode.vIndex - step.vIndex;
                    int d = Math.abs(vForward - wForward);

                    // 如果这个字已经被命中过，再次命中时需要降低这次命中的影响
                    // 2021/12/26修改，一个音只能命中一个字
                    float s = (1.0f / (1 + d)) * hit.alike/* / (step.wBitmap.isMark(i) ? 2 : 1)*/;
                    s += step.score;

//                        float vHitCount = step.vBitmap.markSize + (step.vBitmap.isMark(hit.vIndex) ? 0 : 1);
                    float wHitCount = step.wBitmap.markSize + 1/*(step.wBitmap.isMark(i) ? 0 : 1)*/;
                    float factorScore = s * ((wHitCount / inputLength) * (wHitCount / nameLength));
                    if (factorScore > maxFactorScore) {
                        maxStepScore = s;
                        maxScoreStep = step;
                        maxFactorScore = factorScore;
                    }
                }

                if (maxScoreStep != null) {
                    curNode.steps.add(
                            new Step(hit.vIndex,
                                    i,
                                    maxStepScore,
                                    maxScoreStep.wBitmap.clone(),
                                    maxScoreStep.vBitmap.clone()));
                }
            }
        }

        scores.score = findMaxScore(curNode.steps, nameLength, inputLength);
    }

    private float findMaxScore(List<Step> steps, int nl, int il) {
        float maxScore = Float.MIN_VALUE;
        for (Step step : steps) {
            float score = step.score * (((float) step.wBitmap.markSize / il)
                    * ((float) step.wBitmap.markSize / nl));
            if (score > maxScore) {
                maxScore = score;
            }
        }
        return maxScore;
    }

    private Step findMaxScoreStep(List<Step> steps, int nl, int il) {
        float maxStepScore = Float.MIN_VALUE;
        Step maxStep = null;
        for (Step step : steps) {
            float score = step.score * (((float) step.wBitmap.markSize / il)
                    * ((float) step.wBitmap.markSize / nl));
            if (score > maxStepScore) {
                maxStepScore = score;
                maxStep = step;
            }
        }
        return maxStep;
    }

    @Override
    public void scoring(Scores scores) {
//        simpleScore(scores);
//        fullScore(scores);
        advanceScore(scores);
    }

    private void simpleScore(Scores scores) {
        LinkedList<Hit> hits = scores.hits;


        float score = 0;
        Hit first = hits.getFirst();

        int currentTargetIndex = first.target.getFirstIndex();

        int currentVoiceIndex = first.vIndex;

        int vForward = 0;

        int wForward = 0;

        HashSet<Integer> hashSet = new HashSet<>();
        int nameLength = scores.nameLength;
        for (Hit hit : hits) {

            vForward = hit.vIndex - currentVoiceIndex;

            WordTarget target = hit.target;

            int min = Integer.MAX_VALUE;
            int minIndex = 0;
            for (Integer i : target.getWordIndexList()) {

                wForward = i - currentTargetIndex;

                int d = Math.abs(vForward - wForward);
                if (d < min) {
                    min = d;
                    minIndex = i;
                }
            }

            score += (1.0f / (1 + min)) * hit.alike;

            currentTargetIndex = minIndex;
            currentVoiceIndex = hit.vIndex;
            hashSet.add(currentTargetIndex);
        }

//        float f = (((float) hits.size() + hashSet.size()) / (scores.searchLength + scores.nameLength - hashSet.size()));
        float f = (float) hits.size() / scores.searchLength;
        score = score * f;

        scores.score = score /*/ scores.nameLength*/;
    }
}
