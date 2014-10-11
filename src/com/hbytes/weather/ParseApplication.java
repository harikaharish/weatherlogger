package com.hbytes.weather;

import android.app.Application;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class ParseApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		// Add your initialization code here
		Parse.initialize(this, "IGNONyjV9bw0rvFSRV5TZjrxOChcHCYnJUUEJr0w",
				"QEBR8t3TXErxxGBaSnscI1hWY1BwRIPFI7iZypIE");

		ParseUser.enableAutomaticUser();
		ParseACL defaultACL = new ParseACL();

		// If you would like all objects to be private by default, remove this
		// line.
		defaultACL.setPublicReadAccess(true);

		ParseACL.setDefaultACL(defaultACL, true);

		ParsePush.subscribeInBackground("", new SaveCallback() {

			@Override
			public void done(ParseException e) {
				// TODO Auto-generated method stub
				if (e != null) {
					Log.d("com.parse.push",
							"successfully subscribed to the broadcast channel.");
				} else {
					Log.e("com.parse.push", "failed to subscribe for push", e);
				}

			}

		});

	}

}
