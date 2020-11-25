package com.journaldev.androidcameraxopencv;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Rational;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.aruco.Board;
import org.opencv.aruco.CharucoBoard;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.aruco.Aruco;
import org.opencv.aruco.DetectorParameters;
import org.opencv.aruco.Dictionary;
import org.opencv.core.Mat;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.opencv.aruco.Aruco.DICT_4X4_250;
import static org.opencv.aruco.Aruco.detectMarkers;
import static org.opencv.aruco.Aruco.estimatePoseBoard;
import static org.opencv.aruco.Aruco.getPredefinedDictionary;
import static org.opencv.calib3d.Calib3d.drawFrameAxes;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.cvtColor;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    TextureView textureView;
    ImageView ivBitmap;
    LinearLayout llBottom;

    int currentImageType = Imgproc.COLOR_RGB2GRAY;

    ImageCapture imageCapture;
    ImageAnalysis imageAnalysis;
    Preview preview;

    FloatingActionButton btnCapture, btnOk, btnCancel;

    private final List<AngleHelper.Information> lastMessages = new ArrayList<>();

    static {
        if (!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load OpenCV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnCapture = findViewById(R.id.btnCapture);
        btnOk = findViewById(R.id.btnAccept);
        btnCancel = findViewById(R.id.btnReject);

        btnOk.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        llBottom = findViewById(R.id.llBottom);
        textureView = findViewById(R.id.textureView);
        ivBitmap = findViewById(R.id.ivBitmap);

        CharucoBoard board = CharucoBoard.create(5, 7, 0.04f, 0.02f, getPredefinedDictionary(DICT_4X4_250));
        Mat boardImage = new Mat();
        board.draw(new org.opencv.core.Size(600, 500), boardImage, 10, 1);
        imwrite("board.jpg", boardImage);
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void startCamera() {

        CameraX.unbindAll();
        preview = setPreview();
        imageCapture = setImageCapture();
        imageAnalysis = setImageAnalysis();

        //bind to lifecycle:
        CameraX.bindToLifecycle(this, preview, imageCapture, imageAnalysis);
    }


    private Preview setPreview() {

        Rational aspectRatio = new Rational(textureView.getWidth(), textureView.getHeight());
        android.util.Size screen = new android.util.Size(textureView.getWidth(), textureView.getHeight()); //size of the screen


        PreviewConfig pConfig = new PreviewConfig.Builder().setTargetAspectRatio(aspectRatio).setTargetResolution(screen).build();
        Preview preview = new Preview(pConfig);

        preview.setOnPreviewOutputUpdateListener(
                new Preview.OnPreviewOutputUpdateListener() {
                    @Override
                    public void onUpdated(Preview.PreviewOutput output) {
                        ViewGroup parent = (ViewGroup) textureView.getParent();
                        parent.removeView(textureView);
                        parent.addView(textureView, 0);

                        textureView.setSurfaceTexture(output.getSurfaceTexture());
                        updateTransform();
                    }
                });

        return preview;
    }


    private ImageCapture setImageCapture() {
        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder().setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()).build();
        final ImageCapture imgCapture = new ImageCapture(imageCaptureConfig);


        btnCapture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                imgCapture.takePicture(new ImageCapture.OnImageCapturedListener() {
                    @Override
                    public void onCaptureSuccess(ImageProxy image, int rotationDegrees) {
                        Bitmap bitmap = textureView.getBitmap();
                        showAcceptedRejectedButton(true);
                        ivBitmap.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onError(ImageCapture.UseCaseError useCaseError, String message, @Nullable Throwable cause) {
                        super.onError(useCaseError, message, cause);
                    }
                });
            }
        });

        return imgCapture;
    }


    private ImageAnalysis setImageAnalysis() {

        // Setup image analysis pipeline that computes average pixel luminance
        HandlerThread analyzerThread = new HandlerThread("OpenCVAnalysis");
        analyzerThread.start();


        ImageAnalysisConfig imageAnalysisConfig = new ImageAnalysisConfig.Builder()
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .setCallbackHandler(new Handler(analyzerThread.getLooper()))
                .setImageQueueDepth(1).build();

        ImageAnalysis imageAnalysis = new ImageAnalysis(imageAnalysisConfig);

        imageAnalysis.setAnalyzer(
                new ImageAnalysis.Analyzer() {
                    @Override
                    public void analyze(ImageProxy image, int rotationDegrees) {
                        //Analyzing live camera feed begins.

                        final Bitmap bitmap = textureView.getBitmap();


                        if (bitmap == null)
                            return;

                        Mat mat = new Mat();
                        Mat displayCopy = new Mat();
                        Utils.bitmapToMat(bitmap, mat);
                        Utils.bitmapToMat(bitmap, displayCopy);
                        cvtColor(mat, mat, Imgproc.COLOR_BGRA2GRAY);

                        Dictionary dictionary = getPredefinedDictionary(DICT_4X4_250);
                        List<Mat> corners = new LinkedList<>();
                        Mat ids = new Mat();
                        DetectorParameters parameters = DetectorParameters.create();
                        corners.clear();

                        AngleHelper.Information information = null;
                        //detecting
                        if (!mat.empty()) {
                            long startTime = System.currentTimeMillis();
                            detectMarkers(mat, dictionary, corners, ids, parameters);
                            long estimatedTime = System.currentTimeMillis() - startTime;
                            //Log.e("time milliseconds", estimatedTime + "");
                            if (!ids.empty()) {

                                // klasa przechowująca wszystkie markery
                                Markers markers = new Markers(corners, ids);

                                // klasa obsługująca wyświetlany tekst pomocny np. "lewo", "prawo"
                                AngleHelper angleHelper = new AngleHelper(markers, lastMessages);
                                // wypisuje na środku zdjęcia "PERFECT" jak jest dobre ujęcie
                                information = angleHelper.GetAngleHelper(true);

                                // klasa pomocna do printowania rozmiarow itp
                                DebugUtils utils = new DebugUtils(markers);

                                // wypisuje informacje na znaczniku
                                // utils.DrawMarkersInfo(displayCopy, true, false, false, false);

                                // wypisuje informacje na konsoli
                                // utils.PrintMarkersInfo(false, false, false, true);

                                // rysuje kółko jak znajdzie przekątną
                                // utils.DrawMiddleCircle(displayCopy);
                            }
                        }
                        if (lastMessages.size() > 0) {
                            if (information == null)
                                information = lastMessages.get(lastMessages.size() - 1);
                            AngleHelper.DrawInformation(displayCopy, information);
                        }
                        Utils.matToBitmap(displayCopy, bitmap);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ivBitmap.setImageBitmap(bitmap);
                            }
                        });

                    }
                });


        return imageAnalysis;

    }

    private void showAcceptedRejectedButton(boolean acceptedRejected) {
        if (acceptedRejected) {
            CameraX.unbind(preview, imageAnalysis);
            llBottom.setVisibility(View.VISIBLE);
            btnCapture.hide();
            textureView.setVisibility(View.GONE);
        } else {
            btnCapture.show();
            llBottom.setVisibility(View.GONE);
            textureView.setVisibility(View.VISIBLE);
            textureView.post(new Runnable() {
                @Override
                public void run() {
                    startCamera();
                }
            });
        }
    }


    private void updateTransform() {
        Matrix mx = new Matrix();
        float w = textureView.getMeasuredWidth();
        float h = textureView.getMeasuredHeight();

        float cX = w / 2f;
        float cY = h / 2f;

        int rotationDgr;
        int rotation = (int) textureView.getRotation();

        switch (rotation) {
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

        mx.postRotate((float) rotationDgr, cX, cY);
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

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.black_white:
                currentImageType = Imgproc.COLOR_RGB2GRAY;
                startCamera();
                return true;

            case R.id.hsv:
                currentImageType = Imgproc.COLOR_RGB2HSV;
                startCamera();
                return true;

            case R.id.lab:
                currentImageType = Imgproc.COLOR_RGB2Lab;
                startCamera();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnReject:
                showAcceptedRejectedButton(false);
                break;

            case R.id.btnAccept:
                File file = new File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "" + System.currentTimeMillis() + "_JDCameraX.jpg");
                imageCapture.takePicture(file, new ImageCapture.OnImageSavedListener() {
                    @Override
                    public void onImageSaved(@NonNull File file) {
                        showAcceptedRejectedButton(false);

                        Toast.makeText(getApplicationContext(), "Image saved successfully in Pictures Folder", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {

                    }
                });
                break;
        }
    }
}
