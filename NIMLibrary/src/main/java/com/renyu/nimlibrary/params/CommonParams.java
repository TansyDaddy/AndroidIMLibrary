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

    // 是否被踢下线
    public static boolean isKickout = false;

    public static final String TYPE = "type";

    // 发出"正在输入提示"指令
    public static final String COMMAND_INPUT = "command_input";

    // 当前发送的VR卡片的UUID
    public static String currentVRUUID = "";
    // 当前发送的VR卡片的状态
    public static AVChatTypeEnum currentVRStatus = AVChatTypeEnum.VALID;
}
