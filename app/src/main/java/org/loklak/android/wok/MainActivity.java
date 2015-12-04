/**
 *  Loklak Wok
 *  Copyright 16.11.2015 by Michael Peter Christen, @0rb1t3r
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
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import org.loklak.android.data.MessageEntry;

import java.util.Random;

import processing.core.PApplet;
import processing.core.PFont;


@TargetApi(11)
public class MainActivity extends AppCompatActivity {

    // defined as global static objects to prevent that they are computed again when device is turned
    public static PFont font = null;
    public static final int FRAME_RATE = 12;
    private final static Random random = new Random(System.currentTimeMillis());
    public static Context context; // replace with getBaseContext() ?
    public static StatusLine statusLine;
    public static Sketch sketch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        FragmentManager fragmentManager = getFragmentManager();
        sketch = new Sketch();
        fragmentManager.beginTransaction().replace(R.id.container, sketch).commit();
        context = this.getApplicationContext();

        // debug code to clear all preferences
        //Preferences.clear();

        // create preferences
        String apphash = Preferences.getConfig(Preferences.Key.APPHASH, "");
        if (apphash.length() == 0) {
            apphash = "LW_" + Integer.toHexString(Math.abs((Build.FINGERPRINT == null ? ("A404" + System.currentTimeMillis()) : Build.FINGERPRINT).hashCode()));
            Preferences.setConfig(Preferences.Key.APPHASH, apphash);
        }

        // start background task
        startService(new Intent(MainActivity.this, HarvestService.class));
    }

    public static boolean isConnectedWifi() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm == null ? null : cm.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_WIFI);
    }

    public static class Sketch extends PApplet {

        int fontsize;
        int randomX = 25, randomY = 0;
        boolean showsplash = true;
        boolean wasWifiConnected = true;
        boolean acceptNonWifiConnection = false;
        Buttons buttons_disconnected, buttons_harvesting, buttons_splash;

        @Override
        public void settings() {
            fullScreen();
            size(width, height, JAVA2D);
            for (String font: PFont.list()) Log.d("setup", "font = " + font);
            fontsize = Math.min(width, height) / 38; // computes to a font size of 20 for a 768 width
            if (font == null) font = createFont("DroidSansMono.ttf", fontsize * 4, true); // at a height of 20, this font has a width of 12
            // with this settings, we have exactly space for 64 characters on a horizontal-oriented phone
            statusLine = new StatusLine(this, fontsize * 3 / 2, 32, 180, 230);
        }

        @Override
        public void setup() {
            frameRate(FRAME_RATE);
            // keep app awake
            PowerManager pm = (PowerManager) this.getActivity().getApplicationContext().getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "loklak");
            wl.acquire();
            showsplash = !Preferences.getConfig(Preferences.Key.APPGRANTED, false);
            // first message
            statusLine.show("warming up loklak wok", 4000);
            buttons_disconnected = new Buttons(this);
            buttons_harvesting = new Buttons(this);
            buttons_splash = new Buttons(this);
            Buttons.Button unlock0 = buttons_disconnected.createButton();
            unlock0
                    .setCenter(width / 4, 5 * height / 6)
                    .setWidth(fontsize * 9)
                    .setFontsize(fontsize * 3 / 2)
                    .setOffText("PRESS", "TO", "UNLOCK")
                    .setOnText("", "UNLOCKED", "")
                    .setBorderWidth(8)
                    .setBorderColor(32, 180, 230)
                    .setOnColor(16, 90, 115)
                    .setOffColor(0, 0, 0)
                    .setTextColor(255, 200, 41)
                    .setTransitionTime(300)
                    .setStatus(0);;
            Buttons.Button unlock1 = (Buttons.Button) unlock0.clone();
            unlock1.setCenter(width / 2, 5 * height / 6).setStatus(0);
            Buttons.Button unlock2 = (Buttons.Button) unlock0.clone();
            unlock2.setCenter(3 * width / 4, 5 * height / 6).setStatus(0);
            buttons_disconnected.addButton("unlock0", unlock0);
            buttons_disconnected.addButton("unlock1", unlock1);
            buttons_disconnected.addButton("unlock2", unlock2);
            Buttons.Button terminate = (Buttons.Button) unlock0.clone();
            terminate.setCenter(fontsize, fontsize).setFontsize(fontsize).setWidth(fontsize).setBorderWidth(3).setOffText("", "X", "").setOnText("", "", "").setTextColor(32, 180, 230);
            buttons_harvesting.addButton("terminate", terminate);
            Buttons.Button offline = (Buttons.Button) unlock0.clone();
            offline.setCenter(width - fontsize, fontsize).setFontsize(fontsize).setWidth(fontsize).setBorderWidth(3).setOffText("", "O", "").setOnText("", "", "").setTextColor(32, 180, 230).invisible();
            buttons_harvesting.addButton("offline", offline);

            Buttons.Button startapp = (Buttons.Button) unlock1.clone();
            startapp.setCenter(width / 2, 5 * height / 6).setOffText("PUSH", "TO", "START").setOnText("", "", "");
            buttons_splash.addButton("startapp", startapp);
        }

        @Override
        public void draw() {

            // stats
            long start = System.currentTimeMillis();

            // check wifi status
            boolean harvestEnabled = false;
            if (isConnectedWifi()) {
                harvestEnabled = true;
                wasWifiConnected = true;
            } else if (acceptNonWifiConnection) {
                harvestEnabled = true;
                wasWifiConnected = false;
            } else {
                if (wasWifiConnected) {
                    // old settings to grant access are now old
                    acceptNonWifiConnection = false;
                    harvestEnabled = false;
                }
                wasWifiConnected = false;
            }

            // clean up broken fonts (may happen for unknown reason)
            if (frameCount % (30 * FRAME_RATE) == 2) {
                // the font is broken after some time, we don't know the reason. This fixes it.
                font = createFont("DroidSansMono.ttf", fontsize * 2, true);
            }

            // make a background
            colorMode(RGB, 256);
            background(30, 40, 50);
            textAlign(LEFT, TOP);

            // draw a headline
            stroke(32, 180, 230);
            strokeWeight(1);
            int cw = Math.min(70, width / 10);
            int xo = (width - 10 * cw) / 2;
            int vpos = 20;
            float dd = 0.1f * cw / 70;
            for (int w = 0; w < 3; w++) {
                GraphicData.loklakShape.draw(this, "l", xo + 0 * cw + w, vpos, dd, randomX, randomY);
                GraphicData.loklakShape.draw(this, "o", xo + 1 * cw + w, vpos, dd, randomX, randomY);
                GraphicData.loklakShape.draw(this, "k", xo + 2 * cw + w, vpos, dd, randomX, randomY);
                GraphicData.loklakShape.draw(this, "l", xo + 3 * cw + w, vpos, dd, randomX, randomY);
                GraphicData.loklakShape.draw(this, "a", xo + 4 * cw + w, vpos, dd, randomX, randomY);
                GraphicData.loklakShape.draw(this, "k", xo + 5 * cw + w, vpos, dd, randomX, randomY);

                GraphicData.loklakShape.draw(this, "w", xo + 7 * cw + w, vpos, dd, randomX, randomY);
                GraphicData.loklakShape.draw(this, "o", xo + 8 * cw + w, vpos, dd, randomX, randomY);
                GraphicData.loklakShape.draw(this, "k", xo + 9 * cw + w, vpos, dd, randomX, randomY);
            }
            vpos += 100;

            // draw status line
            fill(255, 200, 41);
            textFont(font, fontsize);
            statusLine.setY(vpos);
            statusLine.draw();


            if (showsplash) {

                // ==== SHOW GREETING AND ASK FOR PERMISSION TO HARVETS DATA FOR loklak.org ====
                if (statusLine.getQueueSize() == 0) {
                    statusLine.show("Welcome to the loklak.org data harvesting app", 3000);
                }

                textFont(font, fontsize * 2);
                textAlign(CENTER, CENTER);
                fill(32, 180, 230);
                int y = (int) (height / 2);
                text("AGREEMENT", width / 2, y); y += 2 * fontsize;
                textFont(font, fontsize);
                text("This app harvests tweets from twitter", width / 2, y); y += fontsize;
                text("and sends them to loklak.org", width / 2, y); y += fontsize;
                text("Push 'START' to agree", width / 2, y); y += fontsize;

                // draw the buttons (always at last to make them visible at all cost)
                buttons_splash.draw();

                // react on button status
                if (buttons_splash.getButton("startapp").isActivated()) play(R.raw.blackie666__alienbleep);
                if (buttons_splash.getStatus("startapp") == 255) {
                    showsplash = false;
                    Preferences.setConfig(Preferences.Key.APPGRANTED, true);
                }

                randomX = 0;

            } else if (!harvestEnabled) {

                // ==== SHOW MESSAGE THAT WE DON'T HAVE WIFI; ASK FOR PERMISSION TO HARVEST ANYWAY ====
                if (statusLine.getQueueSize() == 0) {
                    statusLine.show("Harvesting for non-wifi connections disabled", 3000);
                    statusLine.show("Unlock all three buttons to harvest anyway", 3000);
                }

                stroke(32, 180, 230);
                strokeWeight(1);
                int border = Math.min(width, height) / 4;
                float shapesize = ((float) (border * 2.0f) / (float) (GraphicData.shape_wifi3.maxx() - GraphicData.shape_wifi3.minx()));
                for (int w = 0; w < 5; w++) {
                    GraphicData.wifiShape.draw(this, "shape_wifi3", border, height / 3, shapesize, 50, 20);
                    GraphicData.wifiShape.draw(this, "shape_wifi2", border, height / 3, shapesize, 50, 20);
                    GraphicData.wifiShape.draw(this, "shape_wifi1", border, height / 3, shapesize, 50, 20);
                    GraphicData.wifiShape.draw(this, "shape_wifi0", border, height / 3, shapesize, 50, 20);
                }
                textFont(font, fontsize * 2);
                textAlign(CENTER, CENTER);
                fill(32, 180, 230);
                int y = (int) (height / 3 + (GraphicData.shape_wifi0.maxy() - GraphicData.shape_wifi3.miny()) * shapesize + fontsize);
                text("MISSING WIFI", width / 2, y);
                textFont(font, fontsize);
                text("waiting for authorisation to harvest anyway", width / 2, y + 2 * fontsize);

                // draw the buttons (always at last to make them visible at all cost)
                buttons_disconnected.draw();

                // react on button status
                int unlocksum = buttons_disconnected.getStatus("unlock0") + buttons_disconnected.getStatus("unlock1") + buttons_disconnected.getStatus("unlock2");
                if (buttons_disconnected.getButton("unlock0").isActivated()) play(R.raw.blackie666__alienbleep);
                if (buttons_disconnected.getButton("unlock1").isActivated()) play(R.raw.blackie666__alienbleep);
                if (buttons_disconnected.getButton("unlock2").isActivated()) play(R.raw.blackie666__alienbleep);
                if (unlocksum == 255 && statusLine.getQueueSize() == 0) {
                    statusLine.clear();
                    statusLine.show("Unlock TWO MORE buttons to start harvesting", 3000);
                }
                if (unlocksum == 255 * 2 && statusLine.getQueueSize() == 0) {
                    statusLine.clear();
                    statusLine.show("Unlock ONE LAST button to start harvesting!", 3000);
                }
                if (unlocksum == 255 * 3) {
                    statusLine.clear();
                    acceptNonWifiConnection = true;
                    buttons_disconnected.getButton("unlock0").setStatus(0,0);
                    buttons_disconnected.getButton("unlock1").setStatus(0,0);
                    buttons_disconnected.getButton("unlock2").setStatus(0,0);
                    buttons_harvesting.getButton("offline").visible();
                }

                randomX = 0;

            } else {

                // ==== SHOW THE HARVESTINGT INFOGRAPHICS ====

                // create blala if nothing else is there
                if (statusLine.getQueueSize() == 0) {
                    switch (random.nextInt(5)) {
                        case 0:
                            statusLine.show("Pending Back-End Queries: " + Harvester.hitsOnBackend, 1000);
                            break;
                        case 1:
                            statusLine.show("Pending Messages for Storage: " + Harvester.pushToBackendAccumulationTimeline.size(), 1000);
                            break;
                        case 2:
                            statusLine.show("Pending Lines: " + Harvester.displayMessages.size(), 1000);
                            break;
                        case 3:
                            statusLine.show("http://loklak.org", 2000);
                            break;
                        case 4:
                            if (Harvester.contribution_message_count > 0)
                                statusLine.show("Stored a total of " + Harvester.contribution_message_count + " messages", 1000);
                    }
                }


                // draw messages
                textFont(font, fontsize);
                textAlign(LEFT, TOP);
                int d = 0;
                vpos += 2 * fontsize;
                for (MessageEntry me : Harvester.displayMessages) {
                    if (vpos > height) break;

                    fill(255, 200, 41);
                    //fill(COLOR_ACCENT);
                    text(me.getCreatedAt().toString() + " from @" + me.getScreenName(), 5, vpos);
                    vpos += fontsize;

                    //fill(COLOR_MAIN);
                    fill(32, 180, 230);
                    text(me.getText(10000, ""), 5, vpos);
                    vpos += fontsize;
                    d++;
                }
                if (d < Harvester.displayMessages.size()) Harvester.reduceDisplayMessages();
                randomX = (randomX + Harvester.displayMessages.size() - d) / 2;

                // at some time load data from the newtork
                if (randomX < 20 && frameCount % FRAME_RATE == 1) {
                    this.getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            Harvester.harvest();
                        }
                    });
                }

                // draw the buttons (always at last to make them visible at all cost)
                buttons_harvesting.draw();

                // react on button status
                if (buttons_harvesting.getStatus("offline") == 255) {
                    acceptNonWifiConnection = false;
                    buttons_harvesting.getButton("offline").setStatus(0,0);
                }
                if (buttons_harvesting.getStatus("terminate") == 255) this.exit();

            }

            //Log.d("Main", "draw time: " + (System.currentTimeMillis() - start) + "ms");
        }

        @Override
        public void mousePressed() {
            if (showsplash) {
                buttons_splash.mousePressed(mouseX, mouseY);
            } else {
                buttons_disconnected.mousePressed(mouseX, mouseY);
                buttons_harvesting.mousePressed(mouseX, mouseY);
            }
        }

        @Override
        public void mouseDragged() {
        }

        public static void play(int soundID) {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                MediaPlayer mp = MediaPlayer.create(context, soundID);
                mp.start();
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        mp.release();
                    }
                });
            }
        }
    }


}
