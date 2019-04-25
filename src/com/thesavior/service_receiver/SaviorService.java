package com.thesavior.service_receiver;

import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.thesavior.activities.EmergencyActivity;
import com.thesavior.activities.R;
import com.thesavior.utilities.SharedPreference;

public class SaviorService extends Service {

	private static final String LOGGING_TAG = "The Saviour Service";
	private static final String LOGGING_TAG_SENSORS = "The Saviour Sensor Service";
	private static PowerManager.WakeLock samplingWakeLock;
	private static PowerManager mPwrMgr;
	private SensorEventListener sensorEventListener;
	private SensorManager mSensorEventManager;
	private Context mContext;
	private double totalAcceleration;
	private boolean min = false;
	private boolean max = false;
	private float currentAcceleration = 0;
	private float maxAcceleration = 0;
	private int i;
	private double calibration = SensorManager.STANDARD_GRAVITY;

	@Override
	public IBinder onBind(Intent intent) {
		// We don't need a IBinder interface.
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		Log.d(LOGGING_TAG, "service has been created.");
		mContext = getApplicationContext();
		/**
		 * registering sensors
		 */
		if (SharedPreference.getBoolean(SaviorService.this, "FALL_DETECTION"))
			registeringSensors();

	}

	private void registeringSensors() {

		Log.d(LOGGING_TAG_SENSORS, "sensor has been registered");

		// Obtain a reference to system-wide sensor event manager.
		mSensorEventManager = (SensorManager) mContext
				.getSystemService(Context.SENSOR_SERVICE);

		// Get the default PowerManager for handling sleep off (stand by
		// mode)device
		mPwrMgr = (PowerManager) getSystemService(Context.POWER_SERVICE);

		// Handling Accelerometer sensor event listener
		sensorEventListener = new SensorEventListener() {

			public void onSensorChanged(SensorEvent event) {
				getDetection(event);
			}

			public void onAccuracyChanged(Sensor sensor, int accuracy) {

			}
		};
		// Register for events.
		mSensorEventManager
				.registerListener(sensorEventListener, mSensorEventManager
						.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
						SensorManager.SENSOR_DELAY_NORMAL);

		/**
		 * Register our receiver for the ACTION_SCREEN_OFF action. This will
		 * make our receiver code be called whenever the phone enters standby
		 * mode.
		 **/
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		registerReceiver(mReceiver, filter);
	}

