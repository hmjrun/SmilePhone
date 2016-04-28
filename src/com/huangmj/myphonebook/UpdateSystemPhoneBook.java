package com.huangmj.myphonebook;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

import com.example.myphonebook.R;
import com.huangmj.picture.BitmapUtil;

import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.net.Uri;

import android.provider.ContactsContract;  
  
import android.provider.ContactsContract.CommonDataKinds.Phone;  
import android.provider.ContactsContract.CommonDataKinds.Photo;  
import android.provider.ContactsContract.CommonDataKinds.StructuredName;  
import android.provider.ContactsContract.Contacts.Data;  
import android.provider.ContactsContract.RawContacts;
import android.widget.Toast;

public class UpdateSystemPhoneBook {
	private Context mcontext;
	public UpdateSystemPhoneBook(Context context){
		this.mcontext = context;
	}
	
	//�浽ϵͳͨѶ¼
	public long insert(String given_name, String mobile_number,String filePath) {  
		long rawContactId;
        try {  
            ContentValues values = new ContentValues();  
  
            // ����Ĳ��������RawContacts�������е�rawContactIdʹ������Զ���������ϵ�˵�rawContactId  
            Uri rawContactUri = mcontext.getContentResolver().insert(RawContacts.CONTENT_URI, values);  
            rawContactId = ContentUris.parseId(rawContactUri);  
  
            // ��data�������������  
            if (given_name != "") {  
                values.clear();  
                values.put(Data.RAW_CONTACT_ID, rawContactId);  
                values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);  
                values.put(StructuredName.GIVEN_NAME, given_name);  
                mcontext.getContentResolver().insert(ContactsContract.Data.CONTENT_URI,values);  
            }  
  
            // ��data�����绰����  
            if (mobile_number != "") {  
                values.clear();  
                values.put(Data.RAW_CONTACT_ID, rawContactId);  
                values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);  
                values.put(Phone.NUMBER, mobile_number);  
                values.put(Phone.TYPE, Phone.TYPE_MOBILE);  
                mcontext.getContentResolver().insert(ContactsContract.Data.CONTENT_URI,  
                        values);  
            }  
            
