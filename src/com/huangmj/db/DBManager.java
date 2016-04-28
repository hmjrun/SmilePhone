package com.huangmj.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBManager {
	private DBHelper helper;  
    private SQLiteDatabase db;  
      
    public DBManager(Context context) {  
        helper = new DBHelper(context);  
        //因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);  
        //所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里  
        db = helper.getWritableDatabase();  
    }  
      
    /** 
     * add persons 
     * @param persons 
     */  
    public Boolean add(List<Person> persons) {  
    	Boolean flag = false;
        db.beginTransaction();  //开始事务  
        try {  
            for (Person person : persons) {  
                db.execSQL("INSERT INTO person VALUES(null, ?, ?, ?, ?,?,?)", new Object[]{person.getPhone_number(), person.getPic_path(), person.getPerson_name(),person.getPublish_time(),person.getOther_times(),person.getRawContactId()});  
            }  
            db.setTransactionSuccessful();  //设置事务成功完成  
            flag = true;
        } finally {  
            db.endTransaction();    //结束事务  
        } 
        return flag;
    }  
      
    /** 
     * update person's age 
     * @param person 
     */   
    public void updateotherTimes(Person person) {  
    	db.execSQL("UPDATE person SET publish_time = ? WHERE phone_number = ?", new Object[]{person.getPublish_time(),person.getPhone_number()});
    }  
     
    
    /** 
     * delete old person 
     * @param person 
     */  
    public Boolean deletePerson(Person person) {  
    	Boolean flag = false;
    	db.execSQL("DELETE FROM person WHERE _id = ?", new Object[]{person.get_id()});
        flag = true;
        return flag;
    	// db.delete("person", "age >= ?", new String[]{String.valueOf(person.age)});  
    }  
      
    /** 
     * query all persons, return list 
     * @return List<Person> 
     */  
    public List<Person> query() {  
        ArrayList<Person> persons = new ArrayList<Person>();  
        Cursor c = queryTheCursor();  
        while (c.moveToNext()) {  
            Person person = new Person();  
            person.set_id(c.getInt(c.getColumnIndex("_id")));  
            person.setPhone_number(c.getString(c.getColumnIndex("phone_number")));  
            person.setPic_path(c.getString(c.getColumnIndex("pic_path")));  
            person.setPerson_name(c.getString(c.getColumnIndex("person_name"))); 
            person.setPublish_time(c.getString(c.getColumnIndex("publish_time")));
            person.setOther_times(c.getInt(c.getColumnIndex("other_times")));
            person.setRawContactId(c.getInt(c.getColumnIndex("rawContactId")));
            persons.add(person);  
        }  
        c.close();  
        return persons;  
    }  
      
    /** 
     * query all persons, return cursor 
     * @return  Cursor 
     */  
    public Cursor queryTheCursor() {  
        Cursor c = db.rawQuery("SELECT * FROM person order by publish_time desc", null);  
        return c;  
    }  
    
    public int queryNumberPersonBy_phone(Person person) {  
    	
        Cursor c = db.query("person",
    			new String[]{"phone_number","pic_path","person_name"},
    			"phone_number = ?",
    			new String[]{person.getPhone_number()},null, null, null);  
        return c.getCount();  
    }  
    
    public Person queryPerson_pic_By_phone(Person person) {  
    	
        Cursor c = db.query("person",
    			new String[]{"pic_path","other_times","person_name"},
    			"phone_number = ?",
    			new String[]{person.getPhone_number()},null, null, null);  
        Person rperson = new Person() ;
        c.moveToFirst(); 
        //rperson.set_id(c.getInt(c.getColumnIndex("_id")));  
        //rperson.setRawContactId(c.getInt(c.getColumnIndex("rawContactId"))); 
        rperson.setPic_path(c.getString(c.getColumnIndex("pic_path")));  
        rperson.setOther_times(c.getInt(c.getColumnIndex("other_times")));
        rperson.setPerson_name(c.getString(c.getColumnIndex("person_name")));
        //rperson.setPhone_number(person.getPhone_number());
        c.close();  
        
        return rperson;  
        
    } 
public Person queryPersonBy_phone(Person person) {  
    	
        Cursor c = db.query("person",
    			new String[]{"_id","rawContactId","pic_path","person_name","other_times"},
    			"phone_number = ?",
    			new String[]{person.getPhone_number()},null, null, null);  
        Person rperson = new Person() ;
        c.moveToFirst(); 
        rperson.set_id(c.getInt(c.getColumnIndex("_id")));  
        rperson.setRawContactId(c.getInt(c.getColumnIndex("rawContactId"))); 
        rperson.setPic_path(c.getString(c.getColumnIndex("pic_path")));  
        rperson.setPerson_name(c.getString(c.getColumnIndex("person_name")));
        rperson.setOther_times(c.getInt(c.getColumnIndex("other_times")));
        rperson.setPhone_number(person.getPhone_number());
        c.close();  
        
        return rperson;  
        
    } 

public List<Person> queryPersonsBy_type(Person aim_person) {  
	
    Cursor c = db.query("person",
			new String[]{"_id","rawContactId","pic_path","person_name","phone_number","publish_time","other_times"},
			"other_times = ?",
			new String[]{aim_person.getOther_times()+""},null, null, "publish_time desc");  
    ArrayList<Person> persons = new ArrayList<Person>();
    while (c.moveToNext()) {  
        Person person = new Person();  
        person.set_id(c.getInt(c.getColumnIndex("_id")));  
        person.setPhone_number(c.getString(c.getColumnIndex("phone_number")));  
        person.setPic_path(c.getString(c.getColumnIndex("pic_path")));  
        person.setPerson_name(c.getString(c.getColumnIndex("person_name"))); 
        person.setPublish_time(c.getString(c.getColumnIndex("publish_time")));
        person.setOther_times(c.getInt(c.getColumnIndex("other_times")));
        person.setRawContactId(c.getInt(c.getColumnIndex("rawContactId")));
        persons.add(person);  
    }  
    c.close();  
    return persons; 
    
} 
    
    
//    public long getPersonRawContactId(Person person){
//    	long rawContactId = 0;
//    	Cursor c  = db.rawQuery("SELECT * FROM person where _id = ?", (String[]) new Object[]{person.get_id()});
//    	person.setRawContactId(c.getInt(c.getColumnIndex("rawContactId")));
//    	rawContactId = person.getRawContactId();
//    	return rawContactId;
//    }
      
    /** 
     * close database 
     */  
    public void closeDB() {  
        db.close();  
    }

	public boolean updatePersonById(Person person) {
		
    	db.execSQL("UPDATE person SET phone_number = ?, pic_path = ?, person_name= ?, publish_time = ?, other_times = ? WHERE _id = ?", new Object[]{person.getPhone_number(),person.getPic_path(),person.getPerson_name(),person.getPublish_time(),person.getOther_times(),person.get_id()});
		return true;
	}  

}
