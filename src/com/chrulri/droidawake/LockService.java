/******************************************************************************
 *  DroidAwake, stay awake widget app for Android devices                     *
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

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;

public class LockService extends Service {
    private static final String TAG = LockService.class.getSimpleName();

    private static final int WAKELOCK_FLAGS = PowerManager.SCREEN_DIM_WAKE_LOCK;

    public static boolean isLocked(Context context) {
        return Utils.isServiceRunning(context, LockService.class);
    }

    private ScreenOnOffRecevier screenReceiver;
    private PowerManager.WakeLock wakeLock;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.debug(TAG, "onStart");

        if (wakeLock != null && wakeLock.isHeld()) {
            stopSelf();
        } else {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(WAKELOCK_FLAGS, getPackageName());
            wakeLock.acquire();

            startForeground();

            if (screenReceiver == null) {
                screenReceiver = new ScreenOnOffRecevier();
            }
            registerReceiver(screenReceiver, new IntentFilter(ACTION_SCREEN_OFF));

            Utils.updateWidgets(this);
        }

        return START_STICKY;
    }

    @SuppressWarnings("deprecation")
    private void startForeground() {
        PendingIntent emptyIntent = PendingIntent.getService(getApplicationContext(), 0,
                new Intent(this, LockService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification(R.drawable.ic_launcher,
                getText(R.string.wakelock_on), System.currentTimeMillis());
        notification.setLatestEventInfo(this, getText(R.string.app_name),
                getText(R.string.wakelock_on), emptyIntent);
        notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        startForeground(1, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // not needed
    }

    @Override
    public void onDestroy() {
        Log.debug(TAG, "onDestroy");

        stopForeground(true);

        unregisterReceiver(screenReceiver);

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        wakeLock = null;

        Utils.updateWidgets(this);
    }

    private class ScreenOnOffRecevier extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_SCREEN_OFF.equals(intent.getAction())) {
                Log.debug(TAG, "onReceive ACTION_SCREEN_OFF");
                stopSelf();
            }
        }
    }
}
