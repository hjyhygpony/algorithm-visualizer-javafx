package com.homoonme.algovisualizer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public class PrimAlgorithm {

    public List<GraphStepSnapshot> generateSteps(GraphModel graph, String startNodeId) { //
        List<GraphStepSnapshot> steps = new ArrayList<>();

        // 算法核心
        Set<String> mstNodes = new HashSet<>(); // 已经加入MST的顶点
        Set<Edge> mstEdges = new HashSet<>();   // 已经加入MST的边

        // 优先队列，用于存储“横切边”，按权重排序
        PriorityQueue<Edge> pq = new PriorityQueue<>((a, b) -> a.getWeight() - b.getWeight());

        // 1. 从起始节点开始
        mstNodes.add(startNodeId);
        steps.add(new GraphStepSnapshot(
                "开始Prim算法，" + startNodeId + " 加入MST",
                List.of(startNodeId), List.of(),
                mstNodes, mstEdges
        ));

        // 2. 将起始节点的所有邻边加入优先队列
        for (Edge edge : graph.getNeighbors(startNodeId)) { //
            pq.add(edge);
        }
        steps.add(new GraphStepSnapshot(
                "将 " + startNodeId + " 的所有邻边加入优先队列",
                List.of(startNodeId), graph.getNeighbors(startNodeId), //
                mstNodes, mstEdges
        ));

        // 3. 循环
        while (!pq.isEmpty()) {
            // 4. 取出权重最小的边
            Edge minEdge = pq.poll();
            String source = minEdge.getSource();
            String target = minEdge.getTarget();

            // 检查这条边的两个顶点是否一个在MST内，一个在MST外
            // (防止形成环路)
            if (mstNodes.contains(source) && mstNodes.contains(target)) {
                steps.add(new GraphStepSnapshot(
                        "跳过边 (" + source + "," + target + ")，两端均已在MST中",
                        List.of(source, target), List.of(minEdge),
                        mstNodes, mstEdges
                ));
                continue; // 两个顶点都在MST里了，跳过
            }

            // 5. 确定新节点
            String newNodeId = mstNodes.contains(source) ? target : source;
            mstNodes.add(newNodeId);
            mstEdges.add(minEdge);

            steps.add(new GraphStepSnapshot(
                    "选择最小边 (" + source + "," + target + ")，新节点 " + newNodeId + " 加入MST",
                    List.of(newNodeId), List.of(minEdge),
                    mstNodes, mstEdges
            ));

            // 6. 将新节点的所有“新”邻边加入优先队列
            List<Edge> newEdges = new ArrayList<>();
            for (Edge edge : graph.getNeighbors(newNodeId)) { //
                String neighbor = edge.getTarget();
                // 如果边的另一端还不在MST中，就加入队列
                if (!mstNodes.contains(neighbor)) {
                    pq.add(edge);
                    newEdges.add(edge);
                }
            }
            if (!newEdges.isEmpty()) {
                steps.add(new GraphStepSnapshot(
                        "将 " + newNodeId + " 的新邻边加入优先队列",
                        List.of(newNodeId), newEdges,
                        mstNodes, mstEdges
                ));
            }
        }

        // 7. 结束
        steps.add(new GraphStepSnapshot(
                "最小生成树构建完成",
                List.of(), List.of(),
                mstNodes, mstEdges
        ));

        return steps;
    }
}