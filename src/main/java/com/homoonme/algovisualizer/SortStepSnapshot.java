package com.homoonme.algovisualizer;
import java.util.List;

public class SortStepSnapshot {
    private final List<Integer> data;
    private final List<Integer> highlightedIndices;
    private final String message;

    public SortStepSnapshot(List<Integer> data, List<Integer> highlightedIndices, String message) {
        this.data = List.copyOf(data);
        this.highlightedIndices = List.copyOf(highlightedIndices);
        this.message = message;
    }

    public List<Integer> getData() { return data; }
    public List<Integer> getHighlightedIndices() { return highlightedIndices; }
    public String getMessage() { return message; }
}