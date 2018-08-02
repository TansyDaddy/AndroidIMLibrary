package com.renyu.nimlibrary.util.sticker;

import android.content.res.AssetManager;

import com.blankj.utilcode.util.Utils;
import com.renyu.nimlibrary.util.OtherUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/8/2.
 */
public class StickerUtils {

    private static final String CATEGORY_AJMD = "ajmd";
    private static final String CATEGORY_XXY = "xxy";
    private static final String CATEGORY_LT = "lt";

    /**
     * 数据源
     */
    private static List<StickerCategory> stickerCategories = new ArrayList<>();
    private static Map<String, StickerCategory> stickerCategoryMap = new HashMap<>();
    private static Map<String, Integer> stickerOrder = new HashMap<>(3);

    static {
        initStickerOrder();
        loadStickerCategory();
    }

    /**
     * 设置默认贴图顺序
     */
    private static void initStickerOrder() {
        stickerOrder.put(CATEGORY_AJMD, 1);
        stickerOrder.put(CATEGORY_XXY, 2);
        stickerOrder.put(CATEGORY_LT, 3);
    }

    private static void loadStickerCategory() {
        AssetManager assetManager = Utils.getApp().getResources().getAssets();
        try {
            String[] files = assetManager.list("sticker");
            StickerCategory category;
            for (String name : files) {
                if (!OtherUtils.hasExtentsion(name)) {
                    category = new StickerCategory(name, name, true, getStickerOrder(name));
                    stickerCategories.add(category);
                    stickerCategoryMap.put(name, category);
                }
            }
            // 排序
            Collections.sort(stickerCategories, (l, r) -> l.getOrder() - r.getOrder());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int getStickerOrder(String categoryName) {
        if (stickerOrder.containsKey(categoryName)) {
            return stickerOrder.get(categoryName);
        } else {
            return 100;
        }
    }

    public static List<StickerCategory> getCategories() {
        return stickerCategories;
    }

    private static StickerCategory getCategory(String name) {
        return stickerCategoryMap.get(name);
    }

    public static String getStickerUri(String categoryName, String stickerName) {
        StickerCategory category = getCategory(categoryName);
        if (category == null) {
            return null;
        }

        if (isSystemSticker(categoryName)) {
            if (!stickerName.contains(".png")) {
                stickerName += ".png";
            }

            String path = "sticker/" + category.getName() + "/" + stickerName;
            return "asset:///" + path;
        }

        return null;
    }

    private static boolean isSystemSticker(String category) {
        return CATEGORY_XXY.equals(category) ||
                CATEGORY_AJMD.equals(category) ||
                CATEGORY_LT.equals(category);
    }
}
