package com.uan.vsearch;

import java.util.Iterator;
import java.util.LinkedList;

public class NearPinyinGraph {


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
