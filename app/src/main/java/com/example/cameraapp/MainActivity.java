package com.example.cameraapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {
    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    Button captureImage,saveImage,showImage;
    ImageView capturedImage,cameraIcon;
    Bitmap photo;
    DatabaseHelper databaseHelper;
    TextureView textureView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        captureImage = findViewById(R.id.captureImage);
        saveImage = findViewById(R.id.saveImage);
        capturedImage = findViewById(R.id.capturedImage);
        showImage = findViewById(R.id.showImage);
        textureView = findViewById(R.id.textureView);
        cameraIcon = findViewById(R.id.imgCapture);
        databaseHelper = new DatabaseHelper(this);
        setVisiblity();
        captureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(allPermissionsGranted()) {
                    textureView.setVisibility(View.VISIBLE);
                    cameraIcon.setVisibility(View.VISIBLE);
                    showImage.setVisibility(View.GONE);
                    captureImage.setVisibility(View.GONE);
                    startCamera();
                }else{
                    ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
                }
            }
        });
        saveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG, 0, stream);
                databaseHelper.insertImage(stream.toByteArray());
                setVisiblity();
            }
        });
        showImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,ShowImages.class));
            }
        });
    }
    public void setVisiblity(){
        textureView.setVisibility(View.GONE);
        cameraIcon.setVisibility(View.GONE);
        captureImage.setVisibility(View.VISIBLE);
        showImage.setVisibility(View.VISIBLE);
        capturedImage.setVisibility(View.GONE);
        saveImage.setVisibility(View.GONE);

    }
    private void startCamera() {

        CameraX.unbindAll();

        Rational aspectRatio = new Rational (textureView.getWidth(), textureView.getHeight());
        Size screen = new Size(textureView.getWidth(), textureView.getHeight());


        PreviewConfig pConfig = new PreviewConfig.Builder().setTargetAspectRatio(aspectRatio).setTargetResolution(screen).build();
        Preview preview = new Preview(pConfig);

        preview.setOnPreviewOutputUpdateListener(
                new Preview.OnPreviewOutputUpdateListener() {
                    @Override
                    public void onUpdated(Preview.PreviewOutput output){
                        ViewGroup parent = (ViewGroup) textureView.getParent();
                        parent.removeView(textureView);
                        parent.addView(textureView, 0);

                        textureView.setSurfaceTexture(output.getSurfaceTexture());
                        updateTransform();
                    }
                });


        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder().setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()).build();
        final ImageCapture imgCap = new ImageCapture(imageCaptureConfig);

        cameraIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgCap.takePicture(new ImageCapture.OnImageCapturedListener() {
                    @Override
                    public void onCaptureSuccess(ImageProxy image, int rotationDegrees) {
                        photo = textureView.getBitmap();
                        capturedImage.setImageBitmap(imageProxyToBitmap(image));
                        capturedImage.setRotation(rotationDegrees);
                        capturedImage.setVisibility(View.VISIBLE);
                        saveImage.setVisibility(View.VISIBLE);
                        textureView.setVisibility(View.GONE);
                        cameraIcon.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(ImageCapture.UseCaseError useCaseError, String message, @Nullable Throwable cause) {
                        super.onError(useCaseError, message, cause);
                    }
                });
            }
        });
        CameraX.bindToLifecycle((LifecycleOwner)this, preview, imgCap);
    }

    private void updateTransform(){
        Matrix mx = new Matrix();
        float w = textureView.getMeasuredWidth();
        float h = textureView.getMeasuredHeight();

        float cX = w / 2f;
        float cY = h / 2f;

        int rotationDgr;
        int rotation = (int)textureView.getRotation();

        switch(rotation){
            case Surface.ROTATION_0:
                rotationDgr = 0;
                break;
            case Surface.ROTATION_90:
                rotationDgr = 90;
                break;
            case Surface.ROTATION_180:
                rotationDgr = 180;
                break;
            case Surface.ROTATION_270:
                rotationDgr = 270;
                break;
            default:
                return;
        }

        mx.postRotate((float)rotationDgr, cX, cY);
        textureView.setTransform(mx);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean allPermissionsGranted() {

        for (String permission: REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    private Bitmap imageProxyToBitmap(ImageProxy image) {
        ImageProxy.PlaneProxy planeProxy = image.getPlanes()[0];
        ByteBuffer buffer = planeProxy.getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
