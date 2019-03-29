package com.zh.webview.helper.utils;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.smtt.sdk.DownloadListener;
import com.tencent.smtt.sdk.WebView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;

public class JavaFunctionManager {

    private WebView webView;
    private Context context;

    // 用于存放安装包url与对应id的map
    public static HashMap<String, String> mHashpackname = new HashMap<String, String>();

    public JavaFunctionManager(WebView wv, Context context) {
        webView = wv;
        this.context = context;
        webView.addJavascriptInterface(new CallByJS(), "JavaFunctions");

    }

    public class CallByJS extends Object {
        private String TAG = "CallByJs";
        @JavascriptInterface
        public void downloadfile(String downurl, String filetype) {
            // 参数1 下载链接 参数2 文件存放名字 参数3 下载提示
            // 创建下载任务,downloadUrl就是下载链接
            DownloadManager.Request request = new DownloadManager.Request(
                    Uri.parse(downurl));
            // 设置允许下载的网络环境
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
                    | DownloadManager.Request.NETWORK_MOBILE);
            // 在Notification显示下载进度
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE
                    | DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            String filename = downurl.substring(downurl.lastIndexOf("/") + 1);
            // 指定下载路径和下载文件名
            request.setDestinationInExternalPublicDir("/download/", filename);
            // 显示下载界面
            request.setVisibleInDownloadsUi(true);
            request.setTitle("下载");
            request.setDescription("正在下载");
            request.setAllowedOverRoaming(false);
            //设置文件存放目录
//            request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "mydown");

            DownloadManager downManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
            long fileid= downManager.enqueue(request);

            // 假设是apk需要自动安装功能
            if (filetype != null && filetype.equals("apk")) {
                DownUpdateBroadcastReceiver.addDownloadId(fileid);
            }
            Toast.makeText(context, "已加入下载，请注意查看", Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public void openqq(String qqnumber) {
            boolean canOpenQQ = false;
            final PackageManager packageManager = context.getPackageManager();
            List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
            if (pinfo != null) {
                for (int i = 0; i < pinfo.size(); i++) {
                    String pn = pinfo.get(i).packageName;
                    if (pn.equals("com.tencent.mobileqq")) {
                        canOpenQQ = true;
                        break;
                    }
                }
            }

            if (canOpenQQ == true) {
                String qqurl = "http://wpa.b.qq.com/cgi/wpa.php?ln=2&uin="
                        + qqnumber;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(qqurl));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                String qqurl = "http://crm2.qq.com/page/portalpage/wpa.php?aty=1&a=0&curl=&ty=1&uin="
                        + qqnumber;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(qqurl));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }

        @android.webkit.JavascriptInterface
        // 4.0之后要加这个注解
        public void openWechat() {
            //打开微信页面
            try {
                // set our wechat to clipboard
                ClipboardManager clipboard = (ClipboardManager)
                        context.getSystemService(Context.CLIPBOARD_SERVICE);
                ContentResolver cr = context.getContentResolver();
                ClipData clip = ClipData.newPlainText("simple text", "gh_a6f8f6882ef4");
                clipboard.setPrimaryClip(clip);

                Intent intent=new Intent(Intent.ACTION_MAIN);
                ComponentName componentName=new ComponentName("com.tencent.mm","com.tencent.mm.ui.LauncherUI");
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setComponent(componentName);
                context.startActivity(intent);
            }catch (ActivityNotFoundException e){
                Toast.makeText(context, "检查到手机没安装微信，请安装后使用该功能", Toast.LENGTH_LONG).show();
            }
        }

        @JavascriptInterface
        // 4.0之后要加这个注解
        public void openBrowser(String url) {
            Intent brointent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            brointent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(brointent);
        }

