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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;

import java.lang.reflect.Method;

public abstract class CompatService extends Service {
    private static final String TAG = CompatService.class.getSimpleName();

    private static Method REF_SETFOREGROUND;
    private static Method REF_STARTFOREGROUND;
    private static Method REF_STOPFOREGROUND;

    static {
        Class<?> clz = Service.class;
        try {
            REF_SETFOREGROUND = clz.getMethod("setForeground", boolean.class);
        } catch (NoSuchMethodException e) {
            try {
                REF_STARTFOREGROUND = clz.getMethod("startForeground", int.class,
                        Notification.class);
                REF_STOPFOREGROUND = clz.getMethod("stopForeground", boolean.class);
            } catch (NoSuchMethodException e2) {
                Log.error(TAG,
                        "OS doesn't have Service.startForeground OR Service.setForeground!", null);
            }
        }
    }

    private NotificationManager notifications;

    @Override
    public final int onStartCommand(Intent intent, int flags, int startId) {
        onStart(intent, startId);
        return START_STICKY;
    }

    public final void onStart(Intent intent, int startId) {
        if (REF_SETFOREGROUND != null) {
            notifications = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        onCompatStart(intent, startId);
    }

    public void onCompatStart(Intent intent, int startId) {
    }

    private void invoke(Method method, Object... args) {
        try {
            method.invoke(this, args);
        } catch (Exception e) {
            Log.error(TAG, "Failed to invoke " + method.getName() + "(..)", e);
        }
    }

    /**
     * @see Service#startForeground(int, Notification)
     */
    protected final void startCompatForeground(int id, Notification notification) {
        if (REF_SETFOREGROUND != null) {
            invoke(REF_SETFOREGROUND, true);
            notifications.notify(id, notification);
        } else {
            invoke(REF_STARTFOREGROUND, id, notification);
        }
    }

    /**
     * @see Service#stopForeground(boolean)
     */
    protected final void stopCompatForeground(int id) {
        if (REF_SETFOREGROUND != null) {
            notifications.cancel(id);
            invoke(REF_SETFOREGROUND, false);
        } else {
            invoke(REF_STOPFOREGROUND, true);
        }
    }
}