            // ��data�����ͷ������  
            //Bitmap sourceBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.icon); 
            if(filePath!=null){
            	Bitmap sourceBitmap = getFileBitMap(filePath);
                final ByteArrayOutputStream os = new ByteArrayOutputStream();  
                // ��Bitmapѹ����PNG���룬����Ϊ100%�洢  
                sourceBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);  
                byte[] avatar = os.toByteArray();  
                values.put(Data.RAW_CONTACT_ID, rawContactId);  
                values.put(Data.MIMETYPE, Photo.CONTENT_ITEM_TYPE);  
                values.put(Photo.PHOTO, avatar);  
                mcontext.getContentResolver().insert(ContactsContract.Data.CONTENT_URI,  
                        values);  
            }else{
            	Resources res = mcontext.getResources();  
        	   	Bitmap sourceBitmap = BitmapFactory.decodeResource(res, R.drawable.person_default); 
            	//Bitmap sourceBitmap = getFileBitMap(filePath);
                final ByteArrayOutputStream os = new ByteArrayOutputStream();  
                // ��Bitmapѹ����PNG���룬����Ϊ100%�洢  
                sourceBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);  
                byte[] avatar = os.toByteArray();  
                values.put(Data.RAW_CONTACT_ID, rawContactId);  
                values.put(Data.MIMETYPE, Photo.CONTENT_ITEM_TYPE);  
                values.put(Photo.PHOTO, avatar);  
                mcontext.getContentResolver().insert(ContactsContract.Data.CONTENT_URI,  
                        values);  
            }
        }  
  
        catch (Exception e) {  
        	Toast.makeText(mcontext, "Exception:"+e.toString(), Toast.LENGTH_LONG).show();
            return 0;  
        }  
        //Toast.makeText(mcontext, "rawContactId:"+rawContactId, Toast.LENGTH_LONG).show();
        return rawContactId;  
    }  
	
	//�������ݿ�����ϵ��
    public  boolean ChangeContact(String name, String number,String filePath, String rawContactId){
        ContentValues values = new ContentValues();
        
        // ��������
        values.put(StructuredName.GIVEN_NAME, name);
        mcontext.getContentResolver().update(ContactsContract.Data.CONTENT_URI,values,
        		Data.RAW_CONTACT_ID + "=? and " + Data.MIMETYPE  + "=?",
                new String[] { rawContactId,StructuredName.CONTENT_ITEM_TYPE });

        //���µ绰
        values.clear();
        values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, number);
        mcontext.getContentResolver().update(ContactsContract.Data.CONTENT_URI,
                values,
                Data.RAW_CONTACT_ID + "=? and " + Data.MIMETYPE  + "=?",
                new String[] { rawContactId,Phone.CONTENT_ITEM_TYPE});
        
        //����ͼƬ
        values.clear();
        if(filePath!=null){
        	Bitmap sourceBitmap = getFileBitMap(filePath);
            final ByteArrayOutputStream os = new ByteArrayOutputStream();  
            // ��Bitmapѹ����PNG���룬����Ϊ100%�洢  
            sourceBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);  
            byte[] avatar = os.toByteArray();  
            values.put(Data.RAW_CONTACT_ID, rawContactId);  
            values.put(Data.MIMETYPE, Photo.CONTENT_ITEM_TYPE);  
            values.put(Photo.PHOTO, avatar);  
            mcontext.getContentResolver().update(ContactsContract.Data.CONTENT_URI,
            		values,
            		Data.RAW_CONTACT_ID + "=? and " + Data.MIMETYPE  + "=?",
                    new String[] { rawContactId,Photo.CONTENT_ITEM_TYPE});
       }else{
    	   	Resources res = mcontext.getResources();  
    	   	Bitmap sourceBitmap = BitmapFactory.decodeResource(res, R.drawable.person_default); 
            //Bitmap sourceBitmap = getFileBitMap(filePath);
            final ByteArrayOutputStream os = new ByteArrayOutputStream();  
            // ��Bitmapѹ����PNG���룬����Ϊ100%�洢  
            sourceBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);  
            byte[] avatar = os.toByteArray();  
        	values.put(Data.RAW_CONTACT_ID, rawContactId);  
            values.put(Data.MIMETYPE, Photo.CONTENT_ITEM_TYPE);  
              
            mcontext.getContentResolver().update(ContactsContract.Data.CONTENT_URI,
            values,
            Data.RAW_CONTACT_ID + "=? and " + Data.MIMETYPE  + "=?",
            new String[] { rawContactId,Photo.CONTENT_ITEM_TYPE});
        }
       
        return true;
    }

	
	private Bitmap getFileBitMap(String filePath){
    	Bitmap bitmap = null;
    	Uri uri = Uri.fromFile(new File(filePath));
    	bitmap = BitmapUtil.decodeUriAsBitmap(mcontext, uri);
    	return bitmap;
    }
	
	//ɾ��ͨѶ¼��ϵ��
	public boolean deletePserson(String rawContactId){
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		//delete contact  
        ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)  
                .withSelection(ContactsContract.RawContacts.CONTACT_ID+"="+rawContactId, null)  
                .build());  
        //delete contact information such as phone number,email  
        ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)  
                .withSelection(ContactsContract.Data.CONTACT_ID + "=" + rawContactId, null)  
                .build());  
        
        try {  
        	mcontext.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);  
            //Log.d(TAG, "delete contact success");  
        } catch (Exception e) {  
            //Log.d(TAG, "delete contact failed");  
            //Log.e(TAG, e.getMessage());  
        	return false;
        }  
        return true;
	}
	
	
	
}
