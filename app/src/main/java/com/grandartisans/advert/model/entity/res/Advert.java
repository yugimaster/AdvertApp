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
public class Advert {
    private Long id;

    /**
     * 状态|1|3|0|0正常
     */
    private Integer status;

    /**
     * 名称|1|1|1
     */
    private String name;

    /**
     * 唯一标识|2|1|1
     */
    private String ident;

    /**
     * 广告类型|1|1|1|1 图片 2 视频 3 滚动图片 4 文字
     */
    private Long vtype;

    /**
     * 对比率|1|1|1
     */
    private String rate;

    /**
     * 对比率编号|1|1|1
     */
    private Long rateid;

    /**
     * 广告尺寸|1|1|1
     */
    private String advertSize;

    /**
     * 规格|1|1|1
     */
    private String standard;

    /**
     * 审批编号|1|1|1
     */
    private String auditNumber;

    /**
     * 文件数量|1|1|1
     */
    private Integer fileSize;

    /**
     * 播放类型|1|1|1
     */
    private Long playType;

    /**
     * 描述|1|1|1
     */
    private String description;

    private String ext_rateName;

    private String ext_playTypeName;

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

    public Long getVtype() {
        return vtype;
    }

    public void setVtype(Long vtype) {
        this.vtype = vtype;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public Long getRateid() {
        return rateid;
    }

    public void setRateid(Long rateid) {
        this.rateid = rateid;
    }

    public String getAdvertSize() {
        return advertSize;
    }

    public void setAdvertSize(String advertSize) {
        this.advertSize = advertSize;
    }

    public String getStandard() {
        return standard;
    }

    public void setStandard(String standard) {
        this.standard = standard;
    }

    public String getAuditNumber() {
        return auditNumber;
    }

    public void setAuditNumber(String auditNumber) {
        this.auditNumber = auditNumber;
    }

    public Integer getFileSize() {
        return fileSize;
    }

    public void setFileSize(Integer fileSize) {
        this.fileSize = fileSize;
    }

    public Long getPlayType() {
        return playType;
    }

    public void setPlayType(Long playType) {
        this.playType = playType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExt_rateName() {
        return ext_rateName;
    }

    public void setExt_rateName(String ext_rateName) {
        this.ext_rateName = ext_rateName;
    }

    public String getExt_playTypeName() {
        return ext_playTypeName;
    }

    public void setExt_playTypeName(String ext_playTypeName) {
        this.ext_playTypeName = ext_playTypeName;
    }
}
