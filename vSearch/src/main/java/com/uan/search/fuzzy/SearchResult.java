package com.uan.search.fuzzy;

public class SearchResult {
    private final String source;

    private final int index;

    private final float score;

    public SearchResult(String s, int i, float f) {
        source = s;
        index = i;
        score = f;
    }

    public String getString() {
        return source;
    }

    public int getIndex() {
        return index;
    }

    public float getScore() {
        return score;
    }
}
