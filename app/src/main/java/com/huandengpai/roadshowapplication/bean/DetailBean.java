package com.huandengpai.roadshowapplication.bean;

/**
 * Created by zx on 2017/1/4.
 *
 * 保存 post：
 accessControlValue
 （发布方式：301=公开，302=永久密码，303=单次密码），
 sharePass（密码）
 获取 get：
 accessControlValue
 （发布方式：301=公开，302=永久密码，303=单次密码），
 sharePass（密码）

 */

public class DetailBean extends BaseBean {
    private String title;
    private String summary;
    private String category;
    private String categoryValue;
    private String coverStyle;
    private String voiceStyle;
    private String voiceStyleValue;
    private String pptStatus;
    private String accessControl;
    private String voice_file;
    private String source_file_nid;
    private String accessControlValue;
    private String  sharePass;

    public DetailBean() {
    }

    public DetailBean(String title, String summary, String category, String categoryValue, String coverStyle, String voiceStyle, String voiceStyleValue, String pptStatus, String accessControl, String voice_file, String source_file_nid, String accessControlValue, String sharePass) {
        this.title = title;
        this.summary = summary;
        this.category = category;
        this.categoryValue = categoryValue;
        this.coverStyle = coverStyle;
        this.voiceStyle = voiceStyle;
        this.voiceStyleValue = voiceStyleValue;
        this.pptStatus = pptStatus;
        this.accessControl = accessControl;
        this.voice_file = voice_file;
        this.source_file_nid = source_file_nid;
        this.accessControlValue = accessControlValue;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategoryValue() {
        return categoryValue;
    }

    public void setCategoryValue(String categoryValue) {
        this.categoryValue = categoryValue;
    }

    public String getCoverStyle() {
        return coverStyle;
    }

    public void setCoverStyle(String coverStyle) {
        this.coverStyle = coverStyle;
    }

    public String getVoiceStyle() {
        return voiceStyle;
    }

    public void setVoiceStyle(String voiceStyle) {
        this.voiceStyle = voiceStyle;
    }

    public String getVoiceStyleValue() {
        return voiceStyleValue;
    }

    public void setVoiceStyleValue(String voiceStyleValue) {
        this.voiceStyleValue = voiceStyleValue;
    }

    public String getPptStatus() {
        return pptStatus;
    }

    public void setPptStatus(String pptStatus) {
        this.pptStatus = pptStatus;
    }

    public String getAccessControl() {
        return accessControl;
    }

    public void setAccessControl(String accessControl) {
        this.accessControl = accessControl;
    }

    public String getVoice_file() {
        return voice_file;
    }

    public void setVoice_file(String voice_file) {
        this.voice_file = voice_file;
    }

    public String getSource_file_nid() {
        return source_file_nid;
    }

    public void setSource_file_nid(String source_file_nid) {
        this.source_file_nid = source_file_nid;
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
}
