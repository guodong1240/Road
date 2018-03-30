package com.huandengpai.roadshowapplication.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.huandengpai.roadshowapplication.R;
import com.huandengpai.roadshowapplication.adapter.abslistview.CommonAdapter;
import com.huandengpai.roadshowapplication.adapter.abslistview.ViewHolder;
import com.huandengpai.roadshowapplication.maganer.DavikActivityManager;

import java.util.ArrayList;
import java.util.List;

/**
 * ppt制作——分类
 */
public class CategoryActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {


    private TextView title_tv;
    private RadioButton back_bt;
    private ListView listView;
    private CommonAdapter<String> mAdapter;
    private List<String> list;

    private int choosePostition = -1;
    private Intent intent;
    private String titleStr;
    private String valueStr;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        DavikActivityManager.getScreenManager().pushActivity(this);
    }

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_category);
        intent = getIntent();
        if (intent!=null){
            String string=intent.getStringExtra("name");
            if (!TextUtils.isEmpty(string)){
                titleStr=string;
            }else {
                titleStr="基本信息";
            }
            string=intent.getStringExtra("value");
            if (!TextUtils.isEmpty(string)){
                valueStr=string;
            }else {
                valueStr="";
            }
        }

    }


    @Override
    protected void findViewById() {
        title_tv = ((TextView) findViewById(R.id.title_tv));
        if (titleStr!=null){
         title_tv.setText(titleStr);
        }else {
            title_tv.setText("基本信息");
        }

        back_bt = ((RadioButton) findViewById(R.id.back_bt));
        back_bt.setText("确定");
        back_bt.setOnClickListener(this);

        listView = ((ListView) findViewById(R.id.catgory_listview));
        list = new ArrayList<>();

        initData();
    }

    private void initData() {
        if ("分类".equals(titleStr)){
             list.clear();
             list.add("金融财经");
             list.add("商业计划");
             list.add("产品推介");
             list.add("其他");


        }else if ("发布方式".equals(titleStr)){
            list.clear();
            list.add("公开");
            list.add("永久密码");
            list.add("单次密码");
        }

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(valueStr)){
                choosePostition=i;
            }
        }


        mAdapter = new CommonAdapter<String>(getApplicationContext(), R.layout.category_item, list) {
            @Override
            protected void convert(ViewHolder viewHolder, String item, final int position) {
                final TextView view_tv = (TextView) viewHolder.getView(R.id.content_tv);
                viewHolder.setText(R.id.content_tv, item);
                if (choosePostition == position) {
                    view_tv.setTextColor(Color.BLUE);
                } else {
                    view_tv.setTextColor(Color.BLACK);
                }

            }
        };
        listView.setOnItemClickListener(this);
        listView.setAdapter(mAdapter);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_bt:
                finish();
                break;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        choosePostition = position;
        mAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onPause() {
        super.onPause();
        Intent intent = new Intent();
        intent.setAction("MakepptActivity");
        if (choosePostition != -1) {
            intent.putExtra(titleStr, list.get(choosePostition));
            sendBroadcast(intent);
        }

    }
}
