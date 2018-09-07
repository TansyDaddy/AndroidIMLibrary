package com.renyu.nimlibrary.util;

import com.netease.nimlib.sdk.uinfo.model.UserInfo;

import java.util.Comparator;

public class PinyinComparator implements Comparator<UserInfo> {

    public int compare(UserInfo o1, UserInfo o2) {
        if (getFirstLetter(o1.getName()).equals("@") || getFirstLetter(o2.getName()).equals("#")) {
            return -1;
        } else if (getFirstLetter(o1.getName()).equals("#") || getFirstLetter(o2.getName()).equals("@")) {
            return 1;
        } else {
            return getFirstLetter(o1.getName()).compareTo(getFirstLetter(o2.getName()));
        }
    }

    private String getFirstLetter(String string) {
        return PinyinUtils.getPinyinFirstLetter(string);
    }
}
