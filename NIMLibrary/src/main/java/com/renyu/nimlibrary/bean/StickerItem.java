package com.renyu.nimlibrary.bean;

import java.io.Serializable;

public class StickerItem implements Serializable {
    private String category;//类别名
    private String name;

    public StickerItem(String category, String name) {
        this.category = category;
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }
}
