package com.hbytes.weather;

import java.util.List;

import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {

	Button loginButton;
	Button logoutButton;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Get the view from main.xml
		setContentView(R.layout.settings);
		// Locate EditTexts in main.xml
		logoutButton = (Button) findViewById(R.id.logout);
		loginButton = (Button) findViewById(R.id.login);
		
		userNameDisplay();
	}

	public void userLogout(View view) {
		if (null != ParseUser.getCurrentUser())
			ParseUser.logOut();
		Toast.makeText(getApplicationContext(), "You are now logged out",
				Toast.LENGTH_SHORT).show();
		Intent intent = new Intent(SettingsActivity.this,
				ParseStarterProjectActivity.class);
		startActivity(intent);
		finish();
	}

	public void userLogin(View view) {
		Intent intent = new Intent(SettingsActivity.this,
				LoginSignupActivity.class);
		startActivity(intent);
		finish();
	}
	
	public ParseUser userNameDisplay(){
		TextView username = (TextView) findViewById(R.id.username);
		
		ParseUser currentUser = userAuthenticationCheck();
		if (null == currentUser) {
			username.setText("You are not logged in. Please Login");
        } else {
            username.setText(currentUser.getUsername());
            ParseQuery<ParseObject> query = ParseQuery.getQuery("markers");
    		query.whereEqualTo("createdBy", currentUser.getUsername());
    		query.countInBackground(new CountCallback() {
				
				@Override
				public void done(int arg0, ParseException arg1) {
					// TODO Auto-generated method stub
					TextView postsCount = (TextView) findViewById(R.id.postscount);
					Log.d("userNameDisplay", "count : " + arg0);
					postsCount.setText(arg0+"");
				}
			});
        }
		return currentUser;
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
}
