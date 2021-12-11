package com.uan.vsearch;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

public class Scoring {

    private final float CONTINUE_ALL_POSITIVE_WEIGHTING = 1.5f;

    private final float CONTINUE_ALL_REVERSE_WEIGHTING = 1.2f;

    private final float CONTINUE_VOICE_POSITIVE_WEIGHTING = 1.2f;

    private final float CONTINUE_VOICE_REVERSE_WEIGHTING = 1f;

    private final float CONTINUE_ALL_POSITIVE_INITIAL_VALUE = 1.3f;

    private final float CONTINUE_ALL_REVERSE_INITIAL_VALUE = 1.1f;

    public void scoring(Scores scores) {

        LinkedList<Hit> hits = scores.hits;

        LinkedList<Hit> multiList = new LinkedList<>();
        TreeSet<Hit> hitTreeSet = new TreeSet<>(new Comparator<Hit>() {
            @Override
            public int compare(Hit o1, Hit o2) {
                if (o1.hitIndex > o2.hitIndex) {
                    return 1;
                } else if (o1.hitIndex < o2.hitIndex) {
                    return -1;
                }
                return 0;
            }
        });

        while (!hits.isEmpty()) {
            Hit remove = hits.remove();
            if (remove.target.getIndexSize() > 1) {
                multiList.add(remove);
            } else {
                remove.hitIndex = remove.target.getFirstIndex();
                hitTreeSet.add(remove);
            }
        }

        for (Hit multiHit : multiList) {
            LinkedList<Integer> wordIndexList = multiHit.target.getWordIndexList();

            boolean find = false;
            int unRepeatIndex = -1;
            Iterator<Hit> hitIterator = hitTreeSet.iterator();

            for (Integer index : wordIndexList) {
                boolean repeat = false;
                while (hitIterator.hasNext()) {
                    Hit oneHit = hitIterator.next();
                    if (index == oneHit.hitIndex) {
                        repeat = true;
                        break;
                    }
                }

                if (repeat) {
                    continue;
                }

                unRepeatIndex = index;

                hitIterator = hitTreeSet.iterator();
                while (hitIterator.hasNext()) {
                    Hit oneHit = hitIterator.next();
                    if (index == oneHit.hitIndex - 1) {
                        multiHit.hitIndex = index;
                        find = true;
                        break;
                    } else if (index == oneHit.hitIndex + 1) {
                        multiHit.hitIndex = index;
                        find = true;
                        break;
                    }
                }

                // 找到合适的索引后插入到对应的位置，而不是添加到头或尾，避免后面重新排序
                if (find) {
                    hitTreeSet.add(multiHit);
                    break;
                }
            }

            if (!find && unRepeatIndex != -1) {
                multiHit.hitIndex = unRepeatIndex;
                hitTreeSet.add(multiHit);
            }

        }

        // TODO 移除重复vIndex
//        HashSet<Integer> retainSet = new HashSet<>();
//        HashSet<Integer> removeSet = new HashSet<>();
//        Iterator<Hit> iterator = hitTreeSet.iterator();
//        while (iterator.hasNext()) {
//            Hit hit = iterator.next();
//            int vIndex = hit.vIndex;
//            int hitIndex = hit.hitIndex;
//            if (indexArray[vIndex] > 0) {
//                multiVoiceIndexTreeSet.add(hit);
//            } else {
//                indexArray[vIndex] = 1;
//            }
//        }
//        hitTreeSet.remove()

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

        scores.score = score;
    }

}
