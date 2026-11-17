package com.homoonme.algovisualizer;

import java.util.*;

public class DijkstraAlgorithm {

    /**
     * 生成 Dijkstra 算法的完整结果（包含步骤、前驱边、距离）
     */
    public DijkstraResult generateResult(GraphModel graph, String startNodeId) {
        List<GraphStepSnapshot> steps = new ArrayList<>();

        // 1. 初始化数据结构
        Map<String, Integer> distances = new HashMap<>();
        Map<String, Edge> shortestPathEdges = new HashMap<>();
        Set<String> visitedNodeIds = new HashSet<>();
        PriorityQueue<String> pq = new PriorityQueue<>(
            Comparator.comparingInt(id -> distances.getOrDefault(id, Integer.MAX_VALUE)));

        // 初始化距离
        for (Vertex v : graph.getNodes()) {
            distances.put(v.getId(), Integer.MAX_VALUE);
        }
        distances.put(startNodeId, 0);
        pq.add(startNodeId);

        // --- 初始快照 ---
        steps.add(new GraphStepSnapshot(
                "初始化 Dijkstra，起点 " + startNodeId + " 距离设为 0",
                List.of(startNodeId),
                List.of(),
                visitedNodeIds,
                new HashSet<>(shortestPathEdges.values()),
                new HashMap<>(distances)
        ));

        // 2. 循环处理
        while (!pq.isEmpty()) {
            String u = pq.poll();

            if (visitedNodeIds.contains(u)) continue;
            visitedNodeIds.add(u);

            steps.add(new GraphStepSnapshot(
                    "选定当前距离最小节点 " + u + " (距离: " + distances.get(u) + ")",
                    List.of(u),
                    List.of(),
                    visitedNodeIds,
                    new HashSet<>(shortestPathEdges.values()),
                    new HashMap<>(distances)
            ));

            List<Edge> neighbors = graph.getNeighbors(u);
            neighbors.sort(Comparator.comparingInt(Edge::getWeight));

            for (Edge edge : neighbors) {
                String v = edge.getTarget();

                if (visitedNodeIds.contains(v)) continue;

                steps.add(new GraphStepSnapshot(
                        "检查邻居 " + v + "，边权重: " + edge.getWeight(),
                        List.of(u, v),
                        List.of(edge),
                        visitedNodeIds,
                        new HashSet<>(shortestPathEdges.values()),
                        new HashMap<>(distances)
                ));

                int newDist = distances.get(u) + edge.getWeight();
                if (newDist < distances.get(v)) {
                    int oldDist = distances.get(v);
                    distances.put(v, newDist);
                    shortestPathEdges.put(v, edge);
                    pq.add(v);

                    String oldDistStr = (oldDist == Integer.MAX_VALUE) ? "∞" : String.valueOf(oldDist);
                    steps.add(new GraphStepSnapshot(
                            "更新 " + v + " 的距离: " + oldDistStr + " -> " + newDist,
                            List.of(v),
                            List.of(edge),
                            visitedNodeIds,
                            new HashSet<>(shortestPathEdges.values()),
                            new HashMap<>(distances)
                    ));
                }
            }
        }

        // 3. 结束快照 - 提示用户可以点击节点查看路径
        steps.add(new GraphStepSnapshot(
                "Dijkstra 完成！点击任意节点查看从起点到该节点的最短路径",
                List.of(),
                List.of(),
                visitedNodeIds,
                new HashSet<>(shortestPathEdges.values()),
                new HashMap<>(distances)
        ));

        return new DijkstraResult(steps, shortestPathEdges, distances, startNodeId);
    }

    /**
     * 兼容旧接口：只返回步骤列表
     */
    public List<GraphStepSnapshot> generateSteps(GraphModel graph, String startNodeId) {
        return generateResult(graph, startNodeId).getSteps();
    }
}