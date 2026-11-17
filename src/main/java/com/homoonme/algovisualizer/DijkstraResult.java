package com.homoonme.algovisualizer;

import java.util.*;

/**
 * Dijkstra 算法的完整结果，包含：
 * 1. 动画步骤
 * 2. 前驱边映射（用于回溯路径）
 * 3. 最短距离映射
 */
public class DijkstraResult {
    private final List<GraphStepSnapshot> steps;
    private final Map<String, Edge> predecessorEdges;  // 节点ID -> 到达该节点的边
    private final Map<String, Integer> distances;       // 节点ID -> 最短距离
    private final String startNodeId;

    public DijkstraResult(List<GraphStepSnapshot> steps, 
                          Map<String, Edge> predecessorEdges,
                          Map<String, Integer> distances,
                          String startNodeId) {
        this.steps = steps;
        this.predecessorEdges = new HashMap<>(predecessorEdges);
        this.distances = new HashMap<>(distances);
        this.startNodeId = startNodeId;
    }

    public List<GraphStepSnapshot> getSteps() { return steps; }
    public Map<String, Edge> getPredecessorEdges() { return predecessorEdges; }
    public Map<String, Integer> getDistances() { return distances; }
    public String getStartNodeId() { return startNodeId; }

    /**
     * 获取从起点到目标节点的路径（边的列表）
     */
    public List<Edge> getPathTo(String targetId) {
        List<Edge> path = new ArrayList<>();
        String current = targetId;
        
        // 从目标节点回溯到起点
        while (predecessorEdges.containsKey(current)) {
            Edge edge = predecessorEdges.get(current);
            path.add(edge);
            current = edge.getSource();
        }
        
        // 反转得到从起点到终点的顺序
        Collections.reverse(path);
        return path;
    }

    /**
     * 获取从起点到目标节点的路径上的所有节点ID
     */
    public List<String> getPathNodesTo(String targetId) {
        List<String> nodes = new ArrayList<>();
        List<Edge> path = getPathTo(targetId);
        
        if (!path.isEmpty()) {
            nodes.add(path.get(0).getSource()); // 起点
            for (Edge e : path) {
                nodes.add(e.getTarget());
            }
        } else if (targetId.equals(startNodeId)) {
            nodes.add(startNodeId); // 目标就是起点
        }
        
        return nodes;
    }
}
