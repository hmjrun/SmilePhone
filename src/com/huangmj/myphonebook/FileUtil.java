package com.huangmj.myphonebook;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.huangmj.picture.BitmapUtil;

public class FileUtil {
	//private Context ctx; 
	//将SD卡文件删除
    public static void  deleteFile(File file){
    	//if(sdState.equals(Environment.MEDIA_MOUNTED)){
	    	if (file.exists()){
	    		if (file.isFile()){
	    			file.delete();
	    		}else if (file.isDirectory()){// 如果它是一个目录
	    			// 声明目录下所有的文件 files[];
	    			File files[] = file.listFiles();
	    			for (int i = 0; i < files.length; i++){
	    	    	// 遍历目录下所有的文件
	    	    	deleteFile(files[i]); // 把每个文件 用这个方法进行迭代
	    			}
	    		}
	    		file.delete();
	    	}
    }
    
 
    
    
}
