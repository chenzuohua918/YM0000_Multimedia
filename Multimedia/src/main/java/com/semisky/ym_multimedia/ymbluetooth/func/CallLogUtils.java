/******************************************************************************
 *
 *  Copyright (C) 2009-2013 Broadcom Corporation
 *
 *  This program is the proprietary software of Broadcom Corporation and/or its
 *  licensors, and may only be used, duplicated, modified or distributed
 *  pursuant to the terms and conditions of a separate, written license
 *  agreement executed between you and Broadcom (an "Authorized License").
 *  Except as set forth in an Authorized License, Broadcom grants no license
 *  (express or implied), right to use, or waiver of any kind with respect to
 *  the Software, and Broadcom expressly reserves all rights in and to the
 *  Software and all intellectual property rights therein.
 *  IF YOU HAVE NO AUTHORIZED LICENSE, THEN YOU HAVE NO RIGHT TO USE THIS
 *  SOFTWARE IN ANY WAY, AND SHOULD IMMEDIATELY NOTIFY BROADCOM AND DISCONTINUE
 *  ALL USE OF THE SOFTWARE.
 *
 *  Except as expressly set forth in the Authorized License,
 *
 *  1.     This program, including its structure, sequence and organization,
 *         constitutes the valuable trade secrets of Broadcom, and you shall
 *         use all reasonable efforts to protect the confidentiality thereof,
 *         and to use this information only in connection with your use of
 *         Broadcom integrated circuit products.
 *
 *  2.     TO THE MAXIMUM EXTENT PERMITTED BY LAW, THE SOFTWARE IS PROVIDED
 *         "AS IS" AND WITH ALL FAULTS AND BROADCOM MAKES NO PROMISES,
 *         REPRESENTATIONS OR WARRANTIES, EITHER EXPRESS, IMPLIED, STATUTORY,
 *         OR OTHERWISE, WITH RESPECT TO THE SOFTWARE.  BROADCOM SPECIFICALLY
 *         DISCLAIMS ANY AND ALL IMPLIED WARRANTIES OF TITLE, MERCHANTABILITY,
 *         NONINFRINGEMENT, FITNESS FOR A PARTICULAR PURPOSE, LACK OF VIRUSES,
 *         ACCURACY OR COMPLETENESS, QUIET ENJOYMENT, QUIET POSSESSION OR
 *         CORRESPONDENCE TO DESCRIPTION. YOU ASSUME THE ENTIRE RISK ARISING
 *         OUT OF USE OR PERFORMANCE OF THE SOFTWARE.
 *
 *  3.     TO THE MAXIMUM EXTENT PERMITTED BY LAW, IN NO EVENT SHALL BROADCOM
 *         OR ITS LICENSORS BE LIABLE FOR
 *         (i)   CONSEQUENTIAL, INCIDENTAL, SPECIAL, INDIRECT, OR EXEMPLARY
 *               DAMAGES WHATSOEVER ARISING OUT OF OR IN ANY WAY RELATING TO
 *               YOUR USE OF OR INABILITY TO USE THE SOFTWARE EVEN IF BROADCOM
 *               HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES; OR
 *         (ii)  ANY AMOUNT IN EXCESS OF THE AMOUNT ACTUALLY PAID FOR THE
 *               SOFTWARE ITSELF OR U.S. $1, WHICHEVER IS GREATER. THESE
 *               LIMITATIONS SHALL APPLY NOTWITHSTANDING ANY FAILURE OF
 *               ESSENTIAL PURPOSE OF ANY LIMITED REMEDY.
 *
 *****************************************************************************/

package com.semisky.ym_multimedia.ymbluetooth.func;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.CallLog;
import com.semisky.ym_multimedia.ymbluetooth.tests.BtLogger;

