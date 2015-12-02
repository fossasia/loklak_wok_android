/**
 *  Query
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
 *  but WITHOUT ANY WARRANTY; wo even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program in the file lgpl21.txt
 *  If not, see <http://www.gnu.org/licenses/>.
 */

package org.loklak.android.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A Query is a recording of a search result based on the query.
 * THIS IS NOT RECORDED TO TRACK USER ACTIONS, THIS IS USED TO RE-SEARCH A QUERY INDEFINITELY!
 * Each query will be stored in elasticsearch and retrieved by the caretaker process in
 * order of the retrieval_next field. That date is calculated based on the number of search results
 * in the last time; the retrieval_next is estimated based on the time interval of all tweets in
 * the search results of the last query.
 *
 * Privacy is important:
 * TO ALL COMMITTERS: please do not add any user-identification details to the data structures
 * to protect the privacy of the users; TO CODE EVALUATORS: please look for yourself that this
 * code does not contain any user-related information (like IP, user agent etc.).
 */
public class QueryEntry extends AbstractIndexEntry {

    private final static long DAY_MILLIS = 1000L * 60L * 60L * 24L;
    private final static int RETRIEVAL_CONSTANT = 20; // the number of messages that we get with each retrieval at maximum

    protected String query;           // the query in the exact way as the user typed it in
    protected int query_length;       // the length in the query, number of characters
    protected SourceType source_type; // the (external) retrieval system where that query was submitted
    protected int timezoneOffset;     // the timezone offset of the user
    protected Date query_first;       // the date when this query was submitted by the user the first time
    protected Date query_last;        // the date when this query was submitted by the user the last time
    protected Date retrieval_last;    // the last time when this query was submitted to the external system
    protected Date retrieval_next;    // the estimated next time when the query should be submitted to get all messages
    protected Date expected_next;     // the estimated next time when one single message will appear
    protected int query_count;        // the number of queries by the user of that query done so far
    protected int retrieval_count;    // the number of retrievals of that query done so far to the external system
    protected long message_period;    // the estimated period length between two messages
    protected int messages_per_day;   // a message frequency based on the last query
    protected long score_retrieval;   // score for the retrieval order
    protected long score_suggest;     // score for the suggest order

    /**
     * This initializer can only be used for first-time creation of a query track.
     *
     * @param query
     * @param timezoneOffset
     * @param message_period
     * @param source_type
     * @throws MalformedURLException
     */
    public QueryEntry(final String query, final int timezoneOffset, final long message_period, final SourceType source_type, final boolean byUserQuery) {
        this.query = query;
        this.query_length = query.length();
        this.timezoneOffset = timezoneOffset;
        this.source_type = source_type;
        this.retrieval_count = 0; // will be set to 1 with first update
        this.message_period = 0; // means: unknown
        this.messages_per_day = 0; // means: unknown
        this.score_retrieval = 0;
        this.score_suggest = 0;
        update(message_period, byUserQuery);
        this.query_first = retrieval_last;
    }

    public QueryEntry(JSONObject json) throws IllegalArgumentException, JSONException {
        init(json);
    }

    public void init(JSONObject json) throws IllegalArgumentException, JSONException {
        this.query = (String) json.get("query");
        this.query_length = (int) parseLong((Number) json.get("query_length"));
        String source_type_string = (String) json.get("source_type");
        if (source_type_string == null) source_type_string = SourceType.USER.name();
        this.source_type = SourceType.valueOf(source_type_string);
        this.timezoneOffset = (int) parseLong((Number) json.get("timezoneOffset"));
        Date now = new Date();
        this.query_first = parseDate(json.get("query_first"), now);
        this.query_last = parseDate(json.get("query_last"), now);
        this.retrieval_last = parseDate(json.get("retrieval_last"), now);
        this.retrieval_next = parseDate(json.get("retrieval_next"), now);
        this.expected_next = parseDate(json.get("expected_next"), now);
        this.query_count = (int) parseLong((Number) json.get("query_count"));
        this.retrieval_count = (int) parseLong((Number) json.get("retrieval_count"));
        this.message_period = parseLong((Number) json.get("message_period"));
        this.messages_per_day = (int) parseLong((Number) json.get("messages_per_day"));
        this.score_retrieval = (int) parseLong((Number) json.get("score_retrieval"));
        this.score_suggest = (int) parseLong((Number) json.get("score_suggest"));
    }

