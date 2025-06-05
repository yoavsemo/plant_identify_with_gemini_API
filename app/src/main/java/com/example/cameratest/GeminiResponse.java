package com.example.cameratest;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GeminiResponse {
    @SerializedName("candidates")
    private List<Candidate> candidates;

    public static class Candidate {
        @SerializedName("content")
        private Content content;

        public Content getContent() {
            return content;
        }
    }

    public String getText() {
        if (candidates != null && !candidates.isEmpty() &&
                candidates.get(0).content != null &&
                candidates.get(0).content.parts != null &&
                !candidates.get(0).content.parts.isEmpty()) {

            for (Part part : candidates.get(0).content.parts) {
                if (part.text != null) {
                    return part.text;
                }
            }
        }
        return "No response text available";
    }
}