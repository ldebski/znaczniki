package com.journaldev.androidcameraxopencv;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import static org.opencv.core.CvType.CV_32F;

public class MarkerDetectorPoints {
    static Interpreter  tflite;

    public MarkerDetectorPoints(AssetFileDescriptor fileDescriptor){
        try {
            tflite = new Interpreter((ByteBuffer)loadModelFile(fileDescriptor));
            Log.e("", "Initialized");
        } catch (Exception ex) {
            ex.printStackTrace();;
            Log.e("", "Failed to initialized network");
        }
    }

    public Bitmap runNetwork(Bitmap inputBitmap){
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

    public static float getColor(float i){
        return Math.max(Math.min(i*255, 255), 0);
    }

    private MappedByteBuffer loadModelFile(AssetFileDescriptor fileDescriptor) throws IOException {
        // AssetFileDescriptor fileDescriptor = this.getAssets().openFd("model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public void drawPoints(float[] points, Bitmap bitmap){
        Mat displayCopy = new Mat();
        Utils.bitmapToMat(bitmap, displayCopy);
        for(int i=0;i<points.length;i+=2)
            Imgproc.circle(displayCopy, new Point(points[i]*bitmap.getWidth(), points[i+1]*bitmap.getHeight()), 5, new Scalar(255,0,0), 20);

        Utils.matToBitmap(displayCopy, bitmap);
    }
}
