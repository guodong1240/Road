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
 * ppt制作配音方案
 */
public class MakeVoiceActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private List<String> list;
    private CommonAdapter<String> mAdapter;
    private ListView listview;
    private TextView title_tv;
    private RadioButton back_bt;

    private int choosePostion = -1;

    private Intent intent;
    private String valueStr;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        DavikActivityManager.getScreenManager().pushActivity(this);
    }

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_make_voice);
        intent=getIntent();
        if (intent!=null){
            String string=intent.getStringExtra("value");
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
        title_tv.setText("配音方案");
        back_bt = ((RadioButton) findViewById(R.id.back_bt));
        back_bt.setText("确定");
        back_bt.setOnClickListener(this);

        listview = ((ListView) findViewById(R.id.listview));
        list = new ArrayList<>();
        initDate();
    }

    private void initDate() {
        list.add("无配音");
        list.add("一对一配音");

        if ("无配音".equals(valueStr)){
            choosePostion=0;
        }else if ("一对一配音".equals(valueStr)){
            choosePostion=1;
        }

        mAdapter = new CommonAdapter<String>(getApplicationContext(), R.layout.category_item, list) {
            @Override
            protected void convert(ViewHolder viewHolder, String item, int position) {
                TextView content_tv = (TextView) viewHolder.getView(R.id.content_tv);
                content_tv.setText(item);
                if (choosePostion == position) {
                    content_tv.setTextColor(Color.BLUE);
                } else {
                    content_tv.setTextColor(Color.BLACK);
                }
            }
        };
        listview.setAdapter(mAdapter);
        listview.setOnItemClickListener(this);

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
    protected void onPause() {
        super.onPause();
        Intent intent = new Intent();
        intent.setAction("MakepptActivity");
        if (choosePostion != -1) {
            intent.putExtra("voice", list.get(choosePostion));
            sendBroadcast(intent);

        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        choosePostion = i;
        mAdapter.notifyDataSetChanged();
    }
}
