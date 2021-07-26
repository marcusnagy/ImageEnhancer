package com.softa.imageenhancer;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SmartVEnhancer extends TemplateEnhancer {

    @Override
    protected Bitmap vTransform(Bitmap image, int segments) {


        progress = 0;

        int width = image.getWidth();
        int height = image.getHeight();

        ArrayList<Transform> transforms = new ArrayList<>();
        List<Integer> factors = factorize(width * height);
        int progressPerFactor = 90/factors.size();

        for(int i: factors) {
            Transform transform = new Transform(image, i);
            transforms.add(transform);
            progress += progressPerFactor;
        }

        double bestMAD = 0;
        Transform bestTransform = null;

        Collections.sort(transforms,(t1, t2) -> Double.compare(t1.getMadValue(), t2.getMadValue()));

        for (Transform t: transforms){

            double currentValue = Math.abs(0.25 - t.getMadValue());

            if(bestTransform == null) {
                bestMAD = currentValue;
                bestTransform = t;
            } else if (currentValue < bestMAD) {
                bestMAD = currentValue;
                bestTransform = t;
            }

        }

        Log.d("DEBUG", "bestMAD: " + bestMAD);
        assert bestTransform != null;
        Log.d("DEBUG", "bestTransform number of segments: " + bestTransform.getSegment());


        Transform finalBestTransform = bestTransform;
        new Handler(Looper.getMainLooper()).post(() -> {
            MainActivity.updateSegmentTextSmartEnhance(finalBestTransform.getSegment());;
        });


        progress = 100;


        return bestTransform.getNewImage();
    }


    /**
     * Inner-class Transform represents information about a V-transform for a specific number of
     * segments. This is used for calculating all possible V-transforms for a given image and their
     * MAD-values. This way it is easy to sort and get the right values.
     */
    private static class Transform {

        private double madValue;
        private final int segments;
        private final Bitmap image;
        private Bitmap newImage;

        public Transform(Bitmap image, int segments) {
            this.image = image;
            this.segments = segments;
            calc();
        }

        public double getMadValue() {
            return madValue;
        }

        public Bitmap getNewImage() {
            return newImage;
        }

        public int getSegment() {
            return segments;
        }

        /**
         * Finds the median from a list.
         * @param list
         * @return
         */

        private double getMedian(List<Float> list) {
            Collections.sort(list);

            if (list.size() % 2 == 1) {
                return list.get((list.size() + 1) / 2 - 1);
            } else {
                double lower = list.get(list.size() / 2 - 1);
                double upper = list.get(list.size() / 2);

                return (lower + upper) / 2d;
            }


        }

        /**
         * Calculates the median absolute deviation for pixels.
         * @param pixels
         * @return
         */

        private double calculateMAD(float[][] pixels) {
            int sum = 0;
            int length = 0;
            float mean;

            ArrayList<Float> abs = new ArrayList<>();

            for(int i = 0; i < pixels.length; i++){
                sum += pixels[i][2];
                length++;
            }

            mean =  (float) sum / (float) length;

            for(int i = 0; i < pixels.length; i++){
                abs.add(Math.abs(pixels[i][2] - mean));
            }

            return getMedian(abs);



        }

        /**
         * Calculates the V-transform
         */
        private void calc() {

            int width = image.getWidth();
            int height = image.getHeight();

            int[] pixels = new int[height * width];
            image.getPixels(pixels, 0, width,0,0, width, height);
            float percentageSize = 1f / (float) segments;
            int segmentSize = pixels.length / segments;

            float[][] hsvPixels = convertToHSV(pixels);

            ArrayList<Pixel> newIntensities = new ArrayList<>();

            for (int i = 0; i < hsvPixels.length; i++) {
                newIntensities.add(pf.index(i)
                        .value(hsvPixels[i][2])
                        .build());
            }

            Collections.sort(newIntensities);


            for (int j = 1; j <= segments; j++) {
                float k = slope(percentageSize * j, percentageSize * (j - 1), newIntensities.get((segmentSize * j) - 1).getValue(), newIntensities.get(segmentSize * (j - 1)).getValue());
                float m = mValue(percentageSize * (j - 1), k, newIntensities.get(segmentSize * (j - 1)).getValue());

                for(int l = 0; l < segmentSize; l++) {
                    Pixel currentPixel =  newIntensities.get(l + segmentSize * (j - 1));
                    currentPixel.setValue(k * currentPixel.getValue() + m);
                }
            }


            Collections.sort(newIntensities, (p1, p2) -> Integer.compare(p1.getIndex(), p2.getIndex()));


            for (int pix = 0; pix < hsvPixels.length; pix++) {
                hsvPixels[pix][2] = newIntensities.get(pix).getValue();
                pixels[pix] = Color.HSVToColor(hsvPixels[pix]);
            }

            madValue = calculateMAD(hsvPixels);


            Bitmap modifiedImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            modifiedImage.setPixels(pixels, 0, width, 0, 0, width, height);

            newImage = modifiedImage;


        }
    }
}
