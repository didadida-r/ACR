package com.example.groovemax.ACR.net;

import android.os.Environment;

/**
 * 文件名：
 * 描述：任务单元基类
 * 作者：
 * 时间：
 */
public abstract class ThreadPoolTask implements Runnable {

    private final static String SERVER_URL = "http://1.chick123.applinzi.com/index.php";

    public ThreadPoolTask() {

    }

    public abstract void run();

    public String getURL() {
        return SERVER_URL;
    }
}
