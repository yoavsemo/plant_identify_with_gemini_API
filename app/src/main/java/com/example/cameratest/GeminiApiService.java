// GeminiApiService.java
package com.example.cameratest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    Call<GeminiResponse> generateContent(
            @Path("model") String model,
            @Body GeminiRequest request,
            @Query("key") String apiKey
    );
}