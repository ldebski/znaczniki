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

                        //detecting
                        if (!mat.empty()) {
                            long startTime = System.currentTimeMillis();
                            detectMarkers(mat, dictionary, corners, ids, parameters);
                            long estimatedTime = System.currentTimeMillis() - startTime;
                            //Log.e("time milliseconds", estimatedTime + "");
                            if (!ids.empty()) {


                                // rysuje koło jak wykryje 2 przekątne
                                MarkersUtils marker = new MarkersUtils(corners, ids);
                                if (marker.validate()){
                                    Point circleMiddle = marker.getCircleMiddle();
                                    double radius = marker.getRadius();
                                    Imgproc.circle(displayCopy, circleMiddle, (int)radius, new Scalar(255,0,0), 15);
                                }

                                // printuje losowy szerokość i wysokość losowego markera
                                if (corners.size() > 0){
                                    Marker marker2 = new Marker(corners.get(0), (int)ids.get(0,0)[0]);
                                    Log.e("cos", "Width: " + Double.toString(marker2.getWidth()) + "    Height: " + Double.toString(marker2.getHeight()));
                                }

                                    // próba kalibracji kamery trochę nieudana
                                     //Aruco.drawDetectedMarkers(mat, corners, ids);
                                     Mat cameraMatrix = new Mat(3, 3, CvType.CV_32F);
                                     int row = 0, col = 0;
                                     double[] data = {1624.2491,0.,959.3577,0.,1614.9941,480.5490,0.,0.,1.};
                                     cameraMatrix.put(row,col,data);
//
                                     Mat distort = new Mat(5,1,CvType.CV_32F);
                                     row = 0;
                                     col = 0;
                                     double[] distort_data = {0.0353, 0.8180, 0.00, 0.00,-4.6780};
                                     distort.put(row,col,distort_data);
                                     Mat rvecs = new Mat(), tvecs = new Mat();
                                     Aruco.estimatePoseSingleMarkers(corners, 0.03f, cameraMatrix, distort, rvecs, tvecs);
                                     if(ids.height()>1){
                                            for (int i=0;i<rvecs.height();i++) {
                                                double[] rvec = rvecs.get(i, 0);
                                                double[] tvec = tvecs.get(i, 0);
                                                Mat rvec_mat = new Mat(3, 1, CvType.CV_32F);
                                                row = 0;
                                                col = 0;
                                                rvec_mat.put(row, col, rvec);
                                                Mat tvec_mat = new Mat(3, 1, CvType.CV_32F);
                                                row = 0;
                                                col = 0;
                                                tvec_mat.put(row, col, tvec);
                                                drawFrameAxes(displayCopy, cameraMatrix, distort, rvec_mat, tvec_mat, 0.1f);
                                            }
                                     }
                                     else if(ids.height()==1) {
                                         drawFrameAxes(displayCopy, cameraMatrix, distort, rvecs, tvecs, 0.04f, 10);
                                     }
                                    // for (int i=0;i<rvecs.size().height;i++){
                                    //     double[] rvec = rvecs.get(i, 0);
                                    //     double[] tvec = tvecs.get(i,0);
                                    //     Mat rvec_mat = new Mat(3,1, CvType.CV_32F);
                                    //     row = 0;col = 0;
                                    //     rvec_mat.put(row,col,rvec);
                                    //     Mat tvec_mat = new Mat(3,1, CvType.CV_32F);
                                    //     row = 0;col = 0;
                                    //     tvec_mat.put(row,col,tvec);
                                    //     drawFrameAxes(displayCopy, cameraMatrix, distort, rvec_mat, tvec_mat, 0.1f);
                                    // }
                                    // if(corners.size() >= 1){
                                    //     Log.e("corners size: ", Double.toString(corners.size()));
                                    //     for(int i=0;i<corners.size();i++){
                                    //         Log.e("xzcczx", Double.toString(ids.get(i,0)[0]) + ": " + corners.get(i).dump());
                                    //         int xd = (int)corners.get(0).get(0,2)
                                    //         // Log.e("xzcczx", corners.get(i).dump());
                                    //     }
                                    // }

                                // printuje linie pomiędzy 4 cornerami jak je znajdzie
                                // if(corners.size() == 4) {
                                //     int x1 = (int)corners.get(0).get(0,2)[0];
                                //     int y1 = (int)corners.get(0).get(0,2)[1];
//
                                //     int x2 = (int)corners.get(1).get(0,2)[0];
                                //     int y2 = (int)corners.get(1).get(0,2)[1];
//
                                //     int x3 = (int)corners.get(2).get(0,2)[0];
                                //     int y3 = (int)corners.get(2).get(0,2)[1];
//
                                //     int x4 = (int)corners.get(3).get(0,2)[0];
                                //     int y4 = (int)corners.get(3).get(0,2)[1];
//
                                //     Point point1 = new Point(x1, y1);
                                //     Point point2 = new Point(x2, y2);
                                //     Point point3 = new Point(x3, y3);
                                //     Point point4 = new Point(x4, y4);
                                //     Imgproc.line(displayCopy, point1, point2, new Scalar(64, 64, 64), 10);
                                //     Imgproc.line(displayCopy, point2, point3, new Scalar(64, 64, 64), 10);
                                //     Imgproc.line(displayCopy, point3, point4, new Scalar(64, 64, 64), 10);
                                //     Imgproc.line(displayCopy, point4, point1, new Scalar(64, 64, 64), 10);
                                //     Log.e("time milliseconds", "narysowano");
                                // }
                            }

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
