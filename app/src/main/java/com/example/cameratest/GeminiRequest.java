package com.example.cameratest;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GeminiRequest {
    @SerializedName("contents")
    private List<Content> contents;

    @SerializedName("generationConfig")
    private GenerationConfig generationConfig;

    public GeminiRequest(Content content) {
        this.contents = new ArrayList<>();
        this.contents.add(content);
        this.generationConfig = new GenerationConfig();
    }

    public static GeminiRequest createTextRequest(String text) {
        List<Part> parts = new ArrayList<>();
        parts.add(new Part(text));
        return new GeminiRequest(new Content(parts));
    }

    public static GeminiRequest createImageTextRequest(String text, String base64Image) {
        List<Part> parts = new ArrayList<>();

        // Add the image part first with correct MIME type
        Part.Blob imageBlob = new Part.Blob("image/jpeg", base64Image);
        parts.add(new Part(imageBlob));

        // Add the text part
        if (text != null && !text.isEmpty()) {
            parts.add(new Part(text));
        }

        return new GeminiRequest(new Content(parts));
    }

    // Inner class for generation configuration
    public static class GenerationConfig {
        @SerializedName("temperature")
        private float temperature;

        @SerializedName("topK")
        private int topK;

        @SerializedName("topP")
        private float topP;

        @SerializedName("maxOutputTokens")
        private int maxOutputTokens;

        public GenerationConfig() {
            this.temperature = 0.4f;
            this.topK = 32;
            this.topP = 1.0f;
            this.maxOutputTokens = 2048;
        }
    }
}