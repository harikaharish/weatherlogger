package com.hbytes.weather;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class PostIssueActivity extends Activity{
	static final int REQUEST_IMAGE_CAPTURE = 1;
	ParseUser user;
	ImageView imageView;
	String mCurrentPhotoPath;
	File photoFile;
	Bitmap thumbnailBM;
	static final int REQUEST_TAKE_PHOTO = 1;
	protected static final int REQUEST_OK = 1;
	private String tag = "PostIssueActivity";
	private String voiceMessage;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the view from main.xml
        setContentView(R.layout.postissue);
        Bundle b = getIntent().getExtras();
        voiceMessage = b.getString("voiceMessage");
        userAuthenticationCheck();
        if(null != voiceMessage && voiceMessage.contains("post")){
        	oKbutton(null);
        } else {
            EditText messageView = (EditText) findViewById(R.id.msg);
            messageView.setText(voiceMessage);
            imageView = (ImageView) findViewById(R.id.issueImageView);
            imageView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                	dispatchTakePictureIntent();
                }

            });      	
        }
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
	    	thumbnailBM = null;
	    	if(null != data){
	    		Bundle extras = data.getExtras();
	    		thumbnailBM = (Bitmap) extras.get("data");
	    	} else if(null != photoFile) {
				try{
					Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(photoFile));
					thumbnailBM = ThumbnailUtils.extractThumbnail(bmp, 384, 512);
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					thumbnailBM.compress(CompressFormat.JPEG, 70, stream);
				} catch(Exception e){
					e.printStackTrace();
				}
	    	}
	        imageView.setImageBitmap(thumbnailBM);
	    } else if (requestCode==REQUEST_OK  && resultCode==RESULT_OK) {
        	ArrayList<String> thingsYouSaid = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			Log.d(tag, "Received Voice Command: "+thingsYouSaid);
        	for(String command: thingsYouSaid){
        		if(command.contains("post")){
        			oKbutton(null);
        		}
        	}
        	//((TextView)findViewById(R.id.voiceCommandFeedBack)).setText(thingsYouSaid.get(0));
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
	
	public void saveDataToParse(double lat, double lng) {
		byte[] imageData = null;
		ParseFile parseFile = null;
		ParseObject placeObject = new ParseObject("markers");
		ParseGeoPoint point = new ParseGeoPoint(lat, lng);
		placeObject.put("loc", point);
		//Double check to avoid issue when used back button and not creating a login
		userAuthenticationCheck();
		if(null != user){
			placeObject.put("createdBy", user.getUsername());
			if(null == voiceMessage){
		        EditText messageView = (EditText) findViewById(R.id.msg);
		        placeObject.put("message", messageView.getText().toString());
			} else {
				placeObject.put("message", voiceMessage);
			}
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
					ByteArrayOutputStream thumbnailStream = new ByteArrayOutputStream();
					thumbnailBM.compress(CompressFormat.JPEG, 70, thumbnailStream);
					byte[] thumbnailData = thumbnailStream.toByteArray();
				    ParseFile thumbnail = new ParseFile(thumbnailData);
				    thumbnail.saveInBackground(new SaveCallback() {
				        public void done(ParseException e) {
				            if (e != null) {
				                Toast.makeText(PostIssueActivity.this,
				                        "Error saving: " + e.getMessage(),
				                                Toast.LENGTH_LONG).show();
				            }
				        }
				    });
					placeObject.put("image", parseFile);
					placeObject.put("image_thumbnail", thumbnail);
				} catch(Exception e){
					e.printStackTrace();
				}
			}
			placeObject.saveInBackground();
			Log.d("PostIssueActivity", "dataSaved" + point);
			pushNotification(lat, lng);
			finish();			
		} else {
			Log.d(tag, "User still missing. UserAuthenticationCheck called");
			userAuthenticationCheck();
		}

	}
	
	public void pushNotification(double lat, double lng){
		Log.d("pushNotifications", "inside");
		JSONObject object = new JSONObject();
		String location = "Mumbai";
		try {
			Geocoder geocoder;

			if (Geocoder.isPresent()) {
				List<Address> addresses;
				StringBuilder str = null;
				try {
					geocoder = new Geocoder(getLayoutInflater().getContext(),
							Locale.ENGLISH);
					addresses = geocoder.getFromLocation(
							lat,
							lng, 1);
					Log.d(tag, "got addresses");
					str = new StringBuilder();
					Address returnAddress = addresses.get(0);
					str.append(returnAddress.getAddressLine(0));
					location = str.toString();
					Log.d(tag, str.toString());
				} catch (IOException exception) {
					// TODO Auto-generated catch block

					Log.e("tag", exception.getMessage());
				}
				
			}			
			object.put("alert","New Alert @"+location);
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
	
	public void userAuthenticationCheck(){
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
		user = currentUser;
	}
	
	public void oKbutton(View view){
		Bundle b = getIntent().getExtras();
        double latitude = b.getDouble("latitude");
        double longitude = b.getDouble("longitude");
		saveDataToParse(latitude, longitude);
	}
	
	public void cancelbutton(View view){
		Intent intent = new Intent(this, ParseStarterProjectActivity.class);
		startActivity(intent);
		finish();
	}
	

	public void voiceCheckInCurrentLocation(MenuItem menu) {
		 Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
         i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
         try {
             startActivityForResult(i, REQUEST_OK);
         } catch (Exception e) {
        	 Toast.makeText(this, "Error initializing speech to text engine.", Toast.LENGTH_LONG).show();
         }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.voiceCheckin) {
			voiceCheckInCurrentLocation(null);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
