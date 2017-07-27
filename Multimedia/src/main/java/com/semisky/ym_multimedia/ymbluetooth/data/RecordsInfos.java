package com.semisky.ym_multimedia.ymbluetooth.data;

/**
 * 通话记录的工具类
 * Created by DaiShiHao on 2015/1/16 0016.
 */
public class RecordsInfos {

    private int recordsType;
    private String recordsNum;
    private String recordsName;
    private String recordsDate;
    private String recordsTimeLength;

    public int getRecordsType() {
        return recordsType;
    }

    public void setRecordsType(int recordsType) {
        this.recordsType = recordsType;
    }

    public String getRecordsTimeLength() {
        return recordsTimeLength;
    }

    public void setRecordsTimeLength(String recordsTimeLength) {
        this.recordsTimeLength = recordsTimeLength;
    }

    public String getRecordsDate() {
        return recordsDate;
    }

    public void setRecordsDate(String recordsDate) {
        this.recordsDate = recordsDate;
    }

    public String getRecordsNum() {
        return recordsNum;
    }

    public void setRecordsNum(String recordsNum) {
        this.recordsNum = recordsNum;
    }
    public String getRecordsName() {
        return recordsName;
    }

    public void setRecordsName(String recordsName) {
        this.recordsName = recordsName;
    }
}
