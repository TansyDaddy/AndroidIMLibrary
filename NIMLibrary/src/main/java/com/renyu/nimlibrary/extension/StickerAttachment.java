package com.renyu.nimlibrary.extension;

import com.blankj.utilcode.util.FileUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by zhoujianghua on 2015/7/8.
 */
public class StickerAttachment extends CustomAttachment {

    private final String KEY_CATALOG = "catalog";
    private final String KEY_CHARTLET = "chartlet";

    private String catalog;
    private String chartlet;

    StickerAttachment() {
        super(CustomAttachmentType.Sticker);
    }

    public StickerAttachment(String catalog, String emotion) {
        this();
        this.catalog = catalog;
        this.chartlet = FileUtils.getFileNameNoExtension(emotion);
    }

    @Override
    protected void parseData(JSONObject data) {
        try {
            this.catalog = data.getString(KEY_CATALOG);
            this.chartlet = data.getString(KEY_CHARTLET);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        try {
            data.put(KEY_CATALOG, catalog);
            data.put(KEY_CHARTLET, chartlet);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    public String getCatalog() {
        return catalog;
    }

    public String getChartlet() {
        return chartlet;
    }
}
