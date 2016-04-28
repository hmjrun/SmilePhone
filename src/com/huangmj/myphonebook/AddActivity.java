package com.huangmj.myphonebook;

import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.DefaultHttpClient;

import com.example.myphonebook.R;
import com.huangmj.db.DBManager;
import com.huangmj.db.Person;
import com.huangmj.picture.BitmapUtil;
import com.huangmj.picture.CropHandler;
import com.huangmj.picture.CropHelper;
import com.huangmj.picture.CropParams;
import com.huangmj.picture.ImageUtil;
//import com.huangmj.update.UpdateInfoService;

//import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
//import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
//import android.os.Environment;
//import android.os.Handler;
//import android.os.Message;
//import android.provider.MediaStore;
import android.text.Html;
import android.text.TextUtils;
import android.text.Html.ImageGetter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
//import android.view.ViewDebug.FlagToString;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class AddActivity extends Activity implements CropHandler, OnItemSelectedListener{

	private DBManager mgr;
	
	CropParams mCropParams;
	private String FilePath=null;
	//private String cd_pic_url =null;
	private EditText editNumber;
	private ImageView imageview ; 
	private EditText editName;
	private Button but_save;
	//private Button but_save_call;
	
	//private Integer cd_id=null;
	private Spinner spinner;
	private ArrayAdapter<String> add_adapter;
	//datasource
	private List<String> list;
	//分类识别0:全部, 1:家人, 2:朋友, 3:工友, 4:熟人, 5:其他。
	private int person_type=5;

	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_person);
        //初始化DBManager  
        mgr = new DBManager(this); 
        mCropParams = new CropParams(this);
        imageview = (ImageView) findViewById(R.id.imagofperson);
        editNumber = (EditText) findViewById(R.id.editNumber);
        editName = (EditText) findViewById(R.id.editName);
        but_save = (Button) findViewById(R.id.submit_but);
        //but_save_call = (Button) findViewById(R.id.submit_call_but);
        
        /*获取Intent中的Bundle对象*/
        Bundle bundle = this.getIntent().getExtras();
        
        /*获取Bundle中的数据，注意类型和key*/
        String phoneNumber = bundle.getString("phone");
        
        if(null != phoneNumber && !"".equals(phoneNumber)){
        	if(phoneNumber.substring(0, 1).equals("+")){
        		phoneNumber = phoneNumber.substring(4,phoneNumber.length() );
        		editNumber.setText(phoneNumber);
        	}else{
        		editNumber.setText(phoneNumber);
        	}
        	
        }
        but_save.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				final Person p = save();
				//防止用户没填号码就提交
				if(p != null){
					//判断改手机号是否在数据库中已存，插入或者更新
					new Thread() { 
			        	 public void run() {   
			        		 addPersonToSystem(p); 
			        		 exit_();
			        	 }
			        }.start();	   
				}
			}
		});
        
