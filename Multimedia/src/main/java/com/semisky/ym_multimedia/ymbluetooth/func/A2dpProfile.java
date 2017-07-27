/*
 * Copyright (C) 2011 The Android Open Source Project
 * Copyright (C) 2013 Broadcom Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.semisky.ym_multimedia.ymbluetooth.func;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import com.semisky.ym_multimedia.ymbluetooth.tests.BtLogger;

import java.util.ArrayList;
import java.util.List;

public class A2dpProfile {
     private static final String TAG = "funbox:A2dpProfile";
     private static boolean V = true;

     private BluetoothA2dp mA2dpService;
     private BluetoothAdapter mBluetoothAdapter;
     static final String NAME = "A2DP";
     private static final int ORDINAL = 1;

     public A2dpProfile(Context context) {
	mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	mBluetoothAdapter.getProfileProxy(context, new A2dpServiceListener(),BluetoothProfile.A2DP);
     }

     // These callbacks run on the main thread.
     private final class A2dpServiceListener implements BluetoothProfile.ServiceListener {
	@Override
	public void onServiceConnected(int profile, BluetoothProfile proxy) {
		// TODO Auto-generated method stub
                BtLogger.d(TAG, "onServiceConnected");
		if (profile == BluetoothProfile.A2DP) {
			mA2dpService = (BluetoothA2dp) proxy;
		}
	}

	@Override
	public void onServiceDisconnected(int arg0) {
		// TODO Auto-generated method stub
                BtLogger.d(TAG, "onServiceDisconnected");
		mA2dpService = null;
	}
    }
    public List<BluetoothDevice> getConnectedDevices() {
        if (mA2dpService == null) return new ArrayList<BluetoothDevice>(0);
        return mA2dpService.getDevicesMatchingConnectionStates(
              new int[] {BluetoothProfile.STATE_CONNECTED,
                         BluetoothProfile.STATE_CONNECTING,
                         BluetoothProfile.STATE_DISCONNECTING});
    }
    public boolean connect(BluetoothDevice device) {
        BtLogger.d(TAG, "-->connect");
        if (mA2dpService == null) return false;
        mA2dpService.setPriority(device, BluetoothProfile.PRIORITY_ON);
        List<BluetoothDevice> sinks = getConnectedDevices();
        if (sinks != null) {
            for (BluetoothDevice sink : sinks) {
                mA2dpService.disconnect(sink);
            }
        }
        return mA2dpService.connect(device);
    }

    public boolean disconnect(BluetoothDevice device) {
        BtLogger.d(TAG, "-->disconnectA2dp");
        if (mA2dpService == null) return false;
        // Downgrade priority as user is disconnecting the headset.
        //if (mA2dpService.getPriority(device) > BluetoothProfile.PRIORITY_ON){
            mA2dpService.setPriority(device, BluetoothProfile.PRIORITY_OFF);
        //}
        return mA2dpService.disconnect(device);
    }

    public int getConnectionStatus(BluetoothDevice device) {
        if (mA2dpService == null) {
            return BluetoothProfile.STATE_DISCONNECTED;
        }
        return mA2dpService.getConnectionState(device);
    }

    public boolean isPreferred(BluetoothDevice device) {
        BtLogger.d(TAG, "-->isPreferred");
        if (mA2dpService == null) return false;
        return mA2dpService.getPriority(device) > BluetoothProfile.PRIORITY_OFF;
    }

    public int getPreferred(BluetoothDevice device) {
        BtLogger.d(TAG, "-->getPreferred");
        if (mA2dpService == null) return BluetoothProfile.PRIORITY_OFF;
        return mA2dpService.getPriority(device);
    }

    public void setPreferred(BluetoothDevice device, boolean preferred) {
        BtLogger.d(TAG, "-->setPreferred preferred = "+preferred);
        if (mA2dpService == null) return;
        if (preferred) {
            if (mA2dpService.getPriority(device) < BluetoothProfile.PRIORITY_ON) {
                mA2dpService.setPriority(device, BluetoothProfile.PRIORITY_ON);
            }
        } else {
            mA2dpService.setPriority(device, BluetoothProfile.PRIORITY_OFF);
        }
    }
    boolean isA2dpPlaying() {
        BtLogger.d(TAG, "-->isA2dpPlaying");
        if (mA2dpService == null) return false;
        List<BluetoothDevice> sinks = mA2dpService.getConnectedDevices();
        if (!sinks.isEmpty()) {
            if (mA2dpService.isA2dpPlaying(sinks.get(0))) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return NAME;
    }

    public int getOrdinal() {
        return ORDINAL;
    }
        
    public int getConnectionA2dpState(BluetoothDevice device) {
            int a2dpState=0;
            try { 
                a2dpState = mA2dpService.getConnectionState(device);
	    } catch (final Exception e) {
            }
            BtLogger.d(TAG,"getConnectionA2dpState a2dpState="+a2dpState);
            return a2dpState;
     }
     public boolean isA2dpPlaying(BluetoothDevice device) {
            BtLogger.d(TAG,"isA2dpPlaying");
            try { 
                return mA2dpService.isA2dpPlaying(device);
	    } catch (final Exception e) {
            }
            return false;
     }
}
