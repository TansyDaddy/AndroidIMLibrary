package com.renyu.nimavchatlibrary.params;

public enum AVChatTypeEnum {
    /**
     * 未定义
     */
    UNDEFINE,
    /**
     * 被叫方同意通话
     */
    CALLEE_ACK_AGREE,
    /**
     * 被叫方拒绝通话
     */
    CALLEE_ACK_REJECT,
    /**
     * 被叫方正在忙
     */
    CALLEE_ACK_BUSY,
    /**
     * 被叫方同时在线的其他端同意通话
     */
    CALLEE_ONLINE_CLIENT_ACK_AGREE,
    /**
     * 被叫方同时在线的其他端拒绝通话
     */
    CALLEE_ONLINE_CLIENT_ACK_REJECT,
    /**
     * 对方挂断电话
     */
    PEER_HANG_UP,
    /**
     * 通话中收到的控制命令
     */
    CONTROL_NOTIFICATION,
    /**
     * 超时，无人接听
     */
    PEER_NO_RESPONSE,
    /**
     * 连接建立
     */
    CONN,
    /**
     * 验证失败
     */
    CONFIG_ERROR,
    /**
     * 无效的聊天ID
     */
    INVALIDE_CHANNELID,
    /**
     * 正在呼叫
     */
    CALLEE_ACK_REQUEST,
}
