package com.huangmj.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "phonebook.db";  
    private static final int DATABASE_VERSION = 2;  
      
    public DBHelper(Context context) {  
        //CursorFactory����Ϊnull,ʹ��Ĭ��ֵ  
        super(context, DATABASE_NAME, null, DATABASE_VERSION);  
    }  
  
    //���ݿ��һ�α�����ʱonCreate�ᱻ����  
    @Override  
    public void onCreate(SQLiteDatabase db) {  
        db.execSQL("CREATE TABLE IF NOT EXISTS person" +  
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, phone_number VARCHAR, pic_path VARCHAR, person_name VARCHAR, publish_time VARCHAR, other_times INTERGER DEFAULT 1,rawContactId INTERGER DEFAULT 0)");  
    }  
  
    //���DATABASE_VERSIONֵ����Ϊ2,ϵͳ�����������ݿ�汾��ͬ,�������onUpgrade  
    @Override  
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {  
        db.execSQL("ALTER TABLE person ADD COLUMN rawContactId INTERGER DEFAULT 0");  
    }  
    
}
