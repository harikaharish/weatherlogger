package com.hbytes.adapters;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;
import com.hbytes.weather.ParseStarterProjectActivity;
import com.hbytes.weather.R;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class CustomInfoWindowAdapter implements InfoWindowAdapter {
	private LayoutInflater mInflater;
	private String tag = "CustomInfoWindowAdapter";
	View v;

	public CustomInfoWindowAdapter(LayoutInflater inflater) {
		this.mInflater = inflater;
	}

	private LayoutInflater getLayoutInflater() {
		// TODO Auto-generated method stub
		return mInflater;
	}

	@Override
	public View getInfoContents(final Marker marker) {
		// TODO Auto-generated method stub
		Log.d(tag, "inside getInfoContents");
		return null;
	}

	@Override
	public View getInfoWindow(final Marker marker) {
		// TODO Auto-generated method stub
		Log.d(tag, "inside getInfoWindow");
		v = mInflater.inflate(R.layout.info_window, null);
		v = getLayoutInflater().inflate(R.layout.info_window, null);

		ParseQuery<ParseObject> query = ParseQuery.getQuery("markers");
		ParseObject obj;
		try {
			obj = query.get(marker.getTitle());
			byte[] data = null;
			ParseFile imageFile = (ParseFile) obj.get("image_thumbnail");
			if (null != imageFile) {
				data = imageFile.getData();
				Log.d(tag, "imagefile received");
				// data has the bytes for the resume
				ImageView issueImageView = (ImageView) v.findViewById(R.id.issueImageThumbnail);
				issueImageView.setImageBitmap(BitmapFactory.decodeByteArray(data, 0,
						data.length));
			}

			TextView postedDateView = (TextView) v.findViewById(R.id.postedDate);
			postedDateView.setText(obj.getUpdatedAt().toString());

			Geocoder geocoder;

			if (Geocoder.isPresent()) {
				List<Address> addresses;
				StringBuilder str = null;
				try {
					geocoder = new Geocoder(getLayoutInflater().getContext(),
							Locale.ENGLISH);
					addresses = geocoder.getFromLocation(
							marker.getPosition().latitude,
							marker.getPosition().longitude, 1);
					Log.d(tag, "got addresses");
					str = new StringBuilder();
					Address returnAddress = addresses.get(0);
					str.append(returnAddress.getAddressLine(0));
					Log.d(tag, str.toString());
					if (!str.toString().isEmpty()) {
						TextView addressView = (TextView) v
								.findViewById(R.id.address);
						addressView.setText(str.toString());
					}
				} catch (IOException exception) {
					// TODO Auto-generated catch block

					Log.e("tag", exception.getMessage());
				}
			}
			
	    	ParseQuery<ParseObject> relations = ParseQuery.getQuery("likedislike");
	    	relations.whereEqualTo("markerId", obj.getObjectId());
	    	relations.whereEqualTo("likes", true);
	    	int arg0 = relations.count();
			TextView likesCount = (TextView) v.findViewById(R.id.likesCount);
			Log.d(tag, "likes count : " + arg0);
			likesCount.setText(arg0+"");
			
	    	ParseQuery<ParseObject> dislikeRelations = ParseQuery.getQuery("likedislike");
	    	dislikeRelations.whereEqualTo("markerId", obj.getObjectId());
	    	dislikeRelations.whereEqualTo("likes", false);
	    	arg0 = dislikeRelations.count();	    	
			TextView dislikesCount = (TextView) v.findViewById(R.id.dislikesCount);
			Log.d(tag, "likes count : " + arg0);
			dislikesCount.setText(arg0+"");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return v;
	}

}
