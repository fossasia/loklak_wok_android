/**
 *  Timeline
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


package org.loklak.android.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class Timeline implements Iterable<MessageEntry> {

    public static enum Order {
        CREATED_AT("date"),
        RETWEET_COUNT("long"),
        FAVOURITES_COUNT("long");
        String field_type;

        Order(String field_type) {this.field_type = field_type;}

        public String getMessageFieldName() {
            return this.name().toLowerCase();
        }

        public String getMessageFieldType() {
            return this.field_type;
        }
    }

    private LinkedHashMap<String, MessageEntry> tweets; // the key is the date plus id of the tweet
    private Map<String, UserEntry> users;
    private int hits = -1;
    private String scraperInfo = "", peerid = "";
    final private Order order;
    private String query;

    public Timeline(Order order) {
        this.tweets = new LinkedHashMap<String, MessageEntry>();
        this.users = new ConcurrentHashMap<String, UserEntry>();
        this.order = order;
    }

    public static Order parseOrder(String order) {
        try {
            return Order.valueOf(order.toUpperCase());
        } catch (Throwable e) {
            return Order.CREATED_AT;
        }
    }

    public Timeline reduceToMaxsize(final int maxsize) {
        List<MessageEntry> m = new ArrayList<>();
        Timeline t = new Timeline(this.order);
        if (maxsize < 0) return t;

        // remove tweets from this timeline
        synchronized (tweets) {
            while (this.tweets.size() > maxsize) m.add(this.tweets.remove(this.tweets.keySet().iterator().next()));
        }

        // create new timeline
        for (MessageEntry me: m) {
            t.addUser(this.users.get(me.getScreenName()));
            t.addTweet(me);
        }

        // prune away users not needed any more in this structure
        Set<String> screen_names = new HashSet<>();
        for (MessageEntry me: this.tweets.values()) screen_names.add(me.getScreenName());
        synchronized (this.users) {
            Iterator<Map.Entry<String, UserEntry>> i = this.users.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry<String, UserEntry> e = i.next();
                if (!screen_names.contains(e.getValue().getScreenName())) i.remove();
            }
        }
        return t;
    }

    public void setScraperInfo(String info) {
        this.scraperInfo = info;
    }

    public String getScraperInfo() {
        return this.scraperInfo;
    }

    public void setPeerId(String peerid) {
        this.peerid = peerid;
    }

    public String getPeerId() {
        return this.peerid;
    }


    public String getQuery() {
        return this.query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Order getOrder() {
        return this.order;
    }

    public int size() {
        return this.tweets.size();
    }

    public void add(MessageEntry tweet, UserEntry user) {
        this.addUser(user);
        this.addTweet(tweet);
    }

    public void addAll(Timeline t) {
        for (MessageEntry m: t) this.add(m, t.getUser(m));
    }

    private void addUser(UserEntry user) {
        assert user != null;
        if (user != null) this.users.put(user.getScreenName(), user);
    }

    private void addTweet(MessageEntry tweet) {
        String key = "";
        if (this.order == Order.RETWEET_COUNT) {
            key = Long.toHexString(tweet.getRetweetCount());
            while (key.length() < 16) key = "0" + key;
            key = key + "_" + tweet.getIdStr();
        } else if (this.order == Order.FAVOURITES_COUNT) {
            key = Long.toHexString(tweet.getFavouritesCount());
            while (key.length() < 16) key = "0" + key;
            key = key + "_" + tweet.getIdStr();
        } else {
            key = Long.toHexString(tweet.getCreatedAt().getTime()) + "_" + tweet.getIdStr();
        }
        synchronized (tweets) {
            this.tweets.put(key, tweet);
        }
    }

    protected UserEntry getUser(String user_screen_name) {
        return this.users.get(user_screen_name);
    }

    public UserEntry getUser(MessageEntry fromTweet) {
        return this.users.get(fromTweet.getScreenName());
    }

    public void putAll(Timeline other) {
        if (other == null) return;
        assert this.order.equals(other.order);
        for (Map.Entry<String, UserEntry> u: other.users.entrySet()) {
            UserEntry t = this.users.get(u.getKey());
            if (t == null || !t.containsProfileImage()) {
                this.users.put(u.getKey(), u.getValue());
            }
        }
        for (MessageEntry t: other) this.addTweet(t);
    }

    public JSONObject toJSON(boolean withEnrichedData) throws JSONException {
        JSONObject json = new JSONObject();
        JSONObject metadata = new JSONObject();
        metadata.put("count", Integer.toString(this.tweets.size()));
        if (this.query != null) metadata.put("query", this.query);
        if (this.hits >= 0) metadata.put("hits", Math.max(this.hits, this.size()));
        if (this.scraperInfo.length() > 0) metadata.put("scraperInfo", this.scraperInfo);
        if (this.peerid.length() > 0) metadata.put("peerid", this.peerid);
        json.put("search_metadata", metadata);
        JSONArray statuses = new JSONArray();
        for (MessageEntry t: this) {
            UserEntry u = this.users.get(t.getScreenName());
            statuses.put(t.toJSON(u, withEnrichedData, Integer.MAX_VALUE, ""));
        }
        json.put("statuses", statuses);
        return json;
    }

    /**
     * the tweet iterator returns tweets in descending appearance order (top first)
     */
    @Override
    public Iterator<MessageEntry> iterator() {
        return this.tweets.values().iterator();
    }

    public void setHits(int hits) {
        this.hits = hits;
    }

    public int getHits() {
        return this.hits == -1 ? this.size() : this.hits;
    }
}