package com.thesavior.activities;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.thesavior.utilities.Constants;
import com.thesavior.utilities.Information;
import com.thesavior.utilities.NetworkUtil;
import com.thesavior.utilities.SharedPreference;

public class EmergencyActivity extends Activity {
	/** Called when the activity is first created. */
	private static final String TAG = " EMERGENCY ACTIVITY ";
	private String latLongString;
	private long counterMinutesInMilliseconds = 1000L;
	private long updateTimeMsec = 1000L;
	private double latitude = 0;
	private double longitude = 0;
	private int msg_sendStatus = 0;
	private int SUCCESS = 0;
	private boolean is_gps_enabled = false;
	private boolean is_network_enabled = false;
	private boolean isCounterStart = false;
	private Button stop;
	private Button btn_msg;
	private Button btn_contactCall;
	private Button btn_police_call;
	private TextView heading;
	private TextView chronometer;
	private MediaPlayer mPlayerForAlarm;
	private Dialog myDialog, alarmDialog;
	private MyLocation listener;
	private LocationManager locationManager;
	private AudioManager audioManager;
	private static Context mContext;
	private CountDownTimer countDownTimerForAlarm;
	private BroadcastReceiver sendBroadcastReceiver;
	private BroadcastReceiver deliveryBroadcastReceiver;
	private String SENT = "SMS_SENT";
	private String DELIVERED = "SMS_DELIVERED";

	// here create class for getting address from gps
	public class MyLocation implements LocationListener {
		public void onLocationChanged(Location location) {
			Log.d("Emergency Screen", "on location changed");
			if (location != null) {
				Log.d("Emergency Screen", "location not null");
				updateWithNewLocation(location);
			}
		}

		public void onProviderDisabled(String provider) {
			Toast.makeText(mContext, "Gps Disabled", Toast.LENGTH_SHORT).show();
		}

