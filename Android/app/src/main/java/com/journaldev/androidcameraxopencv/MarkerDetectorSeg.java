package com.journaldev.androidcameraxopencv;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.core.Mat;
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
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import static org.opencv.core.CvType.CV_32F;

public class MarkerDetectorSeg {
    static Interpreter  tflite;

    public MarkerDetectorSeg(AssetFileDescriptor fileDescriptor){
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
        TensorBuffer outputs = TensorBuffer.createFixedSize(new int[] {224, 224, 3}, DataType.FLOAT32);
        tflite.run(image.getBuffer(), outputs.getBuffer());
        ByteBuffer bb = outputs.getBuffer();
        bb.rewind();
        Bitmap bmap = Bitmap.createBitmap(224, 224, Bitmap.Config.ARGB_4444);;
        for (int i=0;i<224;i++){
            for (int j=0;j<224;j++){
                int r = (int)getColor(bb.getFloat());
                int g = (int)getColor(bb.getFloat());
                int b = (int)getColor(bb.getFloat());
                bmap.setPixel(j,i,Color.rgb(r,g,b));
            }
        }
        bmap =  Bitmap.createScaledBitmap(bmap, inputBitmap.getWidth(), inputBitmap.getHeight(), false);
        for (int i=0;i<inputBitmap.getWidth();i++){
            for (int j=0;j<inputBitmap.getHeight();j++){
                int color = bmap.getPixel(i,j);
                if (Color.red(color) > 180 || Color.blue(color) > 40 || Color.green(color) > 40)
                    inputBitmap.setPixel(i,j,Color.rgb(Color.red(color), Color.green(color), Color.blue(color)));
            }
        }
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
}
