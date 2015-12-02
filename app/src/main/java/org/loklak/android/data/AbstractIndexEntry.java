/**
 *  AbstractIndexEntry
 *  Copyright 26.04.2015 by Michael Peter Christen, @0rb1t3r
 *  This class is the android version from the original file,
 *  taken from the loklak_server project. It may be slightly different.
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

package org.loklak.android.data;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;

public class AbstractIndexEntry {

    public AbstractIndexEntry() {
    }

    // helper methods to write json

    public final static DateTimeFormatter utcFormatter = ISODateTimeFormat.dateTime().withZoneUTC();

    // helper methods to read json

    public static Date parseDate(Object d) {
        if (d == null) return new Date();
        if (d instanceof Date) return (Date) d;
        if (d instanceof Long) return new Date(((Long) d).longValue());
        if (d instanceof String) return ISODateTimeFormat.dateOptionalTimeParser().parseDateTime((String) d).toDate();
        assert false;
        return new Date();
    }

    public static Date parseDate(Object d, Date dflt) {
        if (d == null) return dflt;
        if (d instanceof Long) return new Date(((Long) d).longValue());
        if (d instanceof String) return ISODateTimeFormat.dateOptionalTimeParser().parseDateTime((String) d).toDate();
        assert false;
        return dflt;
    }

    public static String parseString(Object s) {
        assert s == null || s instanceof String: "type of object s: " + s.getClass().toString();
        return s == null ? "" : (String) s;
    }

    public static long parseLong(Object n) {
        if (n instanceof String) {
            return Long.parseLong((String) n);
        }
        assert n == null || n instanceof Number;
        return n == null ? 0 : ((Number) n).longValue();
    }

    @SuppressWarnings("unchecked")
    public static LinkedHashSet<String> parseArrayList(Object l) {
        assert l == null || l instanceof String  || l instanceof String[] || l instanceof Collection<?>;
        if (l == null) return new LinkedHashSet<String>();
        if (l instanceof String) {
            LinkedHashSet<String> a = new LinkedHashSet<String>();
            a.add((String) l);
            return a;
        }
        if (l instanceof String[]) {
            LinkedHashSet<String> a = new LinkedHashSet<String>();
            for (String s: ((String[]) l)) if (s != null) a.add(s);
            return a;
        }
        if (l instanceof LinkedHashSet<?>) {
            LinkedHashSet<String> a = new LinkedHashSet<String>();
            for (String s: (LinkedHashSet<String>) l) if (s != null) a.add(s);
            return a;
        }
        if (l instanceof JSONArray) {
            LinkedHashSet<String> a = new LinkedHashSet<String>();
            JSONArray la = (JSONArray) l;
            for (int i = 0; i < la.length(); i++) {
                try {
                    String s = la.getString(i);
                    if (s != null) a.add(s);
                } catch (JSONException e) {}
            }
            return a;
        }
        LinkedHashSet<String> a = new LinkedHashSet<String>();
        for (Object s: ((Collection<?>) l)) if (s != null) a.add((String) s);
        return a;
    }

    public Object lazyGet(JSONObject json, String key) {
        try {
            Object o = json.get(key);
            return o;
        } catch (JSONException e) {
            return null;
        }
    }
}
