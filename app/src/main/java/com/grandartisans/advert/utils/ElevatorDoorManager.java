package com.grandartisans.advert.utils;

import android.util.Log;
import com.grandartisans.advert.interfaces.ElevatorDoorEventListener;

public class ElevatorDoorManager {
    private final String TAG = "ElevatorDoorManager";
    private SerialPortUtils serialPortUtils = null;
    private ElevatorDoorEventListener mElevatorDoorEventListener=null;
    private int distance = 0;
    private int strength = 0;
    private int threshold_distance = 0;
    private int lastdistance = 0;

    static final int DOOR_STATE_INIT  = 0;
    static final int DOOR_STATE_OPENED  = 1;
    static final int DOOR_STATE_CLOSED = 2;

    private int mDoorStatus = DOOR_STATE_INIT;
    public ElevatorDoorManager(int defaultDistance){
        threshold_distance = defaultDistance;
        setDoorStatus(DOOR_STATE_INIT);
        initserialPort();
    }
    public void setDefaultDistance(int defaultDistance){
        threshold_distance = defaultDistance;
    }
    public int getDoorStatus(){
        return mDoorStatus;
    }
    public boolean isDoorOpened(){
        if(mDoorStatus == DOOR_STATE_OPENED) return true;
        else return false;
    }
    private void setDoorStatus(int  status){
        mDoorStatus = status;
    }
    public void  registerListener(ElevatorDoorEventListener eventListener){
        if(eventListener!=null)
            mElevatorDoorEventListener = eventListener;
    }
    public void openSerialPort(){
        if (serialPortUtils != null) {
            if (serialPortUtils.openSerialPort("/dev/" + CommonUtil.getTFMiniDevice()) == null){

            }
        }
    }
    public void closeSeriaPort(){
        if (serialPortUtils != null) {
            serialPortUtils.closeSerialPort();
        }
    }
    private void initserialPort()
    {
        serialPortUtils = new SerialPortUtils();
        //串口数据监听事件
        serialPortUtils.setOnDataReceiveListener(new SerialPortUtils.OnDataReceiveListener() {
            @Override
            public void onDataReceive(byte[] buffer, int size) {
                //Log.i(TAG, "进入数据监听事件中。。。" + new String(buffer));
                if(CommonUtil.getTFMiniEnabled()==0) return;
                distance = dealWithData(buffer,size);
                if(serialPortUtils.serialPortStatus) {
                    //handler.post(ReadThread);
                    if (lastdistance != distance && Math.abs(distance-lastdistance)>5) {
                        if (threshold_distance > 0 && (distance - threshold_distance > 10)) {
                            if(getDoorStatus()!=DOOR_STATE_OPENED) {
                                if (mElevatorDoorEventListener != null)
                                    mElevatorDoorEventListener.onElevatorDoorOpen();
                                setDoorStatus(DOOR_STATE_OPENED);
                            }
                        }else{
                            if(getDoorStatus()!=DOOR_STATE_CLOSED) {
                                if (mElevatorDoorEventListener != null)
                                    mElevatorDoorEventListener.onElevatorDoorClose();
                                setDoorStatus(DOOR_STATE_CLOSED);
                            }
                        }
                        lastdistance = distance;
                    }
                }
            }
        });

    }

    private int  dealWithData(byte[] buffer, int size)
    {
        int distance;
        if(size!=9) {
            Log.i(TAG,"receiv data error ,size= "+ size + " is not 9 bytes");
            distance = 0;
            return distance;
        }
        if(buffer[0] != 0x59 || buffer[1] !=0x59) {
            Log.i(TAG,"receiv data error ,head data0 = "+ buffer[0] +"data1 = "+buffer[1]);
            distance = 0;
            return distance;
        }
        int low = buffer[2]&0xff;
        int high = buffer[3]&0xff;
        distance = low + high*256;
        low = buffer[4]&0xff;
        high = buffer[5]&0xff;
        strength = low + high*256;
		/*
		Log.i(TAG,"strength = " + strength + "dist is " + distance);
		for(int i=0;i<size;i++) {
			Log.i(TAG,""+ (buffer[i]&0xff));
		}
		*/
		return distance;
    }
}
