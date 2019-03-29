package com.zh.webview.helper.utils;

import java.io.File;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

/**
 * 监听安装应用，现暂用其他接口代替
 */
public class DownUpdateBroadcastReceiver extends BroadcastReceiver {
	public static long[] apkidList = null;

	@Override
	public void onReceive(Context context, Intent intent) {

		if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
			long completeDownloadId = intent.getLongExtra(
					DownloadManager.EXTRA_DOWNLOAD_ID, -1);
			boolean isapk = false;
			boolean haveapk = true;

			if (apkidList == null) {
				haveapk = false;
			}

			if (haveapk) {
				for (int i = 0; i < 10; i++) {
					if (completeDownloadId != -1
							&& apkidList[i] == completeDownloadId) {
						Toast.makeText(context, "下载完成", Toast.LENGTH_SHORT)
								.show();
						// 进行安装游戏
						DownloadManager dManager = (DownloadManager) context
								.getSystemService(Context.DOWNLOAD_SERVICE);
						Uri downloadurl = dManager
								.getUriForDownloadedFile(completeDownloadId);
						if( downloadurl == null ){
				        	return;
				        }
						installPack(context, downloadurl, completeDownloadId);
						apkidList[i] = -1;
						isapk = true;
						break;
					}
				}
			}

			if (!isapk) {
				DownloadManager dManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
				Uri downloadurl = dManager.getUriForDownloadedFile(completeDownloadId);
				if( downloadurl == null ){
					return ;
				}
				File file = FindDownloadApk(context,completeDownloadId);
				
				if( file == null ){
					return;
				}
				if (file.exists()) {
					String filename = file.getName();
					Toast.makeText(context, "下载完成，保存至download/" + filename , Toast.LENGTH_LONG).show();
	            }
			}
		}
	}

	public static void addDownloadId(long id) {
		if (apkidList == null) {
			apkidList = new long[10];
			for (int i = 0; i < 10; i++) {
				apkidList[i] = -1;
			}
			apkidList[0] = id;
		} else {
			for (int i = 0; i < 10; i++) {
				if (apkidList[i] == -1) {
					apkidList[i] = id;
					break;
				}
			}
		}
	}

	private void installPack(Context context, Uri apk, long downloadid) {
		if (Build.VERSION.SDK_INT < 23) {
			Intent intents = new Intent();
			intents.setAction("android.intent.action.VIEW");
			intents.addCategory("android.intent.category.DEFAULT");
			intents.setType("application/vnd.android.package-archive");
			intents.setData(apk);
			intents.setDataAndType(apk,
					"application/vnd.android.package-archive");
			intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intents);
		} else {
			File file = FindDownloadApk(context, downloadid);
			if (file.exists()) {
				openFile(file, context);
			}
		}
	}

	/**
	 * 通过downLoadId查询下载的apk，解决6.0以后安装的问题
	 * 
	 * @param context
	 * @return
	 */
	public static File FindDownloadApk(Context context, long downloadid) {
		File targetApkFile = null;
		DownloadManager downloader = (DownloadManager) context
				.getSystemService(Context.DOWNLOAD_SERVICE);
		long downloadId = downloadid;
		if (downloadId != -1) {
			DownloadManager.Query query = new DownloadManager.Query();
			query.setFilterById(downloadId);
			query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);
			Cursor cur = downloader.query(query);
			if (cur != null) {
				if (cur.moveToFirst()) {
					String uriString = cur.getString(cur
							.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
					if (!TextUtils.isEmpty(uriString)) {
						targetApkFile = new File(Uri.parse(uriString).getPath());
					}
				}
				cur.close();
			}
		}
		return targetApkFile;
	}

	private void openFile(File file, Context context) {
		Intent intent = new Intent();
		intent.addFlags(268435456);
		intent.setAction("android.intent.action.VIEW");
		String type = getMIMEType(file);
		intent.setDataAndType(Uri.fromFile(file), type);
		try {
			context.startActivity(intent);
		} catch (Exception var5) {
			var5.printStackTrace();
			Toast.makeText(context, "没有找到打开此类文件的程序", Toast.LENGTH_SHORT).show();
		}
	}

	private String getMIMEType(File var0) {
		String var1 = "";
		String var2 = var0.getName();
		String var3 = var2.substring(var2.lastIndexOf(".") + 1, var2.length())
				.toLowerCase();
		var1 = MimeTypeMap.getSingleton().getMimeTypeFromExtension(var3);
		return var1;
	}

}
