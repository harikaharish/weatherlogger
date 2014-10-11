package com.hbytes.weather;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PushReceiver extends BroadcastReceiver {

	private static final String TAG = "onReceive";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.d(TAG, "In push Receiver Class");
		try {
			//if (intent  != null) {

				//Log.d(TAG, "Receiver intent null");
			//} else {
				String action = intent.getAction();
				Log.d(TAG, "got action " + action);
				if (action.equals("com.parse.starter.UPDATE_STATUS")) {
					String channel = intent.getExtras().getString(
							"com.parse.Channel");
					JSONObject json = new JSONObject(intent.getExtras()
							.getString("com.parse.Data"));

					Log.d("onReceive", "got action " + action + " on channel "
							+ channel + " with:");
					Iterator itr = json.keys();
					while (itr.hasNext()) {
						String key = (String) itr.next();
						if (key.equals("customdata")) {
							Intent pupInt = new Intent(context, ParseStarterProjectActivity.class);
							pupInt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							context.getApplicationContext().startActivity(
									pupInt);
						}
						Log.d("onReceive",
								"..." + key + " => " + json.getString(key));
						Log.d(TAG, "end of pushReciever, pushnotification is sent");

					}
				}
			//}

		} catch (JSONException e) {
			Log.d("onReceive", "JSONException: " + e.getMessage());
		}

	}
}
