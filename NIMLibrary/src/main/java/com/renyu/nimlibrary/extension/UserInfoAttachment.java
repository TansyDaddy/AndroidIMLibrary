package com.renyu.nimlibrary.extension;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by zhoujianghua on 2015/7/8.
 */
public class UserInfoAttachment extends CustomAttachment {

    private final String KEY_USERINFO = "userinfo";

    private String userInfoJson;

    UserInfoAttachment() {
        super(CustomAttachmentType.USERINFO);
    }

    public UserInfoAttachment(String userInfoJson) {
        this();
        this.userInfoJson = userInfoJson;
    }

    @Override
    protected void parseData(JSONObject data) {
        this.userInfoJson = data.getString(KEY_USERINFO);
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        data.put(KEY_USERINFO, userInfoJson);
        return data;
    }

    public String getUserInfoJson() {
        return userInfoJson;
    }
}
