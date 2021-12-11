package com.uan.vsearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

public class Scoring {

    private final static float CONTINUE_ALL_POSITIVE_WEIGHTING = 1.5f;

    private final static float CONTINUE_ALL_REVERSE_WEIGHTING = 1.2f;

    private final static float CONTINUE_VOICE_POSITIVE_WEIGHTING = 1.2f;

    private final static float CONTINUE_VOICE_REVERSE_WEIGHTING = 1f;

    private final static float CONTINUE_ALL_POSITIVE_INITIAL_VALUE = 1.3f;

    private final static float CONTINUE_ALL_REVERSE_INITIAL_VALUE = 1.1f;

    public void scoring(Scores scores) {

        LinkedList<Hit> hits = scores.hits;

        boolean hasRepeatVIndex = false;
        boolean hasMultiHitIndex = false;
        HashSet<Integer> vIndexSet = new HashSet<>();
        for (Hit hit : hits) {
            if (hit.target.getIndexSize() > 1) {
                hasMultiHitIndex = true;
            } else if (vIndexSet.contains(hit.vIndex)) {
                hasRepeatVIndex = true;
            }
            vIndexSet.add(hit.vIndex);
        }

        if (hasMultiHitIndex || hasRepeatVIndex) {
            scores.score = scoringRecursion(scores);
            return;
        }

        float score = getScore(hits);

        scores.score = score;
    }

    private float getScore(LinkedList<Hit> hits) {
        TreeSet<Hit> hitTreeSet = new TreeSet<>((o1, o2) -> {
            if (o1.hitIndex > o2.hitIndex) {
                return 1;
            } else if (o1.hitIndex < o2.hitIndex) {
                return -1;
            }
            return 0;
        });

        hitTreeSet.addAll(hits);

        float score = 0;
        float tempScore = 0;
        int currentWordIndex = 0;
        int preWordIndex = -1;
        int currentVoiceIndex = 0;
        int preVoiceIndex = -1;

        boolean voicePositiveContinueFlag = false;
        boolean wordPositiveContinueFlag = false;
        boolean voiceReverseContinueFlag = false;
        boolean wordReverseContinueFlag = false;

        float allPositiveContinueWeight = CONTINUE_ALL_POSITIVE_INITIAL_VALUE;
        float allReverseContinueWeight = CONTINUE_ALL_REVERSE_INITIAL_VALUE;
        float voicePositiveContinueWeight = 1;
        float wordPositiveContinueWeight = 1;
        float voiceReverseContinueWeight = 1;
        float wordReverseContinueWeight = 1;

        float continueWeight = 1;

        boolean firstFlag = true;
        Iterator<Hit> hitIterator = hitTreeSet.iterator();

        while (hitIterator.hasNext()) {
            Hit hit = hitIterator.next();
            currentVoiceIndex = hit.vIndex;
            currentWordIndex = hit.hitIndex;

            if (!firstFlag) {
                if (preVoiceIndex + 1 == currentVoiceIndex) {
                    voicePositiveContinueFlag = true;
                } else {
                    voicePositiveContinueFlag = false;
                }

                if (preVoiceIndex - 1 == currentVoiceIndex) {
                    voiceReverseContinueFlag = true;
                } else {
                    voiceReverseContinueFlag = false;
                }

                if (preWordIndex + 1 == currentWordIndex) {
                    wordPositiveContinueFlag = true;
                } else {
                    wordPositiveContinueFlag = false;
                }

                if (preWordIndex - 1 == currentWordIndex) {
                    wordReverseContinueFlag = true;
                } else {
                    wordReverseContinueFlag = false;
                }
            }

            if (firstFlag) {
                tempScore = hit.alike;
            } else {

                if (voicePositiveContinueFlag && wordPositiveContinueFlag) {
                    allPositiveContinueWeight = Math.max(allPositiveContinueWeight, voicePositiveContinueWeight) * CONTINUE_ALL_POSITIVE_WEIGHTING;
                    voicePositiveContinueWeight = voiceReverseContinueWeight * CONTINUE_VOICE_POSITIVE_WEIGHTING;
                    tempScore += hit.alike * Math.max(allPositiveContinueWeight, voicePositiveContinueWeight);
                    voicePositiveContinueWeight = 1f;
                    allReverseContinueWeight = CONTINUE_ALL_REVERSE_INITIAL_VALUE;
                } else if (voiceReverseContinueFlag && wordReverseContinueFlag) {
                    allReverseContinueWeight = Math.max(allReverseContinueWeight, voiceReverseContinueWeight) * CONTINUE_ALL_REVERSE_WEIGHTING;
                    tempScore += hit.alike * continueWeight;
                    voiceReverseContinueWeight = 1f;
                    allPositiveContinueWeight = CONTINUE_ALL_POSITIVE_INITIAL_VALUE;
                } else if (voicePositiveContinueFlag) {
                    voicePositiveContinueWeight = voicePositiveContinueWeight * CONTINUE_VOICE_POSITIVE_WEIGHTING;
                    tempScore += hit.alike * voicePositiveContinueWeight;
                    allPositiveContinueWeight = CONTINUE_ALL_POSITIVE_INITIAL_VALUE;
                    allReverseContinueWeight = CONTINUE_ALL_REVERSE_INITIAL_VALUE;
                } else if (voiceReverseContinueFlag) {
                    voiceReverseContinueWeight = voiceReverseContinueWeight * CONTINUE_VOICE_REVERSE_WEIGHTING;
                    tempScore += hit.alike * voiceReverseContinueWeight;
                    allPositiveContinueWeight = CONTINUE_ALL_POSITIVE_INITIAL_VALUE;
                    allReverseContinueWeight = CONTINUE_ALL_REVERSE_INITIAL_VALUE;
                } else {
                    allPositiveContinueWeight = CONTINUE_ALL_POSITIVE_INITIAL_VALUE;
                    allReverseContinueWeight = CONTINUE_ALL_REVERSE_INITIAL_VALUE;
                    voicePositiveContinueWeight = 1f;
                    score += tempScore;
                    tempScore = hit.alike;
                }
            }

            preVoiceIndex = currentVoiceIndex;
            preWordIndex = currentWordIndex;
            firstFlag = false;
        }

        score += tempScore;
        return score;
    }

