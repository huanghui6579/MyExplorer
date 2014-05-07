package com.example.testfileexplorer;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final String TAG = "FileScan";
	
	static final String MIME_IMAGE = "image/*";
	static final String MIME_MUSIC = "audio/*";
	static final String MIME_VIDEO = "video/*";
	
	static final int TYPE_IMAGE = 1;
	static final int TYPE_MUSIC = 2;
	static final int TYPE_VIDEO = 3;
	
	private Context mContext;
	
	private GridView gvFileCategory;
//	Map<Integer, List<File>> fileMaps;
	private Map<Integer, FileCategory> categorieMap;
	private List<FileCategory> categories;
	CategoryAdapter categoryAdapter;
	
	static String[] audioProjection = {
		MediaStore.Audio.Media.DATA
	};
	
	static String[] videoProjection = {
		MediaStore.Video.Media.DATA
	};
	
	static String[] imageProjection = {
		MediaStore.Images.Media.DATA
	};
	
	/**
	 * 忽略扫描的文件夹
	 */
	static String[] ignoreFolder = {
		"Android",
		"LOST.DIR"
	};
	
	static Map<String, Integer> extMap;

	static int[] resIds = {
		R.drawable.category_icon_music, R.drawable.category_icon_video,
		R.drawable.category_icon_picture, R.drawable.category_icon_document,
		R.drawable.category_icon_zip, R.drawable.category_icon_apk,
		R.drawable.category_icon_other, R.drawable.category_icon_other
	};
	
	static int[] filetypes = {
		FileInfo.FILE_TYPE_MUSIC, FileInfo.FILE_TYPE_VIDEO,
		FileInfo.FILE_TYPE_IMAGE, FileInfo.FILE_TYPE_DOC,
		FileInfo.FILE_TYPE_ARCHIVE, FileInfo.FILE_TYPE_APK,
		FileInfo.FILE_TYPE_SDCARD, FileInfo.FILE_TYPE_PHONE
	};
	
	static Map<String, Integer> mimeMap = new HashMap<String, Integer>();
	
	private boolean isSdcardAvailable = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this;
		
		gvFileCategory = (GridView) findViewById(R.id.gv_file_category);
		
		//fileMaps = new HashMap<Integer, List<File>>();
		categories = new ArrayList<FileCategory>();
		
		mimeMap.put(MIME_IMAGE, TYPE_IMAGE);
		mimeMap.put(MIME_MUSIC, TYPE_MUSIC);
		mimeMap.put(MIME_VIDEO, TYPE_VIDEO);
		
		isSdcardAvailable = FileUtil.isSDCardReady();
		
		initCategoryMap();
		
		initExtMap();
		
		//initFileMap();
		
		categoryAdapter = new CategoryAdapter(mContext, categories);
		gvFileCategory.setAdapter(categoryAdapter);
		
		gvFileCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				int fType = filetypes[position];
				Intent intent = new Intent(mContext, FileListActivity.class);
				if(fType == FileInfo.FILE_TYPE_SDCARD) {	//SD卡
					if(isSdcardAvailable) {
						intent.putExtra("target_path", FileUtil.getSdcardPath());
					}
				} else {
					intent.putExtra("fileCategory", categorieMap.get(filetypes[position]));
					intent.putExtra("isCategory", true);
				}
				startActivity(intent);
			}
		});
		if(isSdcardAvailable) {
			new MySacnTask().execute();
		}
	}
	
	/*private void initFileMap() {
		fileMaps.put(FileInfo.FILE_TYPE_MUSIC, new ArrayList<File>());
		fileMaps.put(FileInfo.FILE_TYPE_VIDEO, new ArrayList<File>());
		fileMaps.put(FileInfo.FILE_TYPE_IMAGE, new ArrayList<File>());
		fileMaps.put(FileInfo.FILE_TYPE_DOC, new ArrayList<File>());
		fileMaps.put(FileInfo.FILE_TYPE_ARCHIVE, new ArrayList<File>());
		fileMaps.put(FileInfo.FILE_TYPE_APK, new ArrayList<File>());
		fileMaps.put(FileInfo.FILE_TYPE_OTHER, new ArrayList<File>());
	}*/
	
	private void initExtMap() {
		extMap = new HashMap<String, Integer>();
		extMap.put("apk", FileInfo.FILE_TYPE_APK);
		extMap.put("audio/*", FileInfo.FILE_TYPE_MUSIC);
		extMap.put("video/*", FileInfo.FILE_TYPE_VIDEO);
		extMap.put("image/*", FileInfo.FILE_TYPE_IMAGE);
		String[] docType = getResources().getStringArray(R.array.category_document);
		for(String type : docType) {
			extMap.put(type, FileInfo.FILE_TYPE_DOC);
		}
		String[] zipType = getResources().getStringArray(R.array.category_archive);
		for(String type : zipType) {
			extMap.put(type, FileInfo.FILE_TYPE_ARCHIVE);
		}
	}
	
	class MySacnTask extends AsyncTask<Void, Integer, Void> {
		long start = System.currentTimeMillis();

		@Override
		protected Void doInBackground(Void... params) {
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					initImageData();
					publishProgress(FileInfo.FILE_TYPE_IMAGE);
					initMusicData();
					publishProgress(FileInfo.FILE_TYPE_MUSIC);
					initVideoData();
					publishProgress(FileInfo.FILE_TYPE_VIDEO);
				}
			}).start();
			File root = Environment.getExternalStorageDirectory();
			scanFile(root);
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			int fileType = values[0];
			FileCategory temp = categorieMap.get(fileType);
			for(FileCategory fc : categories) {
				if(fileType == fc.getFiletype()) {
					fc.setCount(temp.getCount());
				}
			}
			super.onProgressUpdate(values);
		}
		
		@Override
		protected void onPostExecute(Void result) {
			long end = System.currentTimeMillis();
			Toast.makeText(mContext, "文件扫描完成,共" + (end - start), Toast.LENGTH_LONG).show();
			for(FileCategory fc : categories) {
				int fileType = fc.getFiletype();
				FileCategory temp = categorieMap.get(fileType);
				if(temp != null) {
					fc.setCount(temp.getCount());
				}
			}
			categoryAdapter.notifyDataSetChanged();
			super.onPostExecute(result);
		}
	}
	
	/**
	 * 初始化音乐的数据
	 */
	private void initMusicData() {
		Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, audioProjection, null, null, null);
		FileCategory fc = categorieMap.get(FileInfo.FILE_TYPE_MUSIC);
		while(cursor.moveToNext()) {
			String filepath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
			File file = new File(filepath);
			fc.getFiles().add(file);
		}
		fc.setCount(cursor.getCount());
		cursor.close();
	}
	
	/**
	 * 初始化视频的数据
	 */
	private void initVideoData() {
		Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoProjection, null, null, null);
		FileCategory fc = categorieMap.get(FileInfo.FILE_TYPE_VIDEO);
		while(cursor.moveToNext()) {
			String filepath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
			File file = new File(filepath);
			fc.getFiles().add(file);
		}
		fc.setCount(cursor.getCount());
		cursor.close();
	}
	
	/**
	 * 初始化视频的数据
	 */
	private void initImageData() {
		Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageProjection, null, null, null);
		FileCategory fc = categorieMap.get(FileInfo.FILE_TYPE_IMAGE);
		while(cursor.moveToNext()) {
			String filepath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
			File file = new File(filepath);
			fc.getFiles().add(file);
		}
		fc.setCount(cursor.getCount());
		cursor.close();
	}
	
	/**
	 * 扫描制定的文件夹
	 * @param file
	 * @return 返回文件的类型
	 */
	private int scanFile(File file) {
		int fileType = 0;
		if(file.isDirectory() && !file.isHidden()) {
			File[] files = file.listFiles(new MyFileFilter());
			if(files != null) {
				int subLen = files.length;
				for(int i = 0; i < subLen; i++) {
					File f = files[i];
					fileType = scanFile(f);
				}
			}
		} else if(file.isFile() && !file.isHidden()) {
			String ext = FileUtil.getFileExtension(file);
			String mimeType = FileUtil.getMimeType(ext);
			Integer mimevalue = mimeMap.get(mimeType);
			if(mimevalue == null || mimevalue == 0) {	//把图片、音频、视频过滤掉
				Set<String> extKeys = extMap.keySet();
				if(extKeys.contains(mimeType)) {	//如果是常用的类型
					fileType = extMap.get(mimeType);
				} else if(extKeys.contains(ext)) {
					fileType = extMap.get(ext);
				}
				if(fileType != 0) {
					FileCategory fc = categorieMap.get(fileType);
					fc.getFiles().add(file);
					int count = fc.getCount();
					fc.setCount(count + 1);
				}
			}
			
		}
		return fileType;
	}
	
	class MyFileFilter implements FileFilter {

		@Override
		public boolean accept(File pathname) {
			String pname = pathname.getName();
			if(pname.startsWith(".")) {	//以“.”开头的是隐藏文件
				return false;
			} else if(pathname.isDirectory()) {	//是目录
				for(String ig : ignoreFolder) {
					if(ig.equalsIgnoreCase(pname)) {
						return false;
					}
				}
				return true;
			} else {
				String ext = FileUtil.getFileExtension(pathname);
				/*Set<String> extKeys = extMap.keySet();
				if(extKeys.contains(mimeType)) {	//如果是常用的类型
					return true;
				} else if(extKeys.contains(ext)) {
					return true;
				} else {
					return false;
				}*/
				if(ext == null) {
					return false;
				}
				return true;
			}
		}
		
	}
	
	/**
	 * 初始化文件类型
	 */
	private void initCategoryMap() {
		categorieMap = new HashMap<Integer, FileCategory>();
		String[] names = getResources().getStringArray(R.array.file_category);
		for(int i = 0; i < names.length; i++) {
			FileCategory fc = new FileCategory();
			fc.setResId(resIds[i]);
			fc.setCategoryName(names[i]);
			fc.setFiletype(filetypes[i]);
			categories.add(fc);
			categorieMap.put(filetypes[i], fc);
		}
	}
	
	class CategoryAdapter extends BaseAdapter {
		List<FileCategory> list;
		private Context context;

		public CategoryAdapter(Context context, List<FileCategory> list) {
			this.context = context;
			this.list = list;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			CategoryViewHolder holder;
			if(convertView == null) {
				holder = new CategoryViewHolder();
				LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(R.layout.item_category, null);
				holder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
				holder.tvCategoryName = (TextView) convertView.findViewById(R.id.tv_category_name);
				holder.tvCount = (TextView) convertView.findViewById(R.id.tv_count);
				convertView.setTag(holder);
			} else {
				holder = (CategoryViewHolder) convertView.getTag();
			}
			FileCategory fc = list.get(position);
			holder.ivIcon.setImageResource(fc.getResId());
			holder.tvCategoryName.setText(fc.getCategoryName());
			int fType = fc.getFiletype();
			holder.tvCount.setVisibility(View.VISIBLE);
			if(FileInfo.FILE_TYPE_SDCARD == fType) {	//SD卡
				holder.tvCount.setText(context.getString(R.string.storage_used_info, FileUtil.getSDAvailableSize(), FileUtil.getSDTotalSize()));
			} else if(FileInfo.FILE_TYPE_PHONE == fType) {
//				holder.tvCount.setText(context.getString(R.string.storage_used_info, FileUtil.getRomAvailableSize(), FileUtil.getRomTotalSize()));
				holder.tvCount.setVisibility(View.INVISIBLE);
			}else {
				holder.tvCount.setText(context.getString(R.string.file_category_count, fc.getCount()));
			}
			return convertView;
		}
		
		class CategoryViewHolder {
			ImageView ivIcon;
			TextView tvCategoryName;
			TextView tvCount;
		}
		
	}
}
