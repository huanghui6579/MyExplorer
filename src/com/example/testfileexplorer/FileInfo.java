package com.example.testfileexplorer;

/**
 * 文件类型
 */
public class FileInfo {
	/**
	 * 音乐1
	 */
	public static final int FILE_TYPE_MUSIC = 1;
	/**
	 * 视频 2
	 */
	public static final int FILE_TYPE_VIDEO = 2;
	/**
	 * 图片 3
	 */
	public static final int FILE_TYPE_IMAGE = 3;
	/**
	 * 文档 4
	 */
	public static final int FILE_TYPE_DOC = 4;
	/**
	 * 音频 压缩文件
	 */
	public static final int FILE_TYPE_ARCHIVE = 5;
	/**
	 * apk安装文件
	 */
	public static final int FILE_TYPE_APK = 6;
	/**
	 * SD卡
	 */
	public static final int FILE_TYPE_SDCARD = 7;
	/**
	 * 手机本机存储
	 */
	public static final int FILE_TYPE_PHONE = 8;

	private long id;
	private String filepath;
	private String filename;
	private long modifyDate;
	private long filesize;
	private boolean isDir;
	private boolean selected;
	private boolean canRead;
	private boolean filetype;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public long getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(long modifyDate) {
		this.modifyDate = modifyDate;
	}

	public long getFilesize() {
		return filesize;
	}

	public void setFilesize(long filesize) {
		this.filesize = filesize;
	}

	public boolean isDir() {
		return isDir;
	}

	public void setDir(boolean isDir) {
		this.isDir = isDir;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isCanRead() {
		return canRead;
	}

	public void setCanRead(boolean canRead) {
		this.canRead = canRead;
	}

	public boolean isFiletype() {
		return filetype;
	}

	public void setFiletype(boolean filetype) {
		this.filetype = filetype;
	}
}
