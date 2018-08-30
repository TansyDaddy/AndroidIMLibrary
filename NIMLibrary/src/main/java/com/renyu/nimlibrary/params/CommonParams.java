package com.renyu.nimlibrary.params;

import android.os.Environment;

import java.io.File;

/**
 * Created by Administrator on 2018/3/21 0021.
 */

public class CommonParams {
    // 用户登录、密码信息
    public static final String SP_ACCID = "sp_accid";
    public static final String SP_TOKEN = "sp_token";

    // SDK根目录路径
    public static final String SDKROOT = Environment.getExternalStorageDirectory().getPath() + File.separator + "example";

    // 是否被踢下线
    public static boolean isKickout = false;

    public static final String TYPE = "type";

    // 发出"正在输入提示"指令
    public static final String COMMAND_INPUT = "command_input";
}
