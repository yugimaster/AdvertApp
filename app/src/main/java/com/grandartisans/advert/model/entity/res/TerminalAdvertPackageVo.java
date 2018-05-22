/**
* <p> * Title: 叶明开发的代码系统*</p>
* <p> * Description: * </p>
* <p> * Copyright: Copyright (c) 2012-2018* </p>
* <p> * Company: java工作者 * </p>
* @author 叶明（开发）
* @version 4.0.2
*/
package com.grandartisans.advert.model.entity.res;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 创建日期：2018年5月2日 上午7:09:20
 * 开发者：叶明(email:guming123416@163.com,QQ:47043760)
 * 修改者：
 * 修改时间：
 * 程序作用：
 * 1、
 * 2、
 * 修改说明：
 * 1、
 * 2、
 * 版本：@version 4.0.2
 * @author 叶明
 */
@SuppressWarnings("serial")
public class TerminalAdvertPackageVo {
	
	
    private String deviceClientid;
	
	private TemplateVo template;
	/**
	 * 模板上区域和排期的对应关系，key为区域id，value为广告位
	 */
    private Map<String,Long> relationMap = new HashMap<>();
    /**
     * 排期上信息，key为排期编号，value为广告排期
     */
    private Map<Long, AdvertPositionVo> advertPositionMap = new HashMap<>();

	/**
	 * @return the deviceClientid
	 */
	public String getDeviceClientid() {
		return deviceClientid;
	}

	/**
	 * @param deviceClientid the deviceClientid to set
	 */
	public void setDeviceClientid(String deviceClientid) {
		this.deviceClientid = deviceClientid;
	}

	/**
	 * @return the template
	 */
	public TemplateVo getTemplate() {
		return template;
	}

	/**
	 * @param template the template to set
	 */
	public void setTemplate(TemplateVo template) {
		this.template = template;
	}

	/**
	 * @return the relationMap
	 */
	public Map<String, Long> getRelationMap() {
		return relationMap;
	}

	/**
	 * @param relationMap the relationMap to set
	 */
	public void setRelationMap(Map<String, Long> relationMap) {
		this.relationMap = relationMap;
	}

	/**
	 * @return the advertPositionMap
	 */
	public Map<Long, AdvertPositionVo> getAdvertPositionMap() {
		return advertPositionMap;
	}

	/**
	 * @param advertPositionMap the advertPositionMap to set
	 */
	public void setAdvertPositionMap(Map<Long, AdvertPositionVo> advertPositionMap) {
		this.advertPositionMap = advertPositionMap;
	}

	
    
    
}
