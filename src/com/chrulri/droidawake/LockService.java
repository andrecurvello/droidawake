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

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.Toast;

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
        onStart(intent, startId);
        return START_STICKY;
    }

    // used for backwards compatibility
    @Override
    public void onStart(Intent intent, int startId) {
        Log.debug(TAG, "onStart");

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(WAKELOCK_FLAGS, getPackageName());
        wakeLock.acquire();

        if (screenReceiver == null) {
            screenReceiver = new ScreenOnOffRecevier();
        }
        registerReceiver(screenReceiver, new IntentFilter(ACTION_SCREEN_OFF));

        showToast(R.string.wakelock_on);

        Utils.updateWidgets(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // not needed
    }

    @Override
    public void onDestroy() {
        Log.debug(TAG, "onDestroy");

        unregisterReceiver(screenReceiver);

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();

            showToast(R.string.wakelock_off);
        }
        wakeLock = null;

        Utils.updateWidgets(this);
    }

    private void showToast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
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
