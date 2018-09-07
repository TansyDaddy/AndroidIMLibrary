package com.renyu.nimavchatlibrary.params;

public enum AVChatTypeEnum {
    /**
     * 未定义
     */
    VALID,
    /**
     * 失效
     */
    INVALID,
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
     * 对方挂断电话
     */
    PEER_HANG_UP,
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
     * 被叫方收到呼叫
     */
    CALLEE_ACK_REQUEST,
}
