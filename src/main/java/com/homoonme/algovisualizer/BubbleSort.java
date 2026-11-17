package com.homoonme.algovisualizer;

import java.util.ArrayList;
import java.util.List;

/**
 * 冒泡排序算法
 * 思想：重复遍历数组，比较相邻元素，如果顺序错误就交换。
 * 每一轮遍历会把最大的元素"冒泡"到末尾。
 */
public class BubbleSort {

    public List<SortStepSnapshot> generateSteps(List<Integer> originalData) {
        List<SortStepSnapshot> steps = new ArrayList<>();
        List<Integer> data = new ArrayList<>(originalData);
        int n = data.size();

        steps.add(new SortStepSnapshot(data, List.of(), "初始数组"));

        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;

            steps.add(new SortStepSnapshot(data, List.of(), 
                    "第 " + (i + 1) + " 轮冒泡开始"));

            for (int j = 0; j < n - 1 - i; j++) {
                // 高亮正在比较的两个元素
                steps.add(new SortStepSnapshot(data, List.of(j, j + 1),
                        "比较 " + data.get(j) + " 和 " + data.get(j + 1)));

                if (data.get(j) > data.get(j + 1)) {
                    // 交换
                    int temp = data.get(j);
                    data.set(j, data.get(j + 1));
                    data.set(j + 1, temp);
                    swapped = true;

                    steps.add(new SortStepSnapshot(data, List.of(j, j + 1),
                            "交换 " + data.get(j) + " 和 " + data.get(j + 1)));
                }
            }

            steps.add(new SortStepSnapshot(data, List.of(n - 1 - i),
                    "第 " + (i + 1) + " 轮结束，" + data.get(n - 1 - i) + " 已就位"));

            // 如果这一轮没有发生交换，说明已经有序
            if (!swapped) {
                steps.add(new SortStepSnapshot(data, List.of(),
                        "未发生交换，数组已有序，提前结束"));
                break;
            }
        }

        steps.add(new SortStepSnapshot(data, List.of(), "排序完成"));
        return steps;
    }
}
