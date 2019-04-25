package com.thesavior.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import com.thesavior.service_receiver.SaviorService;

public class Information {

	public static final String TAG = "INFORMATION";

	public static AlertDialog showDialog(final Activity activity) {
		AlertDialog.Builder builder = new Builder(activity);
		builder.setTitle("Exit");
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setMessage("Do you want to exit from The Saviour App?");
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				activity.finish();
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
		return dialog;
	}

	public static void showDialogs(final Activity activity, String message) {
		new AlertDialog.Builder(activity)
				.setTitle("THE SAVIOUR")
				.setMessage(message)
				.setNeutralButton("Close",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								System.exit(0);
							}
						}).show();
	}

	public static void showDialogs1(final Activity activity, String message) {
		new AlertDialog.Builder(activity).setTitle("THE SAVIOUR")
				.setIcon(android.R.drawable.ic_dialog_info).setMessage(message)
				.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
	}

	// public static void showConnectionDialogs(final Activity activity,
	// String message) {
	// new AlertDialog.Builder(activity).setTitle("THE SAVIOUR")
	// .setTitle(android.R.string.dialog_alert_title)
	// .setIcon(android.R.drawable.presence_offline)
	// .setMessage(message)
	// .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog, int which) {
	// dialog.dismiss();
	// }
	// }).show();
	// }

	// Method for Intent calling and switch one activity to other and finish 1st
	// activity
	public static void call_intent(Activity activity1, Class<?> activity2) {
		activity1.startActivity(new Intent(activity1, activity2));
		activity1.finish();
	}

	public static boolean isEmailValid(String email) {
		boolean isValid = false;
		String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
		CharSequence inputStr = email;
		Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(inputStr);
		if (matcher.matches()) {
			isValid = true;
		}
		return isValid;
	}

	public static boolean isMyServiceRunning(Activity activity) {
		ActivityManager manager = (ActivityManager) activity
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (SaviorService.class.getName().equals(
					service.service.getClassName())) {
				Log.v("Home Activity " + activity.getLocalClassName(),
						"Service Running ");
				return true;
			}
		}
		Log.v("Home Activity", "Service Not Running ");
		return false;
	}

	public static String postData(String tomailId, String body) {
		List<BasicNameValuePair> nameValuePairs;
		String result = null;
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(Constants.URL_FOR_MAIL); // http:maximess.com/mail/verify.php
		try {
			// pass parameters to server in pair of array list
			nameValuePairs = new ArrayList<BasicNameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("to", tomailId));
			nameValuePairs.add(new BasicNameValuePair("msg", body));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
					HTTP.UTF_8));
			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			if (response.getStatusLine().getStatusCode() == 200) {
				result = EntityUtils.toString((response.getEntity()));
				Log.v(TAG, "response code : "
						+ response.getStatusLine().getStatusCode());
				return result.toString();
			} else {
				Log.v(TAG, "response code : "
						+ response.getStatusLine().getStatusCode());
				return null;
			}
		} catch (Exception ex) {
			Log.v(TAG, "Error sending mail : " + ex.getMessage());
			ex.printStackTrace();
			return null;
		}
	}

	public static String EmailFormating(String password) {
		String body = "<![CDATA[ <HTML>"
				+ "<BODY>Dear User,<br><br>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Your The Saviour Passcode is: "
				+ "<b><font color=\"red\">"
				+ password
				+ "</font></b>"
				+ ". Please note the same as this will be required to stop the alarm."
				+ "<br>You can Re-set this passcode from your application at any time on forget passcode button in settings page. "
				+ "<br><br>If you have any suggestions kindly email us "
				+ "at contact@maximess.com or visit www.maximess.com.<br><br>"
				+ "Thanks, <br>Team MAXIMESS.<br>+++++++++++++++++++++++++++++++<br>"
				+ "</BODY></HTML>]]>";
		return body;

	}
}