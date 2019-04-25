package com.thesavior.activities;

import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.thesavior.google.services.Place;
import com.thesavior.google.services.PlacesService;
import com.thesavior.utilities.Constants;
import com.thesavior.utilities.Information;
import com.thesavior.utilities.SharedPreference;
import com.thesavior.utilities.ViewAnim;

public class SearchEmergencyContactActivity extends ListActivity {
	/** Called when the activity is first created. */
	private static final String TAG = "SEARCH EMERGENCY CONTACTS";
	private TextView heading;
	private ScrollView searchMainPage;
	private RelativeLayout searchListPage;
	private Button searchPolice;
	private Button searchFire;
	private Button searchHospital;
	private Button about, back;
	private Animation Left_in_animation;
	private Animation Right_in_animation;
	private Animation Left_out_animation;
	private Animation Right_out_animation;
	private int layoutId = 0;
	private double latitude = 0;
	private double longitude = 0;
	private String[] placeName;
	private String placeNameInDetails = null;
	private String place;
	private String getRefrence[];
	private ListView list;
	private Bundle bundle;
	private MediaPlayer mPlayerForAlarm;
	private AudioManager audioManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_search_emergency);

		Left_in_animation = AnimationUtils.loadAnimation(this,
				R.anim.slide_in_left);
		Right_in_animation = AnimationUtils.loadAnimation(this,
				R.anim.slide_in_right);
		Left_out_animation = AnimationUtils.loadAnimation(this,
				R.anim.slide_out_left);
		Right_out_animation = AnimationUtils.loadAnimation(this,
				R.anim.slide_out_right);

		searchMainPage = (ScrollView) findViewById(R.id.scrollView_SearchMain);
		searchListPage = (RelativeLayout) findViewById(R.id.relativeLayout_searchList);
		searchMainPage.setVisibility(View.VISIBLE);
		searchListPage.setVisibility(View.GONE);
		bundle = getIntent().getExtras();
		latitude = bundle.getDouble("LATITUDE");
		longitude = bundle.getDouble("LONGITUDE");
		Log.v(TAG, String.valueOf("Lat : " + latitude + "\nLng : " + longitude));
		about = (Button) findViewById(R.id.btn_about_info);
		about.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Information.showDialogs1(SearchEmergencyContactActivity.this,
						getResources().getString(R.string.About_us));
			}
		});
		back = (Button) findViewById(R.id.btn_back);
		back.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				callBack();
			}
		});
		heading = (TextView) findViewById(R.id.textView_heading);
		heading.setText(getResources().getString(
				R.string.text_header_SearchEmargencyContact));
		searchPolice = (Button) findViewById(R.id.btn_SearchPolice);
		searchPolice.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				layoutId = 1;
				checkInternetAvalability("police");
			}
		});
		searchFire = (Button) findViewById(R.id.btn_SearchFire);
		searchFire.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				layoutId = 1;
				checkInternetAvalability("fire_station");
			}
		});
		searchHospital = (Button) findViewById(R.id.btn_SearchHospital);
		searchHospital.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				layoutId = 1;
				checkInternetAvalability("hospital");
			}
		});
		list = getListView();
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String phoneNumber = findLocationDetails(getRefrence[position]);
				if (phoneNumber != null)
					showDetailsInDialog(phoneNumber);
				else
					showDetailsInDialog("No Number is Available");
			}
		});
		SharedPreference.putBoolean(SearchEmergencyContactActivity.this,
				Constants.IS_OPEN_SERACH_ACTIVITY, true);
		Log.d(TAG,
				"status :"
						+ SharedPreference.getBoolean(
								SearchEmergencyContactActivity.this,
								Constants.IS_OPEN_SERACH_ACTIVITY));

		if (SharedPreference.getBoolean(SearchEmergencyContactActivity.this,
				"SOUND")) {
			startAlarm();
		}
	}

	private void setLayoutAnimation(String place) {
		if (place.equalsIgnoreCase("police"))
			heading.setText("NEAREST POLICE STATIONS");
		if (place.equalsIgnoreCase("fire_station"))
			heading.setText("NEAREST FIRE STATIONS");
		if (place.equalsIgnoreCase("hospital"))
			heading.setText("NEAREST HOSPITALS");
		searchListPage.startAnimation(Left_in_animation);
		searchListPage.setVisibility(View.VISIBLE);
		searchMainPage.startAnimation(Left_out_animation);
		searchMainPage.setVisibility(View.GONE);
	}

	private void checkInternetAvalability(String place) {
		final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
		if (activeNetwork != null
				&& activeNetwork.getState() == NetworkInfo.State.CONNECTED) {
			new GetPlaces(SearchEmergencyContactActivity.this, list, latitude,
					longitude, place).execute();
		} else {
			showDetailsInDialog(getResources().getString(
					R.string.Error_internetNotAvailable));
		}
	}

	class GetPlaces extends AsyncTask<Void, Void, Boolean> {
		Context context;
		private ListView listView;
		private ProgressDialog progressBar;
		private double latitude;
		private double longitude;
		boolean result = false;

		public GetPlaces(Context context, ListView listView, double lat,
				double lng, String places) {
			this.context = context;
			this.listView = listView;
			this.latitude = lat;
			this.longitude = lng;
			place = places;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			progressBar = new ProgressDialog(context);
			progressBar.setIndeterminate(true);
			progressBar.setMessage("Loading...");
			progressBar.show();
			setLayoutAnimation(place);
		}

		@Override
		protected Boolean doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			String[] allLocationGet = findNearLocation(latitude, longitude,
					place);
			if (allLocationGet.length > 0 && allLocationGet != null)
				return result = true;
			else
				return result;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result) {
				if (progressBar.isShowing())
					progressBar.dismiss();
				this.listView.setAdapter(new ArrayAdapter<String>(context,
						android.R.layout.simple_list_item_1, placeName));
				ViewAnim animation = new ViewAnim();
				list.startAnimation(animation);
			} else {
				Toast.makeText(SearchEmergencyContactActivity.this,
						getResources().getString(R.string.Error_notConnect),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	public String[] findNearLocation(double lat, double lng, String place) {
		PlacesService service = new PlacesService(Constants.GOOGLE_API_KEY);
		/**
		 * Hear you should call the method find nearest place near to central
		 * park new delhi then we pass the lat and lang of central park. hear
		 * you can be pass you current location lat and lang. The third argument
		 * is used to set the specific place if you pass the atm the it will
		 * return the list of nearest atm list. if you want to get the every
		 * thing then you should be pass "" only hear you should be pass the you
		 * current location latitude and Longitude.
		 */
		List<Place> findPlaces = service.findPlaces(lat, lng, place);
		placeName = new String[findPlaces.size()];
		getRefrence = new String[findPlaces.size()];
		for (int i = 0; i < findPlaces.size(); i++) {
			Place placeDetail = findPlaces.get(i);
			placeDetail.getIcon();
			System.out.println(placeDetail.getName()
					+ placeDetail.getVicinity() + placeDetail.getLatitude()
					+ placeDetail.getLongitude() + placeDetail.getReference());
			placeName[i] = "\n" + placeDetail.getName() + "\nAddress : "
					+ placeDetail.getVicinity() + "\n";
			getRefrence[i] = placeDetail.getReference();
		}
		return getRefrence;
	}

	private String findLocationDetails(String refrence) {
		PlacesService service = new PlacesService(Constants.GOOGLE_API_KEY);
		List<Place> findPlaces = service.findPlacesInDetails(refrence);
		for (int i = 0; i < findPlaces.size(); i++) {
			Place placeDetail = findPlaces.get(i);
			placeNameInDetails = placeDetail.getInternational_phone_number();
		}
		return placeNameInDetails;
	}

	private void showDetailsInDialog(final String phone) {
		AlertDialog.Builder builder = new Builder(
				SearchEmergencyContactActivity.this);
		if (phone.equalsIgnoreCase(getResources().getString(
				R.string.Error_internetNotAvailable))) {
			builder.setTitle(android.R.string.dialog_alert_title);
			builder.setIcon(android.R.drawable.presence_offline);
		} else {
			builder.setTitle("Calling");
			builder.setIcon(android.R.drawable.sym_action_call);
		}
		builder.setMessage(phone);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (phone.equalsIgnoreCase("No Number is Available")
						|| phone.equalsIgnoreCase("Internet connectivity is not available.")) {
					dialog.dismiss();
				} else {
					if (SharedPreference.getBoolean(
							SearchEmergencyContactActivity.this, "SOUND"))
						stopAlarm();
					Intent calling = new Intent(Intent.ACTION_CALL, Uri
							.parse("tel:" + phone));
					startActivity(calling);
					SearchEmergencyContactActivity.this.finish();
				}
			}
		});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void callBack() {
		if (layoutId == 0) {
			if (SharedPreference.getBoolean(
					SearchEmergencyContactActivity.this, "SOUND"))
				stopAlarm();
			startActivity(new Intent(SearchEmergencyContactActivity.this,
					EmergencyActivity.class));
			SearchEmergencyContactActivity.this.finish();
		}
		if (layoutId == 1) {
			heading.setText(getResources().getString(
					R.string.text_header_SearchEmargencyContact));
			searchMainPage.startAnimation(Right_in_animation);
			searchMainPage.setVisibility(View.VISIBLE);
			searchListPage.startAnimation(Right_out_animation);
			searchListPage.setVisibility(View.GONE);
			layoutId = 0;
		}
	}

	private void startAlarm() {
		stopAlarm();
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 100,
				AudioManager.FLAG_PLAY_SOUND);
		mPlayerForAlarm = MediaPlayer.create(
				SearchEmergencyContactActivity.this, R.raw.siren);
		mPlayerForAlarm.setLooping(true);
		Log.d(TAG, " start alarm ");
		mPlayerForAlarm.start();
	}

	private void stopAlarm() {
		if (mPlayerForAlarm != null) {
			Log.d(TAG, " stop alarm ");
			mPlayerForAlarm.stop();
			mPlayerForAlarm.release();
			mPlayerForAlarm = null;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			callBack();
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		SearchEmergencyContactActivity.this.finish();
	}

}