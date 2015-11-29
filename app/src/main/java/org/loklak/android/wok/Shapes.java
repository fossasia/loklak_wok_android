/**
 *  Shapes
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

import java.util.HashMap;
import java.util.Random;

import processing.core.PApplet;

@TargetApi(11)
public class Shapes extends HashMap<String, Shapes.Line> {

    private final static Random random = new Random(System.currentTimeMillis());

    int minx = 0, miny = 0, maxx = 0, maxy = 0;

    public Shapes() {
    }

    public void normalize() {
        this.minx = Integer.MAX_VALUE;
        this.miny = Integer.MAX_VALUE;
        this.maxx = Integer.MIN_VALUE;
        this.maxy = Integer.MIN_VALUE;
        for (Line s : this.values()) {
            this.minx = Math.min(this.minx, s.minx());
            this.miny = Math.min(this.miny, s.miny());
            this.maxx = Math.max(this.maxx, s.maxx());
            this.maxy = Math.max(this.maxy, s.maxy());
        }
    }

    public void draw(PApplet sketch, String name, int x, int y, float size, int randomX, int randomY) {
        Line s = this.get(name);
        s.draw(sketch, this.minx, this.miny, x, y, size, randomX, randomY);
    }

    public static class Line {

        public int[] outer, inner;

        public Line(int[] outer, int[] inner) {
            assert outer.length % 2 == 0;
            assert inner == null || inner.length % 2 == 0;
            this.inner = inner;
            this.outer = outer;
        }

        private int min(int o, int[] p) {int m = Integer.MAX_VALUE; for (int i = 0; i < p.length; i += 2) m = Math.min(m, p[i]); return m;}
        private int max(int o, int[] p) {int m = Integer.MIN_VALUE; for (int i = 0; i < p.length; i += 2) m = Math.max(m, p[i]); return m;}
        private int min(int o) {return Math.min(min(o, this.outer), this.inner == null ? Integer.MAX_VALUE : min(o, this.inner));}
        private int max(int o) {return Math.max(max(o, this.outer), this.inner == null ? Integer.MIN_VALUE : max(o, this.inner));}

        public int maxx() {return max(0);}
        public int maxy() {return max(1);}
        public int minx() {return min(0);}
        public int miny() {return min(1);}

        public void draw(PApplet pa, int offx, int offy, int x, int y, float size, int randomX, int randomY) {
            draw(pa, offx, offy, x, y, size, this.outer, randomX, randomY);
            if (this.inner != null) draw(pa, offx, offy, x, y, size, this.inner, randomX, randomY);
        }

        private void draw(PApplet pa, int offx, int offy, int x, int y, float size, int[] p, int randomX, int randomY) {
            int x0 = -1, y0 = -1;
            int xn = -1, yn = -1;
            for (int i = 0; i < p.length; i += 2) {
                int xp = x + (int) (size * (p[i] - offx));
                int yp = y + (int) (size * (p[i + 1] - offy));
                if (randomX > 0) { xp += random.nextInt(randomX) - randomX / 2;}
                if (randomY > 0) { yp += random.nextInt(randomY) - randomY / 2;}
                if (i == 0) {
                    x0 = xp; y0 = yp; xn = xp; yn = yp; continue;
                }
                pa.line(xn, yn, xp, yp);
                xn = xp;
                yn = yp;
            }
            pa.line(xn, yn, x0, y0);
        }
    }
}