package com.focustech.message.model;

import android.content.Context;
import android.content.Intent;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/7/18.
 */

public class BroadcastBean implements Serializable {
    public enum MTCommand implements Serializable {
        HeartBeat,                   // 自增，用于标志心跳包
        Conn,                        // 自增，用于已连接
        Conning,                     // 自增，用于标志发起连接
        Disconn,                     // 自增，用于连接失败
        LoginRsp,
        LoginRspERROR,
        UserInfoRsp,
        FriendGroupsRsp,
        FriendInfoRsp,
        FriendInfoEndRsp,
        GetOfflineMessageRsp,
        UpdateUserStatusNty,
        Message,
        MessageSend,                 // 自增，用于标志发送消息
        MessageComp,                 // 自增，用于标志消息发送成功
        MessageCompByConversation,   // 自增，用于回话详情标志消息发送成功
        MessageFail,                 // 自增，用于标志消息发送失败
        MessageFailByConversation,   // 自增，用于回话详情标志消息发送失败
        MessageReceive,              // 自增，用于标志消息接收完成，用于数据库操作完成后刷新列表使用
        MessageDownloadComp,         // 自增，用于标志下载语音文件完成
        ReceptNty,
        UpdateRead,                  // 自增，用于标志未读消息清除
        NewSysNty,
        GetFriendRuleRsp,
        DeleteFriendRsp,
        SystemMessageResp,       // 自增，用于标志收到系统消息
        AddedFriendSucceededSysNty,
        AddFriendWithoutValidateSucceededSysNty,
        RefreshFriendList,
        FriendInfoNty,
        Kickout
    }

    MTCommand command;
    Serializable serializable;

    public MTCommand getCommand() {
        return command;
    }

    public void setCommand(MTCommand command) {
        this.command = command;
    }

    public Serializable getSerializable() {
        return serializable;
    }

    public void setSerializable(Serializable serializable) {
        this.serializable = serializable;
    }

    public static void sendBroadcast(Context context, MTCommand command, Serializable serializable) {
        String actionName = "";
        try {
            Class clazz = Class.forName("com.renyu.mt.params.InitParams");
            actionName = clazz.getField("actionName").get(clazz).toString();
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }

        BroadcastBean bean=new BroadcastBean();
        bean.setCommand(command);
        bean.setSerializable(serializable);
        Intent intent=new Intent();
        intent.putExtra("broadcast", bean);
        intent.setAction(actionName);
        context.sendBroadcast(intent);
    }
}
