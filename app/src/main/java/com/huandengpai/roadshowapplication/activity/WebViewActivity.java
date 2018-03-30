package com.huandengpai.roadshowapplication.activity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.huandengpai.roadshowapplication.R;
import com.huandengpai.roadshowapplication.bean.OpenInfoBean;
import com.huandengpai.roadshowapplication.bean.RecordParent;
import com.huandengpai.roadshowapplication.bean.RecordeBean;
import com.huandengpai.roadshowapplication.bean.ResultBean;
import com.huandengpai.roadshowapplication.bean.UserBean;
import com.huandengpai.roadshowapplication.item.Constents;
import com.huandengpai.roadshowapplication.maganer.DavikActivityManager;
import com.huandengpai.roadshowapplication.networkrequests.CommonListener;
import com.huandengpai.roadshowapplication.networkrequests.SendActtionTool;
import com.huandengpai.roadshowapplication.tool.NetworkHelper;
import com.huandengpai.roadshowapplication.tool.UrlTool;
import com.huandengpai.roadshowapplication.utils.DialogUtils;
import com.huandengpai.roadshowapplication.utils.LogUtils;
import com.huandengpai.roadshowapplication.utils.MediaUtils;
import com.huandengpai.roadshowapplication.utils.Utils;
import com.huandengpai.roadshowapplication.utils.WeakHandler;
import com.huandengpai.roadshowapplication.view.LodingDialog;
import com.huandengpai.roadshowapplication.view.VersionUpdateDialog;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.mob.tools.utils.UIHandler;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.PlatformDb;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.wechat.friends.Wechat;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * 主activity
 */
public class WebViewActivity extends BaseActivity implements CommonListener, Handler.Callback, PlatformActionListener, EasyPermissions.PermissionCallbacks {
    AsyncHttpClient client = null;
    WebView webView;

    private static final int REQUEST_FOR_FILE_PERMISSION = 1;
    private RelativeLayout empty_layout;
    private TextView empty_tv;
    private SwipeRefreshLayout refreshlayout;
    private boolean isExit = false;
    private VersionUpdateDialog updateDialog;
    private LodingDialog lodingDialog;

    //微信登录
    private String openId, openName, sex;
    private static final int MSG_USERID_FOUND = 1;
    private static final int MSG_LOGIN = 2;
    private static final int MSG_AUTH_CANCEL = 3;
    private static final int MSG_AUTH_ERROR = 4;
    private static final int MSG_AUTH_COMPLETE = 5;
    private List<String> playTag = new ArrayList<>();//播放页标志  控制转屏
    private WebActivityBroadcast webBroadcast;

