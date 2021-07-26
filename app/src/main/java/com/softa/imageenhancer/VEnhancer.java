package com.softa.imageenhancer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VEnhancer extends TemplateEnhancer {


    @Override
    protected Bitmap vTransform(Bitmap image, int segments) {



        progress = 0;

        int width = image.getWidth();
        int height = image.getHeight();

        List<Integer> factors = factorize(width * height);
        int actualFactor = findClosestValue(factors, segments);

        Log.d("DEBUG", "actualFactor: " + actualFactor);

        new Handler(Looper.getMainLooper()).post(() -> {
            MainActivity.updateSegmentTextV(segments, actualFactor);
        });



        Log.d("DEBUG", "Image size is " + width + "px by " + height + "px." );
        int[] pixels = new int[height * width];
        image.getPixels(pixels, 0, width,0,0, width, height);
        float percentageSize = 1f / (float) actualFactor;
        int segmentSize = pixels.length / actualFactor;

        progress = 10;

        Log.d("DEBUG", "pixels length = " + pixels.length);

        //Convert pixels to brightness values;
        float[][] hsvPixels = convertToHSV(pixels);

        progress = 20;

        Log.d("DEBUG", "hsvPixels length = " + hsvPixels.length);

        ArrayList<Pixel> newValues = new ArrayList<>();

        for (int i = 0; i < hsvPixels.length; i++) {
            newValues.add(pf.index(i)
                            .value(hsvPixels[i][2])
                            .build());
        }

        Collections.sort(newValues);

        progress = 45;

        for (int j = 1; j <= actualFactor; j++) {
            float k = slope(percentageSize * j, percentageSize * (j - 1), newValues.get((segmentSize * j) - 1).getValue(), newValues.get(segmentSize * (j - 1)).getValue());
            float m = mValue(percentageSize * (j - 1), k, newValues.get(segmentSize * (j - 1)).getValue());

            for(int l = 0; l < segmentSize; l++) {
                Pixel currentPixel =  newValues.get(l + segmentSize * (j - 1));
                currentPixel.setValue(k * currentPixel.getValue() + m);
            }
        }

        Log.d("DEBUG", "newValue: calculation complete");

        progress = 55;

        Collections.sort(newValues, (p1,p2) -> Integer.compare(p1.getIndex(), p2.getIndex()));


        for (int pix = 0; pix < hsvPixels.length; pix++) {
            hsvPixels[pix][2] = newValues.get(pix).getValue();
            pixels[pix] = Color.HSVToColor(hsvPixels[pix]);
        }

        Log.d("DEBUG", "pixels: new values inserted to pixels");

        progress = 75;
        Log.d("DEBUG","creating BITMAP,width x height "+width+" "+height);
        Bitmap modifiedImage = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        modifiedImage.setPixels(pixels, 0, width, 0, 0, width, height);

        progress = 100;
        return modifiedImage;


    }

    /**
     * Finds the closest value to a given value. Given all factors of possible segmentations in the
     * V-transform where all segments are of equal size, this method finds the closest value from
     * accepted factors from the users desired input of "number of segments".
     * @param sorted
     * @param target
     * @return
     */

    private int findClosestValue(List<Integer> sorted, int target) {
        if ( target <= sorted.get(0)) {
            return sorted.get(0);
        }

        if (target >= sorted.get(sorted.size() - 1)) {
            return sorted.get(sorted.size() - 1);
        }

        int start = 0;
        int end = sorted.size() - 1;
        int mid = 0;

        while(end - start != 1) {

            mid = (start + end) / 2;

            if (target == sorted.get(mid)) {
                return sorted.get(mid);
            }

            if (target < sorted.get(mid)) {
                end = mid;
            }

            if (target > sorted.get(mid)) {
                start = mid;
            }
        }

        return Math.abs(target -  sorted.get(start)) <= Math.abs(target - sorted.get(end)) ? sorted.get(start): sorted.get(end);
    }




}
