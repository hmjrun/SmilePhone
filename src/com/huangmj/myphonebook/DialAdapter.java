package com.huangmj.myphonebook;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;  

import com.example.myphonebook.R;
import com.huangmj.db.DBManager;
import com.huangmj.db.Person;
import com.huangmj.picture.BitmapUtil;
import com.huangmj.picture.ImageUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;


import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;  
import android.content.DialogInterface;
import android.content.Intent;  
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;  
import android.provider.SyncStateContract.Constants;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.util.Log;
import android.view.LayoutInflater;  
import android.view.View;  
import android.view.View.OnClickListener;  
import android.view.ViewGroup;  
import android.widget.AbsListView;
import android.widget.BaseAdapter;  
import android.widget.ImageView;  
import android.widget.ListView;
import android.widget.TextView;  
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
 
  
/** 
 * 电话记录适配器 
 *  
 * @author Administrator 
 *  
 */  
public class DialAdapter extends BaseAdapter{  
  
    private Context ctx;  
  
    private LayoutInflater inflater; 
    private DBManager mgr;
    private List<HashMap<String, Object>> data_list;

//	private ImageLoadingListener animateFirstListener;
//	private DisplayImageOptions options;
//	private ImageLoader imageLoader;
    public DialAdapter(Context context,List<HashMap<String, Object>> data) {  
        this.ctx = context;  
        this.data_list = data;  
        this.inflater = LayoutInflater.from(ctx);  
        this.mgr = new DBManager(ctx); 
        //this.listView = listview;
        //this.animateFirstListener= animateFirstListenerr;
        //this.options =optionss; 
        //this.imageLoader = imageLoaderr;
        //设置第一次载入程序，布尔值为true
      	//isFirst = true;
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
        int type = (Integer) data_list.get(position).get("Type");
        switch (type) {  
        case 1:  
            holder.call_type.setBackgroundResource(R.drawable.ic_calllog_incomming_normal);  
            break;  
        case 2:  
            holder.call_type.setBackgroundResource(R.drawable.ic_calllog_outgoing_nomal);  
            break;  
        case 3:  
            holder.call_type.setBackgroundResource(R.drawable.ic_calllog_missed_normal);  
            break;  
        }  
        String name = (String) data_list.get(position).get("Name");
        holder.name.setText(name);  
        holder.time.setText((CharSequence) data_list.get(position).get("Date"));  
        String phone_number = (String) data_list.get(position).get("Number");
        holder.number.setText(phone_number);
        Bitmap bitmap = (Bitmap) data_list.get(position).get("person_pic");
        String url = (String) data_list.get(position).get("person_pic_url");
        //imageLoader.displayImage(url, holder.call_person_pic, options, animateFirstListener);
        
        if (url != null && !url.equals("")) {
                    // 通过 tag 来防止图片错位
                    if (holder.call_person_pic.getTag() != null && holder.call_person_pic.getTag().equals(url)) {
                    	if(bitmap!=null){
                    		holder.call_person_pic.setImageBitmap(bitmap);
                    	}
                    	
                    }
   
          }
        
        String exist = (String) data_list.get(position).get("exist");		
        HashMap<String,Object > map = new HashMap<String, Object>();
        map.put("name", name);
        map.put("phone_number",phone_number);
        
        if(bitmap!=null){
        	map.put("person_pic", bitmap);
        }else{
        	map.put("person_pic", null);
        }
        
        if(exist.equals("1")){
        	map.put("exist_table", "1");
        }else{
        	map.put("exist_table", "0");
        }
        HashMap<String,String > map1 = new HashMap<String, String>();
        addViewListener(holder.call_btn, map, position);  
        addPhoneBookListener(holder.add_phonebook_btn, map, position);
        return convertView;  
    }  
    
	private static class ViewHolder {
    	ImageView call_person_pic;
        ImageView call_type;  
        TextView name;  
        TextView number;  
        TextView time;  
        TextView call_btn;  
        TextView add_phonebook_btn;
    }  
  
    private void addViewListener(View view, final HashMap<String, Object> data,  
            final int position) {  
        view.setOnClickListener(new OnClickListener() {  
  
            @Override  
            public void onClick(View v) {  
            	
             	AlertDialog.Builder builder = new Builder(ctx);
				builder.setTitle("联系人："+data.get("name"));
		    	builder.setMessage(
		    			Html.fromHtml("<img src=''/>"+"号码："+data.get("phone_number"), new ImageGetter() {  
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
		    	
		    	builder.setPositiveButton(
		    			Html.fromHtml("<img src=''/>拨打电话", new ImageGetter() {  
                    @Override  
                    public Drawable getDrawable(String source) {  
                        // TODO Auto-generated method stub  
                        Drawable mDrawable = ctx.getResources()  
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
						person.setPhone_number((String) data.get("phone_number"));
						mgr.updateotherTimes(person);
		                Uri uri = Uri.parse("tel:" + data.get("phone_number"));  
		                Intent intent = new Intent(Intent.ACTION_CALL, uri);  
		                ctx.startActivity(intent);  		
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
    }  
    
    private void addPhoneBookListener(View view, final HashMap<String, Object> data,  
            final int position) {  
        view.setOnClickListener(new OnClickListener() {  
  
            @Override  
            public void onClick(View v) {     
            	
            	
            	AlertDialog.Builder builder = new Builder(ctx);
				builder.setTitle("联系人："+data.get("name"));
		    	builder.setMessage(
		    			Html.fromHtml("<img src=''/>"+"号码："+data.get("phone_number"), new ImageGetter() {  
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
			    			Html.fromHtml("<img src=''/>修改联系人信息", new ImageGetter() {  
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



