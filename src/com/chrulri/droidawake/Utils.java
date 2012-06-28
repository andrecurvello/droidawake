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

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

public final class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    private Utils() {
    }

    public static boolean isServiceRunning(Context context, Class<? extends Service> serviceClass) {
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void updateWidgets(Context context) {
        Log.debug(TAG, "updateWidgets");
        context.startService(new Intent(context, UpdateService.class));
    }

    public static void startLockService(Context context) {
        Log.debug(TAG, "startLockService");
        context.startService(new Intent(context, LockService.class));
    }

    public static void stopLockService(Context context) {
        Log.debug(TAG, "stopLockService");
        context.stopService(new Intent(context, LockService.class));
    }
}
