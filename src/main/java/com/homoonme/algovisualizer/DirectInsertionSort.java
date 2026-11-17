package com.homoonme.algovisualizer;
import java.util.ArrayList;
import java.util.List;

public class DirectInsertionSort {
    public List<SortStepSnapshot> generateSteps(List<Integer> originalData) {
        List<SortStepSnapshot> steps = new ArrayList<>();
        List<Integer> data = new ArrayList<>(originalData);
        steps.add(new SortStepSnapshot(data, List.of(), "初始数组"));
        for (int i = 1; i < data.size(); i++) {
            int temp = data.get(i);
            int j = i - 1;
            steps.add(new SortStepSnapshot(data, List.of(i), "选定待插入元素 " + temp));
            while (j >= 0 && data.get(j) > temp) {
                steps.add(new SortStepSnapshot(data, List.of(i, j), "比较 " + temp + " 和 " + data.get(j)));
                data.set(j + 1, data.get(j));
                j--;
                steps.add(new SortStepSnapshot(data, List.of(j + 1, j + 2), "元素 " + data.get(j + 2) + " 后移"));
            }
            data.set(j + 1, temp);
            steps.add(new SortStepSnapshot(data, List.of(j + 1), "元素 " + temp + " 插入到位置 " + (j + 1)));
        }
        steps.add(new SortStepSnapshot(data, List.of(), "排序完成"));
        return steps;
    }
}