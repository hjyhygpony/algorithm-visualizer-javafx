package com.homoonme.algovisualizer;
import java.util.ArrayList;
import java.util.List;

public class QuickSort {
    private List<SortStepSnapshot> steps;

    public List<SortStepSnapshot> generateSteps(List<Integer> originalData) {
        this.steps = new ArrayList<>();
        List<Integer> data = new ArrayList<>(originalData);
        steps.add(new SortStepSnapshot(data, List.of(), "初始数组"));

        quickSort(data, 0, data.size() - 1);

        steps.add(new SortStepSnapshot(data, List.of(), "排序完成"));
        return this.steps;
    }

    // 递归主方法
    private void quickSort(List<Integer> data, int low, int high) {
        if (low < high) {
            int pivotIndex = partition(data, low, high);
            quickSort(data, low, pivotIndex - 1);
            quickSort(data, pivotIndex + 1, high);
        }
    }

    // 分区方法
    private int partition(List<Integer> data, int low, int high) {
        int pivot = data.get(high); // 选最后一个元素为基准
        int i = (low - 1); // 慢指针

        steps.add(new SortStepSnapshot(data, List.of(high), "选定基准 (Pivot): " + pivot));

        for (int j = low; j < high; j++) {
            steps.add(new SortStepSnapshot(data, List.of(j, high), "比较 " + data.get(j) + " 和基准 " + pivot));
            if (data.get(j) < pivot) {
                i++;
                steps.add(new SortStepSnapshot(data, List.of(i, j), "交换 " + data.get(i) + " 和 " + data.get(j)));
                // 交换 data[i] 和 data[j]
                int temp = data.get(i);
                data.set(i, data.get(j));
                data.set(j, temp);
                steps.add(new SortStepSnapshot(data, List.of(i, j), "交换完成"));
            }
        }

        steps.add(new SortStepSnapshot(data, List.of(i + 1, high), "交换基准 " + data.get(i + 1) + " 和 " + data.get(high)));
        // 交换 data[i+1] 和 data[high] (基准)
        int temp = data.get(i + 1);
        data.set(i + 1, data.get(high));
        data.set(high, temp);
        steps.add(new SortStepSnapshot(data, List.of(i + 1, high), "分区完成，基准归位"));

        return i + 1; // 返回基准的新索引
    }
}