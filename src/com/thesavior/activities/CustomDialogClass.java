package com.thesavior.activities;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.thesavior.activities.R;
import com.thesavior.utilities.Information;
import com.thesavior.utilities.SharedPreference;

public class CustomDialogClass extends Dialog implements
		android.view.View.OnClickListener {
	private static final String TAG = "CUSTOM DIALOG CLASS";
	private Activity activityRef;
	private Button yes, no;
	private TextView dialogMessage;
	private TextView dialogHeader;
	private boolean buttonShowStatus;
	private String message;
	private String title;

	public CustomDialogClass(Activity activity, boolean status, String message,
			String title) {
		super(activity);
		this.activityRef = activity;
		this.buttonShowStatus = status;
		this.message = message;
		this.title = title;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.custom_dialog_layout);

		dialogHeader = (TextView) findViewById(R.id.textView_dialog_Header);
		dialogMessage = (TextView) findViewById(R.id.textView_dialog_Message);
		yes = (Button) findViewById(R.id.btn_yes);
		no = (Button) findViewById(R.id.btn_no);
		initViews();
		yes.setOnClickListener(this);
		no.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_yes:

			if (TextUtils.equals(
					message,
					activityRef.getResources().getString(
							R.string.dialog_forgetText))) {
				Log.d(TAG,
						"Retrieve passcode email id : "
								+ SharedPreference.getdata(activityRef,
										"RETIEVE_PASSCODE_EMAILID"));
				HomeActivity homeActObj = new HomeActivity();
				homeActObj.checkConnectivityAndSendMail(
						activityRef,
						SharedPreference.getdata(activityRef,
								"RETIEVE_PASSCODE_EMAILID"),
						Html.fromHtml(
								Information.EmailFormating(SharedPreference
										.getdata(activityRef, "PASSCODE")))
								.toString());
				dismiss();
			} else if (TextUtils.equals(message, activityRef.getResources()
					.getString(R.string.Message_Exit))) {
				activityRef.finish();
			}

			break;
		case R.id.btn_no:
			dismiss();
			break;
		default:
			break;
		}
		dismiss();
	}

	private void initViews() {
		dialogHeader.setText(title);
		if (TextUtils.equals(message,
				activityRef.getResources().getString(R.string.About_us))
				|| TextUtils.equals(message, activityRef.getResources()
						.getString(R.string.Desc_setup_page))
				|| TextUtils.equals(message, activityRef.getResources()
						.getString(R.string.Desc_settings_page))) {
			dialogMessage.setLines(12);
			dialogMessage.setVerticalScrollBarEnabled(true);
			dialogMessage.setMovementMethod(new ScrollingMovementMethod());
		}
		dialogMessage.setText(Html.fromHtml(message));
		if (!buttonShowStatus) {
			yes.setVisibility(View.GONE);
			no.setText(android.R.string.ok);
			ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) no
					.getLayoutParams();
			layoutParams.setMargins(0, 0, 0, 0);
			no.setLayoutParams(layoutParams);

		} else {
			yes.setVisibility(View.VISIBLE);
		}
	}
}