package com.tools.payhelper.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.tools.payhelper.CustomApplcation;
import com.tools.payhelper.MainActivity;

import android.R.string;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import de.robv.android.xposed.XposedHelpers;

public class PayHelperUtils {

	public static String WECHATSTART_ACTION = "com.payhelper.wechat.start";
	public static String ALIPAYSTART_ACTION = "com.payhelper.alipay.start";
	public static String MSGRECEIVED_ACTION = "com.tools.payhelper.msgreceived";
	public static String TRADENORECEIVED_ACTION = "com.tools.payhelper.tradenoreceived";
 

//	/*
//	 * 启动一个app
//	 */
//	public static void startAPP() {
//		try {
//			Intent intent = new Intent(CustomApplcation.getInstance().getApplicationContext(), MainActivity.class);
//			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			CustomApplcation.getInstance().getApplicationContext().startActivity(intent);
//		} catch (Exception e) {
//		}
//	}

	/**
	 * 将图片转换成Base64编码的字符串
	 * 
	 * @param path
	 * @return base64编码的字符串
	 */
	public static String imageToBase64(String path) {
		if (TextUtils.isEmpty(path)) {
			return null;
		}
		InputStream is = null;
		byte[] data = null;
		String result = null;
		try {
			is = new FileInputStream(path);
			// 创建一个字符流大小的数组。
			data = new byte[is.available()];
			// 写入数组
			is.read(data);
			// 用默认的编码格式进行编码
			result = Base64.encodeToString(data, Base64.DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != is) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		result = "\"data:image/gif;base64," + result + "\"";
		return result;
	}


//	static Intent broadCastIntentPay = new Intent();
	public synchronized static void sendAppPay(String amount, String ordernumber, Context context) {
		Intent broadCastIntentPay = new Intent();
		broadCastIntentPay.setAction(ALIPAYSTART_ACTION);
		broadCastIntentPay.putExtra("amount", amount);
		broadCastIntentPay.putExtra("ordernumber", ordernumber);
		context.sendBroadcast(broadCastIntentPay);
	}


	/*
	 * 将时间戳转换为时间
	 */
	public static String stampToDate(String s) {
		String res;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long lt = new Long(s);
		Date date = new Date(lt * 1000);
		res = simpleDateFormat.format(date);
		return res;
	}

	/**
	 * 方法描述：判断某一应用是否正在运行
	 * 
	 * @param context
	 *            上下文
	 * @param packageName
	 *            应用的包名
	 * @return true 表示正在运行，false表示没有运行
	 */
	public static boolean isAppRunning(Context context, String packageName) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);
		if (list.size() <= 0) {
			return false;
		}
		for (ActivityManager.RunningTaskInfo info : list) {
			if (info.baseActivity.getPackageName().equals(packageName)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * 启动一个app
	 */

	public static void startAPP(Context context, String appPackageName) {
		try {
			Intent intent = context.getPackageManager().getLaunchIntentForPackage(appPackageName);
			context.startActivity(intent);

		} catch (Exception e) {
		}
	}

	public static void stopApp(Context context,String appPackageName){
		Intent intent = context.getPackageManager().getLaunchIntentForPackage(appPackageName);
		boolean falg = context.stopService(intent);
		if(falg){
			Log.d("rescounter","close alipay success");
		}else {
			Log.d("rescounter","close alipay fail");
		}

	}

	 

	/**
	 * 获取当前本地apk的版本
	 *
	 * @param mContext
	 * @return
	 */
	public static int getVersionCode(Context mContext) {
		int versionCode = 0;
		try {
			// 获取软件版本号，对应AndroidManifest.xml下android:versionCode
			versionCode = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			sendmsg(mContext, "getVersionCode异常" + e.getMessage());
		}
		return versionCode;
	}

	/**
	 * 获取版本号名称
	 *
	 * @param context
	 *            上下文
	 * @return
	 */
	public static String getVerName(Context context) {
		String verName = "";
		try {
			verName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
			sendmsg(context, "getVerName异常" + e.getMessage());
		}
		return verName;
	}


	static Intent broadCastIntent = new Intent();
	public static void sendmsg(Context context, String msg) {
		broadCastIntent.putExtra("msg", msg);
		broadCastIntent.setAction(MSGRECEIVED_ACTION);
		context.sendBroadcast(broadCastIntent);
	}


	public static String getCurrentDate() {
		long l = System.currentTimeMillis();
		Date date = new Date(l);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String d = dateFormat.format(date);
		return d;
	}

	public static String getDiskCachePath(Context context) {
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
				|| !Environment.isExternalStorageRemovable()) {
			return context.getExternalCacheDir().getPath();
		} else {
			return context.getCacheDir().getPath();
		}
	}


	static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	static Random random = new Random();
	static Date date = new Date();
	public static String getOrderNumber() {
		date.setTime(System.currentTimeMillis());
		String newDate = sdf.format(date);
		String result = "";
		for (int i = 0; i < 3; i++) {
			result += random.nextInt(10);
		}
		return newDate + result;
	}




	public static String getMacAddress() {
		String macAddress = null ;
		String str = "" ;
		try {
			//linux下查询网卡mac地址的命令
			Process pp = Runtime.getRuntime().exec( "cat /sys/class/net/wlan0/address" );
			InputStreamReader ir = new InputStreamReader(pp.getInputStream());
			LineNumberReader input = new LineNumberReader(ir);

			for (; null != str;) {
				str = input.readLine();
				if (str != null ) {
					macAddress = str.trim();// 去空格
					break ;
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return macAddress;
	}
	
 
}
