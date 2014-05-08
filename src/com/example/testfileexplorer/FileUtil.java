package com.example.testfileexplorer;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.StatFs;
import android.webkit.MimeTypeMap;

public class FileUtil {
	/**
	 * 获取文件的扩展名，如，xxx.txt，获得的扩展名就是"txt" 
	 * @param file
	 * @return
	 */
	public static String getFileExtension(File file) {
		if (file == null || !file.exists()) {
			return null;
		}
		if (file.isDirectory()) {
			return null;
		}
		String filename = file.getName();
		int end = filename.lastIndexOf(".");
		if (end == -1) { //该文件没有扩展名
			return null;
		}
		String ext = filename.substring(end + 1);
		return ext.toLowerCase(Locale.getDefault());
	}
	
	/**
	 * 获取文件的扩展名，如，xxx.txt，获得的扩展名就是"txt" 
	 * @param filename
	 * @return
	 */
	public static String getFileExtension(String filename) {
		if (filename == null || "".equals(filename)) {
			return null;
		}
		File file = new File(filename);
		return getFileExtension(file);
	}
	
	/**
	 * 获得文件的mimeType
	 * @param file
	 * @return
	 */
	public static String getMimeType(String ext) {
		MimeTypeMap typeMap = MimeTypeMap.getSingleton();
		String type = typeMap.getMimeTypeFromExtension(ext);
		if (type == null) {
			return null;
		}
		String mimeType = type.substring(0, type.indexOf("/") + 1) + "*";
		return mimeType;
	}
	
	/*
     * 采用了新的办法获取APK图标，之前的失败是因为android中存在的一个BUG,通过
     * appInfo.publicSourceDir = apkPath;来修正这个问题，详情参见:
     * http://code.google.com/p/android/issues/detail?id=9151
     */
    public static Drawable getApkIcon(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath,
                PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = apkPath;
            appInfo.publicSourceDir = apkPath;
            try {
                return appInfo.loadIcon(pm);
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    // storage, G M K B
    public static String convertStorage(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format("%.1f G", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f M" : "%.1f M", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f K" : "%.1f K", f);
        } else
            return String.format("%d B", size);
    }
    
    /** 
     * 获得SD卡总大小 
     *  
     * @return 
     */  
    public static String getSDTotalSize() {  
        File path = Environment.getExternalStorageDirectory();  
        StatFs stat = new StatFs(path.getPath());  
        long blockSize = stat.getBlockSize();  
        long totalBlocks = stat.getBlockCount();  
        return convertStorage(totalBlocks * blockSize); 
    }
    
    /** 
     * 获得sd卡剩余容量，即可用大小 
     *  
     * @return 
     */  
    public static String getSDAvailableSize() {  
        File path = Environment.getExternalStorageDirectory();  
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return convertStorage(blockSize * availableBlocks); 
    }
    
    /** 
     * 获得机身内容总大小 
     *  
     * @return 
     */  
    public static String getRomTotalSize() {  
        File path = Environment.getDataDirectory();  
        StatFs stat = new StatFs(path.getPath());  
        long blockSize = stat.getBlockSize();  
        long totalBlocks = stat.getBlockCount();  
        return convertStorage(totalBlocks * blockSize); 
    }
    
    /** 
     * 获得机身可用内存 
     *  
     * @return 
     */  
    public static String getRomAvailableSize() {  
        File path = Environment.getDataDirectory();  
        StatFs stat = new StatFs(path.getPath());  
        long blockSize = stat.getBlockSize();  
        long availableBlocks = stat.getAvailableBlocks();  
        return convertStorage(blockSize * availableBlocks);
    }
    
    public static boolean isSDCardReady() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
    
    public static File getSdcardPath() {
    	return Environment.getExternalStorageDirectory();
    }
    
    public static List<File> getSubFiles(File parent) {
    	if(parent.isDirectory() && parent.canRead()) {
    		File[] fs = parent.listFiles();
    		Arrays.sort(fs, new Comparator<File>() {

				@Override
				public int compare(File lhs, File rhs) {
					if(lhs.isDirectory() && rhs.isFile()) {
						return -1;
					} else if(lhs.isFile() && rhs.isDirectory()) {
						return 1;
					} else {
						return lhs.compareTo(rhs);
					}
				}
			});
    		return Arrays.asList(fs);
    	}
    	return null;
    }
    
    public static List<File> getSubFiles(String parent) {
    	File file = new File(parent);
    	return getSubFiles(file);
    }
    
    public static boolean isSdcardRoot(File path) {
    	if(getSdcardPath().getAbsolutePath().equals(path.getAbsolutePath())) {
    		return true;
    	}
    	return false;
    }
}
