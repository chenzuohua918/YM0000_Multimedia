package com.semisky.ym_multimedia.ymbluetooth.EventMsg;

import android.bluetooth.BluetoothDevice;

/**
 * Created by luoyin on 16/10/18.
 */
public class EventPair {
    private BluetoothDevice bluetoothDevice;
    private int type;
    private boolean state;

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
