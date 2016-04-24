package com.example.groovemax.ACR.SQLite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DataBase name:cmd
 * table name:cmd_table
 * key:_id
 * content:cmd_content
 */
public class DataBaseHelper extends SQLiteOpenHelper{
    final String SQL_CREATE_TABLE = "create table  cmd_table (" +
            "_id integer primary key autoincrement," +
            "cmd_content varchar(50))";

    public DataBaseHelper(Context context, String name, int version) {
        super(context, name, null, version);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        System.out.println("create a database");
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        System.out.println("update a database");
    }
}
