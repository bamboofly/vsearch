package com.uan.vsearch.score;

import com.uan.vsearch.Hit;
import com.uan.vsearch.WordTarget;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

public class MarkDistanceRater implements IAlikeRater {

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
                throw new RuntimeException("mark index out of range");
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
//            int[] a = bitmap.array;
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
    }

    private static class SNode {
        private static final int STEP_LIST_INIT_SIZE = 128;

        public SNode pre;

        public SNode next;

        public int vIndex;

        public final ArrayList<Step> steps = new ArrayList<>(STEP_LIST_INIT_SIZE);
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

    private void fullScore(Scores scores) {


        LinkedList<Hit> hits = scores.hits;

        SNode curNode = mSNode;
        resetSNodes(curNode);


        Hit first = hits.getFirst();

        curNode.vIndex = first.vIndex;

        int vForward = 0;

        int wForward = 0;

        for (Hit hit : hits) {

            if (curNode.vIndex != hit.vIndex) {
                curNode = curNode.next;
                curNode.steps.clear();
                curNode.vIndex = hit.vIndex;

                if (curNode.pre.steps.size() > 0) {
                    float maxStepScore = 0;
                    Step maxStep = null;
                    for (Step step : curNode.pre.steps) {
                        float score = step.score * (((float) step.wBitmap.markSize / scores.searchLength)
                                * ((float) step.wBitmap.markSize / scores.nameLength));
                        if (score > maxStepScore) {
                            maxStepScore = score;
                            maxStep = step;
                        }
                    }
                    if (maxStep != null) {
                        curNode.steps.add(maxStep);
                    }
                }
            }

            WordTarget target = hit.target;
            for (Integer i : target.getWordIndexList()) {

                float maxStepScore = -1;
                ArrayList<Step> steps = curNode.pre.steps;
                if (steps.size() > 0) {
                    Step maxScoreStep = null;
                    float maxFactorScore = 0;
                    for (Step step : steps) {
                        wForward = i - step.wIndex;
                        vForward = curNode.vIndex - step.vIndex;
                        int d = Math.abs(vForward - wForward);

                        // 如果这个字已经被命中过，再次命中时需要降低这次命中的影响
                        float s = (1.0f / (1 + d)) * hit.alike / (step.wBitmap.isMark(i) ? 2 : 1);
                        s += step.score;

                        float vHitCount = step.vBitmap.markSize + (step.vBitmap.isMark(hit.vIndex) ? 0 : 1);
                        float wHitCount = step.wBitmap.markSize + (step.wBitmap.isMark(i) ? 0 : 1);
                        float factorScore = s * ((wHitCount / scores.searchLength) * (wHitCount / scores.nameLength));
                        if (factorScore > maxFactorScore) {
                            maxStepScore = s;
                            maxScoreStep = step;
                            maxFactorScore = factorScore;
                        }
                    }
                    Step newStep = new Step(hit.vIndex, i, maxStepScore, maxScoreStep.wBitmap.clone(), maxScoreStep.vBitmap.clone());

                    curNode.steps.add(newStep);
                } else {
                    MBitmap wBitmap = new MBitmap(scores.nameLength);
                    MBitmap vBitmap = new MBitmap(scores.searchLength);

                    curNode.steps.add(new Step(hit.vIndex, i, hit.alike, wBitmap, vBitmap));
                }
            }
        }

        float maxScore = 0;
        for (Step step : curNode.steps) {
            float score = step.score * (((float) step.wBitmap.markSize / scores.searchLength)
                    * ((float) step.wBitmap.markSize / scores.nameLength));
            if (score > maxScore) {
                maxScore = score;
            }
        }

//        float factor = (((float) hashSet.size()) / (scores.nameLength)) * (((float) hits.size()) / scores.searchLength);

        scores.score = maxScore;
    }

    @Override
    public void scoring(Scores scores) {
//        simpleScore(scores);
        fullScore(scores);
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
