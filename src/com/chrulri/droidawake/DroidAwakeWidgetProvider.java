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

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

public class DroidAwakeWidgetProvider extends AppWidgetProvider {
    private static final String TAG = DroidAwakeWidgetProvider.class.getSimpleName();

    static final String ACTION_TOGGLE = "com.chrulri.droidawake.ACTION_TOGGLE";

    @Override
    public void onEnabled(Context context) {
        Log.debug(TAG, "onEnabled");
        Utils.updateWidgets(context);
    }

    @Override
    public void onDisabled(Context context) {
        Log.debug(TAG, "onDisabled");
        Utils.stopLockService(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.debug(TAG, "onReceive: " + intent);
        if (ACTION_TOGGLE.equals(intent.getAction())) {
            Log.debug(TAG, "onReceive ACTION_TOGGLE");
            if (LockService.isLocked(context)) {
                Utils.stopLockService(context);
            } else {
                Utils.startLockService(context);
            }
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.debug(TAG, "onUpdate");
        Utils.updateWidgets(context);
    }
}
