package com.hbytes.weather;

import com.parse.ParseUser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
	}

	public void userLogout(View view) {
		if (null != ParseUser.getCurrentUser())
			ParseUser.logOut();
		Toast.makeText(getApplicationContext(), "You are now logged out",
				Toast.LENGTH_SHORT).show();
		Intent intent = new Intent(SettingsActivity.this,
				LoginSignupActivity.class);
		startActivity(intent);
		finish();
	}

	public void userLogin(View view) {
		Intent intent = new Intent(SettingsActivity.this,
				LoginSignupActivity.class);
		startActivity(intent);
		finish();
	}
}
