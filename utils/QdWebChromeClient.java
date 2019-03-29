package com.zh.webview.helper.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.zh.webview.helper.MainActivity;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;

public class QdWebChromeClient extends WebChromeClient {

    private View myVideoView;
    private View myNormalView;
    private IX5WebChromeClient.CustomViewCallback callback;

    public ValueCallback<Uri> uploadFile;
    public ValueCallback<Uri[]> uploadFiles;

    private FragmentActivity context;

    private String TAG = "QdWebChromeClient";

    public QdWebChromeClient(FragmentActivity fragmentActivity) {
        context = fragmentActivity;
    }

    @Override
    public boolean onJsConfirm(WebView arg0, String arg1, String arg2,
                               JsResult arg3) {
        return super.onJsConfirm(arg0, arg1, arg2, arg3);
    }

    @Override
    public void onHideCustomView() {
        if (callback != null) {
            callback.onCustomViewHidden();
            callback = null;
        }
        if (myVideoView != null) {
            ViewGroup viewGroup = (ViewGroup) myVideoView.getParent();
            viewGroup.removeView(myVideoView);
            viewGroup.addView(myNormalView);
        }
    }

    @Override
    public boolean onJsAlert(WebView arg0, String arg1, String arg2,
                             JsResult arg3) {
        /**
         * 这里写入你自定义的window alert
         */
        return super.onJsAlert(null, arg1, arg2, arg3);
    }




    // For Android 3.0+
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        Log.i(TAG, "openFileChooser 1");
//            FilechooserActivity.this.uploadFile = uploadFile;
        openFileChooseProcess();
    }

    // For Android < 3.0
    public void openFileChooser(ValueCallback<Uri> uploadMsgs) {
        Log.i(TAG, "openFileChooser 2");
        uploadFile = uploadMsgs;
        openFileChooseProcess();
    }

    // For Android  > 4.1.1
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        Log.i(TAG, "openFileChooser 3");
        uploadFile = uploadMsg;
        openFileChooseProcess();
    }

    // For Android  >= 5.0
    public boolean onShowFileChooser(com.tencent.smtt.sdk.WebView webView,
                                     ValueCallback<Uri[]> filePathCallback,
                                     WebChromeClient.FileChooserParams fileChooserParams) {
        Log.i(TAG, "openFileChooser 4:" + filePathCallback.toString());
        uploadFiles = filePathCallback;
        openFileChooseProcess();
        return true;
    }

    private void openFileChooseProcess() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
//        i.setType("*/*");
        i.setType("image/*");
        context.startActivityForResult(Intent.createChooser(i, "图片"), 0);
    }
}
