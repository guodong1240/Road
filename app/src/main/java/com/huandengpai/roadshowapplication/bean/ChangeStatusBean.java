package com.huandengpai.roadshowapplication.bean;

/**
 * Created by zx on 2017/1/4.
 */

public class ChangeStatusBean extends  BaseBean {

    private String title;
    private String summary;
    private String categoryValue;
    private String voiceStyle;
    private String accessControlValue;
    private String  sharePass;

    public ChangeStatusBean() {
    }

    public ChangeStatusBean(String title, String summary, String categoryValue, String voiceStyle, String accessControlValue, String sharePass) {
        this.title = title;
        this.summary = summary;
        this.categoryValue = categoryValue;
        this.voiceStyle = voiceStyle;
        this.accessControlValue = accessControlValue;
        this.sharePass = sharePass;
    }

    public String getAccessControlValue() {
        return accessControlValue;
    }

    public void setAccessControlValue(String accessControlValue) {
        this.accessControlValue = accessControlValue;
    }

    public String getSharePass() {
        return sharePass;
    }

    public void setSharePass(String sharePass) {
        this.sharePass = sharePass;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getCategoryValue() {
        return categoryValue;
    }

    public void setCategoryValue(String categoryValue) {
        this.categoryValue = categoryValue;
    }

    public String getVoiceStyle() {
        return voiceStyle;
    }

    public void setVoiceStyle(String voiceStyle) {
        this.voiceStyle = voiceStyle;
    }
}
