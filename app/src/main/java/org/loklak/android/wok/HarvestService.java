/**
 *  HarvestService
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

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class HarvestService extends IntentService {

    public HarvestService() {
        // method is required to keep the xml verifier for AndroidManifest silent
        super("harvester");
    }

    public HarvestService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("HarvestService", "onHandleIntent intent = " + intent);
        while (true) {
            if (Preferences.getConfig(Preferences.Key.APPGRANTED, false) && MainActivity.isConnectedWifi()) {
                //Log.d("HarvestService", "onHandleIntent " + intent + (MainActivity.sketch.canDraw() ? ", app canDraw" : ""));
                Harvester.harvest();
            }
            SystemClock.sleep(MainActivity.sketch.canDraw() ? 1000 : 10000);
        }
    }

    @Override
    public void onDestroy() {
        Log.d("HarvestService", "destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("HarvestService", "onBind intend = " + intent);
        return mBinder;
    }

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        HarvestService getService() {
            return HarvestService.this;
        }
    }

}