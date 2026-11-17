package com.homoonme.algovisualizer;
import java.util.ArrayList;
import java.util.List;

public class SimpleSelectionSort {

    public List<SortStepSnapshot> generateSteps(List<Integer> originalData) {
        List<SortStepSnapshot> steps = new ArrayList<>();
        List<Integer> data = new ArrayList<>(originalData);
        steps.add(new SortStepSnapshot(data, List.of(), "初始数组"));

        for (int i = 0; i < data.size() - 1; i++) {
            int minIndex = i; // 假设当前是最小
            steps.add(new SortStepSnapshot(data, List.of(i), "开始新一轮查找，假定 " + data.get(i) + " 最小"));

            for (int j = i + 1; j < data.size(); j++) {
                // 高亮比较的两个元素
                steps.add(new SortStepSnapshot(data, List.of(minIndex, j), "比较 " + data.get(j) + " 和 " + data.get(minIndex)));
                if (data.get(j) < data.get(minIndex)) {
                    minIndex = j; // 找到了更小的值
                    steps.add(new SortStepSnapshot(data, List.of(minIndex), "找到新的最小值: " + data.get(minIndex)));
                }
            }

            // 循环结束，minIndex 存放了最小值的索引
            if (minIndex != i) {
                steps.add(new SortStepSnapshot(data, List.of(i, minIndex), "一轮结束，交换 " + data.get(i) + " 和 " + data.get(minIndex)));
                // 交换
                int temp = data.get(i);
                data.set(i, data.get(minIndex));
                data.set(minIndex, temp);
                steps.add(new SortStepSnapshot(data, List.of(i, minIndex), "交换完成"));
            } else {
                steps.add(new SortStepSnapshot(data, List.of(i), "一轮结束，位置 " + i + " 已是最小，无需交换"));
            }
        }
        steps.add(new SortStepSnapshot(data, List.of(), "排序完成"));
        return steps;
    }
}