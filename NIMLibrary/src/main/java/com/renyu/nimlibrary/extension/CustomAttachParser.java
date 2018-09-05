package com.renyu.nimlibrary.extension;

import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachmentParser;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by zhoujianghua on 2015/4/9.
 */
public class CustomAttachParser implements MsgAttachmentParser {

    private static final String KEY_TYPE = "type";
    private static final String KEY_DATA = "data";

    @Override
    public MsgAttachment parse(String json) {
        CustomAttachment attachment = null;
        try {
            JSONObject object = new JSONObject(json);
            int type = object.getInt(KEY_TYPE);
            JSONObject data = object.getJSONObject(KEY_DATA);
            switch (type) {
                case CustomAttachmentType.Sticker:
                    attachment = new StickerAttachment();
                    break;
                case CustomAttachmentType.VR:
                    attachment = new VRAttachment();
                    break;
                case CustomAttachmentType.HOUSE:
                    attachment = new HouseAttachment();
                    break;
                case CustomAttachmentType.USERINFO:
                    attachment = new UserInfoAttachment();
                    break;
                default:
                    attachment = new DefaultCustomAttachment();
                    break;
            }
            attachment.fromJson(data);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return attachment;
    }

    public static String packData(int type, JSONObject data) {
        JSONObject object = new JSONObject();
        try {
            object.put(KEY_TYPE, type);
            if (data != null) {
                object.put(KEY_DATA, data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }
}
