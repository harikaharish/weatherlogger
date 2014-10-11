package com.hbytes.weather;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseQuery;
import com.parse.PushService;

public class ParseStarterProjectActivity extends Activity {
	private GoogleMap googleMap;
	private LocationManager locationManager;
	private final String tag = "ParseStarterProjectActivity";

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.maps);

		Parse.initialize(this, "IGNONyjV9bw0rvFSRV5TZjrxOChcHCYnJUUEJr0w",
				"QEBR8t3TXErxxGBaSnscI1hWY1BwRIPFI7iZypIE");
		PushService.setDefaultPushCallback(this,
				ParseStarterProjectActivity.class);
		ParseAnalytics.trackAppOpened(getIntent());
		ParseInstallation.getCurrentInstallation().saveInBackground();
		
		ParseObject testObject = new ParseObject("TestObject");
		testObject.put("foo", "bar");
		testObject.saveInBackground();
		locationManager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

		try {
			// Loading map
			initilizeMap();

		} catch (Exception e) {
			Log.d(tag, "Unalble to initialize maps");
			e.printStackTrace();
		}

		placingMarker();
		currentLocation();
		retrieveDataFromParse();
		// deleteMarker();

	}

	private void initilizeMap() {
		if (googleMap == null) {
			googleMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();
			// check if map is created successfully or not
			if (googleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Sorry! Unable to render maps", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	public void placingMarker() {
		googleMap.setOnMapLongClickListener(new OnMapLongClickListener() {

			@Override
			public void onMapLongClick(final LatLng point) {

				AlertDialog.Builder alert = new AlertDialog.Builder(
						ParseStarterProjectActivity.this);
				alert.setTitle("Water Log Alert");

				alert.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								// Drawing marker on the map
								googleMap.addMarker(new MarkerOptions()
										.position(point)
										.icon(BitmapDescriptorFactory
												.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
										.title(point.toString()));

								saveDataToParse(point.latitude, point.longitude);

								Toast.makeText(getBaseContext(),
										"Marker is added to the Map",
										Toast.LENGTH_SHORT).show();
								pushNotifications();
							}
						});
				alert.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.cancel();
							}
						});
				alert.show();

			}

		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		initilizeMap();
	}

	public void currentLocation() {
		googleMap.setMyLocationEnabled(true);
		Criteria criteria = new Criteria();
		String provider = locationManager.getBestProvider(criteria, true);
		Location myLocation = locationManager.getLastKnownLocation(provider);
		googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		try {
			// Get latitude of the current location
			double latitude = myLocation.getLatitude();
	
			// Get longitude of the current location
			double longitude = myLocation.getLongitude();
	
			// Create a LatLng object for the current location
			LatLng latLng = new LatLng(latitude, longitude);
	
			// Show the current location in Google Map
			googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
	
			// Zoom in the Google Map
			googleMap.animateCamera(CameraUpdateFactory.zoomTo(18));
			
			/*googleMap.addMarker(new MarkerOptions()
					.position(new LatLng(latitude, longitude))
					.title("You are here!")
					.icon(BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));*/
	
			//saveDataToParse(latitude, longitude);
			//Toast.makeText(getBaseContext(), "Checkin is done", Toast.LENGTH_SHORT)
					//.show();
		} catch(Exception e){
			Log.d("currentLocation", "Issue with location manager");
		}

	}

	public void saveDataToParse(double lat, double lng) {
		ParseGeoPoint point = new ParseGeoPoint(lat, lng);
		ParseObject placeObject = new ParseObject("markers");
		placeObject.put("loc", point);
		placeObject.saveInBackground();
		Log.d("ParseStarterProjectActivity", "dataSaved" + point);
	}

	public void retrieveDataFromParse() {

		ParseQuery<ParseObject> query = ParseQuery.getQuery("markers");

		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> list, ParseException e) {
				// TODO Auto-generated method stub

				if (e == null) {

					for (ParseObject myobject : list) {

						Log.d("latitude from parse", ""
								+ myobject.getParseGeoPoint("loc")
										.getLatitude());
						Log.d("longitude from parse", ""
								+ myobject.getParseGeoPoint("loc")
										.getLongitude());
						LatLng point = new LatLng(myobject.getParseGeoPoint(
								"loc").getLatitude(), myobject
								.getParseGeoPoint("loc").getLongitude());
						googleMap.addMarker(new MarkerOptions()
								.position(point)
								.icon(BitmapDescriptorFactory
										.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
								.title(point.toString()));

					}

				} else {
					// something went wrong
				}

			}
		});
	}
	
	public void pushNotifications(){
		Log.d("pushNotifications", "inside");
		JSONObject object = new JSONObject();
		try {
			object.put("alert","Water Logged Alert");
			object.put("customdata", "My string");
			object.put("action","Action");
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
	
	private void buildAlertMessageNoGps() {
	    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
	           .setCancelable(false)
	           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	               public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
	                   startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	               }
	           })
	           .setNegativeButton("No", new DialogInterface.OnClickListener() {
	               public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
	                    dialog.cancel();
	               }
	           });
	    final AlertDialog alert = builder.create();
	    alert.show();
	}
	
	public void checkinLocation(MenuItem item){
		if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
	        buildAlertMessageNoGps();
	    } else {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle("Checkin");
			alertDialogBuilder.setMessage("Do You Want to Checkin your Location?" );
			alertDialogBuilder.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
	
							googleMap.setMyLocationEnabled(true);
							LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
							Criteria criteria = new Criteria();
							String provider = locationManager.getBestProvider(criteria, true);
							Location myLocation = locationManager.getLastKnownLocation(provider);
							googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
	
							// Get latitude of the current location
							double latitude = myLocation.getLatitude();
	
							// Get longitude of the current location
							double longitude = myLocation.getLongitude();
	
							// Create a LatLng object for the current location
							LatLng latLng = new LatLng(latitude, longitude);
	
							// Show the current location in Google Map
							googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
	
							// Zoom in the Google Map
							googleMap.animateCamera(CameraUpdateFactory.zoomTo(18));
							googleMap.addMarker(new MarkerOptions()
									.position(new LatLng(latitude, longitude))
									.title("You are here!")
									.icon(BitmapDescriptorFactory
											.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
	
							saveDataToParse(latitude, longitude);
							Toast.makeText(getBaseContext(), "Checkin is done", Toast.LENGTH_SHORT)
									.show();
							pushNotifications();
						}
					});
			alertDialogBuilder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.cancel();
						}
					});
			alertDialogBuilder.show();
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
		if (id == R.id.settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
