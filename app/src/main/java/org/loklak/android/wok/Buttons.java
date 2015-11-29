/**
 *  Buttons
 *  Copyright 28.11.2015 by Michael Peter Christen, @0rb1t3r
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

import java.util.LinkedHashMap;

import processing.core.PApplet;

@TargetApi(11)
public class Buttons {

    private PApplet sketch;
    private LinkedHashMap<String, Button> buttons;

    public Buttons(PApplet sketch) {
        this.sketch = sketch;
        this.buttons = new LinkedHashMap<String, Button>();
    }

    public Button createButton() {
        return new Button(this.sketch);
    }

    public void addButton(String name, Button button) {
        this.buttons.put(name, button);
    }

    public Button getButton(String name) {
        return this.buttons.get(name);
    }

    public int getStatus(String name) {
        Button button = this.buttons.get(name);
        return button == null ? -1 : button.getStatus();
    }

    public void draw() {
        for (Button button: this.buttons.values()) button.draw();
    }

    public void mousePressed(int x, int y) {
        for (Button button: this.buttons.values()) button.mousePressed(x, y);
    }

    public static class Button {
        private PApplet sketch;
        private int x, y, fontsize = 24, width = 5 * fontsize;
        private int tt = 500;
        private int borderWidth = 1;
        private int col_r_text = 0, col_g_text  = 0, col_b_text = 0;
        private int col_r_border = 0, col_g_border = 0, col_b_border = 0;
        private int col_r_on = 0, col_g_on = 255, col_b_on = 0;
        private int col_r_off = 255, col_g_off = 0, col_b_off = 0;
        private int col_r_disabled = 128, col_g_disabled = 128, col_b_disabled = 128;
        private String offtext0 = "", offtext1 = "SWITCH ON", offtext2 = "";
        private String ontext0 = "", ontext1 = "SWITCH OFF", ontext2 = "";
        private int status, sign;
        private boolean disabled, visible;
        private boolean activated, deactivated;

        private Button(PApplet sketch) {
            this.sketch = sketch;
            this.status = 0; this.sign = 0;
            this.visible = true;
            this.disabled = false;
            this.activated = false;
            this.deactivated = false;
        }

        public Object clone() {
            Button b = new Button(this.sketch);
            b.x = this.x; b.y = this.y; b.tt = this.tt; b.fontsize = this.fontsize; b.width = this.width;
            b.borderWidth = this.borderWidth;
            b.col_r_text = this.col_r_text; b.col_g_text = this.col_g_text; b.col_b_text = this.col_b_text;
            b.col_r_border = this.col_r_border; b.col_g_border = this.col_g_border; b.col_b_border = this.col_b_border;
            b.col_r_on = this.col_r_on; b.col_g_on = this.col_g_on; b.col_b_on = this.col_b_on;
            b.col_r_off = this.col_r_off; b.col_g_off = this.col_g_off; b.col_b_off = this.col_b_off;
            b.col_r_disabled = this.col_r_disabled; b.col_g_disabled = this.col_g_disabled; b.col_b_disabled = this.col_b_disabled;
            b.ontext0 = this.ontext0; b.ontext1 = this.ontext1; b.ontext2 = this.ontext2;
            b.offtext0 = this.offtext0; b.offtext1 = this.offtext1; b.offtext2 = this.offtext2;
            return b;
        }

        public Button setCenter(int x, int y) {
            this.x = x; this.y = y;
            return this;
        }

        public Button setWidth(int width) {
            this.width = width;
            return this;
        }

        public Button visible() {
            this.visible = true;
            return this;
        }

        public Button invisible() {
            this.visible = false;
            return this;
        }

        public boolean isVisible() {
            return this.visible;
        }

        /**
         * checks if the button was pushed while status was off
         * This will return 'true' only once while the button is animated to full 'on' state
         * @return true iff the button was activated and the method was not called before during activation
         */
        public boolean isActivated() {
            boolean r = this.activated;
            activated = false;
            return r;
        }

        /**
         * checks if the button was pushed while status was on
         * This will return 'true' only once while the button is animated to full 'off' state
         * @return true iff the button was deactivated and the method was not called before during deactivation
         */
        public boolean isDeactivated() {
            boolean r = this.deactivated;
            this.deactivated = false;
            return r;
        }

        public Button disable() {
            this.disabled = true;
            return this;
        }

        public Button enable() {
            this.disabled = false;
            return this;
        }

        public boolean isEnabled() {
            return !this.disabled;
        }

        public Button setTransitionTime(int milliseconds) {
            this.tt = milliseconds;
            return this;
        }

        public Button setBorderWidth(int borderWidth) {
            this.borderWidth = borderWidth;
            return this;
        }

        public Button setOnColor(int r, int g, int b) {
            this.col_r_on = r; this.col_g_on = g; this.col_b_on = b;
            return this;
        }

        public Button setOffColor(int r, int g, int b) {
            this.col_r_off = r; this.col_g_off = g; this.col_b_off = b;
            return this;
        }

        public Button setDisabledColor(int r, int g, int b) {
            this.col_r_disabled = r; this.col_g_disabled = g; this.col_b_disabled = b;
            return this;
        }

        public Button setBorderColor(int r, int g, int b) {
            this.col_r_border = r; this.col_g_border = g; this.col_b_border = b;
            return this;
        }

        public Button setTextColor(int r, int g, int b) {
            this.col_r_text = r; this.col_g_text = g; this.col_b_text = b;
            return this;
        }

        public Button setOnText(String text0, String text1, String text2) {
            this.ontext0 = text0; this.ontext1 = text1; this.ontext2 = text2;
            return this;
        }

        public Button setOffText(String text0, String text1, String text2) {
            this.offtext0 = text0;; this.offtext1 = text1; this.offtext2 = text2;
            return this;
        }

        public Button setFontsize(int fontsize) {
            this.fontsize = fontsize;
            return this;
        }

        public void draw() {
            // change sign
            if (this.sign != 0) {
                this.status = Math.max(0, Math.min(255, this.status + (int) (this.sign * 255000 / this.tt / this.sketch.frameRate)));
                if (this.status == 0 || this.status == 255) {
                    this.sign = 0;
                }
            }
            if (this.visible) {
                // draw button
                if (borderWidth == 0) sketch.noStroke();
                else {
                    sketch.stroke(col_r_border, col_g_border, col_b_border);
                    sketch.strokeWeight(borderWidth);
                }
                if (this.sign == 0 || this.disabled) {
                    // display button final status
                    if (this.disabled) {
                        sketch.fill(col_r_disabled, col_g_disabled, col_b_disabled);
                    } else if (this.status == 0) {
                        sketch.fill(col_r_off, col_g_off, col_b_off);
                    } else if (this.status == 255) {
                        sketch.fill(col_r_on, col_g_on, col_b_on);
                    }
                    sketch.arc(x, y, width, width, 0, sketch.TWO_PI);

                    // draw text lines
                    sketch.fill(col_r_text, col_g_text, col_b_text);
                    sketch.textSize(fontsize);
                    sketch.textAlign(sketch.CENTER, sketch.CENTER);
                    if (this.status == 0) {
                        sketch.text(offtext0, x, y - fontsize);
                        sketch.text(offtext1, x, y);
                        sketch.text(offtext2, x, y + fontsize);
                    } else if (this.status == 255) {
                        sketch.text(ontext0, x, y - fontsize);
                        sketch.text(ontext1, x, y);
                        sketch.text(ontext2, x, y + fontsize);
                    }
                } else {
                    // transition animation
                    sketch.fill(col_r_off, col_g_off, col_b_off);
                    sketch.arc(x, y, width, width, 0, sketch.TWO_PI);
                    sketch.noStroke();
                    sketch.fill(col_r_on, col_g_on, col_b_on);
                    sketch.arc(x, y, width * status / 255, width * status / 255, 0, sketch.TWO_PI);
                }
            }
        }

        public Button setStatus(int status) {
            this.status = status; this.sign = 0;
            return this;
        }

        public Button setStatus(int status, int sign) {
            this.status = status; this.sign = sign;
            return this;
        }

        /**
         * Get the button status. The status is a number from 0..255 where 0 means off and 255 means on.
         * There can be numbers in between which means that the button is currently is changing its status
         * @return the status
         */
        public int getStatus() {
            return this.status;
        }

        public boolean inside(int x, int y) {
            // compute the distance from center
            int xd = this.x - x;
            int yd = this.y - y;
            int distance = (int) Math.sqrt(xd * xd + yd * yd); // pythagoras!
            return distance * 2 < this.width;
        }

        public void mousePressed(int x, int y) {
            if (this.visible && !this.disabled && inside(x, y)) {
                if (this.sign == 0) {
                    if (this.status == 0) {
                        this.sign = 1;
                        this.activated = true;
                        this.deactivated = false;
                    } else {
                        this.sign = -1;
                        this.activated = false;
                        this.deactivated = true;
                    }
                } else {
                    this.sign = -this.sign;
                }
            }
        }

    }

}
