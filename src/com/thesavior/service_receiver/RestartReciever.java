package com.thesavior.service_receiver;

import com.thesavior.utilities.SharedPreference;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RestartReciever extends BroadcastReceiver {

	static final String LOGGING_TAG = "RestartReciever";

	@Override 
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.v(LOGGING_TAG, "++++++++ RestartReciever ++++++++++ ");
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			if (SharedPreference.getBoolean(context, "FALL_DETECTION")) {
				ComponentName comp = new ComponentName(
						context.getPackageName(),
						com.thesavior.service_receiver.SaviorService.class
								.getName());
				ComponentName service = context.startService(new Intent()
						.setComponent(comp));
				Log.d(LOGGING_TAG, "+++++ start service +++++" + comp.toString());

				if (null == service) {
					Log.e(LOGGING_TAG,  
							"Could not start service " + comp.toString());
				}
			}

		} else {
			Log.e(LOGGING_TAG,
					"Received unexpected intent " + intent.toString());
		}
	}
}
