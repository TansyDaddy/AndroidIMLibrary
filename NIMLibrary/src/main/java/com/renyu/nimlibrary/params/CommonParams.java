package com.renyu.nimlibrary.params;

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
    // 发出用户来源信息
    public static final String COMMAND_USERFROM = "command_userfrom";
}
