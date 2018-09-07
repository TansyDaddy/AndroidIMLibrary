package com.renyu.nimlibrary.params;

import com.renyu.nimavchatlibrary.params.AVChatTypeEnum;

/**
 * Created by Administrator on 2018/3/21 0021.
 */

public class CommonParams {
    // 用户登录、密码信息、用户角色
    public static final String SP_ACCID = "sp_accid";
    public static final String SP_TOKEN = "sp_token";
    public static final String SP_USERROLE = "sp_userrole";

    // 最后一次登录日期
    public static final String SP_LASTSIGNIN = "sp_lastsignin";

    // 是否被踢下线
    public static boolean isKickout = false;

    public static final String TYPE = "type";
    // accid
    public static final String ACCOUNT = "account";
    // 群组
    public static final String ISGROUP = "isGroup";
    // C端友好文字提示
    public static final String TIP = "tip";
    // VR看房卡片
    public static final String VRITEM = "vrItem";
    // 楼盘卡片
    public static final String HOUSEITEM = "houseItem";
    // B端接收到的用户信息
    public static final String USERINFO = "userInfo";
    // 扩展功能
    public static final String CARD = "card";
    // 星标用户
    public static final String STAR = "star";

    // 发出"正在输入提示"指令
    public static final String COMMAND_INPUT = "command_input";
}
