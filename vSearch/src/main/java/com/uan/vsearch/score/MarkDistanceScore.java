package com.uan.vsearch.score;

import com.uan.vsearch.Hit;
import com.uan.vsearch.WordTarget;

import java.util.HashSet;
import java.util.LinkedList;

public class MarkDistanceScore implements IScore {

    private static class Step {
        public final int wIndex;

        public final int vIndex;

        public final float score;

        public Step(int v, int w, float s) {
            vIndex = v;
            wIndex = w;
            score = s;
        }
    }

    private static class SNode {
        public SNode pre;

        public SNode next;

        public int vIndex;

        public final LinkedList<Step> steps = new LinkedList<>();
    }

    private final SNode mSNode;

    public MarkDistanceScore() {
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
        SNode head = sNode;
        head.steps.clear();
        head.vIndex = 0;

        head = head.next;
        head.steps.clear();
        head.vIndex = 0;

        head = head.next;
        head.steps.clear();
        head.vIndex = 0;
    }

    private void fullScore(Scores scores) {


        LinkedList<Hit> hits = scores.hits;

        SNode curNode = mSNode;
        resetSNodes(curNode);


        Hit first = hits.getFirst();

        curNode.vIndex = first.vIndex;

        int vForward = 0;

        int wForward = 0;


        HashSet<Integer> hashSet = new HashSet<>();
        for (Hit hit : hits) {

            if (curNode.vIndex != hit.vIndex) {
                curNode = curNode.next;
                curNode.steps.clear();
                curNode.vIndex = hit.vIndex;

                if (curNode.pre.steps.size() > 0) {
                    float maxStepScore = 0;
                    Step maxStep = null;
                    for (Step step : curNode.pre.steps) {
                        if (step.score > maxStepScore) {
                            maxStepScore = step.score;
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
                LinkedList<Step> steps = curNode.pre.steps;
                if (steps.size() > 0) {
                    for (Step step : steps) {
                        wForward = i - step.wIndex;
                        vForward = curNode.vIndex - step.vIndex;
                        int d = Math.abs(vForward - wForward);

                        float s = (1.0f / (1 + d)) * hit.alike;
                        s += step.score;
                        if (s > maxStepScore) {
                            maxStepScore = s;
                        }
                    }
                    hashSet.add(i);
                    curNode.steps.add(new Step(hit.vIndex, i, maxStepScore));
                } else {
                    curNode.steps.add(new Step(hit.vIndex, i, hit.alike));
                    hashSet.add(i);
                }
            }
        }

        float maxScore = 0;
        for (Step step : curNode.steps) {
            if (step.score > maxScore) {
                maxScore = step.score;
            }
        }

        float factor = (((float) hashSet.size()) / (scores.nameLength)) * (((float) hits.size()) / scores.searchLength);

        scores.score = maxScore * factor;
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
