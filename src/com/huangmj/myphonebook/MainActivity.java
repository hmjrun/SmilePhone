package com.huangmj.myphonebook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
//import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;


import com.example.myphonebook.R;
import com.huangmj.db.DBManager;
import com.huangmj.db.Person;
import com.huangmj.picture.BitmapUtil;
//import com.huangmj.picture.CropHelper;
import com.huangmj.picture.ImageUtil;
import com.huangmj.update.UpdateInfo;
import com.huangmj.update.UpdateInfoService;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

//import android.R.integer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
//import android.provider.ContactsContract;
import android.annotation.SuppressLint;
//import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
//import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
//import android.database.Cursor;
//import android.database.CursorWrapper;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
//import android.support.v4.widget.SimpleCursorAdapter;

import android.text.Html;
import android.text.Html.ImageGetter;
import android.view.GestureDetector;
//import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
//import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
//import android.view.GestureDetector.OnGestureListener;
//import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
//import android.widget.LinearLayout;
//import android.widget.RelativeLayout;
//import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/*-No Nmae Phone Book
 * 目标应用人群：手机文字输入有困难的农村老一辈人群对智能安卓手机手机通讯录人员信息录入(主打图片和数字录入)
 * 
 * 未完成任务
 * 2016.4.4 @huangmj
 * -1,基本增删改信息,拨打电话,图片截取(相机，相册)
 * -2,对话框图片多按钮
 * -3,手机号输入显示3 4 4
 * -4,版本联网在线更新(后续升级更新，相对必须联网存在)
 * -5,截图确认保存
 * -6,根据ID更新删除
 * -7,来去电显示联系人图片
 * -8,应用显示默认头像，一圆到底，圆角。
 * -9,允许保存不填选图片，支持后续再选。布局优化。
 * 
 * -10,获取通话记录显示图片
 * 11,云联系人信息备份(手机丢失，账户一键还原。目标群体条件限制，待议)
 */
@SuppressLint("NewApi")
public class MainActivity extends AbsListViewBaseActivity implements OnItemSelectedListener{
	private DBManager mgr;  
    //private GridView gridView;
    private String deletePhone = null;
    private String deletePic = null;
    private String deleteName = null;
    private Integer delete_id = null;
    private String rawContactId ="0";
    private String call_phoneNumber = null;
    private String call_phoneName = null;
    private int deletePosition;
    private PictureAdapter adapter;
    
    
    private final int RE_Add = 110;
    private final int RE_Edit = 100;
    
    
 // 更新版本要用到的一些信息  
    private UpdateInfo info;  
    private ProgressDialog pBar; 
   
    private ArrayList<HashMap<String, Object>> lstImageItem;
    
	/**
	 * 需要显示的图片url地址
	 */
	String[] imageUrls;
	DisplayImageOptions options;
    
	private Spinner spinner;
	private ArrayAdapter<String> add_adapter;
	//datasource
	private List<String> list;
	private int in_times = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
       
        listView = (GridView) findViewById(R.id.gridview);  

        //初始化DBManager  
        mgr = new DBManager(this);
        
        ArrayList<String> listImgPaths = initData();
        imageUrls =  (String[]) listImgPaths.toArray(new String[listImgPaths.size()]);
        for (int i = 0; i < imageUrls.length; i++) {
        	imageUrls[i] = "file://" + imageUrls[i];
        }
        
        options = new DisplayImageOptions.Builder().cacheInMemory(true) // 加载图片时会在内存中加载缓存
        		.cacheOnDisc(true) // 加载图片时会在磁盘中加载缓存
        		.displayer(new RoundedBitmapDisplayer(90)) // 设置用户加载图片task(这里是圆角图片显示)		
        		.showStubImage(R.drawable.person_default) // 在ImageView加载过程中显示图片
        		.showImageForEmptyUri(R.drawable.person_default) // image连接地址为空时
        		.showImageOnFail(R.drawable.person_default) // image加载失败
        		.build();
        
        adapter = new PictureAdapter( this,lstImageItem); 
        ((GridView) listView).setAdapter(adapter);
        
        LayoutAnimationController lac=new LayoutAnimationController(AnimationUtils.loadAnimation(this, R.anim.zoom_in_grid));
	    lac.setOrder(LayoutAnimationController.ORDER_NORMAL);
	    listView.setLayoutAnimation(lac);
	    listView.startLayoutAnimation();
        
