package com.renyu.nimlibrary.extension;

import org.json.JSONException;
import org.json.JSONObject;

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
        try {
            this.houseJson = data.getString(KEY_HOUSE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        try {
            data.put(KEY_HOUSE, houseJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    public String getHouseJson() {
        return houseJson;
    }

}
