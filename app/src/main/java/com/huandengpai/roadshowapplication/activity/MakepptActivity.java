package com.huandengpai.roadshowapplication.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.huandengpai.roadshowapplication.R;
import com.huandengpai.roadshowapplication.bean.ChangeStatusBean;
import com.huandengpai.roadshowapplication.bean.DetailBean;
import com.huandengpai.roadshowapplication.bean.Node;
import com.huandengpai.roadshowapplication.item.Constents;
import com.huandengpai.roadshowapplication.maganer.DavikActivityManager;
import com.huandengpai.roadshowapplication.networkrequests.CommonListener;
import com.huandengpai.roadshowapplication.networkrequests.SendActtionTool;
import com.huandengpai.roadshowapplication.tool.UrlTool;
import com.huandengpai.roadshowapplication.utils.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 制作PPT
 */
public class MakepptActivity extends BaseActivity implements View.OnClickListener, CommonListener {

    private RadioButton back_bt;
    private TextView title_tv;
    private Node intentNode;
    private EditText ed_title;
    private RadioButton bt_category;
    private RadioButton bt_voice;
    private EditText ed_introduce;
    private LinearLayout saveInfo_ly;
    private LinearLayout save_enter_ly;
    private LinearLayout enter_ly;
    private ReceiveChangeBroadcast broadcast;
    private DetailBean detailBean;
    private RadioButton bt_publish;
    private LinearLayout secretLayout;
    private EditText ed_secret;
    private TextView tv_secret_tip;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        DavikActivityManager.getScreenManager().pushActivity(this);
    }

    @Override
    protected void findViewById() {
        back_bt = ((RadioButton) findViewById(R.id.back_bt));
        back_bt.setOnClickListener(this);
        title_tv = ((TextView) findViewById(R.id.title_tv));
        title_tv.setText("基本信息");
        ed_introduce = ((EditText) findViewById(R.id.ed_info_introduce));
        ed_title = ((EditText) findViewById(R.id.ed_title));

        secretLayout = ((LinearLayout) findViewById(R.id.ll_info_secret));
        ed_secret = ((EditText) findViewById(R.id.ed_secret));
        tv_secret_tip = ((TextView) findViewById(R.id.tv_secret_tip));
        secretLayout.setVisibility(View.INVISIBLE);
        tv_secret_tip.setVisibility(View.INVISIBLE);

        bt_category = ((RadioButton) findViewById(R.id.tv_catgory));
        bt_voice = ((RadioButton) findViewById(R.id.tv_voice));
        bt_publish = ((RadioButton) findViewById(R.id.tv_publish));
        bt_category.setOnClickListener(this);
        bt_voice.setOnClickListener(this);
        bt_publish.setOnClickListener(this);

        saveInfo_ly = ((LinearLayout) findViewById(R.id.ll_saveinfo));
        save_enter_ly = ((LinearLayout) findViewById(R.id.ll_saveinfo_enter));
        enter_ly = ((LinearLayout) findViewById(R.id.ll_enter));

        saveInfo_ly.setOnClickListener(this);
        save_enter_ly.setOnClickListener(this);
        enter_ly.setOnClickListener(this);

    }

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_makeppt);
        Intent intent = getIntent();
        if (intent != null) {
            intentNode = (Node) intent.getSerializableExtra("MakepptActivity");
            getURLData();
        }

        //注册广播
        broadcast = new ReceiveChangeBroadcast();
        IntentFilter filter = new IntentFilter();
        filter.addAction("MakepptActivity");
        registerReceiver(broadcast, filter);
    }

    //获得ppt基本信息
    private void getURLData() {
        long timeMillis = System.currentTimeMillis();
        if (intentNode.getNid() != null) {
            Map<String, String> map = UrlTool.getMapParams("nid", intentNode.getNid());
            String str = new Gson().toJson(map);
            SendActtionTool.getJson(Constents.GETPPT_DETAIL, str, "getDetail", this);
        }
    }

    @Override
    public void onClick(View view) {
        String valueStr="";
        switch (view.getId()) {
            case R.id.back_bt:
                finish();
                break;
            case R.id.tv_catgory:
                valueStr=bt_category.getText().toString();
                Intent intent = new Intent(MakepptActivity.this, CategoryActivity.class);
                intent.putExtra("name", "分类");
                intent.putExtra("value",valueStr);
                startActivity(intent);
                break;
            case R.id.tv_publish:
                valueStr=bt_publish.getText().toString();
                Intent intentt = new Intent(MakepptActivity.this, CategoryActivity.class);
                intentt.putExtra("name", "发布方式");
                intentt.putExtra("value",valueStr);
                startActivity(intentt);
                break;
            case R.id.tv_voice:
                Intent intent1 = new Intent(this, MakeVoiceActivity.class);
                valueStr=bt_voice.getText().toString();
                intent1.putExtra("value",valueStr);
                startActivity(intent1);
                break;
            case R.id.ll_saveinfo:
                upLoadChange("saveChange");
                break;
            case R.id.ll_saveinfo_enter:
                upLoadChange("saveAndEnter");
                break;
            case R.id.ll_enter:
                Intent intent2 = new Intent(this, RecordingActivity.class);
                intent2.putExtra("RecordingActivity", intentNode.getNid());
                startActivity(intent2);
                break;

        }

    }

    private void upLoadChange(String saveChange) {
        String title = ed_title.getText().toString();
        String category = bt_category.getText().toString();
        String voice = bt_voice.getText().toString();
        String vv = "";
        String secret = ed_secret.getText().toString();
        String pb = bt_publish.getText().toString();
        LogUtils.d("发布方式------"+pb);
        String pub = "";

        if ("公开".equals(pb)) {
            pub = "301";
        } else if ("永久密码".equals(pb)) {
            pub = "302";
        } else if ("单次密码".equals(pb)) {
            pub = "303";
        }

        if ("无配音".equals(voice)) {
            vv = Constents.VoiceStyle_No;
        } else if ("一对一配音".equals(voice)) {
            vv = Constents.VoiceStyle_HAVE;
        }
        String summary = ed_introduce.getText().toString();
        ChangeStatusBean bean = new ChangeStatusBean(title, summary, category, vv,pub,secret);
        Map<String, ChangeStatusBean> map = new LinkedHashMap<>();
        map.put(intentNode.getNid(), bean);
        String string = new Gson().toJson(map);
        LogUtils.d("=====change==" + string);
        SendActtionTool.getJson(Constents.CHANGE_PPT_ONLINE, string, saveChange, this);
        // SendActtionTool.postJson(Constents.CHANGE_PPT_ONLINE,string,saveChange,this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcast);
    }

    @Override
    public void onSuccess(Object action, Object value) {
        LogUtils.d("onSuccess" + value.toString());
        switch (((String) action)) {
            case "getDetail":
                try {
                    detailBean = new Gson().fromJson(((JSONObject) value).getString("data"), DetailBean.class);
                    setView(detailBean);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case "saveChange":
                Toast.makeText(this, "修改状态成功", Toast.LENGTH_SHORT).show();
                break;
            case "saveAndEnter":
                Intent intent = new Intent(this, RecordingActivity.class);
                intent.putExtra("RecordingActivity", intentNode.getNid());
                startActivity(intent);

                break;
        }
    }

    private void setView(DetailBean detailBean) {
        if (detailBean.getCategoryValue() != null) {
            bt_category.setText(detailBean.getCategoryValue());
        }
        String voicestyle = detailBean.getVoiceStyleValue();
        if ("202".equals(voicestyle)) {
            bt_voice.setText("一对一配音");
            save_enter_ly.setVisibility(View.VISIBLE);
            enter_ly.setVisibility(View.VISIBLE);

        } else {
            bt_voice.setText("无配音");
            save_enter_ly.setVisibility(View.GONE);
            enter_ly.setVisibility(View.GONE);
        }
        ed_introduce.setText(detailBean.getSummary());
        ed_title.setText(detailBean.getTitle());
        if (detailBean.getAccessControlValue() != null) {
            String string = detailBean.getAccessControlValue();
            if ("301".equals(string)) {
                bt_publish.setText("公开");
                secretLayout.setVisibility(View.INVISIBLE);
                tv_secret_tip.setVisibility(View.INVISIBLE);
            } else {
                secretLayout.setVisibility(View.VISIBLE);
                tv_secret_tip.setVisibility(View.VISIBLE);
                String secret = detailBean.getSharePass();
                if (TextUtils.isEmpty(secret)) {
                    secret = "1234";
                }
                if ("302".equals(string)) {
                    bt_publish.setText("永久密码");
                } else if ("303".equals(string)) {
                    bt_publish.setText("单次密码");
                }
                ed_secret.setText(secret);

            }

        }


    }

    @Override
    public void onFaile(Object action, Object value) {
        LogUtils.d("onFaile" + value.toString());

    }

    @Override
    public void onException(Object action, Object value) {
        LogUtils.d("onException" + value.toString());
    }

    @Override
    public void onStart(Object action) {

    }

    @Override
    public void onFinish(Object action) {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    class ReceiveChangeBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra("分类") != null) {
                bt_category.setText(intent.getStringExtra("分类"));

            }
            if (intent.getStringExtra("发布方式") != null) {
                String string = intent.getStringExtra("发布方式");
                bt_publish.setText(string);

                if ("公开".equals(string)) {
                    secretLayout.setVisibility(View.INVISIBLE);
                    tv_secret_tip.setVisibility(View.INVISIBLE);
                } else {
                    secretLayout.setVisibility(View.VISIBLE);
                    tv_secret_tip.setVisibility(View.VISIBLE);
                }


            }
            if (intent.getStringExtra("voice") != null) {
                bt_voice.setText(intent.getStringExtra("voice"));
                if ("无配音".equals(intent.getStringExtra("voice"))) {
                    enter_ly.setVisibility(View.GONE);
                    save_enter_ly.setVisibility(View.GONE);
                } else {
                    enter_ly.setVisibility(View.VISIBLE);
                    save_enter_ly.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
