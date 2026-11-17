package com.homoonme.algovisualizer;

import java.util.ArrayList;
import java.util.List;

/** 数据工厂
 * 负责在代码中“硬编码”创建我们的数据模型
 */
public class DataFactory {

    public static GraphModel createSampleGraph() {
        List<Vertex> nodes = new ArrayList<>();
        nodes.add(new Vertex("1", 100, 100));
        nodes.add(new Vertex("2", 300, 50));
        nodes.add(new Vertex("3", 500, 100));
        nodes.add(new Vertex("4", 450, 250));
        nodes.add(new Vertex("5", 250, 200));
        nodes.add(new Vertex("6", 300, 400));
        nodes.add(new Vertex("7", 50, 300));

        List<Edge> edges = new ArrayList<>();
        edges.add(new Edge("1", "2", 19));
        edges.add(new Edge("1", "5", 14));
        edges.add(new Edge("1", "7", 18));
        edges.add(new Edge("2", "3", 5));
        edges.add(new Edge("2", "5", 12));
        edges.add(new Edge("3", "4", 3));
        edges.add(new Edge("4", "5", 8));
        edges.add(new Edge("4", "6", 21));
        edges.add(new Edge("5", "7", 16));
        edges.add(new Edge("7", "6", 27));

        return new GraphModel(nodes, edges);
    }

    public static SortModel createSampleSortArray() {
        List<Integer> data = List.of(70, 30, 95, 40, 60, 110, 80, 25);
        return new SortModel(new ArrayList<>(data)); // 传入一个可修改的List
    }
}