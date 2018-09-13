package com.grandartisans.advert.common;

import android.content.Context;

import com.grandartisans.advert.model.entity.post.AdvertParameter;
import com.grandartisans.advert.model.entity.post.TokenParameter;
import com.grandartisans.advert.model.entity.post.UserAgent;
import com.grandartisans.advert.utils.CommonUtil;
import com.grandartisans.advert.utils.EncryptUtil;
import com.grandartisans.advert.utils.SystemInfoManager;
import com.grandartisans.advert.utils.Utils;

import java.io.File;

public class Common {

    /**
     * 获取token api的参数
     *
     * @param context
     * @param signed
     * @return
     */
    public static TokenParameter getTokenParams(Context context, String signed) {
        TokenParameter tokenParameter = new TokenParameter();
        tokenParameter.setDeviceClientid(SystemInfoManager.readFromNandkey("usid")
                .toUpperCase());
        tokenParameter.setTimestamp(System.currentTimeMillis());

        UserAgent userAgent = new UserAgent();
        userAgent.setAppVersionName(Utils.getAppVersionName(context));
        userAgent.setPlatformVersion(CommonUtil.getVersionInfo());

        tokenParameter.setUserAgent(userAgent);

        StringBuilder sign = new StringBuilder();
        EncryptUtil encryptUtil = new EncryptUtil();
        sign.append(tokenParameter.getDeviceClientid())
                .append("$")
                .append(tokenParameter.getTimestamp())
                .append("$123456");
        signed = encryptUtil.MD5Encode(sign.toString(), "");
        tokenParameter.setSign(signed);

        return tokenParameter;
    }

    /**
     * 获取schedule times api的参数
     *
     * @param token
     * @return
     */
    public static AdvertParameter getAdvertParams(String token) {
        AdvertParameter advertParameter = new AdvertParameter();
        advertParameter.setDeviceClientid(SystemInfoManager.readFromNandkey("usid")
                .toUpperCase());
        advertParameter.setRequestUuid(CommonUtil.getRandomString(50));
        advertParameter.setTimestamp(System.currentTimeMillis());
        advertParameter.setToken(token);

        return advertParameter;
    }

    /**
     * 检查文件夹是否存在 不存在则创建它
     *
     * @param filePath
     */
    public static void checkFileDir(final String filePath) {
        File file = new File(filePath);
        if (!file.exists() && !file.isDirectory()) {
            file.mkdir();
            System.out.println(filePath + " 不存在 创建该目录");
        } else {
            System.out.println(filePath + " 已存在");
        }
    }

    /**
     * 获取网络文件的类型
     *
     * @param url
     * @return
     */
    public static String getFileType(String url) {
        String file_type = "unknown";
        if (url != null) {
            if (url.toLowerCase().endsWith("jpg")) {
                file_type = ".jpg";
            } else if (url.toLowerCase().endsWith("png")) {
                file_type = ".png";
            } else if (url.toLowerCase().endsWith("mp4")) {
                file_type = ".mp4";
            } else if (url.toLowerCase().endsWith("gif")) {
                file_type = ".gif";
            }
        }
        return file_type;
    }
}
