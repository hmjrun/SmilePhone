package com.huangmj.myphonebook;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.huangmj.picture.BitmapUtil;

public class FileUtil {
	//private Context ctx; 
	//��SD���ļ�ɾ��
    public static void  deleteFile(File file){
    	//if(sdState.equals(Environment.MEDIA_MOUNTED)){
	    	if (file.exists()){
	    		if (file.isFile()){
	    			file.delete();
	    		}else if (file.isDirectory()){// �������һ��Ŀ¼
	    			// ����Ŀ¼�����е��ļ� files[];
	    			File files[] = file.listFiles();
	    			for (int i = 0; i < files.length; i++){
	    	    	// ����Ŀ¼�����е��ļ�
	    	    	deleteFile(files[i]); // ��ÿ���ļ� ������������е���
	    			}
	    		}
	    		file.delete();
	    	}
    }
    
 
    
    
}
