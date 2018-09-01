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
    // 户型
    private String apartment;
    // 面积
    private String buildarea;
    // 小区
    private String residentialQuarters;
    // VR看房URL
    private String vrUrl;
    // 板块、街道
    private String block;

    // 新房补充
    // 物业类型
    private String channel;
    // 区属
    private String dist;
    // 卖点
    private String intro;

    // 租房补充
    // 出租方式
    private String renttype;
    // 装修情况
    private String fitment;

    // 类型 1新房 2二手房 3租房
    private String type;

    /**
     * 新房构造方法
     * @param houseId
     * @param houseTitle
     * @param coverPic
     * @param channel
     * @param dist
     * @param intro
     * @param vrUrl
     */
    public HouseItem(String houseId, String houseTitle, String coverPic, String channel, String dist, String intro, String vrUrl) {
        this.houseId = houseId;
        this.houseTitle = houseTitle;
        this.coverPic = coverPic;
        this.channel = channel;
        this.dist = dist;
        this.intro = intro;
        this.vrUrl= vrUrl;
        this.type = "1";
    }

    /**
     * 二手房构造方法
     * @param houseId
     * @param houseTitle
     * @param coverPic
     * @param apartment
     * @param buildarea
     * @param block
     * @param residentialQuarters
     * @param vrUrl
     */
    public HouseItem(String houseId, String houseTitle, String coverPic, String apartment, String buildarea, String block, String residentialQuarters, String vrUrl) {
        this.houseId = houseId;
        this.houseTitle = houseTitle;
        this.coverPic = coverPic;
        this.apartment = apartment;
        this.buildarea = buildarea;
        this.block = block;
        this.residentialQuarters = residentialQuarters;
        this.vrUrl= vrUrl;
        this.type = "2";
    }

    /**
     * 租房构造方法
     * @param houseId
     * @param houseTitle
     * @param coverPic
     * @param apartment
     */
    public HouseItem(String houseId, String houseTitle, String coverPic, String apartment, String buildarea, String block, String residentialQuarters, String vrUrl, String renttype, String fitment) {
        this.houseId = houseId;
        this.houseTitle = houseTitle;
        this.coverPic = coverPic;
        this.apartment = apartment;
        this.buildarea = buildarea;
        this.block = block;
        this.residentialQuarters = residentialQuarters;
        this.vrUrl= vrUrl;
        this.renttype = renttype;
        this.fitment = fitment;
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
            jsonObject.put("residentialQuarters", residentialQuarters);
            jsonObject.put("vrUrl", vrUrl);
            jsonObject.put("block", block);
            jsonObject.put("channel", channel);
            jsonObject.put("dist", dist);
            jsonObject.put("intro", intro);
            jsonObject.put("renttype", renttype);
            jsonObject.put("fitment", fitment);
            jsonObject.put("type", type);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }
}
