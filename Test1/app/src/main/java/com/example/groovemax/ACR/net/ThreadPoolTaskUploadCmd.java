package com.example.groovemax.ACR.net;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by 60546 on 5/2/2016.
 */
public class ThreadPoolTaskUploadCmd extends ThreadPoolTask {
    private static final String TAG = "debug";
    private String result;
    private String name;
    private String cond;
    private static Context context;
    private Callback callback;

    public ThreadPoolTaskUploadCmd(String name, String cond, Callback callback){
        this.name = name;
        this.cond = cond;
        this.callback = callback;
    }

    @Override
    public void run() {
        //降低优先级
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_LOWEST);

        result =  NetHelper.sendByGet(name, cond);
        finishNum++;

        if(result != null)
            callback.onReady(result);

    }

    public interface Callback{
        void onReady(String result);
    }
}
