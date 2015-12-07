/**
 *  SearchClient
 *  Copyright 22.02.2015 by Michael Peter Christen, @0rb1t3r
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
    import org.loklak.android.data.MessageEntry;
    import org.loklak.android.data.Timeline;
    import org.loklak.android.data.UserEntry;
    import org.loklak.android.tools.JsonIO;

    import java.io.IOException;
    import java.net.URLEncoder;

public class SearchClient {

    // possible values: cache, twitter, all
    public static Timeline search(final String protocolhostportstub, final String query, final Timeline.Order order, final String source, final int count, final int timezoneOffset, final long timeout) throws IOException {
        Timeline tl = new Timeline(order);
        String urlstring = "";
        try {
            urlstring = protocolhostportstub + "/api/search.json?q=" + URLEncoder.encode(query.replace(' ', '+'), "UTF-8") + "&timezoneOffset=" + timezoneOffset + "&maximumRecords=" + count + "&source=" + (source == null ? "all" : source) + "&minified=true&timeout=" + timeout;
            JSONObject json = JsonIO.loadJson(urlstring);
            if (json == null || json.length() == 0) return tl;
            JSONArray statuses = json.getJSONArray("statuses");
            if (statuses != null) {
                for (int i = 0; i < statuses.length(); i++) {
                    JSONObject tweet = statuses.getJSONObject(i);
                    JSONObject user = tweet.getJSONObject("user");
                    if (user == null) continue;
                    tweet.remove("user");
                    UserEntry u = new UserEntry(user);
                    MessageEntry t = new MessageEntry(tweet);
                    tl.add(t, u);
                }
            }
            if (json.has("search_metadata")) {
                JSONObject metadata = json.getJSONObject("search_metadata");
                if (metadata.has("hits")) {
                    tl.setHits((Integer) metadata.get("hits"));
                }
                if (metadata.has("scraperInfo")) {
                    String scraperInfo = (String) metadata.get("scraperInfo");
                    tl.setScraperInfo(scraperInfo);
                }
            }
        } catch (Throwable e) {
            Log.e("SeachClient", e.getMessage(), e);
        }
        //System.out.println(parser.text());
        return tl;
    }

}
