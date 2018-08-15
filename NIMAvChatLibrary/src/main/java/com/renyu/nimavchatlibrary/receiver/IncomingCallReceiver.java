package com.renyu.nimavchatlibrary.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

/**
 * 来电状态监听
 * <p/>
 * Created by huangjun on 2015/5/13.
 */
public class IncomingCallReceiver extends BroadcastReceiver {

    public enum PhoneCallStateEnum {
        IDLE,           // 空闲
        INCOMING_CALL,  // 有来电
        DIALING_OUT,    // 呼出电话已经接通
        DIALING_IN      // 来电已接通
    }

    public static int phoneState = TelephonyManager.CALL_STATE_IDLE;
    public static PhoneCallStateEnum stateEnum = PhoneCallStateEnum.IDLE;


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
            final String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            onCallStateChanged(state);
        }
    }

    public void onCallStateChanged(String state) {
        IncomingCallReceiver.stateEnum = PhoneCallStateEnum.IDLE;
        if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
            IncomingCallReceiver.phoneState = TelephonyManager.CALL_STATE_IDLE;
            IncomingCallReceiver.stateEnum = PhoneCallStateEnum.IDLE;
        } else if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
            IncomingCallReceiver.phoneState = TelephonyManager.CALL_STATE_RINGING;
            IncomingCallReceiver.stateEnum = PhoneCallStateEnum.INCOMING_CALL;
        } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
            int lastPhoneState = IncomingCallReceiver.phoneState;
            IncomingCallReceiver.phoneState = TelephonyManager.CALL_STATE_OFFHOOK;
            if (lastPhoneState == TelephonyManager.CALL_STATE_IDLE) {
                IncomingCallReceiver.stateEnum = PhoneCallStateEnum.DIALING_OUT;
            } else if (lastPhoneState == TelephonyManager.CALL_STATE_RINGING) {
                IncomingCallReceiver.stateEnum = PhoneCallStateEnum.DIALING_IN;
            }
        }
    }
}
