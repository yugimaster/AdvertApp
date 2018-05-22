/**
* <p> * Title: 君匠共享广告管理系统*</p>
* <p> * Description:  君匠共享广告管理系统* </p>
* <p> * Copyright: Copyright (c) 2012-2018* </p>
* <p> * Company: 苏州明翔信息科技有限公司 * </p>
* @author 叶明（开发）
* @version 1.0
*/
package com.grandartisans.advert.model.entity.res;

import java.io.Serializable;
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
public class TemplateRegion  {
    private Long id;

    /**
     * 状态|1|3|0|0正常
     */
    private int status;

    /**
     * 隶属模板编号|2|3|0
     */
    private Long templateid;
    /**
     * 隶属模板名称|2|3|0
     */
    private String templateName;

	/**
     * 位置信息|1|1|1
     */
    private int regionIndex;

    /**
     * 名称|1|1|1
     */
    private String name;

    /**
     * 宽度|1|1|1
     */
    private int width;

    /**
     * 高度|1|1|1
     */
    private int height;

    /**
     * 对比率编号|1|1|1
     */
    private Long rateid;

    /**
     * 对比率|1|1|1
     */
    private String rate;

    /**
     * 开始坐标|1|1|1
     */
    private String location;

    private String ident;

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

    public Long getTemplateid() {
        return templateid;
    }

    public void setTemplateid(Long templateid) {
        this.templateid = templateid;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public Integer getRegionIndex() {
        return regionIndex;
    }

    public void setRegionIndex(Integer regionIndex) {
        this.regionIndex = regionIndex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Long getRateid() {
        return rateid;
    }

    public void setRateid(Long rateid) {
        this.rateid = rateid;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getIdent() {
        return ident;
    }

    public void setIdent(String ident) {
        this.ident = ident;
    }
}
