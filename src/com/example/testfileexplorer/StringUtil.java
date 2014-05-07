package com.example.testfileexplorer;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class StringUtil {
	/**
	 * 判断字符串是否为空或者空格
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(CharSequence str) {
		if (str != null && str.length() > 0) {
			return false;
		}
		return true;
	}

	/**
	 * 判断字符串是否不为空或者空格
	 * @param str
	 * @return true：不为空，false：为空
	 */
	public static boolean isNotEmpty(CharSequence str) {
		return !isEmpty(str);
	}

	/**
	 * 判断一个map是否为空
	 * @param map
	 * @return true：为空，false：不为空
	 */
	public static boolean isEmpty(Map<?, ?> map) {
		return !isNotEmpty(map);
	}

	/**
	 * 判断一个map是否不为空
	 * @param map
	 * @return true：不为空，false：为空
	 */
	public static boolean isNotEmpty(Map<?, ?> map) {
		if (map != null && map.size() > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 判断集合是否是空的 
	 * @param collection
	 * @return true：表示是空的，false：表示不是空的
	 */
	public static boolean isEmpty(Collection<?> collection) {
		if (collection != null && collection.size() > 0) {
			return false;
		}
		return true;
	}

	/**
	 * 判断集合是否是不为空
	 * @param collection
	 * @return true：不为空，false：为空
	 */
	public static boolean isNotEmpty(Collection<?> collection) {
		return !isEmpty(collection);
	}

	/**
	 * 判断数组是否是空的 
	 * @param collection
	 * @return true：表示是空的，false：表示不是空的
	 */
	public static boolean isEmpty(Object[] objects) {
		if (objects != null && objects.length > 0) {
			return false;
		}
		return true;
	}

	/**
	 * 判断数组是否是不为空
	 * @param objects
	 * @return true：不为空，false：为空
	 */
	public static boolean isNotEmpty(Object[] objects) {
		return !isEmpty(objects);
	}
	
	public static boolean isBlank(String s) {
		if (s == null || "".equals(s.trim())) {
			return true;
		}
		return false;
	}

	public static boolean isNotBlank(String s) {
		return !isBlank(s);
	}
	
	public static String parseTime(long time, String pattern) {
		Date date = new Date(time);
		if(isBlank(pattern)) {
			pattern = "yyyy-MM-dd HH:mm";
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
		return dateFormat.format(date);
	}
}
