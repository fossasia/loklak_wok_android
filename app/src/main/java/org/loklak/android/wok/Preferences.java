/**
 *  Preferences
 *  Copyright 02.12.2015 by Michael Peter Christen, @0rb1t3r
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program in the file lgpl21.txt
 *  If not, see <http://www.gnu.org/licenses/>.
 */

package org.loklak.android.wok;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Preferences {

    public enum Key {
        APPHASH, APPGRANTED;
    }

    // we have a cache to prevent that system resources are accessed to often
    private final static Map<String, Boolean> bc = new ConcurrentHashMap<>();
    private final static Map<String, String> sc = new ConcurrentHashMap<>();

    public static void clear() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
        bc.clear();
        sc.clear();
    }

    public static String getConfig(Key key, String dflt) {
        String r = sc.get(key.name());
        if (r != null) return r;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.context);
        r = prefs.getString(key.name(), dflt);
        if (r != null && dflt != null && !r.equals(dflt)) sc.put(key.name(), r);
        return r;
    }
    public static boolean getConfig(Key key, boolean dflt) {
        Boolean r = bc.get(key.name());
        if (r != null) return r.booleanValue();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.context);
        r = prefs.getBoolean(key.name(), dflt);
        if (r != null && !r.equals(dflt)) bc.put(key.name(), r);
        return r;
    }

    public static void setConfig(Key key, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key.name(), value);
        editor.commit();
        if (value != null) sc.put(key.name(), value);
    }

    public static void setConfig(Key key, boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key.name(), value);
        editor.commit();
        bc.put(key.name(), value);
    }
}
