package com.huandengpai.roadshowapplication.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.huandengpai.roadshowapplication.R;
import com.huandengpai.roadshowapplication.bean.RecordParent;
import com.huandengpai.roadshowapplication.bean.RecordeBean;
import com.huandengpai.roadshowapplication.item.Constents;
import com.huandengpai.roadshowapplication.maganer.DavikActivityManager;
import com.huandengpai.roadshowapplication.networkrequests.CommonListener;
import com.huandengpai.roadshowapplication.networkrequests.SendActtionTool;
import com.huandengpai.roadshowapplication.tool.NetworkHelper;
import com.huandengpai.roadshowapplication.tool.UrlTool;
import com.huandengpai.roadshowapplication.utils.DialogUtils;
import com.huandengpai.roadshowapplication.utils.LogUtils;
import com.huandengpai.roadshowapplication.utils.Utils;
import com.huandengpai.roadshowapplication.view.LodingDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * 发布成功后预览页面
 */
public class PreViewActivity extends BaseActivity {

    private WebView webView;
    private LodingDialog lodingDialog;
    private RelativeLayout empty_layout;
    private TextView empty_tv;
    private SwipeRefreshLayout refreshlayout;
    private Intent intent;
    private String URL;
    private boolean loadError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DavikActivityManager.getScreenManager().pushActivity(this);

    }

    @Override
    protected void findViewById() {

        intent = getIntent();
        URL = intent.getStringExtra("PreViewActivity");

        empty_layout = ((RelativeLayout) findViewById(R.id.rl_layout));
        empty_tv = ((TextView) findViewById(R.id.tv_empty));
        refreshlayout = ((SwipeRefreshLayout) findViewById(R.id.refresh_layout));
        refreshlayout.setVisibility(View.VISIBLE);
        refreshlayout.setRefreshing(true);
        refreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (webView != null && webView.getUrl()!=null)
                    webView.reload();
            }
        });


        webView = ((WebView) findViewById(R.id.webview));
        //设置userAgent
        String ua = webView.getSettings().getUserAgentString();
        WebSettings settings = webView.getSettings();
        settings.setUserAgentString(ua + Constents.USER_AGENT);
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setSavePassword(false);
        settings.setDomStorageEnabled(true);
        settings.setAppCacheMaxSize(1024 * 1024 * 8);
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        settings.setAppCachePath(appCachePath);
        settings.setAllowFileAccess(true);
        settings.setAppCacheEnabled(true);

        webView.addJavascriptInterface(new JavaScript(), "nativeMethod");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if ((lodingDialog != null) && (!lodingDialog.isShowing())) {
                    lodingDialog.show();
                } else {
                    if (lodingDialog == null) {
                        lodingDialog = DialogUtils.createLoadingDialog(PreViewActivity.this, "");
                        lodingDialog.show();
                    }
                }

                empty_layout.setVisibility(View.GONE);
                refreshlayout.setRefreshing(false);
                refreshlayout.setVisibility(View.GONE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (lodingDialog != null && lodingDialog.isShowing()) {
                    lodingDialog.dismiss();
                }

                if (loadError) {
                    empty_tv.setText("您的页面暂时走丢了....");
                    empty_layout.setVisibility(View.VISIBLE);
                    refreshlayout.setRefreshing(false);
                    refreshlayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                loadError=true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient(){
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

        });


        if (NetworkHelper.isNetworkAvailable(getApplicationContext())) {
            //   empty_tv.setText("正在努力加载中，请稍后...");
            empty_layout.setVisibility(View.GONE);
            if (lodingDialog == null) {
                lodingDialog = DialogUtils.createLoadingDialog(this, "");
            }
            lodingDialog.show();
            //WebView加载web资源
            if (URL != null) {
                URL += Constents.PREVIEW_SUCCESS;
                webView.loadUrl(URL);
                // webView.loadUrl(Constents.URL);
            } else {
                empty_layout.setVisibility(View.VISIBLE);
                empty_tv.setText("您的网络不可用，请检查网络");
                refreshlayout.setRefreshing(false);
            }
        }
//
//        if (NetworkHelper.isNetworkAvailable(getApplicationContext())) {
//            if (lodingDialog == null) {
//                lodingDialog = DialogUtils.createLoadingDialog(this, "");
//            }
//            lodingDialog.show();
//            //WebView加载web资源
//
//            } else {
//                Toast.makeText(this, "资源错误！", Toast.LENGTH_SHORT).show();
//            }
//        } else {
//            Toast.makeText(this, "您的网络不可用，请检查网络", Toast.LENGTH_SHORT).show();
//        }

    }

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        //保持当前屏幕常亮显示，即不息屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_pre_view);

    }

    public class JavaScript {
        //调用分享
        @JavascriptInterface
        public void ShareClick(String nid) {
            getShareMessage(nid);
        }

        @JavascriptInterface
        public void backClick() {
//            Toast.makeText(PreViewActivity.this, "点击返回键了", Toast.LENGTH_SHORT).show();
//            LogUtils.d("-----点击返回键了----");
            finish();
        }

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
                    if (datas != null) {
                        RecordeBean data = datas.get(0).getData();
                        // LogUtils.d("===="+data.toString());
                        if (data.getTitle() != null && data.getField_pptf_summary() != null && data.getShare_url() != null && data.getField_ppti_image_file() != null) {
                            Utils.shareTo(PreViewActivity.this, data.getTitle(), data.getField_pptf_summary(), data.getShare_url(), data.getField_ppti_image_file(), PreViewActivity.this);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.removeAllViews();
        }
    }
}
