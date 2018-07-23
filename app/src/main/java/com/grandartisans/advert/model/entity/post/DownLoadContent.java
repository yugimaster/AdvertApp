package com.grandartisans.advert.model.entity.post;

public class DownLoadContent {
    Long advertId;
    String  url ;
    int status ;//1,等待下载，2，下载中，3暂停中，4下载完成，5下载失败
    float percent;//下载比例
    long fileSize ; //文件总大小

    public Long getAdvertId() {
        return advertId;
    }

    public void setAdvertId(Long advertId) {
        this.advertId = advertId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public float getPercent() {
        return percent;
    }

    public void setPercent(float percent) {
        this.percent = percent;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
