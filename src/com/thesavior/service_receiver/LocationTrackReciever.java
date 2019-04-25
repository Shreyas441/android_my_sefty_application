package com.thesavior.service_receiver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.thesavior.utilities.SharedPreference;

public class LocationTrackReciever extends BroadcastReceiver {
	private final String TAG = "LocationTrackReciever";
	private boolean gps_enabled = false;
	private boolean network_enabled = false;
	private MyLocation mylocationListener;
	private LocationManager manager;
	private long updateTimeMsec = 1000L;
	private double latitude = 0;
	private double longitude = 0;
	private String getAddressInString = "";
	private String latLongString;
	private Context mContext;
	private String phoneNo;
	private String messageForSending = "Hi,\nNow I am at ";

	// private int totalContactNo;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.d(TAG, "on Recieve of LocationTrackReciever.");
		this.mContext = context;
		turnGPSOn();
		try {
			if (SharedPreference.getBoolean(mContext, "GPS")) {
				Log.d(TAG, "Time called : " + Calendar.getInstance().getTime());
				Log.d(TAG, "gps enabled.");
				manager = (LocationManager) context
						.getSystemService(Context.LOCATION_SERVICE);
				mylocationListener = new MyLocation();
				Criteria criteria = new Criteria();
				criteria.setAccuracy(Criteria.ACCURACY_FINE);
				criteria.setSpeedRequired(true);
				criteria.setAltitudeRequired(false);
				criteria.setBearingRequired(false);
				criteria.setCostAllowed(true);
				criteria.setPowerRequirement(Criteria.POWER_LOW);
				gps_enabled = manager
						.isProviderEnabled(LocationManager.GPS_PROVIDER);
				network_enabled = manager
						.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
				Log.d(TAG, "++ gps enabled +++ " + gps_enabled);
				Log.d(TAG, "++ network enabled +++ " + network_enabled);
				Log.d(TAG, "++ get current location +++ ");
				if (gps_enabled) {
					Log.d(TAG, "++ gps enabled +++ ");
					manager.requestLocationUpdates(
							LocationManager.GPS_PROVIDER, 10 * updateTimeMsec,
							500.0f, mylocationListener);
				}
				if (network_enabled) {
					Log.d(TAG, "++ network enabled +++ ");
					manager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER,
							50 * updateTimeMsec, 500.0f, mylocationListener);
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "Error during finding location: " + e.getMessage());
		}

	}

	// here create class for getting address from gps
	public class MyLocation implements LocationListener {
		public void onLocationChanged(Location location) {
			Log.d(TAG, "on location changed");
			if (location != null) {
				latitude = location.getLatitude();
				longitude = location.getLongitude();
				latLongString = "Lat: " + latitude + "\nLng: " + longitude;
				// System.out.println(latLongString);
				Log.d(TAG, latLongString);
				if (SharedPreference.getBoolean(mContext, "GPS")) {
					getAddress(latitude, longitude);
				}
			} else {
				latLongString = "No Location Found";
				Log.d(TAG, latLongString);
			}

		}

		public void onProviderDisabled(String provider) {
			Toast.makeText(mContext, "Gps Disabled", Toast.LENGTH_SHORT).show();
		}

		public void onProviderEnabled(String provider) {
			Toast.makeText(mContext, "Gps Enabled", Toast.LENGTH_SHORT).show();
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			if (gps_enabled) {
				Log.d(TAG, "++onStatusChanged gps enabled +++ ");
				manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
						10 * updateTimeMsec, 500.0f, mylocationListener);
			}
			if (network_enabled) {
				Log.d(TAG, "++onStatusChanged network enabled +++ ");
				manager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, 50 * updateTimeMsec,
						500.0f, mylocationListener);
			}
		}
	}

	public void getAddress(double latitude, double longitude) {
		Geocoder geoCoder = new Geocoder(mContext, Locale.getDefault());
		String add = "";
		try {
			latLongString = "Lat: " + latitude + " Lng: " + longitude;
			Log.d(TAG, TAG + " convert Address : " + latLongString);
			List<Address> addresses1 = geoCoder.getFromLocation(latitude,
					longitude, 1);
			if (addresses1.size() > 0) {
				for (int i = 0; i < addresses1.get(0).getMaxAddressLineIndex(); i++)
					add += addresses1.get(0).getAddressLine(i);
			}
			getAddressInString = add;
			Log.d(TAG, TAG + " Address : " + getAddressInString);
			phoneNo = SharedPreference.getdata(mContext, "step1_phone");
			if (!TextUtils.isEmpty(phoneNo)) {
				String maplink = "https://maps.google.com/maps?q=" + latitude
						+ "," + longitude;
				messageForSending = messageForSending + " "
						+ getAddressInString + ". Show Location on Map:\n"
						+ Html.fromHtml(maplink);
				StringTokenizer st = new StringTokenizer(phoneNo, ",");
				Log.v(TAG,
						"total Contact numbers For send Messages "
								+ st.countTokens());
				while (st.hasMoreElements()) {
					String tempMobileNumber = (String) st.nextElement();
					if (tempMobileNumber.length() > 0
							&& messageForSending.trim().length() > 0) {
						Log.d(TAG, "total Contact numbers For send Messages "
								+ st.countTokens() + " :" + tempMobileNumber);
						if (messageForSending.length() < 160)
							sendSMS(tempMobileNumber, messageForSending, false);
						else
							sendSMS(tempMobileNumber, messageForSending, true);
						if (st.hasMoreElements() == false)
							stopGpsListner();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// ---sends a SMS message to another device---
	private void sendSMS(String phoneNumber, String message, boolean split) {
		Log.v(TAG, "Complete message for sending : " + messageForSending);
		Log.v(TAG,
				"++++ message character length ++++"
						+ messageForSending.length());
		SmsManager smsManager = SmsManager.getDefault();
		if (!split) {
			Log.d(TAG, "Sending single message: " + message);
			smsManager.sendTextMessage(phoneNumber, null, message, null, null);
		} else {
			Log.v(TAG, "Sending '" + message + "' in multiple parts.");
			ArrayList<String> parts = smsManager.divideMessage(message);
			Log.v(TAG, "SMS in " + parts.size() + " parts:");
			for (String string : parts) {
				Log.v("SMSTest", string);
			}

			smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null,
					null);
		}

	}

	private void stopGpsListner() {
		if (manager != null)
			manager.removeUpdates(mylocationListener);
		Log.d(TAG, "++ remove listener +++ ");
		turnGPSOff();

	}

	private void turnGPSOn() {
		String provider = Settings.Secure.getString(
				mContext.getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

		if (!provider.contains("gps")) { // if gps is disabled
			final Intent poke = new Intent();
			poke.setClassName("com.android.settings",
					"com.android.settings.widget.SettingsAppWidgetProvider");
			poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
			poke.setData(Uri.parse("3"));
			mContext.sendBroadcast(poke);
			Log.v(TAG, "Gps Enabled From Settings.");
		}
	}

	private void turnGPSOff() {
		String provider = Settings.Secure.getString(
				mContext.getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

		if (provider.contains("gps")) { // if gps is enabled
			final Intent poke = new Intent();
			poke.setClassName("com.android.settings",
					"com.android.settings.widget.SettingsAppWidgetProvider");
			poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
			poke.setData(Uri.parse("3"));
			mContext.sendBroadcast(poke);
			Log.v(TAG, "Gps Disabled From Settings.");
		}
	}

}
