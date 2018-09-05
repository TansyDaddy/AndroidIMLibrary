package com.renyu.nimlibrary.extension;

import org.json.JSONObject;

/**
 * Created by zhoujianghua on 2015/4/10.
 */
public class DefaultCustomAttachment extends CustomAttachment {

    private String content;

    public DefaultCustomAttachment() {
        super(0);
    }

    @Override
    protected void parseData(JSONObject data) {
        content = data.toString();
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = null;
        try {
            data = new JSONObject(content);
        } catch (Exception e) {

        }
        return data;
    }

    public String getContent() {
        return content;
    }
}
