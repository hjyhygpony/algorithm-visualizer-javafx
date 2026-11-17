package com.homoonme.algovisualizer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

public class GraphStepSnapshot {
    private final String message;
    // 状态高亮
    private final List<String> highlightedNodeIds; // 当前正在处理的节点
    private final List<Edge> highlightedEdges;   // 当前正在处理的边

    // 结果高亮
    private final Set<String> visitedNodeIds;     // 已经访问过的节点
    private final Set<Edge> pathEdges;          // 已经确定是路径的边（如最小生成树）

    // Dijkstra 专用：节点距离标签
    private final Map<String, Integer> nodeDistances;

    public GraphStepSnapshot(String message, List<String> highlightedNodeIds, List<Edge> highlightedEdges, Set<String> visitedNodeIds, Set<Edge> pathEdges) {
        this(message, highlightedNodeIds, highlightedEdges, visitedNodeIds, pathEdges, null);
    }

    public GraphStepSnapshot(String message, List<String> highlightedNodeIds, List<Edge> highlightedEdges, 
                             Set<String> visitedNodeIds, Set<Edge> pathEdges, Map<String, Integer> nodeDistances) {
        this.message = message;
        // 创建不可变拷贝
        this.highlightedNodeIds = List.copyOf(highlightedNodeIds);
        this.highlightedEdges = List.copyOf(highlightedEdges);
        this.visitedNodeIds = Set.copyOf(visitedNodeIds);
        this.pathEdges = Set.copyOf(pathEdges);
        this.nodeDistances = (nodeDistances != null) ? new HashMap<>(nodeDistances) : null;
    }

    // Getters
    public String getMessage() { return message; }
    public List<String> getHighlightedNodeIds() { return highlightedNodeIds; }
    public List<Edge> getHighlightedEdges() { return highlightedEdges; }
    public Set<String> getVisitedNodeIds() { return visitedNodeIds; }
    public Set<Edge> getPathEdges() { return pathEdges; }
    public Map<String, Integer> getNodeDistances() { return nodeDistances; }
}