package com.example.groovemax.ACR;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;


public class MyDialog extends Activity
{
	private String seleteString;
	private int seleteIndex;// 保存选择多音字的哪一个音的下标
	private int multiIndex;// 保存input中哪个是多音字

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog);

		// 获取main中的数据
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		final String[] multiPinYin = bundle.getStringArray("stringList");
		multiIndex = bundle.getInt("multiIndex");
		String input = bundle.getString("input");

		// 初始化对话框
		Builder alertDialog = new AlertDialog.Builder(this)
				.setIcon(R.mipmap.ic_launcher)
				.setTitle(
						"请选择\"" + input + "\"中\"" + input.charAt(multiIndex)
								+ "\"的拼音")
				.setSingleChoiceItems(multiPinYin, 0,
						new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface arg0, int which)
							{
								seleteIndex = which;
							}
						});

		alertDialog.setPositiveButton("确定", new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface arg0, int arg1)
			{
				seleteString = multiPinYin[seleteIndex];
				Intent data = new Intent();
				data.putExtra("data", seleteString);
				data.putExtra("multiIndex", multiIndex);
				setResult(2, data);
				finish();
			}
		}).create().show();
	}
}
