package com.homoonme.algovisualizer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class BreadthFirstSearch {

    private List<GraphStepSnapshot> steps;
    private GraphModel graph; //

    public List<GraphStepSnapshot> generateSteps(GraphModel graph, String startNodeId) { //
        this.steps = new ArrayList<>();
        this.graph = graph;

        // --- 算法核心 ---
        Queue<String> queue = new LinkedList<>();
        Set<String> visitedNodeIds = new HashSet<>();
        Set<Edge> pathEdges = new HashSet<>(); // 用它来保存“发现”的边

        // 1. 初始状态
        queue.add(startNodeId);
        visitedNodeIds.add(startNodeId);

        // 添加“初始”快照
        steps.add(new GraphStepSnapshot(
                "开始BFS，" + startNodeId + " 入队",
                List.of(startNodeId), // 高亮节点
                List.of(), // 高亮边
                visitedNodeIds,
                pathEdges
        ));

        // 2. 循环
        while (!queue.isEmpty()) {
            String currentNodeId = queue.poll();

            // 添加“出队”快照
            steps.add(new GraphStepSnapshot(
                    "节点 " + currentNodeId + " 出队并处理",
                    List.of(currentNodeId),
                    List.of(),
                    visitedNodeIds,
                    pathEdges
            ));

            // 3. 遍历邻居
            for (Edge edge : graph.getNeighbors(currentNodeId)) {
                String neighborId = edge.getTarget();

                // 添加“检查邻居”快照
                steps.add(new GraphStepSnapshot(
                        "检查 " + currentNodeId + " 的邻居 " + neighborId,
                        List.of(neighborId), // 高亮邻居
                        List.of(edge), // 高亮边
                        visitedNodeIds,
                        pathEdges
                ));

                if (!visitedNodeIds.contains(neighborId)) {
                    visitedNodeIds.add(neighborId);
                    queue.add(neighborId);
                    pathEdges.add(edge); // 标记这条“发现”边

                    // 添加“入队”快照
                    steps.add(new GraphStepSnapshot(
                            "发现新节点 " + neighborId + "，入队并标记",
                            List.of(neighborId),
                            List.of(edge),
                            visitedNodeIds,
                            pathEdges
                    ));
                }
            }
        }

        // 4. 结束
        steps.add(new GraphStepSnapshot(
                "遍历完成",
                List.of(),
                List.of(),
                visitedNodeIds,
                pathEdges
        ));

        return this.steps;
    }
}