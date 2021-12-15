package com.uan.vsearch.score;

import com.uan.vsearch.Hit;

import java.util.LinkedList;

public class Scores {
    public final int index;
    public final int nameLength;
    public final int searchLength;
    public float score;
    public final LinkedList<Hit> hits = new LinkedList<>();

    public Scores(int i, int nl, int sl) {
        index = i;
        nameLength = nl;
        searchLength = sl;
    }
}
