package com.renyu.nimlibrary.bean;

import org.json.JSONException;
import org.json.JSONObject;

public class UserInfoItem {
    private String userInfo;
    private String lastMessages;

    public UserInfoItem(String userInfo, String lastMessages) {
        this.userInfo = userInfo;
        this.lastMessages = lastMessages;
    }

    public String getUserInfoJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userInfo", userInfo);
            jsonObject.put("lastMessages", lastMessages);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }
}
