package com.uan.vsearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Search {

    private ArrayList<ContactsData> mContactsList = new ArrayList<>();

    private HashMap<String, ArrayList<WordTarget>> mPingyinMap = new HashMap<>();

    private int[] mMapArray = new int[1024 * 1024];

    private PingYinFile mPingYinFile = new PingYinFile(null);

    private final Scoring mScoring = new Scoring();

    public void search(String voice) {

        int length = voice.length();

        HashMap<Integer, Scores> hashMap = new HashMap<>();
        ArrayList<Scores> scoresList = new ArrayList<>();
        final int intLen = length + 2;

        for (int i = 0; i < length; i++) {
            String zi = voice.substring(i, i + 1);
            String pingyin = mPingYinFile.getPingyin(zi);

            ArrayList<WordTarget> arrayList = mPingyinMap.get(pingyin);
            for (int j = 0; j < arrayList.size(); j++) {
                WordTarget target = arrayList.get(j);
                Scores scores = hashMap.get(target.nameIndex);
                if (scores == null) {
                    scores = new Scores();
                    scores.length = target.nameLength;
                    hashMap.put(target.nameIndex, scores);
                    scoresList.add(scores);
                }
                Hit hit = new Hit();
                hit.alike = 1f;
                hit.vIndex = i;
                hit.target = target;
            }
        }

        scoringAndSort(scoresList);
        

    }

    private void scoringAndSort(ArrayList<Scores> list) {

        for (Scores hit :
                list) {
            mScoring.scoring(hit);
        }

        Collections.sort(list, (o1, o2) -> {
            if (o1.score > o2.score) {
                return 1;
            } else if (o1.score < o2.score){
                return -1;
            }
            return 0;
        });
    }


    public void addContacts(List<ContactsData> list) {
        if (list == null) {
            return;
        }

        mContactsList.addAll(list);


    }

    private void buildMap() {
        ArrayList<ContactsData> list = mContactsList;

        for (int j = 0; j < list.size(); j++) {
            ContactsData data = list.get(j);
            String name = data.name;

            int length = name.length();
            HashMap<String, WordTarget> hashMap = new HashMap<>();

            for (int i = 0; i < length; i++) {
                String zi = name.substring(i, i + 1);
                String pingyin = mPingYinFile.getPingyin(zi);
                WordTarget target = hashMap.get(pingyin);
                if (target == null) {
                    target = new WordTarget(j, length);
                    target.addIndex(i);
                    hashMap.put(pingyin, target);
                } else {
                    target.addIndex(i);
                }
            }

            Set<String> keySet = hashMap.keySet();
            Iterator<String> iterator = keySet.iterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                WordTarget target = hashMap.get(next);
                ArrayList<WordTarget> arrayList = mPingyinMap.get(next);
                if (arrayList == null) {
                    arrayList = new ArrayList<>();
                    mPingyinMap.put(next, arrayList);
                }
                arrayList.add(target);
            }

//            for (int i = 0; i < length; ++i) {
//                String zi = name.substring(i, i + 1);
//                String pingyin = mPingYinFile.getPingyin(zi);
//
//                ArrayList<int[]> arrayList = mPingyinMap.get(pingyin);
//                if (arrayList == null) {
//                    arrayList = new ArrayList<>();
//                    mPingyinMap.put(pingyin, arrayList);
//                }
//
//                arrayList.add(new int[]{j, i});
//
//            }
        }


    }

    private boolean isHanzi(String s) {
        return true;
    }



}
