/**
 *  OutlineDrawing
 *  Copyright 07.12.2015 by Michael Peter Christen, @0rb1t3r
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import processing.core.PApplet;

/**
 * An OutlineDrawing is a set of lines which can be animated.
 * The animation is done in the form of a morph from a start position
 * of each stroke of the drawing lines to a destination position.
 */
public class OutlineDrawing {

    private final static Random random = new Random(System.currentTimeMillis());

    private List<StrokeMorph> strokes;
    private int morphsteps;
    private int minx, maxx, miny, maxy;

    public OutlineDrawing() {
        this.strokes = new ArrayList<StrokeMorph>();
        this.morphsteps = -1;
        this.minx = Integer.MAX_VALUE;
        this.maxx = Integer.MIN_VALUE;
        this.miny = Integer.MAX_VALUE;
        this.maxy = Integer.MIN_VALUE;
    }

    public void add(Shapes.Line line, int x, int y, boolean closedShape) {
        add(x, y, line.outer, closedShape);
        if (line.inner != null) add(x, y, line.inner, closedShape);
    }

    private void add(int x, int y, int[] p, boolean closedShape) {
        int x0 = -1, y0 = -1;
        int xn = -1, yn = -1;
        for (int i = 0; i < p.length; i += 2) {
            int xp = x + p[i];
            int yp = y + p[i + 1];
            this.minx = Math.min(this.minx, xp);
            this.maxx = Math.max(this.maxx, xp);
            this.miny = Math.min(this.miny, yp);
            this.maxy = Math.max(this.maxy, yp);
            if (i == 0) {
                x0 = xp; y0 = yp; xn = xp; yn = yp; continue;
            }
            this.strokes.add(new StrokeMorph(new Stroke(xn, yn, xp, yp)));
            xn = xp;
            yn = yp;
        }
        if (closedShape) this.strokes.add(new StrokeMorph(new Stroke(xn, yn, x0, y0)));
    }

    public void morph(OutlineDrawing destination, int steps) {
        this.morphsteps = steps;
    }

    public void draw(PApplet pa, int step, int strength) {
        for (StrokeMorph stroke: this.strokes) {
            stroke.draw(pa, step, strength, 0, 0);
        }
    }

    public void draw(PApplet pa, int step, int strength, int randomX, int randomY) {
        for (StrokeMorph stroke: this.strokes) {
            stroke.draw(pa, step, strength, randomX, randomY);
        }
    }

    public int getMinX() {
        return minx;
    }

    public int getMaxX() {
        return maxx;
    }

    public int getMinY() {
        return miny;
    }

    public int getMaxY() {
        return maxy;
    }

    /**
     * a StrokeMorph is a line which moves from a start position
     * to a destination in a given time and speed
     */
    public class StrokeMorph {

        private Stroke s0, s1;
        private boolean genuine; // non-genuine strokes are invisible at step 0 and at final step

        public StrokeMorph(Stroke stroke) {
            this.s0 = stroke;
            this.s1 = null;
            this.genuine = true;
        }

        public StrokeMorph destination(Stroke stroke) {
            this.s1 = stroke;
            return this;
        }

        public StrokeMorph setGenuine(boolean genuine) {
            this.genuine = genuine;
            return this;
        }

        public void draw(PApplet pa, int step, int strength) {
            draw(pa, step, strength, 0, 0);
        }

        public void draw(PApplet pa, int step, int strength, int randomX, int randomY) {
            int x0, x1, y0, y1;
            if (step == 0 || s1 == null || morphsteps == -1) {
                if (!genuine) return;
                x0 = s0.x0; y0 = s0.y0; x1 = s0.x1; y1 = s0.y1;
            } else if (step >= morphsteps) {
                if (!genuine) return;
                x0 = s1.x0; y0 = s1.y0; x1 = s1.x1; y1 = s1.y1;
            } else {
                // naive algorithm which does not compute a straight line
                // TODO: compute position on the straight line between the points
                int xd0 = s1.x0 - s0.x0;
                int xd1 = s1.x1 - s0.x1;
                int yd0 = s1.y0 - s0.y0;
                int yd1 = s1.y1 - s0.y1;
                x0 = s0.x0 + step * xd0 / morphsteps;
                x1 = s0.x1 + step * xd1 / morphsteps;
                y0 = s0.y0 + step * yd0 / morphsteps;
                y1 = s0.y1 + step * yd1 / morphsteps;
            }
            if (randomX > 0) {
                x0 += random.nextInt(randomX) - randomX / 2;
                x1 += random.nextInt(randomX) - randomX / 2;
            }
            if (randomY > 0) {
                y0 += random.nextInt(randomY) - randomY / 2;
                y1 += random.nextInt(randomY) - randomY / 2;
            }
            pa.line(x0, y0, x1, y1);
            if (strength > 1) {
                for (int i = -strength; i <= strength; i++) {
                    if (i == 0) continue;
                    pa.line(x0 + i, y0, x1 + i, y1);
                    pa.line(x0, y0 + i, x1, y1 + i);
                }
            }
        }

    }


    /**
     * a stroke is a line between two points
     */
    public static class Stroke {
        public int x0, x1, y0, y1;
        public Stroke(int fromx, int fromy, int tox, int toy) {
            this.x0 = fromx;
            this.y0 = fromy;
            this.x1 = tox;
            this.y1 = toy;
        }

        public int distance(Stroke other) {
            int xd = x0 - x1;
            int yd = y0 - y1;
            return xd * xd + yd * yd;
        }

        public void draw(PApplet sketch) {
            sketch.line(x0, y0, x1, y1);
        }
    }

}
