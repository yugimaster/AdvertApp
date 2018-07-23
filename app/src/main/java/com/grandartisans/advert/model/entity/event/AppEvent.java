package com.grandartisans.advert.model.entity.event;

public class AppEvent<T> {
    private T data;
    private int msg;
    public final static int ADVERT_LIST_UPDATE_EVENT = 0;
    public final static int ADVERT_DOWNLOAD_FINISHED_EVENT = 1;
    public final static int POWER_SET_ALARM_EVENT = 2;
    public final static int ADVERT_LIST_DOWNLOAD_FINISHED_EVENT = 3;
    public final static int SET_POWER_OFF = 4;
    public final static int SET_POWER_ON = 5;


    public AppEvent(){

    }

    public AppEvent(int msg, T data){
        this.msg = msg;
        this.data = data;
    }

    public int getMessage() {
        return msg;
    }

    public <T>T getData(){
        if(data == null){

        }
        return (T)data;
    }
}
