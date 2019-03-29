/*
 * Copyright (C) 2017  即时通讯网(52im.net) & Jack Jiang.
 * The MobileIMSDK_X (MobileIMSDK v3.x) Project. 
 * All rights reserved.
 * 
 * > Github地址: https://github.com/JackJiang2011/MobileIMSDK
 * > 文档地址: http://www.52im.net/forum-89-1.html
 * > 即时通讯技术社区：http://www.52im.net/
 * > 即时通讯技术交流群：320837163 (http://www.52im.net/topic-qqgroup.html)
 *  
 * "即时通讯网(52im.net) - 即时通讯开发者社区!" 推荐开源工程。
 * 
 * MessageQoSEventImpl.java at 2017-5-1 21:08:44, code by Jack Jiang.
 * You can contact author with jack.jiang@52im.net or jb2011@163.com.
 */
package com.grandartisans.advert.sdk;

import java.util.ArrayList;

import net.openmob.mobileimsdk.server.protocal.Protocal;
import android.util.Log;

import com.grandartisans.advert.sdk.kernalcode.event.MessageQoSEvent;

/**
 * 消息送达相关事件（由QoS机制通知上来的）在此MessageQoSEvent子类中实现即可。
 * 
 * @author Jack Jiang, 20170501
 * @version.1.1
 */
public class MessageQoSEventImpl implements MessageQoSEvent
{
	private final static String TAG = MessageQoSEventImpl.class.getSimpleName();

	@Override
	public void messagesLost(ArrayList<Protocal> lostMessages)
	{
		Log.d(TAG, "【DEBUG_UI】收到系统的未实时送达事件通知，当前共有"+lostMessages.size()+"个包QoS保证机制结束，判定为【无法实时送达】！");
	}

	@Override
	public void messagesBeReceived(String theFingerPrint)
	{
		if(theFingerPrint != null)
		{
			Log.d(TAG, "【DEBUG_UI】收到对方已收到消息事件的通知，fp="+theFingerPrint);
		}
	}
}
