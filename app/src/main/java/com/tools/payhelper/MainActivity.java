package com.tools.payhelper;
 
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import com.tools.payhelper.bean.AccountBean;
import com.tools.payhelper.bean.ResultBean;
import com.tools.payhelper.constants.MyConstant;
import com.tools.payhelper.net.NetWorks;
import com.tools.payhelper.utils.FileUtils;
import com.tools.payhelper.utils.PayHelperUtils;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import org.jsoup.helper.StringUtil;

import de.robv.android.xposed.XposedBridge;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


public class MainActivity extends Activity implements View.OnClickListener {

	private BillReceived billReceived;
	public  String BILLRECEIVED_ACTION = "com.tools.payhelper.billreceived";
	public  String QRCODERECEIVED_ACTION = "com.tools.payhelper.qrcodereceived";
	public  String MSGRECEIVED_ACTION = "com.tools.payhelper.msgreceived";
	public  String TRADENORECEIVED_ACTION = "com.tools.payhelper.tradenoreceived";
	public  String ordernumberStr;
	private Context context;
    public  List listRuleData ;
    private  String money;
	private int ruleIndex = 0;
	private  Timer timer;
	private  Timer timer2;
	private RecyclerView 	recyclerView;
	private List<String> recycleViewListData;

	private StringBuffer stringBuffer;
	private String accountNum = "123456";
	private String payTypeNum = "ZFB";
	private String uuid = "";
	private List <Integer> moneyList;
	private List<AccountBean> accountBeanList ;
	private String zeroStr = "00";
	private String fuHao = "\"";
	private String amount_ = fuHao + "amount"+ fuHao ;
	private String url_ = fuHao + "url"+ fuHao;

	private Button	btnStartUploadData;
	private Button	btnOpenZFb;
	private EditText	etPayAccount;
	private EditText	etPayType;

	private ExecutorService executorService = Executors.newSingleThreadExecutor();

	private int loggDisplayMaxLength = 4000;
	private Object lockObj = new Object();

	private int[] frequentlyMoney ={10,20,30,40, 50,60,70,80,90,100,200,300,400 ,500 ,600 , 700, 800, 900, 1000,2000 , 3000, 4000,5000 };
	private List<Integer> specialList = new ArrayList<>();

