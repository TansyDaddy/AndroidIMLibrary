package com.renyu.nimlibrary.extension;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by zhoujianghua on 2015/7/8.
 */
public class HouseAttachment extends CustomAttachment {

    private final String KEY_HOUSE = "house";

    private String houseJson;

    HouseAttachment() {
        super(CustomAttachmentType.HOUSE);
    }

    public HouseAttachment(String houseJson) {
        this();
        this.houseJson = houseJson;
    }

    @Override
    protected void parseData(JSONObject data) {
        this.houseJson = data.getString(KEY_HOUSE);
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        data.put(KEY_HOUSE, houseJson);
        return data;
    }

    public String getHouseJson() {
        return houseJson;
    }

}
