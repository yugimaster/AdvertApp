package com.grandartisans.advert.model.entity;

import java.util.HashMap;
import java.util.Map;

public class TerminalGroup {
    private String groupId;;
    private String[] arrTerminal;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String[] getArrTerminal() {
        return arrTerminal;
    }

    public void setArrTerminal(String[] arrTerminal) {
        this.arrTerminal = arrTerminal;
    }
}
