package com.renyu.nimavchatlibrary.params;

/**
 * Created by hzxuwen on 2015/4/24.
 */
public class AVChatExitCode {
    public static final int PEER_HANGUP = 0;

    public static final int PEER_REJECT = 1;

    public static final int HANGUP = 2;

    public static final int NET_CHANGE = 4;

    public static final int REJECT = 5;

    public static final int PEER_BUSY = 6;

    public static final int NET_ERROR = 8;

    public static final int KICKED_OUT = 9;

    public static final int CONFIG_ERROR = 10;

    public static final int PROTOCOL_INCOMPATIBLE_SELF_LOWER = 12;

    public static final int PROTOCOL_INCOMPATIBLE_PEER_LOWER = 13;

    public static final int INVALIDE_CHANNELID = 14;

    public static final int OPEN_DEVICE_ERROR = 15;

    public static final int SYNC_REJECT = 16;

    public static final int SYNC_ACCEPT = 17;

    public static final int SYNC_HANGUP = 18;

    public static final int PEER_NO_RESPONSE = 19; //超时，无人接听

    public static final int CANCEL = 20; //取消

    public static final int LOCAL_CALL_BUSY = 21; // 正在进行本地通话
}
