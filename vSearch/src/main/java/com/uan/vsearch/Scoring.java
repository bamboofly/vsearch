package com.uan.vsearch;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

public class Scoring {


    private final float CONTINUE_WORD_POSITIVE_WEIGHTING = 2.0f;

    private final float CONTINUE_WORD_REVERSE_WEIGHTING = 1.5f;


/*    public void scoring(Hit hit) {

        final int length = hit.hits.length;
        final int[] hits = hit.hits;
        float score = 0;
        float tempScore = 1;
        int current = 0;
        int pre = -1;
        for (int i = 0; i < length; i++) {
            current = hits[i];
            if (current != 0) {
                if (current - 1 == pre) {
                    tempScore = tempScore * CONTINUE_WORD_POSITIVE_WEIGHTING;
                } else if (current + 1 == pre) {
                    tempScore = tempScore * CONTINUE_WORD_REVERSE_WEIGHTING;
                } else {
                    tempScore = 1;
                }
            } else {
                score += tempScore;
                tempScore = 1;
            }

            pre = current;
        }

        hit.score = score;

    }*/

    public void scoring(Scores scores) {

        LinkedList<Hit> hits = scores.hits;

        LinkedList<Hit> multiList = new LinkedList<>();
        TreeSet<Hit> hitTreeSet = new TreeSet<>(new Comparator<Hit>() {
            @Override
            public int compare(Hit o1, Hit o2) {
                if (o1.hitIndex > o2.hitIndex) {
                    return -1;
                } else if (o1.hitIndex < o2.hitIndex){
                    return 1;
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

//        Collections.sort(oneList, new Comparator<com.uan.vsearch.Hit>() {
//            @Override
//            public int compare(com.uan.vsearch.Hit o1, com.uan.vsearch.Hit o2) {
//                if (o1.target.getFirstIndex() > o2.target.getFirstIndex()) {
//                    return -1;
//                } else if (o1.target.getFirstIndex() < o2.target.getFirstIndex()){
//                    return 1;
//                }
//                return 0;
//            }
//        });

        for (Hit multiHit : multiList) {
            LinkedList<Integer> wordIndexList = multiHit.target.getWordIndexList();

            boolean find = false;
            int unRepeatIndex = -1;
            Iterator<Hit> hitIterator = hitTreeSet.iterator();

            for (Integer index : wordIndexList) {
                boolean repeat = false;
                while (hitIterator.hasNext()) {
                    Hit oneHit = hitIterator.next();
                    if (index == oneHit.target.getFirstIndex()) {
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
                    if (index == oneHit.target.getFirstIndex() - 1) {
                        multiHit.hitIndex = index;
                        find = true;
                        break;
                    } else if (index == oneHit.target.getFirstIndex() + 1) {
                        multiHit.hitIndex = index;
                        find = true;
                        break;
                    }
                }

                // 找到合适的索引后插入到对应的位置，而不是添加到头或尾，避免后面重新排序
                if (find) {
                    hitTreeSet.add(multiHit);
                }
            }

            if (!find && unRepeatIndex != -1) {
                multiHit.hitIndex = unRepeatIndex;
                hitTreeSet.add(multiHit);
            }

        }

        float score = 0;
        float tempScore = 1;
        int current = 0;
        int pre = -1;
        Iterator<Hit> hitIterator = hitTreeSet.iterator();
        while (hitIterator.hasNext()) {
            Hit hit = hitIterator.next();
            current = hit.hitIndex;
            if (current != 0) {
                if (current - 1 == pre) {
                    tempScore = hit.alike * tempScore * CONTINUE_WORD_POSITIVE_WEIGHTING;
                } else if (current + 1 == pre) {
                    tempScore = hit.alike * tempScore * CONTINUE_WORD_REVERSE_WEIGHTING;
                } else {
                    tempScore = hit.alike;
                }
            } else {
                score += tempScore;
                tempScore = 1;
            }

            pre = current;
        }

        scores.score = score / scores.length;
    }

}
