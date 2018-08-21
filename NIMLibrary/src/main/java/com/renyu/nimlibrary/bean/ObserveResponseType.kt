package com.renyu.nimlibrary.bean

enum class ObserveResponseType {
    // 最近会话变更
    ObserveRecentContact,
    // 新消息接收
    ObserveReceiveMessage,
    // 用户资料刷新
    UserInfoUpdate,
    // 从服务器获取用户资料
    FetchUserInfo,
    FetchUserInfoByContact,
    // 用户发送的消息状态
    MsgStatus,
    // 用户收到的消息
    ReceiveMessage,
    // 撤销当前消息
    RevokeMessage,
    // 收到已读回执
    MessageReceipt,
    // 通知类消息
    CustomNotification,
    // 在线状态
    OnlineStatus,
    // 被踢下线
    Kickout,
    // emoji
    Emoji,
    // sticker
    Sticker,
    // 好友关系改变通知
    FriendChangedNotify,
    // 黑名单变更通知
    BlackListChangedNotify
}