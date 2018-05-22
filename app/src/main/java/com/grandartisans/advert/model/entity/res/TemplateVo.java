/**
* <p> * Title: 叶明开发的代码系统*</p>
* <p> * Description: * </p>
* <p> * Copyright: * </p>
* <p> * Company:  * </p>
* @author 叶明（开发）
* @version 1.0
*/
package com.grandartisans.advert.model.entity.res;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * 创建日期：2018年5月2日 上午7:12:52
 * 开发者：叶明(email:guming123416@163.com,QQ:47043760)
 * 修改者：
 * 修改时间：
 * 程序作用：
 * 1、
 * 2、
 * 修改说明：
 * 1、
 * 2、
 * 版本：
 * @author 叶明
 */
@SuppressWarnings("serial")
public class TemplateVo {
	
	private Template template;
	
	private List<TemplateRegion> regionList = new ArrayList<>();

	/**
	 * @return the template
	 */
	public Template getTemplate() {
		return template;
	}

	/**
	 * @param template the template to set
	 */
	public void setTemplate(Template template) {
		this.template = template;
	}

	/**
	 * @return the regionList
	 */
	public List<TemplateRegion> getRegionList() {
		return regionList;
	}

	/**
	 * @param regionList the regionList to set
	 */
	public void setRegionList(List<TemplateRegion> regionList) {
		this.regionList = regionList;
	}
	

	
}