	private boolean isClicked = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.context = this;
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);

		initView();
		initSpecialListData();
		initRuleData();
		registerBillReceiver();
	}

	private void initSpecialListData() {
		specialList.add(20);
		specialList.add(30);
		specialList.add(100);
		specialList.add(200);
		specialList.add(300);
		specialList.add(500);
	}

	private void initView() {
		etPayAccount = (EditText) findViewById(R.id.et_input_pay_account);

		etPayType = (EditText) findViewById(R.id.et_input_pay_type);

		btnStartUploadData = (Button) findViewById(R.id.btn_start_upload);
		btnStartUploadData.setOnClickListener(this);

		btnOpenZFb = (Button) findViewById(R.id.btn_open_zfb);
		btnOpenZFb.setOnClickListener(this);

		recyclerView = (RecyclerView) findViewById(R.id.recycleView);
		LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
		recyclerView.setLayoutManager(mLinearLayoutManager);
	}

	private void initRuleData() {
		stringBuffer = new StringBuffer();
		listRuleData = new ArrayList();
		recycleViewListData = new ArrayList<>();

		moneyList = new ArrayList<>();

		timer = new Timer();

		for (int i= 10;i<=100;i++){
			for (int j = 0 ;j<=9;j++){
				money = i - 1 + ".9" + j;
				listRuleData.add(money);
			}
			money = i +".00";
			listRuleData.add(money);

			moneyList.add( i);
		}
		for (int i = 110;i<=1000;i+=10){
			for (int j = 0;j<=9;j++){
				money = i - 1 + ".9" + j;
				listRuleData.add(money);
			}
			money = i+".00";
			listRuleData.add(money);

			moneyList.add( i);
		}
		for (int i = 1100;i<=5000;i+=100){
			for (int j = 0;j<=9;j++){
				money = i-1 +".9"+j;
				listRuleData.add(money);
			}
			money = i+".00";
			listRuleData.add(money);

			moneyList.add( i);
		}
		for (int i = 6000;i<=10000;i += 1000){
			for (int j = 0;j<=9;j++){
				money = i - 1 + ".9" + j;
				listRuleData.add(money);
			}
			money = i + ".00";
			listRuleData.add(money);

			moneyList.add(i);
		}

//
//		//最特殊的金额 需要30张 数据
//		for(int i=0;i<frequentlyMoney.length;i++){
//			for (int j = 0;j<=9;j++){
//				if(specialList.contains(frequentlyMoney[i]) ){
//					money = frequentlyMoney[i] - 1 + ".8" + j;
//					listRuleData.add(money);
//
//					money = frequentlyMoney[i] - 1 + ".7" + j;
//					listRuleData.add(money);
//				}else {
//					money = frequentlyMoney[i] - 1 + ".8" + j;
//					listRuleData.add(money);
//				}
//			}
//		}

	}

	private void registerBillReceiver() {
		billReceived = new BillReceived();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BILLRECEIVED_ACTION);
		intentFilter.addAction(MSGRECEIVED_ACTION);
		intentFilter.addAction(QRCODERECEIVED_ACTION);
		intentFilter.addAction(TRADENORECEIVED_ACTION);
		registerReceiver(billReceived, intentFilter);
	}



	/**
	 * 拼接要上傳 的字符串*/
	private void startJointUploadJson() {
		stringBuffer .setLength(0);
//		stringBuffer.append("qrData={");
		stringBuffer.append("{");
			stringBuffer.append(fuHao+"account"+ fuHao +":"+ fuHao +accountNum+fuHao+",");
			stringBuffer.append(fuHao+"payType"+ fuHao +":"+ fuHao +payTypeNum+fuHao+",");
			stringBuffer.append(fuHao+"uuid"+ fuHao +":"+ fuHao +PayHelperUtils.getMacAddress()+fuHao+",");
			stringBuffer.append(fuHao+"qr"+fuHao+":"+"{");
			for(int i=0;i<moneyList.size();i++){
				stringBuffer.append(fuHao+moneyList.get(i)+fuHao +":"+"[");//
				for(int j=0;j<accountBeanList.size();j++){
					String amount = accountBeanList.get(j).getAmount();

					String[] amountArray = amount.split("\\.");
					if( amountArray[1].equals(zeroStr) ){

						if(amountArray[0].equals(moneyList.get(i)+"")){
//							Log.d("uploadJson","amount:"+amount);
							stringBuffer.append("{");
							stringBuffer.append(amount_ +":"+ fuHao+amount+fuHao +",");
							stringBuffer.append(url_ +":"+ fuHao +accountBeanList.get(j).getUrl()+fuHao);
							stringBuffer.append("},");
						}
//						Log.d("uploadJson",stringBuffer.toString());

					}else{
						int money = Integer.parseInt(amountArray[0]) +1;
						if(money == moneyList.get(i)){
							stringBuffer.append("{");
							stringBuffer.append(amount_ +":"+ fuHao+amount+fuHao +",");
							stringBuffer.append(url_ +":"+ fuHao+accountBeanList.get(j).getUrl()+fuHao);
							stringBuffer.append("},");
						}
//						Log.d("uploadJson",stringBuffer.toString());
					}
				}
				stringBuffer.deleteCharAt(stringBuffer.length() -1);//删除{amount:11,url:""}，最后一个逗号

				stringBuffer.append("],");

			}

		       stringBuffer.deleteCharAt(stringBuffer.length() -1);//删除"20"[{}]，最后一个逗号
		    stringBuffer.append("}");

		stringBuffer.append("}");


//		//这部分注释 是用来分段显示stringBuffer 因为系统默认 单条信息s只能打印4000多的字符
//		String s = stringBuffer.toString();
//		if(s.length() > loggDisplayMaxLength) {
//			for(int i=0;i<s.length();i+=loggDisplayMaxLength){
//				if(i+loggDisplayMaxLength<s.length())
//					Log.i("rescounter"+i,s.substring(i, i+loggDisplayMaxLength));
//				else
//					Log.i("rescounter"+i,s.substring(i, s.length()));
//			}
//		} else{
//			Log.i("rescounter",s.toString());
//		}
//
//		Log.i("rescounter",stringBuffer.toString());

		requestNet();

		recycleViewListData = new ArrayList<>();//清空

	}

	private void requestNet() {
		NetWorks.uploadJson(stringBuffer.toString(), MyConstant.token, new Observer<ResultBean>() {
			@Override
			public void onSubscribe(Disposable d) {
				Log.i("rescounter","start 订阅.......");
			}

			@Override
			public void onNext(final ResultBean resultBean) {

				if(resultBean.getStatus() != 200){
					uploadErrorJson();
				}else if(resultBean.getStatus() == 200){
					MainActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
//						Toast.makeText(MainActivity.this,resultBean.getStatus() +resultBean.getMessage(),Toast.LENGTH_LONG).show();
							showUploadResultDialog(resultBean.getStatus()+":"+resultBean.getMessage());
							Log.i("rescounter","resultBean.getStatus():"+resultBean.getStatus() + resultBean.getMessage());

						}
					});
				}

			}

			@Override
			public void onError(final Throwable e) {
				showUploadResultDialog(e.toString());
				executorService.execute(new Runnable() {
					@Override
					public void run() {
						uploadErrorJson();
					}
				});

			}

			@Override
			public void onComplete() {
				Log.i("rescounter","onComplete()");
			}
		});
	}


	private void uploadErrorJson() {
		NetWorks.uploadErrorJson(stringBuffer.toString(), MyConstant.token, new Observer<ResultBean>() {
			@Override
			public void onSubscribe(Disposable d) {
				Log.i("rescounter","start  uploadErrorJson 订阅.......");
			}

			@Override
			public void onNext(final ResultBean resultBean) {
				MainActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
//						Toast.makeText(MainActivity.this,resultBean.getStatus() +resultBean.getMessage(),Toast.LENGTH_LONG).show();
						showUploadResultDialog(resultBean.getStatus()+":"+resultBean.getMessage());
						Log.i("rescounter","uploadErrorJson  resultBean.getStatus():"+resultBean.getStatus() + resultBean.getMessage());

					}
				});

			}

			@Override
			public void onError(final Throwable e) {
				MainActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Log.i("rescounter",e.toString());
//						Toast.makeText(MainActivity.this,resultBean.getStatus() +resultBean.getMessage(),Toast.LENGTH_LONG).show();
						showUploadResultDialog(e.toString());
					}
				});
			}

			@Override
			public void onComplete() {
				Log.i("rescounter","onComplete()");
			}
		});
	}

	private void showUploadResultDialog(final String failMessage) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
				.setIcon(R.drawable.ic_launcher)
				.setTitle(R.string.upload_result)
				.setMessage(failMessage)
				.setNegativeButton(R.string.fail, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.show();

	}


	//原始测试用的
	SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	Date date = new Date();
	public  void sendmsg(String txt) {
		date.setTime(System.currentTimeMillis());
		String d = dateFormat.format(date);
		String m =  d + ":" + "  结果:" + txt;
		recycleViewListData.add(m);
	}


	@Override
	public void onClick(View v) {

		switch (v.getId()){
			case R.id.btn_open_zfb:		//打开支护宝
				isClicked = false;
				accountBeanList = new ArrayList<>();
				accountNum = etPayAccount.getText().toString();
				payTypeNum = etPayType.getText().toString();
				if(TextUtils.isEmpty(accountNum) ||
						TextUtils.isEmpty(payTypeNum) ){
					Toast.makeText(this, R.string.accountType, Toast.LENGTH_LONG).show();
					return;
				}

				if (!PayHelperUtils.isAppRunning(context, "com.eg.android.AlipayGphone")) {
					PayHelperUtils.startAPP(context, "com.eg.android.AlipayGphone");

				}else {
					PayHelperUtils.stopApp(MainActivity.this,"com.eg.android.AlipayGphone");
					if(timer2 == null){
						timer2 = new Timer();
					}
					timer2.schedule(new TimerTask() {
						@Override
						public void run() {
							PayHelperUtils.startAPP(context, "com.eg.android.AlipayGphone");
						}
					},3000);
				}


				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						if(ruleIndex < listRuleData.size()){
							PayHelperUtils.sendAppPay(String.valueOf(listRuleData.get(ruleIndex)),PayHelperUtils.getOrderNumber(),context);
							ruleIndex++;
						}else{
							timer.cancel();
							timer = null;
						}
					}
				},35000,700);

				break;


			case R.id.btn_start_upload:
				if(isClicked){
					Toast.makeText(MainActivity.this,R.string.pleaseWaitUploadResult,Toast.LENGTH_LONG).show();
					return;
				}
				accountNum = etPayAccount.getText().toString();
				payTypeNum = etPayType.getText().toString();
				if(TextUtils.isEmpty(accountNum) ||
						TextUtils.isEmpty(payTypeNum) ){
					Toast.makeText(this, R.string.accountType, Toast.LENGTH_LONG).show();
					return;
				}
				recyclerView.setAdapter(new MyRecycleAdapter());
				executorService.execute(new Runnable() {
					@Override
					public void run() {
						isClicked = true;
						startJointUploadJson();
					}
				});


				break;
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (PayHelperUtils.isAppRunning(context, "com.eg.android.AlipayGphone")) {
			PayHelperUtils.stopApp(context, "com.eg.android.AlipayGphone");
		}
	}

	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 过滤按键动作
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			moveTaskToBack(true);
		}
		return super.onKeyDown(keyCode, event);
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(billReceived);

		timer.cancel();
		timer = null;
	}



	// 自定义接受订单通知广播
	String amount1; String qrcode = "1";

	class BillReceived extends BroadcastReceiver {
		@Override
		public void onReceive(final Context context, Intent intent) {
			try {
				if (intent.getAction().contentEquals(QRCODERECEIVED_ACTION)) {

					amount1 = intent.getStringExtra("amount");
					ordernumberStr = intent.getStringExtra("ordernumber");
					qrcode = intent.getStringExtra("qrcode");

					if(!StringUtil.isBlank(amount1) && !StringUtil.isBlank(qrcode)){
						sendmsg("生成成功,金额:" + amount1 + "订单:" + ordernumberStr + "二维码:" + qrcode);// 用来测试
						accountBeanList.add( new AccountBean(amount1,qrcode));
					}
				}

			} catch (Exception e) {
				XposedBridge.log(e.getMessage());
			}
		}

	}



	class MyRecycleAdapter extends RecyclerView.Adapter<MyRecycleAdapter.MyViewHolder>{


		@Override
		public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			MyViewHolder myViewHolder = new MyViewHolder(
					LayoutInflater.from(MainActivity.this).inflate(R.layout.item_order,parent,false));

			return myViewHolder;
		}

		@Override
		public void onBindViewHolder(MyViewHolder holder, int position) {
			holder.tv.setText(recycleViewListData.get(position));
		}

		@Override
		public int getItemCount() {
			return recycleViewListData.size();
		}



		class MyViewHolder extends RecyclerView.ViewHolder{
			TextView tv;

			public MyViewHolder(View itemView) {
				super(itemView);
				tv = (TextView) itemView.findViewById(R.id.tv_order_info);
			}
		}
	}



}