		public void onProviderEnabled(String provider) {
			Toast.makeText(mContext, "Gps Enabled", Toast.LENGTH_SHORT).show();
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.d("emergency", "++ on status changed +++ ");
			if (is_gps_enabled) {
				Log.d("emergency", "++ gps enabled +++ ");
				locationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, 10 * updateTimeMsec,
						500.0f, listener);
			}
			if (is_network_enabled) {
				Log.d("emergency", "++ network enabled +++ ");
				locationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, 50 * updateTimeMsec,
						500.0f, listener);
			}
		}
	}

	public void updateWithNewLocation(Location location) {
		if (location != null) {
			latitude = location.getLatitude();
			longitude = location.getLongitude();
			latLongString = "Lat: " + latitude + "\nLng: " + longitude;
			Log.d("Emergency Screen", latLongString);
		} else {
			latLongString = "No Location Found";
			Log.d("Emergency Screen", latLongString);
		}
		if (SharedPreference.getBoolean(EmergencyActivity.this, "CALL_POLICE")) {
			if (latitude != 0 && longitude != 0) {
				btn_police_call.setCompoundDrawablesWithIntrinsicBounds(null,
						null,
						mContext.getResources().getDrawable(R.drawable.check),
						null);
			} else {
				btn_police_call
						.setCompoundDrawablesWithIntrinsicBounds(
								null,
								null,
								mContext.getResources().getDrawable(
										R.drawable.uncheck), null);
			}
		}
		if (SharedPreference.getBoolean(EmergencyActivity.this, "MESSAGE")) {
			getAddress(latitude, longitude);
		}
	}

	public void getAddress(double latitude, double longitude) {
		Geocoder geoCoder = new Geocoder(mContext, Locale.getDefault());
		String currentLocationAddress = "";
		try {
			latLongString = "Lat: " + latitude + " Lng: " + longitude;
			Log.d("Emergency Screen", " convert Address : " + latLongString);
			List<Address> addresses1 = geoCoder.getFromLocation(latitude,
					longitude, 1);
			if (addresses1.size() > 0) {
				for (int i = 0; i < addresses1.get(0).getMaxAddressLineIndex(); i++)
					currentLocationAddress += addresses1.get(0).getAddressLine(
							i);
			}
			Log.d("Emergency Screen", "Current Location Address : "
					+ currentLocationAddress);
			String maplink = "https://maps.google.com/maps?q=" + latitude + ","
					+ longitude;
			Log.d(TAG, "Map link : " + maplink);
			/**
			 * Here send emails with the current location of user
			 */
			sendEmailsWithLocation(currentLocationAddress, maplink);
			/**
			 * Here send sms with the current location of user
			 */
			sendMessagesWithLocation(currentLocationAddress, maplink);

		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, e.getMessage());
			if (NetworkUtil.getConnectivityStatus(EmergencyActivity.this) == NetworkUtil.TYPE_NOT_CONNECTED) {
				Toast.makeText(
						EmergencyActivity.this,
						getResources().getString(
								R.string.Error_internetNotAvailable),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void sendMessagesWithLocation(String currentAddress,
			String locationMapLink) {
		// check message sending status
		if (SharedPreference.getBoolean(EmergencyActivity.this, "MESSAGE")) {
			String phoneNoForSendSMS = SharedPreference.getdata(
					EmergencyActivity.this, "step1_phone");
			String messageForSMS = SharedPreference.getdata(
					EmergencyActivity.this, "step1_msg");
			if (phoneNoForSendSMS.length() > 0) {
				if (messageForSMS.equalsIgnoreCase("")) {
					messageForSMS = "Hi, I am in trouble, please help me.";
				}
				String extraMessageBody = " Am currently at: " + currentAddress
						+ ". See Location on Map:\n"
						+ Html.fromHtml(locationMapLink);
				messageForSMS = messageForSMS + extraMessageBody;
				Log.d(TAG, "Message for sending SMS : " + messageForSMS);
				msg_sendStatus = Constants.MSG_SEND_STATUS;
				StringTokenizer st = new StringTokenizer(phoneNoForSendSMS, ",");
				Log.v(TAG,
						"total Contact numbers For send Messages "
								+ st.countTokens());
				while (st.hasMoreElements()) {
					String tempMobileNumber = (String) st.nextElement();
					if (tempMobileNumber.length() > 0
							&& messageForSMS.trim().length() > 0) {
						Log.d(TAG, "total Contact numbers For send Messages "
								+ st.countTokens() + " :" + tempMobileNumber);
						if (messageForSMS.length() < 160)
							sentSMS(tempMobileNumber, messageForSMS, false);
						else
							sentSMS(tempMobileNumber, messageForSMS, true);
					}
				}
			}
		}
	}

	private void sendEmailsWithLocation(String currentAddress,
			String locationMapLink) {
		String EmailIdForSendMsg = SharedPreference.getdata(mContext,
				"step2_phone");
		if (EmailIdForSendMsg.length() > 0) {

			String mailBody = "Hey, am in trouble, please help me. Am currently at : "
					+ currentAddress
					+ ".\n\n"
					+ "See Location on Map :\n"
					+ Html.fromHtml(locationMapLink);
			Log.d(TAG, "Message for sending Email : " + mailBody);
			StringTokenizer st = new StringTokenizer(EmailIdForSendMsg, ",");
			Log.v(TAG, "total email ID " + st.countTokens());
			while (st.hasMoreElements()) {
				String tempSeperateEmailID = (String) st.nextElement();
				if (tempSeperateEmailID.length() > 0) {
					Log.d(TAG,
							"total email id for send mail " + st.countTokens()
									+ " :" + tempSeperateEmailID);
					checkConnectivityAndSendMail(tempSeperateEmailID, mailBody);
				}
			}
		}
	}

	// ---sends a SMS message to another device---
	public void registerSMSReciever() {
		Log.v(TAG, "++++ registering sms reciever ++++");
		/**
		 * registering broadcast reciever for sending sms ---when the SMS has
		 * been sent---
		 */
		sendBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					SUCCESS = 0;
					Toast();
					if (msg_sendStatus == Constants.MSG_SEND_STATUS)
						btn_msg.setCompoundDrawablesWithIntrinsicBounds(
								null,
								null,
								mContext.getResources().getDrawable(
										R.drawable.check), null);
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					SUCCESS = 1;
					Toast();
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					SUCCESS = 1;
					Toast();
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					SUCCESS = 1;
					Toast();
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					SUCCESS = 1;
					Toast();
					break;
				}
			}
		};
		/**
		 * registering broadcast reciever for delivered sms ---when the SMS has
		 * been delivered---
		 */
		deliveryBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(EmergencyActivity.this, "SMS delivered",
							Toast.LENGTH_SHORT).show();
					break;
				case Activity.RESULT_CANCELED:
					Toast.makeText(EmergencyActivity.this, "SMS not delivered",
							Toast.LENGTH_SHORT).show();
					break;
				}
			}
		};
		registerReceiver(deliveryBroadcastReceiver, new IntentFilter(DELIVERED));
		registerReceiver(sendBroadcastReceiver, new IntentFilter(SENT));
	}

	private void sentSMS(String phoneNumber, String message, boolean split) {

		registerSMSReciever();

		Log.d(TAG, "++++ Send Message on Predefined Contact Numbers ++++");
		Log.v(TAG, "++++ message ++++" + message);
		Log.v(TAG, "++++ message character length ++++" + message.length());
		Log.v(TAG, "++++ phone no ++++" + phoneNumber);

		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(
				SENT), 0);
		PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
				new Intent(DELIVERED), 0);
		SmsManager sms = SmsManager.getDefault();
		if (!split) {
			Log.d(TAG, "Sending single message: " + message);
			sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
		} else {
			Log.d(TAG, "Sending '" + message + "' in multiple parts.");
			ArrayList<String> parts = sms.divideMessage(message);
			Log.d(TAG, "SMS in " + parts.size() + " parts:");
			for (String string : parts) {
				Log.v("SMSTest", string);
			}
			ArrayList<PendingIntent> sentList = new ArrayList<PendingIntent>();
			ArrayList<PendingIntent> deliveredList = new ArrayList<PendingIntent>();
			for (int i = 0; i < parts.size(); i++) {
				sentList.add(sentPI);
				deliveredList.add(deliveredPI);
			}
			sms.sendMultipartTextMessage(phoneNumber, null, parts, sentList,
					deliveredList);
		}

	}

	public void Toast() {
		Toast ImageToast = new Toast(EmergencyActivity.this);
		LinearLayout toastLayout = new LinearLayout(getBaseContext());
		toastLayout.setOrientation(LinearLayout.HORIZONTAL);
		if (SUCCESS == 0) {
			toastLayout
					.setBackgroundResource(R.drawable.notification_bg_success);
			btn_msg.setCompoundDrawablesWithIntrinsicBounds(null, null,
					mContext.getResources().getDrawable(R.drawable.check), null);
		} else {
			toastLayout.setBackgroundResource(R.drawable.notification_bg_fail);
			btn_msg.setCompoundDrawablesWithIntrinsicBounds(null, null,
					mContext.getResources().getDrawable(R.drawable.uncheck),
					null);
		}
		ImageToast.setView(toastLayout);
		ImageToast.setDuration(Toast.LENGTH_LONG);
		ImageToast.setGravity(Gravity.CENTER | Gravity.TOP, 0, 0);
		ImageToast.show();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		unlockScreen();
		setContentView(R.layout.activity_emergency);
		Log.d(TAG, "on create");
		mContext = this;
		initViews();
		stop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				displayStopDialog();
			}
		});
		btn_contactCall.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (SharedPreference.getBoolean(EmergencyActivity.this,
						"CONTACT")) {
					stopAlarm();
					String phoneNoForCall = SharedPreference.getdata(
							EmergencyActivity.this, "step3_phone");
					Intent callIntent = new Intent(Intent.ACTION_CALL, Uri
							.parse("tel:" + phoneNoForCall));
					startActivity(callIntent);
					EmergencyActivity.this.finish();
				}
			}
		});
		btn_police_call.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (SharedPreference.getBoolean(EmergencyActivity.this,
						"CALL_POLICE")) {
					if (SharedPreference.getBoolean(EmergencyActivity.this,
							"SOUND"))
						stopAlarm();

					Log.v(TAG, "lat_long for search near location "
							+ "\nLatitude : " + latitude + "\nLongitude : "
							+ longitude);
					Intent callIntent = new Intent(EmergencyActivity.this,
							SearchEmergencyContactActivity.class);
					callIntent.putExtra("LATITUDE", latitude);
					callIntent.putExtra("LONGITUDE", longitude);
					startActivity(callIntent);
					EmergencyActivity.this.finish();

				}
			}
		});

		Log.d(TAG,
				"OPEN_SERACH_ACTIVITY status :"
						+ SharedPreference.getBoolean(EmergencyActivity.this,
								Constants.IS_OPEN_SERACH_ACTIVITY));
		if (!SharedPreference.getBoolean(EmergencyActivity.this,
				Constants.IS_OPEN_SERACH_ACTIVITY)) {
			displayTriggerAlarmDialog();
		} else {
			if (SharedPreference.getBoolean(EmergencyActivity.this, "SOUND")) {
				startAlarm();
			}
		}
	}

	private void unlockScreen() {
		// this.getWindow().addFlags(
		// WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		// this.getWindow().addFlags(
		// WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		// this.getWindow().addFlags(
		// WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		this.getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
						| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	private void lockScreen() {
		this.getWindow()
				.addFlags(
						WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
								| WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
	}

	private void initViews() {
		btn_msg = (Button) findViewById(R.id.btn_call_msg);
		btn_contactCall = (Button) findViewById(R.id.btn_call_contact);
		btn_police_call = (Button) findViewById(R.id.btn_call_police1);
		heading = (TextView) findViewById(R.id.textView_heading);
		heading.setText(getResources().getString(
				R.string.text_header_EmargencyPage));
		stop = (Button) findViewById(R.id.btn_emergency_stop);
		Typeface FONT_TYPE2 = Typeface.createFromAsset(this.getAssets(),
				"fonts/arista2.0.ttf");
		stop.setTypeface(FONT_TYPE2);
	}

	public void getStatus() {

		turnGPSOn();

		GetCurrentLocation();

		if (SharedPreference.getBoolean(EmergencyActivity.this, "SOUND")) {
			startAlarm();
		}

		if (SharedPreference.getBoolean(EmergencyActivity.this, "CONTACT"))
			btn_contactCall.setCompoundDrawablesWithIntrinsicBounds(null, null,
					getResources().getDrawable(R.drawable.check), null);
		else
			btn_contactCall.setCompoundDrawablesWithIntrinsicBounds(null, null,
					getResources().getDrawable(R.drawable.uncheck), null);

	}

	public void GetCurrentLocation() {

		try {
			// don't start listeners if no provider is enabled
			locationManager = (LocationManager) mContext
					.getSystemService(Context.LOCATION_SERVICE);
			listener = new MyLocation();
			Log.d(TAG, "get current location .");
			final Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			criteria.setSpeedRequired(true);
			criteria.setAltitudeRequired(false);
			criteria.setBearingRequired(false);
			criteria.setCostAllowed(true);
			criteria.setPowerRequirement(Criteria.POWER_LOW);
			// getting GPS status
			is_gps_enabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
			// getting network status
			is_network_enabled = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			Log.i(TAG, "++ get current location +++ ");
			Log.d(TAG, "++ gps enabled +++ " + is_gps_enabled);
			Log.d(TAG, "++ network enabled +++ " + is_network_enabled);

			if (!is_gps_enabled && !is_network_enabled) {
				// no network provider is enabled
				AlertDialog.Builder builder = new Builder(mContext);
				builder.setTitle("Attention!");
				builder.setIcon(android.R.drawable.ic_dialog_alert);
				builder.setMessage("Sorry, location is not determined. Please enable location providers");
				builder.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
				builder.create().show();
			} else {
				if (is_gps_enabled) {
					Log.d(TAG, "++ gps enabled +++ " + is_gps_enabled);
					locationManager.requestLocationUpdates(
							LocationManager.GPS_PROVIDER, 10 * updateTimeMsec,
							500.0f, listener);
					Log.d("GPS Enabled", "GPS Enabled");
				}
				// First get location from Network Provider
				if (is_network_enabled) {
					Log.d(TAG, "++ network enabled +++ " + is_network_enabled);
					locationManager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER,
							50 * updateTimeMsec, 500.0f, listener);
					Log.d("Network", "Network");
				}
			}
		} catch (Exception ex) {
		}
	}

	public void checkConnectivityAndSendMail(String EmailId, String msgBody) {
		final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
		if (activeNetwork != null
				&& activeNetwork.getState() == NetworkInfo.State.CONNECTED
				&& activeNetwork.isConnectedOrConnecting()) {
			new SendingMail(EmergencyActivity.this, EmailId, msgBody, true)
					.execute();
		} else {
			// Information.showConnectionDialogs(getParent(), getResources()
			// .getString(R.string.Error_internetNotAvailable));
			CustomDialogClass customDialog = new CustomDialogClass(
					EmergencyActivity.this, false, getResources().getString(
							R.string.Error_internetNotAvailable),
					getResources().getString(R.string.app_name_inSmall));
			customDialog.show();
		}
	}

	public class SendingMail extends AsyncTask<Void, Void, String> {
		String result = null;
		String EmailIdForSendMsg;
		String messageBody;
		boolean callingActivityStatus;
		Activity actRef;

		public SendingMail(Activity actRef, String emailId, String messageBody,
				boolean status) {
			this.EmailIdForSendMsg = emailId;
			this.messageBody = messageBody;
			this.callingActivityStatus = status;
			this.actRef = actRef;
		}

		protected String doInBackground(final Void... unused) {
			result = Information.postData(EmailIdForSendMsg, messageBody);
			Log.v(TAG, "Result report : " + result);
			return result;
		}

		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			if (result != null) {
				if (result.contains("Error")) {
					Toast.makeText(
							actRef,
							actRef.getResources().getString(
									R.string.Error_messageNotSent),
							Toast.LENGTH_LONG).show();
					return;
				} else if (result.contains("Message has been sent")) {
					Toast.makeText(actRef, result, Toast.LENGTH_LONG).show();
					if (callingActivityStatus) {
						btn_msg.setCompoundDrawablesWithIntrinsicBounds(
								null,
								null,
								mContext.getResources().getDrawable(
										R.drawable.check), null);
						stopGpsListner();
					}
				} else {
					Toast.makeText(actRef, result, Toast.LENGTH_LONG).show();
					if (callingActivityStatus) {
						btn_msg.setCompoundDrawablesWithIntrinsicBounds(
								null,
								null,
								mContext.getResources().getDrawable(
										R.drawable.uncheck), null);
					}
				}
			} else {
				Toast.makeText(
						actRef,
						actRef.getResources().getString(
								R.string.Error_notConnect), Toast.LENGTH_LONG)
						.show();
			}

		}
	}

	private void displayStopDialog() {

		myDialog = new Dialog(EmergencyActivity.this);
		myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		myDialog.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		myDialog.setContentView(R.layout.passcode_dialog);
		myDialog.setCancelable(false);
		InputFilter[] FilterArray = new InputFilter[1];
		FilterArray[0] = new InputFilter.LengthFilter(4);

		TextView text_msg = (TextView) myDialog.findViewById(R.id.textView0);
		text_msg.setText(getResources().getString(
				R.string.text_changePasscodeMsg));
		// hide confirm passcode text view and edit text
		TextView text_cfmPasscode = (TextView) myDialog
				.findViewById(R.id.textView_confirmPasscode);
		text_cfmPasscode.setVisibility(View.GONE);
		EditText edt_cfmPasscode = (EditText) myDialog
				.findViewById(R.id.editText_ConfirmPasscode);
		edt_cfmPasscode.setVisibility(View.GONE);
		// hide email text view and edit text
		TextView text_id = (TextView) myDialog.findViewById(R.id.textView_mail);
		text_id.setVisibility(View.GONE);
		EditText mailId = (EditText) myDialog
				.findViewById(R.id.editText_retrieveEmailId);
		mailId.setVisibility(View.GONE);
		TextView text_accept = (TextView) myDialog
				.findViewById(R.id.textView_accept);
		text_accept.setText("Enter your Passcode to stoping the Alarm.");
		text_accept.setVisibility(View.VISIBLE);

		final EditText passcode = (EditText) myDialog
				.findViewById(R.id.editText_Passcode);
		passcode.setFilters(FilterArray);
		passcode.setInputType(InputType.TYPE_CLASS_NUMBER
				| InputType.TYPE_CLASS_PHONE);
		passcode.setTransformationMethod(PasswordTransformationMethod
				.getInstance());
		passcode.setImeOptions(EditorInfo.IME_ACTION_DONE);
		passcode.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (s.length() == 4) {
					if (TextUtils.equals(passcode.getText().toString(),
							(SharedPreference.getdata(EmergencyActivity.this,
									"PASSCODE")))) {
						showHomeActivity();
					} else
						passcode.setError("Please enter correct passcode!");
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		passcode.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					// Handle IME NEXT key
					if (TextUtils.isEmpty(passcode.getText().toString())) {
						passcode.setError("This field cannot be blank!");
						return true;
					} else if (passcode.getText().length() != 4
							&& passcode.getText().length() > 0) {
						passcode.setError("Oops! Passcode should in 4 digit.");
						return true;
					} else if (TextUtils.equals(passcode.getText().toString(),
							(SharedPreference.getdata(EmergencyActivity.this,
									"PASSCODE")))) {
						showHomeActivity();
					} else
						passcode.setError("Please enter correct passcode!");

				}
				return false;
			}
		});

		Button delete_ok = (Button) myDialog.findViewById(R.id.btn_delete_OK);
		delete_ok.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String enteredPasscode = passcode.getText().toString();
				if (TextUtils.equals(enteredPasscode, (SharedPreference
						.getdata(EmergencyActivity.this, "PASSCODE")))) {
					showHomeActivity();
				} else
					passcode.setError("Please enter correct passcode!");
			}
		});
		Button delete_cancel = (Button) myDialog
				.findViewById(R.id.btn_delete_cancel);
		delete_cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				myDialog.dismiss();
			}
		});
		myDialog.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					myDialog.dismiss();
				}
				return false;
			}
		});

		myDialog.show();

	}

	private void showHomeActivity() {
		if (SharedPreference.getBoolean(EmergencyActivity.this, "SOUND"))
			stopAlarm();
		myDialog.dismiss();
		SharedPreference.putBoolean(EmergencyActivity.this,
				Constants.IS_OPEN_SERACH_ACTIVITY, false);
		Log.d(TAG,
				"status :"
						+ SharedPreference.getBoolean(EmergencyActivity.this,
								Constants.IS_OPEN_SERACH_ACTIVITY));
		startActivity(new Intent(EmergencyActivity.this, HomeActivity.class));
		EmergencyActivity.this.finish();

	}

	private void displayTriggerAlarmDialog() {
		alarmDialog = new Dialog(EmergencyActivity.this);
		alarmDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		alarmDialog.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		alarmDialog.setContentView(R.layout.alarm_trigger_layout1);
		alarmDialog.setCancelable(false);
		chronometer = (TextView) alarmDialog.findViewById(R.id.chronometer);
		setCountDownInSeconds();
		Button delete_cancel = (Button) alarmDialog
				.findViewById(R.id.button_cancelAlarm);
		delete_cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (isCounterStart && countDownTimerForAlarm != null) {
					countDownTimerForAlarm.cancel();
					isCounterStart = false;
				}
				alarmDialog.dismiss();
				boolean screenLock = getIntent().getExtras().getBoolean(
						"SCREEN_LOCK");
				Log.d(TAG, "====== SCREEN LOCK STATUS : " + screenLock
						+ "======");
				if (screenLock == true) {
					lockScreen();
					EmergencyActivity.this.finish();
					System.exit(0);
				} else {
					startActivity(new Intent(EmergencyActivity.this,
							HomeActivity.class));
					EmergencyActivity.this.finish();
				}
			}
		});
		alarmDialog.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
				}
				return true;
			}
		});
		alarmDialog.show();
	}

	private void setCountDownInSeconds() {
		counterMinutesInMilliseconds = counterMinutesInMilliseconds
				* SharedPreference.getInt(EmergencyActivity.this,
						"ALARM_DELAY_TIME");
		final NumberFormat formatter1 = new DecimalFormat("00");
		countDownTimerForAlarm = new CountDownTimer(
				counterMinutesInMilliseconds, 1000) {
			public void onTick(long millisUntilFinished) {
				chronometer.setText(formatter1
						.format(millisUntilFinished / 1000));
				isCounterStart = true;
			}

			public void onFinish() {
				isCounterStart = false;
				if (alarmDialog.isShowing()) {
					alarmDialog.dismiss();
					getStatus();
				}

			}
		}.start();
	}

	private void startAlarm() {
		stopAlarm();
		audioManager = (AudioManager) mContext
				.getSystemService(Context.AUDIO_SERVICE);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 100,
				AudioManager.FLAG_PLAY_SOUND);
		mPlayerForAlarm = MediaPlayer.create(mContext, R.raw.siren);
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

	private void stopGpsListner() {
		if (locationManager != null)
			locationManager.removeUpdates(listener);
		Log.d("emergency", "++ remove listener +++ ");
		turnGPSOff();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (sendBroadcastReceiver != null || deliveryBroadcastReceiver != null) {
			unregisterReceiver(sendBroadcastReceiver);
			unregisterReceiver(deliveryBroadcastReceiver);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Toast.makeText(EmergencyActivity.this,
					"Back button is disable when alarm is triggered.",
					Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, TAG + " has been destroyed.");
		stopGpsListner();
		EmergencyActivity.this.finish();
	}

	private void turnGPSOn() {
		try {
			String provider = Settings.Secure.getString(getContentResolver(),
					Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

			if (!provider.contains("gps")) { // if gps is disabled
				final Intent poke = new Intent();
				poke.setClassName("com.android.settings",
						"com.android.settings.widget.SettingsAppWidgetProvider");
				poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
				poke.setData(Uri.parse("3"));
				sendBroadcast(poke);
				Log.v(TAG, "Gps Enabled From Settings.");
			}
		} catch (Exception ex) {
			Log.e(TAG, "Error Gps enabled From Settings." + ex.getMessage());
		}

	}

	private void turnGPSOff() {
		try {
			String provider = Settings.Secure.getString(getContentResolver(),
					Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

			if (provider.contains("gps")) { // if gps is enabled
				final Intent poke = new Intent();
				poke.setClassName("com.android.settings",
						"com.android.settings.widget.SettingsAppWidgetProvider");
				poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
				poke.setData(Uri.parse("3"));
				sendBroadcast(poke);
				Log.v(TAG, "Gps Disabled From Settings.");
			}
		} catch (Exception ex) {
			Log.e(TAG, "Error Gps Disabled From Settings." + ex.getMessage());
		}
	}

}