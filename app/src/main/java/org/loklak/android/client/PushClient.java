/**
 *  PushClient
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

import org.json.JSONException;
import org.json.JSONObject;
import org.loklak.android.data.Timeline;
import org.loklak.android.tools.JsonIO;

public class PushClient {

    /**
     * transmit the timeline to several hosts
     * @param timeline
     * @return the json object from the host api if the transfer was successfull or null otherwise
     */
    public static JSONObject push(String hoststub, Timeline timeline) {
        // transmit the timeline
        try {
            if (hoststub.endsWith("/")) hoststub = hoststub.substring(0, hoststub.length() - 1);
            JSONObject json = JsonIO.pushJson(hoststub + "/api/push.json", "data", timeline.toJSON(false));
            if (json != null) return json;
        } catch (JSONException e) {
            Log.d("PushClient", e.getMessage(), e);
            return null;
        }
        return null;
    }
}
