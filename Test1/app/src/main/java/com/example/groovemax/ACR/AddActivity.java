package com.example.groovemax.ACR;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.groovemax.ACR.Application.MyApplication;
import com.example.groovemax.ACR.SQLite.DataBaseHelper;
import com.example.groovemax.ACR.Utils.Chinese2PinYin;
import com.example.groovemax.ACR.net.ThreadPoolTaskLoadHmm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * class:添加中文指令
 */
public class AddActivity extends AppCompatActivity {
    private static final String TAG = "debug";

    private SimpleAdapter adapter = null;
    private ListView pinyinLv;
    private EditText pinyinEd;
    private Toolbar toolbar;
    private Chinese2PinYin convert = null;

    private DataBaseHelper helper;//dateBase helper for command
    private Map<String, Object> item = new HashMap<>();
    private ArrayList<Map<String, Object>> mDate = new ArrayList<Map<String, Object>>();//listView的数据源
    private String[] comResult = null;//存储排列后的结果，用于listView的显示
    private final static String commandDir = Environment.getExternalStorageDirectory() + "/ACR/command/";
    private final static String commandPath = commandDir + "commandPY.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_layout);

        helper = new DataBaseHelper(getApplicationContext(), "cmd", 1);
        convert = new Chinese2PinYin();
        initUi();
        inputCheck();//监听editText的输入
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(helper != null)
            helper.close();
    }

    /*
     * to init the Ui, and add new cmd
     * 添加toolbar、为listView添加适配器
     */
    private void initUi(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        //这是AppCompatActivity的方法，要在setSupportActionBar前完成
        setTitle("添加指令");
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.mipmap.back_btn);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        pinyinLv = (ListView) findViewById(R.id.listView);
        pinyinEd = (EditText) findViewById(R.id.editText);

        adapter = new SimpleAdapter(this, mDate, android.R.layout.simple_list_item_1,
                new String[]{"pinyin"}, new int[]{android.R.id.text1});
        pinyinLv.setAdapter(adapter);

        pinyinLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (pinyinEd.getText() == null)
                    return;
                String cmdChinese = pinyinEd.getText().toString();
                cmdChinese = cmdChinese.replaceAll("[^(\\u4e00-\\u9fa5)]", "");//过滤输入
                if (cmdChinese == null) {
                    Toast.makeText(AddActivity.this, "please input chinese!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (cmdChinese.equals("")) {
                    Toast.makeText(AddActivity.this, "please input chinese!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String cmdPinYin = mDate.get(position).get("pinyin").toString();

                //readFromFile();
                String[] split = convert.splitPY(cmdPinYin);
                String[] triPY = convert.py2Tri(split);

                //check and insert the new cmd to database
                Cursor cursor = helper.getReadableDatabase().rawQuery("select * from cmd_table where " +
                        "cmd_content=?", new String[]{cmdChinese});
                if (cursor.moveToFirst()) {
                    Toast.makeText(AddActivity.this, "this command is already added!", Toast.LENGTH_SHORT).show();
                    return;
                }
                cursor.close();
                helper.getReadableDatabase().execSQL("insert into cmd_table values(NULL, ?, ?)",
                        new String[]{cmdChinese, convert.getCmdTri()});

                //start to download models from server
                MyApplication.getThreadPoolManager().addAsyncTask(new ThreadPoolTaskLoadHmm(triPY));
                finish();
            }
        });
    }

    /*
     * function:监听输入，并实时显示
     */
    private void inputCheck(){
        pinyinEd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    mDate.clear();
                    adapter.notifyDataSetChanged();
                    return;
                }
                if(ChineseToPY(pinyinEd.getText().toString()) == 0)
                     adapter.notifyDataSetChanged();
            }
        });
    }

    /*
     * function:将中文转化为拼音，并进行排列组合，存储在listView的mData中
     * 注：中文转拼音类对于某些中文字符不能转化，比如“嚒",需要检测
     */
    private int ChineseToPY(String input){
        input = input.replaceAll("[^(\\u4e00-\\u9fa5)]", "");//过滤输入
        if(input == null)
            return -1;
        if(input.equals(""))
            return -1;

        String[][] result = new String[input.length()][];//存储转化后的结果，每一个汉字对于一个一维数组

        for(int i = 0; i<input.length(); i++){
            result[i] = convert.getCharPinYin(input.charAt(i));
            if(result[i] == null){
                Toast.makeText(this, "指令包含不支持的中文字符", Toast.LENGTH_SHORT).show();
                return -1;
            }
            if(result[i][0] == null){
                Toast.makeText(this, "指令包含不支持的中文字符", Toast.LENGTH_SHORT).show();
                return -1;
            }
            if(result[i].length == 0){
                Toast.makeText(this, "指令包含不支持的中文字符", Toast.LENGTH_SHORT).show();
                return -1;
            }
        }


        if(result[0] == null)
            return -1;
        if(result[0].equals(""))
            return -1;

        int num = 1;
        for(String[] tmp : result)
            num *= tmp.length;

        comResult = new String[num];
        //initial
        for(int i = 0; i<num; i++)
            comResult[i] = "";

        int tmpNum = num;//表示每个字的打印周期
        //遍历所有的字
        for(String[] tmp : result){
            //保证每一条comResult都被赋值
            int k = 0;//每一个k代表一条指令
            int time = 0;//表示每一个字已经打印了多少次
            while(k<comResult.length){
                //进行一轮赋值
                if(tmp.length == 1){
                    comResult[k] += tmp[0];
                    comResult[k] += " ";
                    k++;
                }else {
                    for(int i = 0; i<tmp.length; i++){
                        int down = i*tmpNum/tmp.length + time*tmpNum;
                        int up = (i+1)*tmpNum/tmp.length + time*tmpNum;
                        for(k = down;k<up;k++){
                            comResult[k] += tmp[i];
                            comResult[k] += " ";
                        }
                    }
                    time++;
                }
            }
            tmpNum /= tmp.length;
        }

        //update the data in the listView
        mDate.clear();
        for(int i = 0; i<comResult.length; i++){
            Log.v(TAG, "result" + i + comResult[i]);
            item.put("pinyin", comResult[i]);
            mDate.add(item);
            //这里记得新建一个，不然无效
            item = new HashMap<String, Object>();
        }

        return 0;
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

    private void readFromFile() {
        FileInputStream inputStream;
        try {
            inputStream = openFileInput("command1.txt");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            while (inputStream.read(buffer) != -1)
                stream.write(buffer, 0, buffer.length);
            inputStream.close();
            stream.close();
            String result = new String(stream.toByteArray());
            Log.v(TAG, "readResult " + result);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCmdPath(){
        return commandPath;
    }

}
