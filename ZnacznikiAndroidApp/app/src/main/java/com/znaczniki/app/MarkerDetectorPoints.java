package com.znaczniki.app;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MarkerDetectorPoints {
    static Interpreter tflite;

    public MarkerDetectorPoints(AssetFileDescriptor fileDescriptor) {
        try {
            tflite = new Interpreter((ByteBuffer) loadModelFile(fileDescriptor));
            Log.e("", "Initialized");
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("", "Failed to initialize network");
        }
    }

    public Bitmap runNetwork(Bitmap inputBitmap) {
        Bitmap resized = Bitmap.createScaledBitmap(inputBitmap, 224, 224, false);
        TensorImage image = new TensorImage(DataType.FLOAT32);
        ImageProcessor imageProcessor = new ImageProcessor.Builder().add(new NormalizeOp(0, 255)).build();
        image.load(resized);
        image = imageProcessor.process(image);
        float[][] outputs = new float[1][16];
        tflite.run(image.getBuffer(), outputs);
        drawPoints(outputs[0], inputBitmap);
        return inputBitmap;
    }

    private MappedByteBuffer loadModelFile(AssetFileDescriptor fileDescriptor) throws IOException {
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public void drawPoints(float[] points, Bitmap bitmap) {
        Mat displayCopy = new Mat();
        Utils.bitmapToMat(bitmap, displayCopy);
        for (int i = 0; i < points.length; i += 2)
            Imgproc.circle(displayCopy, new Point(points[i] * bitmap.getWidth(), points[i + 1] * bitmap.getHeight()), 5, new Scalar(255, 0, 0), 20);

        Utils.matToBitmap(displayCopy, bitmap);
    }
}
