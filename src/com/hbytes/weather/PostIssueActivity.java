package com.hbytes.weather;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class PostIssueActivity extends Activity{
	static final int REQUEST_IMAGE_CAPTURE = 1;
	ParseUser user;
	ImageView imageView;
	String mCurrentPhotoPath;
	File photoFile;
	static final int REQUEST_TAKE_PHOTO = 1;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the view from main.xml
        setContentView(R.layout.postissue);
        imageView = (ImageView) findViewById(R.id.issueImageView);
        imageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            	dispatchTakePictureIntent();
            }

        });
        EditText message = (EditText) findViewById(R.id.msg);
        Button okBtn = (Button) findViewById(R.id.ok);
        Button cancelBtn = (Button) findViewById(R.id.cancel);
        user = userAuthenticationCheck();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
	    	Bitmap bitmap = null;
	    	if(null != data){
	    		Bundle extras = data.getExtras();
	    		bitmap = (Bitmap) extras.get("data");
	    	} else if(null != photoFile) {
				try{
					Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(photoFile));
					bitmap = ThumbnailUtils.extractThumbnail(bmp, 512, 384);
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
				    bitmap.compress(CompressFormat.JPEG, 70, stream);
				} catch(Exception e){
					e.printStackTrace();
				}
	    	}
	        imageView.setImageBitmap(bitmap);
	    }
	}
	
	
	
	private File createImageFile() throws IOException {
	    // Create an image file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    String imageFileName = "JPEG_" + timeStamp + "_";
	    File storageDir = Environment.getExternalStoragePublicDirectory(
	            Environment.DIRECTORY_PICTURES);
	    File image = File.createTempFile(
	        imageFileName,  /* prefix */
	        ".jpg",         /* suffix */
	        storageDir      /* directory */
	    );

	    // Save a file: path for use with ACTION_VIEW intents
	    mCurrentPhotoPath = "file:" + image.getAbsolutePath();
	    return image;
	}
	
	private void dispatchTakePictureIntent() {
	    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    // Ensure that there's a camera activity to handle the intent
	    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
	        // Create the File where the photo should go
	        photoFile = null;
	        try {
	            photoFile = createImageFile();
	        } catch (IOException ex) {
	            // Error occurred while creating the File
	        }
	        // Continue only if the File was successfully created
	        if (photoFile != null) {
	            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
	                    Uri.fromFile(photoFile));
	            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
	        }
	    }
	}
	
	public void saveDataToParse(double lat, double lng, ParseUser user) {
		byte[] imageData = null;
		ParseFile parseFile = null;
		ParseObject placeObject = new ParseObject("markers");
		ParseGeoPoint point = new ParseGeoPoint(lat, lng);
		placeObject.put("loc", point);
		placeObject.put("createdBy", user.getUsername());

		if(null != mCurrentPhotoPath){
			try{
				Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(photoFile));
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
			    bitmap.compress(CompressFormat.JPEG, 70, stream);
			    imageData = stream.toByteArray();
			    parseFile = new ParseFile(imageData);
				parseFile.saveInBackground(new SaveCallback() {
			        public void done(ParseException e) {
			            if (e != null) {
			                Toast.makeText(PostIssueActivity.this,
			                        "Error saving: " + e.getMessage(),
			                                Toast.LENGTH_LONG).show();
			            }
			        }
			    });
				placeObject.put("image", parseFile);
			} catch(Exception e){
				e.printStackTrace();
			}
		}
		placeObject.saveInBackground();
		Log.d("PostIssueActivity", "dataSaved" + point);
		pushNotification();
	}
	
	public void pushNotification(){
		Log.d("pushNotifications", "inside");
		JSONObject object = new JSONObject();
		try {
			object.put("alert","Water Log Alert");
			ParsePush push = new ParsePush();
			ParseQuery query = ParseInstallation.getQuery();
			push.setQuery(query);
			push.setMessage("Water might be logged in this area. Avoid this area if possible");
			push.setData(object);
			push.sendInBackground();
			Toast.makeText(getBaseContext(), "Push Notification is sent sucessfully", Toast.LENGTH_SHORT)
			.show();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ParseUser userAuthenticationCheck(){
		ParseUser currentUser = null;
		if (ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser())) {
            // If user is anonymous, send the user to LoginSignupActivity.class
            Intent intent = new Intent(this,
                    LoginSignupActivity.class);
            startActivity(intent);
        } else {
            // If current user is NOT anonymous user
            // Get current user data from Parse.com
            currentUser = ParseUser.getCurrentUser();
            if (null == currentUser) {
            	// Send user to LoginSignupActivity.class
                Intent intent = new Intent(this,
                        LoginSignupActivity.class);
                startActivity(intent);
            }
        }
		return currentUser;
	}
	
	public void oKbutton(View view){
		Bundle b = getIntent().getExtras();
        double latitude = b.getDouble("latitude");
        double longitude = b.getDouble("longitude");
		saveDataToParse(latitude, longitude, user);
		finish();
	}
	
	public void cancelbutton(View view){
		Intent intent = new Intent(this, ParseStarterProjectActivity.class);
		startActivity(intent);
		finish();
	}

}
