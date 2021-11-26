package com.sty.ne.ams.app;

import android.app.Application;

import com.sty.ne.ams.core.AMSManager;

import java.lang.reflect.InvocationTargetException;

/**
 * Author: ShiTianyi
 * Time: 2021/11/16 0016 20:48
 * Description:
 */
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            AMSManager.hookAMSService(this);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        try {
            AMSManager.mActivityThread();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
