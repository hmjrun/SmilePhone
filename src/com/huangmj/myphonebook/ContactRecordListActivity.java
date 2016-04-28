package com.huangmj.myphonebook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;  
import java.util.ArrayList;  
import java.util.Collections;
import java.util.Date;  
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;  

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

//import com.example.android_imageload01.ImageListActivity.AnimateFirstDisplayListener;
import com.example.myphonebook.R;
import com.huangmj.db.DBManager;
import com.huangmj.db.Person;
//import com.huangmj.myphonebook.DialAdapter.ViewHolder;
import com.huangmj.picture.BitmapUtil;
import com.huangmj.picture.ImageUtil;
import com.huangmj.update.UpdateInfo;
import com.huangmj.update.UpdateInfoService;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
//import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
//import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
  
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
//import android.app.Activity;  
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.AsyncQueryHandler;  
import android.content.ContentResolver;  
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;  
//import android.gesture.GestureOverlayView;
//import android.gesture.GestureOverlayView.OnGestureListener;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;  
import android.os.Build;
import android.os.Bundle;  
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;  
import android.text.Html;
import android.text.Html.ImageGetter;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
//import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
//import android.widget.LinearLayout;
import android.widget.ListView;  
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
  
  
  
/** 
 * 通话记录列表 
 *  
 * @author wwj 
 *  
 */  
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ContactRecordListActivity   extends AbsListViewBaseActivity {  
 
    private AsyncQueryHandler asyncQuery;  
    private DialAdapter adapter;  
    private long exitTime = 0;
    
    private ArrayList<HashMap<String, Object>> list_call_records;
    HashSet<String> phoneNumber_hsashSet;
    private DBManager mgr; 
    
    // 更新版本要用到的一些信息  
    private UpdateInfo info;  
    private ProgressDialog pBar; 
    
    private DisplayImageOptions options; // 配置图片加载及显示选项
	/** 
     * 存储图片地址 
     */  
    //ArrayList<String> listImgPath;  
    String[] imageUriArray; 
    
    private EditText edit_phone;
    private Button edit_action_call;
    
    GestureDetector mGestureDetector;
     
    

    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.contact_record_list_view);  
  
        listView = (ListView) findViewById(R.id.call_log_list);  
        asyncQuery = new MyAsyncQueryHandler(getContentResolver());  
        
        edit_phone = (EditText) findViewById(R.id.edit_phone);
        edit_action_call = (Button) findViewById(R.id.edit_action_call);

        edit_phone.addTextChangedListener(new PhoneTextWatcher(edit_phone));
        
        edit_action_call.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View arg0) {
				String number = edit_phone.getText().toString();
				if(null!=number && !number.equals(" ")){
					Person person = new Person();
					person.setPhone_number(number);
					
					if(mgr.queryNumberPersonBy_phone(person)>0){
						SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");       
						Date curDate = new Date(System.currentTimeMillis());//获取当前时间       
						String str = formatter.format(curDate);
						person.setPublish_time(str);
						person.setPhone_number(number);
						mgr.updateotherTimes(person);
					}
						
					Uri uri = Uri.parse("tel:" + number);  
	                Intent intent = new Intent(Intent.ACTION_CALL, uri);  
	                startActivity(intent); 
				}else{
					Toast.makeText(ContactRecordListActivity.this, "请输入号码，再按拨号按钮", Toast.LENGTH_LONG).show();
				}
				
			}
		});
        
        //初始化DBManager  
        mgr = new DBManager(this);
        
        init();  

        options = new DisplayImageOptions.Builder().cacheInMemory(true) // 加载图片时会在内存中加载缓存
        		.cacheOnDisc(true) // 加载图片时会在磁盘中加载缓存
        		.displayer(new RoundedBitmapDisplayer(80)) // 设置用户加载图片task(这里是圆角图片显示)		
        		.showStubImage(R.drawable.person_default) // 在ImageView加载过程中显示图片
        		.showImageForEmptyUri(R.drawable.person_default) // image连接地址为空时
        		.showImageOnFail(R.drawable.person_default) // image加载失败
        		.build();
        
     // 自动检查有没有新版本 如果有新版本就提示更新  
        new Thread() {  
            public void run() {  
                try {  
                    UpdateInfoService updateInfoService = new UpdateInfoService(  
                    		ContactRecordListActivity.this);  
                    info = updateInfoService.getUpDateInfo();  
                    handler1.sendEmptyMessage(0);  
                } catch (Exception e) {  
                    e.printStackTrace();  
                }  
            };  
        }.start(); 
    
        listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				//edit_phone.setText("");
				//edit_phone.setText((CharSequence) list_call_records.get(arg2).get("Number"));
			 	final int position = arg2;
			 	final String names = (String) list_call_records.get(position).get("Name");
			 	final String cachedName = (String) list_call_records.get(position).get("cachedName");
			 	final String name = names==null?cachedName:names;
			 	final String number = (String) list_call_records.get(position).get("Number");
				AlertDialog.Builder builder = new Builder(ContactRecordListActivity.this);
				builder.setTitle("拨打电话："+name);
		    	builder.setMessage(
		    			Html.fromHtml("<img src=''/>"+" "+number, new ImageGetter() {  
                  @Override  
                  public Drawable getDrawable(String source) {  
                      // TODO Auto-generated method stub  
                  	 Drawable mDrawable = null; 
                  	 Bitmap b = (Bitmap) list_call_records.get(position).get("person_pic");
                  	if(b!=null){
                  		//Bitmap bm=; //xxx根据你的情况获取  
                  		//获取圆角图片  
             		     //Bitmap roundBitmap = ImageUtil.getRoundedCornerBitmap(getFileBitMap(deletePic), 150.0f);
                  	 mDrawable= new BitmapDrawable(ContactRecordListActivity.this.getResources(), b);
                  	}else{
                  		mDrawable = ContactRecordListActivity.this.getResources().getDrawable(R.drawable.person_default);
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
                      Drawable mDrawable = ContactRecordListActivity.this.getResources()  
                              .getDrawable(R.drawable.dialog_call);  
                      mDrawable.setBounds(0, 0, 50, 50);  
                      return mDrawable;  
                  }  
              },null), 
              new DialogInterface.OnClickListener() {
		    		@Override
		    		public void onClick(DialogInterface dialog, int which) {

		    			Person person = new Person();
						SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");       
						Date curDate = new Date(System.currentTimeMillis());//获取当前时间       
						String str = formatter.format(curDate);
						person.setPublish_time(str);
						person.setPhone_number(number);
						mgr.updateotherTimes(person);
		                Uri uri = Uri.parse("tel:" + number);  
		                Intent intent = new Intent(Intent.ACTION_CALL, uri);  
		                ContactRecordListActivity.this.startActivity(intent);  		
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
        
//        mGestureDetector = new GestureDetector((android.view.GestureDetector.OnGestureListener) this);     
//        RelativeLayout viewSnsLayout = (RelativeLayout)findViewById(R.id.contact_record_view);     
//        viewSnsLayout.setOnTouchListener(this);     
//        viewSnsLayout.setLongClickable(true); 
    
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
  
    private void init() {  
        Uri uri = android.provider.CallLog.Calls.CONTENT_URI;  
        
        // 查询的列  
        String[] projection = { CallLog.Calls.DATE, // 日期  
                CallLog.Calls.NUMBER, // 号码  
                CallLog.Calls.TYPE, // 类型  
                CallLog.Calls.CACHED_NAME, // 名字  
                CallLog.Calls._ID, // id  
        };  
        asyncQuery.startQuery(0, null, uri, projection, null, null,  
                CallLog.Calls.DEFAULT_SORT_ORDER);  
    }  
    
    @Override
	public void onBackPressed() {
		AnimateFirstDisplayListener.displayedImages.clear();
		super.onBackPressed();
	}
    
    
    private class MyAsyncQueryHandler extends AsyncQueryHandler {  
  
        public MyAsyncQueryHandler(ContentResolver cr) {  
            super(cr);  
        }  
  
        @Override  
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {  
            if (cursor != null && cursor.getCount() > 0) {   
                Date date;  
                cursor.moveToFirst(); // 游标移动到第一项  cursor.getCount()
                list_call_records = new ArrayList<HashMap<String, Object>>();
                phoneNumber_hsashSet = new HashSet();
                ArrayList<String> listImgPath = new ArrayList<String>();
                int count = cursor.getCount();
                if(count>200){
                	count= 700;
                }
                	for (int i = 0; i < count; i++) {  
                    	HashMap<String,Object > map = new HashMap<String, Object>();
                        cursor.moveToPosition(i);  
                        String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));  
                        if(phoneNumber_hsashSet.contains(number)){
                        	continue;
                        }
                        phoneNumber_hsashSet.add(number);
                        date = new Date(cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)));  
                        SimpleDateFormat formatteer = new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss"); 
                        String strDate = formatteer.format(date);    
                        int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));  
                        //String cachedName = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));// 缓存的名称与电话号码，如果它的存在  
                        String cachedName = cursor.getString(3);
                        int id = cursor.getInt(cursor.getColumnIndex(CallLog.Calls._ID));   
                        
                        map.put("Id", id);
                        
                        
                        if (null == cachedName || "".equals(cachedName)||cachedName.length()>8) {  
//                        	int length =  number.length();
//                        	String n =null;
//                            if(number.length()<=6){
//                                 n = number;
//                             }else{
//                               n = number.substring(length-4,length);   
//                             }
//                        	map.put("Name",n);
                        	map.put("cachedName","");
                        } else{
                        	map.put("cachedName",cachedName);
                        }
                        map.put("Type",type);
                        map.put("Date",strDate);
                                           
                        String result = null;
                        if(number.length()<=3){
                            result = number;
                        }
                        if(number.length()>3&&number.length()<=7){
                            result = number.substring(0,3)+" "+number.substring(3);
                         }
                       if(number.length()>7&&number.length()<=11){
                            result = number.substring(0,3)+" "+number.substring(3,7)+" "+number.substring(7);    	
                         }
                       
                       if(number.length()>11&&number.length()<13){
                             result = number.substring(0,3)+" "+number.substring(3,7)+" "+number.substring(7,11)+" "+number.substring(11);    	
                       }
                       if(number.length()>=13){
                           if(number.substring(0,1).equals("+")){
                          	 number = number.substring(3,number.length());
                           }
                           result = number.substring(0,3)+" "+number.substring(3,7)+" "+number.substring(7);    	
                     }

                       Person p = new Person(); 
                       p.setPhone_number(result);
                       map.put("Number",result);
                       int n = mgr.queryNumberPersonBy_phone(p);
                       map.put("exist", "0");
                       if(n>0){
                    	   map.put("exist", "1");
                        	Person newp = mgr.queryPerson_pic_By_phone(p);
                        	map.put("person_type",newp.getOther_times()+"");
                        	String person_name = newp.getPerson_name();
                        	if(person_name!=null&&!person_name.equals("")){
                        		map.put("Name", person_name);
                        	}else{
                        		map.put("Name", "");
                        	}
                        	
                        	String pic_url =newp.getPic_path();
                        	if(pic_url!=null){
                        		listImgPath.add(pic_url);
                        		Bitmap roundBitmap = ImageUtil.getRoundedCornerBitmap(getFileBitMap(pic_url), 150.0f);
                        		map.put("person_pic", roundBitmap);
                        		map.put("person_pic_url",pic_url);
                        	}else{
                        		listImgPath.add("null");
                            	map.put("person_pic", null);
                        	}
                        }else{
                        	listImgPath.add("null");
                        	map.put("person_pic", null);
                        }
                       
                        list_call_records.add(map);
                    } 
                //}
                 
                if (list_call_records.size() > 0) {  
                	setPic_url_array(listImgPath);
                    setAdapter(list_call_records);  
                }  
            }  
            super.onQueryComplete(token, cookie, cursor);  
        }

		
    }  
    
    private void setPic_url_array(ArrayList<String> listImgPaths) {
    	imageUriArray = (String[]) listImgPaths.toArray(new String[listImgPaths.size()]);
        for (int i = 0; i < imageUriArray.length; i++) {
       	 imageUriArray[i] = "file://" + imageUriArray[i];
        }
    	//listImgPath =  listImgPaths;;
	}  
    
    private void setAdapter(List<HashMap<String, Object>> data) {  
    	
        adapter = new DialAdapter(this,data);  
        listView.setAdapter(adapter); 
        LayoutAnimationController lac=new LayoutAnimationController(AnimationUtils.loadAnimation(this, R.anim.zoom_in));
	    lac.setOrder(LayoutAnimationController.ORDER_NORMAL);
	    listView.setLayoutAnimation(lac);
	    listView.startLayoutAnimation();
    } 
    
    
	public Bitmap getFileBitMap(String filePath){
    	Bitmap bitmap = null;
    	Uri uri = Uri.fromFile(new File(filePath));
    	bitmap = BitmapUtil.decodeUriAsBitmap(this, uri);
    	return bitmap;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.record,menu);
    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	int id = item.getItemId();
    	switch (id) {
		case R.id.action_myphone_book:
			Intent intent=new Intent();
	    	intent.setClass(this, MainActivity.class);
	    	startActivityForResult(intent, 0);
			break;
//		case R.id.action_call_index:
//			Toast.makeText(this, "拨打电话", Toast.LENGTH_SHORT).show();
//			break;
    	}
    	return super.onOptionsItemSelected(item);
    }  
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){   
            if((System.currentTimeMillis()-exitTime) > 2000){  
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();                                
                exitTime = System.currentTimeMillis();   
            } else {
                finish();
                System.exit(0);
            }
            return true;   
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    protected void onRestart() {
    	init();
    	super.onRestart();
    }
    @Override
    protected void onDestroy() {
    	mgr.closeDB(); 
    	super.onDestroy();
    }

    /** 图片加载监听事件 **/
  	private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

  		static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

  		@Override
  		public void onLoadingComplete(String imageUri, View view,
  				Bitmap loadedImage) {
  			if (loadedImage != null) {
  				ImageView imageView = (ImageView) view;
  				boolean firstDisplay = !displayedImages.contains(imageUri);
  				if (firstDisplay) {
  					FadeInBitmapDisplayer.animate(imageView, 500); // 设置image隐藏动画500ms
  					displayedImages.add(imageUri); // 将图片uri添加到集合中
  				}
  			}
  		}
  	}

  	
  	
  	public class DialAdapter extends BaseAdapter{  
  		
  	    private Context ctx;  
  	  
  	    private LayoutInflater inflater; 
  	    private DBManager mgr;
  	    private List<HashMap<String, Object>> data_list;

  	    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
  		//private DisplayImageOptions options;
  		//private ImageLoader imageLoader;
  	    public DialAdapter(Context context,List<HashMap<String, Object>> data) {  
  	        this.ctx = context;  
  	        this.data_list = data;  
  	        this.inflater = LayoutInflater.from(ctx);  
  	        this.mgr = new DBManager(ctx); 
  	        //this.listView = listview;
  	        //this.animateFirstListener= animateFirstListenerr;
  	        //this.options =optionss; 
  	        //this.imageLoader = imageLoaderr;
  	    }  
  	  
  	    @Override  
  	    public int getCount() {  
  	        return data_list.size();  
  	    }  
  	  
  	    @Override  
  	    public Object getItem(int position) {  
  	        return data_list.get(position);  
  	    }  
  	  
  	    @Override  
  	    public long getItemId(int position) {  
  	        return position;  
  	    }  
  	  
  	    @SuppressLint("ResourceAsColor")
		@Override  
  	    public View getView(int position, View convertView, ViewGroup parent) {  
  	        ViewHolder holder;  
  	        if (convertView == null) {  
  	            convertView = inflater.inflate(R.layout.contact_record_list_item,  
  	                    null);  
  	            holder = new ViewHolder();  
  	            holder.call_person_pic = (ImageView) convertView.findViewById(R.id.call_person_pic);
  	            holder.call_type = (ImageView) convertView.findViewById(R.id.call_type);  
  	            holder.name = (TextView) convertView.findViewById(R.id.name);  
  	            holder.number = (TextView) convertView.findViewById(R.id.number);  
  	            holder.time = (TextView) convertView.findViewById(R.id.time);  
  	            //holder.call_btn = (TextView) convertView.findViewById(R.id.call_btn);  
  	            holder.add_phonebook_btn = (TextView) convertView.findViewById(R.id.add_phonebook_btn);
  	            convertView.setTag(holder); // 缓存  
  	      }else {  
  	            holder = (ViewHolder) convertView.getTag();  
  	        }  
  	  
  	       // CallLogBean callLog = callLogs.get(position); 
  	      HashMap<String,Object > map = new HashMap<String, Object>();
  	        String cachedName = (String) data_list.get(position).get("cachedName");
  	        String Name = (String) data_list.get(position).get("Name");
  	        if(cachedName!=null && !cachedName.equals("")){
  	        	holder.name.setText(cachedName);
  	        	map.put("name", cachedName);
  	        }else{
  	        	holder.name.setText(Name);
  	        	map.put("name", Name);
  	        }
  	        
  	        String person_type=null;
  	        person_type = (String) data_list.get(position).get("person_type");
  	      //分类识别0:全部, 1:家人, 2:朋友, 3:工友, 4:熟人, 5:其他。
  	       if(person_type!=null){
	  	        if(person_type.equals("1")){
	  	        	person_type = "家人";
	  	        }else if(person_type.equals("2")){
	  	        	person_type = "朋友";
	  	        }else if(person_type.equals("3")){
	  	        	person_type = "工友"; 	
	  	        }else if(person_type.equals("4")){
	  	        	person_type = "熟人";
	  	        }else if(person_type.equals("5")){
	  	        	person_type= "其他";	
	  	        }
  	       }else{
  	    	 person_type= "未知";
  	       }
  	        

  	        int type = (Integer) data_list.get(position).get("Type");
  	        switch (type) {  
  	        case 1:  
  	            holder.call_type.setBackgroundResource(R.drawable.ic_calllog_incomming_normal); 
  	            //holder.name.setTextColor(R.color.color_in_call);
  	            break;  
  	        case 2:  
  	            holder.call_type.setBackgroundResource(R.drawable.ic_calllog_outgoing_nomal);  
  	            //holder.name.setTextColor(R.color.color_out_call);
  	            break;  
  	        case 3:  
  	            holder.call_type.setBackgroundResource(R.drawable.ic_calllog_missed_normal);  
  	            //holder.name.setTextColor(R.color.color_miss_call);
  	            break;  
  	        }  
  	         
  	        holder.time.setText((CharSequence) data_list.get(position).get("Date")+"{"+person_type+"}");  
  	        String phone_number = (String) data_list.get(position).get("Number");
  	        holder.number.setText(phone_number);
  	        Bitmap bitmap = (Bitmap) data_list.get(position).get("person_pic");
  	        //String url = (String) data_list.get(position).get("person_pic_url");
  	        imageLoader.displayImage(imageUriArray[position], holder.call_person_pic, options, animateFirstListener);

  	        String exist = (String) data_list.get(position).get("exist");		
  	        map.put("phone_number",phone_number);
  	        
  	        if(bitmap!=null){
  	        	map.put("person_pic", bitmap);
  	        }else{
  	        	map.put("person_pic", null);
  	        }
  	        
  	        if(exist.equals("1")){
  	        	map.put("exist_table", "1");
  	        	holder.add_phonebook_btn.setBackgroundResource(R.drawable.edit_phone_book);
  	        }else{
  	        	map.put("exist_table", "0");
  	        	holder.add_phonebook_btn.setBackgroundResource(R.drawable.add_phone_book);
  	        }
  	        //HashMap<String,String > map1 = new HashMap<String, String>();
  	        //addViewListener(holder.call_btn, map, position);  
  	        addPhoneBookListener(holder.add_phonebook_btn, map, position);
  	        return convertView;  
  	    }  
  	    
  		private  class ViewHolder {
  	    	ImageView call_person_pic;
  	        ImageView call_type;  
  	        TextView name;  
  	        TextView number;  
  	        TextView time;  
  	        //TextView call_btn;  
  	        TextView add_phonebook_btn;
  	    }  
  	  
  	    private void addViewListener(View view, final HashMap<String, Object> data,  
  	            final int position) {  
  	    	
  	        view.setOnClickListener(new OnClickListener() {  
  	  
  	            @Override  
  	            public void onClick(View v) {  
//    	
//  	             	AlertDialog.Builder builder = new Builder(ctx);
//  					builder.setTitle("联系人："+data.get("name"));
//  			    	builder.setMessage(
//  			    			Html.fromHtml("<img src=''/>"+" "+data.get("phone_number"), new ImageGetter() {  
//  	                    @Override  
//  	                    public Drawable getDrawable(String source) {  
//  	                        // TODO Auto-generated method stub  
//  	                    	 Drawable mDrawable = null; 
//  	                    	 Bitmap b = (Bitmap) data.get("person_pic");
//  	                    	if(b!=null){
//  	                    		//Bitmap bm=; //xxx根据你的情况获取  
//  	                    		//获取圆角图片  
//  	               		     //Bitmap roundBitmap = ImageUtil.getRoundedCornerBitmap(getFileBitMap(deletePic), 150.0f);
//  	                    	 mDrawable= new BitmapDrawable(ctx.getResources(), b);
//  	                    	}else{
//  	                    		mDrawable = ctx.getResources().getDrawable(R.drawable.person_default);
//  	                    	}
//  	                        mDrawable.setBounds(0, 0, 200, 200);  
//  	                        return mDrawable;  
//  	                    }  
//  	                },null));
//  			    	
//  			    	builder.setPositiveButton(
//  			    			Html.fromHtml("<img src=''/>拨打", new ImageGetter() {  
//  	                    @Override  
//  	                    public Drawable getDrawable(String source) {  
//  	                        // TODO Auto-generated method stub  
//  	                        Drawable mDrawable = ctx.getResources()  
//  	                                .getDrawable(R.drawable.dialog_call);  
//  	                        mDrawable.setBounds(0, 0, 50, 50);  
//  	                        return mDrawable;  
//  	                    }  
//  	                },null), 
//  	                new DialogInterface.OnClickListener() {
//  			    		@Override
//  			    		public void onClick(DialogInterface dialog, int which) {
//
//  			    			Person person = new Person();
//  							SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");       
//  							Date curDate = new Date(System.currentTimeMillis());//获取当前时间       
//  							String str = formatter.format(curDate);
//  							person.setPublish_time(str);
//  							person.setPhone_number((String) data.get("phone_number"));
//  							mgr.updateotherTimes(person);
//  			                Uri uri = Uri.parse("tel:" + data.get("phone_number"));  
//  			                Intent intent = new Intent(Intent.ACTION_CALL, uri);  
//  			                ctx.startActivity(intent);  		
//  			    		}
//  			    	});
//
//  			    	builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//  			    		@Override
//  			    		public void onClick(DialogInterface dialog, int which) {
//  			    			dialog.dismiss();
//  			    			}
//  			    		});
//  			    	
//  			    	builder.create().show(); 
  	            }  
  	        });  
  	    }  
  	    
  	    private void addPhoneBookListener(View view, final HashMap<String, Object> data,  
  	            final int position) {  
  	        view.setOnClickListener(new OnClickListener() {  
  	  
  	            @Override  
  	            public void onClick(View v) {     
  	            	
  	            	
  	            	AlertDialog.Builder builder = new Builder(ctx);
  					builder.setTitle("添加/修改联系人信息："+data.get("name"));
  			    	builder.setMessage(
  			    			Html.fromHtml("<img src=''/>"+" "+data.get("phone_number"), new ImageGetter() {  
  	                    @Override  
  	                    public Drawable getDrawable(String source) {  
  	                        // TODO Auto-generated method stub  
  	                    	 Drawable mDrawable = null; 
  	                    	 Bitmap b = (Bitmap) data.get("person_pic");
  	                    	if(b!=null){
  	                    		//Bitmap bm=; //xxx根据你的情况获取  
  	                    		//获取圆角图片  
  	               		        //Bitmap roundBitmap = ImageUtil.getRoundedCornerBitmap(getFileBitMap(deletePic), 150.0f);
  	                    	 mDrawable= new BitmapDrawable(ctx.getResources(), b);
  	                    	}else{
  	                    		mDrawable = ctx.getResources().getDrawable(R.drawable.person_default);
  	                    	}
  	                        mDrawable.setBounds(0, 0, 200, 200);  
  	                        return mDrawable;  
  	                    }  
  	                },null));

  			    	if(data.get("exist_table").equals("1"))
  			    	{//已存
  			    		builder.setNeutralButton(
  				    			Html.fromHtml("<img src=''/>修改信息", new ImageGetter() {  
  		                    @Override  
  		                    public Drawable getDrawable(String source) {  
  		                        // TODO Auto-generated method stub  
  		                        Drawable mDrawable = ctx.getResources()  
  		                                .getDrawable(R.drawable.dialog_xiugai);  
  		                        mDrawable.setBounds(0, 0, 50, 50);  
  		                        return mDrawable;  
  		                    }  
  		                },null),new DialogInterface.OnClickListener() {
  				    		@Override
  				    		public void onClick(DialogInterface dialog, int which) {
  				    			Intent intent = new Intent();
  				    			intent.setClass(ctx, UpdateActivity.class);
  				    			intent.putExtra("phone", data.get("phone_number").toString());
  				    			ctx.startActivity(intent);
  				    			}
  				    		});

  				    	builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
  				    		@Override
  				    		public void onClick(DialogInterface dialog, int which) {
  				    			dialog.dismiss();
  				    			}
  				    		});
  			    		
  			    	}
  			    	else
  			    	{//未存
  			    		builder.setPositiveButton(
  				    			Html.fromHtml("<img src=''/>新增", new ImageGetter() {  
  		                    @Override  
  		                    public Drawable getDrawable(String source) {  
  		                        // TODO Auto-generated method stub  
  		                        Drawable mDrawable = ctx.getResources()  
  		                                .getDrawable(R.drawable.dialog_add_person);  
  		                        mDrawable.setBounds(0, 0, 50, 50);  
  		                        return mDrawable;  
  		                    }  
  		                },null), 
  		                new DialogInterface.OnClickListener() {
  				    		@Override
  				    		public void onClick(DialogInterface dialog, int which) {
  				    			Intent intent=new Intent();
  				    			intent.putExtra("phone", data.get("phone_number").toString());
  				    	    	intent.setClass(ctx, AddActivity.class);
  				    	    	ctx.startActivity(intent);
  				    					
  				    		}
  				    	});
  			    		
  			    		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
  				    		@Override
  				    		public void onClick(DialogInterface dialog, int which) {
  				    			dialog.dismiss();
  				    			}
  				    		});
  			    	}

  			    	builder.create().show();
  	            }  
  	        });  
  	    }
  	
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
                    Toast.makeText(ContactRecordListActivity.this, "SD卡不可用，请插入SD卡", Toast.LENGTH_SHORT).show();  
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
        pBar = new ProgressDialog(ContactRecordListActivity.this);    //进度条，在下载的时候实时更新进度，提高用户友好度  
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

