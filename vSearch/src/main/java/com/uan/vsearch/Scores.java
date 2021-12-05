package com.uan.vsearch;

import java.util.LinkedList;

public class Scores {
    final int index;
    final int length;
    float score;
    final LinkedList<Hit> hits = new LinkedList<>();

    public Scores(int i, int l) {
        index = i;
        length = l;
    }
}
