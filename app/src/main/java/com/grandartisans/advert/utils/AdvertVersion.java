package com.grandartisans.advert.utils;

import com.ljy.devring.DevRing;

public class AdvertVersion {
    public static int  getAdVersion(int id) {
        int version = -1;
        int positionId = DevRing.cacheManager().spCache("advertVersion").getInt("advertPositionId",-1);
        if(positionId == id ) {
            version = DevRing.cacheManager().spCache("advertVersion").getInt("version",-1);
        }
        return version;
    }

    public static void  setAdVersion(int id,int version) {
        DevRing.cacheManager().spCache("advertVersion").put("advertPositionId",id);
        DevRing.cacheManager().spCache("advertVersion").put("version",version);
    }

    public static int  getAdPositionId() {
        int positionId = DevRing.cacheManager().spCache("advertVersion").getInt("advertPositionId",-1);
        return positionId;
    }
}
