# Algorithm Visualizer JavaFX

使用 JavaFX 实现的数据结构与算法可视化工具，展示排序算法和图算法的执行过程。

## 功能

- 冒泡排序
- 直接插入排序
- 简单选择排序
- 快速排序
- BFS
- DFS
- Dijkstra
- Prim
- Kruskal
- 图和排序数据快照
- Graph DSL 解析
- 可选的大模型自然语言生成图结构

## 技术栈

- Java 21
- JavaFX 21
- FXML
- Maven
- JUnit 5

## 运行

```powershell
.\mvnw.cmd clean javafx:run
```

或者先打包：

```powershell
.\mvnw.cmd clean package
```

## 可选：自然语言生成图

`LLMService` 支持通过硅基流动 OpenAI-compatible API 将自然语言转换成图 DSL。API Key 不写入源码，而是从环境变量读取：

```powershell
$env:SILICONFLOW_API_KEY="你的 API Key"
.\mvnw.cmd javafx:run
```

未配置 Key 时仍可使用本地规则和手动输入。

## 项目结构

```text
algorithm-visualizer-javafx/
├─ src/main/java/com/homoonme/algovisualizer/
│  ├─ HelloApplication.java
│  ├─ HelloController.java
│  ├─ *Sort.java
│  ├─ *Algorithm.java
│  ├─ GraphDSLParser.java
│  └─ LLMService.java
├─ src/main/resources/
├─ pom.xml
└─ README.md
```

## 说明

- 图算法和排序算法都通过快照记录中间状态，便于逐步演示。
- 仓库不包含 API Key、IDE 配置、构建产物或 JavaFX SDK 安装包。
- 大模型接口是可选能力，核心可视化功能不依赖网络。