    private float scoringRecursion(Scores scores) {
        LinkedList<Hit> hits = scores.hits;

        ArrayList<LinkedList<Hit>> dfHitList = new ArrayList<>();

        LinkedList<Hit> childrenList = null;
        HashMap<WordTarget, Integer> hashMap = new HashMap<>();
        int lackHitIndex = 0;
        int preVIndex = -1;
        for (Hit hit : hits) {
            int vIndex = hit.vIndex;
            if (!hashMap.containsKey(hit.target)) {
                hashMap.put(hit.target, hit.target.getIndexSize() - 1);
            } else {
                hashMap.put(hit.target, hashMap.get(hit.target) - 1);
            }

            if (vIndex != preVIndex) {
                childrenList = new LinkedList<>();
                dfHitList.add(childrenList);
            } else {
                if (childrenList == null) {
                    childrenList = new LinkedList<>();
                    dfHitList.add(childrenList);
                }
            }
            childrenList.add(hit);
            preVIndex = vIndex;
        }

        for (Integer value : hashMap.values()) {
            lackHitIndex += value;
        }
        lackHitIndex = Math.abs(lackHitIndex);

        LinkedList<Hit> queue = new LinkedList<>();
        return recursion(dfHitList, dfHitList.size(), 0, queue, lackHitIndex);
    }

    private float recursion(ArrayList<LinkedList<Hit>> hitList, int size, int arrayIndex,
                            LinkedList<Hit> queue, int lackHitIndex) {

        if (arrayIndex >= size) {
            return getScore(queue);
        } else {
            LinkedList<Hit> hitLinkedList = hitList.get(arrayIndex);
            float score = 0;
            for (Hit hit : hitLinkedList) {
                int indexSize = hit.target.getIndexSize();
                // 被搜索文字中没有重复音，直接处理
                if (indexSize == 1) {
                    hit.hitIndex = hit.target.getFirstIndex();
                    queue.addLast(hit);
                    score = Math.max(score, recursion(hitList, size, arrayIndex + 1, queue, lackHitIndex));
                    queue.removeLast();
                } else if (indexSize > 1) {
                    LinkedList<Integer> wordIndexList = hit.target.getWordIndexList();
                    // 处理被搜索文字中有重复音的情况
                    for (Integer index : wordIndexList) {
                        boolean repeat = false;
                        for (Hit preHit : queue) {
                            if (index == preHit.hitIndex) {
                                repeat = true;
                                break;
                            }
                        }
                        if (repeat) {
                            continue;
                        }

                        hit.hitIndex = index;
                        queue.addLast(hit);
                        score = Math.max(score, recursion(hitList, size, arrayIndex + 1, queue, lackHitIndex));
                        queue.removeLast();
                    }

                    // 丢掉这个Hit计算，有可能出现前面丢掉将hitIndex让给后面的Hit评分更高
                    // lackHitIndex表示搜索字符串命中被搜索字符串的次数减去被命中字符的个数
                    if (lackHitIndex > arrayIndex) {
                        score = Math.max(score, recursion(hitList, size, arrayIndex + 1, queue, lackHitIndex));
                        count++;
                    }
                }

            }
            return score;
        }
    }

    private int count = 0;
}
