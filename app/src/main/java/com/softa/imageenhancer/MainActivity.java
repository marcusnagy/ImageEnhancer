package com.softa.imageenhancer;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.provider.MediaStore;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {

	private static final String TAG = "MainActivity";

	final static int SELECT_IMAGE = 10;
	private ImageView beforeImageView;
	private static ImageView afterImageView;
	private static Bitmap theImage;
	private Button loadButton;
	private Button improveButton;
	private Button saveButton; // NEW: Save button calling saveImage(Bitmap, String) method
	private Button infoButton; // NEW: Give info about segmentation
	private RadioGroup radioGroup; //NEW: Radio buttons to allow user to choose enhancer.
	private RadioButton radioButton; //NEW: Radio button selected
	private static ImageEnhancer selectedEnhancer;
	private static int selectedConfiguration;
	private static ProgressDialog progressDialog;
	private static String imageName; // NEW: Image name for the image being saved.
	private static int bestV, computed, chosen;
	private boolean smart, transform = false;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		selectedEnhancer = getEnhancers().get(0); // Here we choose which enhancer to use

		//// print how much memory the app is allowed to use on this device
		Runtime rt = Runtime.getRuntime();
		long maxMemory = rt.maxMemory();
		Log.d("onCreate", "maxMemory:" + Long.toString(maxMemory));
		//////////////////////////////////

		loadButton = (Button) findViewById(R.id.load_button);
		improveButton = (Button) findViewById(R.id.improve_button);
		saveButton = (Button) findViewById(R.id.save_image); //NEW: get the save button
		infoButton = (Button) findViewById(R.id.info_button); //NEW: get the info button
		radioGroup = (RadioGroup) findViewById(R.id.enhanceMethods); //NEW: get the radio buttons
		improveButton.setVisibility(View.INVISIBLE);
		infoButton.setVisibility(View.INVISIBLE); //NEW: Change visibility for info button.
		saveButton.setVisibility(View.INVISIBLE); //NEW: Change visibility for save button.
		radioGroup.setVisibility(View.INVISIBLE); //NEW: Change visibility for radio buttons.

		beforeImageView = (ImageView) findViewById(R.id.imageview1);
		afterImageView = (ImageView) findViewById(R.id.imageview2);
		
		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(false);
		progressDialog.setTitle("Processing image");
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setProgressNumberFormat(null);

		loadButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("image/*");
				startActivityForResult(Intent.createChooser(intent, "Zelect image:"), SELECT_IMAGE);

			}
		});

		infoButton.setOnClickListener(view -> {


			if (smart && transform) {
				Toast.makeText(MainActivity.this,"SEGMENTATION\n" + "Best: " + bestV, Toast.LENGTH_SHORT).show();
			} else if (transform) {
				Toast.makeText(MainActivity.this,"SEGMENTATION\n" + "Chosen: " + chosen + "\n" + "Actual: " + computed, Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(MainActivity.this,"Nothing to display", Toast.LENGTH_SHORT).show();
			}
		});

		//NEW: onClick event for save button. Opens the saveDialog() which will handle the saving
		//procedure. If the current device does not support the Save function then a message will
		//be displayed in the form of a Toast.
		saveButton.setOnClickListener(view -> {

			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

				saveDialog();


			} else {
				Toast.makeText(MainActivity.this, Html.fromHtml("<font color='#da0037'>API on Device is <b>" + Build.VERSION.SDK_INT + "</b>, Save requires API <b>" + Build.VERSION_CODES.Q + "</b> or above.</font>") , Toast.LENGTH_LONG).show();
			}

		});

		beforeImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(loadButton.getVisibility() == View.VISIBLE)
				    loadButton.setVisibility(View.INVISIBLE);
				else
					loadButton.setVisibility(View.VISIBLE);

			}
		});

		improveButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				FragmentManager fm = getSupportFragmentManager();

				int selectedID = radioGroup.getCheckedRadioButtonId();
				radioButton = (RadioButton) findViewById(selectedID);

				String current = radioButton.getText().toString();
				checkedEnhancer(fm, current);
			}
		});

		afterImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(improveButton.getVisibility() == View.VISIBLE) {
					improveButton.setVisibility(View.INVISIBLE);
					saveButton.setVisibility(View.INVISIBLE);
					radioGroup.setVisibility(View.INVISIBLE);
					infoButton.setVisibility(View.INVISIBLE);
				} else {
					improveButton.setVisibility(View.VISIBLE);
					saveButton.setVisibility(View.VISIBLE);
					radioGroup.setVisibility(View.VISIBLE);
					infoButton.setVisibility(View.VISIBLE);
				}

			}
		});
	}

	/**
	 * Check the version of the SDK used on the device to use one method of saving the image and
	 * if not the required version or above it will save in a different way if it can.
	 * Save the images as a JPEG and stores it in the phone pictures directory.
	 * @param bitmap
	 * @param name
	 * @throws IOException
	 */

	private void saveImage(Bitmap bitmap, String name) throws IOException {

		OutputStream out;

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			ContentResolver resolver = getContentResolver();
			ContentValues contentValues = new ContentValues();
			contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, name + ".jpg");
			contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
			contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
			Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
			out = resolver.openOutputStream(Objects.requireNonNull(imageUri));
		} else {
			String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
			File image = new File(imagesDir, name + ".jpg");
			out = new FileOutputStream(image);
		}

		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
		out.close();
	}


	/**
	 * Method used to determine what Radio button is selected and from that display the
	 * corresponding alert dialog and execute the image enhancing. Method also tells us how the Info
	 * button will respond.
	 * @param fm
	 * @param name
	 */
	private void checkedEnhancer(FragmentManager fm, String name){
		switch (name) {
			case "Test Enhancer":
				selectedEnhancer = getEnhancers().get(0);
				transform = false;
				smart = false;
				new ConfigurationDialog().show(fm, "configuration_dialog");
				break;
			case "V-Transform":
				selectedEnhancer = getEnhancers().get(1);
				transform = true;
				smart = false;
				new VConfigurationDialog().show(fm, "vconfiguration_dialog");
				break;
			case "Smart Enhance":
				selectedEnhancer = getEnhancers().get(2);
				progressDialog.setProgress(0);
				progressDialog.show();
				transform = true;
				smart = true;
				new ImproveImageTask().execute(theImage);
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK && null != data) {
			Uri selectedImage = data.getData();
			
			ParcelFileDescriptor parcelFileDescriptor;
			try {
				parcelFileDescriptor = getContentResolver().openFileDescriptor(
						selectedImage, "r");
				FileDescriptor fileDescriptor = parcelFileDescriptor
						.getFileDescriptor();
				theImage = BitmapFactory.decodeFileDescriptor(fileDescriptor);
				
				// get screen width and scale image to fit 
				int activityWidth = getWindow().getDecorView().getWidth();
				int activityHeight = getWindow().getDecorView().getHeight() - getStatusBarHeight();
				int width;
				int height;
				if (theImage.getWidth() > theImage.getHeight()) { // Landscape
					width = activityWidth;
					height = theImage.getHeight() * width / theImage.getWidth(); //Keep aspect ratio
				} else {
					height = activityHeight / 2;
					width = theImage.getWidth() * height / theImage.getHeight();
				}
				Log.d("DEBUG","creating scaled BITMAP,width x height "+width+" "+height);
				theImage = Bitmap.createScaledBitmap(theImage, width,
						height, false);
				parcelFileDescriptor.close();
				beforeImageView.setImageBitmap(theImage);
				saveButton.setVisibility(View.VISIBLE);
				radioGroup.setVisibility(View.VISIBLE);
				improveButton.setVisibility(View.VISIBLE);
				infoButton.setVisibility(View.VISIBLE);
				loadButton.setVisibility(View.INVISIBLE);  //Hide the loadButton to not obscure the original pic.
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	private int getStatusBarHeight() {
	    int result = 0;
	    int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
	    if (resourceId > 0) {
	        result = getResources().getDimensionPixelSize(resourceId);
	    }
	    return result;
	}
	
	private static class ImproveImageTask extends AsyncTask<Bitmap, Integer, Bitmap> {

		protected Bitmap doInBackground(Bitmap... urls) {
			
			new Thread(
					  new Runnable() {

					      public void run() {
					         while (progressDialog.isShowing()) {
					        	 publishProgress();
					        	 try {
									Thread.sleep(200);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
					         }
					      }
					}).start();

			return selectedEnhancer.enhanceImage(theImage, selectedConfiguration);

		}

		protected void onProgressUpdate(Integer... progress) {
			
			progressDialog.setProgress(selectedEnhancer.getProgress());

		}

		protected void onPostExecute(Bitmap result) {
			afterImageView.setImageBitmap(result);
			progressDialog.dismiss();

		}
	}


	
	private List<ImageEnhancer> getEnhancers() {
		ArrayList<ImageEnhancer> enhancers = new ArrayList<ImageEnhancer>();
		
		enhancers.add(new TestEnhancer()); // Here below additional enhancers can be added
		enhancers.add(new VEnhancer()); // NEW: My VEnhancer performing the V-transform
		enhancers.add(new SmartVEnhancer()); // NEW: My smart enhancer performing V-transform and return with best MAD value.

		return enhancers;
	}

	
	public static class ConfigurationDialog extends DialogFragment {
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.select_configuration).setItems(
					selectedEnhancer.getConfigurationOptions(),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							selectedConfiguration = which;
							progressDialog.setProgress(0);
							progressDialog.show();
							new ImproveImageTask().execute(theImage);

						}
		    });
		    return builder.create();
		}
	}

	/**
	 * VConfigurationDialog - will open a dialog for the user to select from NumberPicker, of how
	 * many segments the user would like compute the V-transform for.
	 */
	public static class VConfigurationDialog extends DialogFragment {



		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {

			NumberPicker numberPicker = new NumberPicker(getActivity());

			numberPicker.setMinValue(1);
			numberPicker.setMaxValue((int) Math.round(Math.sqrt(theImage.getWidth() * theImage.getHeight())));
			numberPicker.setWrapSelectorWheel(false);
			numberPicker.setOnValueChangedListener((picker, oldVal, newVal) -> Log.d(TAG, "onValueChange: old value = " + oldVal + " new value = " + newVal));

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setView(numberPicker).setTitle("Number of Segments").setMessage("Choose number of segments: ");
			builder.setPositiveButton("Select", (dialog, i) -> {
				Log.d(TAG, "onClick: " + numberPicker.getValue());
				selectedConfiguration = numberPicker.getValue();
				progressDialog.setProgress(0);
				progressDialog.show();
				new ImproveImageTask().execute(theImage);
			});

			builder.setNegativeButton("Cancel", (dialog, i) -> Log.d(TAG, "v-transform: abort configuration"));




			return builder.create();

		}


	}

	/**
	 * Updates the segmentation chosen with details about what was input by the user and the
	 * computed segmentation that the algorithm used for segments to be of equal size.
	 * @param input
	 * @param actual
	 */
	@SuppressLint("SetTextI18n")
	public static void updateSegmentTextV(int input, int actual){
		chosen = input;
		computed = actual;
	}

	/**
	 * Updates the segmentation bestV with details about what the best number of segmentations was
	 * when Smart Enhance was used.
	 * @param best
	 */
	@SuppressLint("SetTextI18n")
	public static void updateSegmentTextSmartEnhance(int best){
		bestV = best;
	}

	/**
	 * saveDialog - method is used by saveButton and will prompt the user for input. The input in
	 * the text field will be the name for the saved image. There is no need for any file extension
	 * since this is handled by the "saveImage" method where it will always be a JPEG. This simplifies
	 * the task of saving for the user and removes possible human errors such as writing incompatible
	 * file extension, although there is now no flexibility for the user when wanting to save.
	 * It will also give feedback to the User if there is nothing to save with a Toast.
	 */
	private void saveDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("File name");
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		builder.setView(input);

		builder.setPositiveButton("OK", (dialog, i) -> {
			if (afterImageView.getDrawable() != null) {
				try {

					Bitmap imageToSave = ((BitmapDrawable) afterImageView.getDrawable()).getBitmap();
					saveImage(imageToSave, input.getText().toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				Toast.makeText(MainActivity.this, "Nothing to Save" , Toast.LENGTH_SHORT).show();
				Log.d("DEBUG", "saveButton: onClick with no improved image");
			}
		});

		builder.setNegativeButton("Cancel", (dialog,i) -> {
			Log.d(TAG, "inputDialog: cancel, abort");
			dialog.cancel();
		});

		builder.show();
	}

}
