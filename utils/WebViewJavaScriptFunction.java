package com.zh.webview.helper.utils;

import android.util.Log;
import android.widget.Toast;

import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebView;

public class WebViewJavaScriptFunction {
    private WebView webView;

    private WebViewJavaScriptFunction() {
    }

    public static WebViewJavaScriptFunction getInstance () {
        return SingletonHolder.instance;
    }

    /**
     * 静态内部类,只有在装载该内部类时才会去创建单例对象
     */
    private static class SingletonHolder {
        private static final WebViewJavaScriptFunction instance = new WebViewJavaScriptFunction();
    }

    public void init(WebView webView) {
        this.webView = webView;
    }

	void setDownloadState(final String tag) {
        final String TAG = "setDownloadState";
        webView.evaluateJavascript("javascript:cat.showDownloadStatus(" + tag + ")", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {
                Log.e(TAG, "onReceiveValue:"+tag);
            }
        });

    }
}
