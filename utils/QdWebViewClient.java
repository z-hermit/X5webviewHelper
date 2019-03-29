package com.zh.webview.helper.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;

import com.alipay.sdk.app.H5PayCallback;
import com.alipay.sdk.app.PayTask;
import com.alipay.sdk.util.H5PayResultModel;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.io.IOException;

public class QdWebViewClient extends WebViewClient {

    private static final String TAG = "X5WebViewClient";
    private Context context;

    public QdWebViewClient(Context context) {
        this.context = context;
    }

    @Override
    public boolean shouldOverrideUrlLoading(final WebView view, String url) {
        Log.i(TAG, "shouldOverrideUrlLoading: " + url);
        if (url.startsWith("weixin://wap/pay?")) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
            return true;

        }

        if(url.startsWith("alipays:") || url.startsWith("alipay")) {
            try {
                context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(url)));
            } catch (Exception e) {
                new AlertDialog.Builder(context)
                        .setMessage("未检测到支付宝客户端，请安装后重试。")
                        .setPositiveButton("立即安装", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Uri alipayUrl = Uri.parse("https://d.alipay.com");
                                context.startActivity(new Intent("android.intent.action.VIEW", alipayUrl));
                            }
                        }).setNegativeButton("取消", null).show();
            }
            return true;
        }

//        final PayTask task = new PayTask((Activity) context);
//        boolean isIntercepted = task.payInterceptorWithUrl(url, true, new H5PayCallback() {
//            @Override
//            public void onPayResult(final H5PayResultModel result) {
//                // 支付结果返回
//                final String url = result.getReturnUrl();
//                if (!TextUtils.isEmpty(url)) {
//                    Activity activity = (Activity)context;
//                    activity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            view.loadUrl(url);
//                        }
//                    });
//                }
//            }
//        });
//        Log.d(TAG, "shouldOverrideUrlLoading: " + isIntercepted);
//        /**
//         * 判断是否成功拦截
//         * 若成功拦截，则无需继续加载该URL；否则继续加载
//         */
//        if (isIntercepted) {
//            return true;
//        }

        return super.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        Log.i(TAG, "onPageStarted: " + url);
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        Log.i(TAG, "onPageFinished: " + url);
        super.onPageFinished(view, url);
    }

//    private WebResourceResponse editResponse() {
//        try {
//            return new WebResourceResponse("application/x-javascript", "utf-8", getAssets().open("webview.js"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        //需处理特殊情况
//        return null;
//    }
//
//    @Override
//    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
//        Log.d(TAG, "shouldInterceptRequest: " + url);
//        if (Build.VERSION.SDK_INT < 21) {
//            return editResponse();
//
//        }
//        return super.shouldInterceptRequest(view, url);
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//        if (Build.VERSION.SDK_INT >= 21) {
//            String url = request.getUrl().toString();
//
//            if (!TextUtils.isEmpty(url)) {
//                return editResponse();
//            }
//        }
//        return super.shouldInterceptRequest(view, request);
//    }
}
