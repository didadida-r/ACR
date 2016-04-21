package com.example.groovemax.ACR;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

/**
 * Created by 60546 on 4/21/2016.
 */
public class AddActivity extends AppCompatActivity {

    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_layout);

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
    }
}
