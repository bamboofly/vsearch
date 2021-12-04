package com.uan.vsearch;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class NearPinyinGraph {

    private static final String NEAR_PINYIN_FILE_NAME = "near-pinyin.txt";

    private final HashMap<String, Node> mPinyinMap = new HashMap<>();


    public LinkedList<Node> getNearPinyin(String pinyin, float dis) {
        LinkedList<Node> list = new LinkedList<>();
        if (!mPinyinMap.containsKey(pinyin)) {
            return list;
        }

        Node node = mPinyinMap.get(pinyin);


        LinkedList<WNode> queue = new LinkedList<>();
        queue.add(new WNode(node, 0f));

        while (queue.size() > 0) {
            WNode first = queue.removeFirst();
            list.add(first.node);

            if (first.node.edgeSize() <= 0) {
                continue;
            }
            Iterator<Edge> edgeIterator = first.node.edgeIterator();
            while (edgeIterator.hasNext()) {
                Edge edgeNext = edgeIterator.next();

                float distance = first.distanceAll + edgeNext.distance;
                if (distance > dis) {
                    continue;
                }
                Node sideNode = edgeNext.otherSideNode(first.node);
                queue.add(new WNode(sideNode, distance));
            }
        }

        for (WNode wNode : queue) {
            list.add(wNode.node);
        }

        return list;
    }

    public void buildPinyinGraph(Context context) {
        readPinyinFromFile(context, mPinyinMap);

        connectTone(mPinyinMap);

        connectH(mPinyinMap);

        connectG(mPinyinMap);
    }

    private void readPinyinFromFile(Context context, HashMap<String, Node> hashMap) {
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
                hashMap.put(line, node);
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

    /**
     * 将同一个拼音但声调不同的连接起来
     * @param hashMap
     */
    private void connectTone(HashMap<String, Node> hashMap) {
        Set<Map.Entry<String, Node>> entries = hashMap.entrySet();

        HashMap<String, LinkedList<Node>> tempHashMap = new HashMap<>();

        for (Map.Entry<String, Node> entry : entries) {
            String key = entry.getKey();
            Node value = entry.getValue();

            String noToneKey = key.replaceAll("[0-9]+", "");

            LinkedList<Node> nodes = tempHashMap.get(noToneKey);
            if (nodes == null) {
                nodes = new LinkedList<>();
                tempHashMap.put(noToneKey, nodes);
            }
            nodes.push(value);
        }

        tempHashMap.forEach((s, nodes) -> {
            if (nodes.size() <= 1) {
                return;
            }

            Collections.sort(nodes, (o1, o2) -> {
                int com = o1.pinyin.compareTo(o2.pinyin);
                if (com > 10) {
                    return -1;
                }
                return com;
            });

            Node node = nodes.removeFirst();
            while (nodes.size() > 0) {
                Node next = nodes.removeFirst();

                Edge edge = new Edge();
                edge.one = node;
                edge.two = next;
                edge.distance = 0.1f;

                node.mEdges.add(edge);
                next.mEdges.add(edge);

                node = next;
            }
        });
    }

    private void connectH(HashMap<String, Node> hashMap) {
        for (Map.Entry<String, Node> entry : hashMap.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("zh") || key.startsWith("ch")) {
                String hKey = key.replaceFirst("h", "");
                if (hashMap.containsKey(hKey)) {
                    Node node = hashMap.get(hKey);
                    Node value = entry.getValue();

                    Edge edge = new Edge();
                    edge.one = node;
                    edge.two = value;
                    edge.distance = 0.05f;

                    node.addEdge(edge);
                    value.addEdge(edge);
                }
            }
        }
    }

    private void connectG(HashMap<String, Node> hashMap) {
        for (Map.Entry<String, Node> entry : hashMap.entrySet()) {
            String key = entry.getKey();
            if (key.endsWith("ng")) {
                String gKey = key.replaceFirst("ng", "n");
                if (hashMap.containsKey(gKey)) {
                    Node node = hashMap.get(gKey);

                    Node value = entry.getValue();

                    Edge edge = new Edge();
                    edge.one = node;
                    edge.two = value;
                    edge.distance = 0.05f;

                    node.addEdge(edge);
                    value.addEdge(edge);
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

        public int edgeSize() {
            return mEdges.size();
        }

        public Iterator<Edge> edgeIterator() {

            return mEdges.iterator();
        }
    }

    class Edge {
        Node one;
        Node two;
        float distance;

        public Node otherSideNode(Node node) {
            if (one == node) {
                return two;
            }

            if (two == node) {
                return one;
            }

            return null;
        }
    }

    private static class WNode {
        final Node node;

        final float distanceAll;

        WNode(Node n, float ne) {
            node = n;
            distanceAll = ne;
        }
    }
}
