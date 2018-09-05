package com.renyu.nimlibrary.extension;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by zhoujianghua on 2015/7/8.
 */
public class VRAttachment extends CustomAttachment {

    private final String KEY_VR = "vr";

    private String vrJson;

    VRAttachment() {
        super(CustomAttachmentType.VR);
    }

    public VRAttachment(String vrJson) {
        this();
        this.vrJson = vrJson;
    }

    @Override
    protected void parseData(JSONObject data) {
        try {
            this.vrJson = data.getString(KEY_VR);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        try {
            data.put(KEY_VR, vrJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    public String getVrJson() {
        return vrJson;
    }

}
