package com.homoonme.algovisualizer;

import java.util.List;

public class SortModel {
    private List<Integer> data;

    // 只保留带参数的构造函数
    public SortModel(List<Integer> data) {
        this.data = data;
    }

    // Getters
    public List<Integer> getData() { return data; }
}