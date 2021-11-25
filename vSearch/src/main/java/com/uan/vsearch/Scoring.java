package com.uan.vsearch;

public class Scoring {


    private final float CONTINUE_WORD_POSITIVE_WEIGHTING = 2.0f;

    private final float CONTINUE_WORD_REVERSE_WEIGHTING = 1.5f;


    public void scoring(Hit hit) {

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

    }

    public static class Hit {
        int index;
        int length;
        float score;
        int[] hits;

        public Hit(int len) {
            hits = new int[len];
        }
    }
}
