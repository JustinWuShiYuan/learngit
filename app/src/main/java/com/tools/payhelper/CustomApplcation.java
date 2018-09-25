package com.tools.payhelper;


import android.app.Application;
import android.content.Context;
import android.util.Log;



public class CustomApplcation extends Application {

	public static CustomApplcation mInstance;
	private static Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		context = getApplicationContext();
	}

	public static CustomApplcation getInstance() {
		return mInstance;
	}

	public static Context getContext() {
		return context;
	}
}
