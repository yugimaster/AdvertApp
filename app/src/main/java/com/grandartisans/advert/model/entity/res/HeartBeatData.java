package com.grandartisans.advert.model.entity.res;

import java.util.ArrayList;
import java.util.List;

public class HeartBeatData<T> {
    private String eventID;
    private T eventData;

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public T getEventData() {
        return eventData;
    }

    public void setEventData(T eventData) {
        this.eventData = eventData;
    }
}
