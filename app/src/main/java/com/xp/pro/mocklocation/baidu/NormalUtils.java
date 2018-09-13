/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.xp.pro.mocklocation.baidu;

import android.app.Activity;
import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NormalUtils {

    public static void gotoSettings(Activity activity) {
        Intent it = new Intent(activity, DemoNaviSettingActivity.class);
        activity.startActivity(it);
    }

    public static String getTTSAppID() {
        return "11811719";
    }

    public static String sDate;
    public static void generateFileName() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        sDate = sdf.format(date);
    }
}
