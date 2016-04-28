package com.huangmj.myphonebook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.myphonebook.R;
import com.huangmj.db.DBManager;
import com.huangmj.db.Person;
import com.huangmj.picture.BitmapUtil;
import com.huangmj.picture.CropHandler;
import com.huangmj.picture.CropHelper;
import com.huangmj.picture.CropParams;
import com.huangmj.picture.ImageUtil;
import com.huangmj.update.UpdateInfoService;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Html;
import android.text.TextUtils;
import android.text.Html.ImageGetter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class UpdateActivity extends Activity implements CropHandler,OnItemSelectedListener{

	private DBManager mgr;
	
	CropParams mCropParams;
	private String FilePath=null;
	private String cd_pic_url =null;
	private String rawContactId;
	private EditText editNumber;
	private ImageView imageview ; 
	private EditText editName;
	private Button but_save;
	//private Button but_save_call;
	
	private Integer cd_id=null;
	private ProgressDialog upBar; 
	 
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
        
        String cd_phone = bundle.getString("phone");
        Person cd_person = new Person();
        cd_person.setPhone_number(cd_phone);
        cd_person = mgr.queryPersonBy_phone(cd_person);
        int person_typee = cd_person.getOther_times();
        //Toast.makeText(this, "person_type:"+person_typee, Toast.LENGTH_LONG).show();
        spinner = (Spinner) findViewById(R.id.add_spinner_person_type);
        list = new ArrayList<String>();
      //分类识别0:全部, 1:家人, 2:朋友, 3:工友, 4:熟人, 5:其他。
        if(person_typee==1){
        	list.add("家人");
    		list.add("朋友");
    		list.add("工友");
    		list.add("熟人");
    		list.add("其他");
        }else if(person_typee==2){
        	list.add("朋友");
        	list.add("家人");
    		list.add("工友");
    		list.add("熟人");
    		list.add("其他");
        }else if(person_typee==3){
        	list.add("工友");
        	list.add("朋友");
        	list.add("家人");
    		list.add("其他");
    		list.add("熟人");  	
        }else if(person_typee==4){
        	list.add("熟人");
        	list.add("工友");
        	list.add("朋友");
        	list.add("家人");
    		list.add("其他");
        }else if(person_typee==5){
        	list.add("其他");
        	list.add("熟人");
        	list.add("工友");
        	list.add("朋友");
        	list.add("家人");	
        }
    	//2,新建ArrayAdapter
		add_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
		
		//3,adapter設置一個下拉列表樣式
		add_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		//4,給spinner加載適配器
		spinner.setAdapter(add_adapter);
		
		//5,spinner設置監聽器
		spinner.setOnItemSelectedListener(this);
		
        String cd_name = cd_person.getPerson_name();
        cd_pic_url = cd_person.getPic_path();
        FilePath = cd_pic_url;
        cd_id = cd_person.get_id();
        rawContactId = cd_person.getRawContactId()+"";
        if(cd_pic_url!=null){
        	Bitmap roundBitmap = ImageUtil.getRoundedCornerBitmap(getFileBitMap(cd_pic_url), 150.0f);
        	imageview.setImageBitmap(roundBitmap);
        }else{
        	imageview.setImageDrawable(getResources().getDrawable(R.drawable.person_default));
        }
        
        editNumber.setText(cd_phone);
        editName.setText(cd_name);
 
        but_save.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				final Person p = save();
				//防止用户没填号码就提交
				if(p != null){
				  Thread but_save_thread = new Thread() { 
			        	 public void run() {   
			        		 addPersonToSystem(p);
			        		 exit_();
			        	 }
			        };
			        but_save_thread.start();
					
				}
			}
		});
        
