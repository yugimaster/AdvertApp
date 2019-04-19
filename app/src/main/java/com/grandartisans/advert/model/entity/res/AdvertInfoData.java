package com.grandartisans.advert.model.entity.res;

public class AdvertInfoData<T> {
    private Long id;
    private Long iorder;
    private Long status;
    private Long createBy;
    private T updateBy;
    private T remark1;
    private T remark2;
    private String createTime;
    private String updateTime;
    private Long slock;

    /**
     * 所属ID
     */
    private Long groupId;

    /**
     * 名称
     */
    private String name;

    /**
     * 类型 1小区 2模板
     */
    private Long type;

    /**
     * 时间|0否1是
     */
    private Long vTime;

    /**
     * 天气|0否1是
     */
    private Long weather;

    /**
     * 背景色
     */
    private String backColor;

    /**
     * 字体颜色
     */
    private String fontColor;

    /**
     * 字体大小
     */
    private Long fontSize;

    /**
     * 滚动速度
     */
    private Long velocity;

    /**
     * 文字信息
     */
    private String writing;

    private Long ownerId;
    private Long vtime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIorder() {
        return iorder;
    }

    public void setIorder(Long iorder) {
        this.iorder = iorder;
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public Long getCreateBy() {
        return createBy;
    }

    public void setCreateBy(Long createBy) {
        this.createBy = createBy;
    }

    public T getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(T updateBy) {
        this.updateBy = updateBy;
    }

    public T getRemark1() {
        return remark1;
    }

    public void setRemark1(T remark1) {
        this.remark1 = remark1;
    }

    public T getRemark2() {
        return remark2;
    }

    public void setRemark2(T remark2) {
        this.remark2 = remark2;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public Long getSlock() {
        return slock;
    }

    public void setSlock(Long slock) {
        this.slock = slock;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getType() {
        return type;
    }

    public void setType(Long type) {
        this.type = type;
    }

    public Long getvTime() {
        return vTime;
    }

    public void setvTime(Long vTime) {
        this.vTime = vTime;
    }

    public Long getWeather() {
        return weather;
    }

    public void setWeather(Long weather) {
        this.weather = weather;
    }

    public String getBackColor() {
        return backColor;
    }

    public void setBackColor(String backColor) {
        this.backColor = backColor;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public Long getFontSize() {
        return fontSize;
    }

    public void setFontSize(Long fontSize) {
        this.fontSize = fontSize;
    }

    public Long getVelocity() {
        return velocity;
    }

    public void setVelocity(Long velocity) {
        this.velocity = velocity;
    }

    public String getWriting() {
        return writing;
    }

    public void setWriting(String writing) {
        this.writing = writing;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getVtime() {
        return vtime;
    }

    public void setVtime(Long vtime) {
        this.vtime = vtime;
    }
}
