package com.example.groovemax.ACR.net;

import android.nfc.Tag;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 文件名：NetHelper
 * 描述：负责建立tcp/ip连接（POST方式）
 * 作者：
 * 时间：
 */
public class NetHelper {
    final private static String TAG = "debug";
    private final static String MODEL_URL = "http://1.chick123.applinzi.com/index.php";
    private final static String modelDirPath = Environment.getExternalStorageDirectory() + "/ACR/model/";

    private final static String CMD_URL = "http://1.chick123.applinzi.com/respondAndroid.php";

    /**
     * function:通过Get方式加载模型
     * input：triPhone的名字
     * output：在.../model/存储模型文件
     */
    public static void sendByGet(String triPhone) {

        String modelPath = modelDirPath + triPhone + ".hmm";
        File destDir = new File(modelDirPath);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        triPhone = triPhone.replace("+", "%2B");
        String url = MODEL_URL + "?name=" + triPhone;
        //Log.v(TAG, url + " is loading");
        try {
            //只是建立tcp连接，没有发送http请求
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(5000);
            urlConnection.setConnectTimeout(5000);
            //建立tcp连接，所有set的设置必须在此之前完成
            urlConnection.connect();

            //获取服务器传送过来的数据
            if(urlConnection.getResponseCode() == 200) {
                byte[] bytes = new byte[1024];
                BufferedInputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(modelPath));
                while (inputStream.read(bytes) != -1) {
                    outputStream.write(bytes);
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();
                Log.v(TAG, triPhone + "save successful");

            }else{
                Log.v(TAG, "fail to connect");
            }

        } catch (IOException e) {
            Log.v(TAG, triPhone + " IOException");
            e.printStackTrace();
        }
    }

    public static String sendByGet(String name, String cond) {

        String result = "";
        String url = CMD_URL + "?name=" + name + "&cond=" + cond;
        Log.v(TAG, "url"+ url);

        try {
            //只是建立tcp连接，没有发送http请求
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(5000);
            urlConnection.setConnectTimeout(5000);
            //建立tcp连接，所有set的设置必须在此之前完成
            urlConnection.connect();

            //获取服务器传送过来的数据
            int len;
            byte buffer[] = new byte[1024];
            if(urlConnection.getResponseCode() == 200) {
                InputStream in = urlConnection.getInputStream();
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                while((len = in.read(buffer)) != -1){
                    byteOut.write(buffer, 0, len);
                }
                result = new String(byteOut.toByteArray());
                in.close();
                byteOut.close();
            }else{
                Log.v(TAG, "fail to connect");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
