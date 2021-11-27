package com.uan.vsearch;

import java.util.Collections;
import java.util.LinkedList;

public class WordTarget {
    public final int nameIndex;

    public final int nameLength;

    private LinkedList<Integer> wordIndexList = new LinkedList<>();

    public WordTarget(int index, int length) {
        nameIndex = index;
        nameLength = length;
    }

    public void addIndex(int index) {
        wordIndexList.add(index);
    }

    public int getIndexSize() {
        return wordIndexList.size();
    }

    public int getFirstIndex() {
        return wordIndexList.getFirst();
    }

    public LinkedList<Integer> getWordIndexList() {
        LinkedList<Integer> list = new LinkedList<>();
        Collections.copy(list, wordIndexList);
        return list;
    }
}
