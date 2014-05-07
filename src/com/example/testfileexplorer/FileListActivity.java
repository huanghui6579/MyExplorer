package com.example.testfileexplorer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class FileListActivity extends Activity implements OnClickListener {
	private static final String TAG = "FileListActivity";
	private static Map<String, String> mimeMap = null;	//文件类型映射
	private Context mContext;
	
	private ListView lvFile;
	private Button btnBack;
	private TextView emptyView;
	private ProgressBar pbLoading;
	private Button btnOk;
	//private CheckBox cbAll;
	
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
		btnOk = (Button) findViewById(R.id.btn_ok);
		btnOk.setOnClickListener(this);
		//cbAll = (CheckBox) findViewById(R.id.cb_all);
		
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
				} else {	//是文件
					Toast.makeText(mContext, "选择了" + f.toString(), Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		/*cbAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				
			}
			
		});*/
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
	
	private int getResId(File file) {
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
		return resId;
	}
	
	class FileAdapter extends BaseAdapter {
		private Context context;
		private List<File> list;
		private SparseBooleanArray checkArray = new SparseBooleanArray();
		private int checkCount = 0;

		public FileAdapter(Context context, List<File> list) {
			super();
			this.context = context;
			this.list = list;
		}
		
		public void update() {
			notifyDataSetChanged();
			checkArray.clear();
			checkCount = 0;
		}

		public SparseBooleanArray getCheckArray() {
			return checkArray;
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
		public View getView(final int position, View convertView, ViewGroup parent) {
			FileViewHolder holder;
			if(convertView == null) {
				holder = new FileViewHolder();
				LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(R.layout.item_file_info, null);
				holder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
				holder.tvFilename = (TextView) convertView.findViewById(R.id.tv_filename);
				holder.tvFileDate = (TextView) convertView.findViewById(R.id.tv_filedate);
				holder.tvFileSize = (TextView) convertView.findViewById(R.id.tv_filesize);
				holder.cbItem = (CheckBox) convertView.findViewById(R.id.cb_item);
				convertView.setTag(holder);
			} else {
				holder = (FileViewHolder) convertView.getTag();
			}
			File file = list.get(position);
			holder.tvFilename.setText(file.getName());
			holder.tvFileDate.setText(StringUtil.parseTime(file.lastModified(), null));
			holder.cbItem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					checkArray.put(position, isChecked);
					if(isChecked) {
						checkCount ++;
						int total = getCount();
						if(checkCount > total) {
							checkCount = total;
						}
					} else {
						checkCount --;
						if(checkCount < 0) {
							checkCount = 0;
						}
					}
					if(checkCount > 0) {	//有选中
						btnOk.setEnabled(true);
					} else {
						btnOk.setEnabled(false);
					}
				}
			});
			if(file.isDirectory()) {	//是文件夹
				holder.ivIcon.setImageResource(R.drawable.icon_folder);
				if(file.canRead()) {	//目录可读
					holder.tvFileSize.setText(context.getString(R.string.folder_children_count, file.list().length));
				} else {
					holder.tvFileSize.setText("不可读");
				}
				holder.cbItem.setVisibility(View.GONE);
			} else {	//是文件
				holder.ivIcon.setImageResource(getResId(file));
				holder.tvFileSize.setText(FileUtil.convertStorage(file.length()));
				holder.cbItem.setVisibility(View.VISIBLE);
			}
			boolean checked = checkArray.get(position);
			if(checked) {
				holder.cbItem.setChecked(true);
			} else {
				holder.cbItem.setChecked(false);
			}
			return convertView;
		}
		
		final class FileViewHolder {
			ImageView ivIcon;
			TextView tvFilename;
			TextView tvFileDate;
			TextView tvFileSize;
			CheckBox cbItem;
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
			adapter.update();
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
		case R.id.btn_ok:	//确定选择
			SparseBooleanArray barray = adapter.getCheckArray();
			List<File> checkList = new ArrayList<File>();
			int len = barray.size();
			for(int i = 0; i < len; i++) {
				int position = barray.keyAt(i);
				boolean check = barray.get(position, false);
				if(check) {
					checkList.add(files.get(position));
				}
			}
			Log.i(TAG, "选择了" + checkList.size() + "个文件" + checkList.toString());
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