	/**
	 * BroadcastReceiver for handling ACTION_SCREEN_OFF action. This will make
	 * our receiver code be called whenever the phone enters standby mode.
	 **/
	public BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			// Check action just to be on the safe side.
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {

				Log.d(LOGGING_TAG_SENSORS,
						"ACTION_SCREEN_OFF action has been triggered.");

				// Turn the screen back on again, from the main thread
				Handler handler = new Handler();
				handler.post(new Runnable() {
					@SuppressLint("Wakelock")
					@SuppressWarnings("deprecation")
					public void run() {
						if (samplingWakeLock != null)
							samplingWakeLock.release();

						samplingWakeLock = mPwrMgr.newWakeLock(
								PowerManager.SCREEN_DIM_WAKE_LOCK
										| PowerManager.ACQUIRE_CAUSES_WAKEUP,
								"SensorMonitor");
						samplingWakeLock.acquire();
					}
				});
			}
		}
	};

	private void getDetection(SensorEvent currentEvent) {

		if (currentEvent.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
			return;
		int sensor = currentEvent.sensor.getType();
		switch (sensor) {
		case Sensor.TYPE_ACCELEROMETER:
			double xAxis_lateralA = currentEvent.values[0];
			double yAxis_longitudinalA = currentEvent.values[1];
			double zAxis_verticalA = currentEvent.values[2];
			totalAcceleration = Math.round(Math.sqrt(Math
					.pow(xAxis_lateralA, 2)
					+ Math.pow(yAxis_longitudinalA, 2)
					+ Math.pow(zAxis_verticalA, 2)));
			Log.v(LOGGING_TAG_SENSORS, "Time called : "
					+ Calendar.getInstance().getTime());
			Log.v(LOGGING_TAG_SENSORS, "acceleration value before calculate: "
					+ totalAcceleration);

			currentAcceleration = Math
					.abs((float) (totalAcceleration - calibration));
			Log.v(LOGGING_TAG_SENSORS, "CURRENT acceleration calculate: "
					+ currentAcceleration);
			if (currentAcceleration > maxAcceleration) {
				maxAcceleration = currentAcceleration;
				Log.v(LOGGING_TAG_SENSORS, "Maximum acceleration : "
						+ maxAcceleration);
			}
			if (totalAcceleration <= 6.0) {
				min = true;
				Log.v(LOGGING_TAG, "+++ checked condition less 6.0 +++");
				Log.v(LOGGING_TAG, "min value : " + min);
				Log.v(LOGGING_TAG, "i value : " + i);
			}

			if (min == true) {
				i++;
				Log.v(LOGGING_TAG, "min value : " + min);
				Log.v(LOGGING_TAG, "i value : " + i);
				if (totalAcceleration >= 27) {// 13.5
					max = true;
					Log.v(LOGGING_TAG,
							"+++ checked condition greater than 27 +++");
					Log.v(LOGGING_TAG, "max value : " + max);
					Log.v(LOGGING_TAG, "i value : " + i);
				}

			}

			if (min == true && max == true) {
				if (SharedPreference.getBoolean(SaviorService.this,
						"FALL_DETECTION")) {
					Toast.makeText(SaviorService.this, "FALL DETECTED!!!!!",
							Toast.LENGTH_SHORT).show();
					buildNotification();
				}
				Log.d(LOGGING_TAG, "++++ The fall is detected.+++++");
				Log.i(LOGGING_TAG, "min value : " + min);
				Log.i(LOGGING_TAG, "max value : " + max);
				i = 0;
				min = false;
				max = false;
			}

			if (i > 4) {
				Log.v(LOGGING_TAG, "+++ checked i value > than 4 +++");
				Log.v(LOGGING_TAG, "i value : " + i);
				i = 0;
				min = false;
				max = false;
			}
			break;

		}
		Log.v("sensor", "sensor change is verifying");
	}

	private void buildNotification() {
		Intent intent = new Intent(SaviorService.this, EmergencyActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("SCREEN_LOCK", false);
		PendingIntent pIntent = PendingIntent.getActivity(SaviorService.this,
				1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		// Declare notification manager
		Bitmap bmp = BitmapFactory.decodeResource(getResources(),
				android.R.drawable.ic_secure);
		Bitmap resizedbitmap = Bitmap.createScaledBitmap(bmp, bmp.getWidth(),
				bmp.getHeight(), true);
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				SaviorService.this);
		builder.addAction(R.drawable.icon_the_savior_launcher,
				"Fall detecting.", pIntent)
				.setSmallIcon(R.drawable.icon_the_savior_launcher)
				.setLargeIcon(resizedbitmap).setContentTitle("Fall detecting.")
				.setContentText("Fall has been detected.")
				.setWhen(System.currentTimeMillis()).setTicker("The Saviour.")
				.setLights(0xFFFF0000, 500, 500) // setLights (int argb, int
													// onMs, int offMs)
				.setContentIntent(pIntent).setAutoCancel(true);

		notificationManager.notify(R.drawable.icon_the_savior_launcher,
				builder.build());
		callEmergency();

	}

	public void callEmergency() {
		Intent intent = new Intent(SaviorService.this, EmergencyActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("SCREEN_LOCK", true);
		startActivity(intent);
	}

	private void stopSensor() {

		if (mSensorEventManager != null)
			mSensorEventManager.unregisterListener(sensorEventListener);
		/**
		 * Unregister ACTION SCREEN OFF receiver.
		 */
		if (mReceiver != null)
			unregisterReceiver(mReceiver);

		/**
		 * Released PowerWake Lock for normally stand by mode.
		 */
		if (samplingWakeLock != null)
			samplingWakeLock.release();
	}

	@Override
	public void onDestroy() {

		/**
		 * Unregister Sensor Manager listener
		 */
		stopSensor();
		Log.d(LOGGING_TAG, "Savior Sensor service has been stopped.");
	}
}