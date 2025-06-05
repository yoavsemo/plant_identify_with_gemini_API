package com.example.cameratest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.io.ByteArrayOutputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final String API_KEY = "AIzaSyCykfCHlEvtikWJdCxjWD17o6dXgaXDY74";
    private static final String MODEL_NAME = "gemini-1.5-flash";

    private ImageView imageView;
    private TextView responseText;
    private View placeholderText;
    private LinearProgressIndicator progressIndicator;
    private Bitmap capturedPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        imageView = findViewById(R.id.imageview1);
        responseText = findViewById(R.id.responseText);
        placeholderText = findViewById(R.id.placeholderText);
        progressIndicator = findViewById(R.id.progressIndicator);

        // Set up capture button
        MaterialButton captureButton = findViewById(R.id.captureButton);
        captureButton.setOnClickListener(v -> checkCameraPermissionAndOpenCamera());

        // Set up floating action button
        FloatingActionButton fabCapture = findViewById(R.id.fabCapture);
        fabCapture.setOnClickListener(v -> checkCameraPermissionAndOpenCamera());

        // Request camera permission on first launch
        checkCameraPermissionAndOpenCamera();
    }

    private void checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CODE
            );
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAMERA_REQUEST_CODE);
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            capturedPhoto = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(capturedPhoto);
            placeholderText.setVisibility(View.GONE);
            sendImageToGemini();
        }
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        // Use NO_WRAP to ensure there are no line breaks in the Base64 string
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }

    private void sendImageToGemini() {
        // Show progress indicator and update text
        progressIndicator.setVisibility(View.VISIBLE);
        responseText.setText("Analyzing plant image...");

        if (capturedPhoto == null) {
            responseText.setText("No image captured");
            progressIndicator.setVisibility(View.GONE);
            return;
        }

        // Resize the image to reduce data size
        Bitmap resizedBitmap = resizeBitmap(capturedPhoto, 800);
        String base64Image = bitmapToBase64(resizedBitmap);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://generativelanguage.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GeminiApiService apiService = retrofit.create(GeminiApiService.class);

        // Create a request with both image and text
        GeminiRequest request = GeminiRequest.createImageTextRequest(
                "Identify this plant and provide detailed care information, including: " +
                        "1. Scientific name and common names " +
                        "2. Light requirements " +
                        "3. Watering needs " +
                        "4. Soil preferences " +
                        "5. Common issues and solutions " +
                        "Please format your response in clear sections.",
                base64Image
        );

        apiService.generateContent(MODEL_NAME, request, API_KEY)
                .enqueue(new Callback<GeminiResponse>() {
                    @Override
                    public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                        progressIndicator.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            responseText.setText(response.body().getText());
                        } else {
                            try {
                                String err = response.errorBody().string();
                                responseText.setText("Analysis failed: " + response.code() + "\n" + err);
                            } catch (Exception e) {
                                responseText.setText("Analysis failed: " + response.code());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<GeminiResponse> call, Throwable t) {
                        progressIndicator.setVisibility(View.GONE);
                        responseText.setText("Request failed: " + t.getMessage());
                    }
                });
    }

    // Helper method to resize bitmap
    private Bitmap resizeBitmap(Bitmap bitmap, int maxDimension) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float ratio = (float) width / (float) height;

        int newWidth;
        int newHeight;

        if (width > height) {
            newWidth = maxDimension;
            newHeight = (int) (maxDimension / ratio);
        } else {
            newHeight = maxDimension;
            newWidth = (int) (maxDimension * ratio);
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
}