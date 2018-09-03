package com.grandartisans.advert.common;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.grandartisans.advert.model.entity.res.TerminalAdvertPackageVo;
import com.ljy.devring.DevRing;

public class ScheduleTimesCache {

    /**
     * 更新排期模板的缓存
     *
     * @param terminalAdvertPackageVo
     */
    public static void update(TerminalAdvertPackageVo terminalAdvertPackageVo) {
        Gson gson = new Gson();
        TerminalAdvertPackageVo oldTadpvo = get();
        String json_cache = gson.toJson(oldTadpvo);
        String json_query = gson.toJson(terminalAdvertPackageVo);
        if (oldTadpvo == null) {
            set(json_query);
        } else {
            long oldTemplateId = oldTadpvo.getTemplate().getTemplate().getId();
            long newTemplateId = terminalAdvertPackageVo.getTemplate().getTemplate().getId();
            if (oldTemplateId != newTemplateId) {
                set(json_query);
            }
        }
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
            return null;
        } else {
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
        DevRing.cacheManager().diskCache("ScheduleTimes").put("data", json_data);
    }
}
