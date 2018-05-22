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
 * 创建日期：2018-05-11
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

public class Template {

    private Long id;

    /**
     * 状态|1|3|0|0正常
     */
    private int status;

    /**
     * 名称|1|1|1
     */
    private String name;

    /**
     * 唯一标识|2|1|1
     */
    private String ident;

    /**
     * 位置数量|1|1|1
     */
    private int locationCount;

    /**
     * 分辨率编号|1|1|1
     */
    private Long resolutionid;

    /**

     * 分辨率|1|1|1
     */
    private String resolution;

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
    public Integer getLocationCount() {
        return locationCount;
    }

    public void setLocationCount(Integer locationCount) {
        this.locationCount = locationCount;
    }
    public Long getResolutionid() {
        return resolutionid;
    }

    public void setResolutionid(Long resolutionid) {
        this.resolutionid = resolutionid;
    }
    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

}
