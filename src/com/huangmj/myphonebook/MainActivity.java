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
 * Ŀ��Ӧ����Ⱥ���ֻ��������������ѵ�ũ����һ����Ⱥ�����ܰ�׿�ֻ��ֻ�ͨѶ¼��Ա��Ϣ¼��(����ͼƬ������¼��)
 * 
 * δ�������
 * 2016.4.4 @huangmj
 * -1,������ɾ����Ϣ,����绰,ͼƬ��ȡ(��������)
 * -2,�Ի���ͼƬ�ఴť
 * -3,�ֻ���������ʾ3 4 4
 * -4,�汾�������߸���(�����������£���Ա�����������)
 * -5,��ͼȷ�ϱ���
 * -6,����ID����ɾ��
 * -7,��ȥ����ʾ��ϵ��ͼƬ
 * -8,Ӧ����ʾĬ��ͷ��һԲ���ף�Բ�ǡ�
 * -9,�����治��ѡͼƬ��֧�ֺ�����ѡ�������Ż���
 * 
 * -10,��ȡͨ����¼��ʾͼƬ
 * 11,����ϵ����Ϣ����(�ֻ���ʧ���˻�һ����ԭ��Ŀ��Ⱥ���������ƣ�����)
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
    
    
 // ���°汾Ҫ�õ���һЩ��Ϣ  
    private UpdateInfo info;  
    private ProgressDialog pBar; 
   
    private ArrayList<HashMap<String, Object>> lstImageItem;
    
	/**
	 * ��Ҫ��ʾ��ͼƬurl��ַ
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

        //��ʼ��DBManager  
        mgr = new DBManager(this);
        
        ArrayList<String> listImgPaths = initData();
        imageUrls =  (String[]) listImgPaths.toArray(new String[listImgPaths.size()]);
        for (int i = 0; i < imageUrls.length; i++) {
        	imageUrls[i] = "file://" + imageUrls[i];
        }
        
        options = new DisplayImageOptions.Builder().cacheInMemory(true) // ����ͼƬʱ�����ڴ��м��ػ���
        		.cacheOnDisc(true) // ����ͼƬʱ���ڴ����м��ػ���
        		.displayer(new RoundedBitmapDisplayer(90)) // �����û�����ͼƬtask(������Բ��ͼƬ��ʾ)		
        		.showStubImage(R.drawable.person_default) // ��ImageView���ع�������ʾͼƬ
        		.showImageForEmptyUri(R.drawable.person_default) // image���ӵ�ַΪ��ʱ
        		.showImageOnFail(R.drawable.person_default) // image����ʧ��
        		.build();
        
        adapter = new PictureAdapter( this,lstImageItem); 
        ((GridView) listView).setAdapter(adapter);
        
        LayoutAnimationController lac=new LayoutAnimationController(AnimationUtils.loadAnimation(this, R.anim.zoom_in_grid));
	    lac.setOrder(LayoutAnimationController.ORDER_NORMAL);
	    listView.setLayoutAnimation(lac);
	    listView.startLayoutAnimation();
        
        //���item����绰
        listView.setOnItemClickListener(new OnItemClickListener(){ 
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,long id) {
				call_phoneNumber = (String) lstImageItem.get(position).get("phone");
				call_phoneName = (String) lstImageItem.get(position).get("name");
				deletePic = (String) lstImageItem.get(position).get("pic_url");
				
            	AlertDialog.Builder builder = new Builder(MainActivity.this);
				builder.setTitle("��ϵ�ˣ�"+call_phoneName);
		    	builder.setMessage(
		    			Html.fromHtml("<img src=''/>"+" "+call_phoneNumber, new ImageGetter() {  
                    @Override  
                    public Drawable getDrawable(String source) {  
                        // TODO Auto-generated method stub  
                    	 Drawable mDrawable = null; 
                    	if(deletePic!=null){
                    		//Bitmap bm=; //xxx������������ȡ  
                    		//��ȡԲ��ͼƬ  
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
		    			Html.fromHtml("<img src=''/>����", new ImageGetter() {  
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
						SimpleDateFormat formatter = new SimpleDateFormat("yyyy��MM��dd�� HH:mm:ss");       
						Date curDate = new Date(System.currentTimeMillis());//��ȡ��ǰʱ��       
						String str = formatter.format(curDate);
						person.setPublish_time(str);
						person.setPhone_number(call_phoneNumber);
						mgr.updateotherTimes(person);
						//��intent��������绰  
		                Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+call_phoneNumber));  
		                startActivity(intent);  		
		    		}
		    	});
		    	
		    	
		    	builder.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
		    		@Override
		    		public void onClick(DialogInterface dialog, int which) {
		    			dialog.dismiss();
		    			}
		    		});
		    	
		    	builder.create().show();
            } 
        });
        
        //����ɾ�����༭��ϵ��
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
				builder.setTitle("��ϵ�ˣ�"+deleteName);
				
		    	builder.setMessage(
		    			Html.fromHtml("<img src=''/>"+" "+deletePhone, new ImageGetter() {  
                    @Override  
                    public Drawable getDrawable(String source) {  
                        // TODO Auto-generated method stub  
                    	 Drawable mDrawable = null; 
                     	if(deletePic!=null){
                     		//Bitmap bm=getFileBitMap(deletePic); //xxx������������ȡ  
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
		    			Html.fromHtml("<img src=''/>ɾ��", new ImageGetter() {  
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
				    					Toast.makeText(MainActivity.this, "ɾ����ϵ�˳ɹ�rawContactId:"+rawContactId, Toast.LENGTH_LONG).show();
		    						}
		    					}
		    					
		    				}
		    			}		
		    		}
		    	});
		    	
		    	builder.setNeutralButton(
		    			Html.fromHtml("<img src=''/>�޸�", new ImageGetter() {  
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
		    	
		    	builder.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
		    		@Override
		    		public void onClick(DialogInterface dialog, int which) {
		    			dialog.dismiss();
		    			}
		    		});
		    	
		    	builder.create().show();
				
				return true;
			}
		});
        
        // �Զ������û���°汾 ������°汾����ʾ����  
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
		
		//1,�O�Ô���Դ	
		list = new ArrayList<String>();
		list.add("ȫ��");
		list.add("����");
		list.add("����");
		list.add("����");
		list.add("����");
		list.add("����");
		
		//2,�½�ArrayAdapter
		add_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
		
		//3,adapter�O��һ�������б��ʽ
		add_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		//4,�ospinner���d�m����
		spinner.setAdapter(add_adapter);
		
		//5,spinner�O�ñO ��
		spinner.setOnItemSelectedListener(this);
    
    }
    
    private Handler handler1 = new Handler() {  
        public void handleMessage(Message msg) {  
            // ����и��¾���ʾ  
            if (isNeedUpdate()) {
            	//����Ĵ����  
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
    
    //��ʼ����sqllite���ݿ�ȡ��ʱ����
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
    			//��ȡԲ��ͼƬ  
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
    
    //��sqllite���ݿ�ȡ��ʱ����Type
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
    			//��ȡԲ��ͼƬ  
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
        //Ӧ�õ����һ��Activity�ر�ʱӦ�ͷ�DB  
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
				 Bundle b=data.getExtras(); //dataΪB�лش���Intent
		         String str=b.getString("Action");//�ش��Ķ���ֵ
		         Toast.makeText(this, "�����ϵ�˳ɹ���", Toast.LENGTH_LONG).show();
		         if(str.equals("call")){
		        	 String phone=b.getString("phone");
		        	 save_call_phone(phone);
		         }
			}
			if(requestCode == RE_Edit){
				 Bundle b=data.getExtras(); //dataΪB�лش���Intent
		         String str=b.getString("Action");//�ش��Ķ���ֵ
		         Toast.makeText(this,"�޸���ϵ�˳ɹ���" , Toast.LENGTH_LONG).show();
//		         if(str.equals("call")){
//		        	 String phone=b.getString("phone");
//		        	 save_call_phone(phone);
//		         }
			}
    	}
    }
    
	//���沢����绰
	public void save_call_phone(String phoneNumber){
		//��intent��������绰  
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
    
    //�Ի���ѯ���Ƿ���Ҫ����
    private void showUpdateDialog() {  
        AlertDialog.Builder builder = new AlertDialog.Builder(this);  
        builder.setIcon(android.R.drawable.ic_dialog_info);  
        builder.setTitle("������APP���汾" + info.getVersion());  
        builder.setMessage(info.getDescription());  
        //builder.setCancelable(false);  
      
        builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {  
      
            @Override  
            public void onClick(DialogInterface dialog, int which) {  
                if (Environment.getExternalStorageState().equals(  
                        Environment.MEDIA_MOUNTED)) {  
                    downFile(info.getUrl());     //������Ĵ����  
                } else {  
                    Toast.makeText(MainActivity.this, "SD�������ã������SD��", Toast.LENGTH_SHORT).show();  
                }  
            }  
        });  
        builder.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {  
      
            @Override  
            public void onClick(DialogInterface dialog, int which) {  
            }  
      
        });  
        builder.create().show();  
    }  
     
    //�ж��Ƿ���Ҫ����
    private boolean isNeedUpdate() {  
    	// ���°汾�İ汾��  
        String v = info.getVersion();
        //Log.i("update",v);  
        //Toast.makeText(MainActivity.this, v, Toast.LENGTH_SHORT).show();  
        if (v.equals(getVersion())) {  
            return false;  
        } else {  
            return true;  
        }  
    }  
      
    // ��ȡ��ǰ�汾�İ汾��  
    private String getVersion() {  
        try {  
            PackageManager packageManager = getPackageManager();  
            PackageInfo packageInfo = packageManager.getPackageInfo(  
                    getPackageName(), 0);  
            return packageInfo.versionName;  
        } catch (NameNotFoundException e) {  
            e.printStackTrace();  
            return "�汾��δ֪";  
        }  
    }  
    
    //����apk�ļ�
    void downFile(final String url) {   
        pBar = new ProgressDialog(MainActivity.this);    //�������������ص�ʱ��ʵʱ���½��ȣ�����û��Ѻö�  
        pBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);  
        pBar.setTitle("��������");  
        pBar.setMessage("���Ժ�...");  
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
                    //��ȡ�ļ���С  
                    pBar.setMax(length);                            
                    //���ý��������ܳ���  
                    InputStream is = entity.getContent();  
                    FileOutputStream fileOutputStream = null;  
                    if (is != null) {  
                        File file = new File(  
                                Environment.getExternalStorageDirectory(),  
                                "myPhoneBook.apk");  
                        fileOutputStream = new FileOutputStream(file);  
                        byte[] buf = new byte[10];   //����ǻ���������һ�ζ�ȡ10�����أ���Ū��С�˵㣬��Ϊ�ڱ��أ�������ֵ̫��һ �¾��������ˣ�������progressbar��Ч����  
                        int ch = -1;  
                        int process = 0;  
                        while ((ch = is.read(buf)) != -1) {         
                            fileOutputStream.write(buf, 0, ch);  
                            process += ch;  
                            pBar.setProgress(process);       //������ǹؼ���ʵʱ���½����ˣ�  
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
  
    //������ɣ���������ʧ����װ
    void down() {  
        handler1.post(new Runnable() {  
            public void run() {  
                pBar.cancel();  
                update();  
            }  
        });  
    }  
    
    //��װ�ļ���һ��̶�д��  
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
		//����ʶ��0:ȫ��, 1:����, 2:����, 3:����, 4:����, 5:������
		if(typename.equals("����")){
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
		}else if(typename.equals("����")){
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
		}else if(typename.equals("����")){
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
		}else if(typename.equals("����")){
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
		}else if(typename.equals("����")){
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
		}else if(typename.equals("ȫ��")){
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
