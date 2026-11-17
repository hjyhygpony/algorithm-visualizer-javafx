package com.homoonme.algovisualizer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 图结构 DSL 解析器
 * 
 * 支持的 DSL 语法：
 * 
 * 1. 定义节点：
 *    node A
 *    node B at (100, 200)
 *    nodes A, B, C, D
 * 
 * 2. 定义边：
 *    edge A -> B
 *    edge A -> B : 10
 *    edge A -- B : 5    (无向边)
 *    A -> B : 10
 * 
 * 3. 快速定义（类似 Mermaid）：
 *    graph:
 *      1 -> 2 : 5
 *      2 -> 3 : 3
 *      1 -> 3 : 10
 * 
 * 4. 自然语言风格：
 *    创建节点 1, 2, 3, 4
 *    连接 1 到 2 权重 5
 *    添加边 <1,3> 权重 10
 */
public class GraphDSLParser {

    public static class ParseResult {
        public List<Vertex> vertices = new ArrayList<>();
        public List<Edge> edges = new ArrayList<>();
        public String errorMessage = null;
        public boolean success = true;
    }

    // 画布尺寸，用于自动布局
    private static final double CANVAS_WIDTH = 700;
    private static final double CANVAS_HEIGHT = 400;
    private static final double MARGIN = 60;

    /**
     * 解析 DSL 文本，生成图结构
     */
    public ParseResult parse(String dslText) {
        ParseResult result = new ParseResult();
        List<String> nodeIds = new ArrayList<>();
        List<int[]> edgeData = new ArrayList<>(); // [sourceIndex, targetIndex, weight]

        String[] lines = dslText.split("\n");
        
        for (int lineNum = 0; lineNum < lines.length; lineNum++) {
            String line = lines[lineNum].trim();
            
            // 跳过空行和注释
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
                continue;
            }
            
            // 跳过 "graph:" 标记
            if (line.equalsIgnoreCase("graph:") || line.equalsIgnoreCase("graph")) {
                continue;
            }

            try {
                // 尝试解析各种格式
                if (!parseNodeDefinition(line, nodeIds) &&
                    !parseEdgeDefinition(line, nodeIds, edgeData) &&
                    !parseNaturalLanguage(line, nodeIds, edgeData)) {
                    // 无法识别的语法
                    result.errorMessage = "第 " + (lineNum + 1) + " 行语法错误: " + line;
                    result.success = false;
                    return result;
                }
            } catch (Exception e) {
                result.errorMessage = "第 " + (lineNum + 1) + " 行解析错误: " + e.getMessage();
                result.success = false;
                return result;
            }
        }

        // 自动布局节点
        layoutNodes(nodeIds, result.vertices);

        // 创建边
        for (int[] ed : edgeData) {
            String sourceId = nodeIds.get(ed[0]);
            String targetId = nodeIds.get(ed[1]);
            int weight = ed[2];
            result.edges.add(new Edge(sourceId, targetId, weight));
        }

