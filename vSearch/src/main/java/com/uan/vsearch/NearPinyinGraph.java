package com.uan.vsearch;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class NearPinyinGraph {

    private static final String NEAR_PINYIN_FILE_NAME = "near-pinyin.txt";
    private static final String TAG = "NearPinyinGraph";

    private static final String PINYIN_ZH = "zh";
    private static final String PINYIN_SH = "sh";
    private static final String PINYIN_CH = "ch";
    private static final String PINYIN_H = "h";

    private static final String PINYIN_NG = "ng";
    private static final String PINYIN_N = "n";

    private final HashMap<String, Node> mPinyinMap = new HashMap<>();


    public LinkedList<NearPinyin> getNearPinyin(String pinyin, float dis) {
        LinkedList<NearPinyin> list = new LinkedList<>();
        if (!mPinyinMap.containsKey(pinyin)) {
            return list;
        }

        Node node = mPinyinMap.get(pinyin);

        HashSet<Node> computedNode = new HashSet<>();

        LinkedList<WNode> queue = new LinkedList<>();
        queue.add(new WNode(node, 0f));
        computedNode.add(node);

        while (queue.size() > 0) {
            WNode first = queue.removeFirst();
            list.add(new NearPinyin(first.node.pinyin, 1 - first.distanceAll));

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

                if (computedNode.contains(sideNode)) {
                    continue;
                }
                queue.add(new WNode(sideNode, distance));
                computedNode.add(sideNode);
            }
        }

        return list;
    }

    public void buildPinyinGraph(Context context) {
        readPinyinFromFile(context, mPinyinMap);

        // 构建声调相似度
        connectTone(mPinyinMap);

        // 构建有无卷舌相似度
        connectH(mPinyinMap);

        // 构建有无后鼻音相似度
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

                line = bufferedReader.readLine();
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
     *
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

            nodes.sort((o1, o2) -> {
                int com = o1.pinyin.compareTo(o2.pinyin);

                if (Math.abs(com) > ('a' - '9')) {
                    return Math.abs(com);
                }
                return com;
            });

            Node node = nodes.removeFirst();
            while (nodes.size() > 0) {
                Node next = nodes.removeFirst();

                Edge edge = new Edge();
                edge.one = node;
                edge.two = next;
                // TODO 一些拼音可能没有第二声，距离不一定都是0.1f
                edge.distance = 0.1f;

                node.mEdges.add(edge);
                next.mEdges.add(edge);

                node = next;
            }
        });
    }

    /**
     * 是否是卷舌音
     *
     * @param s 拼音
     * @return true 卷舌，false 不卷舌
     */
    private boolean isRollingTongue(String s) {
        if (s.startsWith(PINYIN_ZH)) {
            return true;
        }

        if (s.startsWith(PINYIN_SH)) {
            return true;
        }

        if (s.startsWith(PINYIN_CH)) {
            return true;
        }
        return false;
    }

    /**
     * 返回卷舌音对应都不卷舌音
     *
     * @param s 拼音
     * @return 不卷舌的拼音
     */
    private String removeRollingTongue(String s) {
        return s.replaceFirst(PINYIN_H, "");
    }

    private void connectH(HashMap<String, Node> hashMap) {
        for (Map.Entry<String, Node> entry : hashMap.entrySet()) {
            String key = entry.getKey();
            if (isRollingTongue(key)) {
                String hKey = removeRollingTongue(key);
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

    /**
     * 是否是后鼻音
     *
     * @param s 拼音
     * @return true 是，false 否
     */
    private boolean isNasalVoice(String s) {
        return s.endsWith(PINYIN_NG);
    }

    /**
     * 移除后鼻音，把拼音末尾都g去掉
     *
     * @param s 拼音
     * @return 移除g后都拼音
     */
    private String removeNasalVoice(String s) {
        return s.replaceFirst(PINYIN_NG + "$", PINYIN_N);
    }

    private void connectG(HashMap<String, Node> hashMap) {
        for (Map.Entry<String, Node> entry : hashMap.entrySet()) {
            String key = entry.getKey();
            if (isNasalVoice(key)) {
                String gKey = removeNasalVoice(key);
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


    private class Node {
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

    private class Edge {
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
