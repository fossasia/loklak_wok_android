package org.loklak.android.wok;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by admin on 02.12.15.
 */
public class Preferences {

    public enum Key {
        APPHASH, APPGRANTED;
    }

    public static void clear() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
    }

    public static String getConfig(Key key, String dflt) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.context);
        return prefs.getString(key.name(), dflt);

    }
    public static boolean getConfig(Key key, boolean dflt) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.context);
        return prefs.getBoolean(key.name(), dflt);

    }

    public static void setConfig(Key key, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key.name(), value);
        editor.commit();
    }

    public static void setConfig(Key key, boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key.name(), value);
        editor.commit();
    }
}
