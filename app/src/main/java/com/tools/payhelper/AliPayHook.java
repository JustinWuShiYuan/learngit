package com.tools.payhelper;

import java.lang.reflect.Field;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;


public class AliPayHook {

	public static String BILLRECEIVED_ACTION = "com.tools.payhelper.billreceived";
	public static String QRCODERECEIVED_ACTION = "com.tools.payhelper.qrcodereceived";

	public void hook(final ClassLoader classLoader, final Context context) {
		securityCheckHook(classLoader);
		try {
			XposedHelpers.findAndHookMethod("com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity", classLoader,
					"onCreate", Bundle.class, new XC_MethodHook() {
						@Override
						protected void afterHookedMethod(MethodHookParam param) throws Throwable {
							XposedBridge.log("Hook支付宝开始......");
							Field jinErField = XposedHelpers.findField(param.thisObject.getClass(), "b");
							final Object jinErView = jinErField.get(param.thisObject);
							Field beiZhuField = XposedHelpers.findField(param.thisObject.getClass(), "c");
							final Object beiZhuView = beiZhuField.get(param.thisObject);
							Intent intent = ((Activity) param.thisObject).getIntent();
							String ordernumber = intent.getStringExtra("ordernumber");
							String amount = intent.getStringExtra("amount");
							// 设置支付宝金额和备注
							XposedBridge.log("提交订单:" + ordernumber + "金额:" + amount);
							XposedHelpers.callMethod(jinErView, "setText", amount);
							XposedHelpers.callMethod(beiZhuView, "setText", ordernumber);
							// 点击确认
							Field quRenField = XposedHelpers.findField(param.thisObject.getClass(), "e");
							final Button quRenButton = (Button) quRenField.get(param.thisObject);
							quRenButton.performClick();
						}
					});

			XposedHelpers.findAndHookMethod("com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity", classLoader, "a",
					XposedHelpers.findClass("com.alipay.transferprod.rpc.result.ConsultSetAmountRes", classLoader),
					new XC_MethodHook() {
						@Override
						protected void afterHookedMethod(MethodHookParam param) throws Throwable {

							Field moneyField = XposedHelpers.findField(param.thisObject.getClass(), "g");
							String amount = (String) moneyField.get(param.thisObject);

							Field markField = XposedHelpers.findField(param.thisObject.getClass(), "c");
							Object markObject = markField.get(param.thisObject);
							String ordernumber = (String) XposedHelpers.callMethod(markObject, "getUbbStr");

							Object consultSetAmountRes = param.args[0];
							Field consultField = XposedHelpers.findField(consultSetAmountRes.getClass(), "qrCodeUrl");
							String qrcode = (String) consultField.get(consultSetAmountRes);

							XposedBridge.log(amount + "  " + ordernumber + "  " + qrcode);
							XposedBridge.log("调用增加数据方法==>支付宝");
							Intent broadCastIntent = new Intent();
							broadCastIntent.putExtra("amount", amount);
							broadCastIntent.putExtra("ordernumber", ordernumber);
							broadCastIntent.putExtra("qrcode", qrcode);
							broadCastIntent.setAction(QRCODERECEIVED_ACTION);
							context.sendBroadcast(broadCastIntent);
						}
					});

		} catch (Error | Exception e) {
			e.printStackTrace();
		}
	}

	private void securityCheckHook(ClassLoader classLoader) {
		try {
			Class<?> securityCheckClazz = XposedHelpers.findClass("com.alipay.mobile.base.security.CI", classLoader);
			XposedHelpers.findAndHookMethod(securityCheckClazz, "a", String.class, String.class, String.class,
					new XC_MethodHook() {
						@Override
						protected void afterHookedMethod(MethodHookParam param) throws Throwable {
							Object object = param.getResult();
							XposedHelpers.setBooleanField(object, "a", false);
							param.setResult(object);
							super.afterHookedMethod(param);
						}
					});

			XposedHelpers.findAndHookMethod(securityCheckClazz, "a", Class.class, String.class, String.class,
					new XC_MethodReplacement() {
						@Override
						protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
							return (byte) 1;
						}
					});
			XposedHelpers.findAndHookMethod(securityCheckClazz, "a", ClassLoader.class, String.class,
					new XC_MethodReplacement() {
						@Override
						protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
							return (byte) 1;
						}
					});
			XposedHelpers.findAndHookMethod(securityCheckClazz, "a", new XC_MethodReplacement() {
				@Override
				protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
					return false;
				}
			});

		} catch (Error | Exception e) {
			e.printStackTrace();
		}
	}
}