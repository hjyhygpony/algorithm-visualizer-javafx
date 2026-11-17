package com.homoonme.algovisualizer;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.control.ComboBox;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.scene.control.Slider;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import javafx.scene.control.ToggleButton;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import java.util.Optional;
import java.util.ArrayList;

import javafx.scene.control.ListView;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.io.*;// 用于文件I/O和序列化
import javafx.stage.FileChooser; // 用于文件选择对话框


import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class HelloController implements Initializable {

    @FXML
    private Canvas mainCanvas;
    @FXML
    private BorderPane rootPane;

    // 按钮
    @FXML
    private Button loadGraphButton;
    @FXML
    private Button loadSortButton;
    @FXML
    private Button nextStepButton;
    @FXML
    private ComboBox<String> sortAlgoChooser;
    @FXML
    private ComboBox<String> graphAlgoChooser;
    @FXML
    private Button autoPlayButton;
    @FXML
    private Button clearButton;
    @FXML
    private Slider speedSlider;


    @FXML
    private ToggleButton addNodeToggle;
    @FXML
    private ToggleButton addEdgeToggle;

    @FXML
    private ListView<String> logListView; // 日志列表控件


    //浮动提示面板的控件
    @FXML private VBox infoPanel;
    @FXML private Label infoTitleLabel;
    @FXML private Label infoContentLabel;


    private Timeline autoPlayTimeline; // 动画计时器

    // 数据模型
    private GraphModel currentGraph;
    private SortModel currentSortModel;

    // 排序步骤
    private List<SortStepSnapshot> sortSteps;
    private List<GraphStepSnapshot> graphSteps;

    private int currentStepIndex;

    // Dijkstra 路径查询相关
    private DijkstraResult dijkstraResult = null;  // 保存 Dijkstra 完整结果
    private List<Edge> selectedPath = null;        // 当前选中的路径
    private List<String> selectedPathNodes = null; // 当前选中路径的节点

    // 选择起点模式
    private boolean waitingForStartNode = false;   // 是否正在等待用户选择起点
    private String pendingAlgorithm = null;        // 待运行的算法名称

    //编辑模式
    private enum EditMode { NONE, ADD_NODE, ADD_EDGE_START, ADD_EDGE_END, DIJKSTRA_QUERY }
    private EditMode currentEditMode = EditMode.NONE;
    private Vertex firstNodeForEdge = null; // 用于存储添加边时的第一个点

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


        // 初始时禁用“下一步”和“自动播”按钮
        nextStepButton.setDisable(true);
        autoPlayButton.setDisable(true);

        //自动播放相关
        autoPlayTimeline = new Timeline();
        autoPlayTimeline.setOnFinished(e -> {
            // 动画播放完成，自动重置按钮文本
            autoPlayButton.setText("自动播放");
        });


        //排序算法
        if (sortAlgoChooser != null) {
            sortAlgoChooser.getItems().addAll(
                    "直接插入排序",
                    "简单选择排序",
                    "快速排序",
                    "冒泡排序"
            );
            sortAlgoChooser.setValue("直接插入排序"); // 设置默认值
        }

        //图算法
        if (graphAlgoChooser != null) {
            graphAlgoChooser.getItems().addAll(
                    "广度优先遍历",
                    "深度优先遍历",
                    "Prim 最小生成树",
                    "Kruskal 最小生成树",
                    "Dijkstra 最短路径"
            );
            graphAlgoChooser.setValue("广度优先遍历");
        }



        //添加顶点
        addNodeToggle.setOnAction(e -> {
            if (addNodeToggle.isSelected()) { // 如果它被选中
                currentEditMode = EditMode.ADD_NODE;
                addEdgeToggle.setSelected(false); // 确保“添加边”按钮弹起
                // 确保我们有一个图可以编辑
                if (currentGraph == null) {
                    currentGraph = new GraphModel(new ArrayList<>(), new ArrayList<>()); //
                }
            } else {
                currentEditMode = EditMode.NONE;
            }
        });

        //添加边
        addEdgeToggle.setOnAction(e -> {
            if (addEdgeToggle.isSelected()) {
                currentEditMode = EditMode.ADD_EDGE_START;
                addNodeToggle.setSelected(false); // 确保“添加顶点”按钮弹起
                firstNodeForEdge = null; // 重置
            } else {
                currentEditMode = EditMode.NONE;
            }
        });

        //鼠标点击事件
        mainCanvas.setOnMouseClicked(this::handleCanvasClick);
    }



    /** "加载图" 按钮点击事件 **/
    @FXML
    protected void onLoadGraphClick() {

        autoPlayTimeline.stop();
        autoPlayButton.setText("自动播放");

        //1.检查当前是否有用户手动绘制的图。
        //如果 currentGraph 存在且有节点，则不覆盖。
        boolean isUserDrawn = (currentGraph != null && !currentGraph.getNodes().isEmpty());

        if (!isUserDrawn) {
            // 如果当前画布上没有图（或图被清空了），则加载样本图
            currentGraph = DataFactory.createSampleGraph();
        }
        // 否则，保持 currentGraph 不变，继续对用户绘制的图运行算法。

        // 清空排序步骤
        currentSortModel = null;
        this.sortSteps = null;
        this.graphSteps = null;

        // 检查是否有图可以运行
        if (currentGraph == null || currentGraph.getNodes().isEmpty()) {
            // 如果依然没有图（例如用户点了“清空”后又点了“加载”），则退出
            System.err.println("当前没有可运行算法的图。");
            nextStepButton.setDisable(true);
            autoPlayButton.setDisable(true);
            return;
        }

        // 2. 获取用户选择的图算法
        String selectedAlgorithm = graphAlgoChooser.getValue();
        if (selectedAlgorithm == null) {
            selectedAlgorithm = "广度优先遍历";
        }

        // 3. 对于 Dijkstra，让用户点击选择起点
        if (selectedAlgorithm.equals("Dijkstra 最短路径")) {
            // 先绘制图，然后进入"选择起点"模式
            drawGraph(null);
            waitingForStartNode = true;
            pendingAlgorithm = selectedAlgorithm;
            showFloatingFeedback("选择起点", "请点击一个节点作为 Dijkstra 算法的起点", "");
            
            // 确保退出编辑模式
            addNodeToggle.setSelected(false);
            addEdgeToggle.setSelected(false);
            currentEditMode = EditMode.NONE;
            return;
        }

        // 4. 其他算法使用默认起点
        String startNodeId;
        if (isUserDrawn) {
            startNodeId = currentGraph.getNodes().get(0).getId();
        } else {
            startNodeId = "1";
        }

        runGraphAlgorithm(selectedAlgorithm, startNodeId);
    }

    /** 运行图算法（内部方法）**/
    private void runGraphAlgorithm(String selectedAlgorithm, String startNodeId) {
        showFloatingInfo(selectedAlgorithm, "图");

        // 根据选择，调用算法生成步骤
        switch (selectedAlgorithm) {
            case "广度优先遍历":
                BreadthFirstSearch bfs = new BreadthFirstSearch();
                this.graphSteps = bfs.generateSteps(currentGraph, startNodeId);
                break;
            case "深度优先遍历":
                DepthFirstSearch dfs = new DepthFirstSearch();
                this.graphSteps = dfs.generateSteps(currentGraph, startNodeId);
                break;
            case "Prim 最小生成树":
                PrimAlgorithm prim = new PrimAlgorithm();
                this.graphSteps = prim.generateSteps(currentGraph, startNodeId);
                break;
            case "Kruskal 最小生成树":
                KruskalAlgorithm kruskal = new KruskalAlgorithm();
                this.graphSteps = kruskal.generateSteps(currentGraph, startNodeId);
                break;
            case "Dijkstra 最短路径":
                DijkstraAlgorithm dijkstra = new DijkstraAlgorithm();
                this.dijkstraResult = dijkstra.generateResult(currentGraph, startNodeId);
                this.graphSteps = dijkstraResult.getSteps();
                break;
            default:
                return;
        }

        // 重置计数器并绘制初始状态
        this.currentStepIndex = 0;
        this.selectedPath = null;
        this.selectedPathNodes = null;

        if (logListView != null) logListView.getItems().clear();

        if (this.graphSteps != null && !this.graphSteps.isEmpty()) {
            drawGraph(this.graphSteps.get(0));
            updateLogList();
            nextStepButton.setDisable(false);
            autoPlayButton.setDisable(false);
        }

        // 确保退出编辑模式
        addNodeToggle.setSelected(false);
        addEdgeToggle.setSelected(false);
        currentEditMode = EditMode.NONE;
        firstNodeForEdge = null;
    }

    /**"加载排序" 按钮点击事件**/
    @FXML
    protected void onLoadSortClick() {

        autoPlayTimeline.stop();
        autoPlayButton.setText("自动播放");

        //清空图
        currentGraph = null;
        this.graphSteps = null;

        currentSortModel = DataFactory.createSampleSortArray();

        // 1. 获取用户选择的算法
        String selectedAlgorithm = sortAlgoChooser.getValue();
        if (selectedAlgorithm == null) {
            selectedAlgorithm = "直接插入排序"; // 默认
        }
        
        showFloatingInfo(selectedAlgorithm, "排序");

        // 2. 根据选择，实例化不同的算法类
        switch (selectedAlgorithm) {
            case "直接插入排序":
                DirectInsertionSort sorter = new DirectInsertionSort();
                this.sortSteps = sorter.generateSteps(currentSortModel.getData());
                break;
            case "简单选择排序":
                SimpleSelectionSort selectionSorter = new SimpleSelectionSort();
                this.sortSteps = selectionSorter.generateSteps(currentSortModel.getData());
                break;
            case "快速排序":
                QuickSort quickSorter = new QuickSort();
                this.sortSteps = quickSorter.generateSteps(currentSortModel.getData());
                break;
            case "冒泡排序":
                BubbleSort bubbleSorter = new BubbleSort();
                this.sortSteps = bubbleSorter.generateSteps(currentSortModel.getData());
                break;
            default:
                throw new IllegalStateException("未知的算法: " + selectedAlgorithm);
        }

        // 3. 重置计数器并绘制
        this.currentStepIndex = 0;
        drawSortArray(this.sortSteps.get(0));

        if (logListView != null) logListView.getItems().clear();//清空旧日志


        updateLogList();

        nextStepButton.setDisable(false);
        autoPlayButton.setDisable(false);
    }


    /**"下一步" 按钮点击事件**/
    @FXML
    protected void onNextStepClick() {

        // 检查是否在“排序模式”
        if (sortSteps != null) {
            if (currentStepIndex < sortSteps.size() - 1) {
                currentStepIndex++;
//                SortStepSnapshot snapshot = sortSteps.get(currentStepIndex); //
//                drawSortArray(snapshot);
                draw();
            } else {
                nextStepButton.setDisable(true);
                autoPlayTimeline.stop();
                autoPlayButton.setText("自动播放");
                System.out.println("排序已完成");
            }
            // 检查是否在“图模式”
        } else if (graphSteps != null) {
            if (currentStepIndex < graphSteps.size() - 1) {
                currentStepIndex++;
//                GraphStepSnapshot snapshot = graphSteps.get(currentStepIndex);
//                drawGraph(snapshot);
                draw();
            } else {
                nextStepButton.setDisable(true);
                autoPlayTimeline.stop();
                autoPlayButton.setText("自动播放");
                System.out.println("图遍历已完成");
            }
        }
    }

    /**"自动播放" 按钮点击事件**/
    @FXML
    protected void onAutoPlayClick() {
        if (autoPlayTimeline.getStatus() == Timeline.Status.RUNNING) {
            // 如果正在播放，就暂停
            autoPlayTimeline.pause();
            autoPlayButton.setText("自动播放");
        } else {
            // 如果已暂停，就开始/继续播放
            // 1. 设置KeyFrame：告诉Timeline“要做什么”
            //    我们要做的就是调用 onNextStepClick()
            autoPlayTimeline.getKeyFrames().setAll(
                    new KeyFrame(Duration.ZERO, e -> onNextStepClick()),
                    // 2. 设置播放间隔：根据滑块的值来决定
                    //    滑块值越大，速度越慢 (Duration越大)
                    //    我们用 2.1 减去滑块值来反转它 (值1.0 -> 1.1s, 值2.0 -> 0.1s)
                    new KeyFrame(Duration.seconds(2.1 - speedSlider.getValue()))
            );

            // 3. 设置循环次数
            //    我们不知道有多少步，所以设为无限循环
            //    onNextStepClick 会在结束时自动禁用按钮，Timeline也会停止
            autoPlayTimeline.setCycleCount(Timeline.INDEFINITE);
            autoPlayTimeline.play();
            autoPlayButton.setText("暂停");
        }
    }

    /**"清空" 按钮点击事件**/
    @FXML
    protected void onClearClick() {
        // 1. 停止任何正在播放的动画
        autoPlayTimeline.stop();

        // 2. 清空所有数据模型
        currentGraph = null;
        currentSortModel = null;
        sortSteps = null;
        graphSteps = null;
        dijkstraResult = null;
        selectedPath = null;
        selectedPathNodes = null;
        waitingForStartNode = false;
        pendingAlgorithm = null;

        // 3. 重置UI状态
        nextStepButton.setDisable(true);
        autoPlayButton.setDisable(true);
        autoPlayButton.setText("自动播放");

        // 4. 清空画布
        GraphicsContext gc = mainCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());

        if (logListView != null) {
            logListView.getItems().clear(); //清空日志
        }

        // --- 重置编辑模式 ---
        if (currentGraph != null) {
            currentGraph.clear(); // 清空图模型内部数据
        }
        addNodeToggle.setSelected(false);
        addEdgeToggle.setSelected(false);
        currentEditMode = EditMode.NONE;
        firstNodeForEdge = null;
    }

    /** 通用的重绘方法，判断画图还是画数组**/
    private void draw() {
        if (sortSteps != null && !sortSteps.isEmpty()) {
            drawSortArray(sortSteps.get(currentStepIndex));
            updateLogList();
        } else if (graphSteps != null && !graphSteps.isEmpty()) {
            drawGraph(graphSteps.get(currentStepIndex));
            updateLogList();
        } else if (currentGraph != null) {
            drawGraph(null); // 绘制静态图
        } else if (currentSortModel != null) { //
            drawSortArray((SortStepSnapshot) null); // 绘制静态排序
        }
    }

    /**绘制图形**/
    private void drawGraph() {
        if (currentGraph == null) return;
        // 默认没有快照，所有节点和边都是默认颜色
        drawGraph(null);
    }

    /**绘制图形，根据快照高亮特定元素 **/
    private void drawGraph(GraphStepSnapshot snapshot) {
        if (currentGraph == null) return;
        GraphicsContext gc = mainCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());

        Map<String, Vertex> vertexMap = currentGraph.getNodes().stream()
                .collect(Collectors.toMap(Vertex::getId, v -> v));

        // 1. 绘制所有边
        gc.setLineWidth(2);
        for (Edge edge : currentGraph.getEdges()) {
            Vertex source = vertexMap.get(edge.getSource());
            Vertex target = vertexMap.get(edge.getTarget());
            if (source != null && target != null) {
                // --- 根据快照和选中路径设置颜色 ---
                if (selectedPath != null && selectedPath.contains(edge)) {
                    gc.setStroke(Color.BLUE); // 选中的最短路径 - 蓝色高亮
                    gc.setLineWidth(5);
                } else if (snapshot != null && snapshot.getPathEdges().contains(edge)) {
                    gc.setStroke(Color.GREEN); // 最短路径树的边
                    gc.setLineWidth(4);
                } else if (snapshot != null && snapshot.getHighlightedEdges().contains(edge)) {
                    gc.setStroke(Color.ORANGERED); // 正在处理的边
                    gc.setLineWidth(4);
                } else {
                    gc.setStroke(Color.GRAY); // 默认边
                    gc.setLineWidth(2);
                }

                gc.strokeLine(source.getX(), source.getY(), target.getX(), target.getY());

                // 绘制权重 (颜色也做相应变化)
                double midX = (source.getX() + target.getX()) / 2;
                double midY = (source.getY() + target.getY()) / 2;
                gc.setFill(gc.getStroke()); // 文字颜色随线条颜色
                gc.setFont(new Font("Arial", 14));
                gc.fillText(String.valueOf(edge.getWeight()), midX, midY - 5);
            }
        }

        // 2. 绘制所有顶点
        double vertexRadius = 20;
        for (Vertex vertex : currentGraph.getNodes()) {
            // --- 根据快照和选中路径设置颜色 ---
            if (selectedPathNodes != null && selectedPathNodes.contains(vertex.getId())) {
                gc.setFill(Color.DODGERBLUE); // 选中路径上的节点 - 蓝色
            } else if (snapshot != null && snapshot.getHighlightedNodeIds().contains(vertex.getId())) {
                gc.setFill(Color.ORANGERED); // 正在处理的节点
            } else if (snapshot != null && snapshot.getVisitedNodeIds().contains(vertex.getId())) {
                gc.setFill(Color.LIGHTGRAY); // 已访问的节点
            } else {
                gc.setFill(Color.SKYBLUE); // 默认节点
            }

            gc.fillOval(vertex.getX() - vertexRadius, vertex.getY() - vertexRadius, vertexRadius * 2, vertexRadius * 2);
            gc.setStroke(Color.DARKBLUE);
            gc.setLineWidth(1.5);
            gc.strokeOval(vertex.getX() - vertexRadius, vertex.getY() - vertexRadius, vertexRadius * 2, vertexRadius * 2);

            // 绘制节点ID
            gc.setFill(Color.BLACK);
            gc.setFont(new Font("Arial Bold", 16));
            gc.fillText(vertex.getId(), vertex.getX() - 5, vertex.getY() + 5);

            // --- Dijkstra: 绘制距离标签 ---
            if (snapshot != null && snapshot.getNodeDistances() != null) {
                Integer dist = snapshot.getNodeDistances().get(vertex.getId());
                if (dist != null) {
                    String distStr = (dist == Integer.MAX_VALUE) ? "∞" : String.valueOf(dist);
                    gc.setFill(Color.DARKGREEN);
                    gc.setFont(new Font("Arial Bold", 12));
                    gc.fillText("d=" + distStr, vertex.getX() - 15, vertex.getY() - vertexRadius - 5);
                }
            }
        }

        // 3. 在左上角绘制步骤信息
        if (snapshot != null) {
            gc.setFill(Color.BLACK);
            gc.setFont(new Font("Arial Bold", 16));
            gc.fillText(snapshot.getMessage(), 10, 20);
        }
    }

    /**绘制排序数组**/
    private void drawSortArray(SortStepSnapshot snapshot) {
        if (snapshot == null && currentSortModel == null) return;

        List<Integer> data = (snapshot != null) ? snapshot.getData() : currentSortModel.getData();
        if (data == null) return;

        GraphicsContext gc = mainCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());

        double canvasWidth = mainCanvas.getWidth();
        double canvasHeight = mainCanvas.getHeight();
        double barWidth = canvasWidth / data.size() - 5;
        double padding = 20;

        int maxVal = 0;
        for (int val : data) {
            if (val > maxVal) maxVal = val;
        }
        if (maxVal == 0) maxVal = 1;

        for (int i = 0; i < data.size(); i++) {
            double barHeight = (data.get(i) / (double) maxVal) * (canvasHeight - 2 * padding);
            double x = i * (barWidth + 5) + 2.5;
            double y = canvasHeight - barHeight - padding;

            if (snapshot != null && snapshot.getHighlightedIndices().contains(i)) {
                gc.setFill(Color.RED);
            } else {
                gc.setFill(Color.CORNFLOWERBLUE);
            }

            gc.fillRect(x, y, barWidth, barHeight);

            gc.setFill(Color.BLACK);
            gc.setFont(new Font("Arial", 12));
            gc.fillText(String.valueOf(data.get(i)), x + barWidth / 2 - 10, canvasHeight - padding + 15);
        }

        if (snapshot != null) {
            gc.setFill(Color.BLACK);
            gc.setFont(new Font("Arial Bold", 16));
            gc.fillText(snapshot.getMessage(), 10, 20);
        }
    }


    /**处理画布的鼠标点击事件**/
    private void handleCanvasClick(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();

        // --- 选择起点模式：用户点击选择 Dijkstra 起点 ---
        if (waitingForStartNode && currentGraph != null) {
            Vertex clickedVertex = currentGraph.getVertexAt(x, y);
            if (clickedVertex != null) {
                waitingForStartNode = false;
                String startNodeId = clickedVertex.getId();
                runGraphAlgorithm(pendingAlgorithm, startNodeId);
                pendingAlgorithm = null;
                return;
            }
        }

        // --- Dijkstra 路径查询模式：算法完成后点击节点查看路径 ---
        if (dijkstraResult != null && graphSteps != null
                && currentStepIndex == graphSteps.size() - 1 && currentGraph != null) {
            Vertex clickedVertex = currentGraph.getVertexAt(x, y);
            if (clickedVertex != null) {
                String targetId = clickedVertex.getId();
                Integer dist = dijkstraResult.getDistances().get(targetId);

                if (dist != null && dist != Integer.MAX_VALUE) {
                    // 获取并高亮路径
                    selectedPath = dijkstraResult.getPathTo(targetId);
                    selectedPathNodes = dijkstraResult.getPathNodesTo(targetId);

                    // 更新消息并重绘
                    String startId = dijkstraResult.getStartNodeId();
                    GraphStepSnapshot currentSnapshot = graphSteps.get(currentStepIndex);
                    GraphStepSnapshot newSnapshot = new GraphStepSnapshot(
                            "从 " + startId + " 到 " + targetId + " 的最短路径，距离: " + dist,
                            currentSnapshot.getHighlightedNodeIds(),
                            currentSnapshot.getHighlightedEdges(),
                            currentSnapshot.getVisitedNodeIds(),
                            currentSnapshot.getPathEdges(),
                            currentSnapshot.getNodeDistances()
                    );
                    drawGraph(newSnapshot);
                } else if (dist != null) {
                    // 不可达
                    selectedPath = null;
                    selectedPathNodes = null;
                    showFloatingFeedback("不可达", "节点 " + targetId + " 从起点不可达", "");
                    drawGraph(graphSteps.get(currentStepIndex));
                }
                return; // 处理完毕，不进入编辑模式
            }
        }

        // 只有在编辑模式下才响应
        if (currentEditMode == EditMode.NONE || currentGraph == null) {
            return;
        }

        switch (currentEditMode) {
            case ADD_NODE:
                // 模式：添加顶点
                // 1. 创建新顶点
                String id = String.valueOf(currentGraph.getNodes().size()); // 简单用数字ID
                Vertex newVertex = new Vertex(id, x, y);
                currentGraph.addVertex(newVertex);
                drawGraph(null); // 重绘
                break;

            case ADD_EDGE_START:
                // 模式：选择边的第一个点
                firstNodeForEdge = currentGraph.getVertexAt(x, y);
                if (firstNodeForEdge != null) {
                    System.out.println("选中边的起点: " + firstNodeForEdge.getId());
                    currentEditMode = EditMode.ADD_EDGE_END; // 切换到“选终点”模式
                }
                break;

            case ADD_EDGE_END:
                // 模式：选择边的第二个点
                Vertex secondNode = currentGraph.getVertexAt(x, y);
                if (secondNode != null && secondNode != firstNodeForEdge) {
                    // 1. 弹出对话框让用户输入权重
                    TextInputDialog dialog = new TextInputDialog("10"); // 默认值
                    dialog.setTitle("输入权重");
                    dialog.setHeaderText("输入边 (" + firstNodeForEdge.getId() + ") 和 (" + secondNode.getId() + ") 之间的权重:");

                    Optional<String> result = dialog.showAndWait();

                    // 2. 如果用户点击了 "OK"
                    result.ifPresent(weightStr -> {
                        try {
                            int weight = Integer.parseInt(weightStr);
                            currentGraph.addEdge(firstNodeForEdge.getId(), secondNode.getId(), weight);
                            drawGraph(null); // 重绘
                        } catch (NumberFormatException ex) {
                            // (可以加一个错误提示)
                            System.err.println("输入的不是有效数字!");
                        }

                        // 3. 重置模式，准备添加下一条边
                        currentEditMode = EditMode.ADD_EDGE_START;
                        firstNodeForEdge = null;
                    });

                } else if (secondNode == firstNodeForEdge) {
                    // 如果用户点回了第一个点，取消选择
                    firstNodeForEdge = null;
                    currentEditMode = EditMode.ADD_EDGE_START;
                }
                break;
            default:
                break;
        }
    }

    /** 增量更新日志列表 **/
    private void updateLogList() {
        if (logListView == null) return;

        // 1. 确定当前使用的是哪个步骤列表
        List<?> steps = null;
        if (sortSteps != null) {
            steps = sortSteps;
        } else if (graphSteps != null) {
            steps = graphSteps;
        }

        if (steps == null) return;

        // 2. 计算目标大小：当前步数 + 1 (因为索引从0开始)
        int targetSize = currentStepIndex + 1;
        int currentListSize = logListView.getItems().size();

        // 3. 如果列表里的项比当前步数多（比如点了重新加载），先裁掉多余的
        if (currentListSize > targetSize) {
            logListView.getItems().subList(targetSize, currentListSize).clear();
            currentListSize = targetSize;
        }

        // 4. 如果列表里的项比当前步数少，把缺少的步骤补进去
        while (currentListSize < targetSize) {
            Object step = steps.get(currentListSize);
            String message = "";
            if (step instanceof SortStepSnapshot) {
                message = ((SortStepSnapshot) step).getMessage();
            } else if (step instanceof GraphStepSnapshot) {
                message = ((GraphStepSnapshot) step).getMessage();
            }
            logListView.getItems().add(message);
            currentListSize++;
        }

        // 5. 自动滚动到底部并选中最后一行（即最新的一步）
        if (!logListView.getItems().isEmpty()) {
            logListView.scrollTo(logListView.getItems().size() - 1);
            logListView.getSelectionModel().selectLast();
        }
    }



    /**显示浮动介绍面板 **/
    private void showFloatingInfo(String algorithmName, String type) {
        // --- 1. 定义算法介绍文本 (使用你已找到的资料替换这里的内容) ---
        String title = type + "算法简介：" + algorithmName;
        String content = "";

        switch (algorithmName) {
            case "直接插入排序":
                content = "思想：逐个处理待排序的记录，每个新记录都要与前面已排好序的记录进\n" +
                        "行比较，然后插入到适当的位置。\n时间复杂度：平均 O(n²)。";
                break;
            case "简单选择排序":
                content = "思想：找出未排序记录中的最小记录，然后直接与数组中第i个记录交换，比冒泡排序减少了移动次数。\n时间复杂度：总是 O(n²)。";
                break;
            case "快速排序":
                content = "思想：首先选择一个轴值(pivot)\n" +
                        "然后将序列划分为两个子序列L和R，使得L\n" +
                        "中所有记录都小于或等于轴值，R中记录都\n" +
                        "大于轴值，最后对子序列L和R递归进行快速排序。\n时间复杂度：平均 O(n log n)。";
                break;
            case "冒泡排序":
                content = "思想：重复遍历数组，比较相邻元素，如果顺序错误就交换。\n" +
                        "每一轮遍历会把最大的元素冒泡到末尾。\n" +
                        "如果某一轮没有发生交换，说明已经有序，可以提前结束。\n" +
                        "时间复杂度：平均 O(n²)，最好 O(n)。";
                break;
            case "广度优先遍历":
                content = "思想：从图中的某个顶点V出发，并在访问此顶点之后依次访问V,的所有未被访问过的邻\n" +
                        "接点，随后按这些顶点被访问的先后次序依次访问它们的邻接点，直至图中所有与V,有路径\n" +
                        "相通的顶点都被访问到为止。\n使用队列（Queue）来管理待访问的节点。";
                break;
            case "深度优先遍历":
                content = "思想：从图中某个顶点V.出发，访问此顶点，然后依次从V。的各个未被\n" +
                        "访问的邻接点出发深度优先搜索遍历图中的其余顶点，直至图中所有与V,有\n" +
                        "路径相通的顶点都被访问到为止。\n使用递归实现。";
                break;
            case "Prim 最小生成树":
                content = "思想：指定图中某个顶点v作为生成树的根，随后往生成树上添加新的顶点 w。\n" +
                        "不在添加的顶点 w和已经在生成树上的顶点v 之间必定存在一条边，并且该边的权\n" +
                        "值在所有连通顶点 v和 w之间的边中取值最小。之后继续往生成树上添加顶点，直\n" +
                        "至生成树上含有 n个顶点为止。\n";
                break;
            case "Kruskal 最小生成树":
                content = "思想：将所有边按权重从小到大排序，依次选择最小的边。\n" +
                        "如果这条边连接的两个顶点不在同一个连通分量中（不会形成环），\n" +
                        "就将这条边加入最小生成树。使用并查集来判断是否形成环。\n" +
                        "时间复杂度：O(E log E)，E为边数。";
                break;
            case "Dijkstra 最短路径":
                content = "思想：把图中所有顶点分成两组:\n" +
                        "·第一组包括已确定最短路径的顶点s\n第二组包括尚未确定最短路径的顶点u;\n" +
                        "按路径长度递增依次将第二组的顶点加到第一组中去\n" +
                        "直至u中的所有顶点都进入第一组s中\n" ;
                break;
            default:
                content = "抱歉，暂无该算法 (" + algorithmName + ") 的简介。";
        }

        // --- 2. 填充内容并显示面板 ---
        infoTitleLabel.setText(title);
        infoContentLabel.setText(content);
        infoPanel.setVisible(true);
        infoPanel.setOpacity(0.0); // 从透明开始

        // 使用淡入效果
        FadeTransition ft = new FadeTransition(Duration.millis(500), infoPanel);
        ft.setToValue(1.0);
        ft.play();
    }

    /** FXML 事件：隐藏浮动面板 (点击面板时触发) **/
    @FXML
    protected void hideFloatingInfo() {
        // 使用淡出效果
        FadeTransition ft = new FadeTransition(Duration.millis(300), infoPanel);
        ft.setToValue(0.0);

        // 淡出完成后设置不可见，确保不遮挡鼠标事件
        ft.setOnFinished(e -> infoPanel.setVisible(false));
        ft.play();
    }

    /**【扩展E1】保存当前绘制的图结构 (使用Java序列化)**/
    @FXML
    protected void onSaveGraphClick() {
        if (currentGraph == null || currentGraph.getNodes().isEmpty()) {
            // 使用浮窗提示用户没有内容可保存
            showFloatingFeedback("保存失败", "当前画布上没有可保存的图结构。", "请先绘制或加载一个图。");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存图结构文件");
        // 设置默认文件扩展名过滤器
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Graph Files (*.graph)", "*.graph"));

        // 显示保存对话框
        File file = fileChooser.showSaveDialog(rootPane.getScene().getWindow());

        if (file != null) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                // 将整个 GraphModel 对象写入文件
                oos.writeObject(currentGraph);

                showFloatingFeedback("保存成功", "图结构已成功保存！", "文件路径: " + file.getName());

            } catch (IOException e) {
                e.printStackTrace();
                showFloatingFeedback("保存失败", "无法写入文件。", "错误信息: " + e.getMessage());
            }
        }
    }

    /**从文件加载图结构 (使用Java反序列化)**/
    @FXML
    protected void onLoadGraphFromFileClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("加载图结构文件");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Graph Files (*.graph)", "*.graph"));

        File file = fileChooser.showOpenDialog(rootPane.getScene().getWindow());

        if (file != null) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Object loadedObject = ois.readObject();

                if (loadedObject instanceof GraphModel) {
                    currentGraph = (GraphModel) loadedObject;

                    // 成功加载后，清空所有算法步骤状态
                    if (autoPlayTimeline != null) autoPlayTimeline.stop();
                    currentSortModel = null;
                    sortSteps = null;
                    graphSteps = null;
                    currentStepIndex = 0;

                    // 重置 UI 状态
                    nextStepButton.setDisable(true);
                    if (autoPlayButton != null) {
                        autoPlayButton.setDisable(true);
                        autoPlayButton.setText("自动播放");
                    }
                    if (logListView != null) logListView.getItems().clear();

                    // 重绘加载的图
                    drawGraph(null);

                    showFloatingFeedback("加载成功", "图结构已从文件加载！", "文件路径: " + file.getName());

                } else {
                    throw new IOException("文件内容不是有效的图结构对象。");
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                showFloatingFeedback("加载失败", "文件读取失败或类版本不匹配。", "错误信息: " + e.getMessage());
            }
        }
    }

    /**显示一般的浮动提示信息 (用于保存/加载反馈)**/
    private void showFloatingFeedback(String title, String header, String content) {
        if (infoPanel == null) return;

        infoTitleLabel.setText(title);
        // 为了显示更多信息，将 header 和 content 合并
        infoContentLabel.setText(header + "\n\n" + content);

        infoPanel.setVisible(true);
        infoPanel.setOpacity(0.0); // 从透明开始

        // 使用淡入效果
        FadeTransition ft = new FadeTransition(Duration.millis(500), infoPanel);
        ft.setToValue(1.0);
        ft.play();

        // 注意：这里我们使用 showFloatingFeedback，而不是 showFloatingInfo，以区分用途
    }

    /** 自定义排序数组按钮点击事件 **/
    @FXML
    protected void onEditSortArrayClick() {
        // 1. 准备默认文本 (如果当前已有数据，就填进去)
        String defaultText = "50, 20, 80, 10, 30"; // 默认提示
        if (currentSortModel != null && currentSortModel.getData() != null) {
            // 将 List<Integer> 转换为 "10, 20, 30" 格式的字符串
            defaultText = currentSortModel.getData().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
        }

        // 2. 弹出输入对话框
        TextInputDialog dialog = new TextInputDialog(defaultText);
        dialog.setTitle("自定义数组");
        dialog.setHeaderText("请输入要排序的数字（用逗号分隔）:");
        dialog.setContentText("数组内容:");

        Optional<String> result = dialog.showAndWait();

        // 3. 处理用户输入
        result.ifPresent(inputStr -> {
            try {
                // 解析字符串：按逗号或空格分割 -> 去空 -> 转整数 -> 收集为List
                List<Integer> newData = new ArrayList<>();
                String[] parts = inputStr.split("[,\\s]+"); // 支持逗号或空格分隔

                for (String part : parts) {
                    if (!part.trim().isEmpty()) {
                        newData.add(Integer.parseInt(part.trim()));
                    }
                }

                if (newData.isEmpty()) {
                    showFloatingFeedback("错误", "数组不能为空", "请输入至少一个数字。");
                    return;
                }

                // 4. 更新模型并运行算法 (复用类似 onLoadSortClick 的逻辑)
                autoPlayTimeline.stop();
                autoPlayButton.setText("自动播放");

                // 清空图相关
                currentGraph = null;
                this.graphSteps = null;

                // 更新排序模型
                currentSortModel = new SortModel(newData);

                // 获取当前选择的算法
                String selectedAlgorithm = sortAlgoChooser.getValue();
                if (selectedAlgorithm == null) selectedAlgorithm = "直接插入排序";

                // 生成步骤
                switch (selectedAlgorithm) {
                    case "直接插入排序":
                        DirectInsertionSort sorter = new DirectInsertionSort();
                        this.sortSteps = sorter.generateSteps(currentSortModel.getData());
                        break;
                    case "简单选择排序":
                        SimpleSelectionSort selectionSorter = new SimpleSelectionSort();
                        this.sortSteps = selectionSorter.generateSteps(currentSortModel.getData());
                        break;
                    case "快速排序":
                        QuickSort quickSorter = new QuickSort();
                        this.sortSteps = quickSorter.generateSteps(currentSortModel.getData());
                        break;
                    case "冒泡排序":
                        BubbleSort bubbleSorter = new BubbleSort();
                        this.sortSteps = bubbleSorter.generateSteps(currentSortModel.getData());
                        break;
                    default:
                        showFloatingFeedback("错误", "未知的算法", selectedAlgorithm);
                        return;
                }

                // 重置状态并绘制
                this.currentStepIndex = 0;
                if (logListView != null) logListView.getItems().clear();

                drawSortArray(this.sortSteps.get(0));
                updateLogList();

                nextStepButton.setDisable(false);
                autoPlayButton.setDisable(false);

                showFloatingFeedback("加载成功", "自定义数组已加载", "数组长度: " + newData.size());

            } catch (NumberFormatException e) {
                showFloatingFeedback("输入错误", "无法解析输入的数字。", "请确保只输入数字和逗号。\n错误内容: " + e.getMessage());
            }
        });
    }

    /**通过文本输入构建图 **/
    // LLM 服务实例（已预设硅基流动 API Key）
    private LLMService llmService = new LLMService();

    /**通过 DSL 或自然语言构建图 **/
    @FXML
    protected void onTextGraphClick() {
        // 创建对话框
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("DSL / AI 智能构建图");
        dialog.setHeaderText(null);

        // 设置按钮
        ButtonType dslButtonType = new ButtonType("DSL 解析", ButtonBar.ButtonData.OK_DONE);
        ButtonType llmButtonType = new ButtonType("AI 生成", ButtonBar.ButtonData.APPLY);
        dialog.getDialogPane().getButtonTypes().addAll(dslButtonType, llmButtonType, ButtonType.CANCEL);

        // 创建布局
        VBox content = new VBox(10);
        
        // 说明标签
        Label helpLabel = new Label("""
            【DSL 语法】支持多种格式：
            • 箭头格式：A -> B : 10
            • 尖括号：<1,2> : 5
            • 自然语言：连接 1 到 2 权重 5
            
            【AI 生成】输入自然语言描述，如：
            "创建一个包含4个顶点1、2、3、4，边<1,2><2,3><3,4><1,4>的有向图"
            """);
        helpLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

        // 文本输入框
        TextArea textArea = new TextArea();
        textArea.setPromptText("输入 DSL 代码或自然语言描述...\n\n示例：\n1 -> 2 : 5\n2 -> 3 : 3\n3 -> 4 : 2");
        textArea.setPrefRowCount(12);
        textArea.setWrapText(true);

        // 提示标签
        Label tipLabel = new Label("💡 AI 服务：硅基流动 SiliconFlow（已配置）");
        tipLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #2196F3;");

        content.getChildren().addAll(helpLabel, textArea, tipLabel);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(500);

        // 处理按钮点击
        dialog.setResultConverter(dialogButton -> null);

        final Button dslButton = (Button) dialog.getDialogPane().lookupButton(dslButtonType);
        final Button llmButton = (Button) dialog.getDialogPane().lookupButton(llmButtonType);

        // DSL 解析按钮
        dslButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            event.consume();
            String input = textArea.getText();
            if (input != null && !input.trim().isEmpty()) {
                buildGraphFromDSL(input);
                dialog.close();
            }
        });

        // AI 生成按钮
        llmButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            event.consume();
            String input = textArea.getText();
            if (input != null && !input.trim().isEmpty()) {
                buildGraphFromLLM(input, null, textArea);
            }
        });

        dialog.showAndWait();
    }

    /**使用 DSL 解析器构建图**/
    private void buildGraphFromDSL(String dslText) {
        try {
            GraphDSLParser parser = new GraphDSLParser();
            GraphDSLParser.ParseResult result = parser.parse(dslText);

            if (!result.success) {
                showFloatingFeedback("解析错误", result.errorMessage, "请检查 DSL 语法");
                return;
            }

            if (result.vertices.isEmpty()) {
                showFloatingFeedback("解析错误", "未检测到任何节点", "请检查输入格式");
                return;
            }

            // 停止之前的动画，清空状态
            autoPlayTimeline.stop();
            autoPlayButton.setText("自动播放");

            // 构建图模型
            currentGraph = new GraphModel(result.vertices, result.edges);

            // 重置界面状态
            currentSortModel = null;
            sortSteps = null;
            graphSteps = null;
            dijkstraResult = null;
            selectedPath = null;
            selectedPathNodes = null;
            currentStepIndex = 0;

            if (logListView != null) logListView.getItems().clear();

            addNodeToggle.setSelected(false);
            addEdgeToggle.setSelected(false);
            currentEditMode = EditMode.NONE;
            firstNodeForEdge = null;

            drawGraph(null);

            showFloatingFeedback("DSL 解析成功", 
                "图结构已生成", 
                "节点数: " + result.vertices.size() + "，边数: " + result.edges.size());

            nextStepButton.setDisable(true);
            autoPlayButton.setDisable(true);

        } catch (Exception e) {
            e.printStackTrace();
            showFloatingFeedback("解析错误", "无法解析 DSL", e.getMessage());
        }
    }

    /**使用 LLM 生成图**/
    private void buildGraphFromLLM(String naturalLanguage, String apiKey, TextArea outputArea) {
        // 配置 API Key
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            llmService.setApiKey(apiKey.trim());
        }

        if (llmService.isConfigured()) {
            // 使用真实 LLM API
            showFloatingFeedback("AI 处理中", "正在调用 LLM...", "请稍候");
            
            llmService.convertToDSL(naturalLanguage).thenAccept(dsl -> {
                javafx.application.Platform.runLater(() -> {
                    if (dsl != null && !dsl.isEmpty()) {
                        outputArea.setText(dsl);
                        showFloatingFeedback("AI 生成完成", "DSL 已生成", "点击 'DSL 解析' 按钮构建图");
                    } else {
                        showFloatingFeedback("AI 生成失败", "未能生成有效的 DSL", "请尝试更清晰的描述");
                    }
                });
            }).exceptionally(ex -> {
                javafx.application.Platform.runLater(() -> {
                    showFloatingFeedback("AI 调用失败", ex.getMessage(), "请检查 API Key 和网络");
                });
                return null;
            });
        } else {
            // 使用本地模拟
            String dsl = llmService.simulateLocalLLM(naturalLanguage);
            if (dsl != null && !dsl.isEmpty()) {
                outputArea.setText(dsl);
                showFloatingFeedback("本地解析完成", 
                    "已从自然语言提取图结构", 
                    "点击 'DSL 解析' 按钮构建图\n(提示: 配置 API Key 可获得更好的 AI 理解能力)");
            } else {
                showFloatingFeedback("解析失败", 
                    "无法从输入中提取图结构", 
                    "请尝试使用 DSL 格式，或配置 API Key 使用 AI");
            }
        }
    }
}