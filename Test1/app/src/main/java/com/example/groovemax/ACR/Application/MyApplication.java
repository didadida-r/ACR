package com.example.groovemax.ACR.Application;

import android.app.Application;
import android.os.Environment;

import com.example.groovemax.ACR.SQLite.DataBaseHelper;
import com.example.groovemax.ACR.net.ThreadPoolManager;

/**
 * 注：自定义Application需要在manifest中注册
 */
public class MyApplication extends Application{
    private static MyApplication myApplication = null;
    private static ThreadPoolManager threadPoolManager;
    private DataBaseHelper helper;

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;

        threadPoolManager = new ThreadPoolManager(0, 10);
        threadPoolManager.start();
    }

    public static  MyApplication getMyAppliction(){
        return myApplication;
    }

    public static ThreadPoolManager getThreadPoolManager(){
        return threadPoolManager;
    }
}
