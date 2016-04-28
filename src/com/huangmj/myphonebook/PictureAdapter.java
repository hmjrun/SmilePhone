package com.huangmj.myphonebook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.example.myphonebook.R;

import android.content.Context;
import android.graphics.Bitmap;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
           // viewHolder.person_name = (TextView) convertView.findViewById(R.id.tv_person_name);
            convertView.setTag(viewHolder); 
        } else
        { 
            viewHolder = (ViewHolder) convertView.getTag(); 
        } 
        //String id = (String) data_list.get(position).get("id");
        viewHolder.person_number.setText((CharSequence) data_list.get(position).get("phone")); 
        if(data_list.get(position).get("pic")==null){
       	
        }else{
        	viewHolder.person_image.setImageBitmap((Bitmap) data_list.get(position).get("pic")); 
        }
        
        //viewHolder.person_name.setText((CharSequence) data_list.get(position).get("name"));
    
        return convertView; 
    } 

} 
 
class ViewHolder 
{ 
	//public TextView person_name;
    public TextView person_number; 
    public ImageView person_image; 
} 
 
