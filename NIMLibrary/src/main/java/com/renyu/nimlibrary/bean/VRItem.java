package com.renyu.nimlibrary.bean;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class VRItem implements Serializable {

    private String vrurl;
    private String houseTitle;
    private String coverPic;
    private long sendTime;

    public VRItem(String vrurl, String houseTitle, String coverPic) {
        this.vrurl = vrurl;
        this.houseTitle = houseTitle;
        this.coverPic = coverPic;
        this.sendTime = System.currentTimeMillis();
    }

    public String getVRJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("vrurl", vrurl);
            jsonObject.put("houseTitle", houseTitle);
            jsonObject.put("coverPic", coverPic);
            jsonObject.put("sendTime", sendTime);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }
}
