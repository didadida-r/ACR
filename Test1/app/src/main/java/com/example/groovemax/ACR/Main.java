package com.example.groovemax.ACR;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.groovemax.ACR.Utils.RecordTool;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Main extends AppCompatActivity {

    private static final String TAG = "debug";
    private String modelDirPath = Environment.getExternalStorageDirectory()+"/ACR/model/";
    private String modelPath = modelDirPath + "k-ai+m.bin";
    private final static String url = "http://1.chick123.applinzi.com/index.php?name=n-en%2Bang";
    private RecordTool recordTool;
    private TextView textView;
    /*
     * 初始化recordTool类
     */
    @Override
    protected void onStart() {
        super.onStart();
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        recordTool = new RecordTool(audioManager);
        Log.v(TAG, "onStart");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.v(TAG, "onCreate");

        textView = (TextView) findViewById(R.id.textView);
        JniUtils jniUtils = new JniUtils();
        textView.setText(jniUtils.ACRWavDecode(modelPath) + "result");
        //NetTool netTool = new NetTool();


    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "onDestroy");
        super.onDestroy();
        recordTool.destroy();
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.recordBtn:
                recordTool.startRecord();
                new Thread(downloadRun).start();

                break;
            case R.id.stopRecord:
                try{
                    recordTool.stopRecord(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }


    Runnable downloadRun = new Runnable() {
        @Override
        public void run() {
            getFile();
        }
    };

    void getFile(){
        try{
            //只是建立tcp连接，没有发送http请求
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(5000);
            urlConnection.setConnectTimeout(5000);
            //建立tcp连接，所有set的设置必须在此之前完成
            urlConnection.connect();

            File destDir = new File(modelDirPath);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            byte[] bytes = new byte[1024];
            BufferedInputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(modelPath));
            while(inputStream.read(bytes) != -1){
                outputStream.write(bytes);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();


        }catch (IOException e) {
            e.printStackTrace();
        }
    }



}
