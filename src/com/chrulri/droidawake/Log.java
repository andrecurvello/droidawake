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

public final class Log {
    private static boolean DEBUGGING = true;

    private Log() {
    }

    public static void debug(String tag, String msg) {
        if (DEBUGGING) {
            android.util.Log.d(tag, msg);
        }
    }

    public static void error(String tag, String msg, Throwable t) {
        android.util.Log.e(tag, msg, t);
    }
}
