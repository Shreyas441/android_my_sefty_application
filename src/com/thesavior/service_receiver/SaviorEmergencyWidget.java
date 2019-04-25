package com.thesavior.service_receiver;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.thesavior.activities.R;

public class SaviorEmergencyWidget extends AppWidgetProvider {
	public static final String TAG = "WidgetProvider";
	private RemoteViews remoteViews;
	Context context;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v(TAG, "+++ call widget reciever automatically from service +++");
		remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.widget_layout);

		Intent i = new Intent(context,
				com.thesavior.activities.EmergencyActivity.class);
		i.putExtra("SCREEN_LOCK", false);
		PendingIntent pi = PendingIntent.getActivity(context, 0, i,
				PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.imageView_emergencyHelp, pi);
		ComponentName cn = new ComponentName(context,
				SaviorEmergencyWidget.class);
		AppWidgetManager.getInstance(context).updateAppWidget(cn, remoteViews);
	}

	@Override
	public void onUpdate(Context ctxt, AppWidgetManager mgr, int[] appWidgetIds) {
		super.onUpdate(ctxt, mgr, appWidgetIds);
		Log.v(TAG, "+++ call widget reciever automatically from service +++");
		// ComponentName me = new ComponentName(ctxt,
		// SaviourEmergencyWidget.class);
		// mgr.updateAppWidget(me, buildUpdate(ctxt, appWidgetIds));
	}

	// private RemoteViews buildUpdate(Context ctxt, int[] appWidgetIds) {
	// RemoteViews updateViews = new RemoteViews(ctxt.getPackageName(),
	// R.layout.widget_layout);
	// Intent i = new Intent(ctxt, EmergencyActivity.class);
	// PendingIntent pi = PendingIntent.getActivity(ctxt, 0, i,
	// PendingIntent.FLAG_UPDATE_CURRENT);
	// updateViews.setOnClickPendingIntent(R.id.imageView_emergencyHelp, pi);
	//
	// return (updateViews);
	// }
}
