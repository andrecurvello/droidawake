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

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

public class UpdateService extends Service {
    private static final String TAG = UpdateService.class.getSimpleName();

    private static final int BUTTON_ON = R.drawable.bulb_on;
    private static final int BUTTON_OFF = R.drawable.bulb_off;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onStart(intent, startId);
        return START_STICKY;
    }

    // used for backwards compatibility
    @Override
    public void onStart(Intent intent, int startId) {
        Log.debug(TAG, "onStart");

        updateWidgets();

        // stop service, it's not needed anymore
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // not needed
    }

    @Override
    public void onDestroy() {
        Log.debug(TAG, "onDestroy");
    }

    private void updateWidgets() {
        RemoteViews views = getUpdatedViews();
        // push update
        ComponentName thisWidget = new ComponentName(this, DroidAwakeWidgetProvider.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        manager.updateAppWidget(thisWidget, views);
    }

    private RemoteViews getUpdatedViews() {
        boolean hasLock = LockService.isLocked(this);
        Log.debug(TAG, "UpdateService.getUpdatedViews: hasLock = " + hasLock);

        // create an intent to broadcast the toggle action
        Intent intent = new Intent(this, DroidAwakeWidgetProvider.class);
        intent.setAction(DroidAwakeWidgetProvider.ACTION_TOGGLE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        // get the layout, set drawable and attach the on-click listener
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget);
        views.setImageViewResource(R.id.button, hasLock ? BUTTON_ON : BUTTON_OFF);
        views.setOnClickPendingIntent(R.id.button, pendingIntent);
        return views;
    }

}
