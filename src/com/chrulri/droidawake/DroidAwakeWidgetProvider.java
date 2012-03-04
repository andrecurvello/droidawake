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

import static android.content.Intent.ACTION_SCREEN_OFF;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.RemoteViews;
import android.widget.Toast;

public class DroidAwakeWidgetProvider extends AppWidgetProvider {
  static final String TAG = DroidAwakeWidgetProvider.class.getName();

  static final String ACTION_TOGGLE = "com.chrulri.droidawake.ACTION_TOGGLE";
  static final int FLAGS = PowerManager.SCREEN_DIM_WAKE_LOCK;
  static final int BUTTON_ON = R.drawable.bulb_on;
  static final int BUTTON_OFF = R.drawable.bulb_off;

  private static PowerManager.WakeLock wakeLock;

  private static boolean isLocked() {
    return wakeLock != null && wakeLock.isHeld();
  }

  private static void lock(Context context, boolean showToast) {
    PowerManager pm = (PowerManager) context
        .getSystemService(Context.POWER_SERVICE);
    wakeLock = pm.newWakeLock(FLAGS, context.getPackageName());
    wakeLock.acquire();
    if (showToast) {
      showToast(context, R.string.wakelock_on);
    }
  }

  private static void unlock(Context context, boolean showToast) {
    if (wakeLock != null && wakeLock.isHeld()) {
      wakeLock.release();
      if (showToast) {
        showToast(context, R.string.wakelock_off);
      }
    }
    wakeLock = null;
  }

  private static void showToast(Context context, int resId) {
    Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
  }

  private static void startUpdateService(Context context) {
    Log.debug(TAG, "startUpdateService");
    context.startService(new Intent(context, UpdateService.class));
  }

  @Override
  public void onEnabled(Context context) {
    Log.debug(TAG, "onEnabled");
    startUpdateService(context);
  }

  @Override
  public void onDisabled(Context context) {
    Log.debug(TAG, "onDisabled");
    unlock(context, false);
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.debug(TAG, "onReceive: " + intent);
    if (ACTION_TOGGLE.equals(intent.getAction())) {
      Log.debug(TAG, "onReceive ACTION_TOGGLE");
      if (isLocked()) {
        unlock(context, true);
      } else {
        lock(context, true);
      }
      startUpdateService(context);
    }
    super.onReceive(context, intent);
  }

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager,
      int[] appWidgetIds) {
    Log.debug(TAG, "onUpdate");
    startUpdateService(context);
  }

  public static class UpdateService extends Service {

    @Override
    public void onDestroy() {
      Log.debug(TAG, "UpdateService.onDestroy");
    }

    @Override
    public void onStart(Intent intent, int startId) {
      Log.debug(TAG, "UpdateService.onStart");
      
      updateWidgets();

      if (isLocked()) {
        registerReceiver(new ScreenOnOffRecevier(), new IntentFilter(
            ACTION_SCREEN_OFF));
      } else {
        // stop service, it's not needed anymore
        stopSelf();
      }
    }

    private void updateWidgets() {
      RemoteViews views = getUpdatedViews(this);
      // push update
      ComponentName thisWidget = new ComponentName(this,
          DroidAwakeWidgetProvider.class);
      AppWidgetManager manager = AppWidgetManager.getInstance(this);
      manager.updateAppWidget(thisWidget, views);
    }

    private RemoteViews getUpdatedViews(Context context) {
      boolean hasLock = isLocked();
      Log.debug(
          TAG,
          "UpdateService.getUpdatedViews: wakeLock = " + wakeLock + " / hasLock = " + hasLock);
      // create an intent to broadcast the toggle action
      Intent intent = new Intent(context, DroidAwakeWidgetProvider.class);
      intent.setAction(ACTION_TOGGLE);
      PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
          intent, 0);
      // get the layout, set drawable and attach the on-click listener
      RemoteViews views = new RemoteViews(context.getPackageName(),
          R.layout.widget);
      views.setImageViewResource(R.id.button, hasLock ? BUTTON_ON : BUTTON_OFF);
      views.setOnClickPendingIntent(R.id.button, pendingIntent);
      return views;
    }

    @Override
    public IBinder onBind(Intent intent) {
      return null; // not needed
    }

    private class ScreenOnOffRecevier extends BroadcastReceiver {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (ACTION_SCREEN_OFF.equals(intent.getAction())) {
          Log.debug(TAG, "onReceive ACTION_SCREEN_OFF");
          unregisterReceiver(this);
          unlock(context, false);
          updateWidgets();
          stopSelf();
        }
      }
    }
  }
}
