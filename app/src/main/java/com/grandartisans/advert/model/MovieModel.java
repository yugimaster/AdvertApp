package com.grandartisans.advert.model;

import com.grandartisans.advert.model.http.MovieApiService;
import com.grandartisans.advert.model.imodel.IMovieMoel;
import com.ljy.devring.DevRing;

import io.reactivex.Observable;

/**
 * author:  ljy
 * date:    2017/9/27
 * description:  豆瓣电影的model层。进行相关的数据处理与提供
 */

public class MovieModel implements IMovieMoel {

    /**
     * 获取正在上映的电影
     *
     * @param start            请求的起始点
     * @param count            获取的电影数量
     */
    @Override
    public Observable getPlayingMovie(int start,int count) {
        return DevRing.httpManager().getService(MovieApiService.class).getPlayingMovie(start, count);
    }

    /**
     * 获取即将上映的电影
     *
     * @param start            请求的起始点
     * @param count            获取的电影数量
     */
    @Override
    public Observable getCommingMovie(int start,int count) {
        return DevRing.httpManager().getService(MovieApiService.class).getCommingMovie(start, count);
    }
}
