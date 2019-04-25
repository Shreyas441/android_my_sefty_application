package com.thesavior.activities;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.thesavior.activities.R;
import com.thesavior.activities.AlertDialogRadio.AlertPositiveListener;
import com.thesavior.service_receiver.LocationTrackReciever;
import com.thesavior.utilities.Information;
import com.thesavior.utilities.SharedPreference;

public class SettingActivity extends FragmentActivity implements
		OnCheckedChangeListener, OnClickListener, AlertPositiveListener {
	/** Called when the activity is first created. */
	private static final String TAG = "SETTINGS ACTIVTIY";
	private int countChangeLayoutValue = 1;
	private RelativeLayout rLayout_setting;
	private View include;
	private EditText mView_currentPasscode;
	private EditText mView_newPasscode;
	private EditText mView_confirmPasscode;
	private TextView heading;
	private Button btn_save_Password;
	private Button btn_back, btn_about;
	private Button btn_changePasscode;
	private Button btn_forgetPasscode;
	private Button btn_alarm;
	private Button btn_gps;
	private Button btn_police;
	private ToggleButton tb_msg, tb_contact, tb_gps;
	private ToggleButton tb_call_police, tb_alarm_sound, tb_fallDetection;
	private Animation Left_in_animation;
	private Animation Right_in_animation;
	private Animation Left_out_animation;
	private Animation Right_out_animation;
	private Dialog alarmDelayDialog;
	private boolean isCurrentPosition = true;
	private int position = 0;
	private int mySbValue = 0;
	private int MinVal = 5;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_setting);
		initViews();
		initAnimations();
		setViewsData();
		registerListeners();
	}

	private void initViews() {
		// declare all views id's
		heading = (TextView) findViewById(R.id.textView_heading);
		btn_back = (Button) findViewById(R.id.btn_back);
		btn_about = (Button) findViewById(R.id.btn_about_info);
		btn_changePasscode = (Button) findViewById(R.id.btn_changePasscode);
		btn_forgetPasscode = (Button) findViewById(R.id.btn_forgetPasscode);

		btn_gps = (Button) findViewById(R.id.btn_setgps);
		btn_alarm = (Button) findViewById(R.id.btn_alarm_sound);
		btn_police = (Button) findViewById(R.id.btn_setpolice);
		btn_police.requestFocus();
		// toggle button id's
		tb_msg = (ToggleButton) findViewById(R.id.tb_msg);
		tb_gps = (ToggleButton) findViewById(R.id.tb_gps);
		tb_contact = (ToggleButton) findViewById(R.id.tb_contact);
		tb_call_police = (ToggleButton) findViewById(R.id.tb_police);
		tb_alarm_sound = (ToggleButton) findViewById(R.id.tb_alarm_sound);
		tb_fallDetection = (ToggleButton) findViewById(R.id.tb_fallDetection);

		// find id's for layout passcode
		include = findViewById(R.id.include_rLayout_changePasscode);
		rLayout_setting = (RelativeLayout) findViewById(R.id.relativeLayout_setting);
		btn_save_Password = (Button) findViewById(R.id.btn_savePasscode);
		mView_currentPasscode = (EditText) findViewById(R.id.editText_CurrentPasscode);
		mView_newPasscode = (EditText) findViewById(R.id.editText_newPasscode);
		mView_confirmPasscode = (EditText) findViewById(R.id.editText_re_enterPasscode);

		// validation on inputs
		InputFilter[] FilterArray = new InputFilter[1];
		FilterArray[0] = new InputFilter.LengthFilter(4);
		// setting for current password edit text
		mView_currentPasscode.setFilters(FilterArray);
		mView_currentPasscode.setInputType(InputType.TYPE_CLASS_NUMBER
				| InputType.TYPE_CLASS_PHONE);
		mView_currentPasscode
				.setTransformationMethod(PasswordTransformationMethod
						.getInstance());
		mView_currentPasscode.setImeOptions(EditorInfo.IME_ACTION_NEXT);
		// setting for new password edit text
		mView_newPasscode.setFilters(FilterArray);
		mView_newPasscode.setInputType(InputType.TYPE_CLASS_NUMBER
				| InputType.TYPE_CLASS_PHONE);
		mView_newPasscode.setTransformationMethod(PasswordTransformationMethod
				.getInstance());
		mView_newPasscode.setImeOptions(EditorInfo.IME_ACTION_NEXT);
		// setting for confirm password edit text
		mView_confirmPasscode.setFilters(FilterArray);
		mView_confirmPasscode.setInputType(InputType.TYPE_CLASS_NUMBER
				| InputType.TYPE_CLASS_PHONE);
		mView_confirmPasscode
				.setTransformationMethod(PasswordTransformationMethod
						.getInstance());
		mView_confirmPasscode.setImeOptions(EditorInfo.IME_ACTION_DONE);

	}

	private void initAnimations() {
		Left_in_animation = AnimationUtils.loadAnimation(this,
				R.anim.slide_in_left);
		Right_in_animation = AnimationUtils.loadAnimation(this,
				R.anim.slide_in_right);
		Left_out_animation = AnimationUtils.loadAnimation(this,
				R.anim.slide_out_left);
		Right_out_animation = AnimationUtils.loadAnimation(this,
				R.anim.slide_out_right);
	}

	private void setViewsData() {

		heading.setText(getResources().getString(
				R.string.text_header_SettingPage));
		position = SharedPreference.getInt(SettingActivity.this, "TRACK_TIME");
		btn_gps.setText(getResources().getString(R.string.text_gpsTracking)
				+ "("
				+ getResources().getStringArray(R.array.trackingTiming)[position]
				+ ")");
		btn_alarm.setText(getResources().getString(R.string.text_sound)
				+ "(Trigger in "
				+ SharedPreference.getInt(SettingActivity.this,
						"ALARM_DELAY_TIME") + " seconds)");
		tb_msg.setChecked(SharedPreference.getBoolean(SettingActivity.this,
				"MESSAGE"));
		tb_gps.setChecked(SharedPreference.getBoolean(SettingActivity.this,
				"GPS"));
		tb_contact.setChecked(SharedPreference.getBoolean(SettingActivity.this,
				"CONTACT"));
		tb_call_police.setChecked(SharedPreference.getBoolean(
				SettingActivity.this, "CALL_POLICE"));
		tb_alarm_sound.setChecked(SharedPreference.getBoolean(
				SettingActivity.this, "SOUND"));
		tb_fallDetection.setChecked(SharedPreference.getBoolean(
				SettingActivity.this, "FALL_DETECTION"));

	}

	private void registerListeners() {
		// buttons listeners
		btn_gps.setOnClickListener(this);
		btn_alarm.setOnClickListener(this);
		btn_back.setOnClickListener(this);
		btn_about.setOnClickListener(this);
		btn_changePasscode.setOnClickListener(this);
		btn_forgetPasscode.setOnClickListener(this);
		// toggle buttons listeners
		tb_msg.setOnCheckedChangeListener(this);
		tb_gps.setOnCheckedChangeListener(this);
		tb_contact.setOnCheckedChangeListener(this);
		tb_call_police.setOnCheckedChangeListener(this);
		tb_alarm_sound.setOnCheckedChangeListener(this);
		tb_fallDetection.setOnCheckedChangeListener(this);

		// EditorActionListener for change passcode layout
		mView_currentPasscode
				.setOnEditorActionListener(new OnEditorActionListener() {
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						// TODO Auto-generated method stub
						if (actionId == EditorInfo.IME_ACTION_NEXT) {
							// Handle IME NEXT key
							if (mView_currentPasscode.getText().length() == 0) {
								mView_currentPasscode
										.setError("This field cannot be blank!");
								return true;
							} else if (!mView_currentPasscode
									.getText()
									.toString()
									.equalsIgnoreCase(
											SharedPreference.getdata(
													SettingActivity.this,
													"PASSCODE"))) {
								mView_currentPasscode
										.setError("Oops! The Passcode you entered is incorrect.");
								return true;
							}
						}
						return false;
					}
				});
		mView_newPasscode
				.setOnEditorActionListener(new OnEditorActionListener() {
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						// TODO Auto-generated method stub
						if (actionId == EditorInfo.IME_ACTION_NEXT) {
							if (mView_newPasscode.getText().length() == 0) {
								mView_newPasscode
										.setError("This field cannot be blank");
								return true;
							} else if ((mView_newPasscode.getText().length() != 4 && mView_newPasscode
									.getText().length() > 0)) {
								mView_newPasscode
										.setError("Oops! Passcode should in 4 digit.");
								return true;
							}
						}
						return false;
					}
				});
		mView_confirmPasscode
				.setOnEditorActionListener(new OnEditorActionListener() {
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						// TODO Auto-generated method stub
						if (actionId == EditorInfo.IME_ACTION_DONE) {
							// Handle IME NEXT key
							if (mView_confirmPasscode.getText().length() == 0) {
								mView_confirmPasscode
										.setError("This field cannot be blank!");
								return true;
							} else if (!mView_confirmPasscode
									.getText()
									.toString()
									.equalsIgnoreCase(
											mView_newPasscode.getText()
													.toString())) {
								mView_confirmPasscode
										.setError("Oops! The Passcode you entered not Match.");
								return true;
							}
						}
						return false;
					}
				});
		btn_save_Password.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mView_currentPasscode.getText().length() == 0) {
					mView_currentPasscode
							.setError("This field cannot be blank!");
				} else if (mView_newPasscode.getText().length() == 0) {
					mView_newPasscode.setError("This field cannot be blank!");
				} else if (mView_confirmPasscode.getText().length() == 0) {
					mView_confirmPasscode
							.setError("This field cannot be blank!");
				} else if (!mView_currentPasscode
						.getText()
						.toString()
						.equals(SharedPreference.getdata(SettingActivity.this,
								"PASSCODE"))) {
					mView_currentPasscode
							.setError("Oops! The current passcode you entered is incorrect.");
				} else if ((mView_newPasscode.getText().length() != 4 && mView_newPasscode
						.getText().length() > 0)) {
					mView_newPasscode
							.setError("Oops! Passcode should be in 4 digit.");
				} else if (!mView_confirmPasscode.getText().toString()
						.equals(mView_newPasscode.getText().toString())) {
					mView_confirmPasscode
							.setError("Oops! The Passcode you entered not Match.");
				} else {
					SharedPreference.putdata(SettingActivity.this, "PASSCODE",
							mView_newPasscode.getText().toString());
					showSettingLayout();
					HomeActivity homeActObj = new HomeActivity();
					homeActObj.checkConnectivityAndSendMail(
							SettingActivity.this,
							SharedPreference.getdata(SettingActivity.this,
									"RETIEVE_PASSCODE_EMAILID"),
							Html.fromHtml(
									Information.EmailFormating(
											SharedPreference.getdata(SettingActivity.this,
													"PASSCODE")))
									.toString());
				}
			}
		});
	}

	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		switch (buttonView.getId()) {
		case R.id.tb_msg:
			SharedPreference.putBoolean(SettingActivity.this, "MESSAGE",
					tb_msg.isChecked());
			break;
		case R.id.tb_gps:
			SharedPreference.putBoolean(SettingActivity.this, "GPS",
					tb_gps.isChecked());
			if (tb_gps.isChecked()) {
				startTracking();
				Toast.makeText(SettingActivity.this,
						getResources().getString(R.string.text_ToastMsg),
						Toast.LENGTH_LONG).show();
			} else
				stopTracking();
			break;
		case R.id.tb_contact:
			SharedPreference.putBoolean(SettingActivity.this, "CONTACT",
					tb_contact.isChecked());
			break;
		case R.id.tb_police:
			SharedPreference.putBoolean(SettingActivity.this, "CALL_POLICE",
					tb_call_police.isChecked());
			break;
		case R.id.tb_alarm_sound:
			SharedPreference.putBoolean(SettingActivity.this, "SOUND",
					tb_alarm_sound.isChecked());
			break;
		case R.id.tb_fallDetection:
			SharedPreference.putBoolean(SettingActivity.this, "FALL_DETECTION",
					tb_fallDetection.isChecked());
			if (tb_fallDetection.isChecked()) {
				Toast.makeText(
						SettingActivity.this,
						getResources().getString(
								R.string.text_fallDetection_msg),
						Toast.LENGTH_LONG).show();
			}
			break;
		}
	}

	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_setgps:
			showTrackingLimitDialog();
			break;
		case R.id.btn_alarm_sound:
			showAlarmDelayDialog();
			break;
		case R.id.btn_back:
			callBack();
			break;
		case R.id.btn_about_info:
			CustomDialogClass customDialogForAbout = new CustomDialogClass(
					SettingActivity.this, false, getResources().getString(
							R.string.Desc_settings_page), getResources()
							.getString(R.string.app_name_inSmall));
			customDialogForAbout.show();
			break;
		case R.id.btn_changePasscode:
			showChangePasscodeLayout();
			break;
		case R.id.btn_forgetPasscode:
			CustomDialogClass customDialogForgetPwd = new CustomDialogClass(
					SettingActivity.this, true, getResources().getString(
							R.string.dialog_forgetText), getResources()
							.getString(R.string.app_name_inSmall));
			customDialogForgetPwd.show();
			break;

		}
	}

	private void showSettingLayout() {
		heading.setText(getResources().getString(
				R.string.text_header_SettingPage));
		rLayout_setting.startAnimation(Right_in_animation);
		rLayout_setting.setVisibility(View.VISIBLE);
		include.startAnimation(Right_out_animation);
		include.setVisibility(View.GONE);
		countChangeLayoutValue = 1;
		mView_currentPasscode.setText("");
		mView_newPasscode.setText("");
		mView_confirmPasscode.setText("");
	}

	private void showChangePasscodeLayout() {
		heading.setText(getResources().getString(
				R.string.text_header_changePasscodePage));
		rLayout_setting.startAnimation(Left_out_animation);
		rLayout_setting.setVisibility(View.GONE);
		include.startAnimation(Left_in_animation);
		include.setVisibility(View.VISIBLE);
		countChangeLayoutValue = 2;
	}

	private void showAlarmDelayDialog() {

		alarmDelayDialog = new Dialog(SettingActivity.this);
		alarmDelayDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		alarmDelayDialog.setContentView(R.layout.alarm_delay_layout);
		alarmDelayDialog.setCancelable(false);
		final TextView txt_showSeconds = (TextView) alarmDelayDialog
				.findViewById(R.id.textView_seconds);

		SeekBar seekBar_Seconds = (SeekBar) alarmDelayDialog
				.findViewById(R.id.seekBar_seconds);
		seekBar_Seconds.setMax(10);
		mySbValue = SharedPreference.getInt(SettingActivity.this,
				"ALARM_DELAY_TIME");
		txt_showSeconds.setText(mySbValue + " seconds");
		mySbValue = mySbValue - MinVal;
		seekBar_Seconds.setProgress(mySbValue);
		seekBar_Seconds
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						Log.v(TAG, "end touch listener");
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						Log.v(TAG, "start touch listener");
					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						Log.d(TAG, "call listener :" + progress);
						if (progress == 0) {
							mySbValue = MinVal;
							txt_showSeconds.setText(mySbValue + " seconds");
							if (isCurrentPosition) {
								seekBar.incrementProgressBy(1);
								isCurrentPosition = false;
							}
						} else {
							seekBar.setKeyProgressIncrement(2);
							mySbValue = MinVal + progress;
							txt_showSeconds.setText(mySbValue + " seconds");
						}
						SharedPreference.putInt(SettingActivity.this,
								"ALARM_DELAY_TIME", mySbValue);
						Log.d(TAG,
								"ALARM_DELAY_TIME : "
										+ SharedPreference.getInt(
												SettingActivity.this,
												"ALARM_DELAY_TIME"));
					}
				});
		Button delete_cancel = (Button) alarmDelayDialog
				.findViewById(R.id.button_alarmCancel);
		delete_cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				alarmDelayDialog.dismiss();
			}
		});
		alarmDelayDialog.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				// TODO Auto-generated method stub
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					alarmDelayDialog.dismiss();
				}
				return false;
			}
		});
		alarmDelayDialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				btn_alarm.setText(getResources().getString(R.string.text_sound)
						+ "(Trigger in "
						+ SharedPreference.getInt(SettingActivity.this,
								"ALARM_DELAY_TIME") + " seconds)");
				Log.d(TAG,
						"ALARM_DELAY_TIME : "
								+ SharedPreference.getInt(SettingActivity.this,
										"ALARM_DELAY_TIME"));
			}
		});
		alarmDelayDialog.show();
	}

	private void showTrackingLimitDialog() {

		/** Instantiating the DialogFragment class */
		AlertDialogRadio alert = new AlertDialogRadio();

		/** Creating a bundle object to store the selected item's index */
		Bundle b = new Bundle();

		/** Storing the selected item's index in the bundle object */
		b.putInt("position", position);

		/** Setting the bundle object to the dialog fragment object */
		alert.setArguments(b);

		/**
		 * Creating the dialog fragment object, which will in turn open the
		 * alert dialog window
		 */
		alert.show(getSupportFragmentManager(), "alert_dialog_radio");

	}

	@Override
	public void onPositiveClick(int position) {
		// TODO Auto-generated method stub
		this.position = position;
		Log.v(TAG,
				"SELECTED ITEM NO : "
						+ position
						+ "-"
						+ getResources().getStringArray(R.array.trackingTiming)[position]);
		btn_gps.setText(getResources().getString(R.string.text_gpsTracking)
				+ "("
				+ getResources().getStringArray(R.array.trackingTiming)[position]
				+ ")");
		SharedPreference.putInt(SettingActivity.this, "TRACK_TIME", position);
		SharedPreference
				.putInt(SettingActivity.this,
						"INTENT_UPDATE_TIME",
						+getResources().getIntArray(R.array.Intent_callTiming)[position]);
		Log.v(TAG,
				"INTENT_UPDATE_TIME : "
						+ SharedPreference.getInt(SettingActivity.this,
								"INTENT_UPDATE_TIME"));
		if (SharedPreference.getBoolean(SettingActivity.this, "GPS")) {
			startTracking();
			Toast.makeText(SettingActivity.this,
					getResources().getString(R.string.text_ToastMsg),
					Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * registering location tracker alarm Location tracking has been started.
	 * Its will send your location on your registered contacts number on a given
	 * time.
	 */

	private void startTracking() {
		Log.d(TAG, "Tracking Time called : "
				+ Calendar.getInstance().getTime().toString());
		Log.d(TAG,
				"INTENT_UPDATE_TIME : "
						+ SharedPreference.getInt(SettingActivity.this,
								"INTENT_UPDATE_TIME"));
		// SharedPreference.getInt(SettingActivity.this, "INTENT_UPDATE_TIME")
		int UPDATE_ALARM_IN_MINUTES = 1000 * 60 * SharedPreference.getInt(
				SettingActivity.this, "INTENT_UPDATE_TIME");
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		AlarmManager am_call_Tracking = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		am_call_Tracking.setRepeating(AlarmManager.RTC_WAKEUP,
				calendar.getTimeInMillis() + UPDATE_ALARM_IN_MINUTES,
				UPDATE_ALARM_IN_MINUTES, getPendingIntentForAlarm());

		Log.d(TAG,
				"alarm start "
						+ getResources().getStringArray(R.array.trackingTiming)[SharedPreference
								.getInt(SettingActivity.this, "TRACK_TIME")]);
	}

	private void stopTracking() {
		Log.d(TAG, "stop tracking ");
		AlarmManager am_call_Tracking = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		am_call_Tracking.cancel(getPendingIntentForAlarm());
	}

	private PendingIntent getPendingIntentForAlarm() {
		Intent intent = new Intent(SettingActivity.this,
				LocationTrackReciever.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		return PendingIntent.getBroadcast(this, 1, intent, 0);
	}

	private void callBack() {
		if (countChangeLayoutValue == 2) {
			showSettingLayout();
		} else if (countChangeLayoutValue == 1) {
			startActivity(new Intent(SettingActivity.this, HomeActivity.class));
			SettingActivity.this.finish();
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
		SettingActivity.this.finish();
	}

}