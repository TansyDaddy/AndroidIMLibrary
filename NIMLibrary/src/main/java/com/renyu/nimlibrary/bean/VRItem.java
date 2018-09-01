package com.renyu.nimlibrary.bean;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class VRItem implements Serializable {

    private String vrurl;
    private String houseTitle;
    private String coverPic;

    public VRItem(String vrurl, String houseTitle, String coverPic) {
        this.vrurl = vrurl;
        this.houseTitle = houseTitle;
        this.coverPic = coverPic;
    }

    public String getVRJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("vrurl", vrurl);
            jsonObject.put("houseTitle", houseTitle);
            jsonObject.put("coverPic", coverPic);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }
}
