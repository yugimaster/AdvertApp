/**
* <p> * Title: 叶明开发的代码系统*</p>
* <p> * Description: * </p>
* <p> * Copyright: Copyright (c) 2012-2018* </p>
* <p> * Company: java工作者 * </p>
* @author 叶明（开发）
* @version 4.0.2
*/
package com.grandartisans.advert.model.entity.res;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2018年5月14日 下午6:19:56
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
public class AdvertPositionVo {

	private List<DateScheduleVo> dateScheduleVos = new ArrayList<>();
	private AdvertPosition advertPosition;
	//private T advertPosition;

	/**
	 * @return the advertPosition
	 */
	public AdvertPosition getadvertPosition() {
		return advertPosition;
	}

	/**
	 * @param advertPosition the advertPosition to set
	 */
	public void setadvertPosition(AdvertPosition advertPosition) {
		advertPosition = advertPosition;
	}

	/**
	 * @return the dateScheduleVos
	 */
	public  List<DateScheduleVo> getDateScheduleVos() {
		return dateScheduleVos;
	}

	/**
	 * @param dateScheduleVos the dateScheduleVos to set
	 */
	public void setDateScheduleVos(  List<DateScheduleVo> dateScheduleVos) {
		this.dateScheduleVos = dateScheduleVos;
	}

}
