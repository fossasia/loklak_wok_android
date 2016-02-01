/**
 *  Harvester
 *  Copyright 29.11.2015 by Michael Peter Christen, @0rb1t3r
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

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;
import org.loklak.android.client.PushClient;
import org.loklak.android.client.SearchClient;
import org.loklak.android.client.SuggestClient;
import org.loklak.android.data.MessageEntry;
import org.loklak.android.data.QueryEntry;
import org.loklak.android.data.ResultList;
import org.loklak.android.data.Timeline;
import org.loklak.android.harvester.TwitterScraper;
import org.loklak.android.tools.LogLines;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@TargetApi(11)
public class Harvester {

    private final static Random random = new Random(System.currentTimeMillis());

    private final static int MAX_PENDING_CONEXT_QUERIES = 200; // this could be much larger but we don't want to cache too many of these
    private final static int MAX_PENDING_DISPLAY_LINES = 300;
    private final static int MAX_HARVESTED = 10000; // just to prevent a memory leak with possible OOM after a long time we flush that cache after a while
    private final static int HITS_LIMIT_4_QUERIES = 30;
    private final static int FETCH_RANDOM = 3;
    public final static String backend = "http://loklak.org";
    //public final static String backend = "http://10.0.2.2:9001";

    public final static LinkedHashSet<String> pendingQueries = new LinkedHashSet<>();
    public final static ArrayList<String> pendingContext = new ArrayList<>();
    public final static Set<String> harvestedContext = new HashSet<>();

    public static int suggestionsOnBackend = 1000;
    public static int contribution_message_count = -1;

    private static boolean isPushing = false, isLoading = false;

    private static void checkContext(Timeline tl, boolean front) {
        for (MessageEntry tweet: tl) {
            for (String user: tweet.getMentions()) checkContext("from:" + user, front);
            for (String hashtag: tweet.getHashtags()) checkContext(hashtag, true);
        }
    }
    private static void checkContext(String s, boolean front) {
        if (!front && pendingContext.size() > MAX_PENDING_CONEXT_QUERIES) return; // queue is full
        if (!harvestedContext.contains(s) && !pendingContext.contains(s)) {
            if (front) pendingContext.add(0, s); else pendingContext.add(s);
        }
        while (pendingContext.size() > MAX_PENDING_CONEXT_QUERIES) pendingContext.remove(pendingContext.size() - 1);
        if (harvestedContext.size() > MAX_HARVESTED) harvestedContext.clear();
    }

    public static BlockingQueue<Timeline> pushToBackendIndividualTimeline = new LinkedBlockingQueue<Timeline>();
    public static BlockingQueue<Timeline> pushToBackendAccumulationTimeline = new LinkedBlockingQueue<Timeline>();
    public static LogLines<MessageEntry> displayMessages = new LogLines<MessageEntry>(MAX_PENDING_DISPLAY_LINES);

    public static void reduceDisplayMessages() {
        if (displayMessages.size() > 0) {
            displayMessages.poll();
        }
    }

    public static void harvest() {

        if (isPushing || isLoading) return;

        // if we must push to the backend, do that first
        if (pushToBackendIndividualTimeline.size() > 0) {
            try {
                Timeline tl = pushToBackendIndividualTimeline.take();
                isPushing = true;
                MainActivity.statusLine.show("Storing " + tl.size() + " Messages about '" + tl.getQuery() + "'", 2000);
                new PushThread().execute(tl, null, null);
                return;
            } catch (InterruptedException e) {
            }
        }

        // if there are enough messages in the accumulation stack, push that as well first
        Timeline tl = takeTimelineMin(pushToBackendAccumulationTimeline, Timeline.Order.CREATED_AT, 200);
        if (tl != null && tl.size() > 0) {
            // transmit the timeline
            isPushing = true;
            MainActivity.statusLine.show("Storing " + tl.size() + " Messages", 2000);
            new PushThread().execute(tl, null, null);
            return;
        }

        // only if the push-work is done, harvest more
        isLoading = true;
        new LoadThread().execute(null, null, null);
        return;
    }


    private static class LoadThread extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            isLoading = true;
            if (random.nextInt(20) != 0 && suggestionsOnBackend < HITS_LIMIT_4_QUERIES && pendingQueries.size() == 0 && pendingContext.size() > 0) {
                // harvest using the collected keys instead using the queries
                int r = random.nextInt((pendingContext.size() / 2) + 1);
                String q = pendingContext.remove(r);
                harvestedContext.add(q);
                Timeline tl = TwitterScraper.search(q, Timeline.Order.CREATED_AT);
                if (tl == null || tl.size() == 0) {
                    isLoading = false;
                    return null;
                }

                // display the tweets
                for (MessageEntry me: tl) {
                    // we don't want to throttle down just because the display is too full
                    if (displayMessages.size() >= MAX_PENDING_DISPLAY_LINES) reduceDisplayMessages();
                    // add a line at the end of the list
                    displayMessages.add(me);
                }

                // enqueue the tweets
                pushToBackendAccumulationTimeline.add(tl);

                // find content query strings and store them in the context cache
                checkContext(tl, false);
                Log.i("harvest", "retrieval of " + tl.size() + " new messages for q = " + q + ", scheduled push; pendingQueries = " + pendingQueries.size() + ", pendingContext = " + pendingContext.size() + ", harvestedContext = " + harvestedContext.size());
                isLoading = false;
                return null;
            }

            // load more queries if pendingQueries is empty
            if (pendingQueries.size() == 0) {
                MainActivity.statusLine.show("Loading Suggestions", 2000);
                ResultList<QueryEntry> rl = SuggestClient.suggest(backend, "", "query", Math.max(FETCH_RANDOM * 30, suggestionsOnBackend / 10), "asc", "retrieval_next", 0, null, "now", "retrieval_next", FETCH_RANDOM);
                for (QueryEntry qe : rl) {
                    MainActivity.statusLine.show("Got Query '" + qe.getQuery() + "'", 2000);
                    pendingQueries.add(qe.getQuery());
                }
                suggestionsOnBackend = (int) rl.getHits();
                if (rl.size() == 0) {
                    // the backend does not have any new query words for this time.
                    if (pendingContext.size() == 0) {
                        // try to fill the pendingContext using a matchall-query from the cache
                        // http://loklak.org/api/search.json?source=cache&q=
                        try {
                            Timeline tl = SearchClient.search(backend, "", Timeline.Order.CREATED_AT, "cache", 100, 0, 60000);
                            checkContext(tl, false);
                        } catch (IOException e) {}
                    }
                    // if we still don't have any context, we are a bit helpless and hope that this situation
                    // will be better in the future. To prevent that this is called excessively fast, do a pause.
                    if (pendingContext.size() == 0) try {Thread.sleep(10000);} catch (InterruptedException e) {}
                }
            }

            if (pendingQueries.size() == 0) {
                isLoading = false;
                return null;
            }

            // take one of the pending queries or pending context and load the tweets
            String q = pendingQueries.iterator().next();
            pendingQueries.remove(q);
            pendingContext.remove(q);
            harvestedContext.add(q);
            Timeline tl = TwitterScraper.search(q, Timeline.Order.CREATED_AT);

            if (tl == null || tl.size() == 0) {
                isLoading = false;
                return null;
            }

            // display the tweets
            for (MessageEntry me: tl) {
                // we don't want to throttle down just because the display is too full
                if (displayMessages.size() >= MAX_PENDING_DISPLAY_LINES) reduceDisplayMessages();
                // add a line at the end of the list
                displayMessages.add(me);
            }

            // find content query strings and store them in the context cache
            checkContext(tl, true);

            // if we loaded a pending query, push results to backpeer right now
            tl.setQuery(q);
            pushToBackendIndividualTimeline.add(tl);
            isLoading = false;
            return null;
        }
        @Override
        protected void onPreExecute() {}
        @Override
        protected void onProgressUpdate(Void... values) {}
        @Override
        protected void onPostExecute(Void result) {
            isLoading = false;
        }
    }

    private static class PushThread extends AsyncTask<Timeline, Void, Void> {
        @Override
        protected Void doInBackground(Timeline... params) {
            /*
            try {
                Timeline ttl = SearchClient.search(backend, "ccc", Timeline.Order.CREATED_AT, "cache", 100, 0, 4000);
                for (MessageEntry me: ttl) {
                    Log.d("ttl", me.getText(100000, null));
                }
            } catch (IOException e) {}
            */
            isPushing = true;
            Timeline tl = params[0];
            String apphash = Preferences.getConfig(Preferences.Key.APPHASH, "");
            tl.setPeerId(apphash);
            boolean success = false;
            try {
                for (int i = 0; i < 5; i++) {
                    try {
                        long start = System.currentTimeMillis();
                        JSONObject json = PushClient.push(backend, tl);
                        if (json != null) {
                            Log.i("PushThread", "pushed  " + tl.size() + " messages to backend in " + (System.currentTimeMillis() - start) + " ms; pendingQueries = " + pendingQueries.size() + ", pendingContext = " + pendingContext.size() + ", harvestedContext = " + harvestedContext.size() + ", attempt = " + i);

                            // The client tells us how many messages we have pushed already!
                            Object contribution_message_count_obj = json.get("contribution_message_count");
                            if (contribution_message_count_obj != null) {
                                contribution_message_count = (Integer) contribution_message_count_obj;
                            }
                            return null;
                        }
                    } catch (Throwable e) {
                        //e.printStackTrace();
                        Log.d("PushThread", "failed synchronous push to backend, attempt " + i);
                        try {
                            Thread.sleep((i + 1) * 3000);
                        } catch (InterruptedException e1) {
                        }
                    }
                }
            } catch (Throwable e) {
            } finally {
                isPushing = false;
            }
            String q = tl.getQuery();
            tl.setQuery(null);
            pushToBackendAccumulationTimeline.add(tl);
            Log.d("PushThread", "retrieval of " + tl.size() + " new messages for q = " + q + ", scheduled push");
            return null;
        }
        @Override
        protected void onPreExecute() {}
        @Override
        protected void onProgressUpdate(Void... values) {}
        @Override
        protected void onPostExecute(Void result) {
            isPushing = false;
        }
    }


    /**
     * if the given list of timelines contain at least the wanted minimum size of messages, they are flushed from the queue
     * and combined into a new timeline
     * @param dumptl
     * @param order
     * @param minsize
     * @return
     */
    public static Timeline takeTimelineMin(final BlockingQueue<Timeline> dumptl, final Timeline.Order order, final int minsize) {
        int c = 0;
        for (Timeline tl: dumptl) c += tl.size();
        if (c < minsize) return new Timeline(order);

        // now flush the timeline queue completely
        Timeline tl = new Timeline(order);
        try {
            while (dumptl.size() > 0) {
                Timeline tl0 = dumptl.take();
                if (tl0 == null) return tl;
                tl.putAll(tl0);
            }
            return tl;
        } catch (InterruptedException e) {
            return tl;
        }
    }
}
