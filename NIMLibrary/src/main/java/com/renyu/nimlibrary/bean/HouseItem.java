package com.renyu.nimlibrary.bean;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class HouseItem implements Serializable {

    private String houseUrl;
    private String houseTitle;
    private String coverPic;
    private String type;

    public HouseItem(String houseUrl, String houseTitle, String coverPic, String type) {
        this.houseUrl = houseUrl;
        this.houseTitle = houseTitle;
        this.coverPic = coverPic;
        this.type = type;
    }

    public String getHouseJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("houseUrl", houseUrl);
            jsonObject.put("houseTitle", houseTitle);
            jsonObject.put("coverPic", coverPic);
            jsonObject.put("type", type);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }
}
