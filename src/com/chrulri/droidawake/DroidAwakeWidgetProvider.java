/******************************************************************************
 *  DroidAwake, stay awake widget app for Android devices                    *
 *  Copyright (C) 2012  Christian Ulrich <chrulri@gmail.com>                  *
 *                                                                            *
 *  This program is free software: you can redistribute it and/or modify      *
 *  it under the terms of the GNU General Public License as published by      *
 *  the Free Software Foundation, either version 3 of the License, or         *
 *  (at your option) any later version.                                       *
 *                                                                            *
 *  This program is distributed in the hope that it will be useful,           *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of            *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             *
 *  GNU General Public License for more details.                              *
 *                                                                            *
 *  You should have received a copy of the GNU General Public License         *
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.     *
 ******************************************************************************/
package com.chrulri.droidawake;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class DroidAwakeWidgetProvider extends AppWidgetProvider {
	static final String TAG = DroidAwakeWidgetProvider.class.getName();

	static final String ACTION_TOGGLE = "com.chrulri.droidawake.ACTION_TOGGLE";
	static final int FLAGS = PowerManager.SCREEN_DIM_WAKE_LOCK
							| PowerManager.ACQUIRE_CAUSES_WAKEUP
							| PowerManager.ON_AFTER_RELEASE;
	static final int BUTTON_ON = R.drawable.bulb_on;
	static final int BUTTON_OFF = R.drawable.bulb_off;

	private static PowerManager.WakeLock wakeLock;

	@Override
	public void onEnabled(Context context) {
//		Log.d(TAG, "onEnabled");
		startUpdateService(context);
		super.onEnabled(context);
	}

	@Override
	public void onDisabled(Context context) {
//		Log.d(TAG, "onDisabled");
		if (wakeLock != null && wakeLock.isHeld()) {
			Log.d(TAG, "onDisabled: release WakeLock");
			wakeLock.release();
		}
		wakeLock = null;
		super.onDisabled(context);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
//		Log.d(TAG, "onReceive: " + intent);
		if (ACTION_TOGGLE.equals(intent.getAction())) {
//			Log.d(TAG, "onReceive ACTION_TOGGLE");
			if(wakeLock == null || !wakeLock.isHeld()) {
				PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
				wakeLock = pm.newWakeLock(FLAGS, context.getPackageName());
				wakeLock.acquire();
				showToast(context, R.string.wakelock_on);
			} else {
				if (wakeLock.isHeld()) {
					wakeLock.release();
					showToast(context, R.string.wakelock_off);
				}
				wakeLock = null;
			}
			startUpdateService(context);
		}
		super.onReceive(context, intent);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
//		Log.d(TAG, "onUpdate");
		startUpdateService(context);
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	private void showToast(Context context, int resId) {
		Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
	}

	private void startUpdateService(Context context) {
//		Log.d(TAG, "startUpdateService");
		context.startService(new Intent(context, UpdateService.class));
	}

	public static class UpdateService extends Service {

		@Override
		public void onStart(Intent intent, int startId) {
//			Log.d(TAG, "UpdateService.onStart");
			RemoteViews views = getUpdatedViews(this);
			// push update
			ComponentName thisWidget = new ComponentName(this, DroidAwakeWidgetProvider.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(this);
			manager.updateAppWidget(thisWidget, views);
		}

		private RemoteViews getUpdatedViews(Context context) {
            boolean hasLock = wakeLock != null && wakeLock.isHeld();
//          Log.d(TAG, "UpdateService.getUpdatedViews: wakeLock = " + wakeLock + " / hasLock = " + hasLock);
            // create an intent to broadcast the toggle action
			Intent intent = new Intent(context, DroidAwakeWidgetProvider.class);
			intent.setAction(ACTION_TOGGLE);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
			// get the layout, set drawable and attach the on-click listener
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
			views.setImageViewResource(R.id.button, hasLock ? BUTTON_ON : BUTTON_OFF);
			views.setOnClickPendingIntent(R.id.button, pendingIntent);
            return views;
        }

		@Override
		public IBinder onBind(Intent intent) {
			return null; // not needed
		}
	}
}
