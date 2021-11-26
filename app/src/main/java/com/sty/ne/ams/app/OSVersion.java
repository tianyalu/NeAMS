package com.sty.ne.ams.app;

import android.os.Build;

/**
 * Author: ShiTianyi
 * Time: 2021/11/17 0017 19:26
 * Description:
 */
public class OSVersion {
    /**
     * 低版本的AMS类别
     * API Level 21 --> Android 5.0
     * API Level 22 --> Android 5.1
     * API Level 23 --> Android 6.0
     * API Level 24 --> Android 7.0
     * API Level 25 --> Android 7.1
     */
    public static boolean isAndroidOS_21_22_23_24_25() {
        int v = Build.VERSION.SDK_INT;
        if(v < 26) {
            return true;
        }
        return false;
    }

    /**
     * 高版本的AMS类别
     * API Level 26 --> Android 8.0
     * API Level 27 --> Android 8.1
     * API Level 28 --> Android 9.0
     */
    public static boolean isAndroidOS_26_27_28() {
        int v = Build.VERSION.SDK_INT;
        if((v > 26 || v == 26) && (v < 28 || v == 28)) {
            return true;
        }
        return false;
    }
}
