package com.example.cameratest;

import com.google.gson.annotations.SerializedName;

public class Part {
    @SerializedName("text")
    public String text;

    @SerializedName("inlineData")
    public Blob inlineData;

    public Part(String text) {
        this.text = text;
        this.inlineData = null;
    }

    public Part(Blob inlineData) {
        this.text = null;
        this.inlineData = inlineData;
    }

    public static class Blob {
        @SerializedName("mimeType")
        public String mimeType;

        @SerializedName("data")
        public String data;

        public Blob(String mimeType, String data) {
            this.mimeType = mimeType;
            this.data = data;
        }
    }
}