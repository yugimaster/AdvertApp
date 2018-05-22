/**
* <p> * Title: 君匠共享广告管理系统*</p>
* <p> * Description:  君匠共享广告管理系统* </p>
* <p> * Copyright: Copyright (c) 2012-2018* </p>
* <p> * Company: 苏州明翔信息科技有限公司 * </p>
* @author 叶明（开发）
* @version 1.0
*/
package com.grandartisans.advert.model.entity.res;

import java.util.Date;


/**
 * 创建日期：2018-05-02
 * 开发者：叶明(email:guming123416@163.com,QQ:47043760)
 * 修改者：
 * 修改时间：
 * 程序作用：
 * 1、
 * 2、
 * 修改说明：
 * 1、
 * 2、
 * 版本：@version 1.0
 * @author  叶明
 */
public class AdvertFile {
    private Long id;

    /**
     * 状态|1|3|0|0正常
     */
    private Integer status;

    /**
     * 广告编号|2|3|0
     */
    private Long advertid;

    /**
     * 名称|1|1|1
     */
    private String name;

    /**
     * 文件路径|1|1|1
     */
    private String filePath;

    /**
     * 文件大小|1|1|1
     */
    private Long fileSize;

    /**
     * 文件md5|1|1|1
     */
    private String fileMd5;

    /**
     * 视频时长|1|1|1|单位 秒
     */
    private Integer videoDuration;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getAdvertid() {
        return advertid;
    }

    public void setAdvertid(Long advertid) {
        this.advertid = advertid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public Integer getVideoDuration() {
        return videoDuration;
    }

    public void setVideoDuration(Integer videoDuration) {
        this.videoDuration = videoDuration;
    }
}
