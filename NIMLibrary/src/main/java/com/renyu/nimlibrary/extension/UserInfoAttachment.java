package com.renyu.nimlibrary.extension;

import org.json.JSONException;
import org.json.JSONObject;

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
        try {
            this.userInfoJson = data.getString(KEY_USERINFO);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        try {
            data.put(KEY_USERINFO, userInfoJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    public String getUserInfoJson() {
        return userInfoJson;
    }
}