//	public boolean onDown(MotionEvent arg0) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	private int verticalMinDistance = 50;   
//    private int minVelocity         = 0; 
//	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//		 if (e1.getX() - e2.getX() > verticalMinDistance && Math.abs(velocityX) > minVelocity) {   
//			  
//		        // 切换Activity   
//		         
//		        // startActivity(intent);   
//		        //Toast.makeText(this, "向左手势", Toast.LENGTH_SHORT).show(); 
//		        Intent intent = new Intent(ContactRecordListActivity.this, MainActivity.class);     
//		        startActivity(intent);   
//		        finish();
//		        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
//		         
//		    } else if (e2.getX() - e1.getX() > verticalMinDistance && Math.abs(velocityX) > minVelocity) {   
//		  
//		        // 切换Activity   
//		    	//Toast.makeText(this, "向右手势", Toast.LENGTH_SHORT).show(); 
//		    }  
//		return false;
//	}
//
//	public void onLongPress(MotionEvent arg0) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
//			float arg3) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	public void onShowPress(MotionEvent arg0) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	public boolean onSingleTapUp(MotionEvent arg0) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public boolean onTouch(View arg0, MotionEvent event) {
//		 return mGestureDetector.onTouchEvent(event);
//		//return false;
//	}
//
//	//private GestureDetector      mGestureDetector;  
//	  
//	@Override  
//	   public boolean dispatchTouchEvent(MotionEvent ev) {  
//	       mGestureDetector.onTouchEvent(ev);  
//	       // scroll.onTouchEvent(ev);  
//	       return super.dispatchTouchEvent(ev);  
//	   }  
	

}  
