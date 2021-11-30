package com.uan.vsearch;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class NearPinyinGraph {

    private static final String NEAR_PINYIN_FILE_NAME = "near-pinyin.txt";

    private final HashMap<String, Node> mPinyinMap = new HashMap<>();

    public void buildPinyinGraph(Context context) {
        AssetManager assets = context.getAssets();
        InputStream inputStream = null;
        try {
            inputStream = assets.open(NEAR_PINYIN_FILE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String line = null;
        try {
            line = bufferedReader.readLine();
            while (line != null) {
                line = line.trim();
                Node node = new Node(line);
                mPinyinMap.put(line, node);

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    class Node {
        final String pinyin;
        final LinkedList<Edge> mEdges = new LinkedList<>();

        public Node(String p) {
            pinyin = p;
        }

        public void addEdge(Edge edge) {
            mEdges.add(edge);
        }

        public Iterator<Edge> edgeIterator() {

            return mEdges.iterator();
        }
    }

    class Edge {
        Node one;
        Node two;
        float distance;
    }
}
