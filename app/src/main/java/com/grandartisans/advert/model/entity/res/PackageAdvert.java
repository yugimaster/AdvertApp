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
 * 创建日期：2018-05-14
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
public class PackageAdvert {
    private Long id;

    /**
     * 排序字段|2|3|1
     */
    private Long iorder;

    /**
     * 状态|1|3|0|0正常
     */
    private Integer status;

    /**
     * 增加者编号|2|3|0
     */
    private Long createBy;

    /**
     * 最后修改者编号|2|3|0
     */
    private Long updateBy;

    /**
     * 添加者|2|3|0
     */
    private String createUser;

    /**
     * 最后修改者|2|3|0
     */
    private String updateUser;

    /**
     * 备注字段1|2|3|0
     */
    private String remark1;

    /**
     * 备注字段2|2|3|0
     */
    private String remark2;

    /**
     * 增加时间|2|3|0
     */
    private Date createTime;

    /**
     * 最后修改时间|2|3|0
     */
    private Date updateTime;

    /**
     * 锁定标志|1|3|0|0正常 1锁定 2 删除
     */
    private Integer slock;

    /**
     * 广告包编号|1|1|1
     */
    private Long timeScheduleId;

    /**
     * 广告编号|1|1|1
     */
    private Long advertid;

    /**
     * 播放类型|1|1|1
     */
    private Integer playType;

    /**
     * 开始时间|1|1|1
     */
    private Date starttime;

    /**
     * 结束时间|1|1|1
     */
    private Date endtime;

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getCreateBy() {
        return createBy;
    }

    public void setCreateBy(Long createBy) {
        this.createBy = createBy;
    }

    public Long getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(Long updateBy) {
        this.updateBy = updateBy;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }

    public String getRemark1() {
        return remark1;
    }

    public void setRemark1(String remark1) {
        this.remark1 = remark1;
    }

    public String getRemark2() {
        return remark2;
    }

    public void setRemark2(String remark2) {
        this.remark2 = remark2;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getSlock() {
        return slock;
    }

    public void setSlock(Integer slock) {
        this.slock = slock;
    }

    public Long getTimeScheduleId() {
        return timeScheduleId;
    }

    public void setTimeScheduleId(Long timeScheduleId) {
        this.timeScheduleId = timeScheduleId;
    }

    public Long getAdvertid() {
        return advertid;
    }

    public void setAdvertid(Long advertid) {
        this.advertid = advertid;
    }

    public Integer getPlayType() {
        return playType;
    }

    public void setPlayType(Integer playType) {
        this.playType = playType;
    }

    public Date getStarttime() {
        return starttime;
    }

    public void setStarttime(Date starttime) {
        this.starttime = starttime;
    }

    public Date getEndtime() {
        return endtime;
    }

    public void setEndtime(Date endtime) {
        this.endtime = endtime;
    }
}