import com.android.vcard.VCardConstants;
import com.android.vcard.VCardEntryConstructor;
import com.android.vcard.VCardEntryCounter;
import com.android.vcard.VCardInterpreter;
import com.android.vcard.VCardParser;
import com.android.vcard.VCardParser_V21;
import com.android.vcard.VCardParser_V30;
import com.android.vcard.VCardProperty;
import com.android.vcard.VCardSourceDetector;
import com.android.vcard.exception.VCardException;
import com.android.vcard.exception.VCardNestedException;
import com.android.vcard.exception.VCardNotSupportedException;
import com.android.vcard.exception.VCardVersionException;
import com.semisky.ym_multimedia.ymbluetooth.data.CallLogRecords;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CallLogUtils extends VCardEntryConstructor {

    private static String TAG = "CallLogUtils";

    /* package */final static int VCARD_VERSION_V21 = 1;

    /* package */final static int VCARD_VERSION_V30 = 2;

    private VCardParser mVCardParser;

    private ContentResolver mResolver;

    private CallLogRecords mCallLogRecords;
    private List<CallLogRecords> mAllCallLogRecordsList = new ArrayList<CallLogRecords>();
    private Context mContext;
    private FuncBTOperate mFuncBTOperate;

    private void addCallLogRecordsToList(){
        if(null != mCallLogRecords){
            mAllCallLogRecordsList.add(mCallLogRecords);
        }
    }

    public boolean addVCFtoCallLog(Context context, String pathToVcf)
            throws IOException, VCardException {

        VCardEntryCounter counter = null;
        VCardSourceDetector detector = null;

        if (context == null) {
            BtLogger.e(TAG, "invalid context provided");
            return false;
        }
        mContext = context;
        mResolver = context.getContentResolver();
        mFuncBTOperate = FuncBTOperate.getInstance(mContext);
        BtLogger.d(TAG, "mResolver"+mResolver);
//        mVcfCallType = vcfCallType;
        int vcardVersion = VCARD_VERSION_V21;
        try {
            boolean shouldUseV30 = false;
            InputStream is;
            try {
                is = new FileInputStream(pathToVcf);
            } catch (FileNotFoundException fnf) {
                fnf.printStackTrace();
                return false;
            }

            mVCardParser = new VCardParser_V21();

            try {
                counter = new VCardEntryCounter();
                detector = new VCardSourceDetector();
                mVCardParser.addInterpreter(counter);
                mVCardParser.addInterpreter(detector);
                mVCardParser.parse(is);
                BtLogger.d(TAG, "VCardParser_V21");
            } catch (VCardVersionException e1) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                is = new FileInputStream(pathToVcf);

                shouldUseV30 = true;
                mVCardParser = new VCardParser_V30();
                try {
                    counter = new VCardEntryCounter();
                    detector = new VCardSourceDetector();
                    mVCardParser.addInterpreter(counter);
                    mVCardParser.addInterpreter(detector);
                    mVCardParser.parse(is);
                    BtLogger.d(TAG, "VCardParser_V30");
                } catch (VCardVersionException e2) {
                    throw new VCardException("vCard with unspported version.");
                }
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
            }

            vcardVersion = shouldUseV30 ? VCARD_VERSION_V30 : VCARD_VERSION_V21;
        } catch (VCardNestedException e) {
            BtLogger.w(TAG, "Nested Exception is found (it may be false-positive).");
            // Go through without throwing the Exception, as we may be able to
            // detect the version before it
        }

        // Now, we can actually parse
        BtLogger.d(TAG, "VCardEntryCounter count:" + counter.getCount());

        InputStream ins = new FileInputStream(pathToVcf);

        return readOneVCard(ins, detector.getEstimatedType(), detector.getEstimatedCharset(),
                vcardVersion, this);
    }

    @Override
    public void onVCardStarted() {
         BtLogger.d(TAG, "onVCardStarted");
    }

    @Override
    public void onVCardEnded() {
         BtLogger.d(TAG, "onVCardEnded");
    }

    @Override
    public void onEntryStarted() {
         BtLogger.d(TAG, "onEntryStarted");
        mCallLogRecords = new CallLogRecords();
    }

    @Override
    public void onEntryEnded() {
        BtLogger.d(TAG, "添加重写onEntryEnded");
        addCallLogRecordsToList();
    }

    @Override
    public void onPropertyCreated(VCardProperty property) {

        String prop = property.getName();
//        BtLogger.d(TAG, "接收onPropertyCreated prop:" + prop + " value:" + property.getRawValue());
        if (prop.equalsIgnoreCase(VCardConstants.PROPERTY_FN)) {
            mCallLogRecords.setName(property.getRawValue());
        } else if (prop.equalsIgnoreCase(VCardConstants.PROPERTY_TEL)) {
            mCallLogRecords.setNumber(property.getRawValue());
        } else if (prop.equalsIgnoreCase(VCardConstants.PROPERTY_EMAIL)) {
        } else if (prop.equalsIgnoreCase(VCardConstants.PROPERTY_SORT_STRING)) {
        } else if (prop.equalsIgnoreCase("X-IRMC-CALL-DATETIME")) {
            try {
                String dateTimeVal = property.getRawValue();
//                Log.i(TAG, "时间日期dateTimeVal :" + dateTimeVal);
                String dateTime = dateTimeVal.replaceAll("T", "");
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                Date date = formatter.parse(dateTime);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                mCallLogRecords.setDateTime(cal.getTimeInMillis());
                // Map the type in vcf to calllog type
                Map<String, Collection<String>> propertyParameterMap = property.getParameterMap();
                for (String paramType : propertyParameterMap.keySet()) {
                    Collection<String> paramValueList = propertyParameterMap.get(paramType);
                    for (String paramValue : paramValueList) {
                        //BtLogger.d(TAG, "paramValue: " + paramValue);
                        if (paramValue.equalsIgnoreCase("DIALED")){
                            mCallLogRecords.setType(CallLog.Calls.OUTGOING_TYPE);
                        } else if (paramValue.equalsIgnoreCase("RECEIVED")){
                            mCallLogRecords.setType(CallLog.Calls.INCOMING_TYPE);
                        } else if (paramValue.equalsIgnoreCase("MISSED")){
                            mCallLogRecords.setType(CallLog.Calls.MISSED_TYPE);
                        }
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean readOneVCard(InputStream is, int vcardType, String charset, int vcardVersion,
            final VCardInterpreter interpreter) {
        boolean successful = false;
        try {
            if (interpreter instanceof VCardEntryConstructor) {
                // Let the object clean up internal temporary objects,
                ((VCardEntryConstructor)interpreter).clear();
            }

            synchronized (this) {
                mVCardParser = (vcardVersion == VCARD_VERSION_V30 ? new VCardParser_V30(vcardType)
                : new VCardParser_V21(vcardType));
            }
            mVCardParser.parse(is, interpreter);
            successful = true;
            BtLogger.e(TAG, "读成功通话记录readOneVCard:successful=" + successful);
            mFuncBTOperate.saveCallLogRecords(mAllCallLogRecordsList);
            //同步通话记录界面
            mFuncBTOperate.notifyCallLogList(0);
            //同步完通话记录，然后再等待联系人同步完成
//            FuncBTOperate.getInstance(mContext).notifyBTService(14);
        } catch (IOException e) {
            BtLogger.e(TAG, "IOException was emitted: " + e.getMessage());
        } catch (VCardNestedException e) {
            BtLogger.e(TAG, "Nested Exception is found.");
        } catch (VCardNotSupportedException e) {
            BtLogger.e(TAG, e.toString());
        } catch (VCardVersionException e) {
            BtLogger.e(TAG, "Appropriate version for this vCard is not found.");
        } catch (VCardException e) {
            BtLogger.e(TAG, e.toString());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return successful;
    }
}
