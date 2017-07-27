package com.semisky.ym_multimedia.ymbluetooth.EventMsg;

import com.broadcom.bt.hfdevice.BluetoothClccInfo;

import java.util.List;

/**
 * Created by luoyin on 16/10/18.
 */
public class EventBTService {
    private int method;
    private int callType;
    private String number;
    private int numberType;
    private String callState;
    private boolean privateMode;
    private List<BluetoothClccInfo> clcc;

    public List<BluetoothClccInfo> getClcc() {
        return clcc;
    }

    public void setClcc(List<BluetoothClccInfo> clcc) {
        this.clcc = clcc;
    }

    public boolean isPrivateMode() {
        return privateMode;
    }

    public void setPrivateMode(boolean privateMode) {
        this.privateMode = privateMode;
    }

    public int getMethod() {
        return method;
    }

    public void setMethod(int method) {
        this.method = method;
    }

    public int getCallType() {
        return callType;
    }

    public void setCallType(int callType) {
        this.callType = callType;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCallState() {
        return callState;
    }

    public void setCallState(String callState) {
        this.callState = callState;
    }

    public int getNumberType() {
        return numberType;
    }

    public void setNumberType(int numberType) {
        this.numberType = numberType;
    }
}
