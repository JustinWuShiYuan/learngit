package com.tools.payhelper;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import android.widget.Toast;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
 

 
 
public class Main implements IXposedHookLoadPackage {
	private final String ALIPAY_PACKAGE = "com.eg.android.AlipayGphone";
	private boolean ALIPAY_PACKAGE_ISHOOK = false;

	public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam){
		if (lpparam.appInfo == null || (lpparam.appInfo.flags
				& (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0) {
			return;
		}
		final String packageName = lpparam.packageName;
		final String processName = lpparam.processName;
		if (ALIPAY_PACKAGE.equals(packageName)) {
			try {
				XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
						super.afterHookedMethod(param);
						Context context = (Context) param.args[0];
						ClassLoader appClassLoader = context.getClassLoader();
						if (ALIPAY_PACKAGE.equals(processName) && !ALIPAY_PACKAGE_ISHOOK) {
							ALIPAY_PACKAGE_ISHOOK = true;
							StartAlipayReceived startAlipay = new StartAlipayReceived();
							IntentFilter intentFilter = new IntentFilter();
							intentFilter.addAction("com.payhelper.alipay.start");
							context.registerReceiver(startAlipay, intentFilter);
							XposedBridge.log("handleLoadPackage: " + packageName);
							Toast.makeText(context, "HOOK成功", Toast.LENGTH_LONG).show();
							new AliPayHook().hook(appClassLoader, context);
						}
					}
				});
			} catch (Throwable e) {
				XposedBridge.log(e);
				Log.d("Main","e:"+e.toString());
			}
		}
	}


	Intent intent2 = null ;
	// 自定义接受订单通知广播
	class StartAlipayReceived extends BroadcastReceiver {
		@Override

		public void onReceive(Context context, Intent intent) {
			XposedBridge.log("启动支付宝Activity");
			if(null == intent2 ){
				intent2 = new Intent(context, XposedHelpers.findClass("com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity", context.getClassLoader()));
				intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			}
			intent2.putExtra("ordernumber", intent.getStringExtra("ordernumber"));
			intent2.putExtra("amount", intent.getStringExtra("amount"));
			context.startActivity(intent2);
		}
	}
}
