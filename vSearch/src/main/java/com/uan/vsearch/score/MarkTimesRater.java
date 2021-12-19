package com.uan.vsearch.score;

import android.util.Log;

import com.uan.vsearch.Hit;
import com.uan.vsearch.WordTarget;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

public class MarkTimesRater implements IAlikeRater {
    private static final String TAG = "Scoring";

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
            count = 0;
            scores.score = scoringRecursion(scores);
            Log.e("lianghuan", "count = " + count);
        } else {
            scores.score = getScore(hits);
        }
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
        HashSet<WordTarget> wordTargetHashSet = new HashSet<>();
        int canHitCount = 0;
        int preVIndex = -1;
        for (Hit hit : hits) {
            int vIndex = hit.vIndex;
            wordTargetHashSet.add(hit.target);

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

        Iterator<WordTarget> wordTargetIterator = wordTargetHashSet.iterator();
        while (wordTargetIterator.hasNext()) {
            WordTarget wordTarget = wordTargetIterator.next();
            canHitCount += wordTarget.getIndexSize();
        }

        LinkedList<Hit> queue = new LinkedList<>();
        return recursion(dfHitList, dfHitList.size(), 0, queue, canHitCount);
    }

    private float recursion(ArrayList<LinkedList<Hit>> hitList, int size, int arrayIndex,
                            LinkedList<Hit> queue, int canHitCount) {

        if (arrayIndex >= size || queue.size() >= canHitCount) {
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
                    score = Math.max(score, recursion(hitList, size, arrayIndex + 1, queue, canHitCount));
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
                        score = Math.max(score, recursion(hitList, size, arrayIndex + 1, queue, canHitCount));
                        queue.removeLast();
                    }

                    // 丢掉这个Hit计算，有可能出现前面丢掉将hitIndex让给后面的Hit评分更高
                    // 如果剩下的Hit数量大于能命中的数量时才进行计算
                    if (size - arrayIndex > (canHitCount - queue.size())) {
                        score = Math.max(score, recursion(hitList, size, arrayIndex + 1, queue, canHitCount));
                        count++;
                    }
                }

            }
            return score;
        }
    }

    private int count = 0;
}
