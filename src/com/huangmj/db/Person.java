package com.huangmj.db;

public class Person {
	private int _id;  
	
	private int other_times;//����ʶ��0:ȫ��, 1:����, 2:����, 3:����, 4:����, 5:������
	private String phone_number;  
	private String pic_path;  
	private String person_name; 
	private String publish_time;
	private long  rawContactId;
    
	public long getRawContactId() {
		return rawContactId;
	}
	public void setRawContactId(long rawContactId) {
		this.rawContactId = rawContactId;
	}
	public int get_id() {
		return _id;
	}
	public void set_id(int _id) {
		this._id = _id;
	}
	public String getPhone_number() {
		return phone_number;
	}
	public void setPhone_number(String phone_number) {
		this.phone_number = phone_number;
	}
	public String getPic_path() {
		return pic_path;
	}
	public void setPic_path(String pic_path) {
		this.pic_path = pic_path;
	}
	public String getPerson_name() {
		return person_name;
	}
	public void setPerson_name(String person_name) {
		this.person_name = person_name;
	}
	public String getPublish_time() {
		return publish_time;
	}
	public void setPublish_time(String publish_time) {
		this.publish_time = publish_time;
	}
	
	public Person(){
		this._id = 0;
	}
	
	public int getOther_times() {
		return other_times;
	}
	public void setOther_times(int other_times) {
		this.other_times = other_times;
	}
    
    

}
