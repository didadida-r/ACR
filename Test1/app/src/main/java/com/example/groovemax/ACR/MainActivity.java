package com.example.groovemax.ACR;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;

import com.example.groovemax.ACR.Adapter.MyAdapter;
import com.example.groovemax.ACR.Adapter.SimpleItemTouchHelperCallback;
import com.example.groovemax.ACR.Application.MyApplication;
import com.example.groovemax.ACR.MyView.DividerItemDecoration;
import com.example.groovemax.ACR.SQLite.DataBaseHelper;
import com.example.groovemax.ACR.Utils.RecordTool;
import com.example.groovemax.ACR.net.ThreadPoolTask;
import com.example.groovemax.ACR.net.ThreadPoolTaskLoadHmm;
import com.example.groovemax.ACR.net.ThreadPoolTaskUploadCmd;

import android.support.design.widget.CollapsingToolbarLayout;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * class：MainActivity
 * function：识别指令、显示已有指令
 */
public class MainActivity extends Activity implements ThreadPoolTaskUploadCmd.Callback {
    private static final String TAG = "debug";

    private RecyclerView pinyinRv;
    private MyAdapter myAdapter;
    private FloatingActionButton addFab;
    private com.melnykov.fab.FloatingActionButton playFab;

    private int fabFlag;//a flag for playFab to change src

    private ArrayList<Map<String, Object>> mDate = new ArrayList<Map<String, Object>>();//RecyclerView的数据源
    private RecordTool recordTool;
    private JniUtils jniUtils;
    private DataBaseHelper helper;

    private final static String appDir = Environment.getExternalStorageDirectory() + "/ACR";
    private final static String silModelPath = Environment.getExternalStorageDirectory() + "/ACR/model/sil.hmm";
    private final static String commandDir = Environment.getExternalStorageDirectory() + "/ACR/command/";
    private final static String commandPath = commandDir + "commandPY.txt";
    public final static String cmdPath = Environment.getExternalStorageDirectory() + "/ACR/command/commandPY.txt";
    public final static String modelPath = Environment.getExternalStorageDirectory() + "/ACR/model/";
    public static String wavPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        File fileDir = new File(appDir);
        if(!fileDir.exists())
            fileDir.mkdirs();

        File file = new File(silModelPath);
        String[] sil = new String[1];
        sil[0] = "sil";
        if(!file.exists())
            MyApplication.getThreadPoolManager().addAsyncTask(new ThreadPoolTaskLoadHmm(sil));

