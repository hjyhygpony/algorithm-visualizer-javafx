package com.homoonme.algovisualizer;

public class Vertex implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private double x;
    private double y;


    public Vertex() {}
    public Vertex(String id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }


    // Getters
    public String getId() { return id; }
    public double getX() { return x; }
    public double getY() { return y; }
}