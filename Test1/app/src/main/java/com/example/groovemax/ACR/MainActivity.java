package com.example.groovemax.ACR;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.groovemax.ACR.MyView.DividerItemDecoration;
import com.example.groovemax.ACR.Utils.Chinese2PinYin;
import android.support.design.widget.CollapsingToolbarLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by 60546 on 4/19/2016.
 */
public class MainActivity extends Activity {
    private static final String TAG = "debug";

    private SimpleAdapter adapter = null;
    private ListView pinyinLv;
    private EditText pinyinEd;
    private RecyclerView pinyinRv;
    private MyAdapter myAdapter;
    private FloatingActionButton addFab;

    private Map<String, Object> item = new HashMap<String, Object>();
    private ArrayList<Map<String, Object>> mDate = new ArrayList<Map<String, Object>>();//listView的数据源
    private String[] comResult = null;//存储排列后的结果


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testlayout);

        initUi();

        CollapsingToolbarLayout mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);

        mCollapsingToolbarLayout.setTitle("ACR");
        //通过CollapsingToolbarLayout修改字体颜色
        mCollapsingToolbarLayout.setExpandedTitleColor(Color.argb(0, 255, 255, 255));//设置还没收缩时状态下字体颜色
        mCollapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);//设置收缩后Toolbar上字体的颜色

        //pinyinLv = (ListView) findViewById(R.id.listView);
        pinyinEd = (EditText) findViewById(R.id.editText);

        //adapter = new SimpleAdapter(this, mDate, android.R.layout.simple_list_item_1,
        //        new String[]{"pinyin"}, new int[]{android.R.id.text1});
        //pinyinLv.setAdapter(adapter);

        //inputCheck();

        pinyinRv = (RecyclerView) findViewById(R.id.recyclerView);
        pinyinRv.setLayoutManager(new LinearLayoutManager(this));
        pinyinRv.setAdapter(myAdapter = new MyAdapter());
        pinyinRv.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST));

    }

    private void initUi(){
        addFab = (FloatingActionButton) findViewById(R.id.addFad);
    }


    public void onClick(View v){
        switch (v.getId()){
            case R.id.addFad:
                startActivity(new Intent(this, AddActivity.class));
                break;
        }
    }

    /*
     * RecyclerView的Adapter
     */
    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>{

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyViewHolder holder = new MyViewHolder(LayoutInflater.from(MainActivity.this)
            .inflate(R.layout.recycler_item, parent, false));
            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.textView.setText("打开电脑");
        }

        @Override
        public int getItemCount() {
            return 12;
        }

        class MyViewHolder extends RecyclerView.ViewHolder{
            TextView textView;
            public MyViewHolder(View view){
                super(view);
                textView = (TextView) view.findViewById(R.id.textView);
            }
        }
    }

    /*
     * function:将中文转化为拼音，并进行排列组合，存储在listView的mData中
     */
    private void ChineseToPY(String input){
        input = input.replaceAll("[^(\\u4e00-\\u9fa5)]", "");//过滤输入
        Chinese2PinYin convert = new Chinese2PinYin();
        String[][] result = new String[input.length()][];//存储转化后的结果，每一个汉字对于一个一维数组


        for(int i = 0; i<input.length(); i++)
            result[i] = convert.getCharPinYin(input.charAt(i));

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
                    myAdapter.notifyDataSetChanged();
                    return;
                }
                ChineseToPY(pinyinEd.getText().toString());
                myAdapter.notifyDataSetChanged();
            }
        });
    }


}
