/**
 *  LogLines
 *  Copyright 13.01.2016 by Michael Peter Christen, @0rb1t3r
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


package org.loklak.android.tools;

import org.loklak.android.data.MessageEntry;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Implements a concurrent limited log line queue.
 * This shall be used to avoid memory leaks when the queue may
 * get too large and information from the queue can be abandoned safely.
 */
public class LogLines<A> implements Iterable<A> {

    public BlockingQueue<A> lines = new LinkedBlockingQueue<A>();

    private final int limit;

    public LogLines(int limit) {
        this.limit = limit;
    }

    public int size() {
        return lines.size();
    }

    public void add(A line) {
        this.lines.add(line);
        while (this.size() > this.limit) this.lines.poll();
    }

    public A poll() {
        return lines.poll();
    }

    public Iterator<A> iterator() {
        return this.lines.iterator();
    }

}
