package com.huandengpai.roadshowapplication.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.huandengpai.roadshowapplication.R;
import com.huandengpai.roadshowapplication.adapter.MainFragmentAdapter;
import com.huandengpai.roadshowapplication.fragment.HelpFragment;
import com.huandengpai.roadshowapplication.fragment.LocalFragment;
import com.huandengpai.roadshowapplication.fragment.PListFragment;
import com.huandengpai.roadshowapplication.item.Constents;
import com.huandengpai.roadshowapplication.maganer.DBManager;
import com.huandengpai.roadshowapplication.maganer.DavikActivityManager;
import com.huandengpai.roadshowapplication.utils.LogUtils;
import com.huandengpai.roadshowapplication.utils.Utils;
import com.nineoldandroids.view.ViewHelper;

import java.util.ArrayList;
import java.util.List;

import db.bean.LocalPPT;

public class VoiceActivity extends BaseActivity implements View.OnClickListener, LocalFragment.PostSuccess {
    String str_uid;
    private ViewPager mViewPager;
    private List<Fragment> fragmentList;
    private TextView textview_localfile, textview_pptlist, textview_help;
    private View tab_line;
    //屏幕宽度
    private int screenWidth;
    private RadioButton back_bt;
    static PListFragment pListFragment;

    private String mFileName = "";
    private String mFilePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DavikActivityManager.getScreenManager().pushActivity(this);
        Intent intent = getIntent();
        if (intent != null) {
            if (!TextUtils.isEmpty(intent.getStringExtra(Constents.UID))) {
                str_uid = intent.getStringExtra(Constents.UID);
            }
            getPPTdeliver();
        }
        initView();
        initListener();
        initTabLineWidth();
    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_voice);
    }

    //得到从其他应用跳转进来传递的ppt
    private void getPPTdeliver() {
        Uri uri = getIntent().getData();
        if (uri != null) {
            String str = Uri.decode(uri.getEncodedPath());
            if (str != null) {
                String[] strings = str.split("/");
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < strings.length; i++) {
                    sb.append(strings[i] + "/");
                }
                mFileName = strings[strings.length - 1];
                mFilePath = sb.toString();
                String timeStr = Utils.getCurrentTime();
                LogUtils.d(mFileName + "======" + mFilePath + "===" + timeStr);
                //将得到的ppt保存到数据库，方便之后的操作
                DBManager manager = DBManager.getInstance(getApplicationContext());
                LocalPPT ppt = new LocalPPT();
                ppt.setTitle(mFileName);
                ppt.setPath(mFilePath);
                ppt.setTime(timeStr);
                manager.insertUser(ppt);

                List<LocalPPT> localPPTs = new ArrayList<>();
                localPPTs = manager.queryUserList();
                LogUtils.d(localPPTs.size() + "==============");
            }
        }

    }

    private void initView() {
        textview_localfile = (TextView) findViewById(R.id.textview_loaclfile);
        textview_pptlist = (TextView) findViewById(R.id.textview_pptlist);
        textview_help = (TextView) findViewById(R.id.textview_help);
        tab_line = (View) findViewById(R.id.tab_line);
        back_bt = ((RadioButton) findViewById(R.id.back_bt));

        mViewPager = ((ViewPager) findViewById(R.id.my_pager));
        fragmentList = new ArrayList<>();
        pListFragment = PListFragment.newInstance(str_uid, "");
        fragmentList.add(LocalFragment.newInstance("", ""));
        fragmentList.add(pListFragment);
        fragmentList.add(HelpFragment.newInstance("", ""));

        // 初始化标签的高亮状态
        updateTabs(0);

        MainFragmentAdapter mainFragmentAdapter = new MainFragmentAdapter(getSupportFragmentManager(), fragmentList);
        Log.i("PAI", fragmentList.size() + "");
        mViewPager.setOffscreenPageLimit(0);
        mViewPager.setAdapter(mainFragmentAdapter);
        mViewPager.setCurrentItem(0);
        mViewPager.setOnPageChangeListener(new OnVideoPageChangeListener());

//        //TODO 测试接入客服
//        Intent intent = new Intent();
//        intent.setClass(this, KF5ChatActivity.class);
//        startActivity(intent);
    }


    private void initListener() {
        textview_localfile.setOnClickListener(this);
        textview_pptlist.setOnClickListener(this);
        textview_help.setOnClickListener(this);
        back_bt.setOnClickListener(this);
    }

    private void initTabLineWidth() {
        DisplayMetrics dpMetrics = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay()
                .getMetrics(dpMetrics);
        screenWidth = dpMetrics.widthPixels;
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tab_line
                .getLayoutParams();
        lp.width = screenWidth / 3;
        tab_line.setLayoutParams(lp);
    }

    /**
     * 根据选中的position修改全部tab的高亮状态
     */
    private void updateTabs(int position) {
        updateTab(position, 0, textview_localfile);
        updateTab(position, 1, textview_pptlist);
        updateTab(position, 2, textview_help);
    }

    /**
     * 根据tabPosition是否等于position，修改tab的颜色和大小
     */
    private void updateTab(int position, int tabPosition, TextView tab) {
        if (position == tabPosition) {
            tab.setTextColor(0xff009ac9);
            com.nineoldandroids.view.ViewPropertyAnimator.animate(tab).scaleX(1.2f).scaleY(1.2f);
        } else {
            tab.setTextColor(0xffa4a4a4);
            com.nineoldandroids.view.ViewPropertyAnimator.animate(tab).scaleX(1.0f).scaleY(1.0f);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.textview_loaclfile:
                mViewPager.setCurrentItem(0);
                break;
            case R.id.textview_pptlist:
                mViewPager.setCurrentItem(1);
                break;
            case R.id.textview_help:
                mViewPager.setCurrentItem(2);
                break;
            case R.id.back_bt:
                finish();
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        finish();
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    //LocalFragment向PListFragment传值
    @Override
    public void onPostSuccess(boolean b) {
        if (b) {
            pListFragment.updateList();
        }

    }


    private class OnVideoPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        /** 当pager滑动的时候会回调此方法 */
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//            logE("OnVideoPageChangeListener.onPageScrolled: position="+position+";Offset="+positionOffset+";Pixels"+positionOffsetPixels);
            // 移动距离 = 起始位置 + 偏移大小
            // 起始位置 = position * 指示器宽度
            // 偏移大小 = 手指划过屏幕的百分比 * 指示器宽度
            int offsetX = (int) (positionOffset * tab_line.getWidth());
//            int offsetX = positionOffsetPixels / fragments.size();
            int startX = position * tab_line.getWidth();
            int translationX = startX + offsetX;

            ViewHelper.setTranslationX(tab_line, translationX);
        }

        @Override
        /** 当pager被选中的时候回调此方法 */
        public void onPageSelected(int position) {
            // 更新选中tab的颜色和大小
            updateTabs(position);
        }

        @Override
        /** 当pager的滑动状态发生变更时被回调 */
        public void onPageScrollStateChanged(int state) {

        }
    }


}
