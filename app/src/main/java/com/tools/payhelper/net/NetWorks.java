package com.tools.payhelper.net;

import com.tools.payhelper.bean.ResultBean;
import com.tools.payhelper.constants.MyConstant;


import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public class NetWorks extends RetrofitUtils {

    //创建实现接口调用
    protected static final NetService service = getRetrofit().create(NetService.class);

    //设缓存有效期为1天
    protected static final long CACHE_STALE_SEC = 60 * 60 * 24 * 1;
    //查询缓存的Cache-Control设置，使用缓存
    protected static final String CACHE_CONTROL_CACHE = "only-if-cached, max-stale=" + CACHE_STALE_SEC;

    //查询网络的Cache-Control设置。不使用缓存
    protected static final String CACHE_CONTROL_NETWORK = "max-age=0";

    private interface NetService {

        //POST请求
        @FormUrlEncoded
        @POST(MyConstant.uploadQrData)
        Observable<ResultBean> uploadJsonToServer(@Field("qrData") String qrData, @Field("token") String token);

//        //POST请求
//        @FormUrlEncoded
//        @POST("bjws/app.user/login")
//        Observable<Verification> getVerfcationCodePostMap(@FieldMap Map<String, String> map);
//
//        //GET请求
//        @GET("bjws/app.user/login")
//        Observable<Verification> getVerfcationGet(@Query("tel") String tel, @Query("password") String pass);
//
//
//        //GET请求，设置缓存
//        @Headers("Cache-Control: public," + CACHE_CONTROL_CACHE)
//        @GET("bjws/app.user/login")
//        Observable<Verification> getVerfcationGetCache(@Query("tel") String tel, @Query("password") String pass);
//
//
//        @Headers("Cache-Control: public," + CACHE_CONTROL_NETWORK)
//        @GET("bjws/app.menu/getMenu")
//        Observable<MenuBean> getMainMenu();

    }

    //POST请求
    public static void uploadJson(String qrData, String token,Observer<ResultBean> observer){
        setSubscribe(service.uploadJsonToServer(qrData, token),observer);
    }

//
//    //POST请求参数以map传入
//    public static void verfacationCodePostMap(Map<String, String> map, Observer<Verification> observer) {
//        setSubscribe(service.getVerfcationCodePostMap(map),observer);
//    }
//
//    //Get请求设置缓存
//    public static void verfacationCodeGetCache(String tel, String pass,Observer<Verification> observer) {
//        setSubscribe(service.getVerfcationGetCache(tel, pass),observer);
//    }
//
//    //Get请求
//    public static void verfacationCodeGet(String tel, String pass,Observer<Verification> observer) {
//        setSubscribe(service.getVerfcationGet(tel, pass),observer);
//    }
//
//    //Get请求
//    public static void verfacationCodeGetsub(String tel, String pass, Observer<Verification> observer) {
//        setSubscribe(service.getVerfcationGet(tel, pass),observer);
//    }
//
//    //Get请求
//    public static void Getcache( Observer<MenuBean> observer) {
//        setSubscribe(service.getMainMenu(),observer);
//    }

    public static <T> void setSubscribe(Observable<T> observable, Observer<T> observer) {
        observable.subscribeOn(Schedulers.newThread())//子线程访问网络
                .observeOn(AndroidSchedulers.mainThread())//回调到主线程
                .subscribe(observer);
    }
}
