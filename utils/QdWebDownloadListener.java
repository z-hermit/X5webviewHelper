package com.zh.webview.helper.utils;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.widget.Toast;

import com.zh.webview.helper.MainActivity;
import com.tencent.smtt.sdk.DownloadListener;
import com.tencent.smtt.utils.TbsLog;

public class QdWebDownloadListener implements DownloadListener {
    private static final String TAG = "QdWebDownloadListener";

    private Context context;

    public QdWebDownloadListener(Context context) {
        this.context = context;
    }

    @Override
    public void onDownloadStart(final String url, String userAgent,
                                String contentDisposition, String mimetype, long contentLength) {
        new AlertDialog.Builder(context)
                .setTitle("allow to download？")
                .setPositiveButton("yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                Toast.makeText(
                                        context,
                                        "fake message: i'll download...",
                                        Toast.LENGTH_SHORT).show();

                                DownloadManager.Request request = new DownloadManager.Request(
                                        Uri.parse(url));
                                // 设置允许下载的网络环境
                                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
                                        | DownloadManager.Request.NETWORK_MOBILE);
                                // 在Notification显示下载进度
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE
                                        | DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                                String filename = url.substring(url.lastIndexOf("/") + 1);
                                // 指定下载路径和下载文件名
                                request.setDestinationInExternalPublicDir("/download/", filename);
                                request.setDestinationInExternalPublicDir("dirType", "downloadfile.apk");
                                // 显示下载界面
                                request.setVisibleInDownloadsUi(true);
                                request.setTitle("应用下载中");
                                DownloadManager downloadManager = (DownloadManager) context
                                        .getSystemService(Context.DOWNLOAD_SERVICE);
                                // 将下载任务加入下载队列，否则不会进行下载
                                long fileid = downloadManager.enqueue(request);
                                if (url.endsWith(".apk")) {
                                    DownUpdateBroadcastReceiver.addDownloadId(fileid);
                                }
                            }
                        })
                .setNegativeButton("no",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // TODO Auto-generated method stub
                                Toast.makeText(
                                        context,
                                        "fake message: refuse download...",
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
                .setOnCancelListener(
                        new DialogInterface.OnCancelListener() {

                            @Override
                            public void onCancel(DialogInterface dialog) {
                                // TODO Auto-generated method stub
                                Toast.makeText(
                                        context,
                                        "fake message: refuse download...",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }).show();
    }
}
