package com.example.groovemax.ACR.net;

import android.util.Log;

/**
 * 文件名：
 * 描述：任务单元基类
 * 作者：
 * 时间：
 */
public abstract class ThreadPoolTask implements Runnable {
    private static final String TAG = "debug";

    private final static String SERVER_URL = "http://1.chick123.applinzi.com/index.php";
    protected static int threadNum = 0;//代表加载的线程数
    protected static int finishNum = 0;//代表已经加载完毕的线程数

    public ThreadPoolTask() {
        threadNum++;
        Log.v(TAG, "threadNum" + threadNum);
    }

    /*
     * 二者相等说明后台加载模型的线程已经加载完毕，可以进行识别wav的过程了
     */
    public static boolean getState(){
        return threadNum == finishNum;
    }

    public abstract void run();

    public String getURL() {
        return SERVER_URL;
    }
}
