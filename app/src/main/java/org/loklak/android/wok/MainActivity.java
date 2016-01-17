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
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
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
        int voff = 0; // for smooth line drawing
        boolean showsplash = true;
        boolean wasWifiConnected = true;
        boolean acceptNonWifiConnection = false;
        Buttons buttons_missingwifi, buttons_harvesting, buttons_splash;

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

            // buttons for the splash screen
            buttons_splash = new Buttons(this);
            Buttons.Button button_splash_startapp = buttons_splash.createButton();
            button_splash_startapp
                    .setCenter(width / 2, 5 * height / 6)
                    .setWidth(fontsize * 9)
                    .setFontsize(fontsize * 3 / 2)
                    .setOffText("PUSH", "TO", "START")
                    .setOnText("", "", "")
                    .setBorderWidth(8)
                    .setBorderColor(32, 180, 230)
                    .setOnColor(16, 90, 115)
                    .setOffColor(0, 0, 0)
                    .setTextColor(255, 200, 41)
                    .setTransitionTime(300)
                    .setStatus(0);
            buttons_splash.addButton("startapp", button_splash_startapp);

            // buttons for the missing-wifi screen
            buttons_missingwifi = new Buttons(this);
            Buttons.Button button_missingwifi_unlock0 = (Buttons.Button) button_splash_startapp.clone();
            button_missingwifi_unlock0.setCenter(width / 4, 5 * height / 6).setOffText("PRESS", "TO", "UNLOCK").setOnText("", "UNLOCKED", "");
            Buttons.Button button_missingwifi_unlock1 = (Buttons.Button) button_missingwifi_unlock0.clone();
            button_missingwifi_unlock1.setCenter(width / 2, 5 * height / 6).setStatus(0);
            Buttons.Button button_missingwifi_unlock2 = (Buttons.Button) button_missingwifi_unlock0.clone();
            button_missingwifi_unlock2.setCenter(3 * width / 4, 5 * height / 6).setStatus(0);
            buttons_missingwifi.addButton("unlock0", button_missingwifi_unlock0);
            buttons_missingwifi.addButton("unlock1", button_missingwifi_unlock1);
            buttons_missingwifi.addButton("unlock2", button_missingwifi_unlock2);

            // buttons for the harvesting screen
            buttons_harvesting = new Buttons(this);
            Buttons.Button button_harvesting_terminate = (Buttons.Button) button_missingwifi_unlock0.clone();
            button_harvesting_terminate.setCenter(fontsize, fontsize).setFontsize(fontsize).setWidth(fontsize).setBorderWidth(3).setOffText("", "X", "").setOnText("", "", "").setTextColor(32, 180, 230);
            //buttons_harvesting.addButton("terminate", terminate);
            Buttons.Button button_harvesting_switchtomissingwifi = (Buttons.Button) button_missingwifi_unlock0.clone();
            button_harvesting_switchtomissingwifi.setCenter(width - fontsize, fontsize).setFontsize(fontsize).setWidth(fontsize).setBorderWidth(3).setOffText("", "O", "").setOnText("", "", "").setTextColor(32, 180, 230).invisible();
            //buttons_harvesting.addButton("offline", offline);

            // pre-calculation of shape data
            GraphicData.init(width, height, fontsize);
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
            color_bright_stroke();
            strokeWeight(1);
            GraphicData.headline_outline.draw(this, 0, 2, randomX, randomY);
            int vpos = GraphicData.headline_outline.getMaxY() + fontsize;

            // draw status line
            color_bright_fill();
            textFont(font, fontsize);
            statusLine.setY(vpos);
            statusLine.draw();
            vpos += 2 * fontsize;

            // draw the screen content below the status line according to app situation
            if (showsplash) {

                // ==== SHOW GREETING AND ASK FOR PERMISSION TO HARVETS DATA FOR loklak.org ====
                if (statusLine.getQueueSize() == 0) {
                    statusLine.show("Welcome to the loklak.org data harvesting app", 3000);
                }

                GraphicData.cow_outline.draw(this, 0, 2, 0, 0);

                textFont(font, fontsize * 2);
                textAlign(CENTER, CENTER);
                color_dark_fill();
                int y = GraphicData.cow_outline.getMaxY() + 5 * fontsize;
                text("AGREEMENT", width / 2, y); y += 2 * fontsize;
                textFont(font, fontsize);
                text("This app harvests tweets from twitter", width / 2, y); y += fontsize;
                text("and sends them to loklak.org", width / 2, y); y += fontsize;
                text("Push 'START' to agree", width / 2, y); y += fontsize;

                // draw the buttons (always at last to make them visible at all cost)
                buttons_splash.draw();

                // react on button status
                //if (buttons_splash.getButton("startapp").isActivated()) play(R.raw.blackie666__alienbleep);
                if (buttons_splash.getStatus("startapp") == 255) {
                    showsplash = false;
                    Preferences.setConfig(Preferences.Key.APPGRANTED, true);
                }

                randomX = 0;

            } else if (!harvestEnabled) {

                // ==== SHOW MESSAGE THAT WE DON'T HAVE WIFI; ASK FOR PERMISSION TO HARVEST ANYWAY ====
                if (statusLine.getQueueSize() == 0) {
                    statusLine.show("Harvesting for non-wifi connections disabled", 3000);
                    statusLine.show("Harvesting will resume automatically if re-connected to WIFI", 3000);
                    statusLine.show("Unlock all three buttons to harvest anyway", 3000);
                }

                color_bright_stroke();
                strokeWeight(1);
                int border = (width - GraphicData.wifiShape.maxx) / 2;
                for (int w = 0; w < 5; w++) {
                    GraphicData.wifi_outline.draw(this, 0, 1, 50, 20);
                }
                textFont(font, fontsize * 2);
                textAlign(CENTER, CENTER);
                color_dark_fill();
                vpos = GraphicData.wifi_outline.getMaxY() + 2 * fontsize;
                text("MISSING WIFI", width / 2, vpos);
                textFont(font, fontsize);
                vpos += 2 * fontsize;
                text("harvesting will resume automatically if re-connected to WIFI", width / 2, vpos);
                vpos += fontsize;
                text("waiting for authorisation to harvest anyway", width / 2, vpos);

                // draw the buttons (always at last to make them visible at all cost)
                buttons_missingwifi.draw();

                // react on button status
                int unlocksum = buttons_missingwifi.getStatus("unlock0") + buttons_missingwifi.getStatus("unlock1") + buttons_missingwifi.getStatus("unlock2");
                //if (buttons_missingwifi.getButton("unlock0").isActivated()) play(R.raw.blackie666__alienbleep);
                //if (buttons_missingwifi.getButton("unlock1").isActivated()) play(R.raw.blackie666__alienbleep);
                //if (buttons_missingwifi.getButton("unlock2").isActivated()) play(R.raw.blackie666__alienbleep);
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
                    buttons_missingwifi.getButton("unlock0").setStatus(0,0);
                    buttons_missingwifi.getButton("unlock1").setStatus(0,0);
                    buttons_missingwifi.getButton("unlock2").setStatus(0,0);
                    //buttons_harvesting.getButton("offline").visible();
                }

                randomX = 0;

            } else {

                // ==== SHOW THE HARVESTINGT INFOGRAPHICS ====

                // create blala if nothing else is there
                if (statusLine.getQueueSize() == 0) {
                    switch (random.nextInt(5)) {
                        case 0:
                            if (Harvester.suggestionsOnBackend != 1000)
                                statusLine.show("Pending Back-End Queries: " + Harvester.suggestionsOnBackend, 1000);
                            break;
                        case 1:
                            if (Harvester.pushToBackendAccumulationTimeline.size() != 0)
                                statusLine.show("Pending Messages for Storage: " + Harvester.pushToBackendAccumulationTimeline.size(), 1000);
                            break;
                        case 2:
                            if (Harvester.displayMessages.size() != 0)
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

                // draw statistics
                int w = min(width, height);
                int h = w / 4;
                int d = h / 3 * 4;
                int ccx = width / 2;
                int ccy = vpos + h / 2;

                color_bright_stroke();
                strokeWeight(4);
                noFill();
                arc(ccx, ccy, d, d, 3 * PI / 4, 5 * PI / 4);
                arc(ccx, ccy, d, d, 0, PI / 4);
                arc(ccx, ccy, d, d, 7 * PI / 4, 2 * PI);
                line(0, ccy, ccx - d / 2, ccy);
                line(ccx + d / 2, ccy, width, ccy);
                textAlign(CENTER, CENTER);

                textFont(font, fontsize);
                color_dark_fill();
                text("HARVESTED", ccx, ccy - fontsize - fontsize / 2);
                textFont(font, fontsize * 2);
                color_bright_fill();
                text(Harvester.contribution_message_count == -1 ? "none" : "" + Harvester.contribution_message_count, width / 2, vpos + h / 2);

                textFont(font, fontsize);
                // above left
                color_dark_fill();
                text("QUERIES IN BACKEND",
                        (ccx - d / 2) / 2, ccy - 3 * fontsize);
                color_bright_fill();
                text(Harvester.suggestionsOnBackend,
                        (ccx - d / 2) / 2, ccy - 2 * fontsize);
                // above right
                color_dark_fill();
                text("PENDING LINES",//"QUERIES IN LOKLAK WOK",
                        ccx + d / 2 + (ccx - d / 2) / 2, ccy - 3 * fontsize);
                color_bright_fill();
                text(Harvester.displayMessages.size(), //Harvester.pendingQueries.size(),
                        ccx + d / 2 + (ccx - d / 2) / 2, ccy - 2 * fontsize);
                // below left
                color_dark_fill();
                text("CONTEXT PENDING",
                        (ccx - d / 2) / 2, ccy + 2 * fontsize);
                color_bright_fill();
                text(Harvester.pendingContext.size(),
                        (ccx - d / 2) / 2, ccy + 3 * fontsize);
                // below right
                color_dark_fill();
                text("HARVESTED CONTEXT",
                        ccx + d / 2 + (ccx - d / 2) / 2, ccy + 2 * fontsize);
                color_bright_fill();
                text(Harvester.harvestedContext.size(),
                        ccx + d / 2 + (ccx - d / 2) / 2, ccy + 3 * fontsize);

                vpos += h; // jump to text start

                // draw messages
                textFont(font, fontsize);
                textAlign(LEFT, TOP);
                d = 0;
                for (MessageEntry me : Harvester.displayMessages) {
                    if (vpos + voff > height) break;

                    color_dark_fill();
                    text(me.getCreatedAt().toString() + " from @" + me.getScreenName(), 5, vpos + voff);
                    vpos += fontsize;

                    color_bright_fill();
                    text(me.getText(10000, ""), 5, vpos + voff);
                    vpos += fontsize;
                    d++;
                }
                if (voff <= 0) {
                    Harvester.reduceDisplayMessages();
                    voff += fontsize * 2;
                }
                int ex = Harvester.displayMessages.size() - d;
                voff -= Math.min(fontsize * 2, Math.max(1, ex > 0 ? ex / 2 : 0));

                randomX = (randomX + Harvester.displayMessages.size() - d) / 3;

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
                /*
                if (buttons_harvesting.getStatus("offline") == 255) {
                    acceptNonWifiConnection = false;
                    buttons_harvesting.getButton("offline").setStatus(0,0);
                }
                if (buttons_harvesting.getStatus("terminate") == 255) this.exit();
                */
            }

            //Log.d("Main", "draw time: " + (System.currentTimeMillis() - start) + "ms");
        }

        private void color_bright_stroke() {
            stroke(32, 180, 230);
        }
        private void color_bright_fill() {
            fill(255, 200, 41);
        }
        private void color_dark_fill() {
            fill(32, 180, 230);
        }

        @Override
        public void mousePressed() {
            if (showsplash) {
                buttons_splash.mousePressed(mouseX, mouseY);
            } else {
                buttons_missingwifi.mousePressed(mouseX, mouseY);
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
