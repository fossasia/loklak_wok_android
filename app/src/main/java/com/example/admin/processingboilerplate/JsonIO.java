/**
 *  JsonIO
 *  Copyright 17.11.2015 by Michael Peter Christen, @0rb1t3r
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

package com.example.admin.processingboilerplate;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;


public class JsonIO {
    public static JSONObject loadJson(String url) {
        StringBuilder sb = loadString(url);
        if (sb == null || sb.length() == 0) return new JSONObject();
        JSONObject json = null;
        try {
            json = new JSONObject(sb.toString());
            return json;
        } catch (JSONException e) {
            Log.e("loadJson", e.getMessage(), e);
            return new JSONObject();
        }
    }

    public static StringBuilder loadString(String url) {
        StringBuilder sb = new StringBuilder();
        try {
            URLConnection uc = (new URL(url)).openConnection();
            HttpURLConnection con = url.startsWith("https") ? (HttpsURLConnection) uc : (HttpURLConnection) uc;
            con.setReadTimeout(6000);
            con.setConnectTimeout(6000);
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
                String s;
                while ((s = br.readLine()) != null) sb.append(s).append('\n');
            } catch (IOException e) {
                Log.e("loadJson", e.getMessage(), e);
            } finally {
                try {
                    if (br != null) br.close();
                    con.disconnect();
                } catch (IOException e) {
                    Log.e("loadJson", e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            Log.e("loadJson", e.getMessage(), e);
        }
        return sb;
    }
}
