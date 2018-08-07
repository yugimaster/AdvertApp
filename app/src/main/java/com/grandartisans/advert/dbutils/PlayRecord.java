package com.grandartisans.advert.dbutils;


import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
        *
        * 数据库存储 表 的实体类
        * 在类名上加上@Table标签，标签里面的name就是以后生成数据库的表的表名。
        * 实体bean里面的属性，如果没有加上@Column，在生成表的时候，就不会在表里面加上该字段，反之亦然。
        */
@Table(name = "PlayRecord")
public class PlayRecord {
    //id
    @Column(name = "id", isId = true, autoGen = true)
    private long id;

    //模板ID
    @Column(name = "tpid")
    private long tpid;

    //广告位ID
    @Column(name = "apid")
    private long apid;

    //广告ID
    @Column(name = "adid")
    private long adid;

    //广告名称
    @Column(name = "name")
    private String  name;

    //开始时间
    @Column(name = "starttime")
    private long starttime;

    //结束时间
    @Column(name = "endtime")
    private long endtime;

    //播放次数
    @Column(name = "count")
    private int count;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTpid() {
        return tpid;
    }

    public void setTpid(long tpid) {
        this.tpid = tpid;
    }

    public long getApid() {
        return apid;
    }

    public void setApid(long apid) {
        this.apid = apid;
    }

    public long getAdid() {
        return adid;
    }

    public void setAdid(long adid) {
        this.adid = adid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getStarttime() {
        return starttime;
    }

    public void setStarttime(long starttime) {
        this.starttime = starttime;
    }

    public long getEndtime() {
        return endtime;
    }

    public void setEndtime(long endtime) {
        this.endtime = endtime;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
    @Override
    public String toString() {
        return "[id=" + getId() + ",tpid= " + getTpid()  +",apid= "+getApid() + ",adid = " + getAdid() + ",name=" + getName() + ",starttime=" + getStarttime()
                + ",endtime=" + getEndtime() + ",count=" + getCount() +"}\n";
    }

}