package com.grandartisans.advert.common;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.grandartisans.advert.model.entity.res.TerminalAdvertPackageVo;
import com.ljy.devring.DevRing;
import com.ljy.devring.other.RingLog;

public class ScheduleTimesCache {

    /**
     * 更新排期模板的缓存
     *
     * @param terminalAdvertPackageVo
     */
    public static void update(TerminalAdvertPackageVo terminalAdvertPackageVo) {
        Gson gson = new Gson();
        String json_query = gson.toJson(terminalAdvertPackageVo);
        set(json_query);

    }

    /**
     * 获取排期模板数据
     *
     * @return
     */
    public static TerminalAdvertPackageVo get() {
        Gson gson = new Gson();
        String str_cache = DevRing.cacheManager()
                .diskCache("ScheduleTimes")
                .getString("data");
        if (str_cache == null || str_cache.isEmpty()) {
            RingLog.d("ScheduleTimesCache", "cached schedule is null");
            return null;
        } else {
            RingLog.d("ScheduleTimesCache", "cached schedule is :" + str_cache);
            TerminalAdvertPackageVo terminalAdvertPackageVo = gson.fromJson(str_cache,
                    new TypeToken<TerminalAdvertPackageVo>() {}.getType());
            return terminalAdvertPackageVo;
        }
    }

    /**
     * 设置排期模板的缓存
     *
     * @param json_data
     */
    public static void set(String json_data) {
        RingLog.d("ScheduleTimesCache", "save  schedule  cache :" + json_data);
        DevRing.cacheManager().diskCache("ScheduleTimes").put("data", json_data);
    }
}
