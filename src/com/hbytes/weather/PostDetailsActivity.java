package com.hbytes.weather;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.R.drawable;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class PostDetailsActivity extends Activity {
	private String tag = "PostDetailsActivity";
	private Context context;
	private LinearLayout commentsSectionView;
	private String markerId;
	private ParseObject obj;
	private ParseUser user;
	private ImageView like;
	private ImageView dislike;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        // Get the view from main.xml
        setContentView(R.layout.post_details);
        context = this;
        user = userAuthenticationCheck();
        
        if(user == null){
        	
        } else {
        	like = (ImageView) findViewById(R.id.likeDetail);
        	dislike = (ImageView) findViewById(R.id.dislikeDetail);
     		Bundle b = getIntent().getExtras();
            markerId = b.getString("objectId");
            Log.d(tag, "working on "+markerId);
    		ParseQuery<ParseObject> query = ParseQuery.getQuery("markers");
    		
    		try {
    			obj = query.get(markerId);
    			if(null != obj){
    				byte[] data = null;
    				ParseFile imageFile = (ParseFile) obj.get("image_thumbnail");
    				if (null != imageFile) {
    					data = imageFile.getData();
    					Log.d(tag , "imagefile received");
    					// data has the bytes for the resume
    					ImageView issueImageView = (ImageView) findViewById(R.id.issueImageViewDetail);
    					issueImageView.setImageBitmap(BitmapFactory.decodeByteArray(data, 0,
    							data.length));
    				}

    				TextView postedDateView = (TextView) findViewById(R.id.postedDateDetail);
    				postedDateView.setText(obj.getUpdatedAt().toString());
    				
    				TextView userView = (TextView) findViewById(R.id.postedUser);
    				userView.setText(obj.getString("createdBy"));
    				
    				TextView messageView = (TextView) findViewById(R.id.messageDetail);
    				messageView.setText(obj.getString("message"));
    				
    				Geocoder geocoder;

    				if (Geocoder.isPresent()) {
    					List<Address> addresses;
    					StringBuilder str = null;
    					try {
    						geocoder = new Geocoder(getLayoutInflater().getContext(),
    								Locale.ENGLISH);
    						ParseGeoPoint marker = obj.getParseGeoPoint("loc");
    						addresses = geocoder.getFromLocation(
    								marker.getLatitude(),
    								marker.getLongitude(), 1);
    						Log.d(tag, "got addresses");
    						str = new StringBuilder();
    						Address returnAddress = addresses.get(0);
    						str.append(returnAddress.getAddressLine(0)+" ");
    						str.append(returnAddress.getAddressLine(1));
    						Log.d(tag, str.toString());
    						if (!str.toString().isEmpty()) {
    							TextView addressView = (TextView) findViewById(R.id.addressDetail);
    							addressView.setText(str.toString());
    						}
    					} catch (IOException exception) {
    						// TODO Auto-generated catch block

    						Log.e("tag", exception.getMessage());
    					}
    					
    				}
    				loadComments();

					ParseRelation relation = obj.getRelation("likes");
					 
					// generate a query based on that relation
					ParseQuery relationsQuery = relation.getQuery();
					relationsQuery.countInBackground(new CountCallback() {
						
						@Override
						public void done(int arg0, ParseException arg1) {
							// TODO Auto-generated method stub
							Log.d(tag, "Total likes: "+arg0);
							TextView likesCountView = (TextView) findViewById(R.id.likesCountDetail);
							likesCountView.setText(arg0+"");
						}
					});
					
    		        like.setOnClickListener(new OnClickListener() {

    					@Override
    		            public void onClick(View v) {
    						user = userAuthenticationCheck();
    						if(null != user){
	    		            	ParseObject relation = new ParseObject("likedislike");
	    		            	relation.put("feedbackBy", user.getUsername());
	    		            	relation.put("markerId", obj.getObjectId());
	    		            	relation.put("likes", true);
	    		            	
	    		            	Log.d(tag, "User "+user.getUsername()+" liked this post");
	    		            	relation.saveInBackground(new SaveCallback() {
									
									@Override
									public void done(ParseException arg0) {
										// TODO Auto-generated method stub
										if(arg0 == null){
											like.setImageResource(drawable.star_big_on);
										} else {
											arg0.printStackTrace();
										}
									}
								});
	    		            	
    						}
    		            }

    		        }); 
    		        
    		        dislike.setOnClickListener(new OnClickListener() {

    					@Override
    		            public void onClick(View v) {
    						user = userAuthenticationCheck();
    						if(null != user){
	    		            	ParseObject relation = new ParseObject("likedislike");
	    		            	relation.put("feedbackBy", user.getUsername());
	    		            	relation.put("markerId", obj.getObjectId());
	    		            	relation.put("likes", false);
	    		            	
	    		            	Log.d(tag, "User "+user.getUsername()+" disliked this post");
	    		            	relation.saveInBackground(new SaveCallback() {
									
									@Override
									public void done(ParseException arg0) {
										// TODO Auto-generated method stub
										if(arg0 == null){
											dislike.setImageResource(drawable.star_big_off);
										} else {
											arg0.printStackTrace();
										}
									}
								});
    						}
    		            }

    		        });    		        
    			}
    		} catch (ParseException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    				        	
        }
        
	}
	
	public void loadComments(){
		Log.d(tag, "Loading feedback");
		commentsSectionView = (LinearLayout) findViewById(R.id.commentsSection);
		ParseQuery<ParseObject> query = ParseQuery.getQuery("comments");
		query.whereEqualTo("markerId", markerId);
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> arg0, ParseException arg1) {
				// TODO Auto-generated method stub
				Log.d(tag, "Total comments: "+arg0.size());
				for(ParseObject obj: arg0){
					LayoutParams lparams = new LayoutParams(
							   LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					TextView tv=new TextView(context);
					tv.setText("- "+obj.getString("comments"));
					commentsSectionView.addView(tv);
				}
			}
		});
		ParseObject obj;
		
	}
	
	public void saveCommentsInParse(){
		EditText commentView = (EditText) findViewById(R.id.comment);
		if(!commentView.getText().toString().isEmpty()){
			ParseObject comments = new ParseObject("comments");
			comments.put("comments", commentView.getText().toString());
			ParseUser user= userAuthenticationCheck();
			if(null != user){
				comments.put("createdBy", user.getUsername());
			}
			comments.put("markerId", markerId);
			comments.saveInBackground(new SaveCallback() {
				
				@Override
				public void done(ParseException arg0) {
					// TODO Auto-generated method stub
					if(null != arg0){
						Log.d(tag, "Error saving data");
						arg0.printStackTrace();
						Toast.makeText(getApplicationContext(),
								"Sorry! Your feedback could not saved. Please try again", Toast.LENGTH_LONG)
								.show();
					} else {
						Toast.makeText(getApplicationContext(),
								"Thank You! Your feedback is now be saved", Toast.LENGTH_SHORT)
								.show();
					}
				}
			});

			
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
		saveCommentsInParse();
		finish();
	}
	
	public void cancelbutton(View view){
		finish();
	}
}
