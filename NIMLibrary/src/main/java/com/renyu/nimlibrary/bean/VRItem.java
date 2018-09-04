package com.renyu.nimlibrary.bean;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class VRItem implements Serializable {

    // ID
    private String houseId;
    // 名称
    private String houseTitle;
    // 缩略图
    private String coverPic;
    // 户型
    private String apartment;
    // 面积
    private String buildarea;
    // 价格
    private String price;
    // 描述说明文字
    private String desp;
    // VR看房URL
    private String vrUrl;

    // 新房补充
    // 户型名称
    private String unitType;

    // 类型 1新房 2二手房
    private String type;

    /**
     * 新房构造方法
     * @param houseId
     * @param houseTitle
     * @param coverPic
     * @param apartment
     * @param buildarea
     * @param price
     * @param desp
     * @param vrUrl
     * @param unitType
     */
    public VRItem(String houseId, String houseTitle, String coverPic, String apartment, String buildarea, String price, String desp, String vrUrl, String unitType) {
        this.houseId = houseId;
        this.houseTitle = houseTitle;
        this.coverPic = coverPic;
        this.apartment = apartment;
        this.buildarea = buildarea;
        this.price = price;
        this.desp = desp;
        this.vrUrl= vrUrl;
        this.unitType = unitType;
        this.type = "1";
    }

    /**
     * 二手房构造方法
     * @param houseId
     * @param houseTitle
     * @param coverPic
     * @param apartment
     * @param buildarea
     * @param price
     * @param desp
     * @param vrUrl
     */
    public VRItem(String houseId, String houseTitle, String coverPic, String apartment, String buildarea, String price, String desp, String vrUrl) {
        this.houseId = houseId;
        this.houseTitle = houseTitle;
        this.coverPic = coverPic;
        this.apartment = apartment;
        this.buildarea = buildarea;
        this.price = price;
        this.desp = desp;
        this.vrUrl= vrUrl;
        this.type = "1";
    }

    public String getVrJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("houseId", houseId);
            jsonObject.put("houseTitle", houseTitle);
            jsonObject.put("coverPic", coverPic);
            jsonObject.put("apartment", apartment);
            jsonObject.put("buildarea", buildarea);
            jsonObject.put("price", price);
            jsonObject.put("desp", desp);
            jsonObject.put("vrUrl", vrUrl);
            jsonObject.put("unitType", unitType);
            jsonObject.put("type", type);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }
}