        @JavascriptInterface
        public void CopyToClipboard(String text){
            ClipboardManager clip = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
            //clip.getText(); // 粘贴
            try {
                clip.setText(text); // 复制
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        @JavascriptInterface
        public void webDownload(String url, String state, String id,
                                String packname, String version) {
            int tempstateInteger = Integer.valueOf(state);
            int idInteger = Integer.valueOf(id);

            switch (tempstateInteger) {
                case 1:
                case 3:
                    idInteger = createDownloadTask(url, id, packname).start();
                    Log.e("downidstart", "downidstart" + idInteger);
                    Toast.makeText(context, "开始下载", Toast.LENGTH_SHORT).show();
                    mHashpackname.put(packname, id.toString());
                    break;
                case 2:
                    // 重新下载是为了获取idInteger的值，再暂停2次下载
                    idInteger = createDownloadTask(url, id, packname).getId();
                    FileDownloader.getImpl().pause(idInteger);
                    Log.e("downidpause", "downidpause" + idInteger);
                    break;
                case 4:
                    String fileloc = Environment.getExternalStorageDirectory()
                            .getPath() + "/download/" + packname + ".apk";
                    try {
                        File filetemp = new File(fileloc);
                        if (filetemp.exists()) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.fromFile(filetemp),
                                    "application/vnd.android.package-archive");
                            context.startActivity(intent);
                        } else {
                            Toast.makeText(context, "安装包不存在", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, "安装包不存在", Toast.LENGTH_SHORT)
                                .show();
                    }
                    break;
                default:
                    break;
            }
        }
        @JavascriptInterface
        public void WXShareUrl(String url, String title, String des, int type) {
            Log.d(TAG, "WXShareUrl: " + url + ", title: " + title + ", des:" + des + ", type: " + type);
            WXShare wxShare = WXShare.getInstance();
            wxShare.shareUrl(url, title, des, type);
        }
        @JavascriptInterface
        public boolean isInstalledAPP(String packageName) {
            String TAG = "checkInstalledAPP";
            PackageManager packageManager = context.getPackageManager();
            try {
                List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
                for (int i = 0; i < packageInfos.size(); i++) {
                    PackageInfo packageInfo = packageInfos.get(i);
                    //过滤掉系统app
                    if ((ApplicationInfo.FLAG_SYSTEM & packageInfo.applicationInfo.flags) != 0) {
                        continue;
                    }
                    String myAppInfo =packageInfo.packageName;
                    if (packageInfo.applicationInfo.loadIcon(packageManager) == null) {
                        Log.e(TAG,"获取应用包icon信息失败");
                        continue;
                    }
                    if (myAppInfo.equalsIgnoreCase(packageName)) {
                        return true;
                    }
                }
            }catch (Exception e){
                Log.e(TAG,"===============获取应用包信息失败");
                return false;
            }
            return false;
        }
        @JavascriptInterface
        public boolean isPackageExist(String packageName)
        {
            String filename = Environment.getExternalStorageDirectory()
                    .getPath() + "/download/" + packageName + ".apk";
            try {
                File f=new File(filename);
                if(!f.exists()) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
            return true;
        }

        private BaseDownloadTask createDownloadTask(final String tempurl,
                                                    final String id, final String gamename) {
            ViewHolder tag = null;
            String url = null;
            // 把下载路径设置在sdcard/download
            String path = Environment.getExternalStorageDirectory().getPath() + "/"
                    + "download";
//            String path = context.getFilesDir() + "/" + "download";
            File pathFile = new File(path);
            if (!pathFile.exists()) {
                // 若不存在，创建目录
                Log.i("filedownload", "createDownloadTask: " + pathFile.mkdirs());
            }
            url = tempurl;
            tag = new ViewHolder(context, url, id, gamename);
            tag.setFilenameTv(gamename);
            return FileDownloader.getImpl().create(url).setPath(path, true)
                    .setTag(tag).setCallbackProgressTimes(300)
                    .setMinIntervalUpdateSpeed(400)
                    .setListener(new FileDownloadSampleListener() {
                        @Override
                        protected void pending(BaseDownloadTask task,
                                               int soFarBytes, int totalBytes) {
                            super.pending(task, soFarBytes, totalBytes);
                            ((ViewHolder) task.getTag()).updatePending(task);

                        }

                        @Override
                        protected void progress(BaseDownloadTask task,
                                                int soFarBytes, int totalBytes) {
                            super.progress(task, soFarBytes, totalBytes);
                            ((ViewHolder) task.getTag()).updateProgress(soFarBytes,
                                    totalBytes, task.getSpeed());
                        }

                        @Override
                        protected void error(BaseDownloadTask task, Throwable e) {
                            super.error(task, e);
                            ((ViewHolder) task.getTag()).updateError(e,
                                    task.getSpeed());
                        }

                        @Override
                        protected void connected(BaseDownloadTask task,
                                                 String etag, boolean isContinue, int soFarBytes,
                                                 int totalBytes) {
                            super.connected(task, etag, isContinue, soFarBytes,
                                    totalBytes);
                            ((ViewHolder) task.getTag()).updateConnected(etag,
                                    task.getFilename());
                        }

                        @Override
                        protected void paused(BaseDownloadTask task,
                                              int soFarBytes, int totalBytes) {
                            super.paused(task, soFarBytes, totalBytes);
                            Log.e("taskpause", "taskpausetag" + task.getTag());
                            ((ViewHolder) task.getTag()).updatePaused(task
                                    .getSpeed());
                        }

                        @Override
                        protected void completed(BaseDownloadTask task) {
                            super.completed(task);
                            ((ViewHolder) task.getTag()).updateCompleted(task);
                        }
                    });
        }

        private class ViewHolder {
            private Context context;
            private String filenameTv;
            private String id;
            private String packname;

            public ViewHolder(final Context context, final String filenameTv,
                              final String id, final String packname) {
                this.context = context;
                this.filenameTv = filenameTv;
                this.id = id;
                this.packname = packname;
            }

            public void setFilenameTv(String filenameTv) {
                this.filenameTv = filenameTv;
            }

            public void updateProgress(final int sofar, final int total,
                                       final int speed) {
                if (sofar % 20 == 0) {
                    Log.e("webdownpro", "webdownpro	,sofar:" + sofar + ",total:"
                            + total);
                }
                WebViewJavaScriptFunction javaScriptFunction = WebViewJavaScriptFunction.getInstance();
                javaScriptFunction.setDownloadState("2");
            }

            public void updatePending(BaseDownloadTask task) {
                WebViewJavaScriptFunction javaScriptFunction = WebViewJavaScriptFunction.getInstance();
                javaScriptFunction.setDownloadState("1");
            }

            public void updatePaused(final int speed) {
                Toast.makeText(context, "暂停下载", Toast.LENGTH_SHORT).show();
                WebViewJavaScriptFunction javaScriptFunction = WebViewJavaScriptFunction.getInstance();
                javaScriptFunction.setDownloadState("3");
            }

            public void updateConnected(String etag, String filename) {
                if (filenameTv != null) {
                    filenameTv = filename;
                }
            }

            public void updateError(final Throwable ex, final int speed) {
                Toast.makeText(context, "网络错误", Toast.LENGTH_SHORT).show();
                ex.printStackTrace();
                WebViewJavaScriptFunction javaScriptFunction = WebViewJavaScriptFunction.getInstance();
                javaScriptFunction.setDownloadState("5");
            }

            public void updateCompleted(final BaseDownloadTask task) {
                // 下载完毕从tmp改成apk文件
                WebViewJavaScriptFunction javaScriptFunction = WebViewJavaScriptFunction.getInstance();
                javaScriptFunction.setDownloadState("4");
                String filenewname = Environment.getExternalStorageDirectory()
                        .getPath() + "/download/" + packname + ".apk";
                File fileoldname = new File(task.getTargetFilePath());
                fileoldname.renameTo(new File(filenewname));

                // 弹出安装，需修改路径
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(filenewname)),
                        "application/vnd.android.package-archive");
                context.startActivity(intent);
            }
        }

    }
}
