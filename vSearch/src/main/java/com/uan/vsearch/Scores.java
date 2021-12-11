package com.uan.vsearch;

import java.util.LinkedList;

public class Scores {
    final int index;
    final int nameLength;
    final int searchLength;
    float score;
    final LinkedList<Hit> hits = new LinkedList<>();

    public Scores(int i, int nl, int sl) {
        index = i;
        nameLength = nl;
        searchLength = sl;
    }
}
