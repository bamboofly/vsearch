package com.uan.search.naive.score;

import com.uan.search.naive.Hit;
import com.uan.search.participle.WordTarget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;

public class AdvanceAlikeRater implements IAlikeRater {


    private static class Arrow {

        public Arrow pre;

        public Arrow next;

        public final Hit hit;

        public final int index;

        public final ArrayList<Target> targets = new ArrayList<>(4);

        public int select;

        public Arrow(Hit h, int i) {
            hit = h;
            index = i;
        }
    }

    private static class Target {
        public Target pre;

        public Target next;

        public final WordTarget wordTarget;

        public final int index;

        public final ArrayList<Arrow> arrows = new ArrayList<>();

        public int select;

        public Target(WordTarget w, int i) {
            wordTarget = w;
            index = i;
        }
    }

    private final static Arrow ARROW_HEAD = new Arrow(null, Integer.MIN_VALUE);

    private final static Arrow ARROW_TAIL = new Arrow(null, Integer.MAX_VALUE);

    private final static Target TARGET_HEAD = new Target(null, Integer.MIN_VALUE);

    private final static Target TARGET_TAIL = new Target(null, Integer.MAX_VALUE);

    public void scoring2(Scores scores) {
        LinkedList<Hit> hits = scores.hits;


        float score = 0;
        Hit first = hits.getFirst();
        score -= first.vIndex;

        Hit preHit = first;

        int currentTargetIndex = -1;

        int nameLength = scores.nameLength;
        for (Hit hit : hits) {

            int vIndexDistance = hit.vIndex - preHit.vIndex - 1;
            if (vIndexDistance > 0) {
                score -= vIndexDistance;
            }

            WordTarget target = hit.target;

            int min = Integer.MAX_VALUE;
            int minIndex = 0;
            for (Integer i : target.getWordIndexList()) {

                int d = Math.abs(currentTargetIndex - i);
                if (d < min) {
                    min = d;
                    minIndex = i;
                }
            }
            if (min <= 0) {
                min = 1;
            }
            score += (1.0f / min) * hit.alike;

            currentTargetIndex = minIndex;
            preHit = hit;
        }

        score -= scores.searchLength - preHit.vIndex - 1;

        scores.score = score /*/ scores.nameLength*/;
//        Log.e("lianghuan", "score --- " + score);
    }



    public void scoring(Scores scores) {
        LinkedList<Hit> hits = scores.hits;


        TreeSet<Target> targetTreeSet = new TreeSet<>((o1, o2) -> {
            if (o1.index > o2.index) {
                return 1;
            } else if (o1.index < o2.index) {
                return -1;
            }
            return 0;
        });

        TreeMap<Integer, Target> targetTreeMap = new TreeMap<>((o1, o2) -> {
            if (o1 > o2) {
                return 1;
            } else if (o1 < o2) {
                return -1;
            }
            return 0;
        });

        ArrayList<Arrow> arrowArrayList = new ArrayList<>(hits.size());


        for (Hit next : hits) {
            Arrow arrow = new Arrow(next, next.vIndex);
            arrowArrayList.add(arrow);

            WordTarget wordTarget = next.target;
            LinkedList<Integer> wordIndexList = wordTarget.getWordIndexList();
            for (Integer index : wordIndexList) {
                if (!targetTreeMap.containsKey(index)) {
                    Target t = new Target(wordTarget, index);
                    targetTreeMap.put(index, t);

                    arrow.targets.add(t);
                    t.arrows.add(arrow);
                }
            }
        }

        int arrowSize = arrowArrayList.size();
        Arrow preA = arrowArrayList.get(0);
        preA.pre = ARROW_HEAD;
        for (int i = 1; i < arrowSize; i++) {
            Arrow arrow = arrowArrayList.get(i);
            arrow.pre = preA;
            preA.next = arrow;

            preA = arrow;
        }
        preA.next = ARROW_TAIL;

        Collection<Target> values = targetTreeMap.values();
        Iterator<Target> iterator = values.iterator();

        if (!iterator.hasNext()) throw new AssertionError("error, target size is zero");

        Target preT = iterator.next();
        preT.pre = TARGET_HEAD;
        while (iterator.hasNext()) {
            Target target = iterator.next();
            target.pre = preT;
            preT.next = target;

            preT = target;
        }
        preT.next = TARGET_TAIL;

        Target curT = TARGET_HEAD.next;

        while (curT.select < curT.arrows.size()) {

            int curVIndex = curT.arrows.get(curT.select).index;
            int curWordIndex = curT.index;

            Target next = curT.next;

            boolean targetContinue = false;
            if (next.index == curT.index - 1) {
                targetContinue = true;
            }

            for (Arrow arrow : next.arrows) {

            }


            curT = curT.next;


        }

    }
}
