package com.uan.vsearch.pinyin;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NearPinyinGraph {

    private static final String TAG = "NearPinyinGraph";
    private static final String NEAR_PINYIN_TONE_FILE_NAME = "pinyin-tone.txt";
    private static final String NEAR_SHENG_MU_FILE_NAME = "near-shengmu.txt";
    private static final String NEAR_PINYIN_FILE_NAME = "near-pinyin.txt";

    public static final float FULL_ALIKE_SCORE = 1f;
    public static final float PINYIN_ALIKE_SCORE = 0.95f;

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
            list.add(new NearPinyin(first.node.pinyin, PINYIN_ALIKE_SCORE - first.distanceAll));

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

        // 构建声母相似
        connectShengmuLike(mPinyinMap, context);

        // 构建自定义相似
        connectCustomLike(mPinyinMap, context);

        // 声调为0的拼音和没有声调数字的拼音是一样的，这里添加没有声调拼音的映射。注意这方法必须最后执行
        addExpandMap();
    }

    private void addExpandMap() {

        HashMap<String, Node> hashMap = new HashMap<>();
        // 如果声调为0，添加该拼音的另一种写法
        Pattern pattern = Pattern.compile("^.*?0");
        mPinyinMap.forEach((s, node) -> {
            boolean find = pattern.matcher(s).find();
            if (find) {
                String replace = s.replace("0", "");
                hashMap.put(replace, node);
            }
        });
        mPinyinMap.putAll(hashMap);
    }

    private void connectNode(Node one, Node two, float dis) {
        Edge edge = new Edge();
        edge.one = one;
        edge.two = two;

        edge.distance = dis;

        one.mEdges.add(edge);
        two.mEdges.add(edge);
    }

    private void connectShengmuLike(HashMap<String, Node> pinyinMap, Context context) {

        Pattern pattern = Pattern.compile("^([a-z]+) - ([a-z]+) : ([/.0-9]+)$");

        new LineReader(context.getAssets(), NEAR_SHENG_MU_FILE_NAME)
                .eachLine(l -> {
                    Matcher matcher = pattern.matcher(l);
                    int groupCount = matcher.groupCount();

                    if (groupCount < 3) {
                        return;
                    }
                    boolean b = matcher.find();
                    if (!b) {
                        Log.e(TAG, "not match " + ", l " + l);
                    }
                    String one = matcher.group(1);
                    String two = matcher.group(2);
                    String alike = matcher.group(3);

                    float f = Float.parseFloat(alike);

                    for (String key : pinyinMap.keySet()) {
                        if (key.startsWith(one)) {
                            String nearKey = two + key.substring(1);
                            if (pinyinMap.containsKey(nearKey)) {
                                Node nearNode = pinyinMap.get(nearKey);
                                Node node = pinyinMap.get(key);

                                connectNode(node, nearNode, f);
                            }
                        }
                    }
                });
    }

    private void connectCustomLike(HashMap<String, Node> pinyinMap, Context context) {

        Pattern pattern = Pattern.compile("^(.*?) - (.*?) : ([/.0-9]+)$");

        new LineReader(context.getAssets(), NEAR_PINYIN_FILE_NAME)
                .eachLine(l -> {
                    Matcher matcher = pattern.matcher(l);
                    int groupCount = matcher.groupCount();

                    if (groupCount < 3) {
                        return;
                    }
                    boolean b = matcher.find();
                    if (!b) {
                        Log.e(TAG, "not match " + ", l " + l);
                        return;
                    }
                    String one = matcher.group(1);
                    String two = matcher.group(2);
                    String alike = matcher.group(3);

                    float f = Float.parseFloat(alike);

                    Node oneNode = pinyinMap.get(one);
                    Node twoNode = pinyinMap.get(two);
                    if (oneNode != null && twoNode != null) {
                        connectNode(oneNode, twoNode, f);
                    }
                });
    }


    private void readPinyinFromFile(Context context, HashMap<String, Node> hashMap) {
        AssetManager assets = context.getAssets();
        new LineReader(assets, NEAR_PINYIN_TONE_FILE_NAME)
                .eachLine(l -> {
                    Node node = new Node(l);
                    hashMap.put(l, node);
                });
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

            nodes.sort((o1, o2) -> o1.pinyin.compareTo(o2.pinyin));

            Node node = nodes.removeFirst();
            while (nodes.size() > 0) {
                Node next = nodes.removeFirst();

                Edge edge = new Edge();
                edge.one = node;
                edge.two = next;
                // 一些拼音可能没有某个声调，距离不一定都是0.08f
                int k = node.pinyin.compareTo(next.pinyin);
                k = Math.abs(k);
                edge.distance = 0.08f * k;

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
