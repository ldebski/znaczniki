package com.znaczniki.app;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MarkerDetectorSeg {
    static Interpreter tflite;

    public MarkerDetectorSeg(AssetFileDescriptor fileDescriptor) {
        Interpreter.Options options = new Interpreter.Options();
        CompatibilityList compatList = new CompatibilityList();
//
        if (compatList.isDelegateSupportedOnThisDevice()) {
            // if the device has a supported GPU, add the GPU delegate
            GpuDelegate.Options delegateOptions = compatList.getBestOptionsForThisDevice();
            GpuDelegate gpuDelegate = new GpuDelegate(delegateOptions);
            options.addDelegate(gpuDelegate);
        } else {
            // if the GPU is not supported, run on 4 threads
            options.setNumThreads(4);
        }

        try {
            tflite = new Interpreter(loadModelFile(fileDescriptor), options);
            Log.e("", "Initialized");
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("", "Failed to initialized network");
        }
    }

    public static float getColor(float i) {
        return Math.max(Math.min(i * 255, 255), 0);
    }

    public Bitmap runNetwork(Bitmap inputBitmap) {
        TensorBuffer outputs = TensorBuffer.createFixedSize(new int[]{224, 224, 3}, DataType.FLOAT32);
        tflite.run(inputPreprocessing(inputBitmap), outputs.getBuffer());
        Bitmap bmap = outputToBitmap(outputs.getBuffer());
        bmap =  Bitmap.createScaledBitmap(bmap, inputBitmap.getWidth(), inputBitmap.getHeight(), false);
        return drawSegmentation(bmap, inputBitmap);
    }

    private ByteBuffer inputPreprocessing(Bitmap inputBitmap){
        Bitmap resized = Bitmap.createScaledBitmap(inputBitmap, 224, 224, false);
        TensorImage image = new TensorImage(DataType.FLOAT32);
        ImageProcessor imageProcessor = new ImageProcessor.Builder().add(new NormalizeOp(0, 255)).build();
        image.load(resized);
        image = imageProcessor.process(image);
        return image.getBuffer();
    }

    private Bitmap outputToBitmap(ByteBuffer outputsBuffer){
        outputsBuffer.rewind();
        Bitmap bmap = Bitmap.createBitmap(224, 224, Bitmap.Config.ARGB_4444);
        for (int i = 0; i < 224; i++) {
            for (int j = 0; j < 224; j++) {
                int r = (int) getColor(outputsBuffer.getFloat());
                int g = (int) getColor(outputsBuffer.getFloat());
                int b = (int) getColor(outputsBuffer.getFloat());
                bmap.setPixel(j, i, Color.rgb(r, g, b));
            }
        }
        return bmap;
    }

    private Bitmap drawSegmentation(Bitmap bmap, Bitmap inputBitmap){
        for (int i = 0; i < inputBitmap.getWidth(); i++) {
            for (int j = 0; j < inputBitmap.getHeight(); j++) {
                int color = bmap.getPixel(i, j);
                if (Color.red(color) > 180 || Color.blue(color) > 40 || Color.green(color) > 40)
                    inputBitmap.setPixel(i, j, Color.rgb(Color.red(color), Color.green(color), Color.blue(color)));
            }
        }
        return inputBitmap;
    }

    private MappedByteBuffer loadModelFile(AssetFileDescriptor fileDescriptor) throws IOException {
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
}
