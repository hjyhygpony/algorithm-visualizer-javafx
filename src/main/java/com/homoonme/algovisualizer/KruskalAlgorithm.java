package com.homoonme.algovisualizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Kruskal 最小生成树算法
 * 思想：将所有边按权重排序，依次选择最小的边，
 * 如果这条边不会形成环（使用并查集判断），就加入MST。
 */
public class KruskalAlgorithm {

    // 并查集：查找根节点
    private String find(Map<String, String> parent, String x) {
        if (!parent.get(x).equals(x)) {
            parent.put(x, find(parent, parent.get(x))); // 路径压缩
        }
        return parent.get(x);
    }

    // 并查集：合并两个集合
    private void union(Map<String, String> parent, Map<String, Integer> rank, String x, String y) {
        String rootX = find(parent, x);
        String rootY = find(parent, y);
        if (!rootX.equals(rootY)) {
            // 按秩合并
            if (rank.get(rootX) < rank.get(rootY)) {
                parent.put(rootX, rootY);
            } else if (rank.get(rootX) > rank.get(rootY)) {
                parent.put(rootY, rootX);
            } else {
                parent.put(rootY, rootX);
                rank.put(rootX, rank.get(rootX) + 1);
            }
        }
    }

    public List<GraphStepSnapshot> generateSteps(GraphModel graph, String startNodeId) {
        List<GraphStepSnapshot> steps = new ArrayList<>();

        // 1. 初始化并查集
        Map<String, String> parent = new HashMap<>();
        Map<String, Integer> rank = new HashMap<>();
        for (Vertex v : graph.getNodes()) {
            parent.put(v.getId(), v.getId());
            rank.put(v.getId(), 0);
        }

        // 2. 获取所有边并按权重排序（去重，因为无向图有双向边）
        Set<String> edgeSet = new HashSet<>();
        List<Edge> allEdges = new ArrayList<>();
        for (Edge e : graph.getEdges()) {
            String key = e.getSource().compareTo(e.getTarget()) < 0
                    ? e.getSource() + "-" + e.getTarget()
                    : e.getTarget() + "-" + e.getSource();
            if (!edgeSet.contains(key)) {
                edgeSet.add(key);
                allEdges.add(e);
            }
        }
        allEdges.sort((a, b) -> a.getWeight() - b.getWeight());

        Set<String> mstNodes = new HashSet<>();
        Set<Edge> mstEdges = new HashSet<>();

        steps.add(new GraphStepSnapshot(
                "Kruskal 开始，共 " + allEdges.size() + " 条边，按权重排序",
                List.of(), List.of(),
                mstNodes, mstEdges
        ));

        // 3. 依次检查每条边
        int edgeCount = 0;
        int nodeCount = graph.getNodes().size();

        for (Edge edge : allEdges) {
            String u = edge.getSource();
            String v = edge.getTarget();

            steps.add(new GraphStepSnapshot(
                    "检查边 (" + u + "," + v + ")，权重: " + edge.getWeight(),
                    List.of(u, v), List.of(edge),
                    mstNodes, mstEdges
            ));

            // 判断是否形成环
            if (find(parent, u).equals(find(parent, v))) {
                steps.add(new GraphStepSnapshot(
                        "边 (" + u + "," + v + ") 会形成环，跳过",
                        List.of(u, v), List.of(edge),
                        mstNodes, mstEdges
                ));
            } else {
                // 加入 MST
                union(parent, rank, u, v);
                mstNodes.add(u);
                mstNodes.add(v);
                mstEdges.add(edge);
                edgeCount++;

                steps.add(new GraphStepSnapshot(
                        "边 (" + u + "," + v + ") 加入MST，当前 " + edgeCount + " 条边",
                        List.of(u, v), List.of(edge),
                        mstNodes, mstEdges
                ));

                // MST 完成条件：边数 = 节点数 - 1
                if (edgeCount == nodeCount - 1) {
                    break;
                }
            }
        }

        steps.add(new GraphStepSnapshot(
                "Kruskal 完成，最小生成树共 " + edgeCount + " 条边",
                List.of(), List.of(),
                mstNodes, mstEdges
        ));

        return steps;
    }
}
