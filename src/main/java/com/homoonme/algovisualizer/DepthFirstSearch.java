package com.homoonme.algovisualizer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DepthFirstSearch {

    private List<GraphStepSnapshot> steps;
    private GraphModel graph; //
    private Set<String> visitedNodeIds;
    private Set<Edge> pathEdges;

    public List<GraphStepSnapshot> generateSteps(GraphModel graph, String startNodeId) { //
        this.steps = new ArrayList<>();
        this.graph = graph;
        this.visitedNodeIds = new HashSet<>();
        this.pathEdges = new HashSet<>();

        // 1. 初始状态
        steps.add(new GraphStepSnapshot(
                "开始DFS，从 " + startNodeId + " 出发",
                List.of(startNodeId), // 高亮节点
                List.of(), // 高亮边
                visitedNodeIds,
                pathEdges
        ));

        // 2. 调用递归助手
        dfs(startNodeId);

        // 3. 结束
        steps.add(new GraphStepSnapshot(
                "遍历完成",
                List.of(), List.of(),
                visitedNodeIds, pathEdges
        ));

        return this.steps;
    }

    /** DFS 递归辅助方法 **/
    private void dfs(String currentNodeId) {
        // 标记当前节点为已访问
        visitedNodeIds.add(currentNodeId);

        // 添加“访问”快照
        steps.add(new GraphStepSnapshot(
                "访问节点 " + currentNodeId,
                List.of(currentNodeId),
                List.of(),
                visitedNodeIds,
                pathEdges
        ));

        // 遍历邻居
        for (Edge edge : graph.getNeighbors(currentNodeId)) { //
            String neighborId = edge.getTarget();

            // 添加“检查邻居”快照
            steps.add(new GraphStepSnapshot(
                    "检查 " + currentNodeId + " 的邻居 " + neighborId,
                    List.of(currentNodeId, neighborId),
                    List.of(edge),
                    visitedNodeIds,
                    pathEdges
            ));

            if (!visitedNodeIds.contains(neighborId)) {
                // 如果邻居未被访问，标记“发现”边，并递归
                pathEdges.add(edge);

                steps.add(new GraphStepSnapshot(
                        "发现新节点 " + neighborId + "，递归进入",
                        List.of(neighborId),
                        List.of(edge),
                        visitedNodeIds,
                        pathEdges
                ));

                dfs(neighborId); // 递归

                // 递归返回
                steps.add(new GraphStepSnapshot(
                        "从 " + neighborId + " 回溯到 " + currentNodeId,
                        List.of(currentNodeId),
                        List.of(),
                        visitedNodeIds,
                        pathEdges
                ));
            }
        }
    }
}