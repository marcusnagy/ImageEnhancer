package com.softa.imageenhancer;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class TemplateEnhancer implements ImageEnhancer{

    protected int progress;
    protected static final Pixel.PixelFactory pf = Pixel.createPixelFactory();

    /**
     * Method which all classes that extends this abstract class should implement.
     * @param image
     * @param segments
     * @return
     */
    protected abstract Bitmap vTransform(Bitmap image, int segments);

    protected static float[][] convertToHSV(int[] pixels) {
        float[][] hsvPixels = new float[pixels.length][3];
        for (int i = 0; i < pixels.length; i++) {
            Color.RGBToHSV(Color.red(pixels[i]), Color.green(pixels[i]), Color.blue(pixels[i]), hsvPixels[i]);

        }
        return hsvPixels;
    }

    protected static float slope(float y2, float y1, float x2, float x1) {
        return (y2 - y1) / (x2 - x1);
    };

    protected static float mValue(float y, float k, float x){
        return y - (k * x);
    }

    protected static List<Integer> factorize(long number) {

        ArrayList<Integer> factors = new ArrayList<>();

        for(int i = 1; i <= Math.sqrt(number); i++) {
            if (number % i == 0) {
                factors.add(i);
            }
        }
        Collections.sort(factors);

        Log.d("DEBUG", "factorize: " + factors);

        return factors;
    }

    @Override
    public Bitmap enhanceImage(Bitmap bitmap, int configuration) {
        return vTransform(bitmap, configuration);
    }

    @Override
    public int getProgress() {
        return progress;
    }

    @Override
    public String[] getConfigurationOptions() {
        return new String[0];
    }
}
