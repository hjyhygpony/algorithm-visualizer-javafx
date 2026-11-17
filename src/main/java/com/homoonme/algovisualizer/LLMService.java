package com.homoonme.algovisualizer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;


public class LLMService {

    // 硅基流动 API 配置
    private static final String API_URL = "https://api.siliconflow.cn/v1/chat/completions";
    private static final String MODEL = "Qwen/Qwen2.5-7B-Instruct";
    
    // 预设的 API Key
    private String apiKey = System.getenv().getOrDefault("SILICONFLOW_API_KEY", "");

    // 系统提示词
    private static final String SYSTEM_PROMPT = """
        你是一个图结构 DSL 生成器。用户会用自然语言描述一个图结构，你需要将其转换为以下 DSL 格式：
        
        DSL 语法规则：
        1. 定义边：源节点 -> 目标节点 : 权重
        2. 每行一条边
        3. 节点会自动创建，无需单独定义
        4. 如果用户没有指定权重，默认使用 1
        
        示例输入：创建一个包含4个顶点1、2、3、4，以及5条边<1,2>、<1,3>、<1,4>、<2,3>、<3,4>的有向图
        示例输出：
        1 -> 2 : 1
        1 -> 3 : 1
        1 -> 4 : 1
        2 -> 3 : 1
        3 -> 4 : 1
        
        示例输入：创建一个三角形图，节点A、B、C，边权重分别是3、4、5
        示例输出：
        A -> B : 3
        B -> C : 4
        C -> A : 5
        
        只输出 DSL 代码，不要输出任何解释或其他内容。
        """;

    public LLMService() {
    }

    public void setApiKey(String apiKey) {
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            this.apiKey = apiKey.trim();
        }
    }

    public String getApiKey() {
        return apiKey;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }

    /**
     * 异步调用 LLM，将自然语言转换为 DSL
     */
    public CompletableFuture<String> convertToDSL(String naturalLanguage) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return callLLMApi(naturalLanguage);
            } catch (Exception e) {
                throw new RuntimeException("LLM 调用失败: " + e.getMessage(), e);
            }
        });
    }

    /**
     * 同步调用硅基流动 API
     */
    private String callLLMApi(String userMessage) throws Exception {
        if (!isConfigured()) {
            throw new Exception("API Key 未配置");
        }

        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);

        // 构建请求体
        String requestBody = buildRequestBody(userMessage);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            BufferedReader errorReader = new BufferedReader(
                new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = errorReader.readLine()) != null) {
                errorResponse.append(line);
            }
            throw new Exception("API 返回错误 " + responseCode + ": " + errorResponse);
        }

        // 读取响应
        BufferedReader br = new BufferedReader(
            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }

        // 解析响应，提取生成的内容
        return parseResponse(response.toString());
    }

    /**
     * 构建请求体
     */
    private String buildRequestBody(String userMessage) {
        return String.format("""
            {
                "model": "%s",
                "messages": [
                    {"role": "system", "content": %s},
                    {"role": "user", "content": %s}
                ],
                "temperature": 0.3,
                "max_tokens": 1000
            }
            """,
            escapeJson(MODEL),
            toJsonString(SYSTEM_PROMPT),
            toJsonString(userMessage)
        );
    }

    /**
     * 解析 API 响应
     */
    private String parseResponse(String jsonResponse) {
        int contentStart = jsonResponse.indexOf("\"content\":");
        if (contentStart == -1) {
            return "";
        }
        
        contentStart = jsonResponse.indexOf("\"", contentStart + 10) + 1;
        int contentEnd = contentStart;
        boolean escaped = false;
        
        while (contentEnd < jsonResponse.length()) {
            char c = jsonResponse.charAt(contentEnd);
            if (escaped) {
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                break;
            }
            contentEnd++;
        }
        
        String content = jsonResponse.substring(contentStart, contentEnd);
        // 处理各种转义字符
        content = decodeJsonString(content);
        return content.trim();
    }

    /**
     * 解码 JSON 字符串中的转义字符
     */
    private String decodeJsonString(String s) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(i + 1);
                switch (next) {
                    case 'n' -> { result.append('\n'); i += 2; }
                    case 'r' -> { result.append('\r'); i += 2; }
                    case 't' -> { result.append('\t'); i += 2; }
                    case '"' -> { result.append('"'); i += 2; }
                    case '\\' -> { result.append('\\'); i += 2; }
                    case 'u' -> {
                        // Unicode 转义: XXXX
                        if (i + 5 < s.length()) {
                            try {
                                String hex = s.substring(i + 2, i + 6);
                                int codePoint = Integer.parseInt(hex, 16);
                                result.append((char) codePoint);
                                i += 6;
                            } catch (NumberFormatException e) {
                                result.append(c);
                                i++;
                            }
                        } else {
                            result.append(c);
                            i++;
                        }
                    }
                    default -> { result.append(c); i++; }
                }
            } else {
                result.append(c);
                i++;
            }
        }
        return result.toString();
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String toJsonString(String s) {
        return "\"" + escapeJson(s) + "\"";
    }

    /**
     * 本地规则匹配（备用方案）
     */
    public String simulateLocalLLM(String input) {
        StringBuilder dsl = new StringBuilder();
        
        // 提取边 <A,B> 格式
        java.util.regex.Pattern edgePattern = java.util.regex.Pattern.compile(
            "<([\\w\\d]+)\\s*[,，]\\s*([\\w\\d]+)>");
        java.util.regex.Matcher edgeMatcher = edgePattern.matcher(input);
        
        while (edgeMatcher.find()) {
            String source = edgeMatcher.group(1);
            String target = edgeMatcher.group(2);
            dsl.append(source).append(" -> ").append(target).append(" : 1\n");
        }
        
        if (dsl.length() == 0) {
            java.util.regex.Pattern simpleEdgePattern = java.util.regex.Pattern.compile(
                "([\\w\\d]+)\\s*(?:到|->|—|-|→)\\s*([\\w\\d]+)(?:\\s*(?:权重|权值)?\\s*[：:]?\\s*(\\d+))?");
            java.util.regex.Matcher simpleEdgeMatcher = simpleEdgePattern.matcher(input);
            
            while (simpleEdgeMatcher.find()) {
                String source = simpleEdgeMatcher.group(1);
                String target = simpleEdgeMatcher.group(2);
                String weight = simpleEdgeMatcher.group(3);
                dsl.append(source).append(" -> ").append(target)
                   .append(" : ").append(weight != null ? weight : "1").append("\n");
            }
        }
        
        return dsl.toString().trim();
    }
}

