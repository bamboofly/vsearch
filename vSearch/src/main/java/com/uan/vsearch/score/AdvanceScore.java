package com.uan.vsearch.score;

import com.uan.vsearch.Hit;
import com.uan.vsearch.WordTarget;

import java.util.LinkedList;

public class AdvanceScore {


    private static class Arrow {

        public final Arrow pre;

        public final Arrow next;

        public final Hit hit;

        public final int index;

        public final LinkedList<Target> targets = new LinkedList<>();

        public Target select;

        public Arrow(Arrow p, Arrow n, Hit h, int i) {
            pre = p;
            next = n;
            hit = h;
            index = i;
        }
    }

    private static class Target {
        public final Target pre;

        public final Target next;

        public final WordTarget wordTarget;

        public final int index;

        public final LinkedList<Arrow> arrows = new LinkedList<>();

        public Arrow select;

        public Target(Target p, Target n, WordTarget w, int i) {
            pre = p;
            next = n;
            wordTarget = w;
            index = i;
        }
    }

    private final static Arrow ARROW_HEAD = new Arrow(null, null, null, Integer.MIN_VALUE);

    private final static Arrow ARROW_TAIL = new Arrow(null, null, null, Integer.MAX_VALUE);

    private final static Target TARGET_HEAD = new Target(null, null, null, Integer.MIN_VALUE);

    private final static Target TARGET_TAIL = new Target(null, null, null, Integer.MAX_VALUE);
}
