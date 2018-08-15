package com.renyu.nimlibrary.extension;

import com.alibaba.fastjson.JSONObject;

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
        this.vrJson = data.getString(KEY_VR);
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        data.put(KEY_VR, vrJson);
        return data;
    }

    public String getVrJson() {
        return vrJson;
    }

}
