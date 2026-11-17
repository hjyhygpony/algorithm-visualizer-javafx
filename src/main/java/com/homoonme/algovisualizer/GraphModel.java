package com.homoonme.algovisualizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import java.util.Collection;
import java.util.stream.Collectors;

/**图的模型类 内部实现：邻接表*/
public class GraphModel implements java.io.Serializable{

    private static final long serialVersionUID = 1L;//为了序列化的兼容性

    // 1. 顶点映射表 (Map: 顶点ID -> 顶点对象)
    // 存储顶点的附加信息（如坐标）
    private final Map<String, Vertex> vertices;

    // 2. 邻接表 (Map: 顶点ID -> 与该顶点相邻的“边”的列表)
    // 这才是图的核心存储结构
    private final Map<String, List<Edge>> adjacencyList;

    /**构造函数
     * 接收 DataFactory 传来的原始列表，
     * 并在内部将它们转换为“邻接表”结构
     */
    public GraphModel(List<Vertex> vertexList, List<Edge> edgeList) {
        this.vertices = new HashMap<>();
        this.adjacencyList = new HashMap<>();

        // 1. 初始化所有顶点
        for (Vertex v : vertexList) {
            this.vertices.put(v.getId(), v);
            this.adjacencyList.put(v.getId(), new ArrayList<>()); // 为每个顶点创建一个空的邻接“链表”
        }

        // 2. 遍历所有边，构建邻接表
        for (Edge e : edgeList) {
            // (我们按 DataFactory 的数据，构建一个有向图)
            if (this.adjacencyList.containsKey(e.getSource())) {
                this.adjacencyList.get(e.getSource()).add(e);
            }

            // --- 新增：为Prim算法添加反向边，使其成为无向图 ---
            // 创建一条权重相同，但方向相反的边
            Edge reverseEdge = new Edge(e.getTarget(), e.getSource(), e.getWeight());
            if (this.adjacencyList.containsKey(e.getTarget())) {
                this.adjacencyList.get(e.getTarget()).add(reverseEdge);
            }
        }
    }

    // --- 新增：供“算法”使用的方法 (体现邻接表结构) ---

    /**获取一个顶点的所有邻居（出边）**/
    public List<Edge> getNeighbors(String vertexId) {
        return this.adjacencyList.getOrDefault(vertexId, new ArrayList<>());
    }

    /**获取单个顶点对象**/
    public Vertex getVertex(String vertexId) {
        return this.vertices.get(vertexId);
    }

    // --- 保持不变：供“绘图”使用的方法 (兼容 HelloController) ---

    /** 获取所有顶点对象（供 drawGraph 使用）**/
    public List<Vertex> getNodes() {
        return new ArrayList<>(this.vertices.values());
    }

    /** 获取所有边（供 drawGraph 使用）**/
    public List<Edge> getEdges() {
        List<Edge> allEdges = new ArrayList<>();
        for (List<Edge> edges : this.adjacencyList.values()) {
            allEdges.addAll(edges);
        }
        return allEdges;
    }

    /** 添加点 **/
    public boolean addVertex(Vertex v) {
        if (vertices.containsKey(v.getId())) {
            return false; // 顶点ID已存在
        }
        vertices.put(v.getId(), v);
        adjacencyList.put(v.getId(), new ArrayList<>()); // 在邻接表中创建空列表
        return true;
    }

    /** 添加边 **/
    public boolean addEdge(String sourceId, String targetId, int weight) {
        if (!vertices.containsKey(sourceId) || !vertices.containsKey(targetId)) {
            return false; // 顶点不存在
        }
        // 正向添加
        adjacencyList.get(sourceId).add(new Edge(sourceId, targetId, weight));
        // 反向添加 (因为我们是无向图)
        adjacencyList.get(targetId).add(new Edge(targetId, sourceId, weight));
        return true;
    }

    /** 根据坐标找点 **/
    public Vertex getVertexAt(double x, double y) {
        double tolerance = 20; // 20 像素的点击误差范围
        for (Vertex v : vertices.values()) {
            double dx = v.getX() - x;
            double dy = v.getY() - y;
            // 使用勾股定理计算距离，(dx*dx + dy*dy) < tolerance*tolerance 效率更高
            if (dx * dx + dy * dy < tolerance * tolerance) {
                return v;
            }
        }
        return null; // 没找到
    }

    /** 清空图 **/
    public void clear() {
        vertices.clear();
        adjacencyList.clear();
    }
}