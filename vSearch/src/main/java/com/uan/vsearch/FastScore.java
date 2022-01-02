package com.uan.vsearch;

import com.uan.vsearch.score.MRecord;

class FastScore extends MRecord {

    public final int stringMapIndex;

    public float score;

    public FastScore(int i, int v, int w) {
        super(v, w);
        stringMapIndex = i;
    }
}
