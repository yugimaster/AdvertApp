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
import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2018年5月14日 下午8:01:59
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
public class TimeScheduleVo {
	
	private TimeSchedule timeSchedule;
	
	private List<AdvertVo> packageAdverts = new ArrayList<>();

	/**
	 * @return the timeSchedule
	 */
	public TimeSchedule getTimeSchedule() {
		return timeSchedule;
	}

	/**
	 * @param timeSchedule the timeSchedule to set
	 */
	public void setTimeSchedule(TimeSchedule timeSchedule) {
		this.timeSchedule = timeSchedule;
	}


	/**
	 * @return the packageAdverts
	 */
	public List<AdvertVo> getPackageAdverts() {
		return packageAdverts;
	}

	/**
	 * @param packageAdverts the packageAdverts to set
	 */
	public void setPackageAdverts(List<AdvertVo> packageAdverts) {
		this.packageAdverts = packageAdverts;
	}
}
