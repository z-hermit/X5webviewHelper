package com.zh.webview.helper.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebSettings.LayoutAlgorithm;
import com.tencent.smtt.sdk.WebStorage;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class X5WebView extends WebView {

    private static final String TAG = "X5WebView";
    private Context context;

	@SuppressLint("SetJavaScriptEnabled")
	public X5WebView(Context arg0, AttributeSet arg1, WebChromeClient chromeClient) {
		super(arg0, arg1);
		context = arg0;
        this.setWebViewClient(new QdWebViewClient(context));
		this.setWebChromeClient(chromeClient);
		WebStorage webStorage = WebStorage.getInstance();
        webStorage.getOrigins(new ValueCallback<Map>() {
            @Override
            public void onReceiveValue(Map map) {
                Log.i(TAG, "onReceiveValue: "+ map);
            }
        });
		initWebViewSettings();

        //不显示滚动条
        this.setHorizontalScrollBarEnabled(false);//水平不显示
        this.setVerticalScrollBarEnabled(false); //垂直不显示

        //去掉缩放按钮
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            // Use the API 11+ calls to disable the controls
            this.getSettings().setBuiltInZoomControls(true);
            this.getSettings().setDisplayZoomControls(false);
        }

		this.getView().setClickable(true);
	}

	private void initWebViewSettings() {
		WebSettings webSetting = this.getSettings();
		webSetting.setJavaScriptEnabled(true);
		webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
		webSetting.setAllowFileAccess(true);
		webSetting.setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
		webSetting.setSupportZoom(true);
		webSetting.setBuiltInZoomControls(true);
		webSetting.setUseWideViewPort(true);
		webSetting.setSupportMultipleWindows(true);
		// webSetting.setLoadWithOverviewMode(true);
		webSetting.setAppCacheEnabled(true);
		// webSetting.setDatabaseEnabled(true);
		webSetting.setDomStorageEnabled(true);
		webSetting.setGeolocationEnabled(true);
		webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
		// webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
		webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
		// webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
		webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);


        // webSetting.setPreFectch(true);
	}
	//提示文字，是否为x5内核
//	@Override
//	protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
//		boolean ret = super.drawChild(canvas, child, drawingTime);
//		canvas.save();
//		Paint paint = new Paint();
//		paint.setColor(0x7fff0000);
//		paint.setTextSize(24.f);
//		paint.setAntiAlias(true);
//		if (getX5WebViewExtension() != null) {
//			canvas.drawText(this.getContext().getPackageName() + "-pid:"
//					+ android.os.Process.myPid(), 10, 50, paint);
//			canvas.drawText(
//					"X5  Core:" + QbSdk.getTbsVersion(this.getContext()), 10,
//					100, paint);
//		} else {
//			canvas.drawText(this.getContext().getPackageName() + "-pid:"
//					+ android.os.Process.myPid(), 10, 50, paint);
//			canvas.drawText("Sys Core", 10, 100, paint);
//		}
//		canvas.drawText(Build.MANUFACTURER, 10, 150, paint);
//		canvas.drawText(Build.MODEL, 10, 200, paint);
//		canvas.restore();
//		return ret;
//	}

	//no use
	public X5WebView(Context arg0) {
		super(arg0);
		setBackgroundColor(85621);
	}

	public void setLocalStorageItem(String key, String value) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            this.evaluateJavascript("window.localStorage.setItem('" + key + "','" + value + "');", null);
        } else {
            this.loadUrl("javascript:localStorage.setItem('" + key + "','" + value + "');");
        }
    }

    public void getLocalStorageItem(String key, String value) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            this.evaluateJavascript("window.localStorage.getItem('" + key + "');", null);
        } else {
            this.loadUrl("javascript:localStorage.getItem('" + key + "');");
        }
    }

    public void deleteLocalStorageItem(String key, String value) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            this.evaluateJavascript("window.localStorage.removeItem('" + key + "');", null);
        } else {
            this.loadUrl("javascript:localStorage.removeItem('" + key + "');");
        }
    }

    public void saveResource() {
        File filesDir = context.getFilesDir().getAbsoluteFile();
        //本地存储的文件
        File destFile = new File(filesDir, "staticHtmlcopy.html");
        //将assets路径下的文件copy到filesDir路径下，注：此处也可以通过网络访问，对页面进行存储
        InputStream is = null;
        try {
            is = context.getAssets().open("staticHtml.html");
            if (destFile.exists()) {
                destFile.delete();
            }
            FileOutputStream out = new FileOutputStream(destFile);
            try {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) >= 0) {
                    out.write(buffer, 0, bytesRead);
                }
            } finally {
                out.flush();
                try {
                    out.getFD().sync();
                } catch (IOException e) {
                }
                out.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        //拼接生成WebView使用的url地址
        String url="file://"+destFile.getAbsolutePath();
        this.loadUrl(url);

    }
    //html的script的tag的src属性呢，前缀由我们动态地添加
    public String updateTags(String html) {
        Document doc = Jsoup.parse(html, "UTF-8");
        Elements eles = doc.getElementsByTag("script");
        for (Element e : eles) {
            e.attr("src","file:///android_asset"+e.attr("src"));
        }
        return doc.toString();
    }

    //动态执行js
    private void injectScriptFile(WebView view, String scriptFile) {
        InputStream input;
        try {
            input = context.getAssets().open(scriptFile);
            byte[] buffer = new byte[input.available()];
            input.read(buffer);
            input.close();
            String js = new String(buffer);
            view.loadUrl("javascript:" + js);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