//        but_save_call.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View arg0) {
//				final Person p = save();
//				if(p != null){
//					new Thread() { 
//			        	 public void run() {   
//			        		 addPersonToSystem(p);
//			        		 call_phone(editNumber.getText().toString());
//			        	 }
//			        }.start(); 
//				}
//			}	
//		});
        
        //监听Edtext内输入的手机号123 4567 8901
        editNumber.addTextChangedListener(new PhoneTextWatcher(editNumber));
        

	
        
    }
	
	 private Handler handler_update = new Handler() {  
	        public void handleMessage(Message msg) {  
	        	begin();
	        };  
	    }; 
	
	
	
	public void begin(){
		upBar = new ProgressDialog(UpdateActivity.this);    //进度条，在下载的时候实时更新进度，提高用户友好度  
        upBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);  
        upBar.setTitle("正在保存数据");  
        upBar.setMessage("请稍候...");  
        //pBar.setProgress(0);  
        upBar.show();
	}
	
	//处理完数据，关闭upBar
		 void down() {  
		        handler_update.post(new Runnable() {  
		            public void run() {  
		                upBar.cancel();   
		            }  
		        });  
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
			Person person = new Person();
			person.setPic_path(FilePath);
			String name = editName.getText().toString();
			String number = editNumber.getText().toString();
			if(name==null){
				person.setPhone_number(number);
				person.setPerson_name(number);
			}else{
				person.setPhone_number(number);
				person.setPerson_name(name);
			}
			person.set_id(cd_id);
			person.setOther_times(person_type);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日    HH:mm:ss");       
			Date curDate = new Date(System.currentTimeMillis());//获取当前时间       
			String str = formatter.format(curDate);
			person.setPublish_time(str);
			//Toast.makeText(this, "ID： "+person.get_id(), Toast.LENGTH_LONG).show();
			return person;	
		}else{
			Toast.makeText(this, "请添加联系人者手机号 ", Toast.LENGTH_LONG).show();
		}
		
		return null;
	}
Bitmap getFileBitMap(String filePath){
	Bitmap bitmap = null;
	Uri uri = Uri.fromFile(new File(filePath));
	bitmap = BitmapUtil.decodeUriAsBitmap(this, uri);
	return bitmap;
}
	
	//保存并拨打电话
//	public void call_phone(String phoneNumber){
//		Intent intent = new Intent(UpdateActivity.this,MainActivity.class);
//		intent.putExtra("Action", "call");
//        String passString = phoneNumber;
//        intent.putExtra("phone", phoneNumber);
//        setResult(RESULT_OK, intent);
//        //finish();
//	}
	
	//退出本activity
	public void exit_(){
		Intent intent = new Intent(UpdateActivity.this,MainActivity.class);
		//Toast.makeText(this, "修改联系人成功！", Toast.LENGTH_LONG).show();
		//intent.putExtra("Action", "修改联系人成功！");
        //intent.putExtra("phone", "123");
        startActivity(intent);
        finish();
	}
	

	//把数据写到数据库和系统通讯录数据库
    public boolean addPersonToSystem(Person person) {  
    	boolean flag = false;
    	//if(cd_id!=null){
			//person.set_id(cd_id);
			//写入数据到数据库中
			if(mgr.updatePersonById(person)){
				UpdateSystemPhoneBook ups= new UpdateSystemPhoneBook(UpdateActivity.this);
				//联系人有原图
				if(cd_pic_url!=null){
					//Toast.makeText(this, "联系人有原图："+rawContactId, Toast.LENGTH_LONG).show();
					//更新数据到系统数据库中
					ups.ChangeContact(person.getPerson_name(), person.getPhone_number(), person.getPic_path(), rawContactId);
					flag = true;
					//修改了原图
					if(!FilePath.equals(cd_pic_url)){
						//删除联系人原图
						FileUtil.deleteFile(new File(cd_pic_url));
					}
				//联系人没有原图
				}else{
					//Toast.makeText(this, "联系人-木有-原图："+rawContactId, Toast.LENGTH_LONG).show();
					//更新数据到系统通讯录数据库中
					if(person.getPic_path()!=null){
						ups.ChangeContact(person.getPerson_name(), person.getPhone_number(), person.getPic_path(), rawContactId);
						flag = true;
					}else{
						ups.ChangeContact(person.getPerson_name(), person.getPhone_number(),null , rawContactId);
						flag = true;
					}
				}
			}else{
				Toast.makeText(this, "修改联系人失败", Toast.LENGTH_LONG).show();
			}
		
    	//}
    	return flag;
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	
		CropHelper.handleResult(this, requestCode, resultCode, data);
        if (requestCode == 1) {
           
        }
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
