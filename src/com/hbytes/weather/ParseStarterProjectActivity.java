package com.hbytes.weather;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
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
	Geocoder geocoder;
	List<Address> addresses;
	Location loc;

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
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

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
		// userAuthenticationCheck();
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

			/*
			 * googleMap.addMarker(new MarkerOptions() .position(new
			 * LatLng(latitude, longitude)) .title("You are here!")
			 * .icon(BitmapDescriptorFactory
			 * .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
			 */

			// saveDataToParse(latitude, longitude);
			// Toast.makeText(getBaseContext(), "Checkin is done",
			// Toast.LENGTH_SHORT)
			// .show();
		} catch (Exception e) {
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
						StringBuilder str = null;
						try {
							geocoder = new Geocoder(
									ParseStarterProjectActivity.this,
									Locale.ENGLISH);
							addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1);
							str = new StringBuilder();
							if (geocoder.isPresent()) {
								Toast.makeText(getApplicationContext(),
										"geocoder present", Toast.LENGTH_SHORT)
										.show();
								Address returnAddress = addresses.get(0);

								String localityString = returnAddress
										.getLocality();
								String city = returnAddress.getCountryName();
								String region_code = returnAddress
										.getCountryCode();
								String zipcode = returnAddress.getPostalCode();

								str.append(localityString + "");
								str.append(city + "" + region_code + "");
								str.append(zipcode + "");
								Toast.makeText(getApplicationContext(), str,
										Toast.LENGTH_SHORT).show();

							} else {
								Toast.makeText(getApplicationContext(),
										"geocoder not present",
										Toast.LENGTH_SHORT).show();
							}

							// } else {
							// Toast.makeText(getApplicationContext(),
							// "address not available",
							// Toast.LENGTH_SHORT).show();
							// }
						} catch (IOException exception) {
							// TODO Auto-generated catch block

							Log.e("tag", exception.getMessage());
						}

						googleMap.addMarker(new MarkerOptions()
								.position(point)
								.icon(BitmapDescriptorFactory
										.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
								.title(str.toString()));

					}

				} else {
					// something went wrong
				}

			}
		});
	}

	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				"Your GPS seems to be disabled, do you want to enable it?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(
									@SuppressWarnings("unused") final DialogInterface dialog,
									@SuppressWarnings("unused") final int id) {
								startActivity(new Intent(
										android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							@SuppressWarnings("unused") final int id) {
						dialog.cancel();
					}
				});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	public void checkInCustomLocation() {
		googleMap.setOnMapLongClickListener(new OnMapLongClickListener() {

			@Override
			public void onMapLongClick(final LatLng location) {
				navigateToPostIssue(location.latitude, location.longitude);
			}

		});
	}

	public void checkinCurrentLocation(MenuItem item) {
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			buildAlertMessageNoGps();
		} else {
			LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			Criteria criteria = new Criteria();
			String provider = locationManager.getBestProvider(criteria, true);
			Location location = locationManager.getLastKnownLocation(provider);
			// Get latitude of the current location
			navigateToPostIssue(location.getLatitude(), location.getLongitude());
		}
	}

	public void navigateToSettings(MenuItem item) {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
		finish();
	}

	public void navigateToPostIssue(double latitude, double longitude) {
		Intent intent = new Intent(this, PostIssueActivity.class);
		Bundle b = new Bundle();
		b.putDouble("latitude", latitude);
		b.putDouble("longitude", longitude);
		intent.putExtras(b); // Put your id to your next Intent
		startActivity(intent);
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
