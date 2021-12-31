package com.uan.vsearch.score;

public class MBitmap {

    private final int[] array;

    private final int capacity;

    public int markSize;

    public MBitmap(int capacity) {
        this.capacity = capacity;
        int size = capacity / 32 + 1;
        array = new int[size];
    }


    public void mark(int index) {
        if (index < 0 || index >= capacity) {
            throw new RuntimeException("mark index out of range capacity " + capacity + ", index " + index);
        }

        int intIndex = index / 32;
        int offset = index % 32;

        if (((array[intIndex] >> offset) & 0x1) == 0) {
            array[intIndex] |= (1 << offset);
            markSize++;
        }
    }

    public boolean isMark(int index) {
        if (index < 0 || index >= capacity) {
            return false;
        }

        int intIndex = index / 32;
        int offset = index % 32;

        return ((array[intIndex] >> offset) & 0x1) != 0;
    }

    public MBitmap clone() {
        MBitmap bitmap = new MBitmap(capacity);
        int len = array.length;
        for (int i = 0; i < len; i++) {
            bitmap.array[i] = array[i];
        }
        bitmap.markSize = this.markSize;
        return bitmap;
    }
}
