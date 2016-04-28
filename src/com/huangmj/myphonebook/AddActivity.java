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
	//����ʶ��0:ȫ��, 1:����, 2:����, 3:����, 4:����, 5:������
	private int person_type=5;

	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_person);
        //��ʼ��DBManager  
        mgr = new DBManager(this); 
        mCropParams = new CropParams(this);
        imageview = (ImageView) findViewById(R.id.imagofperson);
        editNumber = (EditText) findViewById(R.id.editNumber);
        editName = (EditText) findViewById(R.id.editName);
        but_save = (Button) findViewById(R.id.submit_but);
        //but_save_call = (Button) findViewById(R.id.submit_call_but);
        
        /*��ȡIntent�е�Bundle����*/
        Bundle bundle = this.getIntent().getExtras();
        
        /*��ȡBundle�е����ݣ�ע�����ͺ�key*/
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
				//��ֹ�û�û�������ύ
				if(p != null){
					//�жϸ��ֻ����Ƿ������ݿ����Ѵ棬������߸���
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
        
        //����Edtext��������ֻ���123 4567 8901
        editNumber.addTextChangedListener(new PhoneTextWatcher(editNumber));
        
        spinner = (Spinner) findViewById(R.id.add_spinner_person_type);
		
		//1,�O�Ô���Դ	
		list = new ArrayList<String>();
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
	
	
	
	public void choosePic(View view){
		AlertDialog.Builder builder = new Builder(this);
    	builder.setMessage("��ѡ����Ƭ��Դ");
    	builder.setTitle("��ʾ");
    	builder.setPositiveButton(Html.fromHtml("<img src=''/>���", new ImageGetter() {  
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
    	
    	builder.setNegativeButton(Html.fromHtml("<img src=''/>���", new ImageGetter() {  
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
//				Toast.makeText(this, "��ϵ��ͷ��ͱ�ע���ܶ�Ϊ�գ�������һ ", Toast.LENGTH_LONG).show();
//				return null;
//			}
			String phone_number  = editNumber.getText().toString();	
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy��MM��dd��    HH:mm:ss");       
			Date curDate = new Date(System.currentTimeMillis());//��ȡ��ǰʱ��       
			String str = formatter.format(curDate);
			//��װPerson
			Person person = new Person();
			person.setPublish_time(str);
			person.setOther_times(person_type);
			person.setPhone_number(phone_number);
			person.setPic_path(FilePath);	
			//�û�δ����ϵ�˱�ע�������ֻ��Ŵ���
			if(phone_name==null){
				person.setPerson_name(phone_number);
			}else{
				person.setPerson_name(phone_name);
			}
			return person;	
		}else{
			Toast.makeText(this, "�������ϵ�����ֻ��� ", Toast.LENGTH_LONG).show();
		}
		
		return null;
	}
	
	
	//���沢����绰
	public void save_call_phone(String phoneNumber){
		//��intent��������绰  
		Intent intent = new Intent(AddActivity.this,MainActivity.class);
		intent.putExtra("Action", "call");
        String passString = phoneNumber;
        intent.putExtra("phone", phoneNumber);
        setResult(RESULT_OK, intent);
        finish(); 
	}
	
	//�˳���activity
	public void exit_(){
		Intent intent = new Intent(AddActivity.this,MainActivity.class);
		//Toast.makeText(this, "�����ϵ�˳ɹ���", Toast.LENGTH_LONG).show();
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
    	
    	//���ݿ����иú���ļ�¼��ִ�и��²���
    	if(mgr.queryNumberPersonBy_phone(person)>0){
    		//Toast.makeText(this, "���иú���ļ�¼", Toast.LENGTH_LONG).show();
    		Person old_person = mgr.queryPersonBy_phone(person);
    		person.set_id(old_person.get_id());
    		//д�����ݵ����ݿ���
			if(mgr.updatePersonById(person)){
				UpdateSystemPhoneBook ups= new UpdateSystemPhoneBook(AddActivity.this);
				//��ϵ����ԭͼ
				if(old_person.getPic_path()!=null){
					//�������ݵ�ϵͳ���ݿ���
					//�û����´�ú�����ϵ�ˣ�δ��ȡͼƬ
					if(person.getPic_path()!=null){
						ups.ChangeContact(person.getPerson_name(), person.getPhone_number(), person.getPic_path(), old_person.getRawContactId()+"");
						//�޸���ԭͼ
						if(!person.getPic_path().equals(old_person.getPic_path())){
							//ɾ����ϵ��ԭͼ
							FileUtil.deleteFile(new File(old_person.getPic_path()));
						}
					}else{
						ups.ChangeContact(person.getPerson_name(), person.getPhone_number(), null, old_person.getRawContactId()+"");
						FileUtil.deleteFile(new File(old_person.getPic_path()));
					}
					
				//��ϵ��û��ԭͼ
				}else{
					//�������ݵ�ϵͳͨѶ¼���ݿ���
					ups.ChangeContact(person.getPerson_name(), person.getPhone_number(), person.getPic_path(), old_person.getRawContactId()+"");
				}
			}else{
				Toast.makeText(this, "�޸���ϵ��ʧ��", Toast.LENGTH_LONG).show();
			}
			
		//���ݿ�û���иú���ļ�¼��ִ�в������
    	}else{
    		//Toast.makeText(this, "ľ�иú���ļ�¼", Toast.LENGTH_LONG).show();
    		UpdateSystemPhoneBook ups = new UpdateSystemPhoneBook(AddActivity.this);
            long rawContactId=0;
            if(person.getPerson_name().equals("")){
            	rawContactId = ups.insert(person.getPhone_number(), person.getPhone_number(), FilePath);
           }else{
                rawContactId = ups.insert(person.getPerson_name(), person.getPhone_number(), FilePath);
            }
            //Toast.makeText(this, "ľ�иú���ļ�¼,�¼�¼��"+rawContactId, Toast.LENGTH_LONG).show();
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
	    	//intent.putExtra("extra", "����ҳ��һ������ֵ��");
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
		//����ʶ��0:ȫ��, 1:����, 2:����, 3:����, 4:����, 5:������
		String typename = add_adapter.getItem(position);
		if(typename.equals("����")){
			person_type = 1;
		}else if(typename.equals("����")){
			person_type = 2;
		}else if(typename.equals("����")){
			person_type = 3;
		}else if(typename.equals("����")){
			person_type = 4;
		}else if(typename.equals("����")){
			person_type = 5;
		}
		
	}



	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

}
