package com.softa.imageenhancer;

import java.util.Arrays;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.util.Log;

public class TestEnhancer implements ImageEnhancer {

    private static final int ACTION_3 = 3;
	private static final int ACTION_2 = 2;
	private static final int ACTION_1 = 1;
	private static final int ACTION_0 = 0;
	private int progress;

	public TestEnhancer() {

	}
	
	public Bitmap enhanceImageHSV(Bitmap theImage, int action) {

		// Set progress
		progress = 0;
		
		// Get the image pixels
		int height = theImage.getHeight();
		int width = theImage.getWidth();
		Log.d("DEBUG", "Image size is " + width + "px by " + height + "px." );
		int[] pixels = new int[height * width];
		theImage.getPixels(pixels, 0, width,0,0, width, height);
		
		progress = 5;

		Log.d("DEBUG", "pixels length = " + pixels.length);
		
		//Convert pixels to brightness values;
		float[][] hsvPixels = convertToHSV(pixels);
		
		progress = 40;
		
		Log.d("DEBUG", "hsvPixels length = " + hsvPixels.length);

		// Here below some manipulations of the image is made as examples.
		// This should be changed to your image enhancement algorithms.
		if (action != ACTION_3) {
            float maxValue = 0;
            for (int i = 0; i < hsvPixels.length; i++) {
                if (maxValue < hsvPixels[i][action]) maxValue = hsvPixels[i][action];
            }
            Log.d("DEBUG", "maxValue of hsvPixels = " + maxValue);
            progress = 60;

            for (int i = 0; i < hsvPixels.length; i++) {
                hsvPixels[i][action] = maxValue - hsvPixels[i][action];
                pixels[i] = Color.HSVToColor(hsvPixels[i]);
            }
        } else {
            for (int i = 0; i < hsvPixels.length; i++) {
                hsvPixels[i][1] = 0; // Set color saturation to zero
                pixels[i] = Color.HSVToColor(hsvPixels[i]);
            }
            Log.d("DEBUG", "saturation zeroed");
        }
		progress = 80;
		Log.d("DEBUG","creating BITMAP,width x height "+width+" "+height);
        Bitmap modifiedImage = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		modifiedImage.setPixels(pixels, 0, width, 0, 0, width, height);

		progress = 100;
		return modifiedImage;
	}

	private float[][] convertToHSV(int[] pixels) {
		float[][] hsvPixels = new float[pixels.length][3];
		for (int i = 0; i < pixels.length; i++) {
			Color.RGBToHSV(Color.red(pixels[i]), Color.green(pixels[i]), Color.blue(pixels[i]), hsvPixels[i]);
			
		}
		return hsvPixels;
	}

	public int getProgress() {
		// Log.d("DEBUG", "Progress: "+progress);
		return progress;
	}

	@Override
	public Bitmap enhanceImage(Bitmap bitmap, int configuration) {
		switch (configuration) {
		case ACTION_0:
			return enhanceImageHSV(bitmap, 0);
		case ACTION_1:
			return enhanceImageHSV(bitmap, 1);
		case ACTION_2:
			return enhanceImageHSV(bitmap, 2);
        case ACTION_3:
            return enhanceImageHSV(bitmap, 3);
		default:
			return enhanceImageHSV(bitmap, 0);
		}
	}

	@Override
	public String[] getConfigurationOptions() {
		return new String[]{ "Action 0", "Action 1", "Action 2", "Action 3"};
	}

}
