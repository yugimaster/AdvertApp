package com.grandartisans.advert.model.entity;

public class ImagePlayInfo {

    private int currentIndex;
    private int imageListSize;

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setImageListSize(int imageListSize) {
        this.imageListSize = imageListSize;
    }

    public int getImageListSize() {
        return imageListSize;
    }
}