        //点击item拨打电话
        listView.setOnItemClickListener(new OnItemClickListener(){ 
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,long id) {
				call_phoneNumber = (String) lstImageItem.get(position).get("phone");
				call_phoneName = (String) lstImageItem.get(position).get("name");
				deletePic = (String) lstImageItem.get(position).get("pic_url");
				
            	AlertDialog.Builder builder = new Builder(MainActivity.this);
				builder.setTitle("联系人："+call_phoneName);
		    	builder.setMessage(
		    			Html.fromHtml("<img src=''/>"+" "+call_phoneNumber, new ImageGetter() {  
                    @Override  
                    public Drawable getDrawable(String source) {  
                        // TODO Auto-generated method stub  
                    	 Drawable mDrawable = null; 
                    	if(deletePic!=null){
                    		//Bitmap bm=; //xxx根据你的情况获取  
                    		//获取圆角图片  
               		     Bitmap roundBitmap = ImageUtil.getRoundedCornerBitmap(getFileBitMap(deletePic), 150.0f);
                    	 mDrawable= new BitmapDrawable(getResources(), roundBitmap);
                    	}else{
                    		mDrawable = getResources().getDrawable(R.drawable.person_default);
                    	}
                        mDrawable.setBounds(0, 0, 200, 200);  
                        return mDrawable;  
                    }  
                },null));
		    	
		    	builder.setPositiveButton(
		    			Html.fromHtml("<img src=''/>拨打", new ImageGetter() {  
                    @Override  
                    public Drawable getDrawable(String source) {  
                        // TODO Auto-generated method stub  
                        Drawable mDrawable = getResources()  
                                .getDrawable(R.drawable.dialog_call);  
                        mDrawable.setBounds(0, 0, 50, 50);  
                        return mDrawable;  
                    }  
                },null), 
                new DialogInterface.OnClickListener() {
		    		@Override
		    		public void onClick(DialogInterface dialog, int which) {

						
						//Toast.makeText(MainActivity.this,phoneNumber , Toast.LENGTH_SHORT).show();
						Person person = new Person();
						SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");       
						Date curDate = new Date(System.currentTimeMillis());//获取当前时间       
						String str = formatter.format(curDate);
						person.setPublish_time(str);
						person.setPhone_number(call_phoneNumber);
						mgr.updateotherTimes(person);
						//用intent启动拨打电话  
		                Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+call_phoneNumber));  
		                startActivity(intent);  		
		    		}
		    	});
		    	
		    	
		    	builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
		    		@Override
		    		public void onClick(DialogInterface dialog, int which) {
		    			dialog.dismiss();
		    			}
		    		});
		    	
		    	builder.create().show();
            } 
        });
        
        //长按删除、编辑联系人
        listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,int position, long arg3) {
				deletePosition = position;
				deletePhone = (String) lstImageItem.get(position).get("phone");
				deletePic = (String) lstImageItem.get(position).get("pic_url");
				deleteName = (String) lstImageItem.get(position).get("name");
				delete_id = (Integer) lstImageItem.get(position).get("id");
				rawContactId = lstImageItem.get(position).get("rawContactId")+"";
				AlertDialog.Builder builder = new Builder(MainActivity.this);
				builder.setTitle("联系人："+deleteName);
				
		    	builder.setMessage(
		    			Html.fromHtml("<img src=''/>"+" "+deletePhone, new ImageGetter() {  
                    @Override  
                    public Drawable getDrawable(String source) {  
                        // TODO Auto-generated method stub  
                    	 Drawable mDrawable = null; 
                     	if(deletePic!=null){
                     		//Bitmap bm=getFileBitMap(deletePic); //xxx根据你的情况获取  
                     		Bitmap roundBitmap = ImageUtil.getRoundedCornerBitmap(getFileBitMap(deletePic), 150.0f);
                        	mDrawable= new BitmapDrawable(getResources(), roundBitmap);
                     		//mDrawable= new BitmapDrawable(getResources(), bm);
                     	}else{
                     		mDrawable = getResources().getDrawable(R.drawable.person_default);
                     	}
                         mDrawable.setBounds(0, 0, 200, 200);  
                         return mDrawable;
                    }  
                },null));
		    	
		    	builder.setPositiveButton(
		    			Html.fromHtml("<img src=''/>删除", new ImageGetter() {  
                    @Override  
                    public Drawable getDrawable(String source) {  
                        // TODO Auto-generated method stub  
                        Drawable mDrawable = getResources()  
                                .getDrawable(R.drawable.dialog_delete);  
                        mDrawable.setBounds(0, 0, 50, 50);  
                        return mDrawable;  
                    }  
                },null), 
                new DialogInterface.OnClickListener() {
		    		@Override
		    		public void onClick(DialogInterface dialog, int which) {
		    			dialog.dismiss();
		    			Person person = new Person();
		    			if(deletePhone!=null){
		    				person.set_id(delete_id);
		    				if(deletePic!=null){
		    					FileUtil.deleteFile(new File(deletePic));
		    				}
		    				if(mgr.deletePerson(person)){
		    					lstImageItem.remove(deletePosition);
		    					adapter.notifyDataSetChanged();
		    					if(!rawContactId.equals("0")){
		    						UpdateSystemPhoneBook ups = new UpdateSystemPhoneBook(MainActivity.this);
		    						if(ups.deletePserson(rawContactId)){
				    					Toast.makeText(MainActivity.this, "删除联系人成功rawContactId:"+rawContactId, Toast.LENGTH_LONG).show();
		    						}
		    					}
		    					
		    				}
		    			}		
		    		}
		    	});
		    	
		    	builder.setNeutralButton(
		    			Html.fromHtml("<img src=''/>修改", new ImageGetter() {  
                    @Override  
                    public Drawable getDrawable(String source) {  
                        // TODO Auto-generated method stub  
                        Drawable mDrawable = getResources()  
                                .getDrawable(R.drawable.dialog_xiugai);  
                        mDrawable.setBounds(0, 0, 50, 50);  
                        return mDrawable;  
                    }  
                },null),new DialogInterface.OnClickListener() {
		    		@Override
		    		public void onClick(DialogInterface dialog, int which) {
		    			Intent intent = new Intent();
		    			intent.setClass(MainActivity.this, UpdateActivity.class);
		    			//intent.putExtra("person_id", delete_id);
		    			//intent.putExtra("pic_url", deletePic);
		    			intent.putExtra("phone", deletePhone);
		    			//intent.putExtra("name", deleteName);
		    			//intent.putExtra("rawContactId", rawContactId);
		    			//startActivity(intent);
		    			startActivity(intent);
		    			 finish();
		    			}
		    		});
		    	
		    	builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
		    		@Override
		    		public void onClick(DialogInterface dialog, int which) {
		    			dialog.dismiss();
		    			}
		    		});
		    	
		    	builder.create().show();
				
				return true;
			}
		});
        
        // 自动检查有没有新版本 如果有新版本就提示更新  
        new Thread() {  
            public void run() {  
                try {  
                    UpdateInfoService updateInfoService = new UpdateInfoService(  
                            MainActivity.this);  
                    info = updateInfoService.getUpDateInfo();  
                    handler1.sendEmptyMessage(0);  
                } catch (Exception e) {  
                    e.printStackTrace();  
                }  
            };  
        }.start(); 
   
        
        spinner = (Spinner) findViewById(R.id.spinner_person_type);
		
		//1,設置數據源	
		list = new ArrayList<String>();
		list.add("全部");
		list.add("家人");
		list.add("朋友");
		list.add("工友");
		list.add("熟人");
		list.add("其他");
		
		//2,新建ArrayAdapter
		add_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
		
		//3,adapter設置一個下拉列表樣式
		add_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		//4,給spinner加載適配器
		spinner.setAdapter(add_adapter);
		
		//5,spinner設置監聽器
		spinner.setOnItemSelectedListener(this);
    
    }
    
    private Handler handler1 = new Handler() {  
        public void handleMessage(Message msg) {  
            // 如果有更新就提示  
            if (isNeedUpdate()) {
            	//下面的代码段  
                showUpdateDialog();  
            }  
        };  
    }; 
    
    Bitmap getFileBitMap(String filePath){
    	Bitmap bitmap = null;
    	Uri uri = Uri.fromFile(new File(filePath));
    	bitmap = BitmapUtil.decodeUriAsBitmap(this, uri);
    	return bitmap;
    }
    
    //初始化从sqllite数据库取出时数据
    private ArrayList<String> initData() {
    	lstImageItem = new ArrayList<HashMap<String, Object>>();
    	ArrayList<String> listpic_url = new ArrayList<String>();
    	
    	String imageUrlss[] = null;
    	List<Person> persons = mgr.query();
    	for(Person person : persons){
    		 HashMap<String,Object > map = new HashMap<String, Object>();
    		 map.put("id", person.get_id());
    		 if(person.getPic_path()==null){
    			 map.put("pic", null);
    			 listpic_url.add("null");
    		 }else{
    			//获取圆角图片  
    		     Bitmap roundBitmap = ImageUtil.getRoundedCornerBitmap(getFileBitMap(person.getPic_path()), 150.0f);  
    			 map.put("pic", roundBitmap);
    			 listpic_url.add(person.getPic_path());
    		 }
    		 map.put("pic_url", person.getPic_path());
    		 map.put("phone", person.getPhone_number());
    		 map.put("other_times", person.getOther_times());
    		 map.put("name",person.getPerson_name());
    		 map.put("rawContactId", person.getRawContactId());
    		 lstImageItem.add(map);
    	}
    	return listpic_url;
	}
    
    //从sqllite数据库取出时数据Type
    private ArrayList<String> initDataByType(Person aim_person) {
    	lstImageItem = new ArrayList<HashMap<String, Object>>();
    	ArrayList<String> listpic_url = new ArrayList<String>();
    	
    	String imageUrlss[] = null;
    	List<Person> persons = mgr.queryPersonsBy_type(aim_person);
    	for(Person person : persons){
    		 HashMap<String,Object > map = new HashMap<String, Object>();
    		 map.put("id", person.get_id());
    		 if(person.getPic_path()==null){
    			 map.put("pic", null);
    			 listpic_url.add("null");
    		 }else{
    			//获取圆角图片  
    		     Bitmap roundBitmap = ImageUtil.getRoundedCornerBitmap(getFileBitMap(person.getPic_path()), 150.0f);  
    			 map.put("pic", roundBitmap);
    			 listpic_url.add(person.getPic_path());
    		 }
    		 map.put("pic_url", person.getPic_path());
    		 map.put("phone", person.getPhone_number());
    		 map.put("other_times", person.getOther_times());
    		 map.put("name",person.getPerson_name());
    		 map.put("rawContactId", person.getRawContactId());
    		 lstImageItem.add(map);
    	}
    	return listpic_url;
	}
    
	@Override  
    protected void onDestroy() {  
        super.onDestroy();  
        //应用的最后一个Activity关闭时应释放DB  
        mgr.closeDB();  
    }  
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.main,menu);
    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	int id = item.getItemId();
    	;
    	switch (id) {
		case R.id.action_add:
			Intent intent = new Intent();
	    	intent.putExtra("phone", "");
	    	intent.setClass(this, AddActivity.class);
	    	startActivity(intent);
	    	 finish();
			break;
		case R.id.action_main_back:
			
			Intent intentt = new Intent();
	    	//intent.putExtra("phone", "");
	    	intentt.setClass(this, ContactRecordListActivity.class);
	    	startActivity(intentt);
	    	 finish();
			break;
    	}
    	return super.onOptionsItemSelected(item);
    }  
    
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	// TODO Auto-generated method stub
    	super.onActivityResult(requestCode, resultCode, data);
    	if(resultCode == RESULT_OK){
			if(requestCode == RE_Add){
				 Bundle b=data.getExtras(); //data为B中回传的Intent
		         String str=b.getString("Action");//回传的动作值
		         Toast.makeText(this, "添加联系人成功！", Toast.LENGTH_LONG).show();
		         if(str.equals("call")){
		        	 String phone=b.getString("phone");
		        	 save_call_phone(phone);
		         }
			}
			if(requestCode == RE_Edit){
				 Bundle b=data.getExtras(); //data为B中回传的Intent
		         String str=b.getString("Action");//回传的动作值
		         Toast.makeText(this,"修改联系人成功！" , Toast.LENGTH_LONG).show();
//		         if(str.equals("call")){
//		        	 String phone=b.getString("phone");
//		        	 save_call_phone(phone);
//		         }
			}
    	}
    }
    
	//保存并拨打电话
	public void save_call_phone(String phoneNumber){
		//用intent启动拨打电话  
        Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+phoneNumber));  
        startActivity(intent); 
	}
  
    @Override
    protected void onRestart() {
    	lstImageItem =null;
    	initData();
        adapter = new PictureAdapter( this,lstImageItem); 
        ((GridView) listView).setAdapter(adapter);
    	super.onRestart();
    }
    
    //对话框，询问是否需要更新
    private void showUpdateDialog() {  
        AlertDialog.Builder builder = new AlertDialog.Builder(this);  
        builder.setIcon(android.R.drawable.ic_dialog_info);  
        builder.setTitle("请升级APP至版本" + info.getVersion());  
        builder.setMessage(info.getDescription());  
        //builder.setCancelable(false);  
      
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {  
      
            @Override  
            public void onClick(DialogInterface dialog, int which) {  
                if (Environment.getExternalStorageState().equals(  
                        Environment.MEDIA_MOUNTED)) {  
                    downFile(info.getUrl());     //在下面的代码段  
                } else {  
                    Toast.makeText(MainActivity.this, "SD卡不可用，请插入SD卡", Toast.LENGTH_SHORT).show();  
                }  
            }  
        });  
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {  
      
            @Override  
            public void onClick(DialogInterface dialog, int which) {  
            }  
      
        });  
        builder.create().show();  
    }  
     
    //判断是否需要更新
    private boolean isNeedUpdate() {  
    	// 最新版本的版本号  
        String v = info.getVersion();
        //Log.i("update",v);  
        //Toast.makeText(MainActivity.this, v, Toast.LENGTH_SHORT).show();  
        if (v.equals(getVersion())) {  
            return false;  
        } else {  
            return true;  
        }  
    }  
      
    // 获取当前版本的版本号  
    private String getVersion() {  
        try {  
            PackageManager packageManager = getPackageManager();  
            PackageInfo packageInfo = packageManager.getPackageInfo(  
                    getPackageName(), 0);  
            return packageInfo.versionName;  
        } catch (NameNotFoundException e) {  
            e.printStackTrace();  
            return "版本号未知";  
        }  
    }  
    
    //下载apk文件
    void downFile(final String url) {   
        pBar = new ProgressDialog(MainActivity.this);    //进度条，在下载的时候实时更新进度，提高用户友好度  
        pBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);  
        pBar.setTitle("正在下载");  
        pBar.setMessage("请稍候...");  
        pBar.setProgress(0);  
        pBar.show();  
        new Thread() {  
            public void run() {          
                HttpClient client = new DefaultHttpClient();  
                HttpGet get = new HttpGet(url);  
                HttpResponse response;  
                try {  
                    response = client.execute(get);  
                    HttpEntity entity = response.getEntity();  
                    int length = (int) entity.getContentLength();  
                    //获取文件大小  
                    pBar.setMax(length);                            
                    //设置进度条的总长度  
                    InputStream is = entity.getContent();  
                    FileOutputStream fileOutputStream = null;  
                    if (is != null) {  
                        File file = new File(  
                                Environment.getExternalStorageDirectory(),  
                                "myPhoneBook.apk");  
                        fileOutputStream = new FileOutputStream(file);  
                        byte[] buf = new byte[10];   //这个是缓冲区，即一次读取10个比特，我弄的小了点，因为在本地，所以数值太大一 下就下载完了，看不出progressbar的效果。  
                        int ch = -1;  
                        int process = 0;  
                        while ((ch = is.read(buf)) != -1) {         
                            fileOutputStream.write(buf, 0, ch);  
                            process += ch;  
                            pBar.setProgress(process);       //这里就是关键的实时更新进度了！  
                        }  
  
                    }  
                    fileOutputStream.flush();  
                    if (fileOutputStream != null) {  
                        fileOutputStream.close();  
                    }  
                    down();  
                } catch (ClientProtocolException e) {  
                    e.printStackTrace();  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }  
  
        }.start();  
    }  
  
    //下载完成，进度条消失，安装
    void down() {  
        handler1.post(new Runnable() {  
            public void run() {  
                pBar.cancel();  
                update();  
            }  
        });  
    }  
    
    //安装文件，一般固定写法  
    void update() {                      
        Intent intent = new Intent(Intent.ACTION_VIEW);  
        intent.setDataAndType(Uri.fromFile(new File(Environment  
                .getExternalStorageDirectory(), "myPhoneBook.apk")),  
                "application/vnd.android.package-archive");  
        startActivity(intent);  
    }  
    
    
    class PictureAdapter extends BaseAdapter{ 
        private LayoutInflater inflater; 

        private List<HashMap<String, Object>> data_list;
        private Context context;
     
        public PictureAdapter(Context con,List<HashMap<String, Object>> data) 
        { 
            super(); 
            this.context = con;
            inflater = LayoutInflater.from(context); 
            this.data_list = data;
        } 
     
        @Override
        public int getCount() 
        { 
            return data_list.size();
        } 
     
        @Override
        public Object getItem(int position) 
        { 
            return data_list.get(position); 
        } 
     
        @Override
        public long getItemId(int position) 
        { 
            return position; 
        } 
     
        @Override
        public View getView(int position, View convertView, ViewGroup parent) 
        { 
            ViewHolder viewHolder; 
            if (convertView == null) 
            { 
                convertView = inflater.inflate(R.layout.gridview_item, null); 
                viewHolder = new ViewHolder(); 
                viewHolder.person_number = (TextView) convertView.findViewById(R.id.tv_person_number); 
                viewHolder.person_image = (ImageView) convertView.findViewById(R.id.person_image); 
                //viewHolder.person_name = (TextView) convertView.findViewById(R.id.tv_person_name);
                convertView.setTag(viewHolder); 
            } else
            { 
                viewHolder = (ViewHolder) convertView.getTag(); 
            } 
            //String id = (String) data_list.get(position).get("id");
            //viewHolder.person_number.setText((CharSequence) data_list.get(position).get("phone")); 
            imageLoader.displayImage(imageUrls[position], viewHolder.person_image, options);
            String name = (String) data_list.get(position).get("name");
            String number = (String) data_list.get(position).get("phone");
            if(name.equals("")){
            	viewHolder.person_number.setText(number); 
            }else{
            	viewHolder.person_number.setText(name); 
            }
            //viewHolder.person_name.setText((CharSequence) data_list.get(position).get("name"));
//            if(data_list.get(position).get("pic")==null){
//               	
//            }else{
//            	viewHolder.person_image.setImageBitmap((Bitmap) data_list.get(position).get("pic")); 
//            }
            return convertView; 
        } 

    } 
     
    class ViewHolder 
    { 
    	//public TextView person_name;
        public TextView person_number; 
        public ImageView person_image; 
    }

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int position,long arg3) {
		String typename = add_adapter.getItem(position);
		//分类识别0:全部, 1:家人, 2:朋友, 3:工友, 4:熟人, 5:其他。
		if(typename.equals("家人")){
			//person_type = 1;
			in_times = 1;
			lstImageItem =null;
			imageUrls = null;
			adapter=null;
			Person aim_person = new Person();
			aim_person.setOther_times(1);
			ArrayList<String> listImgPaths = initDataByType(aim_person);
	        imageUrls =  (String[]) listImgPaths.toArray(new String[listImgPaths.size()]);
	        for (int i = 0; i < imageUrls.length; i++) {
	        	imageUrls[i] = "file://" + imageUrls[i];
	        }
			adapter = new PictureAdapter( this,lstImageItem); 
	        ((GridView) listView).setAdapter(adapter);
	        LayoutAnimationController lac=new LayoutAnimationController(AnimationUtils.loadAnimation(this, R.anim.zoom_in_grid));
		    lac.setOrder(LayoutAnimationController.ORDER_NORMAL);
		    listView.setLayoutAnimation(lac);
		    listView.startLayoutAnimation();
		}else if(typename.equals("朋友")){
			//person_type = 2;
			lstImageItem =null;
			adapter=null;
			in_times = 1;
			Person aim_person = new Person();
			aim_person.setOther_times(2);
			imageUrls = null;
			ArrayList<String> listImgPaths = initDataByType(aim_person);
	        imageUrls =  (String[]) listImgPaths.toArray(new String[listImgPaths.size()]);
	        for (int i = 0; i < imageUrls.length; i++) {
	        	imageUrls[i] = "file://" + imageUrls[i];
	        }
			adapter = new PictureAdapter( this,lstImageItem); 
	        ((GridView) listView).setAdapter(adapter);
	        LayoutAnimationController lac=new LayoutAnimationController(AnimationUtils.loadAnimation(this, R.anim.zoom_in_grid));
		    lac.setOrder(LayoutAnimationController.ORDER_NORMAL);
		    listView.setLayoutAnimation(lac);
		    listView.startLayoutAnimation();
		}else if(typename.equals("工友")){
			//person_type = 3;
			in_times = 1;
			lstImageItem =null;
			adapter=null;
			Person aim_person = new Person();
			aim_person.setOther_times(3);
			imageUrls = null;
			ArrayList<String> listImgPaths = initDataByType(aim_person);
	        imageUrls =  (String[]) listImgPaths.toArray(new String[listImgPaths.size()]);
	        for (int i = 0; i < imageUrls.length; i++) {
	        	imageUrls[i] = "file://" + imageUrls[i];
	        }
			adapter = new PictureAdapter( this,lstImageItem); 
	        ((GridView) listView).setAdapter(adapter);
	        LayoutAnimationController lac=new LayoutAnimationController(AnimationUtils.loadAnimation(this, R.anim.zoom_in_grid));
		    lac.setOrder(LayoutAnimationController.ORDER_NORMAL);
		    listView.setLayoutAnimation(lac);
		    listView.startLayoutAnimation();
		}else if(typename.equals("熟人")){
			//person_type = 4;
			in_times = 1;
			lstImageItem =null;
			adapter=null;
			Person aim_person = new Person();
			aim_person.setOther_times(4);
			imageUrls = null;
			ArrayList<String> listImgPaths = initDataByType(aim_person);
	        imageUrls =  (String[]) listImgPaths.toArray(new String[listImgPaths.size()]);
	        for (int i = 0; i < imageUrls.length; i++) {
	        	imageUrls[i] = "file://" + imageUrls[i];
	        }
			//initDataByType(aim_person);
			adapter = new PictureAdapter( this,lstImageItem); 
	        ((GridView) listView).setAdapter(adapter);
	        LayoutAnimationController lac=new LayoutAnimationController(AnimationUtils.loadAnimation(this, R.anim.zoom_in_grid));
		    lac.setOrder(LayoutAnimationController.ORDER_NORMAL);
		    listView.setLayoutAnimation(lac);
		    listView.startLayoutAnimation();
		}else if(typename.equals("其他")){
			//person_type = 5;
			in_times=1;
			lstImageItem =null;
			adapter=null;
			Person aim_person = new Person();
			aim_person.setOther_times(5);
			imageUrls = null;
			ArrayList<String> listImgPaths = initDataByType(aim_person);
	        imageUrls =  (String[]) listImgPaths.toArray(new String[listImgPaths.size()]);
	        for (int i = 0; i < imageUrls.length; i++) {
	        	imageUrls[i] = "file://" + imageUrls[i];
	        }
			adapter = new PictureAdapter( this,lstImageItem); 
	        ((GridView) listView).setAdapter(adapter);
		}else if(typename.equals("全部")){
			//person_type = 0;
			if(in_times!=0){
				lstImageItem =null;
				imageUrls = null;
				ArrayList<String> listImgPaths = initData();;
		        imageUrls =  (String[]) listImgPaths.toArray(new String[listImgPaths.size()]);
		        for (int i = 0; i < imageUrls.length; i++) {
		        	imageUrls[i] = "file://" + imageUrls[i];
		        }
		    	
		        adapter = new PictureAdapter( this,lstImageItem); 
		        ((GridView) listView).setAdapter(adapter);
		        LayoutAnimationController lac=new LayoutAnimationController(AnimationUtils.loadAnimation(this, R.anim.zoom_in_grid));
			    lac.setOrder(LayoutAnimationController.ORDER_NORMAL);
			    listView.setLayoutAnimation(lac);
			    listView.startLayoutAnimation();
			}
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
}
