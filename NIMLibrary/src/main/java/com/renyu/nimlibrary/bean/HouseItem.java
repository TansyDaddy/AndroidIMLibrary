package com.renyu.nimlibrary.bean;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class HouseItem implements Serializable {

    // ID
    private String houseId;
    // 名称
    private String houseTitle;
    // 缩略图
    private String coverPic;
    // 价格
    private String price;

    // 新房补充
    // 物业类型
    private String channel;
    // 区属
    private String dist;

    // 二手房、租房补充
    // 户型
    private String apartment;
    // 面积
    private String buildarea;

    // 类型 1新房 2二手房 3租房
    private String type;

    /**
     * 新房构造方法
     * @param houseId
     * @param houseTitle
     * @param coverPic
     * @param channel
     * @param dist
     * @param price
     */
    public HouseItem(String houseId, String houseTitle, String coverPic, String channel, String dist, String price) {
        this.houseId = houseId;
        this.houseTitle = houseTitle;
        this.coverPic = coverPic;
        this.channel = channel;
        this.dist = dist;
        this.price= price;
        this.type = "1";
    }

    /**
     * 二手房、租房构造方法
     * @param houseId
     * @param houseTitle
     * @param coverPic
     * @param apartment
     * @param buildarea
     * @param price
     */
    public HouseItem(String houseId, String houseTitle, String coverPic, String apartment, String buildarea, String price, String extra1) {
        this.houseId = houseId;
        this.houseTitle = houseTitle;
        this.coverPic = coverPic;
        this.apartment = apartment;
        this.buildarea = buildarea;
        this.price= price;
        this.type = "2";
    }

    /**
     * 租房构造方法
     * @param houseId
     * @param houseTitle
     * @param coverPic
     * @param apartment
     * @param buildarea
     * @param price
     */
    public HouseItem(String houseId, String houseTitle, String coverPic, String apartment, String buildarea, String price, String extra1, String extra2) {
        this.houseId = houseId;
        this.houseTitle = houseTitle;
        this.coverPic = coverPic;
        this.apartment = apartment;
        this.buildarea = buildarea;
        this.price= price;
        this.type = "3";
    }

    public String getHouseJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("houseId", houseId);
            jsonObject.put("houseTitle", houseTitle);
            jsonObject.put("coverPic", coverPic);
            jsonObject.put("apartment", apartment);
            jsonObject.put("buildarea", buildarea);
            jsonObject.put("price", price);
            jsonObject.put("channel", channel);
            jsonObject.put("dist", dist);
            jsonObject.put("type", type);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }
}
