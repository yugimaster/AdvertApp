package com.grandartisans.advert.model.entity.res;

public class AppUpgradeData {
    int id;
    int iorder;
    int status;
    String updateTime;
    String name;
    String ident;
    String version;
    int androidVersion;
    int platform;
    String title;
    String description;
    int isforce;
    String submitTxt;
    String cancleTxt;
    String filePath;
    int fileSize;
    String fileMd5;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIorder() {
        return iorder;
    }

    public void setIorder(int iorder) {
        this.iorder = iorder;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdent() {
        return ident;
    }

    public void setIdent(String ident) {
        this.ident = ident;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getAndroidVersion() {
        return androidVersion;
    }

    public void setAndroidVersion(int androidVersion) {
        androidVersion = androidVersion;
    }

    public int getPlatform() {
        return platform;
    }

    public void setPlatform(int platform) {
        this.platform = platform;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getIsforce() {
        return isforce;
    }

    public void setIsforce(int isforce) {
        this.isforce = isforce;
    }

    public String getSubmitTxt() {
        return submitTxt;
    }

    public void setSubmitTxt(String submitTxt) {
        this.submitTxt = submitTxt;
    }

    public String getCancleTxt() {
        return cancleTxt;
    }

    public void setCancleTxt(String cancleTxt) {
        this.cancleTxt = cancleTxt;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }
}
