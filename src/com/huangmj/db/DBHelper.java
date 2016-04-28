package com.huangmj.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "phonebook.db";  
    private static final int DATABASE_VERSION = 2;  
      
    public DBHelper(Context context) {  
        //CursorFactory设置为null,使用默认值  
        super(context, DATABASE_NAME, null, DATABASE_VERSION);  
    }  
  
    //数据库第一次被创建时onCreate会被调用  
    @Override  
    public void onCreate(SQLiteDatabase db) {  
        db.execSQL("CREATE TABLE IF NOT EXISTS person" +  
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, phone_number VARCHAR, pic_path VARCHAR, person_name VARCHAR, publish_time VARCHAR, other_times INTERGER DEFAULT 1,rawContactId INTERGER DEFAULT 0)");  
    }  
  
    //如果DATABASE_VERSION值被改为2,系统发现现有数据库版本不同,即会调用onUpgrade  
    @Override  
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {  
        db.execSQL("ALTER TABLE person ADD COLUMN rawContactId INTERGER DEFAULT 0");  
    }  
    
}
