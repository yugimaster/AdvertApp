package com.grandartisans.advert.model.entity.post;

public class PlayerStatusParameter {
    boolean playerHandler;
    boolean isPlaying;
    boolean surfaceDestroyedFlag;
    float gSensorDefaultValue;
    int gtfminiDefaultValue;
    int surfaceDestroyedCount;
    int mediaplayerDestroyedCount;
    int menukeyPressedCount;

    public int getSurfaceDestroyedCount() {
        return surfaceDestroyedCount;
    }

    public void setSurfaceDestroyedCount(int surfaceDestroyedCount) {
        this.surfaceDestroyedCount = surfaceDestroyedCount;
    }

    public int getMediaplayerDestroyedCount() {
        return mediaplayerDestroyedCount;
    }

    public void setMediaplayerDestroyedCount(int mediaplayerDestroyedCount) {
        this.mediaplayerDestroyedCount = mediaplayerDestroyedCount;
    }

    public int getMenukeyPressedCount() {
        return menukeyPressedCount;
    }

    public void setMenukeyPressedCount(int menukeyPressedCount) {
        this.menukeyPressedCount = menukeyPressedCount;
    }

    public float getgSensorDefaultValue() {
        return gSensorDefaultValue;
    }

    public void setgSensorDefaultValue(float gSensorDefaultValue) {
        this.gSensorDefaultValue = gSensorDefaultValue;
    }

    public int getGtfminiDefaultValue() {
        return gtfminiDefaultValue;
    }

    public void setGtfminiDefaultValue(int gtfminiDefaultValue) {
        this.gtfminiDefaultValue = gtfminiDefaultValue;
    }

    public boolean isSurfaceDestroyedFlag() {
        return surfaceDestroyedFlag;
    }

    public void setSurfaceDestroyedFlag(boolean surfaceDestroyedFlag) {
        this.surfaceDestroyedFlag = surfaceDestroyedFlag;
    }

    public boolean isPlayerHandler() {
        return playerHandler;
    }

    public void setPlayerHandler(boolean playerHandler) {
        this.playerHandler = playerHandler;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }
}