    /**
     * update the query entry
     *
     * @param message_period
     * @param byUserQuery    is true, if the query was submitted by the user; false if the query was submitted by an automatic system
     */
    public void update(final long message_period, final boolean byUserQuery) {
        this.retrieval_last = new Date();
        this.retrieval_count++;
        if (byUserQuery) {
            this.query_count++;
            this.query_last = this.retrieval_last;
        }
        long new_message_period = message_period; // can be Long.MAX_VALUE if less than 2 messages are in timeline!
        int new_messages_per_day = (int) (DAY_MILLIS / new_message_period); // this is an interpolation based on the last tweet list, can be 0!
        if (new_message_period == Long.MAX_VALUE || new_messages_per_day == 0) {
            this.message_period = this.message_period == 0 ? DAY_MILLIS : Math.min(DAY_MILLIS, this.message_period * 2);
        } else {
            this.message_period = this.message_period == 0 ? new_message_period : (this.message_period + new_message_period) / 2;
        }
        this.messages_per_day = (int) (DAY_MILLIS / this.message_period);
        double ttl_factor = 0.75d;
        long pivot_period = 10000;
        this.expected_next = new Date(this.retrieval_last.getTime() + ((long) (ttl_factor * this.message_period)));
        long strategic_period =   // if the period is far below the minimum, we apply a penalty
                (this.message_period < pivot_period ?
                        pivot_period + 1000 * (long) Math.pow((pivot_period - this.message_period) / 1000, 3) :
                        this.message_period);
        long waitingtime = Math.min(DAY_MILLIS, (long) (ttl_factor * RETRIEVAL_CONSTANT * strategic_period));
        this.retrieval_next = new Date(this.retrieval_last.getTime() + waitingtime);
    }
    // to check the retrieval order created by the update method, call
    // http://localhost:9000/api/suggest.json?orderby=retrieval_next&order=asc

    /**
     * A 'blind' update can be done if the user submits a query but there are rules which prevent that the target system is queried
     * as well. Then the query result is calculated using the already stored messages. To reflect this, only the query-related
     * attributes are changed.
     */
    public void update() {
        this.query_count++;
        this.query_last = new Date();
    }

    public String getQuery() {
        return this.query;
    }

    public int getQueryLength() {
        return this.query_length;
    }

    public SourceType getSourceType() {
        return this.source_type;
    }

    public Date getQueryFirst() {
        return this.query_first;
    }

    public Date getQueryLast() {
        return this.query_last;
    }

    public Date getRetrievalLast() {
        return this.retrieval_last;
    }

    public Date getRetrievalNext() {
        return this.retrieval_next;
    }

    public Date getExpectedNext() {
        return this.expected_next;
    }

    public int getTimezoneOffset() {
        return this.timezoneOffset;
    }

    public int getQueryCount() {
        return this.query_count;
    }

    public int getRetrievalCount() {
        return this.retrieval_count;
    }

    public int getMessagesPerDay() {
        return this.messages_per_day;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("query", this.query);
        m.put("query_length", this.query_length);
        m.put("source_type", this.source_type.name());
        m.put("timezoneOffset", this.timezoneOffset);
        if (this.query_first != null)
            m.put("query_first", utcFormatter.print(this.query_first.getTime()));
        if (this.query_last != null)
            m.put("query_last", utcFormatter.print(this.query_last.getTime()));
        if (this.retrieval_last != null)
            m.put("retrieval_last", utcFormatter.print(this.retrieval_last.getTime()));
        if (this.retrieval_next != null)
            m.put("retrieval_next", utcFormatter.print(this.retrieval_next.getTime()));
        if (this.expected_next != null)
            m.put("expected_next", utcFormatter.print(this.expected_next.getTime()));
        m.put("query_count", this.query_count);
        m.put("retrieval_count", this.retrieval_count);
        m.put("message_period", this.message_period);
        m.put("messages_per_day", this.messages_per_day);
        m.put("score_retrieval", this.score_retrieval);
        m.put("score_suggest", this.score_suggest);
        return m;
    }

    private final static Pattern tokenizerPattern = Pattern.compile("([^\"]\\S*|\".+?\")\\s*"); // tokenizes Strings into terms respecting quoted parts