        initUi();
    }

    //query database to get the newest cmd and update RecyclerView
    @Override
    protected void onResume() {
        super.onResume();
        if (helper == null)
            helper = new DataBaseHelper(getApplicationContext(), "cmd", 1);
        Cursor cursor = helper.getReadableDatabase().rawQuery("select * from cmd_table", null);
        cursor2list(cursor);
        myAdapter.notifyDataSetChanged();

    }

    private void cursor2list(Cursor cursor){
        mDate.clear();
        while(cursor.moveToNext()){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("pinyin", cursor.getString(1));
            mDate.add(map);

        }
    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "onDestroy");
        super.onDestroy();
        recordTool.destroy();
        if(helper != null){
            helper.deleteDataBase(this);
            helper.close();
        }
    }

    private void initUi(){
        addFab = (FloatingActionButton) findViewById(R.id.addFad);
        playFab = (com.melnykov.fab.FloatingActionButton) findViewById(R.id.playFab);

        //通过CollapsingToolbarLayout修改字体颜色
        CollapsingToolbarLayout mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);
        mCollapsingToolbarLayout.setTitle("ACR");
        mCollapsingToolbarLayout.setExpandedTitleColor(Color.argb(0, 255, 255, 255));//设置还没收缩时状态下字体颜色
        mCollapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);//设置收缩后Toolbar上字体的颜色

        pinyinRv = (RecyclerView) findViewById(R.id.recyclerView);
        pinyinRv.setLayoutManager(new LinearLayoutManager(this));
        pinyinRv.setAdapter(myAdapter = new MyAdapter(this, mDate));
        pinyinRv.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST));
        myAdapter.setmOnItemClickListener(new MyAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(MainActivity.this, "select " + position, Toast.LENGTH_SHORT).show();
            }
        });

        //关联ItemTouchHelper和RecyclerView
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(myAdapter);
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(pinyinRv);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        recordTool = new RecordTool(audioManager);
    }


    public void onClick(View v){
        switch (v.getId()){
            case R.id.addFad:
                startActivity(new Intent(this, AddActivity.class));
                break;
            case R.id.playFab:
                //check if models had been loaded
                if(!ThreadPoolTask.getState()){
                    Toast.makeText(this, "loading model! wait a moment",
                            Toast.LENGTH_SHORT).show();
                    break;
                }

                //model loading process finish, start recognise wav
                fabFlag++;
                if(fabFlag%2 == 1){
                    playFab.setImageResource(R.mipmap.pause_fab);
                    recordTool.startRecord();
                }
                else{
                    playFab.setImageResource(R.mipmap.play_fab);
                    try{
                        recordTool.stopRecord(true);

                        //rewrite the command.txt from the DataBase
                        File file = new File(cmdPath);
                        if (file.exists())
                            file.delete();
                        if(helper == null)
                            helper = new DataBaseHelper(getApplicationContext(), "cmd", 1);
                        Cursor cursor = helper.getReadableDatabase().rawQuery("select * from cmd_table", null);
                        while(cursor.moveToNext())
                            write2File(cursor.getString(2));

                        //load the model net
                        jniUtils = new JniUtils();
                        jniUtils.ACRLoadModelBuildNet(modelPath, cmdPath);

                        //decode wav
                        wavPath = recordTool.getCurrentAudio();
                        int id = jniUtils.ACRWavDecode(wavPath);
                        Log.v(TAG, "id" + id);

                        cursor.close();
                        cursor = helper.getReadableDatabase().rawQuery("select * from cmd_table", null);
                        int i = 1;
                        while(cursor.moveToNext()){
                            if(i++ == id){
                                Toast.makeText(this, "result " + cursor.getString(1), Toast.LENGTH_SHORT).show();
                            }
                        }

                        if(id == 1){
                            Log.v(TAG, "id" + id);
                            MyApplication.getThreadPoolManager().addAsyncTask(new ThreadPoolTaskUploadCmd("led1", "0", this));
                        }
                        if(id == 2){
                            Log.v(TAG, "id" + id);
                            MyApplication.getThreadPoolManager().addAsyncTask(new ThreadPoolTaskUploadCmd("led1", "1", this));
                        }
                        if(id == 3){
                            Log.v(TAG, "id" + id);
                            MyApplication.getThreadPoolManager().addAsyncTask(new ThreadPoolTaskUploadCmd("led2", "0", this));
                        }
                        if(id == 4){
                            Log.v(TAG, "id" + id);
                            MyApplication.getThreadPoolManager().addAsyncTask(new ThreadPoolTaskUploadCmd("led2", "1", this));
                        }
                        if(id == 5){
                            Log.v(TAG, "id" + id);
                            MyApplication.getThreadPoolManager().addAsyncTask(new ThreadPoolTaskUploadCmd("led3", "0", this));
                        }
                        if(id == 6){
                            Log.v(TAG, "id" + id);
                            MyApplication.getThreadPoolManager().addAsyncTask(new ThreadPoolTaskUploadCmd("led3", "1", this));
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onReady(String result) {
        Log.v(TAG, "result" + result);


    }

    /*
            * FileOutputStream,追加模式为true
            * commandPY.txt作为jni的输入指令
            */
    private void write2File(String input){
        File destDir = new File(commandDir);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        try{
            //true为追加模式
            FileOutputStream outputStream = new FileOutputStream(commandPath,
                    true);
            outputStream.write(input.getBytes());
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
