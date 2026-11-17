package com.homoonme.algovisualizer;

public class Edge implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private String source;
    private String target;
    private int weight;


    public Edge() {}

    public Edge(String source, String target, int weight) {
        this.source = source;
        this.target = target;
        this.weight = weight;
    }


    // Getters
    public String getSource() { return source; }
    public String getTarget() { return target; }
    public int getWeight() { return weight; }
}