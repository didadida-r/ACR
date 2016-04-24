package com.example.groovemax.ACR.net;

import android.nfc.Tag;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.Arrays;

/**
 * 文件名：
 * 描述：
 * 作者：
 * 时间：
 */
public class ThreadPoolTaskLoadHmm extends ThreadPoolTask {
    private static final String TAG = "debug";

    private String[] triPhoneArray;

    public ThreadPoolTaskLoadHmm(String[] triPhoneArray) {
        this.triPhoneArray = triPhoneArray;
    }

    @Override
    public void run() {
        //降低优先级
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_LOWEST);

        for(String triPhone : triPhoneArray)
            GetHelper.sendByGet(triPhone);
    }


    /*
    设置接口，可用于加载网络线程结束时回调
    public interface CallBack {
        void onReady(String result);
    }
     */


}
