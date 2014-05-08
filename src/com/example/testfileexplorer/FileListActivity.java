package com.example.testfileexplorer;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore.Video.Thumbnails;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.testfileexplorer.FileListActivity.FileAdapter.FileViewHolder;

public class FileListActivity extends Activity implements OnClickListener {
	private static Map<String, String> mimeMap = null;	//文件类型映射
	private Context mContext;
	
	private ListView lvFile;
	private Button btnBack;
	private TextView emptyView;
	private ProgressBar pbLoading;
	
	private List<File> files;
	private FileCategory fileCategory;
	FileAdapter adapter;
	private File currentPath;
	private boolean isCategory = false;	//是否是分类浏览
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_list);
		mContext = this;

		files = new ArrayList<File>();
		
		initMimeType(mContext);
		
		lvFile = (ListView) findViewById(R.id.lv_file);
		btnBack = (Button) findViewById(R.id.btn_back);
		emptyView = (TextView) findViewById(R.id.empty_view);
		pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
		
		Intent intent = getIntent();
		fileCategory = (FileCategory) intent.getSerializableExtra("fileCategory");
		isCategory = intent.getBooleanExtra("isCategory", false);
		if(fileCategory != null) {
			btnBack.setTextSize(18.0F);
			btnBack.setText(fileCategory.getCategoryName());
			files.addAll(fileCategory.getFiles());
		} else {
			currentPath = (File) intent.getSerializableExtra("target_path");
			initChildren(currentPath, false);
		}
		adapter = new FileAdapter(mContext, files);
		lvFile.setAdapter(adapter);
		lvFile.setEmptyView(emptyView);
		
		btnBack.setOnClickListener(this);
		lvFile.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				File f = files.get(position);
				if(f.isDirectory() && f.canRead()) {	//是文件夹
					initChildren(f, false);
				}
			}
		});
	}
	
	/**
	 * 初始化文件类型的映射表
	 */
	private Map<String, String> initMimeType(Context context) {
		if (mimeMap == null) {
			mimeMap = new HashMap<String, String>();
			Properties props = new Properties();
			try {
				props.load(context.getResources().openRawResource(R.raw.mime));
				Set<Object> keys = props.keySet();
				for (Object key : keys) {
					mimeMap.put((String) key, (String) props.get(key));
				}
				return mimeMap;
			} catch (NotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return mimeMap;
	}
	
	public Map<String, String> getMimeMap() {
		if(mimeMap == null) {
			return initMimeType(mContext);
		} else {
			return mimeMap;
		}
	}
	
	class LoadApkIconTAsk extends AsyncTask<Object, Drawable, Drawable> {
		FileViewHolder holder;

		public LoadApkIconTAsk(FileViewHolder holder) {
			super();
			this.holder = holder;
		}

		@Override
		protected Drawable doInBackground(Object... params) {
			Drawable drawable = FileUtil.getApkIcon((Context)params[0], (String)params[1]);
			return drawable;
		}
		
		@Override
		protected void onPostExecute(Drawable result) {
			if(result != null) {
				holder.ivIcon.setImageDrawable(result);
			}
			super.onPostExecute(result);
		}
	}
	
	class AsyncImageLoader {
	    public Map<String, SoftReference<Bitmap>> imageCache = new HashMap<String, SoftReference<Bitmap>>();
	    private ExecutorService executorService = Executors.newFixedThreadPool(5);
	    private final Handler handler = new Handler();
	 
	    /**
	     *
	     * @param imageUrl
	     *            图像url地址
	     * @param callback
	     *            回调接口
	     * @return 返回内存中缓存的图像，第一次加载返回null
	     */
	    public Bitmap loadDrawable(final int fileType, final String imageUrl,
	            final ImageCallback callback) {
	        // 如果缓存过就从缓存中取出数据
	        if (imageCache.containsKey(imageUrl)) {
	            SoftReference<Bitmap> softReference = imageCache.get(imageUrl);
	            if (softReference.get() != null) {
	                return softReference.get();
	            }
	        }
	        // 缓存中没有图像，则从网络上取出数据，并将取出的数据缓存到内存中
	        executorService.submit(new Runnable() {
	        	@Override
	            public void run() {
	                try {
	                    final Bitmap bitmap = loadImageFromUrl(fileType, imageUrl);
	 
	                    imageCache.put(imageUrl, new SoftReference<Bitmap>(
	                    		bitmap));
	 
	                    handler.post(new Runnable() {
	                    	@Override
	                        public void run() {
	                            callback.imageLoaded(bitmap);
	                        }
	                    });
	                } catch (Exception e) {
	                    throw new RuntimeException(e);
	                }
	            }
	        });
	        return null;
	    }
	 
	    protected Bitmap loadImageFromUrl(int fileType, String imagePath) {
	        try {
	        	Bitmap bitmap = null;
	        	if(MainActivity.TYPE_IMAGE == fileType) {	//是图片文件
	        		Drawable drawable = Drawable.createFromPath(imagePath);
	        		bitmap = ((BitmapDrawable) drawable).getBitmap();
	        		bitmap = ThumbnailUtils.extractThumbnail(bitmap, 50, 50);
	        	} else if(MainActivity.TYPE_VIDEO == fileType) {	//是视频文件
	        		bitmap = ThumbnailUtils.createVideoThumbnail(imagePath, Thumbnails.MINI_KIND);
	        	}
	            return bitmap;
	        } catch (Exception e) {
	            throw new RuntimeException(e);
	        }
	    }
	}
	
	 // 对外界开放的回调接口a
    interface ImageCallback {
        // 注意 此方法是用来设置目标对象的图像资源
        public void imageLoaded(Bitmap bitmap);
    }
	
	class FileAdapter extends BaseAdapter {
		private Context context;
		private List<File> list;
		AsyncImageLoader imageLoader;

		public FileAdapter(Context context, List<File> list) {
			super();
			this.context = context;
			this.list = list;
			imageLoader = new AsyncImageLoader();
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
		
		private int getResId(File file, int position, final FileViewHolder holder) {
			String ext = FileUtil.getFileExtension(file);
			String mimeType = FileUtil.getMimeType(ext);
			String extIcon = null;
			if (StringUtil.isNotBlank(mimeType)) {
				if (file.exists() && file.isFile()) {
					extIcon = getMimeMap().get(mimeType);
				}
				if (StringUtil.isBlank(extIcon)) {
					extIcon = getMimeMap().get(ext);
				}
			} else {
				extIcon = getMimeMap().get(ext);
			}
			int resId = 0;
			if (StringUtil.isNotBlank(extIcon)) {
				resId = getResources().getIdentifier(extIcon, "drawable", getPackageName());
			}
			if (resId == 0) {
				resId = R.drawable.att_commom;
			}
			Integer mType = MainActivity.mimeMap.get(mimeType);
			String filepath = file.getAbsolutePath();
			if(mType != null ) {	
				if(MainActivity.TYPE_IMAGE == mType || MainActivity.TYPE_VIDEO == mType) {	//是图片或者视频文件
					imageLoader.loadDrawable(mType, filepath, new ImageCallback() {
						
						@Override
						public void imageLoaded(Bitmap imageBitmap) {
							if(imageBitmap != null) {
								holder.ivIcon.setImageBitmap(imageBitmap);
							}
						}
					});
				}
			} else if("apk".equals(ext)) {	//是安装包文件
				new LoadApkIconTAsk(holder).execute(context, filepath);
			}
			return resId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			FileViewHolder holder;
			if(convertView == null) {
				holder = new FileViewHolder();
				LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(R.layout.item_file_info, null);
				holder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
				holder.tvFilename = (TextView) convertView.findViewById(R.id.tv_filename);
				holder.tvFileDate = (TextView) convertView.findViewById(R.id.tv_filedate);
				holder.tvFileSize = (TextView) convertView.findViewById(R.id.tv_filesize);
				convertView.setTag(holder);
			} else {
				holder = (FileViewHolder) convertView.getTag();
			}
			File file = list.get(position);
			holder.tvFilename.setText(file.getName());
			holder.tvFileDate.setText(StringUtil.parseTime(file.lastModified(), null));
			if(file.isDirectory()) {	//是文件夹
				holder.ivIcon.setImageResource(R.drawable.icon_folder);
				if(file.canRead()) {	//目录可读
					holder.tvFileSize.setText(context.getString(R.string.folder_children_count, file.list().length));
				} else {
					holder.tvFileSize.setText("不可读");
				}
			} else {	//是文件
				holder.ivIcon.setImageResource(getResId(file, position, holder));
				holder.tvFileSize.setText(FileUtil.convertStorage(file.length()));
			}
			return convertView;
		}
		
		final class FileViewHolder {
			ImageView ivIcon;
			TextView tvFilename;
			TextView tvFileDate;
			TextView tvFileSize;
		}
	}
	
	/**
	 * 获得指定目录下的所有文件
	 * @param parent 当前点击的目录项
	 * @param back 是否点击了"向上"
	 */
	private void initChildren(File path, boolean back) {
		if(back) {	//点击了"向上"
			currentPath = currentPath.getParentFile();
		} else {
			currentPath = path;
		}
		btnBack.setText(currentPath.getAbsolutePath());
		pbLoading.setVisibility(View.VISIBLE);
		new MyLoadFileTask().execute(currentPath);
	}
	
	class MyLoadFileTask extends AsyncTask<File, Void, Void> {

		@Override
		protected Void doInBackground(File... params) {
			files.clear();
			files.addAll(FileUtil.getSubFiles(params[0]));
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			pbLoading.setVisibility(View.GONE);
			adapter.notifyDataSetChanged();
			super.onPostExecute(result);
		}
		
	}
	
	/**
	 * 返回上一级
	 */
	private void pathBack() {
		if(isCategory) {	//是分类浏览
			finish();
		} else {
			if(FileUtil.isSdcardRoot(currentPath)) {	//已经是SD卡的根目录了
				finish();
			} else {
				//currentPath = currentPath.getParentFile();
				initChildren(currentPath, true);
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:	//上一级
			pathBack();
			break;

		default:
			break;
		}
	}
	
	@Override
	public void onBackPressed() {
		pathBack();
	}
}