    private int REQUEST_FILE_PICKER = 1;
    private ValueCallback<Uri> mFilePathCallback;
    private ValueCallback<Uri[]> mFilePathCallbacks;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DavikActivityManager.getScreenManager().pushActivity(this);
        playTag.add(Constents.ADD_PLAYTAG);
        //初始化ShareSDK
        ShareSDK.initSDK(getApplicationContext(), Constents.ShareSDK_KEY);
        //设置为竖屏模式
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initdata();
        initGetdata();
        initVersion();
        webBroadcast = new WebActivityBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("WebReceiver");
        registerReceiver(webBroadcast, intentFilter);
        requestForFilePermissions();

    }

    /**
     * 获取版本信息
     */
    private void initVersion() {
        SendActtionTool.getJson(Constents.CheckVersion, "", "CheckVersion", this);

    }

    @Override
    protected void findViewById() {
        empty_layout = ((RelativeLayout) findViewById(R.id.empty_ly));
        empty_tv = ((TextView) findViewById(R.id.empty_tv));
        refreshlayout = ((SwipeRefreshLayout) findViewById(R.id.refreshLayout));
        refreshlayout.setVisibility(View.VISIBLE);
        refreshlayout.setRefreshing(true);
        refreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initdata();

            }
        });
    }

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.webview_layout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        intent = getIntent();
        if (intent != null) {
            String s = intent.getStringExtra("preview");
            if (s != null && webView != null) {
                webView.loadUrl(s);
            }
        }
    }

    private void initGetdata() {
        String url = "";
        url = webView.getUrl();
        LogUtils.d(url + "==");
        final String cookieUrl = url;
        if (!TextUtils.isEmpty(cookieUrl)) {
            client = new AsyncHttpClient();
            client.post(url, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                    CookieManager manager = CookieManager.getInstance();
                    String cookie = manager.getCookie(cookieUrl);
                    String[] cookies = new String[50];
                    if (cookie != null) {
                        cookies = cookie.split(";");
                    }
                    if (cookies.length >= 4) {
                        String sid = cookies[0];
                        if (!TextUtils.isEmpty(sid)) {
                            String[] split = sid.split("=");
                            if (split.length >= 2) {
                                sid = split[1];
                                SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("sid", sid);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {

                }
            });
        }
    }

    private boolean loadError;

    private void initdata() {
//        获得电源控制，播放页保持屏幕不息屏，其他页面正常显示
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        final PowerManager.WakeLock powerTag = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "powerTag");
        powerTag.setReferenceCounted(false);

        webView = (WebView) findViewById(R.id.my_web);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        //关闭密码保存提醒功能
        settings.setSavePassword(false);
        settings.setDomStorageEnabled(true);
        settings.setAppCacheMaxSize(1024 * 1024 * 8);
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        settings.setAppCachePath(appCachePath);
        settings.setAllowFileAccess(true);
        settings.setAppCacheEnabled(true);

        //设置userAgent
        String ua = webView.getSettings().getUserAgentString();
        webView.getSettings().setUserAgentString(ua + Constents.USER_AGENT);

        webView.setWebChromeClient(new MyWebChromeClient());
        //与js的交互
        webView.addJavascriptInterface(new JavaScriptObject(), "nativeMethod");

        //覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
        webView.setWebViewClient(new WebViewClient() {
                                     @Override
                                     public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                         if (url.startsWith(WebView.SCHEME_TEL) ||
                                                 url.startsWith("sms:") ||
                                                 url.startsWith(WebView.SCHEME_MAILTO) ||
                                                 url.startsWith(WebView.SCHEME_GEO) ||
                                                 url.startsWith("maps:")) {
                                             try {
                                                 LogUtils.d("loading in external app");
                                                 Intent intent = new Intent(Intent.ACTION_VIEW);
                                                 intent.setData(Uri.parse(url));
                                                 startActivity(intent);
                                                 return true;
                                             } catch (android.content.ActivityNotFoundException e) {
                                                 LogUtils.d("Error openning external app" + url);
                                                 return true;
                                             }
                                         }

                                         view.loadUrl(url);
                                         initGetdata();
                                         return true;
                                     }

                                     /**
                                      * 网页页面开始加载的时候，执行的回调方法
                                      * @param view
                                      * @param url
                                      * @param favicon
                                      */
                                     @Override
                                     public void onPageStarted(WebView view, String url, Bitmap favicon) {
                                         super.onPageStarted(view, url, favicon);
                                         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                                         if ((lodingDialog != null) && (!lodingDialog.isShowing())) {
                                             lodingDialog.show();
                                         } else {
                                             if (lodingDialog == null) {
                                                 lodingDialog = DialogUtils.createLoadingDialog(WebViewActivity.this, "");
                                                 lodingDialog.show();
                                             }
                                         }
                                         weakHandler.sendEmptyMessageDelayed(100, 500);

                                         empty_layout.setVisibility(View.GONE);
                                         refreshlayout.setRefreshing(false);
                                         refreshlayout.setVisibility(View.GONE);
                                     }

                                     /**
                                      * 网页加载结束的时候执行的回调方法
                                      * @param view
                                      * @param url
                                      */
                                     @Override
                                     public void onPageFinished(WebView view, String url) {

                                         CookieManager cookieManager = CookieManager.getInstance();
                                         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                             cookieManager.flush();
                                         }
                                         String string = "com/app/c/event2a/";
                                         if (url.contains(string)) {
                                             powerTag.acquire();
                                             setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                                         } else {
                                             if (powerTag != null) {
                                                 powerTag.release();
                                             }
                                             setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                                         }
                                         if (loadError) {
                                             empty_tv.setText("您的页面暂时走丢了....");
                                             empty_layout.setVisibility(View.VISIBLE);
                                             refreshlayout.setRefreshing(false);
                                             refreshlayout.setVisibility(View.GONE);
                                         }

                                     }

                                     /**
                                      * 页面加载错误是执行的方法,但是在6.0以下，有时候会不执行这个方法
                                      * @param view
                                      * @param request
                                      * @param error
                                      */
                                     @Override
                                     public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                                         super.onReceivedError(view, request, error);
                                         loadError = true;
                                     }
                                 }


        );
        webView.setWebChromeClient(new WebChromeClient() {

            /**
             * 当WebView加载之后，返回HTML页面的标题title
             * @param view
             * @param title
             */
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                //判断标题 title 中是否包含有“error”字段，如果包含“error”字段，则设置加载失败，显示加载失败的视图
                if ((!TextUtils.isEmpty(title) && title.toLowerCase().contains("error")) || (!TextUtils.isEmpty(title) && title.contains("找不到网页")) || (!TextUtils.isEmpty(title) && title.toLowerCase().contains("TIMED_OUT"))) {
                    loadError = true;
                    empty_tv.setText("您的页面暂时走丢了....");
                    empty_layout.setVisibility(View.VISIBLE);
                    refreshlayout.setRefreshing(false);
                }
            }

            public void openFileChooser(ValueCallback<Uri> filePathCallback) {
                mFilePathCallback = filePathCallback;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(Intent.createChooser(intent, "File Chooser"),
                        REQUEST_FILE_PICKER);
            }

            // 3.0 + 调用这个方法
            public void openFileChooser(ValueCallback filePathCallback,
                                        String acceptType) {
                mFilePathCallback = filePathCallback;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(Intent.createChooser(intent, "File Chooser"),
                        REQUEST_FILE_PICKER);
            }

            //  / js上传文件的<input type="file" name="fileField" id="fileField" />事件捕获
            // Android > 4.1.1 调用这个方法
            public void openFileChooser(ValueCallback<Uri> filePathCallback,
                                        String acceptType, String capture) {
                mFilePathCallback = filePathCallback;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(Intent.createChooser(intent, "File Chooser"),
                        REQUEST_FILE_PICKER);
            }

            @Override
            public boolean onShowFileChooser(WebView webView,
                                             ValueCallback<Uri[]> filePathCallback,
                                             WebChromeClient.FileChooserParams fileChooserParams) {
                mFilePathCallbacks = filePathCallback;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(Intent.createChooser(intent, "File Chooser"),
                        REQUEST_FILE_PICKER);
                return true;
            }
        });


        if (NetworkHelper.isNetworkAvailable(getApplicationContext())) {
            //   empty_tv.setText("正在努力加载中，请稍后...");
            empty_layout.setVisibility(View.GONE);
            if (lodingDialog == null) {
                lodingDialog = DialogUtils.createLoadingDialog(this, "");
            }
            lodingDialog.show();
            //WebView加载web资源
            webView.loadUrl(Constents.URL);
        } else {
            empty_layout.setVisibility(View.VISIBLE);
            empty_tv.setText("您的网络不可用，请检查网络");
            refreshlayout.setRefreshing(false);
        }

    }


    //按返回键时不是退出程序而是返回上一浏览页面
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        LogUtils.d("===========webcurrentURL==" + webView.getUrl());
        String currentURL = webView.getUrl();
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (currentURL.contains(Constents.ADD_PLAYTAG)) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            if (currentURL.contains(Constents.CAN_GOBACKTAG) && webView.canGoBack()) {
                webView.goBack();
            }
            if ((!currentURL.endsWith("/home")) && (!currentURL.contains("user")) && (!currentURL.contains("/app/misc/example/")) && (!currentURL.contains("com/app/c/my/made/public2/list/")) && webView.canGoBack()) {
                webView.goBack();
                return true;
            } else {
                return super.onKeyDown(keyCode, event);
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    //根据uid获得用户的基本信息
    private void getUserDetail() {
        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        String string = sharedPreferences.getString(Constents.UID, "");
        LogUtils.d("==*****=Uid==" + string);
        if (!TextUtils.isEmpty(string)) {
            Map<String, String> map = UrlTool.getMapParams("uid", string);
            String jsonstr = new Gson().toJson(map);
            SendActtionTool.getJson(Constents.USER_Message, jsonstr, "getUser", this);
        }

    }


    @Override
    public void onSuccess(Object action, Object value) {
        LogUtils.d("onSuccess" + value.toString());
        switch (((String) action)) {
            //版本更新
            case "CheckVersion":
                LogUtils.d("==check--version====" + value.toString());
//                ResultBean<VersionBean> result = new Gson().fromJson(value.toString(), new TypeToken<ResultBean<VersionBean>>() {
//                }.getType());
//                VersionBean data = result.getData();
//                if (data != null && (!BaseActivity.mVersionName.equals(data.getVersion()))) {
//                    updateDialog = new VersionUpdateDialog(this, R.style.dialog_general, data, this);
//                    updateDialog.show();
//                }
                break;
            case "getUser":
                ResultBean<UserBean> resultBean = new Gson().fromJson(value.toString(), new TypeToken<ResultBean<UserBean>>() {
                }.getType());
                UserBean userBean = resultBean.getData();
                if (userBean != null) {
                    String mail = userBean.getEmail();
                    if (!TextUtils.isEmpty(mail)) {
                        // initKF5SDK(mail);
                        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(Constents.USER_EMAIL, mail);
                        editor.commit();

                    }
                }

                SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
                String string = sharedPreferences.getString(Constents.USER_EMAIL, "");
                if (!TextUtils.isEmpty(string)) {
                    LogUtils.d("---semailg---" + string);
                }
                break;
        }

    }

    @Override
    public void onFaile(Object action, Object value) {

    }

    @Override
    public void onException(Object action, Object value) {

    }

    @Override
    public void onStart(Object action) {

    }

    @Override
    public void onFinish(Object action) {

    }


    @Override
    public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {

        if (i == Platform.ACTION_USER_INFOR) {
            Message message = new Message();
            message.what = MSG_AUTH_COMPLETE;
            message.obj = platform;
            UIHandler.sendMessage(message, this);
        }
        System.out.println(hashMap);
    }

    @Override
    public void onError(Platform platform, int i, Throwable throwable) {

    }

    @AfterPermissionGranted(REQUEST_FOR_FILE_PERMISSION)
    private void requestForFilePermissions() {
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "此操作需要打开操作权限", REQUEST_FOR_FILE_PERMISSION, perms);
        }
    }

    @Override
    public void onCancel(Platform platform, int i) {

    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }


    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Toast.makeText(this, "只有打开手机文件权限才能进行相应操作，请打开本应用的操作权限", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_FILE_PICKER) {
            if (mFilePathCallback != null) {
                Uri result = intent == null || resultCode != Activity.RESULT_OK ? null
                        : intent.getData();
                if (result != null) {
                    String path = MediaUtils.getPath(getApplicationContext(),
                            result);
                    Uri uri = Uri.fromFile(new File(path));
                    mFilePathCallback.onReceiveValue(uri);
                } else {
                    mFilePathCallback.onReceiveValue(null);
                }
            }
            if (mFilePathCallbacks != null) {
                Uri result = intent == null || resultCode != Activity.RESULT_OK ? null
                        : intent.getData();
                if (result != null) {
                    String path = MediaUtils.getPath(getApplicationContext(),
                            result);
                    Uri uri = Uri.fromFile(new File(path));
                    mFilePathCallbacks
                            .onReceiveValue(new Uri[]{uri});
                } else {
                    mFilePathCallbacks.onReceiveValue(null);
                }
            }

            mFilePathCallback = null;
            mFilePathCallbacks = null;
        }
    }

    public class JavaScriptObject {
        @JavascriptInterface
        //sdk17版本以上加上注解
        //js进行交互，点击录音键进入ppt列表页
        public void voiceClick(String string) {
            Intent intent = new Intent(getApplicationContext(), VoiceActivity.class);
            //TODO 得到用户信息
            intent.putExtra(Constents.UID, string);
            LogUtils.d("===UID-==" + string);
            SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Constents.UID, string);
            editor.commit();
            getUserDetail();
            startActivity(intent);
        }

        @JavascriptInterface
        //获得用户uid并保存
        public void sendUid(String uid) {
            //将返回的uid保存到本地
            if (uid != null) {
                SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Constents.UID, uid);
                editor.commit();
                getUserDetail();
            }

        }

        //调用分享
        @JavascriptInterface
        public void ShareClick(String nid) {
            LogUtils.d("===share==" + nid);
            if (!TextUtils.isEmpty(nid)) {
                getShareMessage(nid);
            }
        }

        @JavascriptInterface
        public void WxLogin() {
            Toast.makeText(WebViewActivity.this, "正在跳转中，请稍后...", Toast.LENGTH_SHORT).show();
            loginByWx();
        }
    }

    //微信登录
    private void loginByWx() {
        authorize(ShareSDK.getPlatform(this, Wechat.NAME));
    }

    private void authorize(Platform plat) {
        if (plat.isValid()) {
            LogUtils.d("====plat.isValid()====");
            plat.removeAccount();
            String userId = plat.getDb().getUserId();
            LogUtils.d("===userId==" + userId);
            if (!TextUtils.isEmpty(userId)) {
                UIHandler.sendEmptyMessage(MSG_USERID_FOUND, this);
                LogUtils.d("==TextUtils.isEmpty(userId)==MSG_USERID_FOUND");
                login(plat, userId, null);
                return;
            }
        }
        plat.setPlatformActionListener(this);
        plat.SSOSetting(false);
        plat.showUser(null);
    }

    private void login(Platform plat, String userId, HashMap<String, Object> userInfo) {
        LogUtils.d("===login==");
        Message msg = new Message();
        msg.what = MSG_LOGIN;
        msg.obj = plat;
        UIHandler.sendMessage(msg, this);
        LogUtils.d("====sendMessage===");
    }

    private OpenInfoBean openInfo;

    //微信接口返回结果
    @Override
    public boolean handleMessage(Message msg) {
        LogUtils.d("===msg==" + msg.what);
        switch (msg.what) {
            case MSG_USERID_FOUND: {
                Toast.makeText(this, "正在跳转登录操作...", Toast.LENGTH_SHORT).show();
            }
            break;
            case MSG_LOGIN: {
                Platform plat = (Platform) msg.obj;
                PlatformDb platDB = plat.getDb();
                openId = platDB.getUserId();
                openName = platDB.getUserName();
                sex = platDB.getUserGender();
                //o8Hzev2lDmOj3YKIxjDQJQS6VaxM
            }
            break;
            case MSG_AUTH_CANCEL: {
                Toast.makeText(this, "授权取消", Toast.LENGTH_SHORT).show();
                System.out.println("-------MSG_AUTH_CANCEL--------");
            }
            break;
            case MSG_AUTH_ERROR: {
                Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
                System.out.println("-------MSG_AUTH_ERROR--------");
            }
            break;
            case MSG_AUTH_COMPLETE: {
                Toast.makeText(this, "正在跳转登录操作,请稍后...", Toast.LENGTH_SHORT).show();
                Platform plat = (Platform) msg.obj;
                PlatformDb platDB = plat.getDb();
                String uniond = platDB.get("unionid");
                String opid = platDB.get("openid");
                String name = platDB.getUserName();
                String userIcon = platDB.getUserIcon();

//                app/u/joinlogin/weixin/entry?source=pptAPP&nickname=%&openid=%&unionid=%&headimgurl=%
                StringBuilder builder = new StringBuilder();
                builder.append("source=pptAPP").append("&nickname=").append(name).append("&openid=").append(opid).append("&headimgurl=").append(userIcon).append("&unionid=").append(uniond);
                String string = builder.toString();
                String url = Constents.WX_LOGIN + string;
                LogUtils.d(url);
                if (webView != null && url != null) {
                    webView.loadUrl(url);
                }
                LogUtils.d("==unioind===" + uniond);
            }
            break;
        }
        return false;
    }


    //得到分享需要的信息
    private void getShareMessage(String nid) {
        Map<String, String> map = UrlTool.getMapParams("nid", nid);
        String json = new Gson().toJson(map);
        SendActtionTool.getJson(Constents.GetShareMessage, nid, "shareMessage", new CommonListener() {
            @Override
            public void onSuccess(Object action, Object value) {
                LogUtils.d("onSuccess==" + value.toString());
                try {
                    List<RecordParent.RecordData> datas = new Gson().fromJson(((JSONObject) value).getString("datas"), new TypeToken<List<RecordParent.RecordData>>() {
                    }.getType());
                    if (datas != null && datas.size() > 0) {
                        LogUtils.d("====datas==" + datas.get(0).toString());
                        RecordeBean data = datas.get(0).getData();
                        if (data == null) {
                            return;
                        }
                        if (data.getTitle() != null && data.getField_pptf_summary() != null && data.getShare_url() != null && data.getField_ppti_image_file() != null) {
                            Utils.shareTo(WebViewActivity.this, data.getTitle(), data.getField_pptf_summary(), data.getShare_url(), data.getField_ppti_image_file(), WebViewActivity.this);
                        }
                    }
                } catch (JSONException e) {
                    Log.e("My_Error", e.toString());
                }
            }

            @Override
            public void onFaile(Object action, Object value) {
                LogUtils.d("onFaile" + value.toString() + action.toString());
            }

            @Override
            public void onException(Object action, Object value) {
                LogUtils.d("onException" + value.toString() + action.toString());

            }

            @Override
            public void onStart(Object action) {

            }

            @Override
            public void onFinish(Object action) {
                LogUtils.d("onfinish");

            }
        });
    }

    //备用 向js传值
    final class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Log.i("Messsage", message);
            result.confirm();
            return true;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onBackPressed() {
        exitBy2Click();
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void exitBy2Click() {
        Timer tExit = null;
        if (isExit == false) {
            isExit = true; // 准备退出
            Toast.makeText(this, "再次点击返回键退出", Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

        } else {
            //finish();
            DavikActivityManager.getScreenManager().exitApp(this.getClass());
           // System.exit(0);
            MobclickAgent.onKillProcess(this);
            System.exit(0);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateDialog != null && updateDialog.isShowing()) {
            updateDialog.dismiss();
        }
        if (lodingDialog != null && lodingDialog.isShowing()) {
            lodingDialog.dismiss();
        }

        //删除WebView的缓存
        boolean b = this.deleteDatabase("webview.db");
        boolean b1 = this.deleteDatabase("webviewCache.db");
        unregisterReceiver(webBroadcast);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    class WebActivityBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if ("1".equals(intent.getStringExtra("postSuccess"))) {
                webView.loadUrl(Constents.URL);
            }

        }
    }

    private WeakHandler weakHandler = new WeakHandler(this) {
        @Override
        public void conventHandleMessage(Message msg) {

            switch (msg.what) {
                case 100:
                    if (lodingDialog != null && lodingDialog.isShowing()) {
                        lodingDialog.dismiss();
                    }
                    break;
            }

        }
    };


}
