package com.example.groovemax.ACR.net;

import android.util.Log;

/**
 * 文件名：
 * 描述：用于加载hmm模型
 */
public class ThreadPoolTaskLoadHmm extends ThreadPoolTask {
    private static final String TAG = "debug";

    private String[] triPhoneArray;//一条指令的triPhone形式

    public ThreadPoolTaskLoadHmm(String[] triPhoneArray) {
        this.triPhoneArray = triPhoneArray;
    }

    @Override
    public void run() {
        //降低优先级
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_LOWEST);

        for(String triPhone : triPhoneArray){
            NetHelper.sendByGet(triPhone);
        }

        //表示完成本条指令的加载
        finishNum++;
        Log.v(TAG, "finishNum" + finishNum);
    }


    /*
    //设置接口，可用于加载网络线程结束时回调
    public interface CallBack {
        void onReady();
    }*/



}
