package com.uan.vsearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;

public class Search {

    private ArrayList<ContactsData> mContactsList = new ArrayList<>();

    private HashMap<String, ArrayList<int[]>> mPingyinMap = new HashMap<>();

    private int[] mMapArray = new int[1024 * 1024];

    private PingYinFile mPingYinFile = new PingYinFile(null);

    private final Scoring mScoring = new Scoring();

    public void search(String voice) {

        int length = voice.length();

        HashMap<Integer, Scoring.Hit> hashMap = new HashMap<>();
        ArrayList<Scoring.Hit> scoreList = new ArrayList<>();
        final int intLen = length + 2;

        for (int i = 0; i < length; i++) {
            String zi = voice.substring(i, i + 1);
            String pingyin = mPingYinFile.getPingyin(zi);

            ArrayList<int[]> arrayList = mPingyinMap.get(pingyin);
            for (int j = 0; j < arrayList.size(); j++) {
                int[] ints = arrayList.get(j);
                Scoring.Hit scores = hashMap.get(ints[0]);
                if (scores == null) {
                    scores = new Scoring.Hit(length);
                    scores.index = ints[0];
                    hashMap.put(ints[0], scores);
                    scoreList.add(scores);
                }
                scores.hits[i] = ints[1] + 1;
            }
        }

        scoringAndSort(scoreList);
        

    }

    private void scoringAndSort(ArrayList<Scoring.Hit> list) {

        for (Scoring.Hit hit :
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
            HashMap<String, int[]> hashMap = new HashMap<>();

            for (int i = 0; i < length; i++) {
                String zi = name.substring(i, i + 1);
                String pingyin = mPingYinFile.getPingyin(zi);
                int[] ints = hashMap.get(pingyin);
                if (ints == null) {
                    hashMap.put(pingyin, new int[]{j, length, i});
                } else {
                    int[] newInts = new int[ints.length + 1];
                    for (int k = 0; k < ints.length; k++) {
                        newInts[k] = ints[k];
                    }
                    newInts[newInts.length - 1] = i;
                    newInts[ints.length - 1] = ints[ints.length - 1] | 0x80000000;
                    hashMap.put(pingyin, newInts);
                }
            }

            Set<String> keySet = hashMap.keySet();
            Iterator<String> iterator = keySet.iterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                int[] ints = hashMap.get(next);
                ArrayList<int[]> arrayList = mPingyinMap.get(next);
                if (arrayList == null) {
                    arrayList = new ArrayList<>();
                    mPingyinMap.put(next, arrayList);
                }
                arrayList.add(ints);
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
