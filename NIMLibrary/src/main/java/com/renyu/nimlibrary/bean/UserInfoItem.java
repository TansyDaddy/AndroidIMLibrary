package com.renyu.nimlibrary.bean;

import org.json.JSONException;
import org.json.JSONObject;

public class UserInfoItem {
    // 发送给B端的用户信息
    private String userInfo;
    // C端自身最近会话列表需要展示的上一条信息文字内容
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