//        but_save_call.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View arg0) {
//				final Person p = save();
//				if(p != null){
//					new Thread(){
//						public void run() {   
//							addPersonToSystem(p);
//							save_call_phone(editNumber.getText().toString());
//			        	 }
//			        }.start();
////			        if(p.getPic_path()!=null){
////			        	try {
////				        	Thread.currentThread().sleep(2200);
////				        } catch (InterruptedException e) {
////				        	e.printStackTrace();
////				        }
////			        }
//			        
//				}
//			}	
//		});
        
        //监听Edtext内输入的手机号123 4567 8901
        editNumber.addTextChangedListener(new PhoneTextWatcher(editNumber));
        
        spinner = (Spinner) findViewById(R.id.add_spinner_person_type);
		
		//1,設置數據源	
		list = new ArrayList<String>();
		list.add("其他");
		list.add("家人");
		list.add("朋友");
		list.add("工友");
		list.add("熟人");
		
		
		//2,新建ArrayAdapter
		add_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
		
		//3,adapter設置一個下拉列表樣式
		add_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		//4,給spinner加載適配器
		spinner.setAdapter(add_adapter);
		
		//5,spinner設置監聽器
		spinner.setOnItemSelectedListener(this);
        
    }
	
	
	
	public void choosePic(View view){
		AlertDialog.Builder builder = new Builder(this);
    	builder.setMessage("请选择照片来源");
    	builder.setTitle("提示");
    	builder.setPositiveButton(Html.fromHtml("<img src=''/>相机", new ImageGetter() {  
            @Override  
            public Drawable getDrawable(String source) {  
                // TODO Auto-generated method stub  
                Drawable mDrawable = getResources()  
                        .getDrawable(R.drawable.dialog_xiangji);  
                mDrawable.setBounds(0, 0, 64, 64);  
                return mDrawable;  
            }  
        },null),new DialogInterface.OnClickListener() {
    		@Override
    		public void onClick(DialogInterface dialog, int which) {
    			dialog.dismiss();
    			mCropParams.enable = true;
    	        mCropParams.compress = false;
    	        Intent intent = CropHelper.buildCameraIntent(mCropParams);
    	        startActivityForResult(intent, CropHelper.REQUEST_CAMERA);
    		}
    	});
    	
    	builder.setNegativeButton(Html.fromHtml("<img src=''/>相册", new ImageGetter() {  
            @Override  
            public Drawable getDrawable(String source) {  
                // TODO Auto-generated method stub  
                Drawable mDrawable = getResources()  
                        .getDrawable(R.drawable.dialog_xiangce);  
                mDrawable.setBounds(0, 0, 64, 64);  
                return mDrawable;  
            }  
        },null), new DialogInterface.OnClickListener() {
    		@Override
    		public void onClick(DialogInterface dialog, int which) {
    			dialog.dismiss();
    			mCropParams.enable = true;
                mCropParams.compress = false;
                Intent intent = CropHelper.buildGalleryIntent(mCropParams);
                startActivityForResult(intent, CropHelper.REQUEST_CROP);
    			}
    		});
    	builder.create().show();
		 
	}
	
	public Person save(){
		
		if(!TextUtils.isEmpty(editNumber.getText().toString())){
			String phone_name  = editName.getText().toString(); 
//			if(phone_name==null&&FilePath==null){
//				Toast.makeText(this, "联系人头像和备注不能都为空！至少填一 ", Toast.LENGTH_LONG).show();
//				return null;
//			}
			String phone_number  = editNumber.getText().toString();	
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日    HH:mm:ss");       
			Date curDate = new Date(System.currentTimeMillis());//获取当前时间       
			String str = formatter.format(curDate);
			//组装Person
			Person person = new Person();
			person.setPublish_time(str);
			person.setOther_times(person_type);
			person.setPhone_number(phone_number);
			person.setPic_path(FilePath);	
			//用户未填联系人备注，暂用手机号代替
			if(phone_name==null){
				person.setPerson_name(phone_number);
			}else{
				person.setPerson_name(phone_name);
			}
			return person;	
		}else{
			Toast.makeText(this, "请添加联系人者手机号 ", Toast.LENGTH_LONG).show();
		}
		
		return null;
	}
	
	
	//保存并拨打电话
	public void save_call_phone(String phoneNumber){
		//用intent启动拨打电话  
		Intent intent = new Intent(AddActivity.this,MainActivity.class);
		intent.putExtra("Action", "call");
        String passString = phoneNumber;
        intent.putExtra("phone", phoneNumber);
        setResult(RESULT_OK, intent);
        finish(); 
	}
	
	//退出本activity
	public void exit_(){
		Intent intent = new Intent(AddActivity.this,MainActivity.class);
		//Toast.makeText(this, "添加联系人成功！", Toast.LENGTH_LONG).show();
        //intent.putExtra("phone", "123");
        startActivity(intent);
        finish();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	
		CropHelper.handleResult(this, requestCode, resultCode, data);
 
	}
	
	@Override
	protected void onDestroy() {
		//CropHelper.clearCacheDir();
		 mgr.closeDB(); 
        super.onDestroy();
	}

	@Override
	public void onPhotoCropped(Uri uri,String filePath) {
		if (!mCropParams.compress){
			 Bitmap roundBitmap = ImageUtil.getRoundedCornerBitmap(BitmapUtil.decodeUriAsBitmap(this, uri), 150.0f);
        	 //mDrawable= new BitmapDrawable(getResources(), roundBitmap);
			imageview.setImageBitmap(roundBitmap);
			FilePath = filePath;
		}
			
		
	}

	@Override
	public void onCompressed(Uri uri) {
		// Compressed uri
		imageview.setImageBitmap(BitmapUtil.decodeUriAsBitmap(this, uri));
		
	}

	@Override
	public void onCancel() {
		
		Toast.makeText(this, "Crop canceled!", Toast.LENGTH_LONG).show();
		
	}

	@Override
	public void onFailed(String message) {
		Toast.makeText(this, "Crop failed: " + message, Toast.LENGTH_LONG).show();
		
	}

	@Override
	public void handleIntent(Intent intent, int requestCode) {
		startActivityForResult(intent, requestCode);
		
	}

	@Override
	public CropParams getCropParams() {
		return mCropParams;
	}
	
    public boolean addPersonToSystem(Person person) { 
    	boolean flag = false;
    	
    	//数据库已有该号码的记录。执行更新操作
    	if(mgr.queryNumberPersonBy_phone(person)>0){
    		//Toast.makeText(this, "已有该号码的记录", Toast.LENGTH_LONG).show();
    		Person old_person = mgr.queryPersonBy_phone(person);
    		person.set_id(old_person.get_id());
    		//写入数据到数据库中
			if(mgr.updatePersonById(person)){
				UpdateSystemPhoneBook ups= new UpdateSystemPhoneBook(AddActivity.this);
				//联系人有原图
				if(old_person.getPic_path()!=null){
					//更新数据到系统数据库中
					//用户重新存该号码联系人，未截取图片
					if(person.getPic_path()!=null){
						ups.ChangeContact(person.getPerson_name(), person.getPhone_number(), person.getPic_path(), old_person.getRawContactId()+"");
						//修改了原图
						if(!person.getPic_path().equals(old_person.getPic_path())){
							//删除联系人原图
							FileUtil.deleteFile(new File(old_person.getPic_path()));
						}
					}else{
						ups.ChangeContact(person.getPerson_name(), person.getPhone_number(), null, old_person.getRawContactId()+"");
						FileUtil.deleteFile(new File(old_person.getPic_path()));
					}
					
				//联系人没有原图
				}else{
					//更新数据到系统通讯录数据库中
					ups.ChangeContact(person.getPerson_name(), person.getPhone_number(), person.getPic_path(), old_person.getRawContactId()+"");
				}
			}else{
				Toast.makeText(this, "修改联系人失败", Toast.LENGTH_LONG).show();
			}
			
		//数据库没有有该号码的记录。执行插入操作
    	}else{
    		//Toast.makeText(this, "木有该号码的记录", Toast.LENGTH_LONG).show();
    		UpdateSystemPhoneBook ups = new UpdateSystemPhoneBook(AddActivity.this);
            long rawContactId=0;
            if(person.getPerson_name().equals("")){
            	rawContactId = ups.insert(person.getPhone_number(), person.getPhone_number(), FilePath);
           }else{
                rawContactId = ups.insert(person.getPerson_name(), person.getPhone_number(), FilePath);
            }
            //Toast.makeText(this, "木有该号码的记录,新记录："+rawContactId, Toast.LENGTH_LONG).show();
    		person.setRawContactId(rawContactId);
    		ArrayList<Person> persons = new ArrayList<Person>();
    		persons.add(person);
    		if(mgr.add(persons)){
    			flag = true;		
    		}
    	}
		return flag;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.add,menu);
    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	int id = item.getItemId();
    	switch (id) {
		case R.id.action_back:
			finish();
            System.exit(0);
			Intent intent=new Intent();
	    	//intent.putExtra("extra", "这是页面一传来的值！");
	    	intent.setClass(this, MainActivity.class);
	    	startActivity(intent);
			break;
    	}
    	return super.onOptionsItemSelected(item);
    }  
    
    @Override
    protected void onRestart() {
		
    	super.onRestart();
    }



	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		//分类识别0:全部, 1:家人, 2:朋友, 3:工友, 4:熟人, 5:其他。
		String typename = add_adapter.getItem(position);
		if(typename.equals("家人")){
			person_type = 1;
		}else if(typename.equals("朋友")){
			person_type = 2;
		}else if(typename.equals("工友")){
			person_type = 3;
		}else if(typename.equals("熟人")){
			person_type = 4;
		}else if(typename.equals("其他")){
			person_type = 5;
		}
		
	}



	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

}