        return result;
    }

    /**
     * 解析节点定义
     * 格式: node A, nodes A B C, 节点 1 2 3
     */
    private boolean parseNodeDefinition(String line, List<String> nodeIds) {
        // node A 或 nodes A, B, C
        Pattern nodePattern = Pattern.compile("^(?:nodes?|节点|顶点|创建节点)\\s+(.+)$", Pattern.CASE_INSENSITIVE);
        Matcher m = nodePattern.matcher(line);
        
        if (m.matches()) {
            String nodesPart = m.group(1);
            // 支持逗号、空格、顿号分隔
            String[] ids = nodesPart.split("[,，、\\s]+");
            for (String id : ids) {
                id = id.trim();
                if (!id.isEmpty() && !nodeIds.contains(id)) {
                    nodeIds.add(id);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 解析边定义
     * 格式: A -> B : 10, A -- B : 5, edge A B 10, <A,B> 10
     */
    private boolean parseEdgeDefinition(String line, List<String> nodeIds, List<int[]> edgeData) {
        // 格式1: A -> B : 10 或 A -> B
        Pattern arrowPattern = Pattern.compile("^(?:edge\\s+)?([\\w\\d]+)\\s*(?:->|→|--|—)\\s*([\\w\\d]+)(?:\\s*[:|：]\\s*(\\d+))?$");
        Matcher m = arrowPattern.matcher(line);
        
        if (m.matches()) {
            String source = m.group(1);
            String target = m.group(2);
            int weight = m.group(3) != null ? Integer.parseInt(m.group(3)) : 1;
            
            addEdge(source, target, weight, nodeIds, edgeData);
            return true;
        }

        // 格式2: <A,B> 10 或 <A,B>:10
        Pattern bracketPattern = Pattern.compile("^<?([\\w\\d]+)\\s*[,，]\\s*([\\w\\d]+)>?(?:\\s*[:|：]?\\s*(\\d+))?$");
        m = bracketPattern.matcher(line);
        
        if (m.matches()) {
            String source = m.group(1);
            String target = m.group(2);
            int weight = m.group(3) != null ? Integer.parseInt(m.group(3)) : 1;
            
            addEdge(source, target, weight, nodeIds, edgeData);
            return true;
        }

        // 格式3: edge A B 10
        Pattern simplePattern = Pattern.compile("^edge\\s+([\\w\\d]+)\\s+([\\w\\d]+)(?:\\s+(\\d+))?$", Pattern.CASE_INSENSITIVE);
        m = simplePattern.matcher(line);
        
        if (m.matches()) {
            String source = m.group(1);
            String target = m.group(2);
            int weight = m.group(3) != null ? Integer.parseInt(m.group(3)) : 1;
            
            addEdge(source, target, weight, nodeIds, edgeData);
            return true;
        }

        return false;
    }

    /**
     * 解析自然语言风格
     * 格式: 连接 A 到 B 权重 10, 添加边 A-B 权重 5
     */
    private boolean parseNaturalLanguage(String line, List<String> nodeIds, List<int[]> edgeData) {
        // 连接 A 到 B 权重 10
        Pattern connectPattern = Pattern.compile("^(?:连接|添加边|边)\\s*<?([\\w\\d]+)[,，]?\\s*(?:到|->|→)?\\s*([\\w\\d]+)>?(?:\\s*(?:权重|权值|weight)[:|：]?\\s*(\\d+))?", Pattern.CASE_INSENSITIVE);
        Matcher m = connectPattern.matcher(line);
        
        if (m.matches()) {
            String source = m.group(1);
            String target = m.group(2);
            int weight = m.group(3) != null ? Integer.parseInt(m.group(3)) : 1;
            
            addEdge(source, target, weight, nodeIds, edgeData);
            return true;
        }

        return false;
    }

    /**
     * 添加边（自动创建不存在的节点）
     */
    private void addEdge(String source, String target, int weight, List<String> nodeIds, List<int[]> edgeData) {
        // 自动添加不存在的节点
        if (!nodeIds.contains(source)) {
            nodeIds.add(source);
        }
        if (!nodeIds.contains(target)) {
            nodeIds.add(target);
        }
        
        int sourceIndex = nodeIds.indexOf(source);
        int targetIndex = nodeIds.indexOf(target);
        edgeData.add(new int[]{sourceIndex, targetIndex, weight});
    }

    /**
     * 自动布局节点（圆形布局）
     */
    private void layoutNodes(List<String> nodeIds, List<Vertex> vertices) {
        int n = nodeIds.size();
        if (n == 0) return;

        double centerX = CANVAS_WIDTH / 2;
        double centerY = CANVAS_HEIGHT / 2;
        double radius = Math.min(CANVAS_WIDTH, CANVAS_HEIGHT) / 2 - MARGIN;

        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n - Math.PI / 2; // 从顶部开始
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            vertices.add(new Vertex(nodeIds.get(i), x, y));
        }
    }


}
