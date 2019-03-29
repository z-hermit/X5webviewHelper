package com.zh.webview.helper.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import com.zh.webview.helper.R;
import com.tencent.mm.opensdk.constants.Build;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class WXShare {
    private String TAG = "WXShare";
    // APP_ID 替换为你的应用从官方网站申请到的合法appID
    private static final String APP_ID = "APP_ID";

    // IWXAPI 是第三方app和微信通信的openApi接口
    private IWXAPI api = null;

    private Context context;

    private WXShare() {

    }

    static private WXShare instance = new WXShare();

    static public WXShare getInstance() {
        return instance;
    }

    public void init(Context context) {
        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        api = WXAPIFactory.createWXAPI(context, APP_ID, false);

        // 将应用的appId注册到微信
        api.registerApp(APP_ID);
        this.context = context;
    }

    public IWXAPI getApi() {
        return api;
    }

    //    type = 0: SendMessageToWX.Req.WXSceneSession,type = 1 SendMessageToWX.Req.WXSceneTimeline
    public void shareUrl(String url, String title, String des, int type) {
        if (api == null) {
            Log.e(TAG, "api is not init");
            return;
        }
        //初始化一个WXWebpageObject，填写url
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl =url;

        //用 WXWebpageObject 对象初始化一个 WXMediaMessage 对象
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title =title;
        msg.description =des;
//        Bitmap thumbBmp = BitmapFactory.decodeResource(getResources(), R.drawable.send_music_thumb);
//        msg.thumbData =Util.bmpToByteArray(thumbBmp, true);

        //构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("wxshare");
        req.message =msg;
        if (type == 0) {
            req.scene = SendMessageToWX.Req.WXSceneSession; //好友
        } else if (type == 1) {
            if (api.getWXAppSupportAPI() >= Build.TIMELINE_SUPPORTED_SDK_INT)
                req.scene = SendMessageToWX.Req.WXSceneTimeline; //朋友圈
            else
                Toast.makeText(context, "微信版本不支持发送到朋友圈", Toast.LENGTH_SHORT).show();
        }

        //调用api接口，发送数据到微信
        api.sendReq(req);
    }

    public static String getAppId() {
        return APP_ID;
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }
}
