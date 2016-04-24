package com.example.groovemax.ACR.Application;

import android.app.Application;
import android.os.Environment;

import com.example.groovemax.ACR.SQLite.DataBaseHelper;
import com.example.groovemax.ACR.net.ThreadPoolManager;

/**
 * 注：自定义Application需要在manifest中注册
 */
public class MyApplication extends Application{
    private static MyApplication myAppliction = null;
    private static ThreadPoolManager threadPoolManager;
    private DataBaseHelper helper;

    @Override
    public void onCreate() {
        super.onCreate();
        myAppliction = this;


        threadPoolManager = new ThreadPoolManager(0, 7);
        threadPoolManager.start();
    }

    public static  MyApplication getMyAppliction(){
        return myAppliction;
    }

    public static ThreadPoolManager getThreadPoolManager(){
        return threadPoolManager;
    }
}
