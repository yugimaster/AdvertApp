
package com.grandartisans.advert.sdk.kernalcode.event;

public interface ChatTransDataEvent
{
	public void onTransBuffer(String fingerPrintOfProtocal, String userid, String dataContent, int typeu);
	public void onErrorResponse(int errorCode, String errorMsg);
}