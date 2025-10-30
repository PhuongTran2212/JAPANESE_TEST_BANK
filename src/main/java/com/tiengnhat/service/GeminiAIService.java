// File: com/tiengnhat/service/GeminiAIService.java
package com.tiengnhat.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeminiAIService {

    private static final String MODEL_NAME = "gemini-1.5-flash-latest"; // Hoặc "gemini-pro"
    private static final String GEMINI_API_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    // !!! CẢNH BÁO AN NINH !!!
    // THAY THẾ BẰNG API KEY THẬT CỦA BẠN CHO TEST CỤC BỘ.
    private static final String YOUR_API_KEY_HERE_FOR_LOCAL_TESTING = "AIzaSyB6zo40asf2QOsjOOMRYT8aUwsiE2E0w18"; // <--- THAY API KEY CỦA BẠN VÀO ĐÂY

    private static String API_KEY;
    private static HttpClient httpClient;

    private static final ExecutorService executor = Executors.newCachedThreadPool(runnable -> {
        Thread t = new Thread(runnable);
        t.setDaemon(true);
        return t;
    });

    static {
        API_KEY = YOUR_API_KEY_HERE_FOR_LOCAL_TESTING; // Gán trực tiếp

        if (API_KEY == null || API_KEY.trim().isEmpty() || API_KEY.equals("YOUR_ACTUAL_API_KEY")) {
            System.err.println("LỖI: API Key của Gemini chưa được cấu hình đúng trong GeminiAIService.java!");
            // Cân nhắc throw new RuntimeException("API Key không hợp lệ...");
        } else {
            System.out.println("INFO: Đang sử dụng API Key được cung cấp trực tiếp cho test cục bộ.");
        }

        httpClient = HttpClient.newBuilder()
                .executor(executor)
                .connectTimeout(Duration.ofSeconds(20))
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    public static CompletableFuture<String> getSuggestionsFromTextAsync(String inputText, String customPrompt) {
        CompletableFuture<String> futureResult = new CompletableFuture<>();

        if (API_KEY == null || API_KEY.trim().isEmpty() || API_KEY.equals("YOUR_ACTUAL_API_KEY")) {
            futureResult.completeExceptionally(
                new IllegalStateException("API Key của Gemini chưa được cấu hình đúng.")
            );
            return futureResult;
        }

        if (inputText == null || inputText.trim().isEmpty()) {
            futureResult.complete("Vui lòng cung cấp văn bản chứa câu hỏi.");
            return futureResult;
        }

        try {
            JSONObject contentPart = new JSONObject();
            contentPart.put("text", customPrompt + "\n\n--- VĂN BẢN CẦN PHÂN TÍCH ---\n" + inputText);

            JSONObject contents = new JSONObject();
            contents.put("parts", new JSONArray().put(contentPart));

            JSONArray generationContents = new JSONArray().put(contents);

            JSONObject requestBodyJson = new JSONObject();
            requestBodyJson.put("contents", generationContents);

            // --- SỬA ĐỔI Ở ĐÂY ---
            // (Tùy chọn) Cấu hình GenerationConfig
            JSONObject generationConfig = new JSONObject();
            // Thử nghiệm với temperature:
            // - Giá trị thấp (ví dụ: 0.2 - 0.4): Kết quả ít ngẫu nhiên, có tính quyết định hơn, bám sát hơn.
            // - Giá trị cao (ví dụ: 0.7 - 0.9): Kết quả sáng tạo hơn, đa dạng hơn, nhưng có thể lan man.
            generationConfig.put("temperature", 0.3); // <--- GIÁ TRỊ THỬ NGHIỆM (1)
            generationConfig.put("maxOutputTokens", 4096);
            // generationConfig.put("topK", 40); // Có thể bỏ comment và thử nghiệm
            // generationConfig.put("topP", 0.95); // Có thể bỏ comment và thử nghiệm
            requestBodyJson.put("generationConfig", generationConfig);
            // --- KẾT THÚC SỬA ĐỔI ---


            JSONArray safetySettingsArray = new JSONArray();
            String[] harmCategories = {"HARM_CATEGORY_HARASSMENT", "HARM_CATEGORY_HATE_SPEECH", "HARM_CATEGORY_SEXUALLY_EXPLICIT", "HARM_CATEGORY_DANGEROUS_CONTENT"};
            for (String category : harmCategories) {
                JSONObject setting = new JSONObject();
                setting.put("category", category);
                // "BLOCK_MEDIUM_AND_ABOVE" là mức cân bằng.
                // "BLOCK_LOW_AND_ABOVE" chặt hơn.
                // "BLOCK_ONLY_HIGH" nới lỏng hơn.
                // "BLOCK_NONE" cho phép tất cả (cẩn thận khi dùng).
                setting.put("threshold", "BLOCK_MEDIUM_AND_ABOVE");
                safetySettingsArray.put(setting);
            }
            requestBodyJson.put("safetySettings", safetySettingsArray);


            String requestBodyString = requestBodyJson.toString();
            String apiUrl = String.format(GEMINI_API_URL_TEMPLATE, MODEL_NAME, API_KEY);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyString, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(90))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                    .thenApply(response -> {
                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            return parseGeminiResponse(response.body());
                        } else {
                            String errorMsg = "Lỗi từ Gemini API: " + response.statusCode() + "\n";
                            try {
                                JSONObject errorJson = new JSONObject(response.body());
                                if (errorJson.has("error") && errorJson.get("error") instanceof JSONObject) {
                                    JSONObject errorDetails = errorJson.getJSONObject("error");
                                    errorMsg += "Message: " + errorDetails.optString("message", "Không có thông điệp lỗi chi tiết.");
                                } else { errorMsg += response.body(); }
                            } catch (JSONException e) { errorMsg += "Không thể parse nội dung lỗi JSON: " + response.body(); }
                            System.err.println(errorMsg);
                            throw new RuntimeException(errorMsg);
                        }
                    })
                    .thenAccept(futureResult::complete)
                    .exceptionally(ex -> {
                        System.err.println("Lỗi khi gọi Gemini API: " + ex.getMessage());
                        futureResult.completeExceptionally(ex);
                        return null;
                    });

        } catch (JSONException e) {
            futureResult.completeExceptionally(new RuntimeException("Lỗi khi tạo JSON request: " + e.getMessage(), e));
        } catch (Exception e) {
            futureResult.completeExceptionally(new RuntimeException("Lỗi không xác định: " + e.getMessage(), e));
        }
        return futureResult;
    }

    private static String parseGeminiResponse(String responseBody) {
        // ... (Giữ nguyên logic parseGeminiResponse như trước)
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);

            if (jsonResponse.has("promptFeedback")) {
                JSONObject promptFeedback = jsonResponse.getJSONObject("promptFeedback");
                if (promptFeedback.has("blockReason")) {
                    return "Yêu cầu bị chặn bởi bộ lọc an toàn. Lý do: " + promptFeedback.getString("blockReason");
                }
            }

            if (jsonResponse.has("candidates") && jsonResponse.get("candidates") instanceof JSONArray) {
                JSONArray candidates = jsonResponse.getJSONArray("candidates");
                if (candidates.length() > 0) {
                    JSONObject firstCandidate = candidates.getJSONObject(0);
                    if (firstCandidate.has("content") && firstCandidate.getJSONObject("content").has("parts") &&
                        firstCandidate.getJSONObject("content").getJSONArray("parts").length() > 0 &&
                        firstCandidate.getJSONObject("content").getJSONArray("parts").getJSONObject(0).has("text")) {
                        return firstCandidate.getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text");
                    }
                    if (firstCandidate.has("finishReason") && !"STOP".equals(firstCandidate.optString("finishReason"))) {
                        return "AI không thể hoàn thành. Lý do: " + firstCandidate.optString("finishReason", "Không rõ");
                    }
                }
            }
            return "Không tìm thấy nội dung văn bản trong phản hồi của Gemini, hoặc phản hồi không hợp lệ.\n" + responseBody;
        } catch (JSONException e) {
            return "Lỗi khi xử lý phản hồi từ Gemini: " + e.getMessage() + "\nPhản hồi: " + responseBody;
        }
    }

    public static void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}