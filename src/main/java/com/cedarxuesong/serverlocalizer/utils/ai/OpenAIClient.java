package com.cedarxuesong.serverlocalizer.utils.ai;

import com.cedarxuesong.serverlocalizer.utils.mylog.mylog;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OpenAI API 客户端
 */
public class OpenAIClient {
    private static final String TAG = "OpenAIClient";
    private static final ExecutorService streamExecutor = Executors.newCachedThreadPool();
    
    /**
     * 使用ApiConfig发送请求
     * @param apiName API配置名称
     * @param messages 消息列表
     * @return API响应
     */
    public static OpenAIResponse sendRequestWithConfig(String apiName, List<Message> messages) {
        ModConfig config = ModConfig.getInstance();
        String baseUrl = config.getBaseUrl(apiName);
        String apiKey = config.getApiKey(apiName);
        String model = config.getModel(apiName);
        double temperature = config.getTemperature(apiName);
        
        return sendRequest(baseUrl, apiKey, model, messages, temperature);
    }
    
    /**
     * 为流式响应启动一个新线程
     * @param apiName API配置名称
     * @param messages 消息列表
     * @param onChunk 当收到数据块时的回调
     * @param onComplete 当流结束时的回调
     * @param onError 当发生错误时的回调
     */
    public static void sendStreamRequestWithConfig(String apiName, List<Message> messages, Consumer<String> onChunk, Consumer<Usage> onComplete, Consumer<String> onError) {
        streamExecutor.submit(() -> {
            ModConfig config = ModConfig.getInstance();
            String baseUrl = config.getBaseUrl(apiName);
            String apiKey = config.getApiKey(apiName);
            String model = config.getModel(apiName);
            double temperature = config.getTemperature(apiName);
            
            HttpsURLConnection connection = null;
            Usage usage = null;
            try {
                URL url = new URL(baseUrl + "/chat/completions");
                
                connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + apiKey);
                connection.setDoOutput(true);
                
                String requestBody = buildRequestBody(model, messages, temperature, true); // 启用流式
                
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                
                int responseCode = connection.getResponseCode();
                
                if (responseCode >= 400) {
                    StringBuilder errorResponse = new StringBuilder();
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            errorResponse.append(line);
                        }
                    }
                    onError.accept("API Error: " + responseCode + " - " + errorResponse.toString());
                    return;
                }
                
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.startsWith("data:")) {
                            String data = line.substring(5).trim();
                            if ("[DONE]".equals(data)) {
                                break;
                            }
                            
                            // 尝试从数据块中解析Usage
                            Usage chunkUsage = parseUsageFromStreamData(data);
                            if (chunkUsage != null) {
                                usage = chunkUsage;
                            }
                            
                            String content = parseContentFromStreamData(data);
                            if (content != null) {
                                onChunk.accept(content);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                mylog.error(TAG, "发送流式API请求时发生错误", e);
                onError.accept("请求错误: " + e.getMessage());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                onComplete.accept(usage); // 传递usage，可能为null
            }
        });
    }

    /**
     * 发送请求到OpenAI API
     * @param baseUrl API基础URL
     * @param apiKey API密钥
     * @param model 模型名称
     * @param messages 消息列表
     * @return API响应
     */
    public static OpenAIResponse sendRequest(String baseUrl, String apiKey, String model, List<Message> messages) {
        return sendRequest(baseUrl, apiKey, model, messages, 0.7); // 使用默认温度
    }
    
    /**
     * 发送请求到OpenAI API，包含温度参数
     * @param baseUrl API基础URL
     * @param apiKey API密钥
     * @param model 模型名称
     * @param messages 消息列表
     * @param temperature 温度参数，控制随机性
     * @return API响应
     */
    public static OpenAIResponse sendRequest(String baseUrl, String apiKey, String model, List<Message> messages, double temperature) {
        HttpsURLConnection connection = null;
        try {
            // 构建请求URL
            URL url = new URL(baseUrl + "/chat/completions");
            
            // 创建连接
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setDoOutput(true);
            
            // 构建请求体
            String requestBody = buildRequestBody(model, messages, temperature, false); // 非流式
            
            // 发送请求
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // 获取响应
            int responseCode = connection.getResponseCode();
            
            // 读取响应内容
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream(),
                            StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }
            
            // 处理错误
            String error = null;
            if (responseCode >= 400) {
                error = response.toString();
                mylog.log(TAG, "API请求失败: " + error);
            }
            
            return new OpenAIResponse(response.toString(), error, responseCode);
            
        } catch (IOException e) {
            mylog.error(TAG, "发送API请求时发生错误", e);
            return new OpenAIResponse(null, e.getMessage(), 500);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    /**
     * 从流式API返回的数据中解析内容
     * @param data JSON数据字符串
     * @return 提取的内容
     */
    private static String parseContentFromStreamData(String data) {
        try {
            Pattern pattern = Pattern.compile("\"content\":\"(.*?)\"");
            Matcher matcher = pattern.matcher(data);
            if (matcher.find()) {
                // 使用反斜杠解码
                return matcher.group(1).replace("\\\\", "\\").replace("\\\"", "\"").replace("\\n", "\n").replace("\\t", "\t");
            }
        } catch (Exception e) {
            mylog.error(TAG, "解析流数据失败: " + data, e);
        }
        return null;
    }

    /**
     * 从流式API返回的数据中解析Usage信息
     * @param data JSON数据字符串
     * @return 提取的Usage对象，如果不存在则返回null
     */
    private static Usage parseUsageFromStreamData(String data) {
        try {
            if (data.contains("\"usage\"")) {
                // 模拟一个完整的响应对象来复用解析逻辑
                OpenAIResponse mockResponse = new OpenAIResponse(data, null, 200);
                return mockResponse.extractUsage();
            }
        } catch (Exception e) {
            mylog.error(TAG, "解析流数据中的Usage失败: " + data, e);
        }
        return null;
    }

    /**
     * 构建请求体JSON
     * @param stream 是否启用流式响应
     */
    private static String buildRequestBody(String model, List<Message> messages, double temperature, boolean stream) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"model\":\"").append(model).append("\",");
        json.append("\"messages\":[");
        
        for (int i = 0; i < messages.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append(messages.get(i).toJson());
        }
        
        json.append("],");
        json.append("\"temperature\":").append(temperature).append(",");
        json.append("\"stream\":").append(stream);
        json.append("}");
        
        return json.toString();
    }
} 