/**
 *  StatusLine
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

import java.util.concurrent.LinkedBlockingQueue;

import processing.core.PApplet;

@TargetApi(11)
public class StatusLine {

    private static char FULL_BLOCK = (char) 0x25A0;
    private static char SPACE = (char) 32;

    private PApplet pa;
    private int y, fontsize, r, g, b;
    private LinkedBlockingQueue<Status> messages;
    private int letters;
    private long completeTime;

    private static class Status {
        public String message;
        public int time;
        public long start;
        public Status(String message, int time) {
            this.message = message; this.time = time; this.start = 0;
        }
    }

    public StatusLine(PApplet pa, int fontsize, int r, int g, int b) {
        this.pa = pa; this.y = 0; this.fontsize = fontsize; this.r = r; this.g = g; this.b = b;
        this.messages = new LinkedBlockingQueue<Status>();
        this.letters = -1;
        this.completeTime = -1;
    }

    public void clear() {
        this.messages.clear();
    }

    public int getQueueSize() {
        return messages.size();
    }

    public void setY(int y) {
        this.y = y;
    }

    public void show(String message, int millis) {
        try {
            if (this.messages.size() > 1) for (Status status: messages) status.time = status.time / 2;
            this.messages.put(new Status(message, Math.max(4000, millis)));
        } catch (InterruptedException e) {}
    }

    public void draw() {
        while (this.messages.size() > 0) {
            Status currentStatus = messages.peek();
            long now = System.currentTimeMillis();
            if (currentStatus.start == 0) currentStatus.start = now;
            int p = (100 * ((int) (now - currentStatus.start))) / currentStatus.time;
            if (p >= 100) {
                messages.poll();
                continue;
            }
            int charsInMessage = currentStatus.message.length();
            int shortened = p < 50 ? charsInMessage * p / 50 : charsInMessage;

            StringBuilder x = new StringBuilder(currentStatus.message);
            if (shortened < charsInMessage) {
                // add cursor in between and remove remaining chars
                x.setCharAt(shortened, FULL_BLOCK);
                for (int q = shortened + 1; q < charsInMessage; q++) x.setCharAt(q, SPACE);
            }

            pa.textAlign(pa.CENTER, pa.TOP);
            pa.text(x.toString(), pa.width / 2, this.y);
            break;
        }
    }
}
