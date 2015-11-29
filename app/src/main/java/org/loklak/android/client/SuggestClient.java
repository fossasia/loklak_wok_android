/**
 *  SuggestClient
 *  Copyright 13.11.2015 by Michael Peter Christen, @0rb1t3r
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

package org.loklak.android.client;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.loklak.android.data.QueryEntry;
import org.loklak.android.data.ResultList;
import org.loklak.android.tools.JsonIO;

import java.net.URLEncoder;

public class SuggestClient {

    public static ResultList<QueryEntry> suggest(
            final String protocolhostportstub,
            final String q,
            final String source,
            final int count,
            final String order,
            final String orderby,
            final int timezoneOffset,
            final String since,
            final String until,
            final String selectby,
            final int random) {
        ResultList<QueryEntry>  rl = new ResultList<QueryEntry>();
        String urlstring = "";
        try {
            urlstring = protocolhostportstub + "/api/suggest.json?q=" + URLEncoder.encode(q.replace(' ', '+'), "UTF-8") +
                "&timezoneOffset=" + timezoneOffset +
                "&count=" + count +
                "&source=" + (source == null ? "all" : source) +
                (order == null ? "" : ("&order=" + order)) +
                (orderby == null ? "" : ("&orderby=" + orderby)) +
                (since == null ? "" : ("&since=" + since)) +
                (until == null ? "" : ("&until=" + until)) +
                (selectby == null ? "" : ("&selectby=" + selectby)) +
                (random < 0 ? "" : ("&random=" + random)) +
                "&minified=true";
            JSONObject json = JsonIO.loadJson(urlstring);
            if (json == null || json.length() == 0) return rl;
            JSONArray queries = json.getJSONArray("queries");
            if (queries != null) {
                for (int i = 0; i < queries.length(); i++) {
                    JSONObject query = queries.getJSONObject(i);
                    if (query == null) continue;
                    QueryEntry qe = new QueryEntry(query);
                    rl.add(qe);
                }
            }

            JSONObject metadata = json.getJSONObject("search_metadata");
            if (metadata != null) {
                long hits = metadata.getLong("hits");
                rl.setHits(hits);
            }
        } catch (Throwable e) {
            Log.e("SuggestClient", e.getMessage(), e);
        }
        return rl;
    }

}
