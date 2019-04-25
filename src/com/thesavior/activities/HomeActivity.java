package com.thesavior.activities;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.mycomp.mamgoogleta.MyValidation;
import com.thesavior.service_receiver.SaviorService;
import com.thesavior.utilities.Information;
import com.thesavior.utilities.SharedPreference;

@SuppressLint("SimpleDateFormat")
public class HomeActivity extends FragmentActivity {

	private static final String TAG = "HOME ACTIVITY";
	private Button btn_emergency_push, btn_setup;
	private Button btn_setting, btn_info;
	private Dialog myDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFormat(PixelFormat.RGBA_8888);

			setContentView(R.layout.activity_home);
	
		Typeface FONT_TYPE = Typeface.createFromAsset(this.getAssets(),
				"fonts/army_wide.ttf");
		Typeface FONT_TYPE2 = Typeface.createFromAsset(this.getAssets(),
				"fonts/arista2.0.ttf");
		TextView mtextView_header1 = (TextView) findViewById(R.id.textView_splashHeader1);
		mtextView_header1.setTypeface(FONT_TYPE);
		TextView mtextView_header2 = (TextView) findViewById(R.id.textView_splashHeader2);
		mtextView_header2.setTypeface(FONT_TYPE);
		TextView mtextView_header3 = (TextView) findViewById(R.id.textView_splashHeader3);
		mtextView_header3.setTypeface(FONT_TYPE2);
		btn_emergency_push = (Button) findViewById(R.id.btn_emergency_push);
		btn_emergency_push.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(HomeActivity.this,
						EmergencyActivity.class).putExtra("SCREEN_LOCK", false));
				HomeActivity.this.finish();
			}

		});
		btn_setup = (Button) findViewById(R.id.btn_home_setup);
		btn_setup.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(HomeActivity.this, SetupActivity.class));
				HomeActivity.this.finish();
			}
		});
		btn_setting = (Button) findViewById(R.id.btn_home_setting);
		btn_setting.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(HomeActivity.this,
						SettingActivity.class));
				HomeActivity.this.finish();
			}
		});

		btn_info = (Button) findViewById(R.id.btn_home_info);
		btn_info.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				CustomDialogClass customDialog = new CustomDialogClass(
						HomeActivity.this, false, getResources().getString(
								R.string.About_us), getResources().getString(
								R.string.app_name_inSmall));
				customDialog.show();
			}
		});

	
			if (!SharedPreference.getBoolean(HomeActivity.this,
					"PASSCODE_DIALOG"))
				displayPasscodeDialog();

		
		startService();
	}

	public void startService() {
		if (SharedPreference.getBoolean(HomeActivity.this, "FALL_DETECTION")) {
			startService(new Intent(HomeActivity.this, SaviorService.class));
		} else {
			stopService(new Intent(HomeActivity.this, SaviorService.class));
		}
	}

	public void displayPasscodeDialog() {
		myDialog = new Dialog(HomeActivity.this);
		myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		myDialog.setContentView(R.layout.passcode_dialog);
		myDialog.setCancelable(false);
		InputFilter[] FilterArray = new InputFilter[1];
		FilterArray[0] = new InputFilter.LengthFilter(4);

		final EditText passcode = (EditText) myDialog
				.findViewById(R.id.editText_Passcode);
		passcode.setFilters(FilterArray);
		passcode.setInputType(InputType.TYPE_CLASS_NUMBER
				| InputType.TYPE_CLASS_PHONE);
		passcode.setTransformationMethod(PasswordTransformationMethod
				.getInstance());
		passcode.setImeOptions(EditorInfo.IME_ACTION_NEXT);
		final EditText confirmPwd = (EditText) myDialog
				.findViewById(R.id.editText_ConfirmPasscode);
		confirmPwd.setFilters(FilterArray);
		confirmPwd.setInputType(InputType.TYPE_CLASS_NUMBER
				| InputType.TYPE_CLASS_PHONE);
		confirmPwd.setTransformationMethod(PasswordTransformationMethod
				.getInstance());
		confirmPwd.setImeOptions(EditorInfo.IME_ACTION_NEXT);
		final EditText emailId = (EditText) myDialog
				.findViewById(R.id.editText_retrieveEmailId);
		emailId.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		emailId.setImeOptions(EditorInfo.IME_ACTION_DONE);
		passcode.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_NEXT) {
					// Handle IME NEXT key
					if (TextUtils.isEmpty(passcode.getText().toString())) {
						passcode.setError("This field cannot be blank!");
						return true;
					} else if (passcode.getText().length() != 4
							&& passcode.getText().length() > 0) {
						passcode.setError("Oops! Passcode should in 4 digit.");
						return true;
					}
					confirmPwd.requestFocus();
				}
				return false;
			}
		});
		confirmPwd.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				// TODO Auto-generated method stub
				if (actionId == EditorInfo.IME_ACTION_NEXT) {
					if (TextUtils.isEmpty(confirmPwd.getText().toString())) {
						confirmPwd.setError("This field cannot be blank!");
						return true;
					} else if (!TextUtils.equals(confirmPwd.getText()
							.toString(), passcode.getText().toString())) {
						confirmPwd
								.setError("Oops! The Passcode you entered not Match.");
						return true;
					}
				}
				return false;
			}
		});
		Button delete_ok = (Button) myDialog.findViewById(R.id.btn_delete_OK);
		delete_ok.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if ((passcode.getText().length() != 0)
						|| (confirmPwd.getText().length() != 0)
						|| (emailId.getText().length() != 0)) {
					if (TextUtils.isEmpty(passcode.getText().toString())) {
						passcode.setError("This field cannot be blank!");
					} else if (passcode.getText().length() != 4
							&& passcode.getText().length() > 0) {
						passcode.setError("Oops! Passcode should in 4 digit.");
					} else if (TextUtils.isEmpty(confirmPwd.getText()
							.toString())) {
						confirmPwd.setError("This field cannot be blank!");
					} else if (!confirmPwd.getText().toString()
							.equalsIgnoreCase(passcode.getText().toString())) {
						confirmPwd
								.setError("Oops! The Passcode you entered not Match.");
					} else if (TextUtils.isEmpty(emailId.getText().toString())) {
						emailId.setError("Please enter your Email id!");
					} else if (!Information.isEmailValid(emailId.getText()
							.toString())) {
						emailId.setError("Please enter valid email id!");
					} else {

						SharedPreference.putInt(HomeActivity.this,
								"TRACK_TIME", 0);
						SharedPreference.putInt(HomeActivity.this,
								"ALARM_DELAY_TIME", 5);
						SharedPreference.putBoolean(HomeActivity.this, "SOUND",
								true);
						// SharedPreference.putBoolean(HomeActivity.this,
						// Constants.IS_ALARM_START,
						// false);

						SharedPreference.putInt(
								HomeActivity.this,
								"INTENT_UPDATE_TIME",
								+getResources().getIntArray(
										R.array.Intent_callTiming)[0]);
						SharedPreference.putBoolean(HomeActivity.this,
								"PASSCODE_DIALOG", true);
						SharedPreference.putdata(HomeActivity.this, "PASSCODE",
								passcode.getText().toString());

						SharedPreference.putdata(HomeActivity.this,
								"RETIEVE_PASSCODE_EMAILID", emailId.getText()
										.toString());
						myDialog.dismiss();
						// addShortcut();
						checkConnectivityAndSendMail(
								HomeActivity.this,
								emailId.getText().toString(),
								Html.fromHtml(
										Information.EmailFormating(passcode
												.getText().toString()))
										.toString());
						startActivity(new Intent(HomeActivity.this,
								SetupActivity.class));
						HomeActivity.this.finish();
					}

				} else {
					CustomDialogClass customDialog = new CustomDialogClass(
							HomeActivity.this, false, getResources().getString(
									R.string.Message_fieldReq), getResources()
									.getString(R.string.app_name_inSmall));
					customDialog.show();
				}
			}
		});
		Button delete_cancel = (Button) myDialog
				.findViewById(R.id.btn_delete_cancel);
		delete_cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				myDialog.dismiss();
				System.exit(0);
			}
		});
		myDialog.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				// TODO Auto-generated method stub
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					myDialog.dismiss();
					System.exit(0);
				}
				return false;
			}
		});
		myDialog.show();
	}

	// private void addShortcut() {
	// // Adding shortcut for MainActivity
	// // on Home screen
	// Intent shortcutIntent = new Intent(getApplicationContext(),
	// HomeActivity.class);
	//
	// shortcutIntent.setAction(Intent.ACTION_MAIN);
	//
	// Intent addIntent = new Intent();
	// addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
	// addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "The Saviour");
	// addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
	// Intent.ShortcutIconResource.fromContext(
	// getApplicationContext(),
	// R.drawable.icon_the_saviour_launcher));
	// addIntent.putExtra("duplicate", false);
	// addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
	// getApplicationContext().sendBroadcast(addIntent);
	// }

	public void checkConnectivityAndSendMail(Activity actRef, String EmailId,
			String msgBody) {
		final ConnectivityManager conMgr = (ConnectivityManager) actRef
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
		if (activeNetwork != null
				&& activeNetwork.getState() == NetworkInfo.State.CONNECTED) {
			Log.d(TAG, "EMAIL ID : " + EmailId + "\nMessage Body : " + msgBody);
			EmergencyActivity emerAct = new EmergencyActivity();
			emerAct.new SendingMail(actRef, EmailId, msgBody, false).execute();

		} else {
			// Information.showConnectionDialogs(getParent(), getResources()
			// .getString(R.string.Error_internetNotAvailable));
			Log.d(TAG,
					actRef.getResources().getString(
							R.string.Error_internetNotAvailable));
			CustomDialogClass customDialog = new CustomDialogClass(actRef,
					false, actRef.getResources().getString(
							R.string.Error_internetNotAvailable), actRef
							.getResources()
							.getString(R.string.app_name_inSmall));
			customDialog.show();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// Information.showDialog(HomeActivity.this);
			CustomDialogClass customDialog = new CustomDialogClass(
					HomeActivity.this, true, getResources().getString(
							R.string.Message_Exit), getResources().getString(
							R.string.app_name_inSmall));
			customDialog.show();
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		HomeActivity.this.finish();
	}

}
