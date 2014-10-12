package com.hbytes.weather;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
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
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.PushService;

public class ParseStarterProjectActivity extends Activity {
	private GoogleMap googleMap;
	private LocationManager locationManager;
	private final String tag = "ParseStarterProjectActivity";
	static final int REQUEST_IMAGE_CAPTURE = 1;

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

		checkInCustomLocation();
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


	@Override
	protected void onResume() {
		super.onResume();
		initilizeMap();
	}

	public void currentLocation() {
		//userAuthenticationCheck();
		googleMap.setMyLocationEnabled(true);
		Criteria criteria = new Criteria();
		String provider = locationManager.getBestProvider(criteria, true);
		Location myLocation = locationManager.getLastKnownLocation(provider);
		googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
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
	
	public void pushNotification(){
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
	
	public void checkInCustomLocation() {
		googleMap.setOnMapLongClickListener(new OnMapLongClickListener() {

			@Override
			public void onMapLongClick(final LatLng point) {
				final ParseUser user = userAuthenticationCheck();
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
								saveDataToParse(point.latitude, point.longitude, user);
								Toast.makeText(getBaseContext(),
										"Marker is added to the Map",
										Toast.LENGTH_SHORT).show();
								pushNotification();
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

	public void checkinCurrentLocation(MenuItem item){
		final ParseUser user = userAuthenticationCheck();
		if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
	        buildAlertMessageNoGps();
	    } else {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.CustomDialog);
			alertDialogBuilder.setTitle("Issue Checkin");
			alertDialogBuilder.setMessage("Do You Want to Checkin your Location?" );
			final EditText msg = new EditText(this);
			alertDialogBuilder.setView(msg);
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
	
							//saveDataToParse(latitude, longitude);
							saveDataToParse(latitude, longitude, user);
							Toast.makeText(getBaseContext(), "Checkin is done", Toast.LENGTH_SHORT)
									.show();
							pushNotification();
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

	public void saveDataToParse(double lat, double lng, ParseUser user) {
		ParseGeoPoint point = new ParseGeoPoint(lat, lng);
		ParseObject placeObject = new ParseObject("markers");
		placeObject.put("loc", point);
		placeObject.put("createdBy", user.getUsername());
		placeObject.saveInBackground();
		Log.d("ParseStarterProjectActivity", "dataSaved" + point);
	}
	
	public ParseUser userAuthenticationCheck(){
		ParseUser currentUser = null;
		if (ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser())) {
            // If user is anonymous, send the user to LoginSignupActivity.class
            Intent intent = new Intent(ParseStarterProjectActivity.this,
                    LoginSignupActivity.class);
            startActivity(intent);
        } else {
            // If current user is NOT anonymous user
            // Get current user data from Parse.com
            currentUser = ParseUser.getCurrentUser();
            if (null == currentUser) {
            	// Send user to LoginSignupActivity.class
                Intent intent = new Intent(ParseStarterProjectActivity.this,
                        LoginSignupActivity.class);
                startActivity(intent);
            }
        }
		return currentUser;
	}

    public void navigateToSettings(MenuItem item){
    	Intent intent = new Intent(this,SettingsActivity.class);
        startActivity(intent);   
        finish();
    }
    
	private void dispatchTakePictureIntent() {
		if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
		    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
		        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
		    }
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
