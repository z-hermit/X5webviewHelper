package com.zh.webview.helper;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.connection.FileDownloadUrlConnection;
import com.tencent.smtt.sdk.QbSdk;

import java.net.Proxy;

public class APPAplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		//搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。
		Log.i("appAplication", " appAplication onCreate, "+System.currentTimeMillis());
		QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
			
			@Override
			public void onViewInitFinished(boolean arg0) {
				//x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
				Log.i("appAplication", " onViewInitFinished is " + arg0+"   " +System.currentTimeMillis());
			}
			
			@Override
			public void onCoreInitFinished() {
				Log.i("appAplication", " onCoreInitFinished "+"   " +System.currentTimeMillis());
			}
		};
		//x5内核初始化接口
		QbSdk.initX5Environment(getApplicationContext(),  cb);

		FileDownloader
				.setupOnApplicationOnCreate(this)
				.connectionCreator(
						new FileDownloadUrlConnection.Creator(
								new FileDownloadUrlConnection.Configuration()
										.connectTimeout(300000)
										.readTimeout(300000)
										.proxy(Proxy.NO_PROXY))).commit();
	}

}