    private static enum Constraint {
        image("images"),
        audio("audio"),
        video("videos"),
        place("place_name"),
        location("location_point"),
        link("links"),
        mention("mentions"),
        source_type("source_type"),
        hashtag("hashtags"),
        emotion("classifier_emotion"),
        profanity("classifier_profanity"),
        language("classifier_language");
        protected String field_name;
        protected Pattern pattern;

        private Constraint(String field_name) {
            this.field_name = field_name;
            this.pattern = Pattern.compile("\\s?\\-?/" + this.name() + "\\S*");
        }
    }

    public static class Tokens {

        public String original;
        public String raw;
        public HashSet<String> constraints_positive, constraints_negative;
        public Map<String, String> modifier;
        public PlaceContext place_context;
        public double[] bbox; // double[]{lon_west,lat_south,lon_east,lat_north}

        public Tokens(final String q) {
            this.original = q;
            List<String> tokens = new ArrayList<String>();
            Matcher m = tokenizerPattern.matcher(q);
            while (m.find()) tokens.add(m.group(1));

            this.constraints_positive = new HashSet<>();
            this.constraints_negative = new HashSet<>();
            this.modifier = new HashMap<String, String>();
            StringBuilder rawb = new StringBuilder(q.length() + 1);
            Set<String> hashtags = new HashSet<>();
            for (String t : tokens) {
                if (t.startsWith("/")) {
                    constraints_positive.add(t.substring(1));
                    continue;
                } else if (t.startsWith("-/")) {
                    constraints_negative.add(t.substring(2));
                    continue;
                } else if (t.indexOf(':') > 0) {
                    int p = t.indexOf(':');
                    modifier.put(t.substring(0, p).toLowerCase(), t.substring(p + 1));
                    rawb.append(t).append(' ');
                    continue;
                } else {
                    if (t.startsWith("#")) hashtags.add(t.substring(1));
                    rawb.append(t).append(' ');
                }
            }
            this.place_context = this.constraints_positive.remove("about") ? PlaceContext.ABOUT : PlaceContext.FROM;
            if (this.constraints_negative.remove("about")) this.place_context = PlaceContext.FROM;
            if (rawb.length() > 0 && rawb.charAt(rawb.length() - 1) == ' ')
                rawb.setLength(rawb.length() - 1);
            this.raw = rawb.toString();
            // fix common mistake using hashtags in combination with their words without hashtag
            for (String h : hashtags) {
                int p = this.raw.indexOf(h + " #" + h);
                if (p >= 0)
                    this.raw = this.raw.substring(0, p) + h + " OR #" + h + this.raw.substring(p + h.length() * 2 + 2);
                p = this.raw.indexOf("#" + h + " " + h);
                if (p >= 0)
                    this.raw = this.raw.substring(0, p) + "#" + h + " OR " + h + this.raw.substring(p + h.length() * 2 + 2);
            }

            // find bbox
            this.bbox = null;
            bboxsearch:
            for (String cs : this.constraints_positive) {
                if (cs.startsWith(Constraint.location.name() + "=")) {
                    String params = cs.substring(Constraint.location.name().length() + 1);
                    String[] coord = params.split(",");
                    if (coord.length == 4) {
                        this.bbox = new double[4];
                        for (int i = 0; i < 4; i++) this.bbox[i] = Double.parseDouble(coord[i]);
                        break bboxsearch;
                    }
                }
            }
        }

    }

    private final static Pattern term4ORPattern = Pattern.compile("(?:^| )(\\S*(?: OR \\S*)+)(?: |$)"); // Pattern.compile("(^\\s*(?: OR ^\\s*+)+)");

    private static List<String> splitIntoORGroups(String q) {
        // detect usage of OR junctor usage. Right now we cannot have mixed AND and OR usage. Thats a hack right now
        q = q.replaceAll(" AND ", " "); // AND is default

        // tokenize the query
        ArrayList<String> list = new ArrayList<>();
        Matcher m = term4ORPattern.matcher(q);
        while (m.find()) {
            String d = m.group(1);
            q = q.replace(d, "").replace("  ", " ");
            list.add(d);
            m = term4ORPattern.matcher(q);
        }
        q = q.trim();
        if (q.length() > 0) list.add(0, q);
        return list;
    }

    public static enum PlaceContext {

        FROM,  // the message was made at that place
        ABOUT; // the message is about that place

    }
}

